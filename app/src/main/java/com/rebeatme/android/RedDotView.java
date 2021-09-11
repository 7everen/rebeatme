package com.rebeatme.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class MiddleLineView extends View {

    Paint paint = new Paint();
    Point screen = null;

    private void init() {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(10);
    }

    public MiddleLineView(Context context) {
        super(context);
        init();
    }

    public MiddleLineView(Context context, Point screen) {
        super(context);
        this.screen = screen;
        init();
    }

    public MiddleLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiddleLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawLine(0, screen.y/2, 1080, screen.y/2, paint);
    }

}
