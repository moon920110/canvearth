package com.canvearth.canvearth;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.canvearth.canvearth.client.GridManager;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PermissionUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.InputStream;

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG="Maps";
    private GoogleMap mMap;
    private MapScaleView scaleView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

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
        setContentView(R.layout.activity_maps);

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

        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng center = new LatLng(41.385064, 2.173403);
        scaleView = findViewById(R.id.scaleView);

        // TODO: Enable tilt gesture when performance issue is resolved
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(center));
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    private void showToast(Context context, String text){
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL|Gravity.BOTTOM,
                0, 200);
        toast.show();
    }

    @Override
    public void onCameraMove() {
        CameraPosition cameraPosition = mMap.getCameraPosition();
        scaleView.update(cameraPosition.zoom, cameraPosition.target.latitude);
    }

    @Override
    public void onCameraIdle() {
        CameraPosition cameraPosition = mMap.getCameraPosition();
        showToast(this, "zoom: " + cameraPosition.zoom);
        scaleView.update(cameraPosition.zoom, cameraPosition.target.latitude);

        GridManager.cleanup();
        if (cameraPosition.zoom >= Constants.GRID_SHOW_MIN_ZOOM_LEVEL && cameraPosition.zoom <= Constants.GRID_SHOW_MAX_ZOOM_LEVEL) {
            GridManager.draw(mMap, Math.round(cameraPosition.zoom));
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
      
        Pixel pixel = PixelUtils.latlng2pix(lat, lng, Constants.LEAF_PIXEL_ZOOM_LEVEL);
        showToast(this, "Lat: " + location.getLatitude() + "\n" +
                "Lng: " + location.getLongitude() + "\n" +
                "Pix: " + pixel.data.x + ", " + pixel.data.y);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
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
}
