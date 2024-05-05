package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.filesource.util.NumberEncoder;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.Optional;

@Slf4j
public enum HeaderOperationTypes {
    ADD("a"),
    SKIP("s");

    final String operation;

    HeaderOperationTypes(String operation) {
        this.operation = operation;
    }

    public static @NotNull Result<FileHeader.HeaderDescriptor, FileEventSourceActions.FileEventError> getOps(byte[] header) {
        String headerStr = new String(header, Charset.defaultCharset());
        String[] forDescriptor = headerStr.split("\\%s".formatted(DataStreamFileDelim.INTRA_HEADER_DESCRIPTORS_DELIM));
        if (Optional.ofNullable(forDescriptor).map(s -> s.length != 2).orElse(false)) {
            return Result.err(new FileEventSourceActions.FileEventError("Could not parse descriptor at beginning of file."));
        }
        String[] descriptorData = forDescriptor[0].split(DataStreamFileDelim.INTER_HEADER_DESCRIPTORS_DELIM.fileDelims);
        if (Optional.ofNullable(descriptorData).map(s -> s.length != 2).orElse(false)) {
            return Result.err(new FileEventSourceActions.FileEventError("Could not parse descriptor data at beginning of file."));
        }
        FileHeader.HeaderDescriptorData data = new FileHeader.HeaderDescriptorData(Long.parseLong(descriptorData[0]), Long.parseLong(descriptorData[1]));
        return null;
//        return Result.ok(
//                new SkipFileOperations.HeaderDescriptor(Arrays.stream(headerStr.split(DataStreamFileDelim.BETWEEN_HEADER_OPS.fileDelims))
//                        .flatMap(fileOp -> {
//                            String[] headerOp = fileOp.split(DataStreamFileDelim.SPLIT_HEADER_OPERATION_TY_DELIM.fileDelims);
//                            if (headerOp.length != 5 || Arrays.stream(headerOp).anyMatch(String::isBlank)) {
//                                return Stream.empty();
//                            }
//                            try {
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
