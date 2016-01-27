package com.kazeor.android.smartcrop;

public class CropRegion {
    private float x;
    private float y;
    private float width;
    private float height;
    private float score;

    public CropRegion(float x, float y, float width, float height, float score) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.score = score;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public float score() {
        return score;
    }

    protected void rotate(int degree) {
        if (degree % 90 != 0) {
            return;
        }

        float fx = x;
        float fy = y;
        float fw = width;
        float fh = height;
        switch (degree) {
            default:
            case 0:
                break;
            case 90:
                x = 1f - fy - fh;
                y = fx;
                width = fh;
                height = fw;
                break;
            case 180:
                x = 1f - fx - fw;
                y = 1f - fy - fh;
                break;
            case 270:
                x = fy;
                y = 1f - fx - fw;
                width = fh;
                height = fw;
                break;
        }
    }
}
