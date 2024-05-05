package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

public enum DataStreamFileDelim {
    /**
     * The delimiter between the header operations, goes between each header operation (including the data of the operation).
     */
    BETWEEN_HEADER_OPS(System.lineSeparator() + System.lineSeparator() + System.lineSeparator()),
    /**
     * The delimiter between the header operation name and the header operation.
     */
    SPLIT_HEADER_OPERATION_TY_DELIM("---"),
    /**
     * The delimiter between the header descriptor and the rest of the file.
     */
    INTRA_HEADER_DESCRIPTORS_DELIM("|||"),
    /**
     * The delimiter between the data in the header descriptor
     */
    INTER_HEADER_DESCRIPTORS_DELIM(":::");

    final String fileDelims;

    DataStreamFileDelim(String fileDelims) {
        this.fileDelims = fileDelims;
    }
}
