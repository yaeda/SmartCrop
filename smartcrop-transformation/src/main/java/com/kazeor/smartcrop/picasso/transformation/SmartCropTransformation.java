package com.kazeor.smartcrop.picasso.transformation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.kazeor.android.smartcrop.CropRegion;
import com.kazeor.android.smartcrop.CropResult;
import com.kazeor.android.smartcrop.Frame;
import com.kazeor.android.smartcrop.SmartCrop;
import com.squareup.picasso.Transformation;

public class SmartCropTransformation implements Transformation {

    private int width;
    private int height;

    public SmartCropTransformation(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        float aspect = (float)width / (float)height;
        Frame frame = new Frame.Builder().bitmap(source).build();
        SmartCrop smartCrop = new SmartCrop.Builder().build();
        CropResult cropResult = smartCrop.crop(frame, aspect);
        CropRegion cropRegion = cropResult.topCrop();

        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        float x = cropRegion.x() * sourceWidth;
        float y = cropRegion.y() * sourceHeight;
        float w = cropRegion.width() * sourceWidth;
        float h = cropRegion.height() * sourceHeight;
        RectF dstRect = new RectF(x, y, x + w, y + h);

        Bitmap bitmap = Bitmap.createBitmap(width, height, source.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(source, null, dstRect, null);
        source.recycle();

        return bitmap;
    }

    @Override
    public String key() {
        return "SmartCropTransformation(width=" + width + ", height=" + height + ")";
    }
}
