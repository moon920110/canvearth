package com.canvearth.canvearth.mapListeners;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.client.VisibilityHandler;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class OnMapClickListenerImpl implements GoogleMap.OnMapClickListener {
    private MapsActivity activity;

    public OnMapClickListenerImpl(MapsActivity activity) {
        super();
        this.activity = activity;
    }

    public void onMapClick(LatLng l) {
        VisibilityHandler.handleMainButtons(activity);
    }
}