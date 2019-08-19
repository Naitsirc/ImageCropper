/*
 *  COPYRIGHT NOTICE  
 *  Copyright (C) 2015, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/ImageCropper
 *   
 *  @license under the Apache License, Version 2.0 
 *
 *  @file    CropWindow.java
 *  @brief   Manage the position and size of crop window
 *  
 *  @version 1.0     
 *  @author  Jhuster
 *  @date    2015/01/09    
 */
package com.jhuster.imagecropper;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;


public class CropWindow {

    private static final int TOUCH_NONE = (1 << 0);
    private static final int TOUCH_GROW_LEFT_EDGE = (1 << 1);
    private static final int TOUCH_GROW_RIGHT_EDGE = (1 << 2);
    private static final int TOUCH_GROW_TOP_EDGE = (1 << 3);
    private static final int TOUCH_GROW_BOTTOM_EDGE = (1 << 4);
    private static final int TOUCH_MOVE_WINDOW = (1 << 5);

    private static final float BORDER_THRESHOLD = 80f;
    private static final float DEFAULT_MIN_WDITH = 180f;
    private static final float DEFAULT_MIN_HEIGHT = 180f;

    private float mLeft;
    private float mTop;
    private float mWidth;
    private float mHeight;
    private RectF mImageRect;
    private int mTouchMode = TOUCH_NONE;

    public CropWindow(RectF imageRect) {


        mWidth = Math.min(imageRect.width(), imageRect.height()) * 4 / 5;
        mHeight = mWidth;

        mLeft = imageRect.left + (imageRect.width() - mWidth) / 2;
        mTop = imageRect.top + (imageRect.height() - mHeight) / 2;

        mImageRect = imageRect;
    }

    public float left() {
        return mLeft;
    }

    public float right() {
        return (mLeft + mWidth);
    }

    public float top() {
        return mTop;
    }

    public float bottom() {
        return (mTop + mHeight);
    }

    public float width() {
        return mWidth;
    }

    public float height() {
        return mHeight;
    }

    public Rect getWindowRect() {
        return new Rect((int) left(), (int) top(), (int) right(), (int) bottom());
    }

    public RectF getWindowRectF() {
        return new RectF(left(), top(), right(), bottom());
    }

    public Rect getWindowRect(float scale) {
        int width = (int) (width() / scale);
        int height = (int) (height() / scale);
        int xoffset = (int) ((left() - mImageRect.left) / scale);
        int yoffset = (int) ((top() - mImageRect.top) / scale);
        return new Rect(xoffset, yoffset, xoffset + width, yoffset + height);
    }

    public RectF[] getOutWindowRects() {
        RectF[] rects = new RectF[4];
        Rect window = getWindowRect();
        rects[0] = new RectF(mImageRect.left, mImageRect.top, mImageRect.right, window.top);
        rects[1] = new RectF(mImageRect.left, window.top, window.left, window.bottom);
        rects[2] = new RectF(window.right, window.top, mImageRect.right, window.bottom);
        rects[3] = new RectF(mImageRect.left, window.bottom, mImageRect.right, mImageRect.bottom);
        return rects;
    }

    public Point[] getDragPoints() {
        Point[] points = new Point[4];
        Rect window = getWindowRect();
        points[0] = new Point(window.left, window.centerY());   //Left
        points[1] = new Point(window.centerX(), window.top);    //Top
        points[2] = new Point(window.right, window.centerY());  //Right
        points[3] = new Point(window.centerX(), window.bottom); //Bottom
        return points;
    }

    //By default, the border equals the image border
    private RectF getGrowBorder() {
        RectF border = new RectF(mImageRect);

        return border;
    }

    //Make sure the crop window inside the border
    private void adjustWindowRect() {
        mLeft = (left() < mImageRect.left) ? mImageRect.left : left();
        mLeft = (right() >= mImageRect.right) ? mImageRect.right - mWidth : left();
        mTop = (top() < mImageRect.top) ? mImageRect.top : top();
        mTop = (bottom() >= mImageRect.bottom) ? mImageRect.bottom - mHeight : top();
    }

    public boolean onTouchDown(float x, float y) {

        RectF window = getWindowRectF();

        //IF set output X&Y, forbid change the crop window size


            //make sure the position is between the top and the bottom edge (with some tolerance). Similar for isYinWindow.
            boolean isXinWindow = (x >= window.left - (BORDER_THRESHOLD/2)) && (x < window.right + (BORDER_THRESHOLD/2));
            boolean isYinWindow = (y >= window.top - (BORDER_THRESHOLD/2)) && (y < window.bottom + (BORDER_THRESHOLD/2));


            Log.v("X/Y",isXinWindow+" / "+isYinWindow);

            Log.v("IZQUIERDA",""+(Math.abs(window.left - x) < BORDER_THRESHOLD));
        Log.v("DERECHA",(Math.abs(window.right - x) < BORDER_THRESHOLD)+"");
        Log.v("ARRIBA",(Math.abs(window.top - y) < BORDER_THRESHOLD)+"");
        Log.v("ABAJO",(Math.abs(window.bottom - y) < BORDER_THRESHOLD)+"");


            // Check whether the position is near some edge(s)
            if ((Math.abs(window.left - x) < BORDER_THRESHOLD) && isYinWindow) {
                mTouchMode |= TOUCH_GROW_LEFT_EDGE;
            }else{
            if ((Math.abs(window.right - x) < BORDER_THRESHOLD) && isYinWindow) {
                mTouchMode |= TOUCH_GROW_RIGHT_EDGE;
            }else{
            if ((Math.abs(window.top - y) < BORDER_THRESHOLD) && isXinWindow) {
                mTouchMode |= TOUCH_GROW_TOP_EDGE;
            }else{
            if ((Math.abs(window.bottom - y) < BORDER_THRESHOLD) && isXinWindow) {
                mTouchMode |= TOUCH_GROW_BOTTOM_EDGE;
            }}}}


        // Not near any edge but inside the rectangle: move
        if (mTouchMode == TOUCH_NONE && window.contains((int) x, (int) y)) {
            mTouchMode = TOUCH_MOVE_WINDOW;
        }

        return mTouchMode != TOUCH_NONE;
    }

    public boolean onTouchUp() {
        mTouchMode = TOUCH_NONE;
        return true;
    }

    public boolean onTouchMoved(float deltaX, float deltaY) {

        if (mTouchMode == TOUCH_NONE) {
            return false;
        }

        if (mTouchMode == TOUCH_MOVE_WINDOW) {
            mLeft += deltaX;
            mTop += deltaY;
            adjustWindowRect();
        } else {


            RectF window = getWindowRectF();
            RectF border = getGrowBorder();

            if ((TOUCH_GROW_LEFT_EDGE & mTouchMode) != 0) {
                window.left += deltaX;
                window.left = (window.left < border.left) ? border.left : window.left;
                window.left = (window.left > window.right - DEFAULT_MIN_WDITH) ? window.right - DEFAULT_MIN_WDITH : window.left;
            }
            if ((TOUCH_GROW_RIGHT_EDGE & mTouchMode) != 0) {
                window.right += deltaX;
                window.right = (window.right > border.right) ? border.right : window.right;
                window.right = (window.right < window.left + DEFAULT_MIN_WDITH) ? window.left + DEFAULT_MIN_WDITH : window.right;
            }
            if ((TOUCH_GROW_TOP_EDGE & mTouchMode) != 0) {
                window.top += deltaY;
                window.top = (window.top < border.top) ? border.top : window.top;
                window.top = (window.top > window.bottom - DEFAULT_MIN_HEIGHT) ? window.bottom - DEFAULT_MIN_HEIGHT : window.top;
            }
            if ((TOUCH_GROW_BOTTOM_EDGE & mTouchMode) != 0) {
                window.bottom += deltaY;
                window.bottom = (window.bottom > border.bottom) ? border.bottom : window.bottom;
                window.bottom = (window.bottom < window.top + DEFAULT_MIN_HEIGHT) ? window.top + DEFAULT_MIN_HEIGHT : window.bottom;
            }

            mLeft = window.left;
            mTop = window.top;

            mWidth = window.right - window.left;
            mHeight = window.bottom - window.top;
        }

        return true;
    }
}

