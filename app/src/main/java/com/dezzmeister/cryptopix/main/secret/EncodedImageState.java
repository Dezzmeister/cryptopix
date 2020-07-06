package com.dezzmeister.cryptopix.main.secret;

import java.io.Serializable;

/**
 * The state of an image. Images can:
 * <ul>
 *     <li>Contain no secret</li>
 *     <li>Contain a corrupted secret</li>
 *     <li>Contain an intact secret with no password</li>
 *     <li>Contain an intact secret with a password</li>
 *     <li>Contain a secret, but the device does not support the functions used</li>
 * </ul>
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public enum EncodedImageState implements Serializable {
    NO_SECRET,
    CORRUPTED,
    SECRET_NO_PASSWORD,
    SECRET_PASSWORD,
    UNSUPPORTED
}
