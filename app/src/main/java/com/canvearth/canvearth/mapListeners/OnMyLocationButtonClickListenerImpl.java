package com.canvearth.canvearth.mapListeners;

import android.content.Context;

import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;

public class OnMyLocationButtonClickListenerImpl implements GoogleMap.OnMyLocationButtonClickListener{
    private MapScaleView scaleView;
    private Context context;

    public OnMyLocationButtonClickListenerImpl(Context context, MapScaleView scaleView) {
        super();
        this.scaleView = scaleView;
        this.context = context;
    }
    @Override
    public boolean onMyLocationButtonClick() {
        // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}
