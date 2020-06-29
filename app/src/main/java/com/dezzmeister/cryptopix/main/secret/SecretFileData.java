package com.dezzmeister.cryptopix.main.secret;

import java.io.Serializable;

/**
 * Unencrypted secret file data. Headers may change between versions of Cryptopix, so this
 * should be implemented by version-specific classes.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public interface SecretFileData extends Serializable {
}
