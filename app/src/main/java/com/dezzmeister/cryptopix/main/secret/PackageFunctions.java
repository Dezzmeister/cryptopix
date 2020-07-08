package com.dezzmeister.cryptopix.main.secret;

import android.provider.Settings;

import com.dezzmeister.cryptopix.main.images.ImageData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Contains functions to extract/hide secret data. Package handlers can use these functions; new functions
 * may be added to support newer encoding techniques.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class PackageFunctions {

    /**
     * Sequentially packs a list of byte-array fields into a single byte array. Can be used to construct
     * secret packages from several individual binary fields.
     *
     * @param fields binary fields
     * @return single byte array containing all fields
     */
    public static final byte[] packSequentialBinaryFields(final List<byte[]> fields) {
        int length = 0;

        for (final byte[] field : fields) {
            length += field.length;
        }

        final byte[] out = new byte[length];

        int index = 0;
        for (final byte[] field : fields) {
            System.arraycopy(field, 0, out, index, field.length);
            index += field.length;
        }

        return out;
    }

    /**
     * Decompresses some binary data using Java's {@link Inflater}.
     *
     * @param in data to decompress
     * @return decompressed data
     * @throws DataFormatException if the compressed data format is invalid
     * @throws IOException if there is a problem closing the ByteArrayOutputStream
     */
    public static final byte[] decompress(final byte[] in) throws DataFormatException, IOException {
        final Inflater inflater = new Inflater();
        inflater.setInput(in);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(in.length);

        byte[] buffer = new byte[1024];

        while (!inflater.finished()) {
            final int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        outputStream.close();

        return outputStream.toByteArray();
    }

    /**
     * Compresses some binary data using Java's {@link Deflater}.
     *
     * @param in data to compress
     * @return compressed data
     * @throws IOException if the ByteArrayOutputStream cannot be closed
     */
    public static byte[] compress(final byte[] in) throws IOException {
        final Deflater deflater = new Deflater();
        deflater.setInput(in);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(in.length);

        deflater.finish();
        byte[] buffer = new byte[1024];

        while (!deflater.finished()) {
            final int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        outputStream.close();

        return outputStream.toByteArray();
    }

    /**
     * Decrypts the given encrypted data using the given key and initialization vector. Uses AES with cipher block
     * chaining mode.
     *
     * @param data data to decrypt
     * @param keySpec key specification
     * @param ivSpec initialization vector specification
     * @return decrypted data
     * @throws NoSuchAlgorithmException if the encryption algorithm is not supported
     * @throws NoSuchPaddingException if the padding algorithm is not supported
     * @throws InvalidKeyException if the key is invalid
     * @throws InvalidAlgorithmParameterException if the given specs are not appropriate for the encryption algorithm
     * @throws IllegalBlockSizeException shouldn't be thrown, only thrown if no padding is used
     * @throws BadPaddingException if the encrypted data does not contain proper padding
     */
    public static final byte[] decrypt(final byte[] data, final SecretKeySpec keySpec, final IvParameterSpec ivSpec) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        final byte[] decrypted = cipher.doFinal(data);

        return decrypted;
    }

    /**
     * Encrypts the given data using the given key and initialization vector. Uses AES with cipher block
     * chaining mode.
     *
     * @param data data to encrypt
     * @param keySpec key specification
     * @param ivSpec initialization vector specification
     * @return encrypted data
     * @throws NoSuchAlgorithmException if the encryption algorithm is not supported
     * @throws NoSuchPaddingException if the padding algorithm is not supported
     * @throws InvalidKeyException if the key is invalid
     * @throws InvalidAlgorithmParameterException if the given specs are not appropriate for the encryption algorithm
     * @throws IllegalBlockSizeException shouldn't be thrown, only thrown if no padding is used
     * @throws BadPaddingException shouldn't be thrown, only thrown in decryption mode
     */
    public static final byte[] encrypt(final byte[] data, final SecretKeySpec keySpec, final IvParameterSpec ivSpec) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        final byte[] encrypted = cipher.doFinal(data);

        return encrypted;
    }

    /**
     * Generates a 16-byte initialization vector.
     *
     * @return 16-byte initialization vector
     */
    public static final IvParameterSpec generateIV() {
        final SecureRandom random = new SecureRandom();
        final byte[] bytes = new byte[16];

        random.nextBytes(bytes);

        return new IvParameterSpec(bytes);
    }

    /**
     * Generates a {@link SecretKeySpec} using PBKDF2 with the given iteration count.
     *
     * @param password password bytes
     * @param salt salt bytes
     * @param iterationCount number of PBKDF2 iterations
     * @return secret key
     * @throws NoSuchAlgorithmException if the algorithm does not exist on this device
     * @throws InvalidKeySpecException if the key spec cannot be generated
     */
    public static final SecretKeySpec generateKey(final byte[] password, final byte[] salt, final int iterationCount) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final char[] passwordChars = new char[password.length];

        for (int i = 0; i < password.length; i++) {
            passwordChars[i] = (char) password[i];
        }

        final PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, iterationCount, 256);
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        final byte[] keyBytes = keyFactory.generateSecret(spec).getEncoded();

        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Returns a random 32-byte salt.
     *
     * @return 32-byte salt
     */
    public static final byte[] generateSalt() {
        final SecureRandom random = new SecureRandom();
        final byte[] out = new byte[32];
        random.nextBytes(out);

        return out;
    }

    /**
     * Salts a password and hashes it using SHA-256. A 32-byte hash is returned.
     *
     * @param salt salt
     * @param password password
     * @return 32-byte SHA-256 hash
     * @throws NoSuchAlgorithmException if SHA-256 is not supported on this device
     */
    public static final byte[] saltAndHashPassword(final byte[] salt, final byte[] password) throws NoSuchAlgorithmException {
        final byte[] salted = new byte[salt.length + password.length];

        int saltIndex = 0;
        int passIndex = 0;
        for (int i = 0; i < salted.length; i++) {
            if (saltIndex < salt.length) {
                salted[i] = salt[saltIndex];
                saltIndex++;
            }

            if (passIndex < password.length) {
                salted[i] = password[passIndex];
                passIndex++;
            }
        }

        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] hash = digest.digest(salted);

        return hash;
    }

    /**
     * Checks a user-provided password against a known salt and hash to determine if the password is correct.
     * The password is salted and hashed, and the resultant hash is compared with the known hash.
     *
     * @param providedPassword password to test
     * @param salt known salt
     * @param hash known hash
     * @return true if <code>providedPassword</code> is correct
     * @throws NoSuchAlgorithmException if the hashing algorithm is not supported on this device
     */
    public static final boolean isCorrectPassword(final byte[] providedPassword, final byte[] salt, final byte[] hash) throws NoSuchAlgorithmException {
        final byte[] providedHash = saltAndHashPassword(salt, providedPassword);

        if (providedHash.length != hash.length) {
            throw new IllegalArgumentException("Known password hash is not the correct length!");
        }

        for (int i = 0; i < hash.length; i++) {
            if (providedHash[i] != hash[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Writes the given secret bytes to the pixel array, starting at the given offset.
     *
     * @param pixels 4-byte ARGB pixel array (will be modified)
     * @param bytes secret bytes
     * @param offset starting offset
     */
    public static final void writeBytes(int[] pixels, final byte[] bytes, final int offset) {
        for (int i = 0; i < bytes.length; i++) {
            final int index = i + offset;
            final int secret = ((int) bytes[i]) & 0xFF;

            final int alpha = (secret & 0xC0) << 18;
            final int red = (secret & 0x30) << 12;
            final int green = (secret & 0x0C) << 6;
            final int blue = secret & 0x03;

            pixels[index] |= (alpha | red | green | blue);
        }
    }

    /**
     * Extract the hidden bytes from an array of ARGB pixels containing secret data. This function
     * will not perform any bounds checks.
     *
     * @param argbPixels 4-byte ARGB pixel array
     * @param numBytes number of bytes to extract, or negative to extract all remaining bytes
     * @param offset byte offset (starts extracting bytes from the given pixel offset)
     * @return extracted bytes
     */
    public static final byte[] extractBytes(final int[] argbPixels, int numBytes, final int offset) {
        if (numBytes < 0) {
            numBytes = argbPixels.length - offset;
        }

        final byte[] out = new byte[numBytes];
        final int alphaMask = 0x03000000;
        final int redMask = 0x00030000;
        final int greenMask = 0x00000300;
        final int blueMask = 0x00000003;

        for (int i = offset; i < offset + numBytes; i++) {
            final int pixel = argbPixels[i];
            final int data =
                            (pixel & blueMask) |
                            ((pixel & greenMask) >>> 6) |
                            ((pixel & redMask) >>> 12) |
                            ((pixel & alphaMask) >>> 18);
            out[i - offset] = (byte)data;
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
     * Converts an int to an array of bytes. The byte array is indexed starting from the most
     * significant byte in the int.
     *
     * @param in int to convert to bytes
     * @return byte array (length 4)
     */
    public static final byte[] intToBytes(final int in) {
        final byte[] out = new byte[4];

        out[0] = (byte)((in & 0xFF000000) >>> 24);
        out[1] = (byte)((in & 0x00FF0000) >>> 16);
        out[2] = (byte)((in & 0x0000FF00) >>> 8);
        out[3] = (byte) (in & 0x000000FF);

        return out;
    }

    /**
     * Converts a long to an array of bytes. The byte array is indexed starting from the most
     * significant byte in the long.
     *
     * @param in long to convert to bytes
     * @return byte array (length 8)
     */
    public static final byte[] longToBytes(final long in) {
        final byte[] out = new byte[8];

        out[0] = (byte)((in & 0xFF00000000000000L) >>> 56);
        out[1] = (byte)((in & 0x00FF000000000000L) >>> 48);
        out[2] = (byte)((in & 0x0000FF0000000000L) >>> 40);
        out[3] = (byte)((in & 0x000000FF00000000L) >>> 32);
        out[4] = (byte)((in & 0x00000000FF000000L) >>> 24);
        out[5] = (byte)((in & 0x0000000000FF0000L) >>> 16);
        out[6] = (byte)((in & 0x000000000000FF00L) >>> 8);
        out[7] = (byte) (in & 0x00000000000000FFL);

        return out;
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
