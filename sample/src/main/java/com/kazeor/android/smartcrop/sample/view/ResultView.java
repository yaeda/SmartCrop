package com.kazeor.android.smartcrop.sample.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.kazeor.android.smartcrop.CropRegion;
import com.kazeor.android.smartcrop.CropResult;

public class ResultView extends ImageView {

    private CropResult mCropResult = null;

    private Paint mPaintRect;

    private Paint mPaintGrid;

    public ResultView(Context context) {
        super(context);
        initGraphicsObjects();
    }

    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGraphicsObjects();
    }

    public ResultView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initGraphicsObjects();
    }

    public void setCropResult(CropResult cropResult) {
        mCropResult = cropResult;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mCropResult != null && mCropResult.topCrop() != null) {
            CropRegion topCrop = mCropResult.topCrop();
            drawGrid(canvas, topCrop, mPaintGrid);
            drawCropRegion(canvas, topCrop, mPaintRect);
        }
    }

    private void initGraphicsObjects() {
        mPaintRect = new Paint();
        mPaintRect.setColor(Color.argb(255, 0, 255, 0));
        mPaintRect.setStyle(Paint.Style.STROKE);
        mPaintRect.setStrokeWidth(2f);
        mPaintRect.setShadowLayer(10.f, 1.f, 1.f, 0xFF000000);
        mPaintRect.setAntiAlias(true);

        mPaintGrid = new Paint();
        mPaintGrid.setColor(Color.argb(255, 0, 255, 0));
        mPaintGrid.setStyle(Paint.Style.STROKE);
        mPaintGrid.setStrokeWidth(1f);
        mPaintGrid.setAntiAlias(true);
    }

    private void drawCropRegion(Canvas canvas, CropRegion cropRegion, Paint paint) {
        int width = getWidth();
        int height = getHeight();
        float fx = width * cropRegion.x();
        float fy = height * cropRegion.y();
        float fw = width * cropRegion.width();
        float fh = height * cropRegion.height();
        canvas.drawRect(fx, fy, fx + fw, fy + fh, paint);
    }

    private void drawGrid(Canvas canvas, CropRegion cropRegion, Paint paint) {
        int width = getWidth();
        int height = getHeight();
        float fx = width * cropRegion.x();
        float fy = height * cropRegion.y();
        float f3w = width * cropRegion.width() / 3f;
        float f3h = height * cropRegion.height() / 3f;
        float[] points = {
                fx + f3w * 1, fy, fx + f3w * 1, fy + f3h * 3,
                fx + f3w * 2, fy, fx + f3w * 2, fy + f3h * 3,
                fx, fy + f3h * 1, fx + f3w * 3, fy + f3h * 1,
                fx, fy + f3h * 2, fx + f3w * 3, fy + f3h * 2,
        };
        canvas.drawLines(points, paint);
    }

}
