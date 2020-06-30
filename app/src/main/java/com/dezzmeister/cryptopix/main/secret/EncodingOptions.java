package com.dezzmeister.cryptopix.main.secret;

import com.dezzmeister.cryptopix.main.images.ImageData;

import java.io.Serializable;

/**
 * Classes can implement this interface and define a set of preferences to be used when encoding a secret
 * message with {@link SecretPackageHandler#encodeSecret(ImageData, byte[], EncodingOptions)}.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public interface EncodingOptions extends Serializable {
}
