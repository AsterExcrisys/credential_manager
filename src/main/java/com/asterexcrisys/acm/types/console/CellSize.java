package com.asterexcrisys.acm.types.console;

@SuppressWarnings("unused")
public enum CellSize {

    SMALL(5, 25),
    MEDIUM_SMALL(5, 50),
    MEDIUM(10, 50),
    MEDIUM_LARGE(10, 100),
    LARGE(20, 100),
    WRAP_SMALL(0, 25),
    WRAP_MEDIUM_SMALL(0, 50),
    WRAP_MEDIUM(0, 50),
    WRAP_MEDIUM_LARGE(0, 100),
    WRAP_LARGE(0, 100);

    private final int height;
    private final int width;

    CellSize(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public int height() {
        return height;
    }

    public int width() {
        return width;
    }

}