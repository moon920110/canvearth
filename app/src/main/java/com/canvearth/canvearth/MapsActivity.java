package com.canvearth.canvearth;

import com.canvearth.canvearth.client.Photo;
import com.canvearth.canvearth.client.SketchPlacerFragment;
import com.canvearth.canvearth.client.VisibilityHandler;
import com.canvearth.canvearth.databinding.ActivityMapsBinding;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.canvearth.canvearth.mapListeners.OnMapReadyCallbackImpl;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.pixel.PixelDataSquare;
import com.canvearth.canvearth.server.FBPixelManager;
import com.canvearth.canvearth.server.SketchRegisterManager;
import com.canvearth.canvearth.server.WatchingPixel;
import com.canvearth.canvearth.sketch.NearbySketch;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PermissionUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.ScreenUtils;
import com.canvearth.canvearth.utils.ShareInvoker;
import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MapsActivity extends AppCompatActivity
        implements SketchShowFragment.OnSketchShowFragmentInteractionListener {
    public static GoogleMap Map;
    public static ContentResolver contentResolver;
    public static String PACKAGE_NAME;

    private static final String TAG = "Maps";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_SELECT_PHOTO = 2;
    private boolean mPermissionDenied = false;
    private ActivityMapsBinding binding = null;
    private Fragment addSketchFragment = null;

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
        PACKAGE_NAME = this.getPackageName();
        contentResolver = this.getContentResolver();
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

        MapScaleView scaleView = findViewById(R.id.scaleView);
        mapFragment.getMapAsync(new OnMapReadyCallbackImpl(this, this, scaleView));
        findViewById(R.id.sketch_view).setVisibility(View.GONE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Log.e(TAG, "something wrong");
            return;
        }

        if (requestCode != REQUEST_SELECT_PHOTO) {
            Log.e(TAG, "something wrong");
            return;
        }
        Photo photo = data.getParcelableExtra("photo");
        addSketchFragment = SketchPlacerFragment.newInstance(photo);
        startFragment(R.id.sketch_placer, addSketchFragment);
    }

    public void addSketchFinish(Rect bound, Photo photo) {
        PixelDataSquare pixelDataSquare = PixelUtils.getPixelDataSquareFromBound(Map, bound, Constants.RESGISTRATION_ZOOM_LEVEL);
        SketchRegisterManager.getInstance().registerSketchAsync(photo.getUri(), pixelDataSquare, (obj)->{
            Log.i(TAG, "Add Sketch Finished");
        });
        endFragment(addSketchFragment);
        addSketchFragment = null;
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public  void onClickMenuCancel() {
        VisibilityHandler.handleMenuButton(this);
    }

    public void onClickShare() {
        new ShareInvoker(MapsActivity.this, Map).shareMapSnapshot();
    }

    public void onClickMyPage() {
        ScreenUtils.showToast(this, "Not Implemented Yet");
    }

    public void onClickAddSketch() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        final int requestCodePermission = 2000;
        if (PermissionUtils.checkSelfPermissions(this, permissions)) {
            final Intent intent = SelectPhotoActivity.createIntent(this);
            startActivityForResult(intent, REQUEST_SELECT_PHOTO);
            return;
        }
        PermissionUtils.requestPermission(this, requestCodePermission, permissions[0], false);
    }

    public void onClickShowSketch() {
        SketchShowFragment fragment = (SketchShowFragment) getFragmentManager().findFragmentById(R.id.sketch_view);
        processNearbySketches(fragment);
        findViewById(R.id.sketch_view).setVisibility(View.VISIBLE);
        findViewById(R.id.all_components).setVisibility(View.GONE);
    }

    public void showAllComponents() {
        findViewById(R.id.all_components).setVisibility(View.VISIBLE);
    }
    @Override
    public void onSketchShowFragmentInteraction(NearbySketch.Sketch sketch) {
        // TODO
    }

    private void processNearbySketches(SketchShowFragment fragment) {
        Projection projection = Map.getProjection();
        LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        PixelData northeastPixelData = PixelUtils.latlng2pix(
                bounds.northeast, Constants.RESGISTRATION_ZOOM_LEVEL).data;
        PixelData southwestPixelData = PixelUtils.latlng2pix(
                bounds.southwest, Constants.RESGISTRATION_ZOOM_LEVEL).data;
        ArrayList<PixelData> pixelDatas = new ArrayList<>();
        for (int y = northeastPixelData.y; y <= southwestPixelData.y; y++) {
            for (int x = southwestPixelData.x; x <= northeastPixelData.x; x++) {
                pixelDatas.add(new PixelData(x, y, Constants.RESGISTRATION_ZOOM_LEVEL));
            }
        }
        SketchRegisterManager.getInstance().getRegisteredSketches(pixelDatas,
            (List<Pair<Photo, String>> list) -> {
                ArrayList<NearbySketch.Sketch> sketches = new ArrayList<>();
                for (Pair<Photo, String> pair: list) {
                    sketches.add(new NearbySketch.Sketch(pair.second, pair.first, pair.second));
                }
                fragment.setSketches(sketches);
                Log.i(TAG, "Done receiving sketches");
            });
    }

    private void startFragment(int id, Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(id, fragment);
        fragmentTransaction.commit();
        findViewById(R.id.all_components).setVisibility(View.GONE);
    }

    private void endFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.remove(fragment);
        addSketchFragment = null;
        fragmentTransaction.commit();
        findViewById(R.id.all_components).setVisibility(View.VISIBLE);
    }
}
