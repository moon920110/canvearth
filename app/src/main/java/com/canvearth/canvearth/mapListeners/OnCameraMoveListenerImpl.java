package com.canvearth.canvearth.mapListeners;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.client.VisibilityHandler;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

public class OnCameraMoveListenerImpl implements GoogleMap.OnCameraMoveListener{
    private MapsActivity activity;

    public OnCameraMoveListenerImpl(MapsActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onCameraMove() {
        VisibilityHandler.checkAndToggleSouthEastButton(activity);
    }
}
