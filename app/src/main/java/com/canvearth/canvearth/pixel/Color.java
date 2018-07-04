package com.canvearth.canvearth.pixel;

public class Color {
    public Long r;
    public Long g;
    public Long b;
    public Long a;

    public Color() {
        // Default constructor required for Firebase db
    }

    public Color(Long r, Long g, Long b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1L;
    }

    public static Color colorCompose(Color color1, Color color2) {
        Color newColor = new Color();
        newColor.r = (color1.r + color2.r) / 2;
        newColor.g = (color1.g + color2.g) / 2;
        newColor.b = (color1.b + color2.b) / 2;
        newColor.a = (color1.a + color2.a) / 2;
        return newColor;
    }
}
