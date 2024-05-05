package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

public enum DataStreamBytes {
    NO_OP('|');

    final byte noOp;

    DataStreamBytes(char c) {
        this.noOp = (byte) c;
    }
}
