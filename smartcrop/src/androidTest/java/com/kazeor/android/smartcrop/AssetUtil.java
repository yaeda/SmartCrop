package com.kazeor.android.smartcrop;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class AssetUtil {

    public static TestSet loadTestSet(Resources resources, String testSetFile) {
        AssetManager assetManager = resources.getAssets();
        TestSet testSet = null;

        try {
            InputStream inputStream = assetManager.open(testSetFile);
            testSet = new ObjectMapper().readValue(inputStream, TestSet.class);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return testSet;
    }

    public static Bitmap loadTestBitmap(Resources resources, String filename) {
        AssetManager assetManager = resources.getAssets();
        Bitmap bitmap = null;
        try {
            InputStream inputStream = assetManager.open(filename);
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
