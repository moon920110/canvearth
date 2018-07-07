package com.canvearth.canvearth.client;

import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.server.FBPixelManager;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.SphericalMercator;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.Map;

public class GridManager {
    private static Map<String, Pixel> pixels = new HashMap<>();
    private static FBPixelManager fBPixelManager = FBPixelManager.getInstance();

    private static void addPixels(GoogleMap map, double pixSize, int gridZoom) {
        Projection projection = map.getProjection();
        LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        double minY = -180 + pixSize * (int) (SphericalMercator.scaleLatitude(bounds.southwest.latitude) / pixSize) - 5 * (pixSize / 2);
        double minX = -180 + pixSize * (int) (SphericalMercator.scaleLongitude(bounds.southwest.longitude) / pixSize) - 5 * (pixSize / 2);
        double maxY = -180 + pixSize * (int) (SphericalMercator.scaleLatitude(bounds.northeast.latitude) / pixSize) + 5 * (pixSize / 2);
        double maxX = -180 + pixSize * (int) (SphericalMercator.scaleLongitude(bounds.northeast.longitude) / pixSize) + 5 * (pixSize / 2);

        for (double y = minY; y < maxY; y += pixSize) {
            if (minX <= maxX) {
                for (double x = minX; x < maxX; x += pixSize) {
                    Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, gridZoom);
                    pixels.put(pixel.data.firebaseId, pixel);
                }
            } else {
                for (double x = -180; x < minX; x += pixSize) {
                    Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, gridZoom);
                    pixels.put(pixel.data.firebaseId, pixel);
                }
                for (double x = maxX; x < 180; x += pixSize) {
                    Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, gridZoom);
                    pixels.put(pixel.data.firebaseId, pixel);
                }
            }
        }

        for (Map.Entry<String, Pixel> entry : pixels.entrySet()) {
            Pixel pix = entry.getValue();
            fBPixelManager.watchPixel(pix.data);
            pix.draw(map);
        }
    }


    public static void draw(GoogleMap map, int zoom) {
        int gridZoom = PixelUtils.getGridZoom(zoom);
        double pixSize = PixelUtils.latlng2bbox(map.getCameraPosition().target, gridZoom).getSize();

        addPixels(map, pixSize, gridZoom);
    }

    public static void cleanup() {
        for (Map.Entry<String, Pixel> entry : pixels.entrySet()) {
            Pixel pix = entry.getValue();
            fBPixelManager.unwatchPixel(pix.data);
            pix.erase();
        }

        pixels.clear();
    }
}
