package com.canvearth.canvearth.pixel;

import com.canvearth.canvearth.client.Palette;
import com.canvearth.canvearth.server.FBPixelManager;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

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
        map.setOnPolygonClickListener(pg -> pg.setFillColor(Palette.getInstance().getColor()));
        this.polygon = polygon;
    }

    public void erase() {
        this.polygon.remove();
    }
}
