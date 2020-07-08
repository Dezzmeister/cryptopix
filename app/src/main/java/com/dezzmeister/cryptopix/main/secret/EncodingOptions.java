package com.dezzmeister.cryptopix.main.secret;

import com.dezzmeister.cryptopix.main.images.ImageData;

import java.io.Serializable;

/**
 * Specifies a set of options to be used when encoding a secret package.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class EncodingOptions implements Serializable {
    /**
     * Cryptopix version code
     */
    public long versionCode = -1;

    /**
     * A password to use when encrypting the package, or null if no password should be used
     */
    public String password = null;

    /**
     * True if the data should be compressed after being encrypted
     */
    public boolean compress = false;

}
