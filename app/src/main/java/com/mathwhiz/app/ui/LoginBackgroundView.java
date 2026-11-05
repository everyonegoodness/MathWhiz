package com.mathwhiz.app.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.mathwhiz.app.R;

/** Repeats and scrolls the illustrated login backdrop without a visible tile seam. */
public class LoginBackgroundView extends View {
    private static final float TILT_DEGREES = 25f;
    private static final long ANIMATION_DURATION_MS = 45000L;

    private final Bitmap tile;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Rect source = new Rect();
    private final RectF destination = new RectF();
    private ValueAnimator animator;
    private float tileWidth;
    private float tileHeight;
    private float offset;

    public LoginBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Bitmap decoded = BitmapFactory.decodeResource(getResources(), R.drawable.login_background_tile);
        if (decoded == null) {
            throw new IllegalStateException("Could not decode the login background tile.");
        }
        tile = decoded;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        stopAnimation();
        double radians = Math.toRadians(TILT_DEGREES);
        tileWidth = (float) (width * Math.cos(radians) + height * Math.sin(radians));
        tileHeight = tileWidth * (float) tile.getHeight() / tile.getWidth();
        offset = 0f;
        startAnimation();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tileHeight <= 0f || tileWidth <= 0f || getWidth() == 0) return;

        float radians = (float) Math.toRadians(TILT_DEGREES);
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        float boundsHeight = getWidth() * sine + getHeight() * cosine;
        float left = (getWidth() - tileWidth) / 2f;
        float top = (getHeight() - boundsHeight) / 2f + offset - tileHeight;

        int save = canvas.save();
        canvas.clipRect(0, 0, getWidth(), getHeight());
        canvas.rotate(TILT_DEGREES, getWidth() / 2f, getHeight() / 2f);
        source.set(0, 0, tile.getWidth(), tile.getHeight());
        while (top < (getHeight() + boundsHeight) / 2f) {
            destination.set(left, top, left + tileWidth, top + tileHeight);
            canvas.drawBitmap(tile, source, destination, paint);
            top += tileHeight;
        }
        canvas.restoreToCount(save);
    }

    private void startAnimation() {
        if (animator != null || tileHeight <= 0f || !isAttachedToWindow()) return;

        animator = ValueAnimator.ofFloat(0f, tileHeight);
        animator.setDuration(ANIMATION_DURATION_MS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            offset = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stopAnimation() {
        if (animator == null) return;
        animator.cancel();
        animator = null;
    }
}
