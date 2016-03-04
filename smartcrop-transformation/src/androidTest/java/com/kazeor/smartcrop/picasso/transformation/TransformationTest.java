package com.kazeor.smartcrop.picasso.transformation;

import android.graphics.Bitmap;
import android.test.InstrumentationTestCase;

public class TransformationTest extends InstrumentationTestCase {

    public void testTransformation() {
        int width = 100;
        int height = 100;
        SmartCropTransformation transformation = new SmartCropTransformation(width, height);

        int[] pixels = new int[width * height];
        Bitmap transformedBitmap = transformation.transform(
                Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888));

        assertEquals(transformedBitmap.getWidth(), width);
        assertEquals(transformedBitmap.getHeight(), height);
    }

}
