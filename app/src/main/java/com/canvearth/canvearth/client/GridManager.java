package com.canvearth.canvearth.client;

import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.pixel.PixelColor;
import com.canvearth.canvearth.server.FBPixelManager;
import com.canvearth.canvearth.server.MockFBPixelManager;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.SphericalMercator;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.Map;

public class GridManager {
    private static Map<String, Pixel> pixels = new HashMap<>();
    private static FBPixelManager fBPixelManager = FBPixelManager.getInstance();

    private static void addPixels(GoogleMap map, double pixSideLen, int gridZoom) {
        Projection projection = map.getProjection();
        LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        double minY = -180 + pixSideLen * (int) (SphericalMercator.scaleLatitude(bounds.southwest.latitude) / pixSideLen) - 5 * (pixSideLen / 2);
        double minX = -180 + pixSideLen * (int) (SphericalMercator.scaleLongitude(bounds.southwest.longitude) / pixSideLen) - 5 * (pixSideLen / 2);
        double maxY = -180 + pixSideLen * (int) (SphericalMercator.scaleLatitude(bounds.northeast.latitude) / pixSideLen) + 5 * (pixSideLen / 2);
        double maxX = -180 + pixSideLen * (int) (SphericalMercator.scaleLongitude(bounds.northeast.longitude) / pixSideLen) + 5 * (pixSideLen / 2);

        for (double y = minY; y < maxY; y += pixSideLen) {
            if (minX <= maxX) {
                for (double x = minX; x < maxX; x += pixSideLen) {
                    Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, gridZoom);
                    pixels.put(pixel.data.firebaseId, pixel);
                }
            } else {
                for (double x = -180; x < minX; x += pixSideLen) {
                    Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, gridZoom);
                    pixels.put(pixel.data.firebaseId, pixel);
                }
                for (double x = maxX; x < 180; x += pixSideLen) {
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
        double pixSideLen = PixelUtils.latlng2bbox(map.getCameraPosition().target, gridZoom).getSideLength();

        addPixels(map, pixSideLen, gridZoom);
    }

    public static void cleanup() {
        for (Map.Entry<String, Pixel> entry : pixels.entrySet()) {
            Pixel pix = entry.getValue();
            fBPixelManager.unwatchPixel(pix.data);
            pix.erase();
        }

        pixels.clear();
    }

    public static void fillMyPixel(double lat, double lng, int gridZoom, int color) {
        Pixel pixel = PixelUtils.latlng2pix(lat, lng, gridZoom);

        MockFBPixelManager.getInstance().writePixelAsync(pixel.data, new PixelColor(color), pixelData -> {
            pixels.get(pixelData.firebaseId).fill(color);
        });
    }
}
