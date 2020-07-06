package com.dezzmeister.cryptopix.main.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.dezzmeister.cryptopix.R;
import com.dezzmeister.cryptopix.main.activities.EncodeMessageActivity;
import com.dezzmeister.cryptopix.main.secret.PackageHandler;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;
import com.dezzmeister.cryptopix.main.session.SessionObject;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class EncodeSecretDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        final SessionObject session = (SessionObject) bundle.getSerializable(DialogArgs.SESSION_OBJECT_KEY);
        final PackageHeader header = (PackageHeader) bundle.getSerializable(DialogArgs.PACKAGE_HEADER_KEY);
        final PackageHandler handler = (PackageHandler) bundle.getSerializable(DialogArgs.PACKAGE_HANDLER_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.encode_secret_dialog)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Intent intent = new Intent(getContext(), EncodeMessageActivity.class);
                        intent.putExtra(DialogArgs.SESSION_OBJECT_KEY, session);
                        intent.putExtra(DialogArgs.PACKAGE_HEADER_KEY, header);
                        intent.putExtra(DialogArgs.PACKAGE_HANDLER_KEY, handler);

                        getContext().startActivity(intent);
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
