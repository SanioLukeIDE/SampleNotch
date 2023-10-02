package com.appslock.fingerprint.samplenotchapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class OverlayDrawingView extends View {

    public OverlayDrawingView(Context context) {
        this(context, null);
    }

    public OverlayDrawingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayDrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}