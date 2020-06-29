package com.dezzmeister.cryptopix.main.secret;

import com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0.PackageHandler_v1_0_0;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains package handlers for files encoded with different versions of Cryptopix. Provides backward
 * compatibility.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class Versions {
    public static final long VERSION_1_0_0 = 0xCACADACC;

    public static final Map<Long, SecretPackageHandler> PACKAGE_HANDLERS;

    static {
        PACKAGE_HANDLERS = new HashMap<Long, SecretPackageHandler>();
        PACKAGE_HANDLERS.put(VERSION_1_0_0, new PackageHandler_v1_0_0());
    }
}
