package com.canvearth.canvearth.client;


import android.os.AsyncTask;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.pixel.PixelColor;
import com.canvearth.canvearth.server.BitmapDrawer;
import com.canvearth.canvearth.server.FBPixelManager;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.SphericalMercator;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GridManager {
    private static String TAG = "GridManager";
    private static Map<String, Pixel> pixels = new HashMap<>();
    private static FBPixelManager fBPixelManager = FBPixelManager.getInstance();
    private static boolean isVisible = true;


    private static void addPixels(GoogleMap map, double pixSideLen, int gridZoom) {
        Projection projection = map.getProjection();
        LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        double minY = -180 + pixSideLen * (int) (SphericalMercator.scaleLatitude(bounds.southwest.latitude) / pixSideLen) - 5 * (pixSideLen / 2);
        double minX = -180 + pixSideLen * (int) (SphericalMercator.scaleLongitude(bounds.southwest.longitude) / pixSideLen) - 5 * (pixSideLen / 2);
        double maxY = -180 + pixSideLen * (int) (SphericalMercator.scaleLatitude(bounds.northeast.latitude) / pixSideLen) + 5 * (pixSideLen / 2);
        double maxX = -180 + pixSideLen * (int) (SphericalMercator.scaleLongitude(bounds.northeast.longitude) / pixSideLen) + 5 * (pixSideLen / 2);

        new AsyncTask<Object, Pixel, Set<String>>() {
            @Override
            protected Set<String> doInBackground(Object... object) {

                Set<String> pixelsToErase = new HashSet<>(pixels.keySet());
                Set<String> pixelsToAdd = new HashSet<>();

                for (double y = minY; y < maxY; y += pixSideLen) {
                    if (minX <= maxX) {
                        for (double x = minX; x < maxX; x += pixSideLen) {
                            Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, gridZoom);
                            String key = pixel.data.getFirebaseId();
                            if (!pixels.containsKey(key)) {
                                pixels.put(key, pixel);
                                pixelsToAdd.add(key);
                            } else {
                                pixelsToErase.remove(key);
                            }
                        }
                    } else {
                        for (double x = -180; x < minX; x += pixSideLen) {
                            Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, gridZoom);
                            String key = pixel.data.getFirebaseId();
                            if (!pixels.containsKey(key)) {
                                pixels.put(key, pixel);
                                pixelsToAdd.add(key);
                            } else {
                                pixelsToErase.remove(key);
                            }
                        }
                        for (double x = maxX; x < 180; x += pixSideLen) {
                            Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, gridZoom);
                            String key = pixel.data.getFirebaseId();
                            if (!pixels.containsKey(key)) {
                                pixels.put(key, pixel);
                                pixelsToAdd.add(key);
                            } else {
                                pixelsToErase.remove(key);
                            }
                        }
                    }
                }

                for (String key : pixelsToAdd) {
                    Pixel pix = pixels.get(key);
                    fBPixelManager.watchPixel(pix.data);
                    publishProgress(pix);
                }
                return pixelsToErase;
            }

            @Override
            protected void onProgressUpdate(Pixel... params) {
                params[0].draw(map, isVisible);
            }

            @Override
            protected void onPostExecute(Set<String> pixelsToErase) {
                super.onPostExecute(pixelsToErase);
                for (String key : pixelsToErase) {
                    Pixel pixelToErase = pixels.get(key);
                    fBPixelManager.unwatchPixel(pixelToErase.data);
                    pixelToErase.erase();
                    pixels.remove(key);
                }
            }
        }.execute();
    }


    public static void draw(GoogleMap map, int zoom, MapsActivity activity) {
        int gridZoom = PixelUtils.getGridZoom(zoom);
        double pixSideLen = PixelUtils.latlng2bbox(map.getCameraPosition().target, gridZoom).getSideLength();


        if (gridZoom > Constants.BITMAP_SHOW_MAX_GRID_ZOOM_LEVEL) {
            addPixels(map, pixSideLen, gridZoom);
        } else if (gridZoom >= Constants.BITMAP_SHOW_MIN_GRID_ZOOM_LEVEL) {
            cleanup();
            BitmapDrawer.getInstance().drawBitmap(
                    Math.min(gridZoom + Constants.GRID_BITMAP_ZOOM_DIFF, Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL),
                    map.getCameraPosition(),
                    activity
            );
        }
    }

    public static void cleanup() {
        for (Map.Entry<String, Pixel> entry : pixels.entrySet()) {
            Pixel pix = entry.getValue();
            fBPixelManager.unwatchPixel(pix.data);
            pix.erase();
        }

        pixels.clear();
    }

    public static void toggleVisibility() {
        isVisible = !isVisible;

        for (Map.Entry<String, Pixel> entry : pixels.entrySet()) {
            Pixel pix = entry.getValue();
            pix.changeVisibility(isVisible);
        }
    }

    public static void fillPixel(double lat, double lng, int gridZoom, int color) {
        Pixel pixel = PixelUtils.latlng2pix(lat, lng, gridZoom);

        pixels.get(pixel.data.getFirebaseId()).fill(color, isVisible);
        FBPixelManager.getInstance().writePixelAsync(pixel.data, new PixelColor(color));
    }

    public static void changePixelColor(String firebaseId, int color) {
        Pixel pixel = pixels.get(firebaseId);
        if (pixel != null) {
            pixel.fill(color, isVisible);
        }
    }
}
