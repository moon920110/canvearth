package com.canvearth.canvearth.mapListeners;

import android.content.Context;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.ScreenUtils;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

public class OnCameraIdleListenerImpl implements GoogleMap.OnCameraIdleListener{

    MapScaleView scaleView;
    Context context;

    public OnCameraIdleListenerImpl(Context context, MapScaleView scaleView) {
        super();
        this.scaleView = scaleView;
        this.context = context;
    }

    @Override
    public void onCameraIdle() {
        CameraPosition cameraPosition = MapsActivity.Map.getCameraPosition();
        ScreenUtils.showToast(context, "lat: " + cameraPosition.target.latitude + "\n" +
                "lng: " + cameraPosition.target.longitude + "\n" +
                "zoom: " + cameraPosition.zoom);
        scaleView.update(cameraPosition.zoom, cameraPosition.target.latitude);

        GridManager.cleanup();
        if (cameraPosition.zoom >= Constants.GRID_SHOW_MIN_ZOOM_LEVEL && cameraPosition.zoom <= Constants.GRID_SHOW_MAX_ZOOM_LEVEL) {
            GridManager.draw(MapsActivity.Map, Math.round(cameraPosition.zoom));
        }
    }
}
