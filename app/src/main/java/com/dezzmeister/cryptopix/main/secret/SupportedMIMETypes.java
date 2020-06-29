package com.dezzmeister.cryptopix.main.secret;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class SupportedMIMETypes {

    public static final String TEXT_TYPE = "text/*";

    public static final String IMAGE_TYPE = "image/*";

    public static final String APPLICATION_TYPE = "application/*";

    public static final String FONT_TYPE = "font/*";

    public static final List<String> types = Arrays.asList(TEXT_TYPE, IMAGE_TYPE, APPLICATION_TYPE, FONT_TYPE);
}
