package com.canvearth.canvearth.pixel;

import android.graphics.Point;

import com.canvearth.canvearth.client.Palette;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class Pixel {
    public PixelData data;
    private Polygon polygon;

    public Pixel(int x, int y, int zoom) {
        this.data = new PixelData(x, y, zoom);
    }

    public boolean isLeaf() {
        return data.isLeaf();
    }

    public boolean isRoot() {
        return data.isRoot();
    }

    public PolygonOptions getPolygonOptions(boolean isVisible) {
        BoundingBox bbox = PixelUtils.pix2bbox(this);
        PolygonOptions polygonOptions = new PolygonOptions()
                .add(new LatLng(bbox.north, bbox.west),
                        new LatLng(bbox.south, bbox.west),
                        new LatLng(bbox.south, bbox.east),
                        new LatLng(bbox.north, bbox.east),
                        new LatLng(bbox.north, bbox.west))
                .strokeWidth(Constants.PIX_STROKE_WIDTH);

        if (isVisible) {
            polygonOptions.strokeColor(Constants.PIX_STROKE_VISIBLE_COLOR);
        } else {
            polygonOptions.strokeColor(Constants.PIX_STROKE_INVISIBLE_COLOR);
        }
        return polygonOptions;
    }

    public void draw(GoogleMap map, boolean isVisible) {
        PolygonOptions polygonOptions = getPolygonOptions(isVisible);
        Polygon polygon = map.addPolygon(polygonOptions);
        this.polygon = polygon;
    }

    public void fill(int color) {
        polygon.setFillColor(color);
    }

    public void erase() {
        this.polygon.remove();
    }

    public void changeVisibility(boolean isVisible) {
        if (!isVisible) {
            this.polygon.setStrokeColor(Constants.PIX_STROKE_INVISIBLE_COLOR);
        } else {
            this.polygon.setStrokeColor(Constants.PIX_STROKE_VISIBLE_COLOR);
        }
    }
}
