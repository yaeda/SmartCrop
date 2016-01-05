package com.kazeor.android.smartcrop.sample.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.kazeor.android.smartcrop.SmartCrop;

public class ResultView extends ImageView {

    private SmartCrop.CropResult mCropResult = null;

    public ResultView(Context context) {
        super(context);
    }

    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ResultView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCropResult(SmartCrop.CropResult cropResult) {
        mCropResult = cropResult;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCropResult != null) {
            Paint paint = new Paint();
            paint.setColor(Color.argb(255, 0, 255, 0));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f);
            paint.setShadowLayer(10.f, 1.f, 1.f, 0xFF000000);
            paint.setAntiAlias(true);

            int width = getWidth();
            int height = getHeight();
            float fx = width * mCropResult.topCrop.x;
            float fy = height * mCropResult.topCrop.y;
            float fw = width * mCropResult.topCrop.width;
            float fh = height * mCropResult.topCrop.height;
            canvas.drawRect(fx, fy, fx + fw, fy + fh, paint);
        }
    }

}
