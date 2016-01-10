package com.kazeor.android.smartcrop;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.SparseArray;

public class SmartCrop {
    private static final String TAG = "SmartCrop";

    // detail
    private float mDetailWeight = 0.2f;

    // skin
    private float[] mSkinColor = {0.78f, 0.57f, 0.44f};
    private float mSkinBias = 0.01f;
    private float mSkinBrightnessMin = 0.2f;
    private float mSkinBrightnessMax = 1.0f;
    private float mSkinThreshold = 0.8f;
    private float mSkinWeight = 1.8f;

    // saturation
    private float mSaturationBrightnessMin = 0.05f;
    private float mSaturationBrightnessMax = 0.9f;
    private float mSaturationThreshold = 0.4f;
    private float mSaturationBias = 0.2f;
    private float mSaturationWeight = 0.3f;

    // step * minscale rounded down to the next power of two should be good
    private int mScoreDownSample = 8;
    private float mStep = 8f;
    private float mScaleStep = 0.1f;
    private float mMinScale = 0.9f;
    private float mMaxScale = 1.0f;

    private float mEdgeRadius = 0.4f;
    private float mEdgeWeight = -20.0f;
    private float mOutsideImportance = -0.5f;
    private boolean mRuleOfThirds = true;
    //private boolean mPrescale = true;

    private boolean mShouldOutputScoreMap = false;

    public class CropResult {
        public CropRegion topCrop = null;
        public SparseArray<CropRegion> crops = null;
        public Bitmap scoreMap = null;
    }

    public class CropRegion {
        public float x;
        public float y;
        public float width;
        public float height;
        public CropScore score = null;
        public CropRegion(float x, float y, float width, float height, CropScore score) {
            this.x = x; this.y = y; this.width = width; this.height = height; this.score = score;
        }
    }

    class CropScore {
        float detail = 0f;
        float saturation = 0f;
        float skin = 0f;
        float total = 0f;
    }

    static public class Builder {

        private float mMinScale = 0.9f;
        private boolean mShouldOutputScoreMap = false;

        public Builder() {}

        public Builder setMinScale(float minScale) {
            this.mMinScale = minScale;
            return this;
        }

        public Builder shouldOutputScoreMap() {
            mShouldOutputScoreMap = true;
            return this;
        }

        public SmartCrop build() {
            return new SmartCrop(this);
        }

    }

    private SmartCrop(Builder builder) {
        mMinScale = builder.mMinScale;
        mShouldOutputScoreMap = builder.mShouldOutputScoreMap;
    }

    public CropResult crop(Frame frame, float aspect) {
        float rotatedAspect = aspect;
        if (frame.getOrientation().getDegree() % 180 != 0) {
            rotatedAspect = 1 / aspect;
        }

        Bitmap image = frame.getBitmap();
        int width = image.getWidth();
        int height = image.getHeight();

        Bitmap scoreMapBitmap = createScoreMap(image);
        SparseArray<RectF> cropRects = createCropRects(width, height, rotatedAspect);
        SparseArray<CropRegion> cropRegions = createCropRegions(cropRects, scoreMapBitmap);


        float topScore = -Float.MAX_VALUE;
        CropRegion topCrop = null;
        int numCrops = cropRegions.size();
        for (int i = 0; i < numCrops; i++) {
            CropRegion cropRegion = cropRegions.valueAt(i);
            if (cropRegion.score.total > topScore) {
                topCrop = cropRegion;
                topScore = cropRegion.score.total;
            }
        }

        // result
        CropResult cropResult = new CropResult();
        cropResult.topCrop = topCrop;
        cropResult.crops = cropRegions;

        if (mShouldOutputScoreMap) {
            cropResult.scoreMap = scoreMapBitmap;
            /*
            cropResult.scoreMap = Bitmap.createBitmap(
                    outputBuffer,
                    0,
                    width,
                    width,
                    height,
                    Bitmap.Config.ARGB_8888);
            */
        } else {
            scoreMapBitmap.recycle();
        }

        // alignment for our usage
        for (int i = 0; i < numCrops; i++) {
            CropRegion cropRegion = cropResult.crops.valueAt(i);
            cropRegion.x /= width;
            cropRegion.y /= height;
            cropRegion.width /= width;
            cropRegion.height /= height;
            rotateRegion(cropRegion, frame.getOrientation());
        }

        return cropResult;
    }

    private void rotateRegion(CropRegion cropRegion, Frame.Orientation orientation) {
        float fx = cropRegion.x;
        float fy = cropRegion.y;
        float fw = cropRegion.width;
        float fh = cropRegion.height;
        switch (orientation) {
            default:
            case DEGREE_0:
                break;
            case DEGREE_90:
                cropRegion.x = 1f - fy - fh;
                cropRegion.y = fx;
                cropRegion.width = fh;
                cropRegion.height = fw;
                break;
            case DEGREE_180:
                cropRegion.x = 1f - fx - fw;
                cropRegion.y = 1f - fy - fh;
                break;
            case DEGREE_270:
                cropRegion.x = fy;
                cropRegion.y = 1f - fx - fw;
                cropRegion.width = fh;
                cropRegion.height = fw;
                break;
        }
    }

    private Bitmap createScoreMap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // allocate buffer
        int[] inputBuffer = new int[width * height];
        int[] outputBuffer = new int[width * height];
        image.getPixels(inputBuffer, 0, width, 0, 0, width, height);
        image.getPixels(outputBuffer, 0, width, 0, 0, width, height);

        skinDetect(width, height, inputBuffer, outputBuffer);
        edgeDetect(width, height, inputBuffer, outputBuffer);
        saturationDetect(width, height, inputBuffer, outputBuffer);

        Bitmap outputImage = Bitmap.createBitmap(outputBuffer, 0, width, width, height, Bitmap.Config.ARGB_8888);
        int scaledWidth = (int)Math.ceil((double)width / mScoreDownSample);
        int scaledHeight = (int)Math.ceil((double)height / mScoreDownSample);
        Bitmap scoreImage = Bitmap.createScaledBitmap(outputImage, scaledWidth, scaledHeight, false);

        // release buffer
        outputImage.recycle();

        return scoreImage;
    }

    private SparseArray<CropRegion> createCropRegions(SparseArray<RectF> cropRects, Bitmap scoreMap) {
        int scoreMapWidth = scoreMap.getWidth();
        int scoreMapHeight = scoreMap.getHeight();
        int[] scoreBuffer = new int[scoreMapWidth * scoreMapHeight];
        scoreMap.getPixels(scoreBuffer, 0, scoreMapWidth, 0, 0, scoreMapWidth, scoreMapHeight);

        SparseArray<CropRegion> cropRegions = new SparseArray<>();
        int numRects = cropRects.size();
        for (int i = 0; i < numRects; i++) {
            RectF rect = cropRects.valueAt(i);
            CropScore score = calcScore(scoreMapWidth, scoreMapHeight, scoreBuffer, rect);
            CropRegion region = new CropRegion(
                    rect.left, rect.top, rect.width(), rect.height(), score);
            cropRegions.append(i, region);
        }

        return cropRegions;
    }

    private void skinDetect(int width, int height, int[] input, int[] output) {
        for (int y = 0; y < height; y++) {
            int p = width * y;
            for (int x = 0; x < width; x++) {
                int color = input[p];
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                float lightness = cie(r, g, b) / 255f;
                float skin = skinColor(r, g, b);
                int outValue = 0;
                if (skin > mSkinThreshold &&
                        lightness >= mSkinBrightnessMin &&
                        lightness <= mSkinBrightnessMax) {
                    outValue = Math.round((skin - mSkinThreshold) * (255f / (1f - mSkinThreshold)));
                }

                // set output
                if (outValue < 0) outValue = 0;
                if (outValue > 255) outValue = 255;
                int outColor = output[p];
                output[p++] = Color.argb(
                        Color.alpha(outColor),
                        outValue,
                        Color.green(outColor),
                        Color.blue(outColor));
            }
        }
    }

    private void edgeDetect(int width, int height, int[] input, int[] output) {
        for (int y = 0; y < height; y++) {
            int p = width * y;
            for (int x = 0; x < width; x++) {
                float lightness;
                if (x == 0 || x >= width - 1 || y == 0 || y >= height - 1) {
                    lightness = sample(input, p);
                } else {
                    lightness = sample(input, p) * 4
                            - sample(input, p - width)
                            - sample(input, p + width)
                            - sample(input, p - 1)
                            - sample(input, p + 1);
                }

                // set output
                int outValue = Math.round(lightness);
                if (outValue < 0) outValue = 0;
                if (outValue > 255) outValue = 255;
                int outColor = output[p];
                output[p++] = Color.argb(
                        Color.alpha(outColor),
                        Color.red(outColor),
                        outValue,
                        Color.blue(outColor));
            }
        }
    }

    private void saturationDetect(int width, int height, int[] input, int[] output) {
        for(int y = 0; y < height; y++) {
            int p = width * y;
            for(int x = 0; x < width; x++) {
                int color = input[p];
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                float lightness = cie(r, g, b) / 255f;
                float saturation = saturation(r, g, b);
                int outValue = 0;
                if (saturation > mSaturationThreshold &&
                        lightness >= mSaturationBrightnessMin &&
                        lightness <= mSaturationBrightnessMax) {
                    outValue = Math.round((saturation - mSaturationThreshold) *
                            (255f / (1f - mSaturationThreshold)));
                }

                // set output
                if (outValue < 0) outValue = 0;
                if (outValue > 255) outValue = 255;
                int outColor = output[p];
                output[p++] = Color.argb(
                        Color.alpha(outColor),
                        Color.red(outColor),
                        Color.green(outColor),
                        outValue);
            }
        }
    }

    private SparseArray<RectF> createCropRects(int width, int height, float aspect) {
        float imageAspect = (float)width / (float)height;
        float cropWidth = aspect <= imageAspect ? height * aspect : width;
        float cropHeight = aspect <= imageAspect ? height : width / aspect;

        SparseArray<RectF> crops = new SparseArray<>();
        int key = 0;
        for (float scale = mMaxScale; scale >= mMinScale; scale -= mScaleStep) {
            for (float y = 0f; y + cropHeight * scale <= height; y += mStep) {
                for (float x = 0f; x + cropWidth * scale <= width; x += mStep){
                    crops.append(key++,
                            new RectF(x, y, x + cropWidth * scale, y + cropHeight * scale));
                }
            }
        }

        return crops;
    }

    private CropScore calcScore(int width, int height, int[] scoreBuffer, RectF crop) {
        CropScore cropScore = new CropScore();
        int downSample = mScoreDownSample;

        for (int y = 0; y < height; y++) {
            int p = width * y;
            for (int x = 0; x < width; x++) {
                int color = scoreBuffer[p];
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                float importanceValue = importance(crop, x * downSample, y * downSample);
                float detail = g / 255f;
                cropScore.detail += detail * importanceValue;
                cropScore.skin += r / 255f * (detail + mSkinBias) * importanceValue;
                cropScore.saturation += b / 255f * (detail + mSaturationBias) * importanceValue;
                p++;
            }
        }
        cropScore.total = (cropScore.detail * mDetailWeight + cropScore.skin * mSkinWeight + cropScore.saturation * mSaturationWeight) / (crop.width() * crop.height());
        return cropScore;
    }

    private float importance(RectF crop, int x, int y) {
        if (x < crop.left || x >= crop.right || y < crop.top || y >= crop.bottom) {
            return mOutsideImportance;
        }

        float sx = (x - crop.left) / crop.width();
        float sy = (y - crop.top) / crop.height();
        float px = Math.abs(0.5f - sx) * 2;
        float py = Math.abs(0.5f - sy) * 2;
        // distance from edge
        float dx = Math.max(px - 1f + mEdgeRadius, 0);
        float dy = Math.max(py - 1f + mEdgeRadius, 0);
        float d = (dx * dx + dy * dy) * mEdgeWeight;
        float s = 1.41f - (float)Math.sqrt(px * px + py * py);
        if (mRuleOfThirds) {
            s += Math.max(0, s + d + 0.5f) * 1.2f * (thirds(px) + thirds(py));
        }
        return s + d;
    }

    // gets value in the range of [0, 1] where 0 is the center of the pictures
    // returns weight of rule of thirds [0, 1]
    private float thirds(float x) {
        //float xx = ((x - (1f / 3f) + 1f) % 2f * 0.5f - 0.5f) * 16f;
        float xx = ((x + (2f / 3f)) % 2f - 1f) * 8f; // opt of above
        return Math.max(1f - xx * xx, 0f);
    }

    private float skinColor(int r, int g, int b) {
        float mag = (float)Math.sqrt(r * r + g * g + b * b);
        float rd = (r / mag - mSkinColor[0]);
        float gd = (g / mag - mSkinColor[1]);
        float bd = (b / mag - mSkinColor[2]);
        float d = (float)Math.sqrt(rd * rd + gd * gd + bd * bd);
        return 1 - d;
    }

    private float cie(int r, int g, int b) {
        return 0.5126f * b + 0.7152f * g + 0.0722f * r;
    }

    private float sample(int[] id, int p) {
        int color = id[p];
        return cie(Color.red(color), Color.green(color), Color.blue(color));
    }

    private float saturation(int r, int g, int b){
        float maximum = Math.max(r / 255f, Math.max(g / 255f, b / 255f));
        float minimum = Math.min(r / 255f, Math.min(g / 255f, b / 255f));
        if (maximum == minimum) {
            return 0;
        }
        float l = (maximum + minimum) / 2f;
        float d = maximum - minimum;
        return l > 0.5 ? d / (2 - maximum - minimum) : d / (maximum + minimum);
    }

}
