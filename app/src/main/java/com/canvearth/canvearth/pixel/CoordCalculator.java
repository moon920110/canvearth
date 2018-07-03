package com.canvearth.canvearth.pixel;

import com.google.android.gms.maps.model.LatLng;

import uk.me.jstott.jcoord.UTMRef;

public class CoordCalculator {
    public static String getPixelId(LatLng latLng, int zoomLevel) {
        //TODO
        return "pixel-id";
    }

    private static UTMRef latLngToUtmRef(LatLng latLng) {
        uk.me.jstott.jcoord.LatLng jcoordLatLng
                = new uk.me.jstott.jcoord.LatLng(latLng.latitude, latLng.longitude);
        return jcoordLatLng.toUTMRef();
    }
}
