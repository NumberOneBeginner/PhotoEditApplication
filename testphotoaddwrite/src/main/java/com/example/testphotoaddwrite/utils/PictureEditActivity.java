package com.example.testphotoaddwrite.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.testphotoaddwrite.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PictureEditActivity extends Activity implements View.OnClickListener {
    private String camera_path = null;
    private OperateUtils operateUtils;
    private LinearLayout linearLayout;
    private TextView text_banana;
    private Button btn_cancel, btn_ok;
    private OperateView operateView;
    private String mPath = null;
    float x = 0;
    float y = 0;
    float DownX;
    float DownY;
    long currentMS;
    int moveX, moveY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_edit);
        Intent intent = getIntent();
        camera_path = intent.getStringExtra("camera_path");
        operateUtils = new OperateUtils(this);
        init();
        fillContent();
    }

    private void init() {
        linearLayout = findViewById(R.id.ll_content);
        text_banana = findViewById(R.id.text_banana);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_ok = findViewById(R.id.btn_ok);
        btn_cancel.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        text_banana.setOnClickListener(this);
    }

    private void fillContent() {
        Bitmap resizeBmp = BitmapFactory.decodeFile(camera_path);
        operateView = new OperateView(PictureEditActivity.this, resizeBmp);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                resizeBmp.getWidth(), resizeBmp.getHeight());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        operateView.setLayoutParams(layoutParams);
        linearLayout.addView(operateView);
        operateView.setMultiAdd(true); // 设置此参数，可以添加多个图片

     operateView.setOnPictureListener(new OperateView.PictureClickListener() {
         @Override
         public void onPictureClick(float x, float y) {
             addpic(R.mipmap.banana, x, y);
         }
     });
//        operateView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        DownX = event.getX();//float DownX
//                        DownY = event.getY();//float DownY
//                        moveX = 0;
//                        moveY = 0;
//                        currentMS = System.currentTimeMillis();//long currentMS     获取系统时间
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        moveX += Math.abs(event.getX() - DownX);//X轴距离
//                        moveY += Math.abs(event.getY() - DownY);//y轴距离
//                        DownX = event.getX();
//                        DownY = event.getY();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        long moveTime = System.currentTimeMillis() - currentMS;//移动时间
//                        //判断是否继续传递信号
//                        if (moveTime > 200 && (moveX > 20 || moveY > 20)) {
//                            return false; //不再执行后面的事件，在这句前可写要执行的触摸相关代码。点击事件是发生在触摸弹起后
//                        }
//                        x = event.getX();
//                        y = event.getY();
//                        return true;
//                }
//                return false;
//            }
//        });
    }

    private void btnSave() {
        operateView.save();
        Bitmap bmp = getBitmapByView(operateView);
        if (bmp != null) {
            mPath = saveBitmap(bmp, "saveTemp");
            Intent okData = new Intent();
            okData.putExtra("camera_path", mPath);
            setResult(RESULT_OK, okData);
            this.finish();
        }
    }

    // 将模板View的图片转化为Bitmap
    public Bitmap getBitmapByView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    // 将生成的图片保存到内存中
    public String saveBitmap(Bitmap bitmap, String name) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File dir = new File(Constants.filePath);
            if (!dir.exists())
                dir.mkdir();
            File file = new File(Constants.filePath + name + ".jpg");
            FileOutputStream out;

            try {
                out = new FileOutputStream(file);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                    out.flush();
                    out.close();
                }
                return file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void addpic(int position) {
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), position);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), position);
        // ImageObject imgObject = operateUtils.getImageObject(bmp);
        ImageObject imgObject = operateUtils.getImageObject(bmp, operateView,
                5, 150, 100);
        operateView.addItem(imgObject);
    }

    private void addpic(int position, float x, float y) {
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), position);
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), position);
        // ImageObject imgObject = operateUtils.getImageObject(bmp);
        ImageObject imgObject = operateUtils.getImageObject(bmp, operateView,
                5, (int) x, (int) y);
        operateView.addItem(imgObject);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                btnSave();
                break;
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.text_banana:
                addpic(R.mipmap.banana);
                break;
            default:

                break;
        }
    }
}
