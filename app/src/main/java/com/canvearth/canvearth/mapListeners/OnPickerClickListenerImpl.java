package com.canvearth.canvearth.mapListeners;

import android.view.View;

import com.canvearth.canvearth.R;
import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.client.Palette;
import com.canvearth.canvearth.utils.Constants;

public class OnPickerClickListenerImpl implements View.OnClickListener {
    public OnPickerClickListenerImpl() {
        super();
    }

    @Override
    public void onClick(View view) {

        //TODO: Needs to change configured palette color when zoom is changed.
        switch (view.getId()) {
            case R.id.redPicker:
                Palette.getInstance().setColor(Constants.RED_COLOR);
                break;
            case R.id.orangePicker:
                Palette.getInstance().setColor(Constants.ORANGE_COLOR);
                break;
            case R.id.yellowPicker:
                Palette.getInstance().setColor(Constants.YELLOW_COLOR);
                break;
            case R.id.greenPicker:
                Palette.getInstance().setColor(Constants.GREEN_COLOR);
                break;
            case R.id.bluePicker:
                Palette.getInstance().setColor(Constants.BLUE_COLOR);
                break;
            case R.id.indigoPicker:
                Palette.getInstance().setColor(Constants.INDIGO_COLOR);
                break;
            case R.id.purplePicker:
                Palette.getInstance().setColor(Constants.PURPLE_COLOR);
                break;
            case R.id.grid_visibility:
                GridManager.toggleVisibility();
                break;
            default:
                break;
        }
    }
}
