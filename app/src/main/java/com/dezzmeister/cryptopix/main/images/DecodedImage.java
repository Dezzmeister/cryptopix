package com.dezzmeister.cryptopix.main.images;

import com.dezzmeister.cryptopix.main.secret.SecretFileData;

import java.io.Serializable;

/**
 * A decoded image, containing unencrypted secret data and the image containing the data.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class DecodedImage implements Serializable {

    /**
     * Secret data (unencrypted). Contains the actual file
     */
    public final SecretFileData secretData;

    /**
     * Contains the original image and header info about the secret payload
     */
    public final ImageData imageData;

    /**
     * Creates a fully decoded and unencrypted image. The file data in {@link #secretData} can
     * be used to reconstruct the original file.
     *
     * @param _secretData secret file (payload) data
     * @param _imageData original image data
     */
    public DecodedImage(final SecretFileData _secretData, final ImageData _imageData) {
        secretData = _secretData;
        imageData = _imageData;
    }
}
