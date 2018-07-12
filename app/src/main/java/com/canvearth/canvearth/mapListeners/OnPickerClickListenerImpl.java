package com.canvearth.canvearth.mapListeners;

import android.view.View;

import com.canvearth.canvearth.R;
import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.client.Palette;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

public class OnPickerClickListenerImpl implements View.OnClickListener{
    private GoogleMap mMap;
    private boolean gridVisibility = true;

    public OnPickerClickListenerImpl(GoogleMap googleMap){
        super();
        this.mMap = googleMap;
    }

    @Override
    public void onClick(View view){
        CameraPosition location = mMap.getCameraPosition();
        double lat = location.target.latitude;
        double lng = location.target.longitude;
        Pixel pixel = PixelUtils.latlng2pix(lat, lng, Constants.LEAF_PIXEL_ZOOM_LEVEL);
        //TODO: Needs to change configured palette color when zoom is changed.
        switch(view.getId()){
            case R.id.redPicker:
                Palette.getInstance().setColor(Constants.RED_COLOR);
                pixel.draw(mMap);
                break;
            case R.id.orangePicker:
                Palette.getInstance().setColor(Constants.ORANGE_COLOR);
                pixel.draw(mMap);
                break;
            case R.id.yellowPicker:
                Palette.getInstance().setColor(Constants.YELLOW_COLOR);
                pixel.draw(mMap);
                break;
            case R.id.greenPicker:
                Palette.getInstance().setColor(Constants.GREEN_COLOR);
                pixel.draw(mMap);
                break;
            case R.id.bluePicker:
                Palette.getInstance().setColor(Constants.BLUE_COLOR);
                pixel.draw(mMap);
                break;
            case R.id.indigoPicker:
                Palette.getInstance().setColor(Constants.INDIGO_COLOR);
                pixel.draw(mMap);
                break;
            case R.id.purplePicker:
                Palette.getInstance().setColor(Constants.PURPLE_COLOR);
                pixel.draw(mMap);
                break;
            case R.id.grid_visibility:
                if (gridVisibility){
                    gridVisibility = false;
                    GridManager.cleanup();
                } else {
                    gridVisibility = true;
                    if (location.zoom >= Constants.GRID_SHOW_MIN_ZOOM_LEVEL && location.zoom <= Constants.GRID_SHOW_MAX_ZOOM_LEVEL) {
                        GridManager.draw(mMap, Math.round(location.zoom));
                    }
                }
                break;
            default: break;
        }
    }
}
