package com.example.pj.ptr_lib.head;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.Random;

/**
 * Created by pj on 2016/11/3.
 */
public class BombHeadItem extends Animation {
    public static final int BALL_DEFAULT_RADIUS = 16;
    public static final int BALL_DEFAULT_COLOR = Color.BLUE;
    private static final String TAG = "BombHeadItem";
    public int mRadius;
    private int mColor;
    public Point center = new Point();
    private Paint mPaint = new Paint();
    private Point mStartPoint = new Point();
    private Point mEndPoint = new Point();
    public float mProgress;
    public Point curPoint = new Point();
    public float translationX;

    private BombHeadItem(Builder builder) {
        this.mColor = builder.color;
        this.mRadius = builder.radius;
        this.center = builder.center;

        this.curPoint.x = center.x;
        this.curPoint.y = center.y;
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColor);
    }

    public void start(Point startPoint, Point endPoint) {
        mStartPoint = startPoint;
        mEndPoint = endPoint;
        startNow();
    }

    /**
     * @param horizontalRandomness 水平偏移量
     */
    public void resetPosition(int horizontalRandomness) {
        Random random = new Random();
        int randomNumber = -random.nextInt(horizontalRandomness) * 2 + horizontalRandomness;
        translationX = randomNumber;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        mProgress = interpolatedTime;
        Log.i(TAG, "mStartPoint.x:" + mStartPoint.x);
        Log.i(TAG, "mStartPoint.y:" + mStartPoint.y);
        if (interpolatedTime == 1f)
            return;

        if (interpolatedTime > 0.5f) {
            curPoint.x = (int) (mEndPoint.x + (mStartPoint.x - mEndPoint.x) * interpolatedTime);
            curPoint.y = (int) (mEndPoint.y + (mStartPoint.y - mEndPoint.y) * interpolatedTime);
        } else {
            curPoint.x = (int) (mStartPoint.x + (mEndPoint.x - mStartPoint.x) * interpolatedTime * 2);
            curPoint.y = (int) (mStartPoint.y + (mEndPoint.y - mStartPoint.y) * interpolatedTime * 2);

        }
    }

    public void draw(Canvas canvas) {
//        Log.i(TAG, "curPoint.x:" + curPoint.x + "---curPoint.y:" + curPoint.y);
        canvas.drawCircle(curPoint.x, curPoint.y, mRadius, mPaint);
    }

    public void resetPosition() {
        curPoint.x = center.x;
        curPoint.y = center.y;
    }

    public static class Builder {
        private int color;
        private int radius;
        private Point center;

        public Builder withColor(int color) {
            this.color = color;
            return this;
        }

        public Builder withRadius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder withCenter(Point point) {
            this.center = point;
            return this;
        }

        public BombHeadItem build() {
            initFieldsWithDefaultValues();
            return new BombHeadItem(this);
        }

        private void initFieldsWithDefaultValues() {
            if (color == 0)
                color = BALL_DEFAULT_COLOR;
            if (radius == 0)
                radius = BALL_DEFAULT_RADIUS;
        }
    }
}
