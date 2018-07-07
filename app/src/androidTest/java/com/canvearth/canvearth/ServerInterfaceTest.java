package com.canvearth.canvearth;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.canvearth.canvearth.pixel.Color;
import com.canvearth.canvearth.pixel.PixelCoord;
import com.canvearth.canvearth.server.Pixel4Firebase;
import com.canvearth.canvearth.server.PixelDataManager;
import com.canvearth.canvearth.utils.BitmapUtils;
import com.canvearth.canvearth.utils.Configs;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.MathUtils;

import org.junit.Assert;
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
        pixelDataManager.watchPixels(samePixelCoords);
        // Get a random pixel info
        Random random = new Random();
        PixelCoord randomPixelCoord = samePixelCoords.get(random.nextInt(20 * 20));
        Pixel4Firebase pixelInfo = pixelDataManager.readPixel(randomPixelCoord);
        // You have to unwatch pixel
        pixelDataManager.unwatchPixels(samePixelCoords);
    }

    @Test
    public void leafPixelWriteTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<PixelCoord> samePixelCoords
                = makeSamplePixelCoords(new PixelCoord(0, 0, Constants.LEAF_PIXEL_LEVEL), 20, 20);
        // You have to watch pixel first..
        pixelDataManager.watchPixels(samePixelCoords);
        // Write black color to the random pixel
        Random random = new Random();
        PixelCoord randomPixelCoord = samePixelCoords.get(random.nextInt(20 * 20));
        pixelDataManager.writePixelAsync(randomPixelCoord, new Color(0L, 0L, 0L),()->{
            Log.d("leafPixelWriteTest", "Succeed");
        });
        // You have to unwatch pixel
        pixelDataManager.unwatchPixels(samePixelCoords);
    }

    @Test
    public void leafPixelWriteReadTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<PixelCoord> samePixelCoords
                = makeSamplePixelCoords(new PixelCoord(0, 0, Constants.LEAF_PIXEL_LEVEL), 20, 20);
        // You have to watch pixel first..
        pixelDataManager.watchPixels(samePixelCoords);
        // Write black color to the random pixel
        Random random = new Random();
        PixelCoord randomPixelCoord = samePixelCoords.get(random.nextInt(20 * 20));
        Color red = new Color(255L, 0L, 0L);
        pixelDataManager.writePixelSync(randomPixelCoord, red);
        // Read same pixel
        Pixel4Firebase pixelInfo = pixelDataManager.readPixel(randomPixelCoord);
        Assert.assertTrue(pixelInfo.color.equals(red));
        // You have to unwatch pixel
        pixelDataManager.unwatchPixels(samePixelCoords);
    }

    @Test
    public void bitmapReadTest() {
        PixelDataManager pixelDataManager = PixelDataManager.getInstance();
        ArrayList<PixelCoord> samePixelCoords
                = makeSamplePixelCoords(new PixelCoord(0, 0, Constants.LEAF_PIXEL_LEVEL), 8, 8);
        // You have to watch pixel first..
        pixelDataManager.watchPixels(samePixelCoords);
        Color green = new Color(0L, 255L, 0L);
        for (PixelCoord pixelCoord : samePixelCoords) {
            pixelDataManager.writePixelSync(pixelCoord, green);
        }
        PixelCoord zoomedOutPixelCoord = new PixelCoord(0, 0, Constants.LEAF_PIXEL_LEVEL - 3);
        Bitmap bitmap = pixelDataManager.getBitmapSync(zoomedOutPixelCoord, 5);
        int expectedSide = MathUtils.intPow(2, 5);
        Assert.assertEquals(bitmap.getWidth(), expectedSide);
        Assert.assertEquals(bitmap.getHeight(), expectedSide);
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                Assert.assertEquals(bitmap.getPixel(x, y), BitmapUtils.intColor(green));
            }
        }
        // You have to unwatch pixel
        pixelDataManager.unwatchPixels(samePixelCoords);
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
