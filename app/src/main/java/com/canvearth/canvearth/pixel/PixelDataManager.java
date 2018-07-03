package com.canvearth.canvearth.pixel;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;

import uk.me.jstott.jcoord.UTMRef;

public class PixelDataManager {
    public Pixel readPixel(LatLng latLng, int zoomLevel) {
        //TODO
        return new Pixel();
    }

    public boolean writePixel(String pixelId, FirebaseUser user, Color color) {
        //TODO
        return true;
    }

    public String getPixelId(LatLng latLng, int zoomLevel) {
        //TODO
        return "pixel-id";
    }

    private UTMRef latLngToUtmRef(LatLng latLng) {
        uk.me.jstott.jcoord.LatLng jcoordLatLng
                = new uk.me.jstott.jcoord.LatLng(latLng.latitude, latLng.longitude);
        return jcoordLatLng.toUTMRef();
    }
}
