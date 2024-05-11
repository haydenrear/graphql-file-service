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
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
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
        return FileHelpers.createFile(headerResult.responses(), file, FileChangeType.CREATED);
    }

    public static Result<DataNode.FileEventHeaderResult, FileEventSourceActions.FileEventError> writeHeader(
            FileHeader.HeaderDescriptor headerDescriptor,
            FileProperties fileProperties
    ) {
        byte[] headerSize = new byte[Math.toIntExact(fileProperties.getDataStreamFileHeaderLengthBytes())];
        int i = 0;
        Collection<Result<DataNode.FileEventHeaderResult, FileEventSourceActions.FileEventError>> errors
                = new ArrayList<>();
        int count = 0;
        for (DataNode d : headerDescriptor.inIndices()) {
            int j = i;
            byte[] nextToWrite = new byte[
                    (DataStreamFileDelim.INTRA_HEADER_DESCRIPTORS_DELIM.fileDelims + "a").getBytes().length
                    + (DataStreamFileDelim.INTER_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes().length * 4)
                            + (16 * 4)];
            switch (d)  {
                case DataNode.AddNode a -> j = writeNode(a, nextToWrite, j, "a");
                case DataNode.SkipNode skip -> j = writeNode(skip, nextToWrite, j, "s");
                default -> errors.add(Result.err(new FileEventSourceActions.FileEventError("Could not write %s".formatted(d))));
            };
            System.arraycopy(nextToWrite,0, headerSize, i, nextToWrite.length);
            i = j;
            if (count < headerDescriptor.inIndices().size()) {
                byte[] delim = DataStreamFileDelim.BETWEEN_HEADER_OPS.fileDelims.getBytes();
                System.arraycopy(delim, 0, headerSize, i, delim.length);
                i += delim.length;
            }
            count += 1;
        }

        return Result.all(
                errors,
                Result.from(new DataNode.FileEventHeaderResult(headerSize), new FileEventSourceActions.FileEventError())
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
            List<byte[]> eachHeaderOp = ByteUtility.splitByteArrayByByteValue(bytes, header, false);
            AtomicLong max = new AtomicLong(header.length);
            List<DataNode> out = eachHeaderOp.stream()
                    .flatMap(eachHeader -> {
                        byte[] starting = DataStreamFileDelim.INTRA_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes();
                        try {
                            List<byte[]> headerIndices = ByteUtility.splitByteArrayByByteValue(starting, eachHeader, false);
                            if (headerIndices.isEmpty()) {
                                return Stream.<DataNode>empty();
                            }
                            byte[] first = headerIndices.get(0);
                            if (isAddOrRemove("a".getBytes(), first)) {
                                List<byte[]> split = ByteUtility.splitByteArrayByByteValue("a".getBytes(), first, false);
                                return split.stream().map(toSplit -> {
                                    List<byte[]> eachIndex = ByteUtility.splitByteArrayByByteValue(DataStreamFileDelim.INTER_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes(), toSplit, false);
                                    return new DataNode.AddNode(
                                            NumberEncoder.decodeNumber(eachIndex.get(0)),
                                            NumberEncoder.decodeNumber(eachIndex.get(1)),
                                            NumberEncoder.decodeNumber(eachIndex.get(2)),
                                            getDataEnd(eachIndex, max),
                                            false
                                    );
                                });
                            }
                            if (isAddOrRemove("s".getBytes(), first)) {
                                List<byte[]> split = ByteUtility.splitByteArrayByByteValue("s".getBytes(), first, false);
                                return split.stream().map(toSplit -> {
                                    List<byte[]> eachIndex = ByteUtility.splitByteArrayByByteValue(DataStreamFileDelim.INTER_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes(), toSplit, false);
                                    return new DataNode.SkipNode(
                                            NumberEncoder.decodeNumber(eachIndex.get(0)),
                                            NumberEncoder.decodeNumber(eachIndex.get(1)),
                                            NumberEncoder.decodeNumber(eachIndex.get(2)),
                                            getDataEnd(eachIndex, max),
                                            false
                                    );
                                });
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            return Stream.empty();
                        }
                        return Stream.empty();
                    })
                    .toList();

            return Result.ok(new FileHeader.HeaderDescriptor(out, new FileHeader.HeaderDescriptorData(header.length, max.get())));
        } catch (ArrayIndexOutOfBoundsException e) {
            return Result.err(new FileEventSourceActions.FileEventError(e));
        }
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

    private static long parsHeaderOp(String headerOp) {
        return stripNoOp(headerOp);
    }

    private static long stripNoOp(String value) {
        var b = value.getBytes();
        var outBytes = new byte[b.length];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] != DataStreamBytes.NO_OP.noOp) {
                outBytes[i] = b[i];
            } else {
                break;
            }
            j = i;
        }
        var outB = new byte[j];
        System.arraycopy(outBytes, 0, outB, 0, j);
        return NumberEncoder.decodeNumber(outB);
    }

}
