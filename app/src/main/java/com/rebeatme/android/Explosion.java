package com.rebeatme.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Explosion {

    public int x = 0;
    public int y = 0;
    public Bitmap explosion;
    public int width = 0;
    public int height = 0;

    public Explosion(Context context, int x, int y) {
        explosion = BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion1);

        this.x = x;
        this.y = y;
        width  = explosion.getWidth();
        height = explosion.getHeight();

        width /= 4;
        height /= 4;

        explosion = Bitmap.createScaledBitmap(explosion, width, height, false);
    }

    public Rect getCollisionShape() {
        return new Rect(x, y, x + width, y + height);
    }
}
