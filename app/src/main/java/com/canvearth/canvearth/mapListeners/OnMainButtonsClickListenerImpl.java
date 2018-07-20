package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.canvearth.canvearth.R;
import com.canvearth.canvearth.MapsActivity;

public class OnMainButtonsClickListenerImpl implements View.OnClickListener{
    private MapsActivity activity;
    private Context context;
    private boolean menuVisibility;
    private boolean pickerBucketVisibility;
    public OnMainButtonsClickListenerImpl(Context context, MapsActivity activity) {
        super();
        this.activity = activity;
        this.context = context;
        this.menuVisibility = false;
        this.pickerBucketVisibility = false;
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.showMeneButton:
                LinearLayout menuLayout = activity.findViewById(R.id.menuLayout);
                if (menuVisibility){
                    menuLayout.setVisibility(View.INVISIBLE);
                    menuVisibility = false;
                } else {
                    menuLayout.setVisibility(View.VISIBLE);
                    menuVisibility = true;
                }
                break;
            case R.id.pickerBucketButton:
                LinearLayout pickerButtonsLayout = activity.findViewById(R.id.pickerBucket);
                if(pickerBucketVisibility){
                    pickerButtonsLayout.setVisibility(View.INVISIBLE);
                    pickerBucketVisibility = false;
                } else {
                    pickerButtonsLayout.setVisibility(View.VISIBLE);
                    pickerBucketVisibility = true;
                }
                break;
            default:
                break;
        }
    }
}
