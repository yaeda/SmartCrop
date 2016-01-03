package com.kazeor.android.smartcrop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.test.InstrumentationTestCase;

public class OptionTest extends InstrumentationTestCase {

    final float ASSERT_FLOAT_DELTA = 0.00001f;
    SmartCrop smartcrop = null;
    Bitmap bitmap = null;

    private Context getApplicationContext() {
        return getInstrumentation().getTargetContext().getApplicationContext();
    }

    private Resources getResources() {
        return getApplicationContext().getResources();
    }

    public void setUp() throws Exception {
        super.setUp();
        smartcrop = new SmartCrop.Builder().build();
        TestSet testSet = AssetUtil.loadTestSet(getResources(), "test_monkey.json");
        bitmap = AssetUtil.loadTestBitmap(getResources(), testSet.filename);
    }

    public void tearDown() throws Exception {
        bitmap.recycle();
    }

    // crop should be something sane
    public void testAspectOption1by1() throws Exception {
        SmartCrop.CropResult cropResult = smartcrop.crop(bitmap, 1);

        assertEquals(
                bitmap.getWidth() * cropResult.topCrop.width,
                bitmap.getHeight() * cropResult.topCrop.height
        );
    }

    public void testAspectOption16by9() throws Exception {
        float aspect = 16f / 9f;
        SmartCrop.CropResult cropResult = smartcrop.crop(bitmap, aspect);

        assertEquals(
                aspect,
                (bitmap.getWidth() * cropResult.topCrop.width) / (bitmap.getHeight() * cropResult.topCrop.height),
                ASSERT_FLOAT_DELTA
        );
    }

    public void testAspectOption9by16() throws Exception {
        float aspect = 9f / 16f;
        SmartCrop.CropResult cropResult = smartcrop.crop(bitmap, aspect);

        assertEquals(
                aspect,
                (bitmap.getWidth() * cropResult.topCrop.width) / (bitmap.getHeight() * cropResult.topCrop.height),
                ASSERT_FLOAT_DELTA
        );
    }

}