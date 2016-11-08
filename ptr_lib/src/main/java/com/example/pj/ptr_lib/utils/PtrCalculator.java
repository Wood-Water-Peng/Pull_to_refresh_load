package com.example.pj.ptr_lib.utils;

import android.graphics.Point;

/**
 * Created by pj on 2016/11/4.
 */
public class PtrCalculator {
    public static Point getEndPoint(Point start, Point end, float scale) {
        double start_end_dis = Math.sqrt(Math.pow((start.x - end.x), 2) + Math.pow((start.y - end.y), 2));
        double scaled_dis = start_end_dis * scale;
        int x;
        int y;
        //算出斜率
        float slop_y = (float) (1.0f * (end.y - start.y) / start_end_dis);
        y = (int) (scaled_dis * slop_y + start.y);

        float slop_x = (float) (1.0f * (end.x - start.x) / start_end_dis);
        x = (int) (scaled_dis * slop_x + start.x);

        return new Point(x, y);
    }
}
