package com.canvearth.canvearth;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.PixelData;
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

    private ArrayList<PixelData> makeSamplePixelData(PixelData startPixelData, int numX, int numY) {
        ArrayList<PixelData> pixelData = new ArrayList<>();
        for (int x = 0; x < numX; x++) {
            for (int y = 0; y < numY; y++) {
                PixelData nearbyPixelData = new PixelData(
                        startPixelData.x + x,
                        startPixelData.y + y,
                        startPixelData.zoom);
                pixelData.add(nearbyPixelData);
            }
        }
        return pixelData;
    }

    @Before
    public void setup() {
        Configs.TESTING = true;
    }

    @Test
    public void leafPixelReadTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<PixelData> samePixelData = makeSamplePixelData(
                new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL),
                20,
                20);
        // You have to watch pixel first..
        for (PixelData pixelData : samePixelData) {
            pixelDataManager.watchPixel(pixelData);
        }
        // Get a random pixel info
        Random random = new Random();
        PixelData randomPixelData = samePixelData.get(random.nextInt(20 * 20));
        FBPixel pixelInfo = pixelDataManager.readPixel(randomPixelData);
        // You have to unwatch pixel
        for (PixelData pixelData : samePixelData) {
            pixelDataManager.unwatchPixel(pixelData);
        }
    }

    @Test
    public void leafPixelWriteTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<PixelData> samePixelData = makeSamplePixelData(new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL),
                20,
                20);

        // You have to watch pixel first..
        for (PixelData pixelData : samePixelData) {
            pixelDataManager.watchPixel(pixelData);
        }

        // Write black color to the random pixel
        Random random = new Random();
        PixelData randomPixelData = samePixelData.get(random.nextInt(20 * 20));
        pixelDataManager.writePixel(randomPixelData, new Color(0L, 0L, 0L), () -> {
            Log.d("leafPixelWriteTest", "Succeed");
        });

        // You have to unwatch pixel
        for (PixelData pixelData : samePixelData) {
            pixelDataManager.unwatchPixel(pixelData);
        }
    }

    @Test
    public void leafPixelWriteReadTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<PixelData> samePixelData = makeSamplePixelData(new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL),
                20,
                20);
        // You have to watch pixel first..
        for (PixelData pixelData : samePixelData) {
            pixelDataManager.watchPixel(pixelData);
        }

        // Write black color to the random pixel
        Random random = new Random();
        PixelData randomPixelData = samePixelData.get(random.nextInt(20 * 20));
        Color black = new Color(0L, 0L, 0L);
        pixelDataManager.writePixel(randomPixelData, black, () -> {
            Log.d("leafPixelWriteTest", "Succeed");
        });

        // Read same pixel
        FBPixel pixelInfo = pixelDataManager.readPixel(randomPixelData);
        assert (pixelInfo.color.equals(black));
        // You have to unwatch pixel
        for (PixelData pixelData : samePixelData) {
            pixelDataManager.unwatchPixel(pixelData);
        }
    }
}
