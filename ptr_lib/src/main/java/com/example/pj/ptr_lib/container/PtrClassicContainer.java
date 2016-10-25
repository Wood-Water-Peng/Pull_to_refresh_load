package com.example.pj.ptr_lib.container;

import android.content.Context;
import android.util.AttributeSet;

import com.example.pj.ptr_lib.head.DefaultHeaderView;

/**
 * Created by pj on 2016/10/17.
 */
public class PtrClassicContainer extends PtrContainer {
    private DefaultHeaderView mHeaderView;
    private Context mContext;

    public PtrClassicContainer(Context context) {
        this(context, null);
    }

    public PtrClassicContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView() {
        mHeaderView = new DefaultHeaderView(getContext());
        setHeaderView(mHeaderView);
        /**
         * 这样写的问题
         * 1.在父控件内部，ListView并没有填充出来，那么，这个footView就填充不进去
         * 解决办法
         */
//        setFootView(LayoutInflater.from(mContext).inflate(R.layout.foot_layout, null));
        addPtrUIHandler(mHeaderView);
    }
}
