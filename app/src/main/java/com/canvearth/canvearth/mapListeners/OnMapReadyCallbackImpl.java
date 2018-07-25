package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PermissionUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

public class OnMapReadyCallbackImpl implements OnMapReadyCallback {
    private Context context;
    private MapsActivity activity;
    private SupportMapFragment mapFragment;
    private View locationButton;


    public OnMapReadyCallbackImpl(Context context, MapsActivity activity, SupportMapFragment mapFragment) {
        super();
        this.context = context;
        this.activity = activity;
        this.mapFragment = mapFragment;
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
    public void onMapReady(GoogleMap map) {
        activity.locationReady();
        activity.requestLocationUpdate();

        MapsActivity.map = map;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, R.raw.style_json));

            if (!success) {
                Log.e("CUSTOM", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("CUSTOM", "Can't find style. Error: ", e);
        }
        // TODO: Enable tilt gesture when performance issue is resolved
        map.getUiSettings().setTiltGesturesEnabled(false);
        map.getUiSettings().setRotateGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setZoomGesturesEnabled(false);
        map.setOnCameraIdleListener(new OnCameraIdleListenerImpl(context, activity));
        map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListenerImpl(context));
        map.setOnMyLocationClickListener(new OnMyLocationClickListenerImpl(context, map));
        map.setMaxZoomPreference(Constants.GRID_SHOW_MAX_CAM_ZOOM_LEVEL);
        map.setOnMapClickListener(new OnMapClickListenerImpl(activity, map));
        PermissionUtils.enableMyLocation(context, activity);

        ToggleButton gridVisibilityButton = activity.findViewById(R.id.grid_visibility);

        Button menuButton = activity.findViewById(R.id.showMenuButton);
        gridVisibilityButton.setOnClickListener(view -> GridManager.toggleVisibility());
        menuButton.setVisibility(View.VISIBLE);
        gridVisibilityButton.setVisibility(View.VISIBLE);

        Button zoomInButton = activity.findViewById(R.id.zoomIn);
        zoomInButton.setOnClickListener(view -> map.animateCamera(CameraUpdateFactory.zoomIn()));

        Button zoomOutButton = activity.findViewById(R.id.zoomOut);
        zoomOutButton.setOnClickListener(view -> map.animateCamera(CameraUpdateFactory.zoomOut()));

        // Extract My Location View from maps fragment
        locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        // Change the visibility of my location button
        if (locationButton != null)
            locationButton.setVisibility(View.GONE);

        Button myLocationButton = activity.findViewById(R.id.myLocation);
        myLocationButton.setOnClickListener(view -> {
            if (locationButton != null)
                locationButton.callOnClick();
        });

        activity.requestLocationUpdate();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(activity.mLocation, Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL));
    }

}
