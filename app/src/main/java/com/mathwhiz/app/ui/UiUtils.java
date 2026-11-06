package com.mathwhiz.app.ui;

import android.content.Context;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/** Small helpers shared by the screens. */
final class UiUtils {
    private UiUtils() {}

    static void show(View v, boolean visible) {
        v.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Sets the largest text size (between minPx and maxPx) at which the text fits inside
     * availWidth x availHeight. Lines that the text already breaks with '\n' are kept, and the
     * search first tries not to add extra wrapping (so fruit rows stay in groups of five).
     * Replaces android:autoSizeTextType, which does not work on wrap_content TextViews and
     * left the fruit pictures stuck at the minimum size.
     */
    static void fitText(TextView tv, float minPx, float maxPx, int availWidth, int availHeight) {
        CharSequence text = tv.getText();
        if (text == null || text.length() == 0 || availWidth <= 0 || availHeight <= 0) return;
        int naturalLines = 1;
        for (int i = 0; i < text.length(); i++) if (text.charAt(i) == '\n') naturalLines++;

        float best = largestFitting(tv, text, minPx, maxPx, availWidth, availHeight, naturalLines);
        if (best < 0) best = largestFitting(tv, text, minPx, maxPx, availWidth, availHeight, Integer.MAX_VALUE);
        if (best < 0) best = minPx;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, best);
    }

    private static float largestFitting(TextView tv, CharSequence text, float lo, float hi,
                                        int width, int height, int maxLines) {
        TextPaint paint = new TextPaint(tv.getPaint());
        if (fits(tv, paint, text, hi, width, height, maxLines)) return hi;
        if (!fits(tv, paint, text, lo, width, height, maxLines)) return -1;
        while (hi - lo > 1f) {                       // invariant: lo fits, hi does not
            float mid = (lo + hi) / 2f;
            if (fits(tv, paint, text, mid, width, height, maxLines)) lo = mid;
            else hi = mid;
        }
        return lo;
    }

    private static boolean fits(TextView tv, TextPaint paint, CharSequence text, float sizePx,
                                int width, int height, int maxLines) {
        paint.setTextSize(sizePx);
        StaticLayout layout = StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(tv.getLineSpacingExtra(), tv.getLineSpacingMultiplier())
                .setIncludePad(tv.getIncludeFontPadding())
                .build();
        return layout.getLineCount() <= maxLines && layout.getHeight() <= height;
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
