package com.example.pj.ptr_lib.head;

import android.graphics.Point;
import android.util.SparseArray;

import java.util.ArrayList;

/**
 * Created by pj on 2016/11/3.
 */
public class BombItemCenter {
    /**
     * return  circle's center list according to count
     *
     * @param count
     * @return
     */
    private static final SparseArray<float[]> sCenterList;

    public static ArrayList<Point> getCenter(int count, float scale) {
        float[] floats = sCenterList.get(count);
        ArrayList<Point> points = new ArrayList<>();
        Point point = null;
        for (int i = 0; i < floats.length; i++) {
            if (i % 2 == 0) {
                point = new Point();
                point.x = (int) (floats[i] * scale);
            } else {
                point.y = (int) (floats[i] * scale);
                points.add(point);
            }
        }
        return points;
    }

    public static ArrayList<Point> getCenter(int count) {
        return getCenter(count, 2.5f);
    }


    static {
        sCenterList = new SparseArray<float[]>();
        float[][] SHAPES = new float[][]{
                //triangle
                new float[]{
                        0, 0, 30, 0, 15, 30
                },
                //rectangle
                new float[]{
                        30, 0, 60, 30, 30, 60, 0, 30
                },
                new float[]{

                },
                //hexagon(六边形)
                new float[]{
                        25, 0, 50, 15, 50, 45, 25,60, 0, 45, 0, 15
                }

        };
        for (int i = 0; i < SHAPES.length; i++) {
            sCenterList.append(i + 3, SHAPES[i]);
        }
    }
}
