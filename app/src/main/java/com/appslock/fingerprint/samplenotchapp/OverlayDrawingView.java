package com.appslock.fingerprint.samplenotchapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class OverlayDrawingView extends View {

    private final Paint paint;

    public OverlayDrawingView(Context context) {
        this(context, null);
    }

    public OverlayDrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayDrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() > 0) {
            int W = getWidth();
            int H = getHeight();
            float interval = (float) W / STEP;

            // Draw diagonal lines pattern
            float currentX = 0f;
            float currentY = 0f;
            for (int i = 0; i < STEP + H / interval + 1; i++) {
                canvas.drawLine(currentX, 0f, 0f, currentY, paint);
                currentX += interval;
                currentY += interval;
            }

            canvas.drawRect(0f, 0f, (float) W, (float) H, paint);
        }
    }

    private static final int STEP = 9;
}