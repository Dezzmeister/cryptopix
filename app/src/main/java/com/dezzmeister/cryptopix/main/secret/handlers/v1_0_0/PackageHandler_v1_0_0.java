package com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.DataFormatException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.dezzmeister.cryptopix.main.exceptions.SizeLimitExceededException;
import com.dezzmeister.cryptopix.main.images.ImageData;
import com.dezzmeister.cryptopix.main.secret.EncodedImageState;
import com.dezzmeister.cryptopix.main.secret.EncodingOptions;
import com.dezzmeister.cryptopix.main.secret.PackageFunctions;
import com.dezzmeister.cryptopix.main.secret.PackageHandler;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;
import com.dezzmeister.cryptopix.main.secret.Payload;

/**
 * Package handler for files generated with Cryptopix 1.0.0. The static members give the byte-length of
 * package header fields. In Cryptopix 1.0.0, 1 byte of package data corresponds to 1 pixel (4 bytes
 * of image data).
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class PackageHandler_v1_0_0 implements PackageHandler {

    /**
     * Cryptopix version code length (8-byte field). The version code should be
     * {@link com.dezzmeister.cryptopix.main.secret.Versions#VERSION_1_0_0 Versions.VERSION_1_0_0}.
     */
    private static final int VERSION_CODE_LENGTH = 8;

    /**
     * Number of bytes in the payload hash (128 bit MD5)
     */
    private static final int PAYLOAD_HASH_LENGTH = 16;

    /**
     * Number of bytes in the payload (4-byte field)
     */
    private static final int PAYLOAD_SIZE_LENGTH = 4;

    /**
     * Number of bytes determining various boolean values (1-byte field)
     *
     * The bits of this field determine the following values (starting from least significant):
     * <li>
     *     <ol>Whether a password is present</ol>
     *     <ol>Whether the data is compressed</ol>
     * </li>
     */
    private static final int BOOLEAN_FLAGS_LENGTH = 1;

    /**
     * Number of bytes in the optional salt (32-byte field)
     */
    private static final int SALT_LENGTH = 32;

    /**
     * Number of bytes in the optional initialization vector (16-byte field)
     */
    private static final int INIT_VECTOR_LENGTH = 16;

    /**
     * Number of bytes in the optional password hash (32-byte SHA-256 field)
     */
    private static final int HASH_LENGTH = 32;

    /**
     * Minimum byte-length of a package header, if no password is used
     */
    private static final int MIN_PACKAGE_HEADER_LENGTH = VERSION_CODE_LENGTH + PAYLOAD_HASH_LENGTH + PAYLOAD_SIZE_LENGTH + BOOLEAN_FLAGS_LENGTH;

    /**
     * Maximum byte-length of a package header, if a password is used
     */
    private static final int MAX_PACKAGE_HEADER_LENGTH = MIN_PACKAGE_HEADER_LENGTH + SALT_LENGTH + INIT_VECTOR_LENGTH + HASH_LENGTH;

    /**
     * The number of PBKDF2 iterations to perform when encrypting/decrypting data (not a binary field)
     */
    private static final int PBKDF2_ITERATIONS = 5000;

    @Override
    public EncodedImageState getImageState(final ImageData secret, final PackageHeader header) {
        final MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

            return EncodedImageState.UNSUPPORTED;
        }

        final PackageData_v1_0_0 packageHeader;

        if (!(header instanceof PackageData_v1_0_0)) {
            packageHeader = (PackageData_v1_0_0) extractHeader(secret);
        } else {
            packageHeader = (PackageData_v1_0_0) header;
        }

        // The payload data starts at packageHeader.dataOffset
        final byte[] data = PackageFunctions.extractBytes(secret.pixels, packageHeader.payloadSize, packageHeader.dataOffset);

        final byte[] realHash = md5.digest(data);

        if (Arrays.equals(realHash, packageHeader.payloadHash)) {
            if (packageHeader.hasPassword) {
                return EncodedImageState.SECRET_PASSWORD;
            } else {
                return EncodedImageState.SECRET_NO_PASSWORD;
            }
        } else {
            return EncodedImageState.CORRUPTED;
        }
    }

    @Override
    public boolean passwordProtected(final ImageData secret, final PackageHeader data) {
        if (data != null && data instanceof PackageData_v1_0_0) {
            return ((PackageData_v1_0_0) data).hasPassword;
        }

        final PackageData_v1_0_0 packageData = (PackageData_v1_0_0) extractHeader(secret);
        return packageData.hasPassword;
    }

    @Override
    public PackageHeader extractHeader(final ImageData secret) {
        final int[] pixels = secret.pixels;

        if (pixels.length < MIN_PACKAGE_HEADER_LENGTH) {
            throw new IllegalArgumentException("Image is too small!");
        }

        int seek = 0;
        final long versionCode = PackageFunctions.versionCode(secret);

        seek = 8;
        final byte[] payloadHash = PackageFunctions.extractBytes(pixels, PAYLOAD_HASH_LENGTH, seek);

        seek += PAYLOAD_HASH_LENGTH;
        final byte[] payloadSizeBytes = PackageFunctions.extractBytes(pixels, PAYLOAD_SIZE_LENGTH, seek);
        final int payloadSize = PackageFunctions.intFromBytes(payloadSizeBytes);

        seek += PAYLOAD_SIZE_LENGTH;
        final byte[] passwordFlagBytes = PackageFunctions.extractBytes(pixels, BOOLEAN_FLAGS_LENGTH, seek);

        final int flags = (((int) passwordFlagBytes[0]) & 0xFF);
        final boolean passwordFlag = (flags & 0x01) == 1;
        final boolean compressedFlag = ((flags & 0x02) >>> 1) == 1;

        seek += BOOLEAN_FLAGS_LENGTH;

        if (passwordFlag && pixels.length < MAX_PACKAGE_HEADER_LENGTH) {
            throw new IllegalArgumentException("Image is too small!");
        }

        final byte[] salt;
        final byte[] initVector;
        final byte[] passwordHash;

        if (passwordFlag) {
            salt = PackageFunctions.extractBytes(pixels, SALT_LENGTH, seek);

            seek += SALT_LENGTH;
            initVector = PackageFunctions.extractBytes(pixels, INIT_VECTOR_LENGTH, seek);

            seek += INIT_VECTOR_LENGTH;
            passwordHash = PackageFunctions.extractBytes(pixels, HASH_LENGTH, seek);

            seek += HASH_LENGTH;
        } else {
            salt = null;
            initVector = null;
            passwordHash = null;
        }

        final PackageData_v1_0_0 data = new PackageData_v1_0_0();
        data.cryptopixVersionCode = versionCode;
        data.payloadHash = payloadHash;
        data.initVector = initVector;
        data.payloadSize = payloadSize;
        data.hasPassword = passwordFlag;
        data.compressed = compressedFlag;
        data.salt = salt;
        data.passwordHash = passwordHash;
        data.dataOffset = seek;

        return data;
    }

    // Payload header field sizes

    /**
     * The filename size field (4-byte field). An ASCII string containing the name of the file follows this field,
     * and this field gives the length of the string.
     */
    private static final int PAYLOAD_FILENAME_SIZE = 4;

    /**
     * The MIME type size field (4-byte field). An ASCII string containing the MIME type of the file follows this field,
     * and this field gives the length of the string.
     */
    private static final int PAYLOAD_MIMETYPE_SIZE = 4;

    @Override
    public ImageData encodeSecret(final ImageData original, final Payload secretData, final EncodingOptions options) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, SizeLimitExceededException {
        /**
         * 1. Compress data (if compression option specified)
         * 2. Generate key spec and iv spec (if password supplied)
         * 3. Encrypt data (if password supplied)
         * 4. Generate payload
         * 5. Generate package header
         * 6. Add payload to package
         * 7. Pack entire package into a byte array
         * 8. Stuff the byte array in the image
         */
        byte[] fileData = secretData.data();

        if (options.compress) {
            fileData = PackageFunctions.compress(fileData);
        }

        final ArrayList<byte[]> payloadFields = new ArrayList<byte[]>();
        final byte[] fileNameSizeField = PackageFunctions.intToBytes(secretData.fileName().length());
        final byte[] fileName = secretData.fileName().getBytes(StandardCharsets.US_ASCII);
        final byte[] mimeTypeSizeField = PackageFunctions.intToBytes(secretData.mimeType().length());
        final byte[] mimeType = secretData.mimeType().getBytes(StandardCharsets.US_ASCII);

        payloadFields.add(fileNameSizeField);
        payloadFields.add(fileName);
        payloadFields.add(mimeTypeSizeField);
        payloadFields.add(mimeType);
        payloadFields.add(fileData);

        byte[] salt = null;
        byte[] passwordHash = null;
        SecretKeySpec keySpec = null;
        IvParameterSpec ivSpec = null;

        final byte[] payload;

        if (options.password != null) {
            final byte[] password = options.password.getBytes(StandardCharsets.US_ASCII);

            salt = PackageFunctions.generateSalt();
            passwordHash = PackageFunctions.saltAndHashPassword(salt, password);
            keySpec = PackageFunctions.generateKey(password, salt, PBKDF2_ITERATIONS);
            ivSpec = PackageFunctions.generateIV(INIT_VECTOR_LENGTH);

            final byte[] rawPayload = PackageFunctions.packSequentialBinaryFields(payloadFields);
            payload = PackageFunctions.encrypt(rawPayload, keySpec, ivSpec);
        } else {
            payload = PackageFunctions.packSequentialBinaryFields(payloadFields);
        }

        final MessageDigest md5 = MessageDigest.getInstance("MD5");
        final byte[] payloadHash = md5.digest(payload);

        final int passwordFlag = (options.password != null) ? 1 : 0;
        final int compressFlag = options.compress ? 1 : 0;
        final byte flags = (byte)(((compressFlag << 1) | passwordFlag) & 0xFF);

        final ArrayList<byte[]> packageFields = new ArrayList<byte[]>();
        final byte[] versionCodeField = PackageFunctions.longToBytes(options.versionCode);
        final byte[] payloadSizeField = PackageFunctions.intToBytes(payload.length);
        final byte[] flagsField = {flags};

        packageFields.add(versionCodeField);
        packageFields.add(payloadHash);
        packageFields.add(payloadSizeField);
        packageFields.add(flagsField);

        if (options.password != null) {
            packageFields.add(salt);
            packageFields.add(ivSpec.getIV());
            packageFields.add(passwordHash);
        }

        packageFields.add(payload);

        final byte[] completeSecretPackage = PackageFunctions.packSequentialBinaryFields(packageFields);

        final int[] out = new int[original.pixels.length];
        System.arraycopy(original.pixels, 0, out, 0, out.length);

        if (completeSecretPackage.length > original.pixels.length) {
            throw new SizeLimitExceededException("Secret package is larger than image!");
        }

        PackageFunctions.writeBytes(out, completeSecretPackage, 0);

        return new ImageData(out, original.width, original.height);
    }

    @Override
    public Payload decode(final ImageData secret, final PackageHeader header, final String password) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, DataFormatException, IOException {
        final PackageData_v1_0_0 packageData;

        if (header instanceof PackageData_v1_0_0) {
            packageData = (PackageData_v1_0_0) header;
        } else {
            throw new IllegalArgumentException("Wrong package header type!");
        }

        if (packageData.dataOffset + packageData.payloadSize > secret.pixels.length) {
            throw new IllegalArgumentException("Image is too small to contain specified payload!");
        }

        final byte[] rawPayload = PackageFunctions.extractBytes(secret.pixels, packageData.payloadSize, packageData.dataOffset);
        final byte[] unencryptedPayload;

        if (packageData.hasPassword) {
            final byte[] passwordBytes = password.getBytes(StandardCharsets.US_ASCII);

            if (!PackageFunctions.isCorrectPassword(passwordBytes, packageData.salt, packageData.passwordHash)) {
                throw new SecurityException("Invalid password!");
            }

            final IvParameterSpec ivSpec = PackageFunctions.createIV(packageData.initVector);
            final SecretKeySpec keySpec = PackageFunctions.generateKey(passwordBytes, packageData.salt, PBKDF2_ITERATIONS);

            unencryptedPayload = PackageFunctions.decrypt(rawPayload, keySpec, ivSpec);
        } else {
            unencryptedPayload = rawPayload;
        }

        int seek = 0;
        final byte[] fileNameSizeField = PackageFunctions.getSubarray(unencryptedPayload, PAYLOAD_FILENAME_SIZE, seek);
        final int fileNameSize = PackageFunctions.intFromBytes(fileNameSizeField);
        seek += PAYLOAD_FILENAME_SIZE;

        if (fileNameSize + seek > unencryptedPayload.length) {
            throw new IllegalArgumentException("Illegal file name size in payload header!");
        }

        final byte[] fileNameField = PackageFunctions.getSubarray(unencryptedPayload, fileNameSize, seek);
        final String fileName = new String(fileNameField, StandardCharsets.US_ASCII);
        seek += fileNameSize;

        final byte[] mimeTypeSizeField = PackageFunctions.getSubarray(unencryptedPayload, PAYLOAD_MIMETYPE_SIZE, seek);
        final int mimeTypeSize = PackageFunctions.intFromBytes(mimeTypeSizeField);
        seek += PAYLOAD_MIMETYPE_SIZE;

        if (mimeTypeSize + seek > unencryptedPayload.length) {
            throw new IllegalArgumentException("Illegal MIME type size in payload header!");
        }

        final byte[] mimeTypeField = PackageFunctions.getSubarray(unencryptedPayload, mimeTypeSize, seek);
        final String mimeType = new String(mimeTypeField, StandardCharsets.US_ASCII);
        seek += mimeTypeSize;

        final byte[] rawFileData = PackageFunctions.getSubarray(unencryptedPayload, -1, seek);
        final byte[] fileData;

        if (packageData.compressed) {
            fileData = PackageFunctions.decompress(rawFileData);
        } else {
            fileData = rawFileData;
        }

        final Payload payloadObject = getEmptyPayload();
        payloadObject.setFileName(fileName);
        payloadObject.setMimeType(mimeType);
        payloadObject.setData(fileData);

        return payloadObject;
    }

    @Override
    public Payload getEmptyPayload() {
        return new Payload_v1_0_0();
    }
}
