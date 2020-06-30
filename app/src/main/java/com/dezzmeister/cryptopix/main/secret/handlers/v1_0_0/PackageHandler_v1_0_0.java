package com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0;

import com.dezzmeister.cryptopix.main.images.DecodedImage;
import com.dezzmeister.cryptopix.main.images.ImageData;
import com.dezzmeister.cryptopix.main.secret.EncodingOptions;
import com.dezzmeister.cryptopix.main.secret.PackageFunctions;
import com.dezzmeister.cryptopix.main.secret.SecretPackageData;
import com.dezzmeister.cryptopix.main.secret.SecretPackageHandler;

/**
 * Package handler for files generated with Cryptopix 1.0.0. The static members give the byte-length of
 * package header fields. In Cryptopix 1.0.0, 1 byte of package data corresponds to 1 pixel (4 bytes
 * of image data).
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class PackageHandler_v1_0_0 implements SecretPackageHandler {

    /**
     * Cryptopix version code length (8-byte field). The version code should be
     * {@link com.dezzmeister.cryptopix.main.secret.Versions#VERSION_1_0_0 Versions.VERSION_1_0_0}.
     */
    private final int VERSION_CODE_LENGTH = 8;

    /**
     * Number of bytes in the payload header (4-byte field)
     */
    private final int PAYLOAD_HEADER_SIZE_LENGTH = 4;

    /**
     * Number of bytes in the payload (4-byte field)
     */
    private final int PAYLOAD_SIZE_LENGTH = 4;

    /**
     * Number of bytes determining the use of a password (1-byte field)
     */
    private final int PASSWORD_FLAG_LENGTH = 1;

    /**
     * Number of bytes in the optional salt (32-byte field)
     */
    private final int SALT_LENGTH = 32;

    /**
     * NUmber of bytes in the optional password hash (32-byte field)
     */
    private final int HASH_LENGTH = 32;

    /**
     * Minimum byte-length of a package header, if no password is used
     */
    private final int MIN_PACKAGE_HEADER_LENGTH = VERSION_CODE_LENGTH + PAYLOAD_HEADER_SIZE_LENGTH + PAYLOAD_SIZE_LENGTH + PASSWORD_FLAG_LENGTH;

    /**
     * Maximum byte-length of a package header, if a password is used
     */
    private final int MAX_PACKAGE_HEADER_LENGTH = MIN_PACKAGE_HEADER_LENGTH + SALT_LENGTH + HASH_LENGTH;

    @Override
    public SecretPackageData extractHeader(final ImageData secret) {
        final int[] pixels = secret.pixels;

        if (pixels.length < MIN_PACKAGE_HEADER_LENGTH) {
            throw new IllegalArgumentException("Image is too small!");
        }

        int seek = 0;
        final long versionCode = PackageFunctions.versionCode(secret);

        seek = 8;
        final byte[] payloadHeaderSizeBytes = PackageFunctions.extractBytes(pixels, PAYLOAD_HEADER_SIZE_LENGTH, seek);
        final int payloadHeaderSize = PackageFunctions.intFromBytes(payloadHeaderSizeBytes);

        seek += PAYLOAD_HEADER_SIZE_LENGTH;
        final byte[] payloadSizeBytes = PackageFunctions.extractBytes(pixels, PAYLOAD_SIZE_LENGTH, seek);
        final int payloadSize = PackageFunctions.intFromBytes(payloadSizeBytes);

        seek += PAYLOAD_SIZE_LENGTH;
        final byte[] passwordFlagBytes = PackageFunctions.extractBytes(pixels, PASSWORD_FLAG_LENGTH, seek);
        final boolean passwordFlag = (passwordFlagBytes[0] != 0);

        seek += PASSWORD_FLAG_LENGTH;

        if (passwordFlag && pixels.length < MAX_PACKAGE_HEADER_LENGTH) {
            throw new IllegalArgumentException("Image is too small!");
        }

        final String salt;
        final String passwordHash;

        if (passwordFlag) {
            final byte[] saltBytes = PackageFunctions.extractBytes(pixels, SALT_LENGTH, seek);
            salt = new String(saltBytes);

            seek += SALT_LENGTH;
            final byte[] hashBytes = PackageFunctions.extractBytes(pixels, HASH_LENGTH, seek);
            passwordHash = new String(hashBytes);

            seek += HASH_LENGTH;
        } else {
            salt = null;
            passwordHash = null;
        }

        final PackageData_v1_0_0 data = new PackageData_v1_0_0();
        data.cryptopixVersionCode = versionCode;
        data.payloadHeaderSize = payloadHeaderSize;
        data.payloadSize = payloadSize;
        data.hasPassword = passwordFlag;
        data.salt = salt;
        data.passwordHash = passwordHash;
        data.dataOffset = seek;

        return data;
    }

    @Override
    public ImageData encodeSecret(final ImageData original, final byte[] secretData, final EncodingOptions options) {
        return null;
    }

    @Override
    public DecodedImage decode(final ImageData secret) {
        return null;
    }
}
