package com.rebeatme.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Ball {

    public boolean catched = false;
    public int x = 0;
    public int y = 0;
    public Bitmap ball;
    public int width = 0;
    public int height = 0;

    public Ball(Context context) {
        ball = BitmapFactory.decodeResource(context.getResources(), R.drawable.ball);

        width  = ball.getWidth();
        height = ball.getHeight();

        width /= 4;
        height /= 4;

        ball = Bitmap.createScaledBitmap(ball,width, height, false);
    }

    public Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}
