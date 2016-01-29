package com.kazeor.android.smartcrop;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.SparseArray;

public class SmartCrop {
    // detail
    private float detailWeight = 0.2f;

    // skin
    private float[] skinColor = {0.78f, 0.57f, 0.44f};
    private float skinBias = 0.01f;
    private float skinBrightnessMin = 0.2f;
    private float skinBrightnessMax = 1.0f;
    private float skinThreshold = 0.8f;
    private float skinWeight = 1.8f;

    // saturation
    private float saturationBrightnessMin = 0.05f;
    private float saturationBrightnessMax = 0.9f;
    private float saturationThreshold = 0.4f;
    private float saturationBias = 0.2f;
    private float saturationWeight = 0.3f;

    // step * minscale rounded down to the next power of two should be good
    private int scoreMapSize;
    private float step = 8f;
    private float scaleStep = 0.1f;
    private float minScale;
    private float maxScale = 1.0f;

    private float edgeRadius = 0.4f;
    private float edgeWeight = -20.0f;
    private float outsideImportance = -0.5f;
    private boolean ruleOfThirds = true;

    private boolean shouldOutputScoreMap;

    static public class Builder {

        private float minScale;
        private int scoreMapSize;
        private boolean shouldOutputScoreMap;

        public Builder() {
            minScale = 0.9f;
            scoreMapSize = 160 * 120;
            shouldOutputScoreMap = false;
        }

        public Builder minScale(float minScale) {
            this.minScale = minScale;
            return this;
        }

        public Builder scoreMapSize(int size) {
            scoreMapSize = size;
            return this;
        }

        public Builder shouldOutputScoreMap() {
            shouldOutputScoreMap = true;
            return this;
        }

        public SmartCrop build() {
            return new SmartCrop(this);
        }

    }

    private SmartCrop(Builder builder) {
        this.minScale = builder.minScale;
        this.scoreMapSize = builder.scoreMapSize;
        this.shouldOutputScoreMap = builder.shouldOutputScoreMap;
    }

    public CropResult crop(Frame frame, float aspect) {
        float rotatedAspect = aspect;
        if (frame.orientation().getDegree() % 180 != 0) {
            rotatedAspect = 1 / aspect;
        }

        Bitmap image = frame.bitmap();
        int width = image.getWidth();
        int height = image.getHeight();

        Bitmap scoreMapBitmap;
        if (frame.hasScoreMap()) {
            scoreMapBitmap = frame.scoreMap();
        } else {
            scoreMapBitmap = createScoreMap(image);
        }

        SparseArray<RectF> cropRects = createCropRects(width, height, rotatedAspect);
        SparseArray<CropRegion> cropRegions = createCropRegions(cropRects, scoreMapBitmap);

        float topScore = -Float.MAX_VALUE;
        CropRegion topCrop = null;
        int numCrops = cropRegions.size();
        for (int i = 0; i < numCrops; i++) {
            CropRegion cropRegion = cropRegions.valueAt(i);
            float score = cropRegion.score();
            if (score > topScore) {
                topCrop = cropRegion;
                topScore = score;
            }
        }

        // result
        CropResult cropResult;
        if (shouldOutputScoreMap) {
            cropResult = new CropResult(topCrop, cropRegions, scoreMapBitmap);
        } else {
            cropResult = new CropResult(topCrop, cropRegions, null);
            scoreMapBitmap.recycle();
        }

        // rotate result with aspect
        for (int i = 0; i < numCrops; i++) {
            CropRegion cropRegion = cropResult.crops().valueAt(i);
            cropRegion.rotate(frame.orientation().getDegree());
        }

        return cropResult;
    }

    private Bitmap createScoreMap(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int area = width * height;

        // allocate buffer
        int[] inputBuffer = new int[area];
        int[] outputBuffer = new int[area];
        image.getPixels(inputBuffer, 0, width, 0, 0, width, height);
        image.getPixels(outputBuffer, 0, width, 0, 0, width, height);

        skinDetect(width, height, inputBuffer, outputBuffer);
        edgeDetect(width, height, inputBuffer, outputBuffer);
        saturationDetect(width, height, inputBuffer, outputBuffer);

        Bitmap outputImage = Bitmap.createBitmap(outputBuffer, 0, width, width, height, Bitmap.Config.ARGB_8888);
        float scale = (float)Math.sqrt((double) scoreMapSize / (double)area);
        int scaledWidth = Math.round(width * scale);
        int scaledHeight = Math.round(height * scale);
        Bitmap scoreImage = Bitmap.createScaledBitmap(outputImage, scaledWidth, scaledHeight, false);

        // release buffer
        if (outputImage != scoreImage) {
            outputImage.recycle();
        }

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
            float score = calcScore(scoreMapWidth, scoreMapHeight, scoreBuffer, rect);
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
                if (skin > skinThreshold &&
                        lightness >= skinBrightnessMin &&
                        lightness <= skinBrightnessMax) {
                    outValue = Math.round((skin - skinThreshold) * (255f / (1f - skinThreshold)));
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
                if (saturation > saturationThreshold &&
                        lightness >= saturationBrightnessMin &&
                        lightness <= saturationBrightnessMax) {
                    outValue = Math.round((saturation - saturationThreshold) *
                            (255f / (1f - saturationThreshold)));
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
        float cropWidth = aspect <= imageAspect ? aspect / imageAspect : 1f;
        float cropHeight = aspect <= imageAspect ? 1f : imageAspect / aspect;

        SparseArray<RectF> crops = new SparseArray<>();
        int key = 0;
        float stepWidth = step / width;
        float stepHeight = step / height;
        for (float scale = maxScale; scale >= minScale; scale -= scaleStep) {
            float scaledCropWidth = cropWidth * scale;
            float scaledCropHeight = cropHeight * scale;
            for (float y = 0f; y + scaledCropHeight <= 1f; y += stepHeight) {
                for (float x = 0f; x + scaledCropWidth <= 1f; x += stepWidth){
                    crops.append(key++,
                            new RectF(x, y, x + scaledCropWidth, y + scaledCropHeight));
                }
            }
        }

        return crops;
    }

    private float calcScore(int width, int height, int[] scoreBuffer, RectF crop) {
        float detailScore = 0f;
        float skinScore = 0f;
        float saturationScore = 0f;
        float relStepX = 1f / width;
        float relStepY = 1f / height;
        for (int y = 0; y < height; y++) {
            int p = width * y;
            float relY = relStepY * y;
            for (int x = 0; x < width; x++) {
                float relX = relStepX * x;
                int color = scoreBuffer[p];
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                float importanceValue = importance(crop, relX, relY);
                float detail = g / 255f;
                detailScore += detail * importanceValue;
                skinScore += r / 255f * (detail + skinBias) * importanceValue;
                saturationScore += b / 255f * (detail + saturationBias) * importanceValue;
                p++;
            }
        }

        float totalScore =(detailScore * detailWeight + skinScore * skinWeight + saturationScore * saturationWeight);
        return totalScore / (crop.width() * crop.height());
    }

    private float importance(RectF crop, float x, float y) {
        if (x < crop.left || x >= crop.right || y < crop.top || y >= crop.bottom) {
            return outsideImportance;
        }

        float sx = (x - crop.left) / crop.width();
        float sy = (y - crop.top) / crop.height();
        float px = Math.abs(0.5f - sx) * 2;
        float py = Math.abs(0.5f - sy) * 2;
        // distance from edge
        float dx = Math.max(px - 1f + edgeRadius, 0);
        float dy = Math.max(py - 1f + edgeRadius, 0);
        float d = (dx * dx + dy * dy) * edgeWeight;
        float s = 1.41f - (float)Math.sqrt(px * px + py * py);
        if (ruleOfThirds) {
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
        float rd = (r / mag - skinColor[0]);
        float gd = (g / mag - skinColor[1]);
        float bd = (b / mag - skinColor[2]);
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
