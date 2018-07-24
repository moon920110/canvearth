package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.ScreenUtils;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

public class OnCameraIdleListenerImpl implements GoogleMap.OnCameraIdleListener {
    private Context context;
    private MapsActivity activity;

    public OnCameraIdleListenerImpl(Context context, MapsActivity activity) {
        super();
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onCameraIdle() {
        CameraPosition cameraPosition = MapsActivity.Map.getCameraPosition();
        int gridZoom = PixelUtils.getGridZoom(Math.round(cameraPosition.zoom));
        ScreenUtils.showToast(context, "zoom: " + Float.toString(cameraPosition.zoom) + "\n" +
                "grid zoom: " + gridZoom);

        if (cameraPosition.zoom < Constants.REGISTRATION_ZOOM_LEVEL) {
            Button addSketchButton = activity.findViewById(R.id.addSketchButton);
            addSketchButton.setVisibility(View.INVISIBLE);
        } else {
            Button addSketchButton = activity.findViewById(R.id.addSketchButton);
            addSketchButton.setVisibility(View.VISIBLE);
        }

        if (cameraPosition.zoom >= Constants.GRID_SHOW_MIN_CAM_ZOOM_LEVEL && cameraPosition.zoom <= Constants.GRID_SHOW_MAX_CAM_ZOOM_LEVEL) {
            GridManager.draw(MapsActivity.Map, Math.round(cameraPosition.zoom));
        }
    }
}
