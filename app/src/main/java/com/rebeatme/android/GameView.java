package com.rebeatme.android;

import android.content.Context;
import android.graphics.Bitmap;

import android.graphics.Point;
import android.os.Handler;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

public class GameView extends SurfaceView implements Runnable {
    private boolean isPlaying = false;
    private Thread thread;

    private final com.rebeatme.android.Brick brick1;
    private final com.rebeatme.android.Brick brick2;
    private final com.rebeatme.android.Brick brick3;
    private final com.rebeatme.android.Brick brick4;
    private final com.rebeatme.android.Brick brick5;
    private final com.rebeatme.android.Brick brick6;
    private final com.rebeatme.android.Brick brick7;
    private final com.rebeatme.android.Brick brick8;
    private final Paint paint;
    private final SurfaceHolder surfaceHolder;
    private final BlockingQueue<com.rebeatme.android.Ball> balls;
    private final com.rebeatme.android.Ball measureBall;
    private final int[] ballsX;
    private Bitmap bitmap;


    private RedDotView rect1;

    private int delay = 0;
    private final int screenX;
    private final int screenY;
    private final TextView scoreView;
    private int score = 0;

    private float scale = 1.0F;

    public GameView(Context context, TextView scoreView, int screenX, int screenY) {
        super(context);

        this.scoreView = scoreView;

        this.screenX = screenX;
        this.screenY = screenY;


        System.out.println("Screen x: " + screenX + " Y: " + screenY);
        brick1 = new com.rebeatme.android.Brick(context);
        brick2 = new com.rebeatme.android.Brick(context);
        brick3 = new com.rebeatme.android.Brick(context);
        brick4 = new com.rebeatme.android.Brick(context);
        brick5 = new com.rebeatme.android.Brick(context);
        brick6 = new com.rebeatme.android.Brick(context);
        brick7 = new com.rebeatme.android.Brick(context);
        brick8 = new com.rebeatme.android.Brick(context);


        surfaceHolder = getHolder();
        paint = new Paint();

        measureBall = new com.rebeatme.android.Ball(context);
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

        rect1 = new RedDotView(context);
        rect1.setXY(100, 100);
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
        if ((delay % 40) == 0) {
            ball = new com.rebeatme.android.Ball(getContext());
            int index = getRandomNumber(0, 6);
            ball.x = ballsX[index];
            ball.y = -measureBall.height;
            balls.add(ball);
        }
        delay++;

        for (com.rebeatme.android.Ball update : balls) {
            update.y += 10;
            /*if (Rect.intersects(update.getCollisionShape(),
                    brick.getCollisionShape())) {

                // catched
                score++;
                scoreView.setText("Score: " + score);
                //update.catched = true;
                //balls.remove(update);

                //Explosion explosion = new Explosion(getContext(), update.x, update.y);
                *//*new Handler().postDelayed({
                        explosion
                }, 1000);*//*


            }*/
            if (update.y > screenY) {
                balls.remove(update);
            }
        }
    }

    public void setImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            if (bitmap != null) {
                Bitmap rb = getResizedBitmap(bitmap, screenX);
                canvas.drawBitmap(rb, 0, 200, null);
            }
            canvas.drawBitmap(brick1.brick, brick1.x, brick1.y, paint);
            canvas.drawBitmap(brick2.brick, brick2.x, brick2.y, paint);
            canvas.drawBitmap(brick3.brick, brick3.x, brick3.y, paint);
            canvas.drawBitmap(brick4.brick, brick4.x, brick4.y, paint);
            canvas.drawBitmap(brick5.brick, brick5.x, brick5.y, paint);
            canvas.drawBitmap(brick6.brick, brick6.x, brick6.y, paint);
            canvas.drawBitmap(brick7.brick, brick7.x, brick7.y, paint);
            canvas.drawBitmap(brick8.brick, brick8.x, brick8.y, paint);

            for (com.rebeatme.android.Ball ball : balls) {
                if (!ball.catched) {
                    canvas.drawBitmap(ball.ball, ball.x, ball.y, paint);
                }
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = scaleWidth;
        int newHeight = (int) scaleHeight * height;

        scale = scaleWidth;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(-scaleWidth, scaleHeight, newWidth / 2, newHeight / 2);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;
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

    public void processRecognition(List<PoseLandmark> leftHand, List<PoseLandmark> rightHand) {


        List<Point> leftHand2 = leftHand.stream().map(mark -> {
            if(mark != null){
                return new Point(screenX - (int) (scale * mark.getPosition().x), (int) (scale * mark.getPosition().y) + (int) (screenY * 0.05));
            }else{
                return null;
            }
        }).collect(Collectors.toList());

        List<Point> rightHand2 = rightHand.stream().map(mark -> {
            if(mark != null){
                return new Point(screenX - (int) (scale * mark.getPosition().x), (int) (scale * mark.getPosition().y) + (int) (screenY * 0.05));
            }else{
                return null;
            }
        }).collect(Collectors.toList());


        if(leftHand2.get(0) != null) {
            brick1.y = leftHand2.get(0).y;
            brick1.x = leftHand2.get(0).x;
        }

        if(leftHand2.get(1) != null) {
            brick2.y = leftHand2.get(1).y;
            brick2.x = leftHand2.get(1).x;
        }

        if(leftHand2.get(2) != null) {
            brick3.y = leftHand2.get(2).y;
            brick3.x = leftHand2.get(2).x;
        }

        if(leftHand2.get(3) != null) {
            brick4.y = leftHand2.get(3).y;
            brick4.x = leftHand2.get(3).x;
        }

        if(rightHand2.get(0) != null) {
            brick5.y = rightHand2.get(0).y;
            brick5.x = rightHand2.get(0).x;
        }

        if(rightHand2.get(1) != null) {
            brick6.y = rightHand2.get(1).y;
            brick6.x = rightHand2.get(1).x;
        }

        if(rightHand2.get(2) != null) {
            brick7.y = rightHand2.get(2).y;
            brick7.x = rightHand2.get(2).x;
        }

        if(rightHand2.get(3) != null) {
            brick8.y = rightHand2.get(3).y;
            brick8.x = rightHand2.get(3).x;
        }

        balls.stream()
                .filter(ball -> isCaught(ball, leftHand2) || isCaught(ball, rightHand2))
                .forEach(ball -> {

                    Log.i("123", ">>>>>>>~~~~~~!!!!!!!   CATCHED!!!!!!");

                    ball.catched = true;
                    balls.remove(ball);
                });

    }

    private boolean isCaught(Ball ball, List<Point> hand) {
        return hand.stream()
                .filter(m -> m != null)
                .anyMatch(leftHandPoint -> (ball.x + ball.width) > leftHandPoint.x
                        && (ball.x) < leftHandPoint.x
                        && (ball.y + ball.height) > leftHandPoint.y
                        && (ball.y) < leftHandPoint.y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return true;
    }
}
