package org.linuxmotion.asyncloaders;

import java.util.concurrent.RejectedExecutionException;


import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
//import android.support.v4.util.LruCache;
import android.util.LruCache;
import android.widget.ImageView;

import org.linuxmotion.asyncloaders.io.DiskLruImageCache;
import org.linuxmotion.asyncloaders.utils.AeSimpleSHA1;
import org.linuxmotion.asyncloaders.utils.AsyncDrawable;


public class ImageLoader {
    private static final String TAG = ImageLoader.class.getSimpleName();
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    private Bitmap mLoadingMap;
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruImageCache mDiskCache;
    private int mMemClass = 0;
    private boolean mUseCache = true;
    private Context mContext;
    private boolean mTaksHeld = false;


    /**
     * Initialize the the image loader
     * <p/>
     * Uses a default image map for the loading icon
     *
     * @param context
     */
    public ImageLoader(Context context, int res) {
        this(context,
                ((BitmapDrawable) context.getResources().getDrawable(res)).getBitmap(),
                true,
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass());


    }

    /**
     * @param context
     * @param useCache
     */
    public ImageLoader(Context context, int res, boolean useCache) {
        this(context,
                ((BitmapDrawable) context.getResources().getDrawable(res)).getBitmap(),
                useCache,
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass());


    }

    /**
     * @param context
     * @param loading
     * @param cacheSize
     */
    public ImageLoader(Context context, Bitmap loading, int cacheSize) {
        mContext = context;
        mLoadingMap = loading;
        mUseCache = true;
        mMemClass = cacheSize;
        if (mUseCache) {
            initMemCache(true, mMemClass);
            initDiskCache(context, DISK_CACHE_SIZE);
        }
    }

    /**
     * @param context
     * @param loading
     * @param useCache
     * @param memClass
     */
    public ImageLoader(Context context, Bitmap loading, boolean useCache, int memClass) {
        mContext = context;
        mLoadingMap = loading;
        mUseCache = useCache;
        mMemClass = memClass;
        if (mUseCache) {
            initMemCache(true, mMemClass);
            initDiskCache(context, DISK_CACHE_SIZE);
        }
    }

    /**
     * @param context
     * @param loading
     * @param cacheSize
     * @param diskCacheSize
     */
    public ImageLoader(Context context, Bitmap loading, int cacheSize, int diskCacheSize) {
        mContext = context;
        mLoadingMap = loading;
        mUseCache = true;
        mMemClass = cacheSize;
        if (mUseCache) {
            initMemCache(true, cacheSize);
            initDiskCache(context, diskCacheSize);
        }
    }

    /**
     * Is there a task running on the current imageview.
     * If there is a task running, check to see if the
     * imageview is being reused. If it is not being reused
     * then a new task should be started with the required
     * data. If imageview is being reused then cancel
     * the current running task
     * @param abspath The path to the image to decode
     * @param imageView The imageview in which to store the decoded image
     * @return True if the decoding was a reused imageview and it was not cancelled
     */
    private static boolean cancelPotentialDecoding(String abspath, ImageView imageView) {
        ImageLoaderTask bitmapDownloaderTask = getImageLoaderTask(imageView);


        if (bitmapDownloaderTask != null) {
            // Get the loader task key
            String bitmapPath = bitmapDownloaderTask.getKey();

            String retHash = null;
            if (bitmapPath != null){
                // Create sha1 of the saved key
                retHash = AeSimpleSHA1.SHA1(bitmapPath);
            }
            else {
                // The task must not have run
                LogWrapper.Logv(TAG, "No key to loaderTask");
            }

            // create sha1 of the absolute path to image, (local path only??)
            String absHash = AeSimpleSHA1.SHA1(abspath);


            LogWrapper.Logi(TAG, "The Sha1 has of the path: " + bitmapPath + " is hash: " + retHash);

            if ((retHash != null) || (absHash != null))
                return false;
            // Cancel the task if the view is being reused
            // the saved key hash should match the path provided
            // if it is the same picture and imageview
            if (retHash != absHash) {

                // The ta
                boolean cancelled = bitmapDownloaderTask.cancel(true);
                LogWrapper.Logi(TAG, "Prevoius task for image key: " +
                        retHash + " was cancelled = " + cancelled
                        );
                return false;

            }
            else
            {
                return true;
            }

        }
        return false;
    }

    protected static ImageLoaderTask getImageLoaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                AsyncDrawable downloadedDrawable = (AsyncDrawable) drawable;
                return downloadedDrawable.getImageLoaderTask();
            }
        }
        return null;
    }

    public void setImage(String abspath, ImageView imageView) {

        if (abspath == null) {
            throw new NullPointerException("The path for the image view is null");
        }
        if (imageView == null) {
            throw new NullPointerException("The imageview is null");
        }

        // Cancel a current task if it exists
        if (cancelPotentialDecoding(abspath, imageView)) {
            // There was a reused imageview and it was not
            // a different image, use the mem cache if it
            //exists and is available
            if(!setBitmapFromMemCache(abspath, imageView)){
                LogWrapper.Logd(TAG, "Cached image not available.");
            }
            else {
                // The cached image was used
                return;
            }
        }

        // Start a new task for the current imageview
        ImageLoaderTask task = new ImageLoaderTask(this, imageView);
        AsyncDrawable downloadedDrawable = new AsyncDrawable(mContext.getResources(), mLoadingMap, task);
        imageView.setImageDrawable(downloadedDrawable);
        try {
            task.execute(abspath);
        } catch (RejectedExecutionException e) {
            //e.printStackTrace();
            // Is the cancel function having a fall through
        }



    }

    private boolean setBitmapFromMemCache(String abspath, ImageView imageView) {
        if (mUseCache) {

            Bitmap bmap = getBitmapFromMemCache(AeSimpleSHA1.SHA1(abspath));
            if (bmap != null) {

                imageView.setImageBitmap(bmap);
                return true;
            }
        }
        return false;
    }

    private void initDiskCache(Context context, int cacheSize) {
        mDiskCache = new DiskLruImageCache(context, DISK_CACHE_SUBDIR, cacheSize, CompressFormat.JPEG, 50);

    }

    /**
     * @param defaultSize Use the default size cache as per device
     * @param memClass    The memClass or the size of the class if defaultSize
     *                    is false
     */
    private void initMemCache(boolean defaultSize, int memClass) {


        int cacheSize = 1;
        if (defaultSize) {
            // Use 1/8th of the available memory for this memory cache.
            cacheSize = 1024 * 1024 * memClass / 8;
            LogWrapper.Logi(TAG, "The cache size in memory is " + (memClass / 8) + "MB");
        } else {
            // Use the user defined cache size
            cacheSize = 1024 * 1024 * memClass;
            LogWrapper.Logi(TAG, "The cache size in memory is " + memClass + "MB");
        }


        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in bytes rather than number of items.
                return bitmap.getRowBytes() * bitmap.getHeight();// int result permits bitmaps up to 46,340 x 46,340
            }
        };


    }

    /**
     * @param key    The key to store the Bitmap against
     * @param bitmap The bitmap to store
     * @return True if the bitmap was added succesfully
     */
    public synchronized boolean addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (mUseCache && getBitmapFromMemCache(key) == null) {
            LogWrapper.Logv(TAG, "Setting mem cache file for bitmap " + key);
            mMemoryCache.put(key, bitmap);
            return true;
        }
        return false;
    }

    /**
     * @param key    The key to store the Bitmap against
     * @param bitmap The bitmap to store
     * @return True if the bitmap was added successfully
     */
    public synchronized boolean addBitmapToDiskCache(String key, Bitmap bitmap) {
        if (mUseCache && getBitmapFromDiskCache(key) == null) {
            LogWrapper.Logv(TAG, "Setting disk cache file for bitmap " + key);
            mDiskCache.put(key, bitmap);
            return true;
        }
        return false;
    }

    public synchronized Bitmap getBitmapFromMemCache(String key) {

        if (key == null) return null;
        Bitmap b = mUseCache ? mMemoryCache.get(key) : null;
        if (b != null)
            LogWrapper.Logv(TAG, "Retrived mem cached bitmap for " + key);
        return b;
    }

    public synchronized Bitmap getBitmapFromDiskCache(String key) {
        if (key == null) return null;
        Bitmap b = mUseCache ? mDiskCache.getBitmap(key) : null;
        if (b != null)
            LogWrapper.Logv(TAG, "Retrived disk cached bitmap for " + key);
        return b;
    }

    public void holdTaskLoader() {
        mTaksHeld = true;

    }

    public void releaseTaskLoader() {
        mTaksHeld = false;

    }

}