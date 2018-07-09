package com.canvearth.canvearth;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.canvearth.canvearth.pixel.PixelColor;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.server.FBPixel;
import com.canvearth.canvearth.server.FBPixelManager;
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
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData = makeSamplePixelData(
                new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL),
                20,
                20);
        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        // Get a random pixel info
        Random random = new Random();
        PixelData randomPixelData = samePixelData.get(random.nextInt(20 * 20));
        FBPixel pixelInfo = fBPixelManager.readPixel(randomPixelData);
        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }

    @Test
    public void leafPixelWriteTest() {
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData = makeSamplePixelData(new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL),
                20,
                20);

        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        // Write black pixelColor to the random pixel
        Random random = new Random();
        PixelData randomPixelData = samePixelData.get(random.nextInt(20 * 20));
        fBPixelManager.writePixelAsync(randomPixelData, new PixelColor(0L, 0L, 0L), () -> {
            Log.d("leafPixelWriteTest", "Succeed");
        });

        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }

    @Test
    public void leafPixelWriteReadTest() {
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData = makeSamplePixelData(new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL),
                20,
                20);
        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        // Write black pixelColor to the random pixel
        Random random = new Random();
        PixelData randomPixelData = samePixelData.get(random.nextInt(20 * 20));
        PixelColor red = new PixelColor(255L, 0L, 0L);
        fBPixelManager.writePixelSync(randomPixelData, red);
        // Read same pixel
        FBPixel pixelInfo = fBPixelManager.readPixel(randomPixelData);
        Assert.assertTrue(pixelInfo.pixelColor.equals(red));
        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }

    @Test
    public void bitmapReadTest() {
        FBPixelManager fBPixelManager = FBPixelManager.getInstance();
        ArrayList<PixelData> samePixelData
                = makeSamplePixelData(new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL), 8, 8);
        // You have to watch pixel first..
        fBPixelManager.watchPixels(samePixelData);
        PixelColor green = new PixelColor(0L, 255L, 0L);
        for (PixelData pixelData : samePixelData) {
            fBPixelManager.writePixelSync(pixelData, green);
        }
        PixelData zoomedOutPixelData = new PixelData(0, 0, Constants.LEAF_PIXEL_ZOOM_LEVEL - 3);
        Bitmap bitmap = fBPixelManager.getBitmapSync(zoomedOutPixelData, 5);
        int expectedSide = MathUtils.intPow(2, 5);
        Assert.assertEquals(bitmap.getWidth(), expectedSide);
        Assert.assertEquals(bitmap.getHeight(), expectedSide);
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                Assert.assertEquals(bitmap.getPixel(x, y), BitmapUtils.intColor(green));
            }
        }
        // You have to unwatch pixel
        fBPixelManager.unwatchPixels(samePixelData);
    }

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
}
