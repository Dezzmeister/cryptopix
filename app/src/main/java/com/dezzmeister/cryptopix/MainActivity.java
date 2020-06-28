package com.dezzmeister.cryptopix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.dezzmeister.cryptopix.maindialogs.EncodeOrDecodeDialog;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.io.FileNotFoundException;
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
    private static final int REQUEST_IMAGE_GET = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private ImageView mainImageView;
    private File imageDirectory;
    private String currentPhotoPath;
    private Bitmap fullSizeImage;

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
        final String imageFileName = "PNG_" + timeStamp + "_";
        final File image = File.createTempFile(imageFileName, ".png", imageDirectory);

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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_IMAGE_GET: {
                if (resultCode == RESULT_OK) {
                    final Uri contentURI = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(contentURI));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        final Toast toast = Toast.makeText(this, "File does not exist!", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }

                    fullSizeImage = bitmap;
                    mainImageView.setImageBitmap(bitmap);

                    super.onActivityResult(requestCode, resultCode, data);
                    showEncodeDecodeDialog(fullSizeImage);

                    return;
                }
            }
            case REQUEST_IMAGE_CAPTURE: {
                if (resultCode == RESULT_OK) {
                    final Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

                    fullSizeImage = bitmap;
                    mainImageView.setImageBitmap(bitmap);

                    super.onActivityResult(requestCode, resultCode, data);
                    showEncodeDecodeDialog(fullSizeImage);

                    return;
                }
            }
            default: {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * Shows a dialog allowing the user to hide a message, or extract a hidden message.
     * Passes the image to the dialog fragment.
     *
     * @param image image to hide secret message in or extract secret message from
     */
    private void showEncodeDecodeDialog(final Bitmap image) {
        final EncodeOrDecodeDialog dialog = new EncodeOrDecodeDialog();
        dialog.setCancelable(false);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(EncodeOrDecodeDialog.IMAGE_KEY, image);
        dialog.setArguments(bundle);

        dialog.show(getSupportFragmentManager(), "encodeOrDecode");
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
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
