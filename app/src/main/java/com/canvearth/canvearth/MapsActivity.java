package com.canvearth.canvearth;

import com.canvearth.canvearth.databinding.ActivityMapsBinding;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.mapListeners.OnMapReadyCallbackImpl;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PermissionUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.ScreenUtils;
import com.canvearth.canvearth.utils.ShareInvoker;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;

import java.io.InputStream;

public class MapsActivity extends AppCompatActivity {
    public static GoogleMap Map;

    private static final String TAG = "Maps";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private boolean utilVisibility = false;
    private ActivityMapsBinding binding = null;

    protected class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public DownloadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps);
        binding.setMapsActivityHandler(this);

        String userName = getIntent().getExtras().getString("userName");
        Log.d(TAG, "userName: " + userName);

        String userPhotoUrlString = getIntent().getExtras().getString("userPhotoUrl");
        Log.d(TAG, "userPhotoUriString: " + userPhotoUrlString);

        ImageView photoImageView = findViewById(R.id.userPhotoImageView);
        DownloadImageTask photoImageDonwloadTask = new DownloadImageTask(photoImageView);
        photoImageDonwloadTask.execute(userPhotoUrlString);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        MapScaleView scaleView = (MapScaleView) findViewById(R.id.scaleView);
        mapFragment.getMapAsync(new OnMapReadyCallbackImpl(this, this, scaleView));

        LinearLayout utilButtonsLayout = findViewById(R.id.util_items);
        Button utilButton = findViewById(R.id.utilButton);
        utilButton.setOnClickListener((View v) -> {
            if (utilVisibility) {
                utilButtonsLayout.setVisibility(View.INVISIBLE);
                utilVisibility = false;
            } else {
                utilButtonsLayout.setVisibility(View.VISIBLE);
                utilVisibility = true;
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            PermissionUtils.enableMyLocation(this, this);
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            return;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void onClickShare() {
        new ShareInvoker(MapsActivity.this, Map).shareMapSnapshot();
    }

    public void onClickMyPage() {
        ScreenUtils.showToast(this, "Not Implemented Yet");
    }

    public void onClickAddSketch() {
        ScreenUtils.showToast(this, "Not Implemented Yet");
    }

    public void onClickShowSketch() {
        ScreenUtils.showToast(this, "Not Implemented Yet");
    }

    public void hidePaletteButton() {
        binding.utilButton.setVisibility(View.GONE);
    }

    public void showPaletteButton() {
        binding.utilButton.setVisibility(View.VISIBLE);
    }

    public void hideSketchButton() {
        binding.addSketchButton.setVisibility(View.GONE);
        binding.showSketchButton.setVisibility(View.GONE);
    }

    public void showSketchButton() {
        binding.addSketchButton.setVisibility(View.VISIBLE);
        binding.showSketchButton.setVisibility(View.VISIBLE);
    }
}
