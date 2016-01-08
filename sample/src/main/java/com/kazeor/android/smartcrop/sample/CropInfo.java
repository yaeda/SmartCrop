package com.kazeor.android.smartcrop.sample;

import com.kazeor.android.smartcrop.Frame;
import com.kazeor.android.smartcrop.SmartCrop;

public class CropInfo {
    public enum CROP_ASPECT {
        SQUARE,
        LANDSCAPE,
        PORTRAIT
    }

    public SmartCrop.CropResult cropResultSquare;
    public SmartCrop.CropResult cropResultLandscape;
    public SmartCrop.CropResult cropResultPortrait;
    public Frame.Orientation orientation;
    public long mediaId;
}
