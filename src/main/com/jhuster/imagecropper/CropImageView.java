/*
 *  COPYRIGHT NOTICE  
 *  Copyright (C) 2015, Jhuster <lujun.hust@gmail.com>
 *  https://github.com/Jhuster/ImageCropper
 *   
 *  @license under the Apache License, Version 2.0 
 *
 *  @file    CropImageView.java
 *  @brief   Draw ImageView and CropWindow
 *  
 *  @version 1.0     
 *  @author  Jhuster
 *  @date    2015/01/09    
 */
package com.jhuster.imagecropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


public class CropImageView extends View{

    private static final float CROP_WINDOW_PAINTER_WIDTH = 3.0f;
    private static final float OUTSIDE_WINDOW_PAINTER_WIDTH = 1.0f;
    private static final float DRAG_ICONS_RADIUS = 20.0f;

    private Paint mCropPainter;
    private Paint mOutsidePainter;

    private Bitmap mOriginBitmap;
    private RotateBitmap mCropBitmap;
    private Matrix mMatrix = new Matrix();

    private CropWindow mCropWindow;
    private boolean mIsCropParamChanged = true;

    private float mScaleRate = (float) 1.0;

    private Drawable[] selectionDrawables;

    public void setSelectionDrawables(Drawable[] dra){
        this.selectionDrawables = dra;
        invalidate();
    }

    public Drawable[] getSelectionDrawables(){
        if(selectionDrawables==null){
            return new Drawable[]{};
        }

        return selectionDrawables;
    }


    public CropImageView(Context context) {
        super(context);
        createPainter();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createPainter();
    }

    public void destroy() {
        if (mCropBitmap != null && !mCropBitmap.getBitmap().isRecycled()) {
            mCropBitmap.recycle();
            mCropBitmap = null;
        }
        if (mOriginBitmap != null && !mOriginBitmap.isRecycled()) {
            mOriginBitmap.recycle();
            mOriginBitmap = null;
        }
    }

    private void createPainter() {
        mCropPainter = new Paint();
        mCropPainter.setAntiAlias(true);
        mCropPainter.setStyle(Style.STROKE);
        mCropPainter.setStrokeWidth(CROP_WINDOW_PAINTER_WIDTH);
        mCropPainter.setColor(Color.YELLOW);

        mOutsidePainter = new Paint();
        mOutsidePainter.setAntiAlias(true);
        mOutsidePainter.setStyle(Style.FILL);
        mOutsidePainter.setARGB(125, 50, 50, 50);
        mOutsidePainter.setStrokeWidth(OUTSIDE_WINDOW_PAINTER_WIDTH);
    }

    public void initialize(Bitmap bitmap) {
        mOriginBitmap = bitmap;
        initialize(bitmap, 0);
    }

    public void initialize(Bitmap bitmap, int degrees) {
        mOriginBitmap = bitmap;
        replace(bitmap, degrees);
    }

    public Bitmap getCropBitmap() {

        if (mCropBitmap != null) {

            float cropWidth = mCropWindow.width() / mScaleRate;
            float cropHeight = mCropWindow.height() / mScaleRate;

            Rect cropRect = mCropWindow.getWindowRect(mScaleRate);
            RectF dstRect = new RectF(0, 0, cropWidth, cropHeight);

            Matrix cropMatrix = new Matrix();
            cropMatrix.setRectToRect(new RectF(cropRect), dstRect, Matrix.ScaleToFit.FILL);
            cropMatrix.preConcat(mCropBitmap.getRotateMatrix());

            if(cropWidth<=1){
                cropWidth = 1;
            }

            if(cropHeight<=1){
                cropHeight = 1;
            }

            Bitmap cropped = Bitmap.createBitmap((int) cropWidth, (int) cropHeight, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(cropped);
            canvas.drawBitmap(mCropBitmap.getBitmap(), cropMatrix, null);

            return cropped;
        }

        return mCropBitmap.getBitmap();

    }

    public void zoom() {

        if (mCropBitmap != null) {

            Bitmap cropped = getCropBitmap();
            replace(cropped, 0);

        }

    }

    public void rotate() {
        if (mCropBitmap != null) {
            mCropBitmap.setRotation(mCropBitmap.getRotation() + 90);
            mIsCropParamChanged = true;
            invalidate();
        }
    }

    public void reset() {
        if (mCropBitmap == null) {
            return;
        }
        replace(mOriginBitmap, 0);
    }

    private void replace(Bitmap bitmap, int degrees) {
        if (mCropBitmap != null && mCropBitmap.getBitmap() != mOriginBitmap) {
            mCropBitmap.recycle();
        }
        mCropBitmap = new RotateBitmap(bitmap, degrees);
        mIsCropParamChanged = true;
        invalidate();
    }


    private void drawOutsideCropArea(Canvas canvas) {
        RectF[] rects = mCropWindow.getOutWindowRects();
        for (RectF rect : rects) {
            canvas.drawRect(rect, mOutsidePainter);
        }
    }

    private void drawDragIcons(Canvas canvas) {
        Point[] points = mCropWindow.getDragPoints();

        for (int i = 0; i < getSelectionDrawables().length; i++) {
            getSelectionDrawables()[i].setBounds((int) (points[i].x - DRAG_ICONS_RADIUS), (int) (points[i].y - DRAG_ICONS_RADIUS), (int) (points[i].x + DRAG_ICONS_RADIUS), (int) (points[i].y + DRAG_ICONS_RADIUS));
            getSelectionDrawables()[i].draw(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        if (mCropBitmap != null) {
            if (mIsCropParamChanged) {

                mScaleRate = Math.min((float) getWidth() / mCropBitmap.getWidth(), (float) getHeight() / mCropBitmap.getHeight());

                float offsetX = (getWidth() - mCropBitmap.getWidth() * mScaleRate) / 2;
                float offsetY = (getHeight() - mCropBitmap.getHeight() * mScaleRate) / 2;

                mMatrix.reset();
                mMatrix.postConcat(mCropBitmap.getRotateMatrix());
                mMatrix.postScale(mScaleRate, mScaleRate);
                mMatrix.postTranslate(offsetX, offsetY);

                RectF border = new RectF(offsetX, offsetY, offsetX + mCropBitmap.getWidth() * mScaleRate, offsetY + mCropBitmap.getHeight() * mScaleRate);

                mCropWindow = new CropWindow(border);

                mIsCropParamChanged = false;
            }
            canvas.drawBitmap(mCropBitmap.getBitmap(), mMatrix, mCropPainter);
            canvas.drawRect(mCropWindow.getWindowRectF(), mCropPainter);
            drawOutsideCropArea(canvas);
            drawDragIcons(canvas);
        }
        canvas.restore();
        super.onDraw(canvas);
    }


    private static final float DETECT_THRESHOLD = (float) 0.05;

    private PointF mPoint = new PointF(0, 0);
    private boolean mIsDetectStarted = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCropBitmap != null) {
            if ( event.getPointerCount() != 1) {
                mIsDetectStarted = false;
                return false;
            }else {

                int action = event.getAction() & MotionEvent.ACTION_MASK;
                if (action == MotionEvent.ACTION_DOWN) {
                    mCropWindow.onTouchDown(event.getX(), event.getY());
                    mIsDetectStarted = true;
                } else if (action == MotionEvent.ACTION_UP) {
                    mCropWindow.onTouchUp();
                    mIsDetectStarted = false;
                } else if (mIsDetectStarted && action == MotionEvent.ACTION_MOVE) {
                    if (Math.abs(mPoint.x - event.getX()) > DETECT_THRESHOLD || Math.abs(mPoint.y - event.getY()) > DETECT_THRESHOLD) {

                        mCropWindow.onTouchMoved(event.getX() - mPoint.x, event.getY() - mPoint.y);
                        invalidate();
                    }
                }

                mPoint.set(event.getX(), event.getY());

            }
        }
        return true;
    }

}
