package com.canvearth.canvearth.server;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.canvearth.canvearth.pixel.PixelColor;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.MathUtils;
import com.canvearth.canvearth.utils.concurrency.Function;

import java.util.List;

public class MockFBPixelManager {
    private static final MockFBPixelManager ourInstance = new MockFBPixelManager();

    public static MockFBPixelManager getInstance() {
        return ourInstance;
    }


    private MockFBPixelManager() {
    }

    public void watchPixel(final PixelData pixelData) {
        return;
    }

    public void watchPixels(List<PixelData> pixelDataList) {
        return;
    }

    public void unwatchPixel(PixelData pixelData) {
        return;
    }

    public FBPixel readPixel(PixelData pixelData) {
        return new FBPixel(new PixelColor(Constants.PALETTE_DEFAULT_COLOR));
    }

    public void unwatchPixels(List<PixelData> pixelDataList) {
        return;
    }

    // prefer use this rather than getBitmapAsync
    public void getCachedBitmapAsync(PixelData pixelData, Function<Bitmap> callback) {
        final Bitmap bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
        callback.run(bitmap);
    }

    public void getBitmapAsync(PixelData pixelData, int resolutionFactor, Function<Bitmap> callback) {
        callback.run(getBitmapSync(pixelData, resolutionFactor));
    }

    // You don't have to watch this pixel (for now).. I'm nervous about performance issue of this method.
    // returns Bitmap which has resolution of 2^resolutionFactor * 2^resolutionFactor
    // TODO this seems better to executed in server side.
    public Bitmap getBitmapSync(PixelData pixelData, int resolutionFactor) {
        int resolution = MathUtils.intPow(2, resolutionFactor);
        final Bitmap bitmap = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    public void writePixelAsync(PixelData pixelData, PixelColor pixelColor, @Nullable Function<PixelData> callback) {
        callback.run(pixelData);
    }

    // Please prefer writePixelAsync, for performance.
    // returns last update pixel data.
    public PixelData writePixelSync(PixelData pixelData, PixelColor pixelColor) {
        return pixelData;
    }

}
