package com.mathwhiz.app.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/** Small helpers shared by the screens. */
final class UiUtils {
    private UiUtils() {}

    static void show(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    static void toast(Context c, String message) {
        Toast.makeText(c, message, Toast.LENGTH_LONG).show();
    }

    static void showError(TextView tv, String message) {
        if (message == null) {
            tv.setVisibility(View.GONE);
        } else {
            tv.setText(message);
            tv.setVisibility(View.VISIBLE);
        }
    }
}
