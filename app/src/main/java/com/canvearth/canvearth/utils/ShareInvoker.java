package com.canvearth.canvearth.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

public class ShareInvoker {
    private final String TAG = "ShareInvoker";
    protected Activity mParentActivity;
    protected GoogleMap mMap;

    public ShareInvoker(Activity parentActivity, GoogleMap map) {
        this.mParentActivity = parentActivity;
        this.mMap = map;
    }

    public void shareMapSnapshot(Void... voids) {
        if (mParentActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    String bitmapPath = MediaStore.Images.Media.insertImage(mParentActivity.getContentResolver(), bitmap, "canvearthMapSnapshot", null);
                    Uri bitmapUri = Uri.parse(bitmapPath);

                    Log.d(TAG, "snapshot Ready");
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND)
                            .setType("image/*")
                            .putExtra(Intent.EXTRA_STREAM, bitmapUri);
                    mParentActivity.startActivity(sendIntent);
                }
            });
        }
        else {
            ActivityCompat.requestPermissions(mParentActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
}
