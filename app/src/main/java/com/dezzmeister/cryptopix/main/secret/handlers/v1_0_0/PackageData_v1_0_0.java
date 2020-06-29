package com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0;

import com.dezzmeister.cryptopix.main.secret.SecretPackageData;

/**
 * Header data about a secret file hidden in an image. The secret file will contain its own header.
 *
 * @author Joe Desmond
 */
public class PackageData_v1_0_0 implements SecretPackageData {

    /**
     * A Cryptopix version code
     */
    public final long cryptopixVersionCode = -1;

    /**
     * Size of the package header, in bytes (excluding version code)
     */
    public final int headerSizeBytes = -1;

    /**
     * Size of the payload, in bytes
     */
    public final int dataSizeBytes = -1;

    /**
     * True if the file is encrypted with a password
     */
    public final boolean hasPassword = false;

    /**
     * Salt (only exists if there is a password)
     */
    public final String salt = null;

    /**
     * Password hash (only exists if there is a password)
     */
    public final String passwordHash = null;
}
