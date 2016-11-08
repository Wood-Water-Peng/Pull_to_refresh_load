package com.example.pj.ptr_lib.head;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Transformation;

import com.example.pj.ptr_lib.container.PtrContainer;
import com.example.pj.ptr_lib.utils.PtrCalculator;
import com.example.pj.ptr_lib.utils.PtrIndicator;
import com.example.pj.ptr_lib.utils.PtrLocalDisplay;

import java.util.ArrayList;

/**
 * Created by pj on 2016/11/3.
 */
public class BombHeadView extends View implements PtrHeadUIHandler {

    private static final String TAG = "BombHeadView";
    public ArrayList<BombHeadItem> mItemList = new ArrayList<>();
    private int mDrawZoneWidth = 0;
    private int mDrawZoneHeight = 0;
    private int mOffsetX = 0;
    private int mOffsetY = 0;
    private boolean mIsLoading = false;
    private AniController mAniController = new AniController();
    private Transformation mTransformation = new Transformation();
    private float mInternalAnimationFactor = 0.7f;
    private Point mCenter = new Point();
    private float mProgress;
    private int mHorizontalRandomness;
    private float mDropHeight;

    public BombHeadView(Context context) {
        this(context, null);
    }

    public BombHeadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        PtrLocalDisplay.init(getContext());
        mHorizontalRandomness = PtrLocalDisplay.SCREEN_WIDTH_PIXELS / 2;
    }

    public void initWithItemList(ArrayList<BombHeadItem> items) {
        if (null == items)
            throw new IllegalStateException("items must not be null!");
        if (items.size() < 3)
            throw new IllegalStateException("items size must be larger than 3");
    }

    /**
     * @param count the count of the items,must larger than 3
     * @param color the color of the items
     */
    public void initWithSpecifiedParam(int count, int color) {
        this.initWithSpecifiedParam(count, color, BombHeadItem.BALL_DEFAULT_RADIUS);
    }

    /**
     * @param count  the count of the items,must larger than 3
     * @param color  the color of the items
     * @param radius the radius of the items
     */
    public void initWithSpecifiedParam(int count, int color, int radius) {

        int drawWidth = 0;
        int drawHeight = 0;

        ArrayList<Point> centerList = BombItemCenter.getCenter(count);
        for (int i = 0; i < centerList.size(); i++) {
            Point point = centerList.get(i);
            mItemList.add(new BombHeadItem.Builder().withColor(color)
                    .withRadius(radius)
                    .withCenter(point)
                    .build());
            drawWidth = Math.max(drawWidth, point.x);
            drawHeight = Math.max(drawHeight, point.y);
        }
        mDrawZoneWidth = drawWidth;
        mDrawZoneHeight = drawHeight;

    }

    private void findCenter() {
        Log.i(TAG, "size:" + mItemList.size());
        if (mItemList.size() == 4) {
            mCenter.y = (mItemList.get(0).center.y + mItemList.get(2).center.y) / 2;
            mCenter.x = (mItemList.get(1).center.x + mItemList.get(3).center.x) / 2;
            Log.i(TAG, "mCenter.x:" + mCenter.x + "--mCenter.y:" + mCenter.y);
        } else if (mItemList.size() == 6) {
            mCenter.y = (mItemList.get(0).center.y + mItemList.get(3).center.y) / 2;
            mCenter.x = (mItemList.get(1).center.x + mItemList.get(5).center.x) / 2;
        }
        Log.i(TAG, "mOffsetX:" + mOffsetX + "--mOffsetY:" + mOffsetY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getTopOffset() + mDrawZoneHeight + getBottomOffset();
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mOffsetX = (getMeasuredWidth() - mDrawZoneWidth) / 2;
        mOffsetY = getTopOffset();
        mDropHeight = getTopOffset();

        findCenter();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int c1 = canvas.save();
        int len = mItemList.size();
        for (int i = 0; i < len; i++) {
            canvas.save();
            BombHeadItem bombHeadItem = mItemList.get(i);
            int offsetX = mOffsetX;
            int offsetY = mOffsetY;
//          int radius = bombHeadItem.mRadius;
            if (mIsLoading) {
                bombHeadItem.getTransformation(getDrawingTime(), mTransformation);
                canvas.translate(mOffsetX, mOffsetY);
                invalidate();
            } else {
                if (mProgress == 0) {
                    bombHeadItem.resetPosition(mHorizontalRandomness);
                    continue;
                }
                /**
                 * 根据bombItem位置的不同，计算出不同的偏移量
                 */
                float realProgress;
                float startPadding = (1 - mInternalAnimationFactor) * i / len;
                float endPadding = 1 - mInternalAnimationFactor - startPadding;

                realProgress = Math.min(1, (mProgress - startPadding) / mInternalAnimationFactor);
                Log.i(TAG, "realProgress:" + realProgress);
                offsetX = (int) (mOffsetX + (bombHeadItem.center.x + bombHeadItem.translationX) * (1 - realProgress));
                offsetY = (int) (mOffsetY - (mDropHeight + bombHeadItem.center.y) * (1 - realProgress));
                Matrix matrix = new Matrix();
                matrix.postTranslate(offsetX, offsetY);
                canvas.concat(matrix);
            }
            bombHeadItem.draw(canvas);
            canvas.restore();
        }
        canvas.restoreToCount(c1);
    }

    private int getTopOffset() {
        return getPaddingTop() + PtrLocalDisplay.dp2px(20);
    }

    private int getBottomOffset() {
        return getPaddingBottom() + PtrLocalDisplay.dp2px(20);
    }

    @Override
    public void onUIReset() {

    }

    @Override
    public void onUIRefreshPrepare(PtrContainer container) {

    }

    @Override
    public void onUIRefreshBegin() {
        beginLoading();
    }

    private void beginLoading() {
        Log.i(TAG, "beginLoading");
        mIsLoading = true;
        mAniController.start();
        invalidate();
    }

    @Override
    public void onUIRefreshComplete() {
        loadFinish();
        resetPosition();
    }

    private void resetPosition() {
        for (int i = 0; i < mItemList.size(); i++) {
            mItemList.get(i).resetPosition();
        }
    }

    private void loadFinish() {
        mIsLoading = false;
        mAniController.stop();

    }

    @Override
    public void onUIPositionChange(PtrContainer container, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        float currentPercent = Math.min(1f, ptrIndicator.getCurrentPercent());
        setProgress(currentPercent);
        invalidate();
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
    }

    private class AniController implements Runnable {
        private boolean mRunning = true;
        private long mInterval = 100;
        private int curCount = 0;

        private void start() {
            mRunning = true;
            run();
        }

        @Override
        public void run() {
            int count = curCount % mItemList.size();
            BombHeadItem bombHeadItem = mItemList.get(count);
            bombHeadItem.setFillAfter(false);
            bombHeadItem.setFillEnabled(true);
            bombHeadItem.setFillBefore(false);
            bombHeadItem.setDuration(100);
            Log.i(TAG, "startX:" + bombHeadItem.center.x + "--startY:" + bombHeadItem.center.y);
            Log.i(TAG, "mCenter.x:" + mCenter.x + "--mCenter.y:" + mCenter.y);
            bombHeadItem.start(bombHeadItem.center, PtrCalculator.getEndPoint(mCenter, bombHeadItem.center, 1.5f));
            curCount++;
            Log.i(TAG, "curCount:" + curCount);
            if (mRunning) {
                postDelayed(this, mInterval);
            }
        }

        public void stop() {
            mRunning = false;
            removeCallbacks(this);
        }
    }
}
