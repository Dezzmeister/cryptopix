package com.dezzmeister.cryptopix.main.session;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import com.dezzmeister.cryptopix.main.images.ImageData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A user session; allows the user to open an app and maintain the previous state. When the internal
 * state is modified, the session will save itself to the given location. A session can be restored
 * by calling {@link #loadFrom(Context, File)};
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class SessionObject implements Serializable {

    /**
     * Location to save this session object to, or null if the session object won't persist
     */
    private final File location;

    /**
     * Current image as a Bitmap
     */
    public transient Bitmap bitmapImage;

    /**
     * Image pixels and dimensions
     */
    private transient ImageData imageData;

    /**
     * True if the image ({@link #imageData} contains secret data
     */
    private boolean imageHasSecret = false;

    /**
     * File path to the image
     */
    private File imagePath;

    /**
     * True if dark mode is enabled
     */
    private boolean darkMode;

    /**
     * Creates a session object that will save itself to the given location. A session will save itself
     * when its internal state is updated (for example, when a new image is set with {@link #setBitmap(Context, Bitmap, File)}.
     *
     * @param _location location to save this session to
     */
    public SessionObject(final File _location) {
        location = _location;
        imageData = null;
        imagePath = null;
        darkMode = false;
    }

    public final void resolveImagePath() {
        final Bitmap bitmap = BitmapFactory.decodeFile(imagePath.getAbsolutePath());
        final int imageWidth = bitmap.getWidth();
        final int imageHeight = bitmap.getHeight();
        final int[] image = convert(bitmap);
        imageData = new ImageData(image, imageWidth, imageHeight);
        bitmapImage = bitmap;
    }

    /**
     * Sets the image associated with this session. The image is internally converted to an
     * ARGB pixel array to facilitate serialization.
     *
     * @param context context to show an error Toast
     * @param bitmap bitmap image
     * @param bitmapLocation location of the bitmap image
     */
    public final void setBitmap(final Context context, final Bitmap bitmap, final File bitmapLocation) {
        final int imageWidth = bitmap.getWidth();
        final int imageHeight = bitmap.getHeight();
        final int[] image = convert(bitmap);
        imageData = new ImageData(image, imageWidth, imageHeight);

        bitmapImage = bitmap;
        imagePath = bitmapLocation;
        save(context);
    }

    /**
     * Sets the state of the image (true if it contains secret data, false if not).
     *
     * @param context context to show error toasts when saving state
     * @param containsSecret true if the image contains a secret
     */
    public final void setImageContainsSecret(final Context context, final boolean containsSecret) {
        imageHasSecret = containsSecret;
        save(context);
    }

    /**
     * Returns true if the image contains secret data, false if not.
     *
     * @return true if the image contains a secret
     */
    public final boolean imageContainsSecret() {
        return imageHasSecret;
    }

    /**
     * Returns the current bitmap image.
     *
     * @return current image as a bitmap
     */
    public final Bitmap getBitmap() {
        return bitmapImage;
    }

    /**
     * Returns the current image as an array of 4-byte ARGB pixels.
     *
     * @return current image (pixels and dimensions)
     */
    public final ImageData getImage() {
        return imageData;
    }

    /**
     * Creates a bitmap image from the given pixel data. Color space is 4-byte ARGB.
     *
     * @param imageData image pixel data (4-byte ARGB)
     * @return bitmap
     */
    public static final Bitmap fromImagePixels(final ImageData imageData) {
        return Bitmap.createBitmap(imageData.pixels, imageData.width, imageData.height, Bitmap.Config.ARGB_8888);
    }

    /**
     * Convert an Android Bitmap to an ARGB array of pixels.
     *
     * @param bitmap bitmap image
     * @return ARGB array of pixels
     */
    private int[] convert(final Bitmap bitmap) {
        final int[] out = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(out, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return out;
    }

    /**
     * Enables/disables dark mode and saves the state.
     *
     * @param context used to show an error if the session cannot be updated
     */
    public void toggleDarkMode(final Context context) {
        darkMode = !darkMode;
        save(context);
    }

    /**
     * Saves this session object to {@link #location}. A context is required to show a Toast
     * if a problem occurs.
     *
     * @param context context
     */
    public void save(final Context context) {
        if (location == null) {
            return;
        }

        try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(location))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(context, "Failed to save session!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Loads a serialized session object from the given path.
     *
     * @param context context to show a Toast if an error occurs
     * @param location location of the SessionObject
     * @return SessionObject at the given location
     */
    public static final SessionObject loadFrom(final Context context, final File location) {
        SessionObject object = new SessionObject(location);

        try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(location))) {
            object = (SessionObject) ois.readObject();
            object.resolveImagePath();
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(context, "Failed to load session!", Toast.LENGTH_SHORT);
            toast.show();
        }

        return object;
    }

    /**
     * Returns true if dark mode is enabled.
     *
     * @return true if dark mode is enabled
     */
    public boolean darkModeEnabled() {
        return darkMode;
    }
}
