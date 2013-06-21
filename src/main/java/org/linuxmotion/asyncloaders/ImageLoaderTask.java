package org.linuxmotion.asyncloaders;

import java.lang.ref.WeakReference;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = ImageLoaderTask.class.getSimpleName();
    private final WeakReference<ImageView> imageViewReference;
    private String mKey;
    private String mPath;
    private ImageLoader mLoader;

    public ImageLoaderTask(ImageLoader loader, ImageView imageView) {
        mLoader = loader;
        imageViewReference = new WeakReference<ImageView>(imageView);

    }

    @Override
    // Actual download method, run in the task thread
    protected Bitmap doInBackground(String... params) {
        // params comes from the execute() call: params[0] is the url.
        if (params.length == 0) {

            return null;
        }
        mPath = params[0];
        mKey = AeSimpleSHA1.SHA1(mPath);
        //String f = new File(params[0]).getName();

        //String hash = String.valueOf((new File(f)).hashCode());

        if (isCancelled()) return null;

        try {
            if (isCancelled()) return null;
            Bitmap bitmap = mLoader.getBitmapFromDiskCache(mKey);
            if (bitmap != null)
                LogWrapper.Logv(TAG, "Using disk cached bitmap for image = " + mKey);
            // No cached bitmap found
            if (bitmap == null) bitmap = BitmapHelper.decodeSampledBitmapFromImage(mPath, 50, 50);
            if (bitmap != null) {// Add bitmap to cache if bitmap was decoded

                LogWrapper.Logi(TAG, "[Bitmap Height = " + bitmap.getHeight() + "]");
                LogWrapper.Logi(TAG, "[Bitmap Width = " + bitmap.getWidth() + "]");
                mLoader.addBitmapToDiskCache(mKey, bitmap);
                mLoader.addBitmapToMemoryCache(mKey, bitmap);
            }


            return bitmap;// Return the bitmap,
            //can still be null here if it could not decode, ie a video file

        } catch (OutOfMemoryError e) {
            if (isCancelled()) return null;
            LogWrapper.Loge("ImageLoaderTask", "Failed to decode the bitmap due to Out of Memory Error");
            System.gc(); // Try and start garbage collection

            if (isCancelled()) return null;
        }

        return null;
    }

    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap.recycle();
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            ImageView imageView = imageViewReference.get();
            ImageLoaderTask bitmapDownloaderTask = ImageLoader.getImageLoaderTask(imageView);
            // Change bitmap only if this process is still associated with it
            if (this == bitmapDownloaderTask) {
                imageView.setImageBitmap(bitmap);
            }

        }
    }

    /**
     * @return the url
     */
    public String getKey() {
        return mKey;
    }

    public ImageView getReference() {
        if (imageViewReference != null)
            return imageViewReference.get();
        else
            return null;
    }

}