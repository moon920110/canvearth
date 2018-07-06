package com.canvearth.canvearth.pixel;

import android.util.Log;

import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class Pixel {
    public int x;
    public int y;
    public int zoom;
    private Polygon polygon;

    public Pixel(int x, int y, int zoom) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
    }

    public boolean isLeaf() {
        return zoom == Constants.LEAF_PIXEL_ZOOM_LEVEL;
    }

    public boolean isRoot() {
        return zoom == 0;
    }

    public String getFirebaseId() { // This method have to be synchronized with FBPixel.getFirebaseId().
        return Integer.toString(zoom) + "," + Integer.toString(x) + "," + Integer.toString(y);
    }

    public PolygonOptions getPolygonOptions() {
        BoundingBox bbox = PixelUtils.pix2bbox(this);
        return new PolygonOptions()
                .add(new LatLng(bbox.north, bbox.west),
                        new LatLng(bbox.south, bbox.west),
                        new LatLng(bbox.south, bbox.east),
                        new LatLng(bbox.north, bbox.east),
                        new LatLng(bbox.north, bbox.west))
                .strokeColor(Constants.PIX_STROKE_COLOR)
                .strokeWidth(Constants.PIX_STROKE_WIDTH);
    }

    public void draw(GoogleMap map) {
        PolygonOptions polygonOptions = getPolygonOptions();
        Polygon polygon = map.addPolygon(polygonOptions);

        polygon.setClickable(true);
        map.setOnPolygonClickListener(pg -> pg.setFillColor(Constants.PIX_COLOR_RED));
        this.polygon = polygon;
    }

    public void erase() {
        this.polygon.remove();
    }
}
