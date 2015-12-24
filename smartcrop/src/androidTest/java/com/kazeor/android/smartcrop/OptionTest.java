package com.kazeor.android.smartcrop;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.InstrumentationTestCase;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class OptionTest extends InstrumentationTestCase {

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
    public void testAspectOption() throws Exception {
        AssetManager assetManager = getResoutce().getAssets();
        InputStream isJson = assetManager.open("test_monkey.json");
        TestSet testSet = new ObjectMapper().readValue(isJson, TestSet.class);

        InputStream isImage = assetManager.open(testSet.filename);
        Bitmap bitmap = BitmapFactory.decodeStream(isImage);
        SmartCrop.Result result = smartcrop.crop(bitmap, 1);

        assertEquals(
                bitmap.getWidth() * result.topCrop.width,
                bitmap.getHeight() * result.topCrop.height
        );

        // finalize
        isJson.close();
        isImage.close();
        bitmap.recycle();
    }

}
