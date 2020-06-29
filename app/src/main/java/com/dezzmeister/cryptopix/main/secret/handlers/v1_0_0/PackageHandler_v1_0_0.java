package com.dezzmeister.cryptopix.main.secret.handlers.v1_0_0;

import com.dezzmeister.cryptopix.main.images.DecodedImage;
import com.dezzmeister.cryptopix.main.images.ImageData;
import com.dezzmeister.cryptopix.main.secret.SecretPackageHandler;

public class PackageHandler_v1_0_0 implements SecretPackageHandler {
    @Override
    public ImageData encodeSecret(ImageData original, byte[] secretData) {
        return null;
    }

    @Override
    public DecodedImage decode(ImageData secret) {
        return null;
    }
}
