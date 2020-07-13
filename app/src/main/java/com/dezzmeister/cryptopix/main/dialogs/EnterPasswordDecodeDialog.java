package com.dezzmeister.cryptopix.main.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.dezzmeister.cryptopix.MainActivity;
import com.dezzmeister.cryptopix.R;
import com.dezzmeister.cryptopix.main.activities.EncodeMessageActivity;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class EnterPasswordDecodeDialog extends DialogFragment {
    private MainActivity callingActivity;

    public void setCallingActivity(final MainActivity _callingActivity) {
        callingActivity = _callingActivity;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.decode_password_dialog, null);
        final EditText textBox = view.findViewById(R.id.password_textbox);
        textBox.setFilters(new InputFilter[] {new AsciiInputFilter()});

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.enter_password_decode_dialog_title)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final EditText textBox = view.findViewById(R.id.password_textbox);
                        final String password = textBox.getText().toString();
                        callingActivity.decodeImage(password);
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
