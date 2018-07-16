package com.canvearth.canvearth.mapListeners;

import android.content.Context;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.utils.ScreenUtils;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

public class OnCameraMoveListenerImpl implements GoogleMap.OnCameraMoveListener{
    MapScaleView scaleView;
    Context context;

    public OnCameraMoveListenerImpl(Context context, MapScaleView scaleView) {
        super();
        this.scaleView = scaleView;
        this.context = context;

    }

    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition = MapsActivity.Map.getCameraPosition();
        scaleView.update(cameraPosition.zoom, cameraPosition.target.latitude);
    }
}
