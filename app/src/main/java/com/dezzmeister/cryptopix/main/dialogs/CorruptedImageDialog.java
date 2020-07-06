package com.dezzmeister.cryptopix.main.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.dezzmeister.cryptopix.R;
import com.dezzmeister.cryptopix.main.session.SessionObject;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class CorruptedImageDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        final SessionObject session = (SessionObject) bundle.getSerializable(DialogArgs.SESSION_OBJECT_KEY);
        final

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.corrupted_image_dialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO: Start activity to encode secret message
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO: Do nothing
                    }
                });

        return builder.create();
    }
}
