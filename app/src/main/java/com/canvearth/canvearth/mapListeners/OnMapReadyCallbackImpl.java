package com.canvearth.canvearth.mapListeners;

import android.content.Context;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.utils.PermissionUtils;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class OnMapReadyCallbackImpl implements OnMapReadyCallback {
    private Context context;
    private MapsActivity activity;
    private MapScaleView scaleView;

    public OnMapReadyCallbackImpl(Context context, MapsActivity activity, MapScaleView scaleView) {
        super();
        this.context = context;
        this.activity = activity;
        this.scaleView = scaleView;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsActivity.Map = googleMap;
        googleMap.setPadding(20, 150, 20, 150);
        // TODO: Enable tilt gesture when performance issue is resolved
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setOnCameraIdleListener(new OnCameraIdleListenerImpl(activity, context, scaleView));
        googleMap.setOnCameraMoveListener(new OnCameraMoveListenerImpl(context, scaleView));
        googleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListenerImpl(context, scaleView));
        googleMap.setOnMyLocationClickListener(new OnMyLocationClickListenerImpl(context, scaleView, googleMap));
        PermissionUtils.enableMyLocation(context, activity);
    }


}
