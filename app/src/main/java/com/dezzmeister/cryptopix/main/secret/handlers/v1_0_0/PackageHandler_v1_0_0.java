package com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0;

import android.content.Context;
import android.widget.Toast;

import com.dezzmeister.cryptopix.main.images.DecodedImage;
import com.dezzmeister.cryptopix.main.images.ImageData;
import com.dezzmeister.cryptopix.main.secret.EncodedImageState;
import com.dezzmeister.cryptopix.main.secret.EncodingOptions;
import com.dezzmeister.cryptopix.main.secret.PackageFunctions;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;
import com.dezzmeister.cryptopix.main.secret.PackageHandler;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    private final int VERSION_CODE_LENGTH = 8;

    /**
     * Number of bytes in the payload hash (128 bit MD5)
     */
    private final int PAYLOAD_HASH_LENGTH = 16;

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
    private final int MIN_PACKAGE_HEADER_LENGTH = VERSION_CODE_LENGTH + PAYLOAD_HASH_LENGTH + PAYLOAD_HEADER_SIZE_LENGTH + PAYLOAD_SIZE_LENGTH + PASSWORD_FLAG_LENGTH;

    /**
     * Maximum byte-length of a package header, if a password is used
     */
    private final int MAX_PACKAGE_HEADER_LENGTH = MIN_PACKAGE_HEADER_LENGTH + SALT_LENGTH + HASH_LENGTH;

    @Override
    public EncodedImageState getImageState(final Context context, final ImageData secret, final PackageHeader header) {
        MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(context, "MD5 is not supported on this device!", Toast.LENGTH_SHORT);
            toast.show();
            return EncodedImageState.UNSUPPORTED;
        }

        final PackageData_v1_0_0 packageHeader;

        if (!(header instanceof PackageData_v1_0_0)) {
            packageHeader = (PackageData_v1_0_0) extractHeader(secret);
        } else {
            packageHeader = (PackageData_v1_0_0) header;
        }

        final int startIndex = VERSION_CODE_LENGTH + PAYLOAD_HASH_LENGTH;
        final byte[] data = PackageFunctions.extractBytes(secret.pixels, -1, startIndex);

        final byte[] realHash = md5.digest(data);

        if (realHash.equals(packageHeader.payloadHash)) {
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

        final byte[] salt;
        final byte[] passwordHash;

        if (passwordFlag) {
            salt = PackageFunctions.extractBytes(pixels, SALT_LENGTH, seek);

            seek += SALT_LENGTH;
            passwordHash = PackageFunctions.extractBytes(pixels, HASH_LENGTH, seek);

            seek += HASH_LENGTH;
        } else {
            salt = null;
            passwordHash = null;
        }

        final PackageData_v1_0_0 data = new PackageData_v1_0_0();
        data.cryptopixVersionCode = versionCode;
        data.payloadHash = payloadHash;
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
