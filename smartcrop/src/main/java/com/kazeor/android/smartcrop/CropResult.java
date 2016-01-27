package com.kazeor.android.smartcrop;

import android.graphics.Bitmap;
import android.util.SparseArray;

public class CropResult {
    private CropRegion topCrop = null;
    private SparseArray<CropRegion> crops = null;
    private Bitmap scoreMap = null;

    public CropResult(CropRegion topCrop, SparseArray<CropRegion> crops, Bitmap scoreMap) {
        this.topCrop = topCrop;
        this.crops = crops;
        this.scoreMap = scoreMap;
    }

    public CropRegion topCrop() {
        return topCrop;
    }

    public SparseArray<CropRegion> crops() {
        return crops;
    }

    public Bitmap scoreMap() {
        return scoreMap;
    }
}

