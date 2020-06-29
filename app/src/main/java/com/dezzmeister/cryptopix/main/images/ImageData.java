package com.dezzmeister.cryptopix.main.images;

import java.io.Serializable;

/**
 * Raw image data. Pixels are 4 byte ARGB.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class ImageData implements Serializable {

    /**
     * 4-byte ARGB pixels
     */
    public final int[] pixels;

    /**
     * Width of the image (pixels)
     */
    public final int width;

    /**
     * Height of the image (pixels)
     */
    public final int height;

    /**
     * Constructs an ImageData object given an image, its dimensions, and header data about its secret information.
     *
     * @param _pixels 4-byte ARGB pixels, row-wise
     * @param _width width of the image (pixels)
     * @param _height height of the image (pixels)
     */
    public ImageData(final int[] _pixels, final int _width, final int _height) {
        pixels = _pixels;
        width = _width;
        height = _height;
    }
}
