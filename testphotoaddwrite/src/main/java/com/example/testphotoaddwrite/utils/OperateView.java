/*
 *          Copyright (C) 2016 jarlen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.example.testphotoaddwrite.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.example.testphotoaddwrite.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jarlen
 */
public class OperateView extends View {
    private List<ImageObject> imgLists = new ArrayList<ImageObject>();
    private Rect mCanvasLimits;
    private Bitmap bgBmp;
    private Paint paint = new Paint();
    private Context mContext;
    private boolean isMultiAdd;// true 代表可以添加多个水印图片（或文字），false 代表只可添加单个水印图片（或文字）
    private float picScale = 0.4f;
    private float pictureX, pictureY;
    private float moveX, moveY, DownX, DownY;
    long currentMS;
    boolean clickEnable;
    boolean enablePictureClick = true;

    /**
     * 设置水印图片初始化大小
     *
     * @param picScale
     */
    public void setPicScale(float picScale) {
        this.picScale = picScale;
    }

    /**
     * 设置是否可以添加多个图片或者文字对象
     *
     * @param isMultiAdd true 代表可以添加多个水印图片（或文字），false 代表只可添加单个水印图片（或文字）
     */
    public void setMultiAdd(boolean isMultiAdd) {
        this.isMultiAdd = isMultiAdd;
    }

    public OperateView(Context context, Bitmap resizeBmp) {
        super(context);
        this.mContext = context;
        bgBmp = resizeBmp;
        // 获取屏幕密度（方法2）
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels; // 屏幕宽（像素，如：480px）
        int height = dm.heightPixels; // 屏幕高（像素，如：800px）
//		int width = bgBmp.getWidth();
//		int height = bgBmp.getHeight();
        mCanvasLimits = new Rect(0, 0, width, height);
    }

    /**
     * 将图片对象添加到View中
     *
     * @param imgObj 图片对象
     */
    public void addItem(ImageObject imgObj) {
        if (imgObj == null) {
            return;
        }
        if (!isMultiAdd && imgLists != null) {
            imgLists.clear();
        }
        imgObj.setSelected(true);
        if (!imgObj.isTextObject) {
            imgObj.setScale(picScale);
        }
        ImageObject tempImgObj = null;
        for (int i = 0; i < imgLists.size(); i++) {
            tempImgObj = imgLists.get(i);
            tempImgObj.setSelected(false);
        }
        imgLists.add(imgObj);
        invalidate();
    }

    /**
     * 画出容器内所有的图像
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int sc = canvas.save();
//        canvas.clipRect(mCanvasLimits);
//        canvas.drawBitmap(bgBmp, 0, 0, paint);
        canvas.drawBitmap(bgBmp, new Rect(0, 0, bgBmp.getWidth(), bgBmp.getHeight()), new Rect(0, 0, getWidth(), getHeight()), null);
        drawImages(canvas);
        canvas.restoreToCount(sc);
        for (ImageObject ad : imgLists) {
            if (ad != null && ad.isSelected()) {
                ad.drawIcon(canvas);
            }
        }
    }

    public void save() {
        ImageObject io = getSelected();
        if (io != null) {
            io.setSelected(false);
        }
        invalidate();
    }


    /**
     * 根据触控点重绘View
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            handleSingleTouchManipulateEvent(event);
        } else {
            handleMultiTouchManipulateEvent(event);
        }
        invalidate();
        if (clickEnable) {
            myPictureListener.onPictureClick(event.getX(), event.getY());
            clickEnable = false;
            return true;
        }
        super.onTouchEvent(event);
        return true;
    }

    private boolean mMovedSinceDown = false;
    private boolean mResizeAndRotateSinceDown = false;
    private float mStartDistance = 0.0f;
    private float mStartScale = 0.0f;
    private float mStartRot = 0.0f;
    private float mPrevRot = 0.0f;
    static public final double ROTATION_STEP = 2.0;
    static public final double ZOOM_STEP = 0.01;
    static public final float CANVAS_SCALE_MIN = 0.25f;
    static public final float CANVAS_SCALE_MAX = 3.0f;
    private Point mPreviousPos = new Point(0, 0); // single touch events
    float diff;
    float rot;

    /**
     * 多点触控操作
     *
     * @param event
     */
    private void handleMultiTouchManipulateEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                float x1 = event.getX(0);
                float x2 = event.getX(1);
                float y1 = event.getY(0);
                float y2 = event.getY(1);
                float delX = (x2 - x1);
                float delY = (y2 - y1);
                diff = (float) Math.sqrt((delX * delX + delY * delY));
                mStartDistance = diff;
                // float q = (delX / delY);
                mPrevRot = (float) Math.toDegrees(Math.atan2(delX, delY));
                for (ImageObject io : imgLists) {
                    if (io.isSelected()) {
                        mStartScale = io.getScale();
                        mStartRot = io.getRotation();
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                x1 = event.getX(0);
                x2 = event.getX(1);
                y1 = event.getY(0);
                y2 = event.getY(1);
                delX = (x2 - x1);
                delY = (y2 - y1);
                diff = (float) Math.sqrt((delX * delX + delY * delY));
                float scale = diff / mStartDistance;
                float newscale = mStartScale * scale;
                rot = (float) Math.toDegrees(Math.atan2(delX, delY));
                float rotdiff = mPrevRot - rot;
                for (ImageObject io : imgLists) {
                    if (io.isSelected() && newscale < 10.0f && newscale > 0.1f) {
                        float newrot = Math.round((mStartRot + rotdiff) / 1.0f);
                        if (Math.abs((newscale - io.getScale()) * ROTATION_STEP) > Math
                                .abs(newrot - io.getRotation())) {
                            io.setScale(newscale);
                        } else {
                            io.setRotation(newrot % 360);
                        }
                        break;
                    }
                }

                break;
        }
    }

    /**
     * 获取选中的对象ImageObject
     *
     * @return
     */
    private ImageObject getSelected() {
        for (ImageObject ibj : imgLists) {
            if (ibj.isSelected()) {
                return ibj;
            }
        }
        return null;
    }

    private long selectTime = 0;

    /**
     * 单点触控操作
     *
     * @param event
     */
    private void handleSingleTouchManipulateEvent(MotionEvent event) {

        long currentTime = 0;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mMovedSinceDown = false;
                mResizeAndRotateSinceDown = false;
                int selectedId = -1;
                DownX = event.getX();//float DownX
                DownY = event.getY();//float DownY
                moveX = 0;
                moveY = 0;
                currentMS = System.currentTimeMillis();//long currentMS     获取系统时间

                for (int i = imgLists.size() - 1; i >= 0; --i) {
                    ImageObject io = imgLists.get(i);
                    if (io.contains(event.getX(), event.getY())
                            || io.pointOnCorner(event.getX(), event.getY(),
                            OperateConstants.RIGHTBOTTOM)
                            || io.pointOnCorner(event.getX(), event.getY(),
                            OperateConstants.LEFTTOP)) {
                        enablePictureClick = false;
                        io.setSelected(true);
                        imgLists.remove(i);
                        imgLists.add(io);
                        selectedId = imgLists.size() - 1;
                        currentTime = System.currentTimeMillis();
                        if (currentTime - selectTime < 300) {
                            if (myListener != null) {
                                if (getSelected().isTextObject()) {
                                    myListener
                                            .onClick((TextObject) getSelected());
                                }
                            }
                        }
                        selectTime = currentTime;
                        break;
                    }
                }
                if (selectedId < 0) {
                    for (int i = imgLists.size() - 1; i >= 0; --i) {
                        ImageObject io = imgLists.get(i);
                        if (io.contains(event.getX(), event.getY())
                                || io.pointOnCorner(event.getX(), event.getY(),
                                OperateConstants.RIGHTBOTTOM)
                                || io.pointOnCorner(event.getX(), event.getY(),
                                OperateConstants.LEFTTOP)) {
                            enablePictureClick = false;
                            io.setSelected(true);
                            imgLists.remove(i);
                            imgLists.add(io);
                            selectedId = imgLists.size() - 1;
                            break;
                        }
                    }
                }
                for (int i = 0; i < imgLists.size(); ++i) {
                    ImageObject io = imgLists.get(i);
                    if (i != selectedId) {
                        io.setSelected(false);
                    }
                }

                ImageObject io = getSelected();
                if (io != null) {
                    enablePictureClick = false;
                    if (io.pointOnCorner(event.getX(), event.getY(),
                            OperateConstants.LEFTTOP)) {
                        imgLists.remove(io);
                    } else if (io.pointOnCorner(event.getX(), event.getY(),
                            OperateConstants.RIGHTBOTTOM)) {
                        mResizeAndRotateSinceDown = true;
                        float x = event.getX();
                        float y = event.getY();
                        float delX = x - io.getPoint().x;
                        float delY = y - io.getPoint().y;
                        diff = (float) Math.sqrt((delX * delX + delY * delY));
                        mStartDistance = diff;
                        mPrevRot = (float) Math.toDegrees(Math
                                .atan2(delX, delY));
                        mStartScale = io.getScale();
                        mStartRot = io.getRotation();
                    } else if (io.contains(event.getX(), event.getY())) {
                        mMovedSinceDown = true;
                        mPreviousPos.x = (int) event.getX();
                        mPreviousPos.y = (int) event.getY();
                    }

                }
                break;

            case MotionEvent.ACTION_UP:

                mMovedSinceDown = false;
                mResizeAndRotateSinceDown = false;
                long moveTime = System.currentTimeMillis() - currentMS;//移动时间
                if (enablePictureClick) {
                    //判断是否继续传递信号
                    if (moveTime > 200 && (moveX > 20 || moveY > 20)) {
                        clickEnable = false;
                        break;
//                    return true; //不再执行后面的事件，在这句前可写要执行的触摸相关代码。点击事件是发生在触摸弹起后
                    } else {
                        clickEnable = true;
                        break;

                    }
                } else {
                    enablePictureClick = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // Log.i("jarlen"," 移动了");
                // 移动
                moveX += Math.abs(event.getX() - DownX);//X轴距离
                moveY += Math.abs(event.getY() - DownY);//y轴距离
                DownX = event.getX();
                DownY = event.getY();
                if (mMovedSinceDown) {
                    int curX = (int) event.getX();
                    int curY = (int) event.getY();
                    int diffX = curX - mPreviousPos.x;
                    int diffY = curY - mPreviousPos.y;
                    mPreviousPos.x = curX;
                    mPreviousPos.y = curY;
                    io = getSelected();
                    Point p = io.getPosition();
                    int x = p.x + diffX;
                    int y = p.y + diffY;
                    if (p.x + diffX >= mCanvasLimits.left
                            && p.x + diffX <= mCanvasLimits.right
                            && p.y + diffY >= mCanvasLimits.top
                            && p.y + diffY <= mCanvasLimits.bottom)
                        io.moveBy((int) (diffX), (int) (diffY));
                }
                // 旋转和缩放
                if (mResizeAndRotateSinceDown) {
                    io = getSelected();
                    float x = event.getX();
                    float y = event.getY();
                    float delX = x - io.getPoint().x;
                    float delY = y - io.getPoint().y;
                    diff = (float) Math.sqrt((delX * delX + delY * delY));
                    float scale = diff / mStartDistance;
                    float newscale = mStartScale * scale;
                    rot = (float) Math.toDegrees(Math.atan2(delX, delY));
                    float rotdiff = mPrevRot - rot;
                    if (newscale < 10.0f && newscale > 0.1f) {
                        float newrot = Math.round((mStartRot + rotdiff) / 1.0f);
                        if (Math.abs((newscale - io.getScale()) * ROTATION_STEP) > Math
                                .abs(newrot - io.getRotation())) {
                            io.setScale(newscale);
                        } else {
                            io.setRotation(newrot % 360);
                        }
                    }
                }
                break;
        }

        cancelLongPress();

    }

    /**
     * 循环画图像
     *
     * @param canvas
     */
    private void drawImages(Canvas canvas) {
        for (ImageObject ad : imgLists) {
            if (ad != null) {
                ad.draw(canvas);
            }
        }
    }

    /**
     * 向外部提供双击监听事件（双击弹出自定义对话框编辑文字）
     */
    MyListener myListener;

    //
    public void setOnListener(MyListener myListener) {
        this.myListener = myListener;
    }

    //
    public interface MyListener {
        public void onClick(TextObject tObject);
    }

    /**
     * 向外部提供点击击监听事件
     */
    PictureClickListener myPictureListener;

    public void setOnPictureListener(PictureClickListener myPictureListener) {
        this.myPictureListener = myPictureListener;
    }

    public interface PictureClickListener {
        public void onPictureClick(float x, float y);
    }


}
