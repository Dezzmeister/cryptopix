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
    public long cryptopixVersionCode = -1;

    /**
     * Size of the package header, in bytes (excluding version code)
     */
    public int payloadHeaderSize = -1;

    /**
     * Size of the payload, in bytes (excluding the header)
     */
    public int payloadSize = -1;

    /**
     * True if the file is encrypted with a password
     */
    public boolean hasPassword = false;

    /**
     * Salt (only exists if there is a password)
     */
    public String salt = null;

    /**
     * Password hash (only exists if there is a password)
     */
    public String passwordHash = null;

    /**
     * The pixel at which payload data begins (the payload header, possibly encrypted)
     */
    public int dataOffset = -1;
}
