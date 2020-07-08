package com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0;

import com.dezzmeister.cryptopix.main.secret.Payload;

/**
 * A payload in Cryptopix 1.0.0. Files contain a mime type, a file name, and the raw, uncompressed
 * and unencrypted data.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class Payload_v1_0_0 implements Payload {
    public String fileName;

    public String mimeType;

    public byte[] data;

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public String mimeType() {
        return mimeType;
    }

    @Override
    public byte[] data() {
        return data;
    }

    @Override
    public void setFileName(final String _fileName) {
        fileName = _fileName;
    }

    @Override
    public void setMimeType(final String _mimeType) {
        mimeType = _mimeType;
    }

    @Override
    public void setData(final byte[] _data) {
        data = _data;
    }
}
