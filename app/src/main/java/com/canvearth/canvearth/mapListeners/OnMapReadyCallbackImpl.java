package com.canvearth.canvearth.mapListeners;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.utils.PermissionUtils;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

public class OnMapReadyCallbackImpl implements OnMapReadyCallback {
    private Context context;
    private AppCompatActivity activity;
    private MapScaleView scaleView;

    public OnMapReadyCallbackImpl(Context context, AppCompatActivity activity, MapScaleView scaleView) {
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

        // TODO: Enable tilt gesture when performance issue is resolved
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setOnCameraIdleListener(new OnCameraIdleListenerImpl(context, scaleView));
        googleMap.setOnCameraMoveListener(new OnCameraMoveListenerImpl(context, scaleView));
        googleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListenerImpl(context, scaleView));
        googleMap.setOnMyLocationClickListener(new OnMyLocationClickListenerImpl(context, scaleView));
        PermissionUtils.enableMyLocation(context, activity);
    }


}
