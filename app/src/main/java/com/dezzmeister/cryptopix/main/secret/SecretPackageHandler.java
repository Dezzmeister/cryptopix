package com.dezzmeister.cryptopix.main.secret;

import com.dezzmeister.cryptopix.main.images.DecodedImage;
import com.dezzmeister.cryptopix.main.images.ImageData;

/**
 * Implemented by any class that can encode/decode Cryptopix images. This exists for backward
 * compatibility, so that new Cryptopix versions using different strategies can work with older
 * versions.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public interface SecretPackageHandler {

    /**
     * Extracts only the package header of an image containing secret data. Anyone can view the package
     * header even if a password is used; the password is only used to encrypt the payload and
     * payload header.
     *
     * @param secret image containing secret payload
     * @return package header
     */
    SecretPackageData extractHeader(final ImageData secret);

    /**
     * Hides the given data in the image, accounting for the options provided. This function returns a
     * new {@link ImageData} instance with a new pixel array, to keep the original intact.
     *
     * @param original original image
     * @param secretData secret data to hide in the image
     * @param options options to use when hiding data
     * @return image containing secret data
     */
    ImageData encodeSecret(final ImageData original, final byte[] secretData, final EncodingOptions options);

    DecodedImage decode(final ImageData secret);
}
