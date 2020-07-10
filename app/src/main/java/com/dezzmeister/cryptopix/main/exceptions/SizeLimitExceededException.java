package com.dezzmeister.cryptopix.main.exceptions;

/**
 * An exception to be thrown when a file cannot be encoded because the constructed secret package
 * is larger than the carrier image.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class SizeLimitExceededException extends Exception {

    /**
     * Creates a SizeLimitExceededException with the given message.
     *
     * @param message message
     */
    public SizeLimitExceededException(final String message) {
        super(message);
    }
}
