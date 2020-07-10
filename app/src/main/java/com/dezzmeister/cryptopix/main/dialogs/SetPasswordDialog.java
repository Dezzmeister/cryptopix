package com.dezzmeister.cryptopix.main.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import com.dezzmeister.cryptopix.R;
import com.dezzmeister.cryptopix.main.activities.EncodeMessageActivity;
import com.dezzmeister.cryptopix.main.secret.PackageHandler;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;
import com.dezzmeister.cryptopix.main.session.SessionObject;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SetPasswordDialog extends DialogFragment {
    private EncodeMessageActivity callingActivity;

    public void setCallingActivity(final EncodeMessageActivity _callingActivity) {
        callingActivity = _callingActivity;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.encode_secret_dialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO: Do nothing
                    }
                });

        final AlertDialog dialog = builder.create();
        
        return dialog;
    }
}
