package com.kazeor.android.smartcrop;

public class TestSet {
    public String filename;
    public Region region;

    public class Region {
        public float x;
        public float y;
        public float width;
        public float height;

        public boolean isInsideOf(CropRegion region) {
            boolean testStartX = region.getX() <= this.x;
            boolean testStartY = region.getY() <= this.y;
            boolean testEndX = region.getX() + region.getWidth() >= this.x + this.width;
            boolean testEndY = region.getY() + region.getHeight() >= this.y + this.height;
            return testStartX && testStartY && testEndX && testEndY;
        }
    }
}
