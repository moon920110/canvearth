package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.client.Palette;
import com.canvearth.canvearth.utils.Configs;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.GoogleMap;

public class OnMyLocationClickListenerImpl implements GoogleMap.OnMyLocationClickListener {
    Context context;
    GoogleMap map;

    public OnMyLocationClickListenerImpl(Context context, GoogleMap map) {
        super();
        this.context = context;
        this.map = map;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        if (!Configs.TESTING) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            int viewZoom = Math.round(map.getCameraPosition().zoom);
            int gridZoom = PixelUtils.getGridZoom(viewZoom);

            if (gridZoom == Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL) {
                GridManager.fillPixel(lat, lng, gridZoom, Palette.getInstance().getColor());
            }
        }
    }
}
