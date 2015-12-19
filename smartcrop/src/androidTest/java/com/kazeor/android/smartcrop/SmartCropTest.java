package com.kazeor.android.smartcrop;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import junit.framework.TestCase;

/**
 * Created by 22715434 on 12/17/15.
 */
public class SmartCropTest extends TestCase {

    private static final String TAG = SmartCropTest.class.getSimpleName();
    SmartCrop smartcrop = null;

    public void setUp() throws Exception {
        super.setUp();
        smartcrop = new SmartCrop();
    }

    public void tearDown() throws Exception {
    }

    public void testCrop() throws Exception {
        int width = 128, height = 64;
        int[] colors = new int[width * height];
        int p = 0, red = Color.argb(255, 255, 0, 0), white = Color.argb(255, 255, 255, 255);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x > 96 && x < 96 + 16 && y > 32 && y < 32 + 16) {
                    colors[p] = red;
                } else {
                    colors[p] = white;
                }
                p++;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
        SmartCrop.Result result = smartcrop.crop(bitmap, 1);
        assertTrue(result.topCrop.x <= 96f / width);
        assertTrue(result.topCrop.y <= 32f / height);
        assertTrue(result.topCrop.width >= 16f / width);
        assertTrue(result.topCrop.height >= 16f / height);
        bitmap.recycle();
    }
}