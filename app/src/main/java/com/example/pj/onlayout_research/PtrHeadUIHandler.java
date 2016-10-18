package com.example.pj.onlayout_research;

/**
 * Created by pj on 2016/10/18.
 */
interface PtrHeadUIHandler {
    public void onUIReset();

    public void onUIRefreshPrepare(PtrContainer container);

    public void onUIRefreshBegin();

    public void onUIRefreshComplete();

    public void onUIPositionChange(PtrContainer container,boolean isUnderTouch,byte status,PtrIndicator ptrIndicator);

}
