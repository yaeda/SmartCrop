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

        private Bitmap bitmap;

        private Orientation orientation;

        private Bitmap scoreMap;

        public Builder() {
            bitmap = null;
            orientation = Orientation.DEGREE_0;
            scoreMap = null;
        }

        public Builder(Frame frame) {
            this.bitmap = frame.bitmap;
            this.orientation = frame.orientation;
            this.scoreMap = frame.scoreMap;
        }

        public Builder bitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            return this;
        }

        public Builder orientation(Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder scoreMap(Bitmap scoreMap) {
            this.scoreMap = scoreMap;
            return this;
        }

        public Frame build() {
            return new Frame(this);
        }

    }

    private Bitmap bitmap;

    private Orientation orientation;

    private Bitmap scoreMap;

    private Frame(Builder builder) {
        this.bitmap = builder.bitmap;
        this.orientation = builder.orientation;
        this.scoreMap = builder.scoreMap;
    }

    public Bitmap bitmap() {
        return bitmap;
    }

    public Orientation orientation() {
        return orientation;
    }

    public boolean hasScoreMap() {
        return scoreMap != null;
    }

    public Bitmap scoreMap() {
        return scoreMap;
    }

}
