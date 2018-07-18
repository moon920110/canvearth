package com.canvearth.canvearth;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.canvearth.canvearth.pixel.PixelColor;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.server.FBPixelManager;
import com.canvearth.canvearth.utils.Configs;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.DatabaseUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.TimeUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
public class ServerPerformanceTest {

    @BeforeClass
    public static void setup() {
        Configs.TESTING = true;
    }

    @AfterClass
    public static void tearDown() {
        DatabaseUtils.clearDev();
    }

    @Test
    public void pixelWritePerformanceTest() {
        try {
            final String TAG = "ServerPerformanceTest/pixelWritePerformanceTest";
            FBPixelManager fBPixelManager = FBPixelManager.getInstance();
            ArrayList<PixelData> samePixelData = PixelUtils.makeBatchPixelData(
                    new PixelData(0, 0, Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL), 5, 5);

            // You have to watch pixel first..
            fBPixelManager.watchPixels(samePixelData);
            // Write black color to the random pixel
            long startTime = System.nanoTime();
            for (PixelData pixelData : samePixelData) {
                fBPixelManager.writePixelAsync(pixelData, new PixelColor(0L, 0L, 0L)).join();
            }
            long endTime = System.nanoTime();
            long elapsedTime = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            float averageElapsedTime = elapsedTime / samePixelData.size();
            Log.i(TAG, "Average Elapsed Time: " + averageElapsedTime);
            // You have to unwatch pixel
            fBPixelManager.unwatchPixels(samePixelData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void bitmapReadPerformanceTest() {
        try {
            final String TAG = "ServerPerformanceTest/bitmapReadPerformanceTest";
            FBPixelManager fBPixelManager = FBPixelManager.getInstance();
            ArrayList<PixelData> samePixelData = PixelUtils.makeBatchPixelData(
                    new PixelData(0, 0, Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL), 5, 5);

            // You have to watch pixel first..
            fBPixelManager.watchPixels(samePixelData);
            PixelColor green = new PixelColor(0L, 255L, 0L);
            for (PixelData pixelData : samePixelData) {
                fBPixelManager.writePixelAsync(pixelData, green).join();
            }
            PixelData zoomedOutPixelData8x8 = new PixelData(0, 0, Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL - 3);
            long elapsedTime8x8 = TimeUtils.measureTimeMillis((Object object) -> {
                fBPixelManager.getBitmapSync(zoomedOutPixelData8x8, 3);
            });
            Log.i(TAG, "Elapsed Time For getting 8x8 bitmap: " + elapsedTime8x8 + "ms");
            PixelData zoomedOutPixelData16x16 = new PixelData(0, 0, Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL - 4);
            long elapsedTime16x16 = TimeUtils.measureTimeMillis((Object object) -> {
                fBPixelManager.getBitmapSync(zoomedOutPixelData16x16, 4);
            });
            Log.i(TAG, "Elapsed Time For getting 16x16 bitmap: " + elapsedTime16x16 + "ms");

            // You have to unwatch pixel
            fBPixelManager.unwatchPixels(samePixelData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
