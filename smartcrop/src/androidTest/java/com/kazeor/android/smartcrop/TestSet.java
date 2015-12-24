package com.kazeor.android.smartcrop;

/**
 * Created by 22715434 on 12/24/15.
 */
public class TestSet {
    public String filename;
    public Region region;

    public class Region {
        public float x;
        public float y;
        public float width;
        public float height;

        public boolean isInsideOf(SmartCrop.CropRegion region) {
            boolean testStartX = region.x <= this.x;
            boolean testStartY = region.y <= this.y;
            boolean testEndX = region.x + region.width >= this.x + this.width;
            boolean testEndY = region.y + region.height >= this.y + this.height;
            return testStartX && testStartY && testEndX && testEndY;
        }
    }
}
