package com.rebeatme.android;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Point point = new Point();
        System.out.println("point x:" + point.x + " y:" + point.y);
        getWindowManager().getDefaultDisplay().getSize(point);
        gameView = new GameView(this, point.x, point.y);

        FrameLayout framelayout = new FrameLayout(this);
        framelayout.setLayoutParams(new AbsListView.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        framelayout.addView(gameView);

        TextView textView1 = new TextView(this);
        textView1.setText("Score: 123");
        textView1.setTextSize(25);
        textView1.setGravity(Gravity.CENTER_HORIZONTAL);
        textView1.setTextColor(Color.parseColor("#333333"));
        textView1.setTypeface(null, Typeface.BOLD);
        framelayout.addView(textView1);

        Display display = getWindowManager().getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        int width = screenSize.x;
        int height = screenSize.y;

        MiddleLineView middleLineView = new MiddleLineView(this, screenSize);
        framelayout.addView(middleLineView);

        setContentView(framelayout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
}