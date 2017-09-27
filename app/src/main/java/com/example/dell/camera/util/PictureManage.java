package com.example.dell.camera.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zxh on 2017/9/19.
 */
public class PictureManage {

    //按宽度缩放图片显示
    public static Bitmap fitBitmap(Bitmap target, int newLong)
    {
        int width = target.getWidth();
        int height = target.getHeight();
        Matrix matrix = new Matrix();
        if(width>height){
            int newHeight = newLong;
            float scaleHeight = ((float)newHeight) / height;
            int newWidth = (int) (scaleHeight * width);
            matrix.postScale(scaleHeight, scaleHeight);
        }else{
            float scaleWidth = ((float) newLong) / width;
            // float scaleHeight = ((float)newHeight) / height;
            int newHeight = (int) (scaleWidth * height);
            matrix.postScale(scaleWidth, scaleWidth);
        }
        // Bitmap result = Bitmap.createBitmap(target,0,0,width,height,
        // matrix,true);
        Bitmap bmp = Bitmap.createBitmap(target, 0, 0, width, height, matrix,
                true);
        if (target != null && !target.equals(bmp) && !target.isRecycled())
        {
            target.recycle();
            target = null;
        }
        return bmp;// Bitmap.createBitmap(target, 0, 0, width, height, matrix,
        // true);
    }

    //dp转px
    public static int dptopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //文件中读取Bitmap效率问题
    public static Bitmap readBitmap(String path){

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;

        options.inJustDecodeBounds=false;
        //整个图像，下采样
        Bitmap patch=Bitmap.createBitmap(bitmap, 10, 10, 100, 100);
        return patch;
    }

    //保存图片
  /*  public static void saveImage(Bitmap bmp) {
        File appDir = new File(Environment.getExternalStorageDirectory(), "Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
    public static String saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片

        File appDir = new File(Environment.getExternalStorageDirectory(),"Boohee");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
                                         bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + appDir+"/"+fileName)));
        return appDir+"/"+fileName;
    }

}
