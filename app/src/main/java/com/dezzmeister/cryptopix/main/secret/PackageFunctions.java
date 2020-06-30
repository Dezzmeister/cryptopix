package com.dezzmeister.cryptopix.main.secret;

import com.dezzmeister.cryptopix.main.images.ImageData;

/**
 * Contains functions to extract secret data. Package handlers can use these functions; new functions
 * may be added to support newer encoding techniques.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class PackageFunctions {

    /**
     * Extract the hidden bytes from an array of ARGB pixels containing secret data. This function
     * will not perform any bounds checks.
     *
     * @param argbPixels 4-byte ARGB pixel array
     * @param numBytes number of bytes to extract
     * @param offset byte offset (starts extracting bytes from the given pixel offset)
     * @return extracted bytes
     */
    public static final byte[] extractBytes(final int[] argbPixels, final int numBytes, final int offset) {
        final byte[] out = new byte[numBytes];
        final int alphaMask = 0x03000000;
        final int redMask = 0x00030000;
        final int greenMask = 0x00000300;
        final int blueMask = 0x00000003;

        for (int i = offset; i < argbPixels.length; i++) {
            final int pixel = argbPixels[i];
            final int data =
                            (pixel & blueMask) |
                            ((pixel & greenMask) >>> 6) |
                            ((pixel & redMask) >>> 12) |
                            ((pixel & alphaMask) >>> 18);
            out[i] = (byte)data;
        }

        return out;
    }

    /**
     * Gets the Cryptopix version code of an image containing secret data.
     *
     * @param secret image with secret data
     * @return Cryptopix version code
     * @see Versions
     */
    public static final long versionCode(final ImageData secret) {
        if (secret.pixels.length < 8) {
            throw new IllegalArgumentException("Image has less than 8 pixels!");
        }

        final byte[] headerBytes = extractBytes(secret.pixels, 8, 0);
        return longFromBytes(headerBytes);
    }

    /**
     * Constructs an int from an array of 4 bytes. If the input array does not contain exactly
     * 4 bytes, an exception will be thrown. The byte at index 0 is the least significant byte.
     *
     * @param bytes input byte array
     * @return int containing bytes from input array
     */
    public static final int intFromBytes(final byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Byte array must have length 4!");
        }

        final int i0 = ((int) bytes[3]) & 0xFF;
        final int i1 = (((int) bytes[2]) & 0xFF) << 8;
        final int i2 = (((int) bytes[1]) & 0xFF) << 16;
        final int i3 = (((int) bytes[0]) & 0xFF) << 24;

        return (i3 | i2 | i1 | i0);
    }

    /**
     * Constructs a long from an array of 8 bytes. If the input array does not contain exactly
     * 8 bytes, an exception will be thrown. The byte at index 0 is the least significant byte.
     *
     * @param bytes input byte array
     * @return long containing bytes from input array
     */
    public static final long longFromBytes(final byte[] bytes) {
        if (bytes.length != 8) {
            throw new IllegalArgumentException("Byte array must have length 8!");
        }

        final long l0 = ((long) bytes[7]) & 0xFFL;
        final long l1 = (((long) bytes[6]) & 0xFFL) << 8;
        final long l2 = (((long) bytes[5]) & 0xFFL) << 16;
        final long l3 = (((long) bytes[4]) & 0xFFL) << 24;
        final long l4 = (((long) bytes[3]) & 0xFFL) << 32;
        final long l5 = (((long) bytes[2]) & 0xFFL) << 40;
        final long l6 = (((long) bytes[1]) & 0xFFL) << 48;
        final long l7 = (((long) bytes[0]) & 0xFFL) << 56;

        return (l7 | l6 | l5 | l4 | l3 | l2 | l1 | l0);

    }
}
