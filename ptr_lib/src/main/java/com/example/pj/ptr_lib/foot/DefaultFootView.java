package com.example.pj.ptr_lib.foot;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.pj.ptr_lib.R;
import com.example.pj.ptr_lib.container.PtrContainer;
import com.example.pj.ptr_lib.utils.PtrIndicator;

/**
 * Created by pj on 2016/10/25.
 */
public class DefaultFootView extends FrameLayout implements PtrFootUIHandler {
    private TextView tv_prepare;
    private TextView tv_error;
    private ProgressBar pb_loading;

    public DefaultFootView(Context context) {
        this(context, null);
    }

    public DefaultFootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.foot_layout, this);
        tv_prepare = (TextView) view.findViewById(R.id.text_prepare);
        tv_error = (TextView) view.findViewById(R.id.text_load_failed);
        pb_loading = (ProgressBar) view.findViewById(R.id.load_progress_bar);
    }


    @Override
    public void onUIReset() {

    }

    @Override
    public void onUILoadBegin(PtrContainer container) {
        tv_error.setVisibility(GONE);
        pb_loading.setVisibility(VISIBLE);
        tv_prepare.setVisibility(GONE);
    }

    @Override
    public void onUILoadCompleted() {
        tv_error.setVisibility(GONE);
        pb_loading.setVisibility(GONE);
        tv_prepare.setVisibility(VISIBLE);
    }

    @Override
    public void onUIPositionChanged(PtrContainer container, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {

    }
}
