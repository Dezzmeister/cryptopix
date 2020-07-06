package com.dezzmeister.cryptopix.main.secret;

import java.io.Serializable;

/**
 * Secret package header info. Headers may change between different versions of Cryptopix, so this
 * is an interface. Version-specific classes should implement this for backwards compatibility.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public interface PackageHeader extends Serializable {
}
