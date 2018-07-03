package com.canvearth.canvearth.pixel;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import uk.me.jstott.jcoord.UTMRef;

public class Pixel {

    public UTMRef rootUTMRef;
    public Double easting;
    public Double northing;
    public Double width;
    public Double height;

    public Long level; // leaf Pixel's level is 0.
    public ArrayList<String> childrenIds;
    public String parentId;

    //TODO image member variable needed

    public Pixel() {
        // Default constructor required for Firebase db
    }

    public Pixel(LatLng latLng, int zoomLevel) {
        // TODO
    }

    public String getPixelId() {
        //TODO
        return "pixel-id";
    }
}
