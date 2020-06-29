package com.dezzmeister.cryptopix.fileio;

import org.nustaq.serialization.FSTConfiguration;

/**
 * Contains global singleton instance of FST configuration (for fast serialization).
 * The config is expensive to initialize, so it is initialized once and used throughout the app
 * to serialize objects.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class FSTConfig {
    public static final FSTConfiguration config = FSTConfiguration.createAndroidDefaultConfiguration();
}
