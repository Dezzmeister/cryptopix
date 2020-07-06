package com.dezzmeister.cryptopix.main.secret;

import com.dezzmeister.cryptopix.main.images.ImageData;
import com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0.PackageHandler_v1_0_0;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains package handlers for files encoded with different versions of Cryptopix. Provides backward
 * compatibility.
 *
 * Every image with encoded data will contain an 8-byte header (as part of the secret data) specifying the
 * Cryptopix version code. The version code can be used with {@link #PACKAGE_HANDLERS} to obtain a package
 * handler to decode the secret file.
 *
 * The version code can be obtained with {@link PackageFunctions#versionCode(ImageData)}.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class Versions {

    /**
     * The first version.
     * @since 1.0.0
     */
    public static final long VERSION_1_0_0 = 0xCACADACC;

    /**
     * Always the current version. This variable should be updated before a new version is released.
     */
    public static final long THIS_VERSION = VERSION_1_0_0;

    /**
     * Package handlers. Plug in a version code and get a package handler to encode/decode packages
     * in that version of Cryptopix.
     */
    public static final Map<Long, PackageHandler> PACKAGE_HANDLERS;

    static {
        PACKAGE_HANDLERS = new HashMap<Long, PackageHandler>();
        PACKAGE_HANDLERS.put(VERSION_1_0_0, new PackageHandler_v1_0_0());
    }

    /**
     * Returns true if the given Cryptopix version is supported. If the version is supported,
     * a handler can be obtained with {@link #getHandler(long)}.
     *
     * @param versionCode Cryptopix version code
     * @return true if the given version is supported, false if not
     * @see #THIS_VERSION
     */
    public static final boolean isSupported(final long versionCode) {
        return PACKAGE_HANDLERS.containsKey(versionCode);
    }

    /**
     * Returns the package handler for a given Cryptopix version. If no handler exists, this method
     * will return null. {@link #isSupported(long)} can be used to check if the given version is
     * supported.
     *
     * @param versionCode Cryptopix version code
     * @return package handler for the given version
     * @see #THIS_VERSION
     */
    public static final PackageHandler getHandler(final long versionCode) {
        return PACKAGE_HANDLERS.get(versionCode);
    }
}
