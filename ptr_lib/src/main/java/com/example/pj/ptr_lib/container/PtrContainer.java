package com.example.pj.ptr_lib.container;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Scroller;

import com.example.pj.ptr_lib.R;
import com.example.pj.ptr_lib.foot.PtrFootUIHandler;
import com.example.pj.ptr_lib.head.PtrHeadUIHandler;
import com.example.pj.ptr_lib.utils.PtrIndicator;

/**
 * Created by pj on 2016/10/17.
 */
public class PtrContainer extends ViewGroup {

    public final static byte PTR_STATUS_INIT = 1;
    private byte mStatus = PTR_STATUS_INIT;
    public final static byte PTR_STATUS_PREPARE = 2;
    public final static byte PTR_STATUS_LOADING = 3;
    public final static byte PTR_STATUS_COMPLETE = 4;
    private static final String TAG = "PtrContainer";
    private final int mPagingTouchSlop;
    private View mHeaderView;
    private View mContentView;
    private float startY;
    private PtrIndicator mPtrIndicator = new PtrIndicator();
    private MotionEvent mLastMoveEvent;

    /**
     * 对于头部高度
     * 应该讲头部自身的margin和父控件的padding算进去
     * 后面计算的时候，统一以该高度为标准
     */
    private int mHeaderHeight;
    private boolean mPullToRefresh;
    private PtrUIHandlerHolder mPtrUIHandlerHolder = PtrUIHandlerHolder.create();
    private PtrHandler mPtrHandler;
    private float mRatioOfHeaderHeightToRefresh;
    private ScrollHelper mScrollHelper;
    private PtrUIHandlerHook mRefreshCompleteHook;
    private ListView mListView;

    /**
     * 默认属性
     */
    private float ptr_resistance = 1.5f;
    private float ptr_ratio_of_head_to_refresh = 0.5f;
    private int ptr_duration_to_close_head = 1000;

    public PtrContainer(Context context) {
        this(context, null);
    }

    public PtrContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
        mPtrIndicator.setRatioOfHeaderHeightToRefresh(1.5f);
        mScrollHelper = new ScrollHelper();

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.PtrContainer, 0, 0);
        if (arr != null) {
            ptr_duration_to_close_head = arr.getInt(R.styleable.PtrContainer_ptr_duration_to_close_header, ptr_duration_to_close_head);
            mPtrIndicator.setResistance(arr.getFloat(R.styleable.PtrContainer_ptr_resistance, ptr_resistance));
            mPtrIndicator.setRatioOfHeaderHeightToRefresh(arr.getFloat(R.styleable.PtrContainer_ptr_ratio_of_header_height_to_refresh, ptr_ratio_of_head_to_refresh));
            arr.recycle();
        }
        Log.i(TAG, "mPagingTouchSlop:" + mPagingTouchSlop);

    }

    public void setPtrHandler(PtrHandler ptrHandler) {
        mPtrHandler = ptrHandler;
    }

    public void addPtrUIHandler(PtrHeadUIHandler ptrHeadUIHandler) {
        PtrUIHandlerHolder.addHandler(mPtrUIHandlerHolder, ptrHeadUIHandler);
    }

    private int temp_startY;
    private int temp_curY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
//      Log.i(TAG, "------e------:" + e.getAction());
        switch (e.getAction()) {
            case MotionEvent.ACTION_UP:
                /**
                 * 判断上拉加载模块
                 */
                temp_curY = (int) e.getY();
                if (canLoadMore()) {
                    loadMore();
                }
            case MotionEvent.ACTION_CANCEL:
                mPtrIndicator.onRelease();
                if (mPtrIndicator.hasLeftStartPosition()) {
                    onRelease(false);
                    if (mPtrIndicator.hasMovedAfterPressedDown()) {
                        /**
                         * 不管是否下拉到刷新的距离
                         * 都认为刷新事件取消,直接回滚到顶部
                         */
                        sendCancelEvent();  //取消刷新事件
                        return true;
                    } else {
                        return dispatchTouchEventSupper(e);
                    }
                } else {
                    return dispatchTouchEventSupper(e);
                }
            case MotionEvent.ACTION_DOWN:
                temp_startY = (int) e.getY();
                mPtrIndicator.onPressDown(e.getX(), e.getY());
                return dispatchTouchEventSupper(e);
//                return true;     //对于有些控件，可能不会消费DOWN事件，所以这里直接返回true，才能接收到后续的MOVE事件
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = e;
                mPtrIndicator.onMove(e.getX(), e.getY());
                float offsetY = mPtrIndicator.getOffsetY();
                /**
                 * 头部在初始位置时上滑，将事件交由默认的逻辑处理
                 */
                boolean moveDown = offsetY > 0;  //判断用户是上拉还是下拉
                boolean moveUp = !moveDown;
                boolean canMoveUp = mPtrIndicator.hasLeftStartPosition(); //判断头部是否可以上滑

                if (mContentView instanceof ScrollView) {
                    Log.i(TAG, "" + mContentView.getScrollY());
                }

                if (moveDown && mContentView.canScrollVertically(-1)) {
                    return dispatchTouchEventSupper(e);
                }
//                Log.i(TAG, "canMoveUp:" + canMoveUp);
                if ((moveUp && canMoveUp) || moveDown) {
                    movePos(offsetY);
                    return dispatchTouchEventSupper(e);
                }

        }
        return dispatchTouchEventSupper(e);
    }

    private void loadMore() {
        if (mOnLoadListener != null) {
            setLoading();
        }
    }

    private void setLoading() {
        Log.i(TAG, "---setLoading---");
        mOnLoadListener.onLoad();
        mFootUIHandler.onUILoadBegin(this);
    }

    private View mFootView;


    private boolean canLoadMore() {
        if (mListView == null)
            return false;
        Log.i(TAG, "mStatus:" + mStatus);
        return isListViewBottom() && mStatus != PTR_STATUS_LOADING && isPullingUp();

    }

    private OnLoadListener mOnLoadListener;

    public void setOnLoadListener(OnLoadListener loadListener) {
        this.mOnLoadListener = loadListener;
    }

    private boolean isPullingUp() {
//        boolean isPullingUp = mPtrIndicator.getLastPosY() - mPtrIndicator.getCurrentPosY() > mPagingTouchSlop / 8;
        boolean isPullingUp = (temp_startY - temp_curY) > mPagingTouchSlop / 2;
//        Log.i(TAG, "lastPosY:" + mPtrIndicator.getLastPosY());
//        Log.i(TAG, "curPosY:" + mPtrIndicator.getCurrentPosY());
//        Log.i(TAG, "mPagingTouchSlop:" + mPagingTouchSlop);
        Log.i(TAG, "temp_curY:" + temp_curY);
        Log.i(TAG, "temp_startY:" + temp_startY);
        return isPullingUp;
    }

    private boolean isListViewBottom() {
        if (mListView.getAdapter() == null)
            return false;
        if (mListView.getCount() > 0) {
            Log.i(TAG, "Last child's bottom:" + mListView.getChildAt(mListView.getChildCount() - 1).getBottom());
            Log.i(TAG, "ListView height:" + mListView.getHeight());
            if (mListView.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1) {
                return true;
            }
        }

        return false;
    }

    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }

    /**
     * 当手指释放后，有多种状态
     * 1.下拉的距离没有达到执行刷新操作的要求   直接回到初始位置
     * 2.下拉的距离达到了执行刷新操作的要求     回滚到刷新操作的位置
     *
     * @param b
     */
    private void onRelease(boolean b) {
        tryToPerformRefresh();
        Log.i(TAG, "onRelease——mStatus:" + mStatus);
        if (mStatus == PTR_STATUS_LOADING) {
            mScrollHelper.tryToScrollTo(mPtrIndicator.getOffsetToKeepHeaderWhileLoading(), 200);
        } else {
            if (mStatus == PTR_STATUS_COMPLETE) {
                notifyUIRefreshComplete(false);
            } else {
                tryScrollBackToTopAbortRefresh();
            }
        }
    }

    private void tryScrollBackToTopAbortRefresh() {
        tryScrollBackToTop();
    }

    private void tryToPerformRefresh() {

        if (mStatus != PTR_STATUS_PREPARE) {
            return;
        }

        if (mPtrIndicator.isOverOffsetToRefresh()) {
            Log.i(TAG, "---onRefreshBegin---");
            mStatus = PTR_STATUS_LOADING;
            performRefresh();
        }
    }

    private void performRefresh() {
        if (mPtrUIHandlerHolder.hasHandler()) {
            mPtrUIHandlerHolder.onUIRefreshBegin();
        }
        if (mPtrHandler != null) {
            mPtrHandler.onRefreshBegin();
        }
    }

    private void movePos(float deltaY) {

        if (deltaY < 0 && mPtrIndicator.isInStartPosition()) {
            return;
        }

        int to = mPtrIndicator.getCurrentPosY() + (int) deltaY;
        mPtrIndicator.setCurrentPos(to);
        int change = to - mPtrIndicator.getLastPosY();

//        Log.i(TAG, "deltaY:" + deltaY);
//        Log.i(TAG, "lastPosY:" + mPtrIndicator.getLastPosY());
//        Log.i(TAG, "to:" + to);
//        Log.i(TAG, "change:" + change);

        updatePos(change);

    }

    private void updatePos(int change) {
        if (change == 0) {
            return;
        }
        boolean isUnderTouch = mPtrIndicator.isUnderTouch();
        /**
         * 处理取消事件
         * 当手指移出屏幕外后，向子孩子发送一个 CANCEL EVENT
         */
        if (isUnderTouch && mPtrIndicator.hasMovedAfterPressedDown()) {
            sendCancelEvent();
        }
//        Log.i(TAG, "-------------------------:" + mPtrIndicator.hasJustLeftStartPosition());
        if (mPtrIndicator.hasJustLeftStartPosition() && mStatus == PTR_STATUS_INIT) {
            mStatus = PTR_STATUS_PREPARE;
            mPtrUIHandlerHolder.onUIRefreshPrepare(this);
        }

        if (mHeaderView != null) {
            mHeaderView.offsetTopAndBottom(change);
//            Log.i(TAG, "mHeaderView--Top:" + mHeaderView.getTop());
//            Log.i(TAG, "mHeaderView--Y:" + mHeaderView.getY());
        }
        if (mContentView != null) {
            mContentView.offsetTopAndBottom(change);
        }

        if (mPtrUIHandlerHolder.hasHandler()) {
            mPtrUIHandlerHolder.onUIPositionChange(this, isUnderTouch, mStatus, mPtrIndicator);
        }

        invalidate();
    }

    private void sendCancelEvent() {
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEventSupper(e);
    }

    public boolean isPullToRefresh() {
        return mPullToRefresh;
    }

    private Runnable mPerformRefreshCompleteDelay = new Runnable() {
        @Override
        public void run() {

            performRefreshComplete();
        }
    };

    private void performRefreshComplete() {
        Log.i(TAG, "---performRefreshComplete---");
        mStatus = PTR_STATUS_COMPLETE;
        notifyUIRefreshComplete(false);
    }

    private void notifyUIRefreshComplete(boolean b) {
        if (mPtrUIHandlerHolder.hasHandler()) {
            mPtrUIHandlerHolder.onUIRefreshComplete();
        }
        mPtrIndicator.onUIRefreshComplete();
        tryScrollBackToTopAfterComplete();
        tryToNotifyReset();
    }

    private void tryToNotifyReset() {
        /**
         * 回滚到顶部之后将状态重置
         */
        if ((mStatus == PTR_STATUS_COMPLETE || mStatus == PTR_STATUS_PREPARE) && mPtrIndicator.isInStartPosition()) {
            if (mPtrUIHandlerHolder.hasHandler()) {
                mPtrUIHandlerHolder.onUIReset();
            }
            mStatus = PTR_STATUS_INIT;
        }

    }

    private void tryScrollBackToTopAfterComplete() {
        tryScrollBackToTop();
    }

    private void tryScrollBackToTop() {
        Log.i(TAG, "isUnderTouch:" + mPtrIndicator.isUnderTouch());

        if (!mPtrIndicator.isUnderTouch()) {
            mScrollHelper.tryToScrollTo(PtrIndicator.POS_START, 500);
        }
    }

    /**
     * 暴露给外部的接口,表示数据加载已经结束，可以进行自动回滚了
     */
    final public void refreshComplete() {
        Log.i(TAG, "---refreshComplete---");
        postDelayed(mPerformRefreshCompleteDelay, 1000);
    }


    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();
        int childCount = getChildCount();
        /**
         * HeadView有两种填充方式
         * 1.动态设置
         * {@link setHeadView(View head)}
         *
         * 2.写在xml文件中
         */
        if (childCount > 2) {
            throw new IllegalStateException("PtrContainer can only contain 2 children");
        }
        if (childCount == 2) {
            //找到head和content
            findHeadAndContent();
        }
        if (mHeaderView != null) {
            mHeaderView.bringToFront();
        }
    }

    private void findHeadAndContent() {
        View child1 = getChildAt(0);
        View child2 = getChildAt(1);
        Log.i(TAG, "child1:" + child1.getClass().getCanonicalName());
        Log.i(TAG, "child2:" + child2.getClass().getCanonicalName());

        if (child1 instanceof PtrHeadUIHandler) {
//                Log.i(TAG, "------------------");
            mHeaderView = child1;
            mContentView = child2;
        } else if (child2 instanceof PtrHeadUIHandler) {
//                Log.i(TAG, "===================");
            mContentView = child1;
            mHeaderView = child2;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            mHeaderHeight = mHeaderView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            mPtrIndicator.setHeaderHeight(mHeaderHeight);
        }

        if (mContentView != null) {
            measureContentView(mContentView, widthMeasureSpec, heightMeasureSpec);
        }

    }

    private void measureContentView(View child,
                                    int parentWidthMeasureSpec,
                                    int parentHeightMeasureSpec) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    public void setHeaderView(View header) {
        if (mHeaderView != null && header != null && mHeaderView != header) {
            removeView(mHeaderView);
        }
        LayoutParams lp = (LayoutParams) header.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(-1, -2);
//            lp.topMargin = -300;
            Log.i(TAG, "lp:" + lp.getClass().getCanonicalName());
            header.setLayoutParams(lp);
        }
        mHeaderView = header;
        addView(header, lp);
        Log.i(TAG, "addHeadView_childCount:" + getChildCount());
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG, "changed:" + changed);
        layoutChildren();
        if (changed) {
        }
    }


    private void layoutChildren() {
        /**
         * 为什么这里需要一个offset?
         * 在布局的时候为什么要将这个offset加上去
         */
        int offset = mPtrIndicator.getCurrentPosY();
        Log.i(TAG, "offset:" + offset);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();

        if (mHeaderView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            Log.i(TAG, "headView_topMargin:" + lp.topMargin);
            Log.i(TAG, "mHeaderHeight:" + mHeaderHeight);
            // enhance readability(header is layout above screen when first init)
            final int top = -(mHeaderHeight - paddingTop - lp.topMargin - offset);
            final int right = left + mHeaderView.getMeasuredWidth();
            final int bottom = top + mHeaderView.getMeasuredHeight();
            Log.i(TAG, "header_top:" + top + "------header_bottom:" + bottom);
            mHeaderView.layout(left, top, right, bottom);

        }

        if (mContentView != null) {
            MarginLayoutParams lp = (MarginLayoutParams) mContentView.getLayoutParams();
            final int left = paddingLeft + lp.leftMargin;
            final int top = paddingTop + lp.topMargin + offset;
            final int right = left + mContentView.getMeasuredWidth();
            final int bottom = top + mContentView.getMeasuredHeight();
            Log.i(TAG, "content_top:" + top + "------content_bottom:" + bottom);
            mContentView.layout(left, top, right, bottom);
        }
    }

    public void setRefreshCompleteHook(PtrUIHandlerHook mPtrUIHandlerHook) {
        mRefreshCompleteHook = mPtrUIHandlerHook;
        mPtrUIHandlerHook.setResumeAction(new Runnable() {
            @Override
            public void run() {
                notifyUIRefreshComplete(true);
            }
        });
    }

    private PtrFootUIHandler mFootUIHandler;

    public void setFootUIHanlder(PtrFootUIHandler footUIHandler) {
        this.mFootUIHandler = footUIHandler;
    }

    public void loadMoreCompleted() {
        Log.i(TAG, "---loadMoreCompleted---");
        mFootUIHandler.onUILoadCompleted();
    }

    public void setListView(ListView mListView) {
        this.mListView = mListView;
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    /**
     * 用户释放后的回滚操作
     * 本质上还是采用offsetTopAndBottom的方法，并不涉及ScrollTo的操作
     */

    class ScrollHelper implements Runnable {
        private int mLastPosY;
        private boolean mIsRunning;
        private Scroller mScroller;
        private int mStart;   //滚动起始值
        private int mTo;     //滚动结束值

        public ScrollHelper() {
            this.mScroller = new Scroller(getContext());
        }

        @Override
        public void run() {
            /**
             * 根据Scroller计算的值来判断滚动是否结束
             */
            boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastPosY;
            if (!finish) {
                mLastPosY = curY;
                movePos(deltaY);
                post(this);
            } else {
                finish();
            }

        }

        /**
         * 此时的滚动完结有两种状态
         * 1.释放刷新--->开始刷新
         * 2.刷新完成--->回到初始状态
         */
        private void finish() {
            reset();
            onPtrScrollFinish();
        }

        /**
         * 重置ScrollerHelper
         */
        private void reset() {
            mIsRunning = false;
            mLastPosY = 0;
            removeCallbacks(this);
        }

        public void tryToScrollTo(int to, int duration) {
            if (mPtrIndicator.isAlreadyHere(to)) {
                return;
            }
            mStart = mPtrIndicator.getCurrentPosY();
            mTo = to;

            int distance = mTo - mStart;
            Log.i(TAG, "mStart:" + mStart + "---mTo:" + mTo);
            mScroller.startScroll(0, 0, 0, distance, duration);
            post(this);
            mIsRunning = true;
        }
    }

    private void onPtrScrollFinish() {
        onRelease(true);
    }

    public interface OnLoadListener {
        void onLoad();
    }
}
