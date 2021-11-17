package com.example.paint;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class GestureListener extends GestureDetector.SimpleOnGestureListener
        implements GestureDetector.OnDoubleTapListener{
    private PaintView canvas;

    void setCanvas(PaintView canvas) {
        this.canvas = canvas;
    }

}
