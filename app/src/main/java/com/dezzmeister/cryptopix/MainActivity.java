package com.dezzmeister.cryptopix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.dezzmeister.cryptopix.main.dialogs.CorruptedImageDialog;
import com.dezzmeister.cryptopix.main.dialogs.DecodeSecretDialog;
import com.dezzmeister.cryptopix.main.dialogs.DialogArgs;
import com.dezzmeister.cryptopix.main.dialogs.EncodeSecretDialog;
import com.dezzmeister.cryptopix.main.dialogs.UnsupportedAlgorithmDialog;
import com.dezzmeister.cryptopix.main.images.ImageData;
import com.dezzmeister.cryptopix.main.secret.EncodedImageState;
import com.dezzmeister.cryptopix.main.secret.PackageFunctions;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;
import com.dezzmeister.cryptopix.main.secret.PackageHandler;
import com.dezzmeister.cryptopix.main.secret.Versions;
import com.dezzmeister.cryptopix.main.session.SessionObject;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The main app activity. Allows the user to select an image, which can be further encoded/decoded.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Folder (in the internal app directory) containing the session object
     */
    private static final String SESSION_OBJECT_FOLDER = "session";

    /**
     * Name of the session object
     */
    private static final String SESSION_OBJECT_FILENAME = "session_v1_0_0.joe";

    /**
     * Name of the local image (stored in local files), in {@link #SESSION_OBJECT_FOLDER}
     */
    private static final String LOCAL_IMAGE_FILENAME = "image_v1_0_0.png";

    /**
     * Request code to select an image in the gallery
     */
    private static final int REQUEST_IMAGE_GET = 1;

    /**
     * Request code to capture an image with the camera
     */
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    /**
     * The main ImageView
     */
    private ImageView mainImageView;

    /**
     * Main image directory (where to store and look for images)
     */
    private File imageDirectory;

    /**
     * Path to the current image, if created for a camera capture
     */
    private String currentPhotoPath;

    /**
     * The current image (full size)
     */
    private Bitmap fullSizeImage;

    /**
     * The current session object
     */
    private SessionObject session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        final AdView adView = findViewById(R.id.adView);
        final AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        mainImageView = findViewById(R.id.imageView);

        imageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        session = getSessionObject();
        final Bitmap image = session.getBitmap();
        if (image != null) {
            mainImageView.setImageBitmap(image);
        }


        if (session.darkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private SessionObject getSessionObject() {
        final File internalFiles = getApplicationContext().getFilesDir();
        final File sessionDirectory = new File(internalFiles, SESSION_OBJECT_FOLDER);
        final File sessionPath = new File(sessionDirectory, SESSION_OBJECT_FILENAME);

        if (!sessionDirectory.exists()) {
            // Session directory doesn't exist; directory and session need to be created

            if (!sessionDirectory.mkdir()) {
                // Directory can't be created for some reason. Display an error message

                final Toast toast = Toast.makeText(this, "Unable to create session object!", Toast.LENGTH_SHORT);
                toast.show();
                return new SessionObject(null);
            }

            return new SessionObject(sessionPath);
        } else {
            if (!sessionPath.exists()) {
                // Delete all files (possibly a session from an old version)

                for (final File file : sessionDirectory.listFiles()) {
                    if (!file.isDirectory()) {
                        file.delete();
                    }
                }

                return new SessionObject(sessionPath);
            } else {
                return SessionObject.loadFrom(this, sessionPath);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Creates an intent to select an image from the image gallery.
     */
    private void selectImage() {
        final Intent imageChooserIntent = new Intent();

        imageChooserIntent.setAction(Intent.ACTION_GET_CONTENT);
        imageChooserIntent.setType("image/*");
        if (imageChooserIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(imageChooserIntent, REQUEST_IMAGE_GET);
        }
    }

    /**
     * Creates a new empty PNG image file and returns the file handle. The {@link #currentPhotoPath}
     * is also set so that an image captured with the camera can be retrieved. The file is created
     * in the default android picture directory.
     *
     * @return file handle to empty PNG image
     * @throws IOException if there is a problem creating the temporary file
     */
    private File createImageFile() throws IOException {
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String imageFileName = "PNG_" + timeStamp + "_.png";
        final File image = new File(imageDirectory, imageFileName);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Creates an intent to capture a new image with the camera.
     */
    private void captureImage() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();

                final Toast toast = Toast.makeText(this, "Unable to capture photo! Select a gallery image instead", Toast.LENGTH_SHORT);
                toast.show();
            }

            if (photoFile != null) {
                final Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Copy a bitmap to the local persistent storage. Use this to circumvent URI permission rules
     * and have images persist across app sessions.
     *
     * @param bitmap bitmap image
     * @return file handle to the local image
     */
    private File copyBitmapToLocal(final Bitmap bitmap) {
        final File internalFiles = getApplicationContext().getFilesDir();
        final File sessionDirectory = new File(internalFiles, SESSION_OBJECT_FOLDER);
        final File imageLocation = new File(sessionDirectory, LOCAL_IMAGE_FILENAME);

        try (final FileOutputStream fos = new FileOutputStream(imageLocation)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Unable to save image!", Toast.LENGTH_SHORT);
            toast.show();
        }

        return imageLocation;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_GET: {
                if (resultCode == RESULT_OK) {
                    final Uri contentURI = data.getData();

                    try {
                        final Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(contentURI));
                        final File location = copyBitmapToLocal(bitmap);

                        session.setBitmap(this, bitmap, location);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        final Toast toast = Toast.makeText(this, "File does not exist!", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }

                    fullSizeImage = session.getBitmap();
                    mainImageView.setImageBitmap(session.getBitmap());

                    super.onActivityResult(requestCode, resultCode, data);
                    handleNewImage(session.getImage());

                    return;
                }
            }
            case REQUEST_IMAGE_CAPTURE: {
                if (resultCode == RESULT_OK) {
                    final Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                    session.setBitmap(this, bitmap, new File(currentPhotoPath));

                    fullSizeImage = session.getBitmap();
                    mainImageView.setImageBitmap(session.getBitmap());

                    super.onActivityResult(requestCode, resultCode, data);
                    handleNewImage(session.getImage());

                    return;
                }
            }
            default: {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void handleNewImage(final ImageData imageData) {
        final long versionCode = PackageFunctions.versionCode(imageData);
        final PackageHandler handler = Versions.getHandler(versionCode);
        final EncodedImageState state;
        final PackageHeader packageHeader;

        if (handler == null) {
            state = EncodedImageState.NO_SECRET;
            packageHeader = null;
        } else {
            packageHeader = handler.extractHeader(imageData);
            state = handler.getImageState(this, imageData, packageHeader);
        }

        splitOnImageState(state, session, packageHeader, handler);
    }

    /**
     * Splits the flow of the app based on the state of the image. There are five cases:
     * <ul>
     *     <li>The image contains valid, uncorrupted and unencrypted secret data</li>
     *     <li>The image contains valid, uncorrupted secret data encrypted with a password</li>
     *     <li>The image contains corrupted secret data (extremely rare case that there isn't any secret data)</li>
     *     <li>The image cannot be decoded because a hash function is not supported on the device</li>
     *     <li>The image does not contain any secret data (or it does, but it was generated by a newer Cryptopix version)</li>
     * </ul>
     *
     * @param state state of the image
     * @param session session object containing the image (in either Bitmap format or ImageData format)
     * @param packageHeader secret package header (can be null if the image contains no secret)
     * @param packageHandler secret package handler
     */
    private void splitOnImageState(final EncodedImageState state, final SessionObject session, final PackageHeader packageHeader, final PackageHandler packageHandler) {
        final Bundle bundle = new Bundle();

        bundle.putSerializable(DialogArgs.SESSION_OBJECT_KEY, session);
        bundle.putSerializable(DialogArgs.PACKAGE_HEADER_KEY, packageHeader);
        bundle.putSerializable(DialogArgs.PACKAGE_HANDLER_KEY, packageHandler);

        switch (state) {
            case SECRET_NO_PASSWORD:
            case SECRET_PASSWORD: {
                final DecodeSecretDialog dialog = new DecodeSecretDialog();
                dialog.setCancelable(false);
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "decodeSecret");

                return;
            }
            case CORRUPTED: {
                final CorruptedImageDialog dialog = new CorruptedImageDialog();
                dialog.setCancelable(false);
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "corruptedImage");

                return;
            }
            case UNSUPPORTED: {
                final UnsupportedAlgorithmDialog dialog = new UnsupportedAlgorithmDialog();
                dialog.setCancelable(false);
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "unsupportedAlgorithm");

                return;
            }
            default:
            case NO_SECRET: {
                final EncodeSecretDialog dialog = new EncodeSecretDialog();
                dialog.setCancelable(false);
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "encodeSecret");

                return;
            }
        }
    }

    /**
     * Toggles dark theme.
     */
    private void toggleDarkMode() {
        session.toggleDarkMode(this);

        if (session.darkModeEnabled()) {
            getDelegate().setLocalNightMode((AppCompatDelegate.MODE_NIGHT_YES));
        } else {
            getDelegate().setLocalNightMode((AppCompatDelegate.MODE_NIGHT_NO));
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_picture: {
                selectImage();
                return true;
            }
            case R.id.capture_picture: {
                captureImage();
                return true;
            }
            case R.id.dark_mode: {
                toggleDarkMode();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
