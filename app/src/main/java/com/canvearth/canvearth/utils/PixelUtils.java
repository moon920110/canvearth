package com.canvearth.canvearth.utils;

import android.graphics.Point;
import android.graphics.Rect;

import com.canvearth.canvearth.pixel.BoundingBox;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.pixel.PixelDataSquare;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import junit.framework.Assert;

import java.util.ArrayList;

public class PixelUtils {

    public static Pixel latlng2pix(LatLng latLng, final int zoom) {
        return latlng2pix(latLng.latitude, latLng.longitude, zoom);
    }

    public static Pixel latlng2pix(double lat, double lng, final int zoom) {
        int xtile = (int) Math.floor((lng + 180) / 360 * (1 << zoom));
        int ytile = (int) Math.floor((1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom));
        if (xtile < 0)
            xtile = 0;
        if (xtile >= (1 << zoom))
            xtile = ((1 << zoom) - 1);
        if (ytile < 0)
            ytile = 0;
        if (ytile >= (1 << zoom))
            ytile = ((1 << zoom) - 1);

        return new Pixel(xtile, ytile, zoom);
    }

    public static BoundingBox pix2bbox(Pixel pixel) {
        return pixdata2bbox(pixel.data);
    }

    public static BoundingBox pixdata2bbox(PixelData pixelData) {
        final int x = pixelData.x;
        final int y = pixelData.y;
        final int zoom = pixelData.zoom;
        BoundingBox bb = new BoundingBox();
        bb.north = pix2lat(y, zoom);
        bb.south = pix2lat(y + 1, zoom);
        bb.west = pix2lng(x, zoom);
        bb.east = pix2lng(x + 1, zoom);
        return bb;
    }

    public static BoundingBox latlng2bbox(double lat, double lng, int zoom) {
        Pixel pix = latlng2pix(lat, lng, zoom);
        return pix2bbox(pix);
    }

    public static BoundingBox latlng2bbox(LatLng latlng, int zoom) {
        double lat = latlng.latitude;
        double lng = latlng.longitude;
        Pixel pix = latlng2pix(lat, lng, zoom);
        return pix2bbox(pix);
    }

    public static int getGridZoom(int viewZoom) {
        return Math.min(viewZoom + Constants.VIEW_GRID_ZOOM_DIFF, Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL);
    }

    public static double pix2lng(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double pix2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    public static PixelData getAncestorPixelData(PixelData pixelData, int level) throws Exception {
        if (pixelData.zoom - level < 0) {
            throw new Exception("Cannot get ancestor");
        }
        int zoom = pixelData.zoom - level;
        int x = pixelData.x / MathUtils.intPow(2, level);
        int y = pixelData.y / MathUtils.intPow(2, level);
        return new PixelData(x, y, zoom);
    }

    public static PixelData getParentPixelData(PixelData pixelData) throws Exception {
        if (pixelData.zoom == 0) {
            throw new Exception("Cannot get parent of root pixel");
        }
        int zoom = pixelData.zoom - 1;
        int x = pixelData.x / 2;
        int y = pixelData.y / 2;
        return new PixelData(x, y, zoom);
    }

    public static ArrayList<PixelData> getChildrenPixelData(PixelData pixelData, int level) throws Exception {
        if (pixelData.zoom + level > Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL) {
            throw new Exception("Cannot get children of leaf pixel");
        }
        int numChildrenSide = MathUtils.intPow(2, level);
        ArrayList<PixelData> childrenPixelData = new ArrayList<>(numChildrenSide * numChildrenSide);
        int zoom = pixelData.zoom + level;
        for (int y = 0; y < numChildrenSide; y++) {
            for (int x = 0; x < numChildrenSide; x++) {
                childrenPixelData.add(new PixelData(pixelData.x * numChildrenSide + x, pixelData.y * numChildrenSide + y, zoom));
                Assert.assertEquals(childrenPixelData.size(), y * numChildrenSide + x + 1);
            }
        }
        return childrenPixelData;
    }

    public static ArrayList<PixelData> makeBatchPixelData(PixelData startPixelData, int numX, int numY) {
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

    public static PixelDataSquare getPixelDataSquareFromBound(GoogleMap map, Rect bound, int zoom) {
        Projection projection = map.getProjection();
        LatLng leftTop = projection.fromScreenLocation(new Point(bound.left, bound.top));
        LatLng rightBottom = projection.fromScreenLocation(new Point(bound.right, bound.bottom));

        PixelData leftTopPixelData = latlng2pix(leftTop, zoom).data;
        PixelData rightBottomPixelData = latlng2pix(rightBottom, zoom).data;

        return new PixelDataSquare(leftTopPixelData, rightBottomPixelData);
    }
}
