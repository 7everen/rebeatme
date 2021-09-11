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

        TextView scoreView = new TextView(this);
        scoreView.setText("Score: 0");
        scoreView.setTextSize(25);
        scoreView.setGravity(Gravity.CENTER_HORIZONTAL);
        scoreView.setTextColor(Color.parseColor("#333333"));
        scoreView.setTypeface(null, Typeface.BOLD);

        gameView = new GameView(this, scoreView, point.x, point.y);

        FrameLayout framelayout = new FrameLayout(this);
        framelayout.setLayoutParams(new AbsListView.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        framelayout.addView(gameView);


        framelayout.addView(scoreView);

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