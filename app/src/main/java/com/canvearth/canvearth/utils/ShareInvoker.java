package com.canvearth.canvearth.utils;

import android.Manifest;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.canvearth.canvearth.SelectPhotoActivity;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.GoogleMap;

public class ShareInvoker {
    private final String TAG = "ShareInvoker";
    protected AppCompatActivity mParentActivity;
    protected GoogleMap mMap;

    public ShareInvoker(AppCompatActivity parentActivity, GoogleMap map) {
        this.mParentActivity = parentActivity;
        this.mMap = map;
    }

    public void shareMapSnapshot(Void... voids) {
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        final int requestCodePermission = 2000;
        PermissionUtils.requestPermission(mParentActivity, requestCodePermission, permissions[0], false);
        if (PermissionUtils.checkSelfPermissions(mParentActivity, permissions)) {
            mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    String bitmapPath = MediaStore.Images.Media.insertImage(mParentActivity.getContentResolver(), bitmap, "canvearthMapSnapshot", null);
                    Uri bitmapUri = Uri.parse(bitmapPath);

                    Log.d(TAG, "snapshot Ready");
                    SharePhoto photo = new SharePhoto.Builder()
                            .setBitmap(bitmap)
                            .build();
                    SharePhotoContent content = new SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build();
                    ShareDialog shareDialog = new ShareDialog(mParentActivity);
                    if (ShareDialog.canShow(SharePhotoContent.class)) {
                        shareDialog.show(mParentActivity, content);
                    }
                }
            });
        } else {
            PermissionUtils.requestPermission(mParentActivity, requestCodePermission, permissions[0], false);
            PermissionUtils.requestPermission(mParentActivity, requestCodePermission, permissions[1], false);
        }
    }
}
