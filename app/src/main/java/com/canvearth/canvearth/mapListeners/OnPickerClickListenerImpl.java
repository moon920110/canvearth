package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.R;
import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.client.Palette;
import com.canvearth.canvearth.client.VisibilityHandler;
import com.canvearth.canvearth.utils.Constants;

public class OnPickerClickListenerImpl implements View.OnClickListener {
    private MapsActivity activity;
    private Context context;
    private Palette palette = Palette.getInstance();

    public OnPickerClickListenerImpl(Context context, MapsActivity activity) {
        super();
        this.activity = activity;
        this.context = context;
    }

    @Override
    public void onClick(View view) {
        Button button = activity.findViewById(R.id.pickerBucketButton);
        int color;
        switch (view.getId()) {
            case R.id.redPicker:
                color = activity.getResources().getColor(R.color.red);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_red_button));
                break;
            case R.id.pinkPicker:
                color = activity.getResources().getColor(R.color.pink);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_pink_button));
                break;
            case R.id.purplePicker:
                color = activity.getResources().getColor(R.color.purple);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_purple_button));
                break;
            case R.id.deepPurplePicker:
                color = activity.getResources().getColor(R.color.deep_purple);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_deep_purple_button));
                break;
            case R.id.indigoPicker:
                color = activity.getResources().getColor(R.color.indigo);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_indigo_button));
                break;
            case R.id.bluePicker:
                color = activity.getResources().getColor(R.color.blue);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_blue_button));
                break;
            case R.id.lightBluePicker:
                color = activity.getResources().getColor(R.color.light_blue);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_light_blue_button));
                break;
            case R.id.cyanPicker:
                color = activity.getResources().getColor(R.color.cyan);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_cyan_button));
                break;
            case R.id.tealPicker:
                color = activity.getResources().getColor(R.color.teal);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_teal_button));
                break;
            case R.id.greenPicker:
                color = activity.getResources().getColor(R.color.green);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_green_button));
                break;
            case R.id.lightGreenPicker:
                color = activity.getResources().getColor(R.color.light_green);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_light_green_button));
                break;
            case R.id.limePicker:
                color = activity.getResources().getColor(R.color.lime);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_lime_button));
                break;
            case R.id.yellowPicker:
                color = activity.getResources().getColor(R.color.yellow);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_yellow_button));
                break;
            case R.id.amberPicker:
                color = activity.getResources().getColor(R.color.amber);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_amber_button));
                break;
            case R.id.orangePicker:
                color = activity.getResources().getColor(R.color.orange);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_orange_button));
                break;
            case R.id.deepOrangePicker:
                color = activity.getResources().getColor(R.color.deep_orange);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_deep_orange_button));
                break;
            case R.id.brownPicker:
                color = activity.getResources().getColor(R.color.brown);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_brown_button));
                break;
            case R.id.greyPicker:
                color = activity.getResources().getColor(R.color.grey);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_grey_button));
                break;
            case R.id.blueGreyPicker:
                color = activity.getResources().getColor(R.color.blue_grey);
                palette.setColor(color);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_blue_grey_button));
                break;
            case R.id.defaultColorPicker:
                palette.setColor(Constants.PALETTE_DEFAULT_COLOR);
                button.setBackground(ContextCompat.getDrawable(context, R.drawable.round_button));
                break;
            case R.id.grid_visibility:
                GridManager.toggleVisibility();
                break;
            default:
                break;
        }
        VisibilityHandler.handlePickerBucketButton(activity);
    }
}
