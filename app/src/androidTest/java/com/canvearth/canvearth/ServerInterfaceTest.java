package com.canvearth.canvearth;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.PixelCoord;
import com.canvearth.canvearth.server.Pixel4Firebase;
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

    @Before
    public void setup() {
        Configs.TESTING = true;
    }

    @Test
    public void leafPixelReadTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<PixelCoord> samePixelCoords
                = makeSamplePixelCoords(new PixelCoord(0, 0, Constants.LEAF_PIXEL_LEVEL), 20, 20);
        // You have to watch pixel first..
        for (PixelCoord pixelCoord : samePixelCoords) {
            pixelDataManager.watchPixel(pixelCoord);
        }
        // Get a random pixel info
        Random random = new Random();
        PixelCoord randomPixelCoord = samePixelCoords.get(random.nextInt(20 * 20));
        Pixel4Firebase pixelInfo = pixelDataManager.readPixel(randomPixelCoord);
        // You have to unwatch pixel
        for (PixelCoord pixelCoord : samePixelCoords) {
            pixelDataManager.unwatchPixel(pixelCoord);
        }
    }

    @Test
    public void leafPixelWriteTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<PixelCoord> samePixelCoords
                = makeSamplePixelCoords(new PixelCoord(0, 0, Constants.LEAF_PIXEL_LEVEL), 20, 20);
        // You have to watch pixel first..
        for (PixelCoord pixelCoord : samePixelCoords) {
            pixelDataManager.watchPixel(pixelCoord);
        }
        // Write black color to the random pixel
        Random random = new Random();
        PixelCoord randomPixelCoord = samePixelCoords.get(random.nextInt(20 * 20));
        pixelDataManager.writePixelAsync(randomPixelCoord, new Color(0L, 0L, 0L),()->{
            Log.d("leafPixelWriteTest", "Succeed");
        });
        // You have to unwatch pixel
        for (PixelCoord pixelCoord : samePixelCoords) {
            pixelDataManager.unwatchPixel(pixelCoord);
        }
    }

    @Test
    public void leafPixelWriteReadTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<PixelCoord> samePixelCoords
                = makeSamplePixelCoords(new PixelCoord(0, 0, Constants.LEAF_PIXEL_LEVEL), 20, 20);
        // You have to watch pixel first..
        for (PixelCoord pixelCoord : samePixelCoords) {
            pixelDataManager.watchPixel(pixelCoord);
        }
        // Write black color to the random pixel
        Random random = new Random();
        PixelCoord randomPixelCoord = samePixelCoords.get(random.nextInt(20 * 20));
        Color black = new Color(0L, 0L, 0L);
        pixelDataManager.writePixelAsync(randomPixelCoord, black,()->{
            Log.d("leafPixelWriteTest", "Succeed");
        });
        // Read same pixel
        Pixel4Firebase pixelInfo = pixelDataManager.readPixel(randomPixelCoord);
        assert(pixelInfo.color.equals(black));
        // You have to unwatch pixel
        for (PixelCoord pixelCoord : samePixelCoords) {
            pixelDataManager.unwatchPixel(pixelCoord);
        }
    }

    private ArrayList<PixelCoord> makeSamplePixelCoords(PixelCoord startPixelCoord, int numX, int numY) {
        ArrayList<PixelCoord> pixelCoords = new ArrayList<>();
        for (int x = 0; x < numX; x++) {
            for (int y = 0; y < numY; y++) {
                PixelCoord nearbyPixelCood
                        = new PixelCoord(startPixelCoord.x + x, startPixelCoord.y + y, startPixelCoord.zoom);
                pixelCoords.add(nearbyPixelCood);
            }
        }
        return pixelCoords;
    }
}
