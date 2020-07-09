package com.dezzmeister.cryptopix.main.secret;

import android.content.Context;

import com.dezzmeister.cryptopix.main.images.DecodedImage;
import com.dezzmeister.cryptopix.main.images.ImageData;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.zip.DataFormatException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Implemented by any class that can encode/decode Cryptopix images. This exists for backward
 * compatibility, so that new Cryptopix versions using different strategies/schema can work with older
 * versions.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public interface PackageHandler extends Serializable {

    /**
     * Returns a status value representing the state of the encoded data within the image.
     *
     * @param context context to use when creating error Toasts
     * @param secret image containing secret
     * @param header optional image header (for <code>secret</code>). If this is null, the image header will be recomputed.
     * @return true if the secret data is valid and not corrupt
     */
    EncodedImageState getImageState(final Context context, final ImageData secret, final PackageHeader header);

    /**
     * Returns true if the given image contains a password-protected payload. Does not ensure that the
     * secret data is valid; validity must still be checked with {@link #getImageState(Context, ImageData, PackageHeader)}.
     *
     * @param secret image data
     * @param data package header for the given image, if it has already been extracted. If this parameter is not null,
     *             this function will return the value in the package header, else it will compute a new
     *             package header and return the value in that.
     * @return true if the given image is password protected
     */
    boolean passwordProtected(final ImageData secret, final PackageHeader data);

    /**
     * Extracts only the package header of an image containing secret data. Anyone can view the package
     * header even if a password is used; the password is only used to encrypt the payload and
     * payload header.
     *
     * @param secret image containing secret payload
     * @return package header
     */
    PackageHeader extractHeader(final ImageData secret);

    /**
     * Hides the given data in the image, accounting for the options provided. This function returns a
     * new {@link ImageData} instance with a new pixel array, to keep the original intact.
     *
     * @param original original image
     * @param secretData secret data to hide in the image
     * @param options options to use when hiding data
     * @return image containing secret data
     */
    ImageData encodeSecret(final ImageData original, final Payload secretData, final EncodingOptions options) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException;

    Payload decode(final ImageData secret, final PackageHeader header, final String password) throws NoSuchAlgorithmException, InvalidKeySpecException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, DataFormatException, IOException;

    /**
     * Creates an empty Payload. The user can fill this payload with necessary file data and call
     * {@link #encodeSecret(ImageData, Payload, EncodingOptions)}.
     *
     * @return an empty payload
     */
    Payload getEmptyPayload();
}
