package com.example.pj.ptr_lib.foot;

import com.example.pj.ptr_lib.container.PtrContainer;
import com.example.pj.ptr_lib.utils.PtrIndicator;

/**
 * Created by pj on 2016/10/25.
 */
public interface PtrFootUIHandler {
    void onUIReset();

    void onUILoadBegin(PtrContainer container);

    void onUILoadCompleted();

    void onUIPositionChanged(PtrContainer container, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator);
}
