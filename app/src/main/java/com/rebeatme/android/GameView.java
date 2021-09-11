package com.rebeatme.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class GameView extends SurfaceView implements Runnable {
    private boolean isPlaying =false;
    private Thread thread;

    private final com.rebeatme.android.Brick brick;
    private final Paint paint;
    private final SurfaceHolder surfaceHolder;
    private final BlockingQueue<com.rebeatme.android.Ball> balls;
    private final com.rebeatme.android.Ball measureBall;
    private final int[] ballsX;

    private int delay = 0;
    private final int screenX;
    private final int screenY;
    private final TextView scoreView;
    private int score = 0;

    public GameView(Context context, TextView scoreView, int screenX, int screenY) {
        super(context);

        this.scoreView = scoreView;

        this.screenX = screenX;
        this.screenY = screenY;


        System.out.println("Screen x: "+screenX + " Y: "+screenY);
        brick = new com.rebeatme.android.Brick(context);
        brick.y = screenY/2 - brick.height/2;
        brick.x = (screenX / 2) - brick.width/2;

        surfaceHolder = getHolder();
        paint = new Paint();

        measureBall  = new com.rebeatme.android.Ball(context);
        ballsX = new int[]
        {
                0,
                measureBall.width,
                measureBall.width * 2,
                measureBall.width * 3,
                measureBall.width * 4,
                measureBall.width * 5,
                measureBall.width * 6
        };
        balls = new LinkedBlockingDeque<>();
        balls.clear();
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }

    private void update() {
        com.rebeatme.android.Ball ball;
        if ((delay%40) == 0) {
            ball = new com.rebeatme.android.Ball(getContext());
            int index = getRandomNumber(0, 6);
            ball.x = ballsX[index];
            ball.y = -measureBall.height;
            balls.add(ball);
        }
        delay++;

        for (com.rebeatme.android.Ball update : balls) {
            update.y += 10;
            if (Rect.intersects(update.getCollisionShape(),
                                brick.getCollisionShape())) {

                // catched
                score++;
                scoreView.setText("Score: "+score);
                update.catched = true;
                balls.remove(update);

            }
            if (update.y > screenY) {
                balls.remove(update);
            }
        }
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(brick.brick, brick.x, brick.y, paint);

            for (com.rebeatme.android.Ball ball : balls) {
                if (!ball.catched) {
                    canvas.drawBitmap(ball.ball, ball.x, ball.y, paint);
                }
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {

        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {

        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    private void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {

        }
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getX() > (screenX / 2) - brick.width / 2) {
                if (brick.x < screenX - brick.width) {
                    brick.x += brick.width / 2;
                }
            } else {
                if (brick.x > 0) {
                    brick.x -= brick.width / 2;
                }
            }
        }
        return true;
    }
}
