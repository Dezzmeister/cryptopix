package com.dezzmeister.cryptopix.main.secret;

/**
 * Contains unencrypted file data, extracted from an image containing a secret.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public interface Payload {

    /**
     * Returns the original name of this file, including the extension.
     *
     * @return name of the file
     */
    String fileName();

    /**
     * Returns the MIME type of this file.
     *
     * @return file MIME type
     */
    String mimeType();

    /**
     * Returns the raw file data (the contents of the file).
     *
     * @return file data
     */
    byte[] data();

    /**
     * Sets the file name.
     *
     * @param fileName file name
     */
    void setFileName(final String fileName);

    /**
     * Sets the MIME type of this file.
     *
     * @param mimeType file MIME type
     */
    void setMimeType(final String mimeType);

    /**
     * Sets the contents of this file.
     *
     * @param data file data
     */
    void setData(final byte[] data);
}
