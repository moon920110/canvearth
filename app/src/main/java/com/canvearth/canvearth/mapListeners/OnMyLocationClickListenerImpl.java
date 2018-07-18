package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.client.Palette;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.ScreenUtils;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;

public class OnMyLocationClickListenerImpl implements GoogleMap.OnMyLocationClickListener {
    MapScaleView scaleView;
    Context context;
    GoogleMap map;
    Palette palette = Palette.getInstance();

    public OnMyLocationClickListenerImpl(Context context, MapScaleView scaleView, GoogleMap map) {
        super();
        this.context = context;
        this.scaleView = scaleView;
        this.map = map;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        int viewZoom = Math.round(map.getCameraPosition().zoom);
        int gridZoom = PixelUtils.getGridZoom(viewZoom);

        if (gridZoom == Constants.LEAF_PIXEL_ZOOM_LEVEL) {
            GridManager.fillMyPixel(lat, lng, gridZoom, palette.getColor());
        }
    }
}
