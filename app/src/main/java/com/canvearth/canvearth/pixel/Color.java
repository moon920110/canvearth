package com.canvearth.canvearth.pixel;

public class Color {
    public Long r;
    public Long g;
    public Long b;

    public Color() {
        // Default constructor required for Firebase db
    }

    public Color(Long r, Long g, Long b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
