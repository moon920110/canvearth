package com.canvearth.canvearth.mapListeners;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.ScreenUtils;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;

public class OnMyLocationClickListenerImpl implements GoogleMap.OnMyLocationClickListener{
    private MapScaleView scaleView;
    private Context context;

    public OnMyLocationClickListenerImpl(Context context, MapScaleView scaleView) {
        super();
        this.scaleView = scaleView;
        this.context = context;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Pixel pixel = PixelUtils.latlng2pix(lat, lng, Constants.LEAF_PIXEL_ZOOM_LEVEL);
        ScreenUtils.showToast(context, "Lat: " + location.getLatitude() + "\n" +
                "Lng: " + location.getLongitude() + "\n" +
                "Pix: " + pixel.data.x + ", " + pixel.data.y);
    }
}
