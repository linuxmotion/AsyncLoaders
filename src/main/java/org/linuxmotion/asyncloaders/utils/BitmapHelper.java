package org.linuxmotion.asyncloaders.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.linuxmotion.asyncloaders.LogWrapper;

public class BitmapHelper {
    private static final String TAG = BitmapHelper.class.getSimpleName();

    public static Bitmap decodeSampledBitmapFromResource(Resources resc, int reqWidth, int reqHeight, int id) throws OutOfMemoryError {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // decode bitmap to get its width + height
        // don't allocate memory for bitmap pixels
        BitmapFactory.decodeResource(resc, id, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resc, id, options);
    }

    public static Bitmap decodeSampledBitmapFromImage(String path, int reqWidth, int reqHeight) throws OutOfMemoryError {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // decode bitmap to get its width + height
        // don't allocate memory for bitmap pixels
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image

        final int height = options.outHeight;
        final int width = options.outWidth;


        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        LogWrapper.Logi(TAG, "New insamplesize = " + inSampleSize);
        return inSampleSize;
    }
}