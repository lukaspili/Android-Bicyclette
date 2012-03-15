package com.siu.bicyclette.helper;

/**
 * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
 */
public final class ByteHelper {

    private ByteHelper() {
    }

    public static final int unsignedShortToInt(byte[] b, int index) {
        int i = 0;
        i |= b[index + 1] & 0xFF;
        i <<= 8;
        i |= b[index] & 0xFF;
        return i;
    }
}
