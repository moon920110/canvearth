package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.widget.Button;
import android.widget.ToggleButton;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.utils.Constants;
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
        googleMap.setMaxZoomPreference(Constants.GRID_SHOW_MAX_ZOOM_LEVEL);
        PermissionUtils.enableMyLocation(context, activity);

        OnPickerClickListenerImpl pickerButtons = new OnPickerClickListenerImpl(context, activity);
        setPickers(pickerButtons);
    }

    private void setPickers(OnPickerClickListenerImpl pickerButtons) {
        Button redPicker = activity.findViewById(R.id.redPicker);
        redPicker.setOnClickListener(pickerButtons);
        Button pinkPicker = activity.findViewById(R.id.pinkPicker);
        pinkPicker.setOnClickListener(pickerButtons);
        Button purplePicker = activity.findViewById(R.id.purplePicker);
        purplePicker.setOnClickListener(pickerButtons);
        Button deepPurplePicker = activity.findViewById(R.id.deepPurplePicker);
        deepPurplePicker.setOnClickListener(pickerButtons);
        Button indigoPicker = activity.findViewById(R.id.indigoPicker);
        indigoPicker.setOnClickListener(pickerButtons);
        Button bluePicker = activity.findViewById(R.id.bluePicker);
        bluePicker.setOnClickListener(pickerButtons);
        Button lightBluePicker = activity.findViewById(R.id.lightBluePicker);
        lightBluePicker.setOnClickListener(pickerButtons);
        Button cyanPicker = activity.findViewById(R.id.cyanPicker);
        cyanPicker.setOnClickListener(pickerButtons);
        Button tealPicker = activity.findViewById(R.id.tealPicker);
        tealPicker.setOnClickListener(pickerButtons);
        Button greenPicker = activity.findViewById(R.id.greenPicker);
        greenPicker.setOnClickListener(pickerButtons);
        Button lightGreenPicker = activity.findViewById(R.id.lightGreenPicker);
        lightGreenPicker.setOnClickListener(pickerButtons);
        Button limePicker = activity.findViewById(R.id.limePicker);
        limePicker.setOnClickListener(pickerButtons);
        Button yellowPicker = activity.findViewById(R.id.yellowPicker);
        yellowPicker.setOnClickListener(pickerButtons);
        Button amberPicker = activity.findViewById(R.id.amberPicker);
        amberPicker.setOnClickListener(pickerButtons);
        Button orangePicker = activity.findViewById(R.id.orangePicker);
        orangePicker.setOnClickListener(pickerButtons);
        Button deepOrangePicker = activity.findViewById(R.id.deepOrangePicker);
        deepOrangePicker.setOnClickListener(pickerButtons);
        Button brownPicker = activity.findViewById(R.id.brownPicker);
        brownPicker.setOnClickListener(pickerButtons);
        Button greyPicker = activity.findViewById(R.id.greyPicker);
        greyPicker.setOnClickListener(pickerButtons);
        Button blueGreyPicker = activity.findViewById(R.id.blueGreyPicker);
        blueGreyPicker.setOnClickListener(pickerButtons);
        Button defaultPicker = activity.findViewById(R.id.defaultColorPicker);
        defaultPicker.setOnClickListener(pickerButtons);

        ToggleButton gridVisibilityButton = activity.findViewById(R.id.grid_visibility);
        gridVisibilityButton.setOnClickListener(pickerButtons);
    }
}
