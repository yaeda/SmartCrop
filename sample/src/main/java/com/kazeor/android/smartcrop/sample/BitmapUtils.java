package com.kazeor.android.smartcrop.sample;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {

    public static final int SIZE_VGA = 640 * 480;
    public static final int SIZE_QVGA = 320 * 240;
    public static final int SIZE_QQVGA = 160 * 120;

    private static class Size {
        int width;
        int height;
        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    private static Size loadBitmapSize(ContentResolver contentResolver, Uri uri) {
        Size size = null;
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            size = new Size(options.outWidth, options.outHeight);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (FileNotFoundException e) { // openInputStream
            e.printStackTrace();
        } catch (IOException e) { // close
            e.printStackTrace();
        }

        return size;
    }

    public static Bitmap createBitmap(ContentResolver contentResolver, Uri uri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        Bitmap bitmap = null;
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (FileNotFoundException e) { // openInputStream
            e.printStackTrace();
        } catch (IOException e) { // close
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap createScaledBitmap(ContentResolver contentResolver, Uri uri, int targetAreaSize) {
        Size originalSize = loadBitmapSize(contentResolver, uri);
        if (originalSize == null) {
            return null;
        }

        Bitmap scaledBitmap;
        int originalWidth = originalSize.getWidth();
        int originalHeight = originalSize.getHeight();
        int originalAreaSize = originalWidth * originalHeight;
        if (originalAreaSize > targetAreaSize) {
            float scale = (float)Math.sqrt((float)originalAreaSize / (float)targetAreaSize);
            // load small size
            int sampleSize = (int)Math.floor((double)scale);
            Bitmap tmpBitmap = createBitmap(contentResolver, uri, sampleSize);
            // load exact size
            scaledBitmap = Bitmap.createScaledBitmap(tmpBitmap,
                    Math.round(originalWidth / scale),
                    Math.round(originalHeight / scale),
                    false);

            tmpBitmap.recycle();
        } else {
            scaledBitmap = createBitmap(contentResolver, uri, 1); // no sampling
        }

        return scaledBitmap;
    }

}
