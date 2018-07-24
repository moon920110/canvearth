package com.canvearth.canvearth.server;

import android.graphics.Camera;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import junit.framework.Assert;

import java.net.MalformedURLException;
import java.net.URL;

public class BitmapDrawer {
//    private static final BitmapDrawer ourInstance = new BitmapDrawer();
//
//    public static BitmapDrawer getInstance() {
//        return ourInstance;
//    }
//
//    private BitmapDrawer() {
//    }
//
//    public void drawBitmap(int showingZoomLevel, CameraPosition currentPosition) {
//        int bitmapTargetZoomLevel = showingZoomLevel + Constants.BITMAP_CACHE_RESOLUTION_FACTOR;
//        double pixSideLen = PixelUtils.latlng2bbox(currentPosition.target, bitmapTargetZoomLevel).getSideLength();
//
//
//
//
//
//
//
//
//
//        TileProvider tileProvider = new UrlTileProvider(256, 256) {
//            @Override
//            public URL getTileUrl(int x, int y, int zoom) {
//
//                /* Define the URL pattern for the tile images */
//                String s = String.format("http://my.image.server/images/%d/%d/%d.png",
//                        zoom, x, y);
//
//                if (!checkTileExists(x, y, zoom)) {
//                    return null;
//                }
//
//                try {
//                    return new URL(s);
//                } catch (MalformedURLException e) {
//                    throw new AssertionError(e);
//                }
//            }
//
//            /*
//             * Check that the tile server supports the requested x, y and zoom.
//             * Complete this stub according to the tile range you support.
//             * If you support a limited range of tiles at different zoom levels, then you
//             * need to define the supported x, y range at each zoom level.
//             */
//            private boolean checkTileExists(int x, int y, int zoom) {
//                return false;
//            }
//        };
//
//        TileOverlay tileOverlay = MapsActivity.Map.addTileOverlay(new TileOverlayOptions()
//                .tileProvider(tileProvider));
//    }
}
