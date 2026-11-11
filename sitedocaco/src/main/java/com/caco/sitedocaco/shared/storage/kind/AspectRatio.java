package com.caco.sitedocaco.shared.storage.kind;

public enum AspectRatio {

    SQUARE(1, 1),
    R_4_3(4, 3),
    R_3_2(3, 2),
    R_21_9(21, 9);

    private final int width;
    private final int height;

    AspectRatio(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() { return width; }

    public int height() { return height; }

    public double ratio() { return (double) width / height; }

    @Override
    public String toString() { return width + ":" + height; }
}
