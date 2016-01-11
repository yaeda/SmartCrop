package com.kazeor.android.smartcrop.sample;

import com.kazeor.android.smartcrop.CropResult;
import com.kazeor.android.smartcrop.Frame;

public class CropInfo {
    public enum CROP_ASPECT {
        SQUARE,
        LANDSCAPE,
        PORTRAIT
    }

    public CropResult cropResultSquare;
    public CropResult cropResultLandscape;
    public CropResult cropResultPortrait;
    public Frame.Orientation orientation;
    public long mediaId;
}
