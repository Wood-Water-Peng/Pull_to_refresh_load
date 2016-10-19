package com.example.pj.onlayout_research;

/**
 * Created by pj on 2016/10/18.
 */
public class PtrUIHandlerHolder implements PtrHeadUIHandler {
    private PtrHeadUIHandler mHandler;
    private PtrUIHandlerHolder mNext;

    public static void addHandler(PtrUIHandlerHolder head, PtrHeadUIHandler handler) {
        if (head == null || handler == null) {
            return;
        }

        /**
         * 如果这个链表中的head还没有持有handler
         */
        if (null == head.mHandler) {
            head.mHandler = handler;
            return;
        }

        PtrUIHandlerHolder current = head;

        while (true) {
            current = current.mNext;
            if (current == null)
                break;
        }
        PtrUIHandlerHolder newHolder = new PtrUIHandlerHolder();
        newHolder.mHandler = handler;
        current.mNext = newHolder;

    }

    public boolean hasHandler() {
        return mHandler != null;
    }

    private PtrHeadUIHandler getHandler() {
        return mHandler;
    }

    public static PtrUIHandlerHolder create() {
        return new PtrUIHandlerHolder();
    }

    @Override
    public void onUIReset() {
        PtrUIHandlerHolder current = this;
        do {
            final PtrHeadUIHandler handler = current.getHandler();
            if (null != handler) {
                handler.onUIReset();
            }
        } while ((current = current.mNext) != null);
    }

    @Override
    public void onUIRefreshPrepare(PtrContainer container) {
        if (!hasHandler()) {
            return;
        }
        /**
         * 遍历链表中的handler
         */
        PtrUIHandlerHolder current = this;
        do {
            PtrHeadUIHandler handler = current.getHandler();
            if (null != handler) {
                handler.onUIRefreshPrepare(container);
            }
        } while ((current = current.mNext) != null);

    }

    @Override
    public void onUIRefreshBegin() {
        PtrUIHandlerHolder current = this;
        do {
            final PtrHeadUIHandler handler = current.getHandler();
            if (null != handler) {
                handler.onUIRefreshBegin();
            }
        } while ((current = current.mNext) != null);
    }

    @Override
    public void onUIRefreshComplete() {
        PtrUIHandlerHolder current = this;
        do {
            final PtrHeadUIHandler handler = current.getHandler();
            if (null != handler) {
                handler.onUIRefreshComplete();
            }
        } while ((current = current.mNext) != null);
    }

    @Override
    public void onUIPositionChange(PtrContainer container, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        PtrUIHandlerHolder current = this;
        do {
            final PtrHeadUIHandler handler = current.getHandler();
            if (null != handler) {
                handler.onUIPositionChange(container, isUnderTouch, status, ptrIndicator);
            }
        } while ((current = current.mNext) != null);
    }


}
