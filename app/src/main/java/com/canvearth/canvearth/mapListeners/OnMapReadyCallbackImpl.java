package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ToggleButton;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.client.VisibilityHandler;
import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PermissionUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MapStyleOptions;

public class OnMapReadyCallbackImpl implements OnMapReadyCallback {
    private Context context;
    private MapsActivity activity;


    public OnMapReadyCallbackImpl(Context context, MapsActivity activity) {
        super();
        this.context = context;
        this.activity = activity;
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
        activity.locationReady();
        activity.requestLocationUpate();
        MapsActivity.Map = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, R.raw.style_json));

            if (!success) {
                Log.e("CUSTOM", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("CUSTOM", "Can't find style. Error: ", e);
        }
        // TODO: Enable tilt gesture when performance issue is resolved
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setOnCameraIdleListener(new OnCameraIdleListenerImpl(context, activity));
        googleMap.setOnCameraMoveListener(new OnCameraMoveListenerImpl(activity));
        googleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListenerImpl(context));
        googleMap.setOnMyLocationClickListener(new OnMyLocationClickListenerImpl(context, googleMap));
        googleMap.setMaxZoomPreference(Constants.GRID_SHOW_MAX_CAM_ZOOM_LEVEL);
        googleMap.setOnMapClickListener(new OnMapClickListenerImpl(activity, googleMap));
        PermissionUtils.enableMyLocation(context, activity);

        VisibilityHandler.init();

        ToggleButton gridVisibilityButton = activity.findViewById(R.id.grid_visibility);
        gridVisibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GridManager.toggleVisibility();
            }
        });

        OnMainButtonsClickListenerImpl mainButtons = new OnMainButtonsClickListenerImpl(context, activity);
        setMainButtons(mainButtons);

        // TODO: calling this once when map ready for now, should be moved to OnMapClickListenerImpl
        VisibilityHandler.handleMainButtons(activity);
    }

    private void setMainButtons(OnMainButtonsClickListenerImpl mainButtons) {
        Button menu = activity.findViewById(R.id.showMenuButton);
        menu.setOnClickListener(mainButtons);
        Button brushButton = activity.findViewById(R.id.brushButton);
        brushButton.setOnClickListener(mainButtons);
    }
}
