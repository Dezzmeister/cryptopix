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

    ImageData encodeSecret(final ImageData original, final byte[] secretData);

    DecodedImage decode(final ImageData secret);
}
