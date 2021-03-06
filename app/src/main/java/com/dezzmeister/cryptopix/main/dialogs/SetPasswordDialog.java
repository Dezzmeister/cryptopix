package com.dezzmeister.cryptopix.main.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.dezzmeister.cryptopix.R;
import com.dezzmeister.cryptopix.main.activities.EncodeMessageActivity;
import com.dezzmeister.cryptopix.main.secret.PackageHandler;
import com.dezzmeister.cryptopix.main.secret.PackageHeader;
import com.dezzmeister.cryptopix.main.session.SessionObject;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * A dialog box to set the password for a secret payload.
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class SetPasswordDialog extends DialogFragment {
    private EncodeMessageActivity callingActivity;

    public void setCallingActivity(final EncodeMessageActivity _callingActivity) {
        callingActivity = _callingActivity;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.set_password_dialog, null);
        final EditText textBox = view.findViewById(R.id.password_textbox);
        textBox.setFilters(new InputFilter[] {new AsciiInputFilter()});

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.set_password_dialog_title)
               .setView(view)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final EditText textBox = view.findViewById(R.id.password_textbox);
                        final String password = textBox.getText().toString();
                        callingActivity.setPassword(password);
                    }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO: Do nothing
                    }
               });

        final AlertDialog dialog = builder.create();

        return dialog;
    }
}
