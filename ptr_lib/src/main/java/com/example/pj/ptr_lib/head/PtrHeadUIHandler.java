package com.example.pj.ptr_lib.head;

import com.example.pj.ptr_lib.container.PtrContainer;
import com.example.pj.ptr_lib.utils.PtrIndicator;

/**
 * Created by pj on 2016/10/18.
 */
public interface PtrHeadUIHandler {
    public void onUIReset();

    public void onUIRefreshPrepare(PtrContainer container);

    public void onUIRefreshBegin();

    public void onUIRefreshComplete();

    public void onUIPositionChange(PtrContainer container, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator);

}
