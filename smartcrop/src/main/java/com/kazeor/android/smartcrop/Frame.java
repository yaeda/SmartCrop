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

        private Bitmap mBitmap;

        private Orientation mOrientation;

        private Bitmap mScoreMap;

        public Builder() {
            mBitmap = null;
            mOrientation = Orientation.DEGREE_0;
            mScoreMap = null;
        }

        public Builder(Frame frame) {
            this.mBitmap = frame.mBitmap;
            this.mOrientation = frame.mOrientation;
            this.mScoreMap = frame.mScoreMap;
        }

        public Builder setBitmap(Bitmap bitmap) {
            mBitmap = bitmap;
            return this;
        }

        public Builder setOrientation(Orientation orientation) {
            mOrientation = orientation;
            return this;
        }

        public Builder setScoreMap(Bitmap scoreMap) {
            mScoreMap = scoreMap;
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
        this.mBitmap = builder.mBitmap;
        this.mOrientation = builder.mOrientation;
        this.mScoreMap = builder.mScoreMap;
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
