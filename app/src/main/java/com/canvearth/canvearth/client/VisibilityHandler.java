package com.canvearth.canvearth.client;

import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.model.CameraPosition;


public class VisibilityHandler {
    public static boolean mainsVisibility;
    private static boolean pickerBucketVisibility;
    private static boolean menuVisibility;

    public static void init(){
        mainsVisibility = false;
        pickerBucketVisibility = false;
        menuVisibility = false;
    }

    public static void handleMenuButton(MapsActivity activity){
        LinearLayout menuLayout = activity.findViewById(R.id.menuLayout);
        if(menuVisibility) {
            menuLayout.setVisibility(View.INVISIBLE);
            menuLayout.startAnimation(getAnimation(0, -menuLayout.getWidth(), 0, 0));
            menuVisibility = !menuVisibility;
        } else {
            menuLayout.setVisibility(View.VISIBLE);
            menuLayout.startAnimation(getAnimation(-menuLayout.getWidth(), 0, 0, 0));
            menuVisibility = !menuVisibility;

            if(pickerBucketVisibility){
                handlePickerBucketButton(activity);
            }
        }
    }

    public static void handlePickerBucketButton(MapsActivity activity){
        GridView paletteGridView = activity.findViewById(R.id.palette);
        if(pickerBucketVisibility){
            paletteGridView.setVisibility(View.INVISIBLE);
            pickerBucketVisibility = !pickerBucketVisibility;
        } else {
            paletteGridView.setVisibility(View.VISIBLE);
            pickerBucketVisibility = !pickerBucketVisibility;

            if(menuVisibility){
                handleMenuButton(activity);
            }
        }
    }

    public static void handleMainButtons(MapsActivity activity){
        Button menuButton = activity.findViewById(R.id.showMenuButton);
//        Button pickerBucketButton = activity.findViewById(R.id.pickerBucketButton);
        Button addSketchButton = activity.findViewById(R.id.addSketchButton);
        ToggleButton gridVisibilityButton = activity.findViewById(R.id.grid_visibility);

        CameraPosition cameraPosition = MapsActivity.Map.getCameraPosition();
        boolean pickerBucketShow = PixelUtils.getGridZoom(Math.round(cameraPosition.zoom)) == Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL;
        if(mainsVisibility){
            if(menuVisibility){
                handleMenuButton(activity);
            }
            menuButton.setVisibility(View.INVISIBLE);
            menuButton.startAnimation(getAnimation(0, 0, 0, -menuButton.getHeight()));
            gridVisibilityButton.setVisibility(View.INVISIBLE);
            gridVisibilityButton.startAnimation(getAnimation(0, 0, 0, gridVisibilityButton.getHeight()));
//            if (pickerBucketShow) {
//                if(pickerBucketVisibility) {
//                    handlePickerBucketButton(activity);
//                }
//                pickerBucketButton.setVisibility(View.INVISIBLE);
//                pickerBucketButton.startAnimation(getAnimation(0, 0, 0, pickerBucketButton.getHeight()));
//            } else {
//                addSketchButton.setVisibility(View.INVISIBLE);
//                addSketchButton.startAnimation(getAnimation(0, 0, 0, addSketchButton.getHeight()));
//            }
            mainsVisibility = !mainsVisibility;
        } else {
            menuButton.setVisibility(View.VISIBLE);
            menuButton.startAnimation(getAnimation(0, 0, -menuButton.getHeight(), 0));
            gridVisibilityButton.setVisibility(View.VISIBLE);
            gridVisibilityButton.startAnimation(getAnimation(0, 0, gridVisibilityButton.getHeight(), 0));
//            if (pickerBucketShow) {
//                pickerBucketButton.setVisibility(View.VISIBLE);
//                pickerBucketButton.startAnimation(getAnimation(0, 0, pickerBucketButton.getHeight(), 0));
//            } else {
//                addSketchButton.setVisibility(View.VISIBLE);
//                addSketchButton.startAnimation(getAnimation(0, 0, addSketchButton.getHeight(), 0));
//            }
            mainsVisibility = !mainsVisibility;
        }
    }

    public static void checkAndToggleSouthEastButton(MapsActivity activity){
//        CameraPosition cameraPosition = MapsActivity.Map.getCameraPosition();
//        boolean pickerBucketShow = PixelUtils.getGridZoom(Math.round(cameraPosition.zoom)) == Constants.LEAF_PIXEL_GRID_ZOOM_LEVEL;
//        Button pickerBucketButton = activity.findViewById(R.id.pickerBucketButton);
//        Button addSketchButton = activity.findViewById(R.id.addSketchButton);
//
//        if(mainsVisibility){
//            if(pickerBucketShow){
//                addSketchButton.setVisibility(View.INVISIBLE);
//                pickerBucketButton.setVisibility(View.VISIBLE);
//            } else {
//                pickerBucketButton.setVisibility(View.INVISIBLE);
//                addSketchButton.setVisibility(View.VISIBLE);
//            }
//        }
    }
    private static TranslateAnimation getAnimation(int fromX, int toX, int fromY, int toY) {
        TranslateAnimation ani = new TranslateAnimation(fromX, toX, fromY, toY);
        ani.setDuration(500);
        return ani;
    }
}
