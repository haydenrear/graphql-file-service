package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.codegen.types.FileChangeType;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.filesource.FileHelpers;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode.DataNode;
import com.hayden.fileservice.filesource.util.NumberEncoder;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.ByteUtility;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.map.ResultCollectors;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
public enum HeaderOperationTypes {
    ADD("a"),
    SKIP("s");

    final String operation;

    HeaderOperationTypes(String operation) {
        this.operation = operation;
    }

    public static Result<FileMetadata, FileEventSourceActions.FileEventError> flushHeader(File file, DataNode.FileEventHeaderResult headerResult) {
        return FileHelpers.writeToFile(headerResult.responses(), file, FileChangeType.CREATED);
    }

    public static Result<DataNode.FileEventHeaderResult, FileEventSourceActions.FileEventError> writeHeader(
            FileHeader.HeaderDescriptor headerDescriptor,
            FileProperties fileProperties
    ) {
        byte[] header = new byte[Math.toIntExact(fileProperties.getDataStreamFileHeaderLengthBytes())];
        int i = 0;
        Collection<Result<DataNode.FileEventHeaderResult, FileEventSourceActions.FileEventError>> errors
                = new ArrayList<>();
        int count = 0;
        for (DataNode d : headerDescriptor.inIndices()) {
            int j = i;
            byte[] nextToWrite = new byte[
                    (DataStreamFileDelim.INTRA_HEADER_DESCRIPTORS_DELIM.fileDelims + HeaderOperationTypes.ADD.operation).getBytes().length
                    + (DataStreamFileDelim.INTER_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes().length * 4)
                            + (16 * 4)];
            switch (d)  {
                case DataNode.AddNode a -> j = writeNode(a, nextToWrite, j, HeaderOperationTypes.ADD.operation);
                case DataNode.SkipNode skip -> j = writeNode(skip, nextToWrite, j, HeaderOperationTypes.SKIP.operation);
                default -> errors.add(Result.err(new FileEventSourceActions.FileEventError("Could not write %s".formatted(d))));
            }
            System.arraycopy(nextToWrite,0, header, i, nextToWrite.length);
            i = j;
            if (count < headerDescriptor.inIndices().size()) {
                byte[] delim = DataStreamFileDelim.BETWEEN_HEADER_OPS.fileDelims.getBytes();
                System.arraycopy(delim, 0, header, i, delim.length);
                i += delim.length;
            }
            count += 1;
        }

        byte[] emptyHeaderSpace = new byte[(int) fileProperties.getDataStreamFileHeaderLengthBytes() - count];
        Arrays.fill(emptyHeaderSpace, (byte) 0);
        System.arraycopy(emptyHeaderSpace, 0, header, count, emptyHeaderSpace.length);

        return Result.all(
                errors,
                Result.from(new DataNode.FileEventHeaderResult(header), new FileEventSourceActions.FileEventError())
        );
    }

    private static int writeNode(DataNode a, byte[] nextToWrite, int i, String nodeType) {
        int startPosCounter = 0;
        byte[] add = (DataStreamFileDelim.INTRA_HEADER_DESCRIPTORS_DELIM.fileDelims + nodeType).getBytes();
        System.arraycopy(add, 0, nextToWrite, 0, add.length);
        startPosCounter += add.length;
        byte[] startIndex = NumberEncoder.encodeNumber(a.indexStart());
        System.arraycopy(startIndex, 0, nextToWrite, startPosCounter, startIndex.length);
        startPosCounter += 16;
        byte[] delim = DataStreamFileDelim.INTER_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes();
        System.arraycopy(delim, 0, nextToWrite, startPosCounter, delim.length);
        startPosCounter += delim.length;
        byte[] endIndex = NumberEncoder.encodeNumber(a.indexEnd());
        System.arraycopy(endIndex, 0, nextToWrite, startPosCounter, endIndex.length);
        startPosCounter += 16;
        System.arraycopy(delim, 0, nextToWrite, startPosCounter, delim.length);
        startPosCounter += delim.length;
        byte[] dataStart = NumberEncoder.encodeNumber(a.dataStart());
        System.arraycopy(dataStart, 0, nextToWrite, startPosCounter, dataStart.length);
        startPosCounter += 16;
        System.arraycopy(delim, 0, nextToWrite, startPosCounter, delim.length);
        startPosCounter += delim.length;
        byte[] dataEnd = NumberEncoder.encodeNumber(a.dataEnd());
        System.arraycopy(dataEnd, 0, nextToWrite, startPosCounter, dataEnd.length);
        startPosCounter += 16;
        i += startPosCounter;
        return i;
    }


    public static @NotNull Result<FileHeader.HeaderDescriptor, FileEventSourceActions.FileEventError> getOps(byte[] header) {
        byte[] bytes = DataStreamFileDelim.BETWEEN_HEADER_OPS.fileDelims.getBytes();
        try {
            List<byte[]> eachHeaderOp = ByteUtility.splitByteArrayByByteValue(bytes, header);
            AtomicLong max = new AtomicLong(header.length);
            return eachHeaderOp.stream()
                    .flatMap(eachHeader -> {
                        byte[] starting = DataStreamFileDelim.INTRA_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes();
                        try {
                            List<byte[]> headerIndices = ByteUtility.splitByteArrayByByteValue(starting, eachHeader);
                            if (headerIndices.isEmpty()) {
                                return Stream.empty();
                            }
                            byte[] first = headerIndices.getFirst();
                            return Stream.concat(
                                    createFrom(HeaderOperationTypes.ADD.operation, first, DataNode.AddNode::new, max)
                                            .map(d -> Result.<FileHeader.HeaderDescriptor, FileEventSourceActions.FileEventError>ok(new FileHeader.HeaderDescriptor(d))),
                                    createFrom(HeaderOperationTypes.SKIP.operation, first, DataNode.SkipNode::new, max)
                                            .map(d -> Result.<FileHeader.HeaderDescriptor, FileEventSourceActions.FileEventError>ok(new FileHeader.HeaderDescriptor(d)))
                            );
                        } catch (ArrayIndexOutOfBoundsException e) {
                            return Stream.of(Result.<FileHeader.HeaderDescriptor, FileEventSourceActions.FileEventError>err(
                                    new FileEventSourceActions.FileEventError("Could not parse %s".formatted(e))));
                        }
                    })
                    .collect(ResultCollectors.from(
                            new FileHeader.HeaderDescriptor(),
                            new FileEventSourceActions.FileEventError()
                    ))
                    .flatMapResult(o -> Result.ok(new FileHeader.HeaderDescriptor(o.inIndices(), new FileHeader.HeaderDescriptorData(header.length, max.get()))));
        } catch (ArrayIndexOutOfBoundsException e) {
            return Result.err(new FileEventSourceActions.FileEventError(e));
        }
    }

    private static Stream<DataNode> createFrom(String nodeOp,
                                               byte[] headerValue,
                                               Function<DataNode.NodeArgs, DataNode> nodeFactory,
                                               AtomicLong max) {
        if (isAddOrRemove(nodeOp.getBytes(), headerValue)) {
            List<byte[]> split = ByteUtility.splitByteArrayByByteValue(nodeOp.getBytes(), headerValue);
            return split.stream()
                    .map(toSplit -> {
                        List<byte[]> eachIndex = ByteUtility.splitByteArrayByByteValue(DataStreamFileDelim.INTER_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes(), toSplit);
                        return nodeFactory.apply(
                                new DataNode.NodeArgs(
                                        NumberEncoder.decodeNumber(eachIndex.get(0)),
                                        NumberEncoder.decodeNumber(eachIndex.get(1)),
                                        NumberEncoder.decodeNumber(eachIndex.get(2)),
                                        getDataEnd(eachIndex, max),
                                        false
                                ));
                    });
        }

        return Stream.empty();
    }

    private static long getDataEnd(List<byte[]> eachIndex, AtomicLong max) {
        long nextDataEnd = NumberEncoder.decodeNumber(eachIndex.get(3));
        if (nextDataEnd > max.get()) {
            max.set(nextDataEnd);
        }

        return nextDataEnd;
    }

    private static boolean isAddOrRemove(byte[] starting, byte[] first) {
        int count = 0;
        for (int i = 0; i < starting.length; i++) {
            if (starting[count] != first[i]) {
                return false;
            }
            count += 1;
        }
        return true;
    }



}
