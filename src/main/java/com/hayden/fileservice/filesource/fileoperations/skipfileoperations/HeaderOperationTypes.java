package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.filesource.util.NumberEncoder;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public enum HeaderOperationTypes {
    ADD("a"),
    SKIP("s");

    final String operation;

    HeaderOperationTypes(String operation) {
        this.operation = operation;
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
    public static List<byte[]> splitByteArrayByByteValue(byte[] toSplitBy, byte[] toSplit) {
        return splitByteArrayByByteValue(toSplitBy, toSplit, true) ;
    }

    public static List<byte[]> splitByteArrayByByteValue(byte[] toSplitBy, byte[] toSplit, boolean include) {
        int counter = 0;
        int i = -1;
        List<byte[]> result = new ArrayList<>();
        while (counter < toSplit.length) {
            if (bytesMatch(toSplitBy, toSplit, counter)) {
                if (i == -1) {
                    i = counter;
                    if (counter != 0) {
                        byte[] out = new byte[counter];
                        System.arraycopy(toSplit, 0, out, 0, counter);
                        result.add(out);
                    }
                } else {
                    if (include) {
                        byte[] out = new byte[counter - i];
                        System.arraycopy(toSplit, i, out, 0, counter - i);
                        result.add(out);
                    } else {
                        byte[] out = new byte[counter - i - toSplitBy.length];
                        System.arraycopy(toSplit, i + toSplitBy.length , out, 0, counter - i - toSplitBy.length);
                        result.add(out);
                    }
                    i = counter;
                }
            }
            counter += 1;
        }

        if (bytesMatch(toSplitBy, toSplit, i)) {
            if (i != -1) {
                if (include) {
                    byte[] out = new byte[counter - i];
                    System.arraycopy(toSplit, i, out, 0, counter - i);
                    result.add(out);
                } else if (toSplitBy.length != toSplit.length) {
                    byte[] out = new byte[counter - i - toSplitBy.length];
                    System.arraycopy(toSplit, i + toSplitBy.length , out, 0, counter - i - toSplitBy.length );
                    result.add(out);
                }
            }
        }

        return result;
    }

    private static boolean bytesMatch(byte[] toSplitBy, byte[] toSplit, int counter) {
        for (int j = 0; j < toSplitBy.length; j++) {
            if (toSplit[counter + j] != toSplitBy[j]) return false;
        }
        return true;
    }

    public static @NotNull Result<FileHeader.HeaderDescriptor, FileEventSourceActions.FileEventError> getOps(byte[] header) {
        byte[] bytes = DataStreamFileDelim.BETWEEN_HEADER_OPS.fileDelims.getBytes();
        List<byte[]> eachHeaderOp = splitByteArrayByByteValue(bytes, header, false);
        eachHeaderOp.forEach(eachHeader -> {
            byte[] starting = DataStreamFileDelim.INTRA_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes();
            List<byte[]> headerIndices = splitByteArrayByByteValue(starting, eachHeader, false);
            if (headerIndices.size() == 0) {
                return;
            }
            byte[] first = headerIndices.get(0);
            if(isAddOrRemove("a".getBytes(), first)) {
                List<byte[]> split = splitByteArrayByByteValue("a".getBytes(), first, false);
                split.forEach(toSplit -> {
                    List<byte[]> eachIndex = splitByteArrayByByteValue(DataStreamFileDelim.INTER_HEADER_DESCRIPTORS_DELIM.fileDelims.getBytes(), toSplit, false);
                    eachIndex.forEach(item -> {
                        long l = NumberEncoder.decodeNumber(item);
                    });
                });

            }
            if(isAddOrRemove("s".getBytes(), first)) {
                List<byte[]> split = splitByteArrayByByteValue("s".getBytes(), first, false);
            }
        });

//        String[] descriptorData = forDescriptor[0].split(DataStreamFileDelim.INTER_HEADER_DESCRIPTORS_DELIM.fileDelims);
//        if (Optional.of(descriptorData).map(s -> s.length != 2).orElse(false)) {
//            return Result.err(new FileEventSourceActions.FileEventError("Could not parse descriptor data at beginning of file."));
//        }
//        FileHeader.HeaderDescriptorData data = new FileHeader.HeaderDescriptorData(Long.parseLong(descriptorData[0]), Long.parseLong(descriptorData[1]));
        return null;
//        return Result.ok(
//                new FileHeader.HeaderDescriptor(Arrays.stream(headerStr.split(DataStreamFileDelim.BETWEEN_HEADER_OPS.fileDelims))
//                        .flatMap(fileOp -> {
//                            String[] headerOp = fileOp.split(DataStreamFileDelim.SPLIT_HEADER_OPERATION_TY_DELIM.fileDelims);
//                            if (headerOp.length != 5 || Arrays.stream(headerOp).anyMatch(String::isBlank)) {
//                                return Stream.empty();
//                            }
//                            try {
//
//                                SkipFileOperations.HeaderOpIndices getIndices = new SkipFileOperations.HeaderOpIndices(
//                                        parsHeaderOp(headerOp[0]),
//                                        parsHeaderOp(headerOp[2]),
//                                        parsHeaderOp(headerOp[3]),
//                                        parsHeaderOp(headerOp[4])
//                                );
//                                return Stream.of(Map.entry(HeaderOperationTypes.valueOf(headerOp[0]), getIndices));
//                            } catch (
//                                    ClassCastException |
//                                    IllegalArgumentException c) {
//                                log.error("Error when attempting to parse header operation: {}, {}.", Arrays.toString(headerOp), c.getMessage());
//                                return Stream.empty();
//                            }
//                        })
//                        .sorted(Comparator.comparing(e -> e.getValue().indexStart()))
//                        .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList()))),
//                        data
//                ));

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
