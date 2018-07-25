package com.canvearth.canvearth.pixel;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Pixel {
    public PixelData data;
    private Polygon polygon;
    private boolean deleted = false;
    private Lock lock = new ReentrantLock();

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
        if (deleted)
            return;
        lock.lock();
        if (polygon == null) {
            PolygonOptions polygonOptions = getPolygonOptions(isVisible);
            polygon = MapsActivity.map.addPolygon(polygonOptions);
        }
        lock.unlock();
    }

    public void fill(int color, boolean isVisible) {
        if (deleted)
            return;
        lock.lock();
        if (polygon == null) {
            PolygonOptions polygonOptions = getPolygonOptions(isVisible);
            polygon = MapsActivity.map.addPolygon(polygonOptions);
        }
        lock.unlock();
        polygon.setFillColor(color);
    }

    public void erase() {
        deleted = true;
        lock.lock();
        if (this.polygon != null) {
            this.polygon.remove();
        }
        lock.unlock();
    }

    public void changeVisibility(boolean isVisible) {
        lock.lock();
        if (!isVisible) {
            this.polygon.setStrokeColor(Constants.PIX_STROKE_INVISIBLE_COLOR);
        } else {
            this.polygon.setStrokeColor(Constants.PIX_STROKE_VISIBLE_COLOR);
        }

        lock.unlock();
    }
}
