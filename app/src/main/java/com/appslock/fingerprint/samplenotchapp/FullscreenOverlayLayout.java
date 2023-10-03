package com.appslock.fingerprint.samplenotchapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class FullscreenOverlayLayout extends FrameLayout {

    public FullscreenOverlayLayout(Context context) {
        super(context);
        init(context);
    }

    public FullscreenOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FullscreenOverlayLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point screenSize = new Point();
        windowManager.getDefaultDisplay().getRealSize(screenSize);

        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(screenSize.x, MeasureSpec.getMode(MeasureSpec.UNSPECIFIED));
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(screenSize.y, MeasureSpec.getMode(MeasureSpec.UNSPECIFIED));

        super.measure(widthMeasureSpec, heightMeasureSpec);
    }
}