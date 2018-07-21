package com.canvearth.canvearth.mapListeners;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.client.Palette;
import com.canvearth.canvearth.client.VisibilityHandler;
import com.canvearth.canvearth.utils.Configs;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class OnMapClickListenerImpl implements GoogleMap.OnMapClickListener {
    private MapsActivity activity;
    GoogleMap map;


    public OnMapClickListenerImpl(MapsActivity activity, GoogleMap map) {
        super();
        this.activity = activity;
        this.map = map;
    }

    public void onMapClick(LatLng point) {
//        VisibilityHandler.handleMainButtons(activity);
        if (Configs.TESTING) {
            int gridZoom = PixelUtils.getGridZoom(Math.round(map.getCameraPosition().zoom));

            if (gridZoom == Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL) {
                GridManager.fillPixel(
                        point.latitude,
                        point.longitude,
                        gridZoom,
                        Palette.getInstance().getColor()
                );
            }
        }
    }
}