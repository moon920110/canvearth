package com.canvearth.canvearth.client;

import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.server.FBPixel;

public class PixelEvents {
    // When the first time to call FBPixelManager.getInstance().watchPixel
    // or watching pixel data are changed, this function will be called.
    public static void watchingPixelChanged(PixelData pixelData, FBPixel FBPixel) {
        GridManager.changePixelColor(pixelData.firebaseId, FBPixel.pixelColor.getIntColor());
    }
}
