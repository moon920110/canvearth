package com.canvearth.canvearth.utils;

import com.canvearth.canvearth.pixel.BoundingBox;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.pixel.PixelData;
import com.google.android.gms.maps.model.LatLng;

public class PixelUtils {

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
        final int x = pixel.data.x;
        final int y = pixel.data.y;
        final int zoom = pixel.data.zoom;
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
        return Math.min(viewZoom + Constants.VIEW_GRID_ZOOM_DIFF, Constants.LEAF_PIXEL_ZOOM_LEVEL);
    }

    public static double pix2lng(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public static double pix2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
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

    public static PixelData[] getChildrenPixelData(PixelData pixelData) throws Exception {
        if (pixelData.zoom == Constants.LEAF_PIXEL_ZOOM_LEVEL) {
            throw new Exception("Cannot get children of leaf pixel");
        }
        PixelData[] childrenPixelData = new PixelData[4];
        int zoom = pixelData.zoom + 1;
        childrenPixelData[0] = new PixelData(pixelData.x * 2, pixelData.y * 2, zoom);
        childrenPixelData[1] = new PixelData(pixelData.x * 2 + 1, pixelData.y * 2, zoom);
        childrenPixelData[2] = new PixelData(pixelData.x * 2, pixelData.y * 2 + 1, zoom);
        childrenPixelData[3] = new PixelData(pixelData.x * 2 + 1, pixelData.y * 2 + 1, zoom);
        return childrenPixelData;
    }
}
