package com.canvearth.canvearth;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.server.FBPixel;
import com.canvearth.canvearth.server.PixelDataManager;
import com.canvearth.canvearth.utils.Configs;
import com.canvearth.canvearth.utils.Constants;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Random;


@RunWith(AndroidJUnit4.class)
public class ServerInterfaceTest {

    private ArrayList<Pixel> makeSamplePixels(Pixel startPixel, int numX, int numY) {
        ArrayList<Pixel> pixels = new ArrayList<>();
        for (int x = 0; x < numX; x++) {
            for (int y = 0; y < numY; y++) {
                Pixel nearbyPixelCood
                        = new Pixel(startPixel.x + x, startPixel.y + y, startPixel.zoom);
                pixels.add(nearbyPixelCood);
            }
        }
        return pixels;
    }

    @Before
    public void setup() {
        Configs.TESTING = true;
    }

    @Test
    public void leafPixelReadTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<Pixel> samePixels
                = makeSamplePixels(new Pixel(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL), 20, 20);
        // You have to watch pixel first..
        for (Pixel pixel : samePixels) {
            pixelDataManager.watchPixel(pixel);
        }
        // Get a random pixel info
        Random random = new Random();
        Pixel randomPixel = samePixels.get(random.nextInt(20 * 20));
        FBPixel pixelInfo = pixelDataManager.readPixel(randomPixel);
        // You have to unwatch pixel
        for (Pixel pixel : samePixels) {
            pixelDataManager.unwatchPixel(pixel);
        }
    }

    @Test
    public void leafPixelWriteTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<Pixel> samePixels
                = makeSamplePixels(new Pixel(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL), 20, 20);
        // You have to watch pixel first..
        for (Pixel pixel : samePixels) {
            pixelDataManager.watchPixel(pixel);
        }
        // Write black color to the random pixel
        Random random = new Random();
        Pixel randomPixel = samePixels.get(random.nextInt(20 * 20));
        pixelDataManager.writePixel(randomPixel, new Color(0L, 0L, 0L), () -> {
            Log.d("leafPixelWriteTest", "Succeed");
        });
        // You have to unwatch pixel
        for (Pixel pixel : samePixels) {
            pixelDataManager.unwatchPixel(pixel);
        }
    }

    @Test
    public void leafPixelWriteReadTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<Pixel> samePixels
                = makeSamplePixels(new Pixel(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL), 20, 20);
        // You have to watch pixel first..
        for (Pixel pixel : samePixels) {
            pixelDataManager.watchPixel(pixel);
        }
        // Write black color to the random pixel
        Random random = new Random();
        Pixel randomPixel = samePixels.get(random.nextInt(20 * 20));
        Color black = new Color(0L, 0L, 0L);
        pixelDataManager.writePixel(randomPixel, black, () -> {
            Log.d("leafPixelWriteTest", "Succeed");
        });
        // Read same pixel
        FBPixel pixelInfo = pixelDataManager.readPixel(randomPixel);
        assert (pixelInfo.color.equals(black));
        // You have to unwatch pixel
        for (Pixel pixel : samePixels) {
            pixelDataManager.unwatchPixel(pixel);
        }
    }
}
