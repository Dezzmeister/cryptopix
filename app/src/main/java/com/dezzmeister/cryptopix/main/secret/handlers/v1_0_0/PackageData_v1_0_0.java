package com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0;

import com.dezzmeister.cryptopix.main.secret.PackageHeader;

/**
 * Header data about a secret file hidden in an image. The secret file will contain its own header.
 *
 * @author Joe Desmond
 */
public class PackageData_v1_0_0 implements PackageHeader {

    /**
     * A Cryptopix version code
     */
    public long cryptopixVersionCode = -1;

    /**
     * MD5 payload hash. All data after this hash is hashed to produce this value
     */
    public byte[] payloadHash = null;

    /**
     * Size of the payload, in bytes (excluding the header)
     */
    public int payloadSize = -1;

    /**
     * True if the file is encrypted with a password
     */
    public boolean hasPassword = false;

    /**
     * True if the data is compressed. Data can be compressed before encryption.
     */
    public boolean compressed = false;

    /**
     * Salt (only exists if there is a password)
     */
    public byte[] salt = null;

    /**
     * 16-byte initialization vector (only exists if there is a password)
     */
    public byte[] initVector = null;

    /**
     * SHA-256 password hash (only exists if there is a password)
     */
    public byte[] passwordHash = null;

    /**
     * The pixel at which payload data begins (the payload header, possibly encrypted)
     */
    public int dataOffset = -1;
}
