package com.example.pj.onlayout_research;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by pj on 2016/10/17.
 */
public class DefaultHeaderView extends FrameLayout implements PtrHeadUIHandler {
    private final static String KEY_SharedPreferences = "cube_ptr_classic_last_update";
    private static final String TAG = "DefaultHeaderView";
    private static SimpleDateFormat sDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private TextView mTitleTextView;
    private View mRotateView;
    private View mProgressBar;
    private TextView mLastUpdateTextView;
    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;
    private int mRotateAniTime = 150;
    private String mLastUpdateTimeKey;
    private boolean mShouldShowLastUpdate;
    private long mLastUpdateTime = -1;
    private LastUpdateTimeUpdater mLastUpdateTimeUpdater = new LastUpdateTimeUpdater();

    public DefaultHeaderView(Context context) {
        this(context, null);
    }

    public DefaultHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.DefaultHeaderView);
        if (arr != null) {
            mRotateAniTime = arr.getInt(R.styleable.DefaultHeaderView_ptr_rotate_ani_time, mRotateAniTime);
        }
        buildAnimation();
        View header = LayoutInflater.from(context).inflate(R.layout.header_layout, this);

        mRotateView = header.findViewById(R.id.ptr_default_header_rotate_view);
        mTitleTextView = (TextView) header.findViewById(R.id.ptr_default_header_rotate_view_header_title);
        mLastUpdateTextView = (TextView) header.findViewById(R.id.ptr_default_header_rotate_view_header_last_update);
        mProgressBar = header.findViewById(R.id.ptr_default_header_rotate_view_progressbar);

        resetView();


    }

    private void resetView() {
        hideRotateView();
        mProgressBar.setVisibility(INVISIBLE);
    }

    private void hideRotateView() {
        mRotateView.clearAnimation();
        mRotateView.setVisibility(INVISIBLE);
    }

    private void buildAnimation() {
        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(mRotateAniTime);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(mRotateAniTime);
        mReverseFlipAnimation.setFillAfter(true);
    }

    @Override
    public void onUIReset() {


    }

    /**
     * 用户开始下拉，该方法只会执行一次，相当于对头部的初始化操作
     */
    @Override
    public void onUIRefreshPrepare(PtrContainer container) {
        /**
         * 将更新时间显示出来，并执行更新时间任务
         */
        Log.i(TAG, "---onUIRefreshPrepare---");
        mShouldShowLastUpdate = true;
        tryUpdateLastUpdateTime();
        mLastUpdateTimeUpdater.start();  //开启刷新时间的任务
        mProgressBar.setVisibility(INVISIBLE);
        mRotateView.setVisibility(VISIBLE);
        /**
         * 拿到当前的状态  下拉--释放刷新
         */
        if (container.isPullToRefresh()) {
            mTitleTextView.setText(getResources().getString(R.string.cube_ptr_pull_down_to_refresh));
        } else {
            mTitleTextView.setText(getResources().getString(R.string.cube_ptr_pull_down));
        }

    }

    @Override
    public void onUIRefreshBegin() {
        mShouldShowLastUpdate = false;
        hideRotateView();
        mTitleTextView.setVisibility(VISIBLE);
        mTitleTextView.setText(R.string.cube_ptr_refreshing);
        mLastUpdateTimeUpdater.stop();

    }

    @Override
    public void onUIRefreshComplete() {

        hideRotateView();
        mProgressBar.setVisibility(INVISIBLE);
        mTitleTextView.setVisibility(VISIBLE);
        mTitleTextView.setText(getResources().getString(R.string.cube_ptr_refresh_complete));

        /**
         * update last update time
         */
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(KEY_SharedPreferences, 0);

        if (!TextUtils.isEmpty(mLastUpdateTimeKey)) {
            mLastUpdateTime = new Date().getTime();
            sharedPreferences.edit().putLong(mLastUpdateTimeKey, mLastUpdateTime).commit();
        }
    }

    @Override
    public void onUIPositionChange(PtrContainer container, boolean isUnderTouch, byte status,PtrIndicator ptrIndicator) {

    }


    /**
     * 专门用于更新头部刷新时间
     */
    private class LastUpdateTimeUpdater implements Runnable {
        private boolean isRunning;

        public void start() {
            isRunning = true;
            run();
        }

        public void stop() {
            isRunning = false;
            removeCallbacks(this);
        }

        @Override
        public void run() {
            tryUpdateLastUpdateTime();
            if (isRunning) {
                postDelayed(this, 1000);
            }
        }

    }

    private void tryUpdateLastUpdateTime() {
        if (TextUtils.isEmpty(mLastUpdateTimeKey) || !mShouldShowLastUpdate) {
            mLastUpdateTextView.setVisibility(GONE);
        } else {
            String time = getLastUpdateTime();
            if (TextUtils.isEmpty(time)) {
                mLastUpdateTextView.setVisibility(GONE);
            } else {
                mLastUpdateTextView.setVisibility(VISIBLE);
                mLastUpdateTextView.setText(time);
            }

        }
    }

    private String getLastUpdateTime() {
        if (mLastUpdateTime == -1 && !TextUtils.isEmpty(mLastUpdateTimeKey)) {
            mLastUpdateTime = getContext().getSharedPreferences(KEY_SharedPreferences, 0).getLong(mLastUpdateTimeKey, -1);
        }
        if (mLastUpdateTime == -1) {
            return null;
        }
        /**
         * 拿到上一次刷新成功的时间
         */
        long diffTime = new Date().getTime() - mLastUpdateTime;
        int seconds = (int) (diffTime / 1000);
        if (seconds <= 0 || diffTime < 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        sb.append(getContext().getString(R.string.cube_ptr_last_update));

        if (seconds < 60) {
            sb.append(seconds + getContext().getString(R.string.cube_ptr_seconds_ago));
        } else {
            int minutes = seconds / 60;
            if (minutes > 60) {
                int hours = minutes / 60;
                if (hours > 24) {
                    Date date = new Date(mLastUpdateTime);
                    sb.append(sDataFormat.format(date));
                } else {
                    sb.append(hours + getContext().getString(R.string.cube_ptr_hours_ago));
                }
            } else {
                sb.append(minutes + getContext().getString(R.string.cube_ptr_minutes_ago));
            }
        }

        return sb.toString();
    }

}
