package com.example.peter.photoeditapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;

import cn.jarlen.photoedit.operate.OperateUtils;
import cn.jarlen.photoedit.utils.FileUtils;

import static java.security.AccessController.getContext;

public class MainActivity extends Activity {
    /* 照相机拍照得到的图片 */
    private File mCurrentPhotoFile;
    private String photoPath = null, tempPhotoPath, camera_path = null;
    /* 用来标识请求照相功能的activity */
    private static final int CAMERA_WITH_DATA = 3023;
    private static final int PICTURE_WITH_EDIT = 3022;
    OperateUtils operateUtils;
    ImageView img_camera_callback;
    Button btn_openCamera, btn_photo_edit;
    RelativeLayout rleativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 0);
        operateUtils = new OperateUtils(this);
        rleativeLayout =findViewById(R.id.activity_main);
        img_camera_callback = findViewById(R.id.img_camera_callback);
        btn_openCamera = findViewById(R.id.btn_open_camera);
        btn_photo_edit = findViewById(R.id.btn_photo_edit);
        btn_openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPictureFormCamera();
            }
        });
        btn_photo_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PictureEditActivity.class);
                intent.putExtra("camera_path", camera_path);
                startActivityForResult(intent, PICTURE_WITH_EDIT);
            }
        });
    }

    /* 从相机中获取照片 */
    private void getPictureFormCamera() {
//        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE",null);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);

        tempPhotoPath = FileUtils.DCIMCamera_PATH + FileUtils.getNewFileName()
                + ".jpg";

        mCurrentPhotoFile = new File(tempPhotoPath);

        if (!mCurrentPhotoFile.exists()) {
            try {
                mCurrentPhotoFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        ContentValues contentValues = new ContentValues(1);
        contentValues.put(MediaStore.Images.Media.DATA, mCurrentPhotoFile.getAbsolutePath());
        Uri uri = this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        intent.putExtra("android.intent.extra.quickCapture", true);//启用快捷拍照
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                Uri.fromFile(mCurrentPhotoFile));
        startActivityForResult(intent, CAMERA_WITH_DATA);
    }

    private void compressed() {
        int degree =ImageDegreeUtil.getBitmapDegree(photoPath);
       Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        Bitmap resizeBmp = operateUtils.compressionFiller(photoPath,
//                rleativeLayout);
        Bitmap resizeBmp = operateUtils.compressionFiller(returnBm,
                rleativeLayout);
//        Bitmap bitmap = null;
        if (photoPath != null) {
            btn_photo_edit.setVisibility(View.VISIBLE);
//             bitmap = getRotateBitmap(resizeBmp, 90.0f);

            img_camera_callback.setImageBitmap(resizeBmp);
        }
//        Bitmap resizeBmp = BitmapFactory.decodeFile(photoPath);
        camera_path = SaveBitmap(resizeBmp, "saveTemp");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == CAMERA_WITH_DATA) {
            photoPath = tempPhotoPath;
            compressed();
//            if (photoPath != null) {
//                btn_photo_edit.setVisibility(View.VISIBLE);
//                Bitmap bitmap = getRotateBitmap(BitmapFactory.decodeFile(photoPath), 90.0f);
//
//                img_camera_callback.setImageBitmap(bitmap);
//            }
        }
        if (requestCode ==PICTURE_WITH_EDIT){
            String resultPath = data.getStringExtra("camera_path");
            Bitmap resultBitmap = BitmapFactory.decodeFile(resultPath);
            img_camera_callback.setImageBitmap(resultBitmap);
        }
    }

    // 将生成的图片保存到内存中
    public String SaveBitmap(Bitmap bitmap, String name) {
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


}
