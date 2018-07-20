package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.view.View;

import com.canvearth.canvearth.R;
import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.client.VisibilityHandler;

public class OnMainButtonsClickListenerImpl implements View.OnClickListener{
    private MapsActivity activity;

    public OnMainButtonsClickListenerImpl(Context context, MapsActivity activity) {
        super();
        this.activity = activity;
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.showMeneButton:
                VisibilityHandler.handleMenuButton(activity);
                break;
            case R.id.pickerBucketButton:
                VisibilityHandler.handlePickerBucketButton(activity);
                break;
            default:
                break;
        }
    }
}
