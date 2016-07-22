package com.g.mike.orbdemo;

/**
 * Created by iosuser12 on 7/22/16.
 */
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.PriorityQueue;

/**
 * Created by Mike on 6/30/2016.
 */
public class ImageStore {
    private static final int MEDIA_TYPE_JPEG = 1;
    private static final int MEDIA_TYPE_BMP = 2;
    public static final int SAVE_AS_JPEG = 3;
    public static final int SAVE_AS_BITMAP = 4;

    private  Bitmap image;
    public ImageStore(byte[] data){
        image = BitmapFactory.decodeByteArray(data,0,data.length);
    }
    public void store(int orientation, int saveFormat){
        //TODO implement dialogFragment to choose
        if(orientation==0)
            rotate(90);
        else
            rotate(orientation);

        if(saveFormat == SAVE_AS_BITMAP) {
            saveAsBitmap();
        }
        if(saveFormat == SAVE_AS_JPEG) {
            saveAsIs();
        }
    }

    public void store(int saveFormat){
        if(saveFormat == SAVE_AS_BITMAP) {
            saveAsBitmap();
        }
        if(saveFormat == SAVE_AS_JPEG) {
            saveAsIs();
        }
    }
    private void rotate(int degree) {
        Matrix matrix = new Matrix();

        matrix.postRotate(degree);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, image.getWidth(), image.getHeight(), true);

        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        image = rotatedBitmap;
    }


    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "ShaderCam");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("ShaderCam", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_JPEG){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_BMP) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "BMP_"+ timeStamp + ".png");
        } else {
            return null;
        }

        return mediaFile;
    }
    private void writeAsJPEG(File file){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void writeUncompressed(File file){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void saveAsBitmap(){
        File bmpFile = getOutputMediaFile(MEDIA_TYPE_BMP);
        writeUncompressed(bmpFile);
    }
    public void saveAsIs() {
        File jpgFile = getOutputMediaFile(MEDIA_TYPE_JPEG);
        writeAsJPEG(jpgFile);
    }

}
