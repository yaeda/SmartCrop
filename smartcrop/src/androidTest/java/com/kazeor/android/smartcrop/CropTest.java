package com.kazeor.android.smartcrop;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.test.InstrumentationTestCase;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class CropTest extends InstrumentationTestCase {

    private static final String TAG = CropTest.class.getSimpleName();
    SmartCrop smartcrop = null;

    private Context getApplicationContext() {
        return getInstrumentation().getTargetContext().getApplicationContext();
    }

    private Resources getResoutce() {
        return getApplicationContext().getResources();
    }

    public void setUp() throws Exception {
        super.setUp();
        smartcrop = new SmartCrop();
    }

    public void tearDown() throws Exception {
    }

    // crop should be something sane
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

        // finalize
        bitmap.recycle();
    }

    // crop should crop smartly
    public void testCropMonkeySmartly() throws Exception {
        AssetManager assetManager = getResoutce().getAssets();
        InputStream isJson = assetManager.open("test_monkey.json");
        TestSet testSet = new ObjectMapper().readValue(isJson, TestSet.class);

        InputStream isImage = assetManager.open(testSet.filename);
        Bitmap bitmap = BitmapFactory.decodeStream(isImage);
        SmartCrop.Result result = smartcrop.crop(bitmap, 1);

        assertTrue(testSet.region.isInsideOf(result.topCrop));

        // finalize
        isJson.close();
        isImage.close();
        bitmap.recycle();
    }

    public void testCropFishSmartly() throws  Exception {
        AssetManager assetManager = getResoutce().getAssets();
        InputStream isJson = assetManager.open("test_fish.json");
        TestSet testSet = new ObjectMapper().readValue(isJson, TestSet.class);

        InputStream isImage = assetManager.open(testSet.filename);
        Bitmap bitmap = BitmapFactory.decodeStream(isImage);
        SmartCrop.Result result = smartcrop.crop(bitmap, 1);

        assertTrue(testSet.region.isInsideOf(result.topCrop));

        // finalize
        isJson.close();
        isImage.close();
        bitmap.recycle();
    }

}