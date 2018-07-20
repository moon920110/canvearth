package com.canvearth.canvearth.mapListeners;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.client.VisibilityHandler;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

public class OnCameraMoveListenerImpl implements GoogleMap.OnCameraMoveListener{
    private MapScaleView scaleView;
    private MapsActivity activity;

    public OnCameraMoveListenerImpl(MapsActivity activity, MapScaleView scaleView) {
        super();
        this.scaleView = scaleView;
        this.activity = activity;
    }

    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition = MapsActivity.Map.getCameraPosition();

        scaleView.update(cameraPosition.zoom, cameraPosition.target.latitude);
        VisibilityHandler.checkAndToggleSouthEastButton(activity);
    }
}
