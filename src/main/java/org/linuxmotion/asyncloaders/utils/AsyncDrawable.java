package org.linuxmotion.asyncloaders.utils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import org.linuxmotion.asyncloaders.ImageLoaderTask;

public class AsyncDrawable extends BitmapDrawable {
    private final WeakReference<ImageLoaderTask> ImageloaderTaskReference;

    public AsyncDrawable(Resources res, Bitmap bitmap, ImageLoaderTask bitmaploaderTask) {
        super(res, bitmap);
        ImageloaderTaskReference =
                new WeakReference<ImageLoaderTask>(bitmaploaderTask);

    }

    public void Recycle(){
        try {
            ImageloaderTaskReference.get().get().recycle();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public ImageLoaderTask getImageLoaderTask() {
        return ImageloaderTaskReference.get();
    }
}