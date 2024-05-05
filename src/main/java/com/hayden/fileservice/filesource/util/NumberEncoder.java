package com.hayden.fileservice.filesource.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NumberEncoder {
    public static byte[] encodeNumber(long number) {
        byte[] bytes = new byte[16];
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (number & 0xFF);
            number >>= 8;
        }
        return bytes;
    }

    public static long decodeNumber(byte[] bytes) {
        long number = 0;
        for (int i = 0; i < bytes.length; i++) {
            number = (number << 8) + (bytes[bytes.length - i - 1] & 0xFF);
        }
        return number;
    }
}
