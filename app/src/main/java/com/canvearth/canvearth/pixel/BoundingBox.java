package com.canvearth.canvearth.pixel;

import android.graphics.Color;

import com.canvearth.canvearth.utils.Constants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;

public class BoundingBox {
    public double north; // latitude
    public double south; // latitude
    public double east; // longitude
    public double west; // longitude

    public LatLngBounds toLatLngBounds() {
        LatLng southwest = new LatLng(south, west);
        LatLng northeast = new LatLng(north, east);

        return new LatLngBounds(southwest, northeast);
    }

    public double getSideLength() {
        return Math.abs(east - west);
    }

    @Override
    public String toString() {
        return "north: " + Double.toString(north) + "\n " +
                "south: " + Double.toString(south) + "\n " +
                "east: " + Double.toString(east) + "\n " +
                "west: " + Double.toString(west) + "\n ";
    }
}
