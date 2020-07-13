package com.dezzmeister.cryptopix.main.dialogs;

import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

/**
 * Filters out any non-ASCII characters (anything greater than 0x7F).
 *
 * @author Joe Desmond
 * @since 1.0.0
 */
public class AsciiInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned spanned, int dstart, int dend) {
        if (source instanceof SpannableStringBuilder) {
            final SpannableStringBuilder ssb = (SpannableStringBuilder) source;

            for (int i = end - 1; i >= start; i--) {
                final char currentChar = source.charAt(i);

                if (currentChar > 0x7F) {
                    ssb.delete(i, i + 1);
                }
            }

            return source;
        } else {
            final StringBuilder filtered = new StringBuilder();

            for (int i = start; i < end; i++) {
                final char currentChar = source.charAt(i);

                if (currentChar <= 0x7F) {
                    filtered.append(currentChar);
                }
            }

            return filtered.toString();
        }
    }
}
