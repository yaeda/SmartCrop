package com.kazeor.android.smartcrop;

import android.graphics.Bitmap;

public class Frame {

    public enum Orientation {
        DEGREE_0(0),
        DEGREE_90(90),
        DEGREE_180(180),
        DEGREE_270(270);

        private final int degree;
        Orientation(final int degree) {
            this.degree = degree;
        }
        public int getDegree() {
            return this.degree;
        }
    }

    public static class Builder {

        private Bitmap mBitmap = null;

        private Orientation mOrientation = Orientation.DEGREE_0;

        public Builder() {}

        public Builder setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
            return this;
        }

        public Builder setOrientation(Orientation orientation) {
            mOrientation = orientation;
            return this;
        }

        public Frame build() {
            return new Frame(this);
        }

    }

    private Bitmap mBitmap;

    private Orientation mOrientation;

    private Bitmap mScoreMap;

    private Frame(Builder builder) {
        mBitmap = builder.mBitmap;
        mOrientation = builder.mOrientation;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public Orientation getOrientation() {
        return mOrientation;
    }

    public boolean hasScoreMap() {
        return mScoreMap != null;
    }

    public Bitmap getScoreMap() {
        return mScoreMap;
    }

}
