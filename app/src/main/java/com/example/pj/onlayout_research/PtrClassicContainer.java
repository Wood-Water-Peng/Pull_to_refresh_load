package com.example.pj.onlayout_research;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by pj on 2016/10/17.
 */
public class PtrClassicContainer extends PtrContainer {
    private MaterialHeader mHeaderView;

    public PtrClassicContainer(Context context) {
        this(context, null);
    }

    public PtrClassicContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mHeaderView = new MaterialHeader(getContext());
        setHeaderView(mHeaderView);
        addPtrUIHandler(mHeaderView);
    }
}
