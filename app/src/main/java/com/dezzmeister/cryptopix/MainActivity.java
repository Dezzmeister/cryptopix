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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.dezzmeister.cryptopix.main.activities.DecodeMessageActivity;
import com.dezzmeister.cryptopix.main.activities.EncodeMessageActivity;
import com.dezzmeister.cryptopix.main.dialogs.CorruptedImageDialog;
import com.dezzmeister.cryptopix.main.dialogs.DecodeSecretDialog;
import com.dezzmeister.cryptopix.main.dialogs.DialogArgs;
import com.dezzmeister.cryptopix.main.dialogs.EncodeSecretDialog;
import com.dezzmeister.cryptopix.main.dialogs.EnterPasswordDecodeDialog;
import com.dezzmeister.cryptopix.main.dialogs.UnsupportedAlgorithmDialog;
import com.dezzmeister.cryptopix.main.images.ImageData;
import com.dezzmeister.cryptopix.main.secret.EncodedImageState;
import com.dezzmeister.cryptopix.main.secret.PackageFunctions;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;
import com.dezzmeister.cryptopix.main.secret.PackageHandler;
import com.dezzmeister.cryptopix.main.secret.Payload;
import com.dezzmeister.cryptopix.main.secret.Versions;
import com.dezzmeister.cryptopix.main.session.SessionObject;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
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

    /**
     * The current package handler
     */
    private PackageHandler handler;

    /**
     * The current package header
     */
    private PackageHeader header;

    /**
     * Floating action button for encoding data
     */
    private FloatingActionButton encodeFAB;

    /**
     * Floating action button for decoding data
     */
    private FloatingActionButton decodeFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        encodeFAB = findViewById(R.id.encode_image_fab);
        decodeFAB = findViewById(R.id.decode_image_fab);

        encodeFAB.setOnClickListener(this::onClickEncodeFAB);
        decodeFAB.setOnClickListener(this::onClickDecodeFAB);

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

        final File externalFiles = getApplicationContext().getExternalFilesDir(null);
        final File encodedImageDir = new File(externalFiles, EncodeMessageActivity.ENCODED_IMAGE_FOLDER);
        final File decodedImageDir = new File(externalFiles, DecodeMessageActivity.DECODED_PAYLOAD_FOLDER);

        deleteAppExternalData();

        encodedImageDir.mkdir();
        decodedImageDir.mkdir();

        session = getSessionObject();
        final Bitmap image = session.getBitmap();
        if (image != null) {
            mainImageView.setImageBitmap(image);
        }

        if (session.imageContainsSecret()) {
            final ImageData imageData = session.getImage();
            final long versionCode = PackageFunctions.versionCode(imageData);
            handler = Versions.getHandler(versionCode);
            header = handler.extractHeader(imageData);
        }

        setFABVisibility(session);

        if (session.darkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Defines the behavior when the "Encode" floating action button is clicked. This should open a new
     * activity to encode some secret data in the image.
     *
     * @param view view
     */
    private final void onClickEncodeFAB(final View view) {
        if (handler == null) {
            handler = Versions.getHandler(Versions.THIS_VERSION);
        }

        final Intent intent = new Intent(this, EncodeMessageActivity.class);
        intent.putExtra(DialogArgs.SESSION_OBJECT_KEY, session);
        intent.putExtra(DialogArgs.PACKAGE_HEADER_KEY, header);
        intent.putExtra(DialogArgs.PACKAGE_HANDLER_KEY, handler);

        startActivity(intent);
    }

    private final void onClickDecodeFAB(final View view) {
        if (header == null || handler == null) {
            final Toast toast = Toast.makeText(this, "No secret data to extract!", Toast.LENGTH_SHORT);
            toast.show();
            setFABVisibility(session);

            return;
        }

        if (!header.isPasswordProtected()) {
            final Intent intent = new Intent(this, DecodeMessageActivity.class);
            intent.putExtra(DialogArgs.SESSION_OBJECT_KEY, session);
            intent.putExtra(DialogArgs.PACKAGE_HEADER_KEY, header);
            intent.putExtra(DialogArgs.PACKAGE_HANDLER_KEY, handler);

            startActivity(intent);
        }

        final EnterPasswordDecodeDialog dialog = new EnterPasswordDecodeDialog();
        dialog.setCancelable(false);
        dialog.setCallingActivity(this);
        dialog.show(getSupportFragmentManager(), "enterPassword");
    }

    public void decodeImage(final String password) {
        if (password != null) {
            try {
                if (!handler.isCorrectPassword(password, header)) {
                    final Toast toast = Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT);
                    toast.show();

                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                final Toast toast = Toast.makeText(this, "This device does not have the necessary cryptographic algorithms!", Toast.LENGTH_LONG);
                toast.show();

                return;
            }
        }

        try {
            final Payload decoded = handler.decode(session.getImage(), header, password);
            final File decodedFilesDir = new File(getApplicationContext().getExternalFilesDir(null), DecodeMessageActivity.DECODED_PAYLOAD_FOLDER);
            final File decodedFile = new File(decodedFilesDir, decoded.fileName());

            final FileOutputStream fos = new FileOutputStream(decodedFile);
            fos.write(decoded.data());
            fos.close();

            final Intent intent = new Intent(this, DecodeMessageActivity.class);
            intent.putExtra(DecodeMessageActivity.DECODED_FILE_KEY, decodedFile);
            intent.putExtra(DecodeMessageActivity.DECODED_MIMETYPE_KEY, decoded.mimeType());

            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Unable to decode image!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Sets the visibility of the two encode/decode floating action buttons based on the state of the
     * session. The encode button should only be available if an image is selected, and the decode button
     * should only be available if the image contains a valid secret.
     *
     * @param session current session object
     */
    private final void setFABVisibility(final SessionObject session) {
        if (session.getImage() != null) {
            if (session.imageContainsSecret()) {
                decodeFAB.setVisibility(View.VISIBLE);
            } else {
                decodeFAB.setVisibility(View.GONE);
            }

            encodeFAB.setVisibility(View.VISIBLE);
        } else {
            encodeFAB.setVisibility(View.GONE);
            decodeFAB.setVisibility(View.GONE);
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

                deleteAllFiles(sessionDirectory);

                return new SessionObject(sessionPath);
            } else {
                return SessionObject.loadFrom(this, sessionPath);
            }
        }
    }

    /**
     * Recursively deletes all files and folders in a directory.
     *
     * @param dir root directory (won't be deleted)
     */
    private final void deleteAllFiles(final File dir) {
        if (!dir.isDirectory()) {
            return;
        }

        for (final File file : dir.listFiles()) {
            if (file.isDirectory()) {
                deleteAllFiles(file);
            }

            file.delete();
        }
    }

    /**
     * Deletes everything in the app's external data folder. This folder may contain sensitive
     * decoded and unencrypted files that need to be deleted.
     */
    public void deleteAppExternalData() {
        final File externalFilesDir = getApplicationContext().getExternalFilesDir(null);
        deleteAllFiles(externalFilesDir);
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

    /**
     * Gets the image state of a new image, and sets some instance variables (package header and
     * package handler). The image state determines the control flow of the application.
     *
     * @param imageData new image
     */
    private void handleNewImage(final ImageData imageData) {
        final long versionCode = PackageFunctions.versionCode(imageData);
        System.out.println("FOOBI Version Code: " + Long.toHexString(versionCode));
        final PackageHandler handler = Versions.getHandler(versionCode);
        final EncodedImageState state;
        final PackageHeader packageHeader;

        if (handler == null) {
            state = EncodedImageState.NO_SECRET;
            packageHeader = null;

            this.header = null;
            this.handler = Versions.getHandler(Versions.THIS_VERSION);
        } else {
            packageHeader = handler.extractHeader(imageData);
            state = handler.getImageState(imageData, packageHeader);

            this.header = packageHeader;
            this.handler = handler;
        }

        splitOnImageState(state, session, this.header, this.handler);
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
                session.setImageContainsSecret(this, true);

                final DecodeSecretDialog dialog = new DecodeSecretDialog();
                dialog.setCancelable(false);
                dialog.setArguments(bundle);
                dialog.setCallingActivity(this);
                dialog.show(getSupportFragmentManager(), "decodeSecret");

                break;
            }
            case CORRUPTED: {
                session.setImageContainsSecret(this, false);

                final CorruptedImageDialog dialog = new CorruptedImageDialog();
                dialog.setCancelable(false);
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "corruptedImage");

                break;
            }
            case UNSUPPORTED: {
                session.setImageContainsSecret(this, false);

                final UnsupportedAlgorithmDialog dialog = new UnsupportedAlgorithmDialog();
                dialog.setCancelable(false);
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "unsupportedAlgorithm");

                break;
            }
            default:
            case NO_SECRET: {
                session.setImageContainsSecret(this, false);

                final EncodeSecretDialog dialog = new EncodeSecretDialog();
                dialog.setCancelable(false);
                dialog.setArguments(bundle);
                dialog.show(getSupportFragmentManager(), "encodeSecret");

                break;
            }
        }

        setFABVisibility(session);
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
