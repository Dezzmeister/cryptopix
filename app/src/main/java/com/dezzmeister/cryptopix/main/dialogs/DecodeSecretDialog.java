package com.dezzmeister.cryptopix.main.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.dezzmeister.cryptopix.MainActivity;
import com.dezzmeister.cryptopix.R;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DecodeSecretDialog extends DialogFragment {

    private MainActivity callingActivity;

    public void setCallingActivity(final MainActivity _callingActivity) {
        callingActivity = _callingActivity;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        final PackageHeader header = (PackageHeader) bundle.get(DialogArgs.PACKAGE_HEADER_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.decode_secret_dialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!header.isPasswordProtected()) {
                            callingActivity.decodeImage(null);
                        } else {
                            final EnterPasswordDecodeDialog dialog = new EnterPasswordDecodeDialog();
                            dialog.setCancelable(false);
                            dialog.setCallingActivity(callingActivity);
                            dialog.show(callingActivity.getSupportFragmentManager(), "enterPassword");
                        }
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
