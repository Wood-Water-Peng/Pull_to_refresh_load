package com.example.pj.ptr_lib.head;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;

import com.example.pj.ptr_lib.container.PtrContainer;
import com.example.pj.ptr_lib.utils.PtrIndicator;
import com.example.pj.ptr_lib.utils.PtrLocalDisplay;

import java.util.ArrayList;

/**
 * Created by pj on 2016/10/26.
 */
public class TestStoreHouseHead extends View implements PtrHeadUIHandler {

    private static final String TAG = "TestStoreHouseHead";
    public ArrayList<StoreHouseBarItem> mItemList = new ArrayList<StoreHouseBarItem>();

    private int mLineWidth = -1;
    private float mScale = 1;
    private int mDropHeight = -1;
    private float mInternalAnimationFactor = 1.0f;
    private int mHorizontalRandomness = -1;

    private float mProgress = 0;

    private int mDrawZoneWidth = 0;
    private int mDrawZoneHeight = 0;
    private int mOffsetX = 0;
    private int mOffsetY = 0;
    private float mBarDarkAlpha = 0.4f;
    private float mFromAlpha = 1.0f;
    private float mToAlpha = 0.4f;

    private int mLoadingAniDuration = 3000;
    private int mLoadingAniSegDuration = 1000;
    private int mLoadingAniItemDuration = 400;

    private Transformation mTransformation = new Transformation();
    private boolean mIsInLoading = false;
    private AniController mAniController = new AniController();
    private int mTextColor = Color.WHITE;

    public TestStoreHouseHead(Context context) {
        this(context, null);
    }

    public TestStoreHouseHead(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        PtrLocalDisplay.init(getContext());
        mLineWidth = PtrLocalDisplay.dp2px(1);
        mDropHeight = PtrLocalDisplay.dp2px(40);
        mHorizontalRandomness = PtrLocalDisplay.SCREEN_WIDTH_PIXELS / 2;
        ScaleAnimation scaleAnimation = new ScaleAnimation(0,1,0,1);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        setAnimation(scaleAnimation);
        scaleAnimation.start();
    }

    private void setProgress(float progress) {
        mProgress = progress;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        Log.i(TAG, "TopOffset:" + getTopOffset());
        Log.i(TAG, "BottomOffset:" + getBottomOffset());
        Log.i(TAG, "DrawZoneHeight:" + mDrawZoneHeight);

        int height = getTopOffset() + mDrawZoneHeight + getBottomOffset();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        mOffsetX = (getMeasuredWidth() - mDrawZoneWidth) / 2;
        mOffsetY = getTopOffset();
        mDropHeight = getTopOffset();

    }

    private int getTopOffset() {
        return getPaddingTop() + PtrLocalDisplay.dp2px(30);
    }

    private int getBottomOffset() {
        return getPaddingBottom() + PtrLocalDisplay.dp2px(10);
    }

    public void initWithString(String str) {
        initWithString(str, 25);
    }

    public void initWithString(String str, int fontSize) {
        ArrayList<float[]> pointList = StoreHousePath.getPath(str, fontSize * 0.01f, 14);
        initWithPointList(pointList);
    }

    public void initWithPointList(ArrayList<float[]> pointList) {

        float drawWidth = 0;
        float drawHeight = 0;
        boolean shouldLayout = mItemList.size() > 0;
        mItemList.clear();
        for (int i = 0; i < pointList.size(); i++) {
            float[] line = pointList.get(i);
            PointF startPoint = new PointF(PtrLocalDisplay.dp2px(line[0]) * mScale, PtrLocalDisplay.dp2px(line[1]) * mScale);
            PointF endPoint = new PointF(PtrLocalDisplay.dp2px(line[2]) * mScale, PtrLocalDisplay.dp2px(line[3]) * mScale);

            drawWidth = Math.max(drawWidth, startPoint.x);
            drawWidth = Math.max(drawWidth, endPoint.x);

            drawHeight = Math.max(drawHeight, startPoint.y);
            drawHeight = Math.max(drawHeight, endPoint.y);

//            Log.i(TAG, "drawWidth:" + drawWidth + "drawHeight:" + drawHeight);
            StoreHouseBarItem item = new StoreHouseBarItem(i, startPoint, endPoint, mTextColor, mLineWidth);
            item.resetPosition(mHorizontalRandomness);
            Log.i(TAG, "item:" + item.translationX);
            mItemList.add(item);
        }
        mDrawZoneWidth = (int) Math.ceil(drawWidth);
        mDrawZoneHeight = (int) Math.ceil(drawHeight);
        if (shouldLayout) {
            requestLayout();
        }
    }

    private void beginLoading() {
        mIsInLoading = true;
        Log.i(TAG, "---beginLoading---");
        mAniController.start();
        invalidate();  //这是一个保险的方法，加上后思路更清晰
    }

    private void loadFinish() {
        mIsInLoading = false;
        mAniController.stop();
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float progress = mProgress;
        int c1 = canvas.save();
        int len = mItemList.size();

        for (int i = 0; i < len; i++) {

            canvas.save();
            StoreHouseBarItem storeHouseBarItem = mItemList.get(i);
            float offsetX = mOffsetX + storeHouseBarItem.midPoint.x;
            float offsetY = mOffsetY + storeHouseBarItem.midPoint.y;
//            float offsetY = mOffsetY;

//            Log.i(TAG, "storeHouseBarItem.midPoint.y:" + storeHouseBarItem.midPoint.y);

            if (mIsInLoading) {
                Log.i(TAG, "---mIsInLoading---");
                /**
                 * 该方法会导致applyTransformation()方法的调用
                 */
                storeHouseBarItem.getTransformation(getDrawingTime(), mTransformation);  //正式开始执行动画
                canvas.translate(offsetX, offsetY);
            } else {
                Log.i(TAG, "---notLoading---");
                if (progress == 0) {
                    storeHouseBarItem.resetPosition(mHorizontalRandomness);
                    continue;
                }

//                float startPadding = (1 - mInternalAnimationFactor) * i / len;
                float startPadding = 0;
//                float endPadding = 1 - mInternalAnimationFactor - startPadding;
//                float endPadding = 1 - mInternalAnimationFactor - ((1 - mInternalAnimationFactor) * i / len);
                float endPadding = ((1 - mInternalAnimationFactor) * i * 2 / len);
//                Log.i(TAG, "startPadding-endPadding:" + startPadding + "-" + endPadding);

                // done
                if (progress == 1 || progress >= 1 - endPadding) {
//                    Log.i(TAG, "offsetY:" + offsetY);
                    canvas.translate(offsetX, offsetY);
                    storeHouseBarItem.setAlpha(mBarDarkAlpha);
                } else {
                    float realProgress;
                    if (progress <= startPadding) {
                        realProgress = 0;
                    } else {
                        realProgress = Math.min(1, (progress - startPadding) / mInternalAnimationFactor);
                    }

                    offsetX += storeHouseBarItem.translationX * (1 - realProgress);
                    offsetY += -mDropHeight * (1 - realProgress);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(360 * realProgress);
                    matrix.postScale(realProgress, realProgress);
                    matrix.postTranslate(offsetX, offsetY);
                    storeHouseBarItem.setAlpha(mBarDarkAlpha * realProgress);
                    canvas.concat(matrix);
                }
            }
            storeHouseBarItem.draw(canvas);
            canvas.restore();
        }
        if (mIsInLoading) {
            invalidate();
        }
        canvas.restoreToCount(c1);
    }

    @Override
    public void onUIReset() {
        loadFinish();
        for (int i = 0; i < mItemList.size(); i++) {
            mItemList.get(i).resetPosition(mHorizontalRandomness);

        }
    }

    @Override
    public void onUIRefreshPrepare(PtrContainer container) {

    }

    @Override
    public void onUIRefreshBegin() {
        beginLoading();
    }

    @Override
    public void onUIRefreshComplete() {
        loadFinish();
    }

    @Override
    public void onUIPositionChange(PtrContainer container, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

        float currentPercent = Math.min(1f, ptrIndicator.getCurrentPercent());
        setProgress(currentPercent);
        invalidate();
    }

    /**
     * 在真正的加载过程中，总时间是不确定的
     * 我只能确定刷新的频率
     */
    private class AniController implements Runnable {

        private int mTick = 0;
        private int mCountPerSeg = 0;
        private int mSegCount = 0;
        private int mInterval = 0;
        private boolean mRunning = true;

        private void start() {
            mRunning = true;
            mTick = 0;

            mInterval = mLoadingAniDuration / mItemList.size();
            Log.i(TAG, "mInterval:" + mInterval);
            mCountPerSeg = mLoadingAniSegDuration / mInterval;
            mSegCount = mItemList.size() / mCountPerSeg + 1;
            Log.i(TAG, "mSegCount:" + mSegCount);
            run();
        }

        @Override
        public void run() {

            int pos = mTick % mCountPerSeg;
            for (int i = 0; i < mSegCount; i++) {

                int index = i * mCountPerSeg + pos;
                if (index > mTick) {
                    continue;
                }
                Log.i(TAG, "index:" + index);
                Log.i(TAG, "mTick:" + mTick);
                index = index % mItemList.size();
                StoreHouseBarItem item = mItemList.get(index);

                item.setFillAfter(false);
                item.setFillEnabled(true);
                item.setFillBefore(false);
                item.setDuration(mLoadingAniItemDuration);

                item.start(mFromAlpha, mToAlpha);

            }

            mTick++;
            if (mRunning) {
                postDelayed(this, mInterval);
            }
        }

        private void stop() {
            mRunning = false;
            removeCallbacks(this);
        }
    }
}
