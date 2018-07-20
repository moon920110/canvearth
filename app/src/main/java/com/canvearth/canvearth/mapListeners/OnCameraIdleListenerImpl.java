package com.canvearth.canvearth.mapListeners;

import android.content.Context;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.ScreenUtils;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

public class OnCameraIdleListenerImpl implements GoogleMap.OnCameraIdleListener {
    private MapScaleView scaleView;
    private Context context;

    public OnCameraIdleListenerImpl(Context context, MapScaleView scaleView) {
        super();
        this.scaleView = scaleView;
        this.context = context;
    }

    @Override
    public void onCameraIdle() {
        CameraPosition cameraPosition = MapsActivity.Map.getCameraPosition();
        scaleView.update(cameraPosition.zoom, cameraPosition.target.latitude);
        ScreenUtils.showToast(context, "zoom: " + Float.toString(cameraPosition.zoom) + "\n" +
                "grid zoom: " + PixelUtils.getGridZoom(Math.round(cameraPosition.zoom)));

        GridManager.cleanup();

        if (cameraPosition.zoom >= Constants.GRID_SHOW_MIN_CAM_ZOOM_LEVEL && cameraPosition.zoom <= Constants.GRID_SHOW_MAX_CAM_ZOOM_LEVEL) {
            GridManager.draw(MapsActivity.Map, Math.round(cameraPosition.zoom));
        }
    }
}
