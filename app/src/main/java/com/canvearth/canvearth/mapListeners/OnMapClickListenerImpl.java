package com.canvearth.canvearth.mapListeners;

import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ToggleButton;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class OnMapClickListenerImpl implements GoogleMap.OnMapClickListener {
    private Button menuButton;
    private Button pickerBucketButton;
    private Button addSketchButton;
    private ToggleButton gridVisibilityButton;
    private boolean utilVisibility;


    public OnMapClickListenerImpl(MapsActivity activity) {
        super();
        menuButton = activity.findViewById(R.id.showMeneButton);
        pickerBucketButton = activity.findViewById(R.id.pickerBucketButton);
        addSketchButton = activity.findViewById(R.id.addSketchButton);
        gridVisibilityButton = activity.findViewById(R.id.grid_visibility);
        utilVisibility = false;
    }

    public void onMapClick(LatLng l) {
        CameraPosition cameraPosition = MapsActivity.Map.getCameraPosition();
        boolean pickerBucketShow = PixelUtils.getGridZoom(Math.round(cameraPosition.zoom)) == Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL;
        if (utilVisibility) {
            menuButton.setVisibility(View.INVISIBLE);
            menuButton.startAnimation(getAnimation(0, -menuButton.getHeight()));
            gridVisibilityButton.setVisibility(View.INVISIBLE);
            gridVisibilityButton.startAnimation(getAnimation(0, gridVisibilityButton.getHeight()));
            if (pickerBucketShow) {
                pickerBucketButton.setVisibility(View.INVISIBLE);
                pickerBucketButton.startAnimation(getAnimation(0, pickerBucketButton.getHeight()));
            } else {
                addSketchButton.setVisibility(View.INVISIBLE);
                addSketchButton.startAnimation(getAnimation(0, addSketchButton.getHeight()));
            }
            utilVisibility = !utilVisibility;
        } else {
            menuButton.setVisibility(View.VISIBLE);
            menuButton.startAnimation(getAnimation(-menuButton.getHeight(), 0));
            gridVisibilityButton.setVisibility(View.VISIBLE);
            gridVisibilityButton.startAnimation(getAnimation(gridVisibilityButton.getHeight(), 0));
            if (pickerBucketShow) {
                pickerBucketButton.setVisibility(View.VISIBLE);
                pickerBucketButton.startAnimation(getAnimation(pickerBucketButton.getHeight(), 0));
            } else {
                addSketchButton.setVisibility(View.VISIBLE);
                addSketchButton.startAnimation(getAnimation(addSketchButton.getHeight(), 0));
            }
            utilVisibility = !utilVisibility;
        }
    }

    private TranslateAnimation getAnimation(int fromY, int toY) {
        TranslateAnimation ani = new TranslateAnimation(0, 0, fromY, toY);
        ani.setDuration(500);
        return ani;
    }
}