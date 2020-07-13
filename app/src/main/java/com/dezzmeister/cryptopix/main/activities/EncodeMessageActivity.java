package com.dezzmeister.cryptopix.main.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dezzmeister.cryptopix.R;
import com.dezzmeister.cryptopix.main.dialogs.DialogArgs;
import com.dezzmeister.cryptopix.main.dialogs.SetPasswordDialog;
import com.dezzmeister.cryptopix.main.exceptions.SizeLimitExceededException;
import com.dezzmeister.cryptopix.main.images.ImageData;
import com.dezzmeister.cryptopix.main.secret.EncodingOptions;
import com.dezzmeister.cryptopix.main.secret.PackageHandler;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;
import com.dezzmeister.cryptopix.main.secret.Payload;
import com.dezzmeister.cryptopix.main.secret.Versions;
import com.dezzmeister.cryptopix.main.session.SessionObject;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import javassist.bytecode.ByteArray;

/**
 * An activity to hide a file in an image.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class EncodeMessageActivity extends AppCompatActivity {

    public static final String ENCODED_IMAGE_FOLDER = "encoded";

    private static final int REQUEST_OPEN_FILE = 1;

    private static final int REQUEST_SAVE_FILE = 2;

    private SessionObject sessionObject;
    private PackageHeader packageHeader;
    private PackageHandler packageHandler;
    private Payload payload;

    private EncodingOptions encodingOptions;

    private CheckBox passwordCheckbox;
    private TextView fileNameView;
    private Button saveFileButton;
    private Button sendFileButton;

    /**
     * A quick hack to ensure that the password checkbox is only checked when the user supplies a password
     */
    private boolean ignorePasswordCheckboxLogic = false;

    private ImageData imageToSave = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        sessionObject = (SessionObject) intent.getSerializableExtra(DialogArgs.SESSION_OBJECT_KEY);
        sessionObject.resolveImagePath();

        packageHeader = (PackageHeader) intent.getSerializableExtra(DialogArgs.PACKAGE_HEADER_KEY);
        packageHandler = (PackageHandler) intent.getSerializableExtra(DialogArgs.PACKAGE_HANDLER_KEY);

        setContentView(R.layout.activity_encode_message);

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

        encodingOptions = new EncodingOptions();
        encodingOptions.versionCode = Versions.THIS_VERSION;

        final Button selectFileButton = findViewById(R.id.select_file);
        selectFileButton.setOnClickListener(this::onSelectFile);

        passwordCheckbox = findViewById(R.id.set_password);
        passwordCheckbox.setChecked(false);
        passwordCheckbox.setOnCheckedChangeListener(this::onSetPasswordCheckboxChanged);

        final CheckBox compressFileCheckbox = findViewById(R.id.compress_file);
        compressFileCheckbox.setChecked(true);
        compressFileCheckbox.setOnCheckedChangeListener(this::onCompressFileCheckboxChanged);

        fileNameView = findViewById(R.id.filename);
        saveFileButton = findViewById(R.id.save_file);
        saveFileButton.setOnClickListener(this::onSaveButtonPressed);
        sendFileButton = findViewById(R.id.send_file);
        sendFileButton.setOnClickListener(this::onSendButtonPressed);
        disableExportFunctions();
    }

    private final File encodeAndSaveExternal() {
        final File filesDir = getApplicationContext().getExternalFilesDir(null);
        final File encodedImageDir = new File(filesDir, EncodeMessageActivity.ENCODED_IMAGE_FOLDER);

        try {
            final ImageData encoded = encode();

            final Bitmap bitmap = Bitmap.createBitmap(encoded.pixels, encoded.width, encoded.height, Bitmap.Config.ARGB_8888);
            final String fileName = System.currentTimeMillis() + ".png";
            final File file = new File(encodedImageDir, fileName);

            final FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

            return file;
        } catch (IOException e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Unable to save encoded image to external storage!", Toast.LENGTH_LONG);
            toast.show();
        }

        return null;
    }

    private final ImageData encode() {

        // TODO: Show different messages for each exception
        try {
            final ImageData encoded = packageHandler.encodeSecret(sessionObject.getImage(), payload, encodingOptions);

            return encoded;
        } catch (SizeLimitExceededException e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Selected file is too large!", Toast.LENGTH_SHORT);
            toast.show();

            return null;
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "An unknown error occurred", Toast.LENGTH_SHORT);
            toast.show();

            return null;
        }
    }

    private final void onSaveButtonPressed(final View view) {
        final ImageData encodedImage = encode();
        imageToSave = encodedImage;

        if (encodedImage == null) {
            return;
        }

        final long timeStamp = System.currentTimeMillis();
        final String fileName = timeStamp + ".png";

        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, REQUEST_SAVE_FILE);
    }

    /**
     * Encodes the {@linkplain #payload} using {@link #encodingOptions}. Creates a chooser to select an app
     * to send the secret file.
     *
     * @param view view
     */
    private final void onSendButtonPressed(final View view) {
        final File encodedImage = encodeAndSaveExternal();
        final Uri imageURI = Uri.fromFile(encodedImage);

        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, imageURI);
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent, "Send to"));
    }

    /**
     * Disables the buttons that export the encoded file. The buttons are disabled as long as no file
     * has been selected.
     */
    private final void disableExportFunctions() {
        saveFileButton.setVisibility(View.GONE);
        saveFileButton.setClickable(false);

        sendFileButton.setVisibility(View.GONE);
        sendFileButton.setClickable(false);
    }

    /**
     * Enables the buttons that export the encoded file. The buttons are enabled when a file is selected.
     */
    private final void enableExportFunctions() {
        saveFileButton.setVisibility(View.VISIBLE);
        saveFileButton.setClickable(true);

        sendFileButton.setVisibility(View.VISIBLE);
        sendFileButton.setClickable(true);
    }

    /**
     * Runs when the "Select File" button is pressed. Creates an intent to select a file of any type
     * and starts an activity.
     *
     * @param v view
     */
    private final void onSelectFile(final View v) {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_OPEN_FILE);
        }
    }

    private final void onSetPasswordCheckboxChanged(final CompoundButton buttonView, final boolean isChecked) {
        if (!ignorePasswordCheckboxLogic) {
            if (!isChecked) {
                encodingOptions.password = null;
            } else {
                final SetPasswordDialog dialog = new SetPasswordDialog();
                dialog.setCallingActivity(this);
                dialog.setCancelable(false);
                passwordCheckbox.setChecked(false);

                dialog.show(getSupportFragmentManager(), "setPassword");
            }
        }

        ignorePasswordCheckboxLogic = false;
    }

    public void setPassword(final String password) {
        encodingOptions.password = password;
        ignorePasswordCheckboxLogic = true;
        passwordCheckbox.setChecked(true);
    }

    /**
     * Runs when the "Compress File" checkbox is toggled.
     *
     * @param buttonView checkbox view
     * @param isChecked true if the checkbox is checked, false if unchecked
     */
    private final void onCompressFileCheckboxChanged(final CompoundButton buttonView, final boolean isChecked) {
        encodingOptions.compress = isChecked;
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_OPEN_FILE: {
                if (resultCode == RESULT_OK) {
                    final Uri contentURI = data.getData();
                    try {
                        final InputStream is = getContentResolver().openInputStream(contentURI);
                        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        int numRead;
                        byte[] buffer = new byte[1024];
                        while ((numRead = is.read(buffer, 0, buffer.length)) != -1) {
                            baos.write(buffer, 0, numRead);
                        }

                        final byte[] payloadData = baos.toByteArray();
                        final String mimeType = getContentResolver().getType(contentURI);
                        final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                        final String extension = mimeTypeMap.getExtensionFromMimeType(mimeType);
                        final String timeStamp = System.currentTimeMillis() + "";
                        final String fileName = timeStamp + "." + extension;

                        payload = packageHandler.getEmptyPayload();

                        payload.setFileName(fileName);
                        payload.setMimeType(mimeType);
                        payload.setData(payloadData);

                        fileNameView.setText(fileName);

                        enableExportFunctions();
                    } catch (Exception e) {
                        e.printStackTrace();

                        final Toast toast = Toast.makeText(this, "Unable to read file!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            case REQUEST_SAVE_FILE: {
                if (resultCode == RESULT_OK) {
                    try (final OutputStream outputStream = getContentResolver().openOutputStream(data.getData())) {
                        if (outputStream != null && imageToSave != null) {
                            final Bitmap bitmap = Bitmap.createBitmap(imageToSave.pixels, imageToSave.width, imageToSave.height, Bitmap.Config.ARGB_8888);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                        final Toast toast = Toast.makeText(this, "Unable to save encoded image!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
        }
    }
}
