package com.hayden.fileservice.filesource.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NumberEncoder {
    public static byte[] encodeNumber(long number) {
        byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            // Get the rightmost 8 bits of the number
            bytes[i] = (byte) (number & 0xFF);
            // Shift the number right by 8 bits
            number >>>= 8;
        }
        return bytes;
    }

    public static long decodeNumber(byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Input byte array must be 16 bytes long");
        }
        long number = 0;
        for (int i = 0; i < 16; i++) {
            // Shift the current number left by 8 bits
            number <<= 8;
            // Set the rightmost 8 bits with the current byte value
            number |= (bytes[i] & 0xFF);
        }
        return number;
    }
}
