package com.rebeatme.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Brick {

    public int x;
    public int y;
    public int width;
    public int height;
    public Bitmap brick;

    public Brick(Context context) {
        brick = BitmapFactory.decodeResource(context.getResources(), R.drawable.brick);
        width = brick.getWidth();
        height = brick.getHeight();
        width/=4;
        height/=4;

        y = 0;
        x = 0;

        brick = Bitmap.createScaledBitmap(brick, width, height, false);
    }

    public Rect getCollisionShape () {
        return new Rect(x, y, x + width, y + height);
    }
}
