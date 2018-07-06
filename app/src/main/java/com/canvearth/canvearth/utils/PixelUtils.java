package com.canvearth.canvearth.utils;

import com.canvearth.canvearth.pixel.BoundingBox;
import com.canvearth.canvearth.pixel.PixelCoord;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PixelUtils {

    public static PixelCoord latlng2pix(double lat, double lng, final int zoom) {
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

        return new PixelCoord(xtile, ytile, zoom);
    }

    public static BoundingBox pix2bbox(PixelCoord pixelCoord) {
        final int x = pixelCoord.x;
        final int y = pixelCoord.y;
        final int zoom = pixelCoord.zoom;
        BoundingBox bb = new BoundingBox();
        bb.north = pix2lat(y, zoom);
        bb.south = pix2lat(y + 1, zoom);
        bb.west = pix2lng(x, zoom);
        bb.east = pix2lng(x + 1, zoom);
        return bb;
    }

    public static double pix2lng(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double pix2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    public static PixelCoord getParentPixelCoord(PixelCoord pixelCoord) throws Exception {
        if (pixelCoord.zoom == 0) {
            throw new Exception("Cannot get parent of root pixel");
        }
        int zoom = pixelCoord.zoom - 1;
        int x = pixelCoord.x / 2;
        int y = pixelCoord.y / 2;
        return new PixelCoord(x, y, zoom);
    }

    public static ArrayList<PixelCoord> getChildrenPixelCoord(PixelCoord pixelCoord, int level) throws Exception {
        if (pixelCoord.zoom + level > Constants.LEAF_PIXEL_LEVEL) {
            throw new Exception("Cannot get children of leaf pixel");
        }
        int numChildrenWidth = MathUtils.intPow(2, level);
        ArrayList<PixelCoord> childrenPixelCoords = new ArrayList<>(numChildrenWidth * numChildrenWidth);
        int zoom = pixelCoord.zoom + level;
        for (int y = 0; y < numChildrenWidth; y++) {
            for(int x = 0; x < numChildrenWidth; x++) {
                childrenPixelCoords.set(y * numChildrenWidth + x,
                        new PixelCoord(pixelCoord.x * numChildrenWidth + x, pixelCoord.y * numChildrenWidth + y, zoom));
            }
        }
        return childrenPixelCoords;
    }
}
