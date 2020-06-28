package com.dezzmeister.cryptopix.maindialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.dezzmeister.cryptopix.R;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * A dialog box presenting an option for the user to extract a hidden message from an image,
 * or hide a message in an image.
 *
 * This dialog expects an image with the key {@link EncodeOrDecodeDialog#IMAGE_KEY}.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class EncodeOrDecodeDialog extends DialogFragment {
    /**
     * The key to use when passing a bitmap image to this DialogFragment
     */
    public static final String IMAGE_KEY = "bitmap_image";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        final Bitmap image = (Bitmap) bundle.getParcelable(IMAGE_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.main_dialog_encode_or_decode)
                .setPositiveButton(R.string.main_dialog_eod_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Start activity to get secret message
                    }
                })
                .setNegativeButton(R.string.main_dialog_eod_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Start activity to hide secret message
                    }
                });

        return builder.create();
    }
}
