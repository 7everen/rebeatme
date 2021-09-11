package com.rebeatme.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

public class RedDotView extends View {

    Paint paint = new Paint();
    Point screen = null;
    int x = 0;
    int y = 0;


    private void init() {
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
    }

    public RedDotView(Context context) {
        super(context);
        init();
    }

    public RedDotView(Context context, Point screen) {
        super(context);
        this.screen = screen;
        init();
    }

    public RedDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RedDotView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setXY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRect(x, y, x+50, y+50, paint);
    }

}
