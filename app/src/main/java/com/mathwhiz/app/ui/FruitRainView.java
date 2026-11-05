package com.mathwhiz.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.mathwhiz.app.logic.FruitCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class FruitRainView extends View {
    private static final long DURATION_MS = 2800L;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final List<Drop> drops = new ArrayList<>();
    private long startedAt;

    public FruitRainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint.setTypeface(Typeface.DEFAULT);
        setVisibility(INVISIBLE);
    }

    public void start(List<String> fruits) {
        drops.clear();
        if (fruits == null || fruits.isEmpty()) return;
        int count = Math.max(12, fruits.size() * 4);
        for (int i = 0; i < count; i++) {
            String fruit = fruits.get(i % fruits.size());
            drops.add(new Drop(FruitCatalog.emojiFor(fruit), random.nextFloat(),
                    -random.nextFloat(), 0.75f + random.nextFloat() * 0.8f,
                    random.nextFloat() * 0.8f));
        }
        startedAt = System.currentTimeMillis();
        setVisibility(VISIBLE);
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long elapsed = System.currentTimeMillis() - startedAt;
        float progress = Math.min(1f, elapsed / (float) DURATION_MS);
        paint.setTextSize(34f * getResources().getDisplayMetrics().scaledDensity);
        paint.setTextAlign(Paint.Align.CENTER);
        for (Drop drop : drops) {
            float y = (drop.startY + progress * (1.25f + drop.speed)) * getHeight();
            float x = drop.x * getWidth();
            canvas.drawText(drop.emoji, x, y, paint);
        }
        if (progress < 1f) {
            postInvalidateOnAnimation();
        } else {
            setVisibility(INVISIBLE);
        }
    }

    private static final class Drop {
        final String emoji;
        final float x;
        final float startY;
        final float speed;
        final float delay;

        Drop(String emoji, float x, float startY, float speed, float delay) {
            this.emoji = emoji == null ? "\uD83C\uDF4E" : emoji;
            this.x = x;
            this.startY = startY - delay;
            this.speed = speed;
            this.delay = delay;
        }
    }
}
