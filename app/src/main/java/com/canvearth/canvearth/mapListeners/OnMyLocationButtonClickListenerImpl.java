package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.widget.Toast;

import com.canvearth.canvearth.utils.Constants;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

public class OnMyLocationButtonClickListenerImpl implements GoogleMap.OnMyLocationButtonClickListener {
    private Context context;

    public OnMyLocationButtonClickListenerImpl(Context context) {
        super();
        this.context = context;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}
