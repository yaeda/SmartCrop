package com.kazeor.android.smartcrop;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.test.InstrumentationTestCase;

public class OptionTest extends InstrumentationTestCase {

    final float ASSERT_FLOAT_DELTA = 0.00001f;
    Frame frame = null;

    private Context getApplicationContext() {
        return getInstrumentation().getTargetContext().getApplicationContext();
    }

    private Resources getResources() {
        return getApplicationContext().getResources();
    }

    public void setUp() throws Exception {
        super.setUp();
        TestSet testSet = AssetUtil.loadTestSet(getResources(), "test_monkey.json");
        Bitmap bitmap = AssetUtil.loadTestBitmap(getResources(), testSet.filename);
        frame = new Frame.Builder().setBitmap(bitmap).build();
    }

    public void tearDown() throws Exception {
        frame.getBitmap().recycle();
    }

    // crop should be something sane
    public void testAspectOption1by1() throws Exception {
        SmartCrop smartcrop = new SmartCrop.Builder().build();
        CropResult cropResult = smartcrop.crop(frame, 1);

        Bitmap bitmap = frame.getBitmap();
        assertEquals(
                bitmap.getWidth() * cropResult.getTopCrop().getWidth(),
                bitmap.getHeight() * cropResult.getTopCrop().getHeight()
        );
    }

    public void testAspectOption16by9() throws Exception {
        float aspect = 16f / 9f;
        SmartCrop smartcrop = new SmartCrop.Builder().build();
        CropResult cropResult = smartcrop.crop(frame, aspect);

        Bitmap bitmap = frame.getBitmap();
        assertEquals(
                aspect,
                (bitmap.getWidth() * cropResult.getTopCrop().getWidth()) /
                        (bitmap.getHeight() * cropResult.getTopCrop().getHeight()),
                ASSERT_FLOAT_DELTA
        );
    }

    public void testAspectOption9by16() throws Exception {
        float aspect = 9f / 16f;
        SmartCrop smartcrop = new SmartCrop.Builder().build();
        CropResult cropResult = smartcrop.crop(frame, aspect);

        Bitmap bitmap = frame.getBitmap();
        assertEquals(
                aspect,
                (bitmap.getWidth() * cropResult.getTopCrop().getWidth()) /
                        (bitmap.getHeight() * cropResult.getTopCrop().getHeight()),
                ASSERT_FLOAT_DELTA
        );
    }

    public void testOutputScoreMap_Disabled() throws Exception {
        SmartCrop smartcrop = new SmartCrop.Builder()
                .build();
        CropResult cropResult = smartcrop.crop(frame, 1);

        assertNull(cropResult.getScoreMap());
    }

    public void testOutputScoreMap_Enabled() throws Exception {
        SmartCrop smartcrop = new SmartCrop.Builder()
                .shouldOutputScoreMap()
                .build();
        CropResult cropResult = smartcrop.crop(frame, 1);

        assertNotNull(cropResult.getScoreMap());
    }
}
