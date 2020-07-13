package com.dezzmeister.cryptopix.main.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dezzmeister.cryptopix.R;
import com.dezzmeister.cryptopix.main.images.ImageData;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DecodeMessageActivity extends AppCompatActivity {

    public static final String DECODED_FILE_KEY = "decoded_file";

    public static final String DECODED_MIMETYPE_KEY = "decoded_mimetype";

    public static final String DECODED_PAYLOAD_FOLDER = "decoded";

    private static final int REQUEST_SAVE_FILE = 1;

    private File decodedFile;

    private String decodedFileMimeType;

    private byte[] decodedFileData;

    private Button saveFileButton;

    private Button sendFileButton;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        decodedFile = (File) intent.getSerializableExtra(DECODED_FILE_KEY);
        decodedFileMimeType = (String) intent.getSerializableExtra(DECODED_MIMETYPE_KEY);

        try {
            decodedFileData = loadFile(decodedFile);
        } catch (Exception e) {
            e.printStackTrace();

            final Toast toast = Toast.makeText(this, "Unable to load decoded file!", Toast.LENGTH_SHORT);
            toast.show();
        }

        setContentView(R.layout.activity_decode_message);

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

        final TextView fileNameTextView = findViewById(R.id.filename);
        fileNameTextView.setText(decodedFile.getName());

        saveFileButton = findViewById(R.id.save_file);
        saveFileButton.setOnClickListener(this::onSaveButtonPressed);
        sendFileButton = findViewById(R.id.send_file);
        sendFileButton.setOnClickListener(this::onSendButtonPressed);
    }

    /**
     * Loads a file as an array of bytes.
     *
     * @param file file to load
     * @return array of bytes
     * @throws IOException if there is a problem reading the file
     */
    private byte[] loadFile(final File file) throws IOException {
        final RandomAccessFile raf = new RandomAccessFile(file, "r");
        final byte[] out = new byte[(int) raf.length()];
        raf.readFully(out);

        return out;
    }

    private final void onSaveButtonPressed(final View view) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(decodedFileMimeType);
        intent.putExtra(Intent.EXTRA_TITLE, decodedFile.getName());
        startActivityForResult(intent, REQUEST_SAVE_FILE);
    }

    private final void onSendButtonPressed(final View view) {
        final Uri fileURI = Uri.fromFile(decodedFile);

        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, fileURI);
        intent.setType(decodedFileMimeType);
        startActivity(Intent.createChooser(intent, "Send to"));
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_SAVE_FILE: {

                if (resultCode == RESULT_OK) {
                    try (final OutputStream outputStream = getContentResolver().openOutputStream(data.getData())) {
                        if (outputStream != null && decodedFileData != null) {
                            outputStream.write(decodedFileData);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                        final Toast toast = Toast.makeText(this, "Unable to save decoded file!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
        }
    }
}
