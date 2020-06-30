package com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0;

import com.dezzmeister.cryptopix.main.secret.EncodingOptions;

/**
 * Specifies different options to use when encoding files. As of Cryptopix 1.0.0, a password can
 * be used to encrypt a file.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class EncodingOptions_v1_0_0 implements EncodingOptions {

    /**
     * A user-specified password to encrypt the file, or null if no password should be used.
     */
    String password = null;
}
