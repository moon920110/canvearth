package com.canvearth.canvearth;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.canvearth.canvearth.client.Palette;
import com.canvearth.canvearth.client.PaletteAdapter;
import com.canvearth.canvearth.client.Photo;
import com.canvearth.canvearth.client.SketchPlacerFragment;
import com.canvearth.canvearth.databinding.ActivityMapsBinding;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.GridView;
import android.net.Uri;

import com.canvearth.canvearth.mapListeners.OnMapReadyCallbackImpl;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.pixel.PixelDataSquare;
import com.canvearth.canvearth.server.RegisteredSketch;
import com.canvearth.canvearth.server.SketchRegisterManager;
import com.canvearth.canvearth.sketch.Sketch;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.PermissionUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.ScreenUtils;
import com.canvearth.canvearth.utils.ShareInvoker;
import com.canvearth.canvearth.utils.VisibilityUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class MapsActivity extends AppCompatActivity
        implements SketchShowFragment.OnSketchShowFragmentInteractionListener, MySketchFragment.OnMySketchFragmentInteractionListener {
    public static GoogleMap Map;
    public static ContentResolver contentResolver;
    public static String PACKAGE_NAME;

    private static final String TAG = "Maps";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_SELECT_PHOTO = 2;
    private boolean mPermissionDenied = false;
    private ActivityMapsBinding binding = null;
    private Fragment addSketchFragment = null;
    private Drawer navigationDrawer = null;

    // Used in showing nearby sketches
    private GroundOverlay mGroundOverlay = null;
    private Sketch mSeeingNearbySketch = null;
    private Disposable mDisposableNearbySketch = null;

    // Used in my interesting sketches
    private Sketch mSeeingMySketch = null;
    private LatLng mLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Disposable mDisposableMySketch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = this.getPackageName();
        contentResolver = this.getContentResolver();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps);
        binding.setMapsActivityHandler(this);

        addNavigationDrawer();
        setupPalette();
        findViewById(R.id.sketch_view).setVisibility(View.INVISIBLE);
        findViewById(R.id.my_sketch).setVisibility(View.INVISIBLE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallbackImpl(this, this, mapFragment));
        findViewById(R.id.sketch_view).setVisibility(View.GONE);
        findViewById(R.id.my_sketch).setVisibility(View.GONE);

        GridView gridview = findViewById(R.id.palette);
        PaletteAdapter paletteAdapter = new PaletteAdapter(this);
        gridview.setAdapter(paletteAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                int color = MapsActivity.this.getResources().getColor(paletteAdapter.paletteColors[position]);
                Palette.getInstance().setColor(color);

                Button brushColor = MapsActivity.this.findViewById(R.id.brushColor);
                GradientDrawable drawable = (GradientDrawable) brushColor.getBackground();
                drawable.setColor(color);
                VisibilityUtils.toggleViewVisibility(gridview);
            }
        });

        if (mDisposableNearbySketch != null && mDisposableNearbySketch.isDisposed() == false) {
            mDisposableNearbySketch.dispose();
            mDisposableNearbySketch = null;
        }
        if (mDisposableMySketch != null && mDisposableMySketch.isDisposed() == false) {
            mDisposableMySketch.dispose();
            mDisposableMySketch = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposableNearbySketch != null && mDisposableNearbySketch.isDisposed() == false) {
            mDisposableNearbySketch.dispose();
            mDisposableNearbySketch = null;
        }
        if (mDisposableMySketch != null && mDisposableMySketch.isDisposed() == false) {
            mDisposableMySketch.dispose();
            mDisposableMySketch = null;
        }

    }


    public void locationReady() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {


            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    public void requestLocationUpdate() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT <= 23) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                mLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());


            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
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

    public void addSketchConfirm(Rect bound, String sketchName, Photo photo) {
        PixelDataSquare pixelDataSquare = PixelUtils.getPixelDataSquareFromBound(Map, bound, Constants.REGISTRATION_ZOOM_LEVEL);
        SketchRegisterManager.getInstance().registerSketchAsync(photo.getUri(), sketchName, pixelDataSquare, (obj) -> {
            Log.i(TAG, "Add Sketch Finished");
        });
        endFragment(addSketchFragment);
    }

    public void addSketchCancel() {
        endFragment(addSketchFragment);
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

    public void onClickStarredSketches() {
        MySketchFragment fragment = (MySketchFragment) getFragmentManager().findFragmentById(R.id.my_sketch);
        processMySketches(fragment);
        findViewById(R.id.my_sketch).setVisibility(View.VISIBLE);
        hideAllComponents();
    }

    public void onClickShowSketch() {
        SketchShowFragment fragment = (SketchShowFragment) getFragmentManager().findFragmentById(R.id.sketch_view);
        processNearbySketches(fragment);
        findViewById(R.id.sketch_view).setVisibility(View.VISIBLE);
        hideAllComponents();
    }

    public void onClickSignout() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        LoginManager.getInstance().logOut();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void onClickAddSketch() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };
        final int requestCodePermission = 2000;
        if (PermissionUtils.checkSelfPermissions(this, permissions)) {
            Map.animateCamera(CameraUpdateFactory.zoomTo(Constants.REGISTRATION_ZOOM_LEVEL));
            final Intent intent = SelectPhotoActivity.createIntent(this);
            startActivityForResult(intent, REQUEST_SELECT_PHOTO);
            return;
        }
        PermissionUtils.requestPermission(this, requestCodePermission, permissions[0], false);
        if (PermissionUtils.checkSelfPermissions(this, permissions)) {
            Map.animateCamera(CameraUpdateFactory.zoomTo(Constants.REGISTRATION_ZOOM_LEVEL));
            final Intent intent = SelectPhotoActivity.createIntent(this);
            startActivityForResult(intent, REQUEST_SELECT_PHOTO);
        } else {
            ScreenUtils.showToast(this, "You have to allow permissions!");
        }
    }

    public void showAllComponents() {
        findViewById(R.id.all_components).setVisibility(View.VISIBLE);
    }

    public void hideAllComponents() {
        findViewById(R.id.all_components).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSketchShowFragmentInteraction(Sketch sketch) {
        CameraUpdateFactory.zoomTo(Constants.REGISTRATION_ZOOM_LEVEL);

        Glide.with(this).asBitmap().load(sketch.photo.getUri())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        if (mGroundOverlay != null) {
                            mGroundOverlay.remove();
                        }
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resource);
                        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
                        groundOverlayOptions.positionFromBounds(sketch.pixelDataSquare.getLatLngBounds());
                        groundOverlayOptions.image(bitmapDescriptor);
                        groundOverlayOptions.transparency(0.5f);
                        mGroundOverlay = Map.addGroundOverlay(groundOverlayOptions);
                        mSeeingNearbySketch = sketch;
                    }
                });
        Log.i(TAG, "Added Interesting Sketch");
    }

    public void addSelectedToMyInterest() {
        SketchRegisterManager.getInstance().addInterestingSketch(mSeeingNearbySketch.id, mSeeingNearbySketch.name);
        mGroundOverlay.remove();
        mGroundOverlay = null;
        mSeeingNearbySketch = null;
        showAllComponents();
    }

    public void detachSelectedShowingSketch() {
        mGroundOverlay.remove();
        mGroundOverlay = null;
        mSeeingNearbySketch = null;
    }

    /////////
    // My Interest related methods
    /////////

    @Override
    public void onMySketchFragmentInteraction(Sketch sketch) {
        binding.mysketchThumbnailContainer.setVisibility(View.VISIBLE);
        mSeeingMySketch = sketch;
        Glide.with(this).asBitmap().load(sketch.photo.getUri())
                .into(binding.mysketchThumbnail);
        Log.i(TAG, "Showing my sketch");
    }

    public void onClickHideInterestThumbnail() {
        binding.mysketchThumbnailContainer.setVisibility(View.INVISIBLE);
    }

    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    public boolean isInSeeingSketch() {
        LatLngBounds bounds = mSeeingMySketch.pixelDataSquare.getLatLngBounds();
        return bounds.contains(mLocation);
    }

    public double getXFractionInSeeingSketch() {
        LatLngBounds bounds = mSeeingMySketch.pixelDataSquare.getLatLngBounds();
        double leftLng = bounds.southwest.longitude;
        double rightLng = bounds.northeast.longitude;
        return (mLocation.longitude - leftLng) / (rightLng - leftLng);
    }

    public double getYFractionInSeeingSketch() {
        LatLngBounds bounds = mSeeingMySketch.pixelDataSquare.getLatLngBounds();
        double bottomLat = bounds.southwest.latitude;
        double topLat = bounds.northeast.latitude;
        return (mLocation.latitude - bottomLat) / (topLat - bottomLat);
    }

    private void addNavigationDrawer() {

        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }
        });


        Bundle bundle = getIntent().getExtras();
        String userName = bundle.getString(Constants.userName);
        String userPhotoUrlString = bundle.getString(Constants.userPhotoUrl);
        String userEmail = bundle.getString(Constants.userEmail);

        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem()
                                .withName(userName)
                                .withTextColor(getResources().getColor(R.color.material_drawer_primary_text))
                                .withEmail(userEmail)
                                .withTextColor(getResources().getColor(R.color.material_drawer_primary_text))
                                .withIcon(Uri.decode(userPhotoUrlString)))
                .withSelectionListEnabledForSingleProfile(false)
                .build();


        //if you want to update the items at a later time it is recommended to keep it in a variable
        SecondaryDrawerItem share = new SecondaryDrawerItem()
                .withIdentifier(Constants.DRAWER_ID_SHARE)
                .withIcon(FontAwesome.Icon.faw_facebook)
                .withName(Constants.DRAWER_TEXT_SHARE);
        SecondaryDrawerItem starredSketches = new SecondaryDrawerItem()
                .withIdentifier(Constants.DRAWER_ID_STARRED_SKETCHES)
                .withIcon(FontAwesome.Icon.faw_star)
                .withName(Constants.DRAWER_TEXT_STARRED_SKETCHES);
        SecondaryDrawerItem nearbySketches = new SecondaryDrawerItem()
                .withIdentifier(Constants.DRAWER_ID_NEARBY_SKETCHES)
                .withIcon(FontAwesome.Icon.faw_images)
                .withName(Constants.DRAWER_TEXT_NEARBY_SKETCHES);
        SecondaryDrawerItem logout = new SecondaryDrawerItem()
                .withIdentifier(Constants.DRAWER_ID_SIGNOUT)
                .withIcon(FontAwesome.Icon.faw_sign_out_alt)
                .withName(Constants.DRAWER_TEXT_SIGNOUT);


        //create the navigationDrawer and remember the `Drawer` result object
        navigationDrawer = new DrawerBuilder()
                .withAccountHeader(header)
                .withActivity(this)
                .withSelectedItem(-1)
                .addDrawerItems(
                        share,
                        starredSketches,
                        nearbySketches,
                        logout
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    switch (position) {
                        case Constants.DRAWER_ID_SHARE:
                            onClickShare();
                            break;
                        case Constants.DRAWER_ID_STARRED_SKETCHES:
                            onClickStarredSketches();
                            break;
                        case Constants.DRAWER_ID_NEARBY_SKETCHES:
                            onClickShowSketch();
                            break;
                        case Constants.DRAWER_ID_SIGNOUT:
                            onClickSignout();
                            break;
                        default:
                            break;
                    }
                    navigationDrawer.setSelection(-1);
                    navigationDrawer.closeDrawer();
                    return true;
                })
                .build();

        Button menu = findViewById(R.id.showMenuButton);
        menu.setOnClickListener(view -> {
            navigationDrawer.openDrawer();
        });
    }

    private void processNearbySketches(SketchShowFragment fragment) {
        Projection projection = Map.getProjection();
        LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        PixelData northeastPixelData = PixelUtils.latlng2pix(
                bounds.northeast, Constants.REGISTRATION_ZOOM_LEVEL).data;
        PixelData southwestPixelData = PixelUtils.latlng2pix(
                bounds.southwest, Constants.REGISTRATION_ZOOM_LEVEL).data;
        ArrayList<PixelData> pixelDatas = new ArrayList<>();
        for (int y = northeastPixelData.y; y <= southwestPixelData.y; y++) {
            for (int x = southwestPixelData.x; x <= northeastPixelData.x; x++) {
                pixelDatas.add(new PixelData(x, y, Constants.REGISTRATION_ZOOM_LEVEL));
            }
        }

        final Observer<Pair<String, RegisteredSketch>> observer = new Observer<Pair<String, RegisteredSketch>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposableNearbySketch = d;
            }

            @Override
            public void onNext(Pair<String, RegisteredSketch> stringRegisteredSketchPair) {
                String key = stringRegisteredSketchPair.first;
                RegisteredSketch registeredSketch = stringRegisteredSketchPair.second;
                Sketch emptySketch = new Sketch(key, new Photo(), registeredSketch.sketchName, registeredSketch.fbPixelDataSquare.toPixelDataSquare());
                fragment.removeProgressForAll();
                int idx = fragment.addSketch(emptySketch);
                if (idx == -1) {
                    return;
                }
                SketchRegisterManager.getInstance().getSketchImage(emptySketch, (Sketch sketch) -> {
                    fragment.changeSketch(idx, sketch);
                });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                fragment.removeProgressForAll();
            }
        };

        SketchRegisterManager.getInstance()
                .processRegisteredSketchMetas(pixelDatas, observer);
    }

    private void processMySketches(MySketchFragment fragment) {

        Observer<Pair<String, RegisteredSketch>> observer = new Observer<Pair<String, RegisteredSketch>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mDisposableMySketch = d;
            }

            @Override
            public void onNext(Pair<String, RegisteredSketch> stringRegisteredSketchPair) {
                String key = stringRegisteredSketchPair.first;
                RegisteredSketch registeredSketch = stringRegisteredSketchPair.second;
                Sketch emptySketch = new Sketch(key, new Photo(), registeredSketch.sketchName, registeredSketch.fbPixelDataSquare.toPixelDataSquare());
                fragment.removeProgressForAll();
                int idx = fragment.addSketch(emptySketch);
                if (idx == -1) {
                    return;
                }
                SketchRegisterManager.getInstance().getSketchImage(emptySketch, (Sketch sketch) -> {
                    fragment.changeSketch(idx, sketch);
                });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                fragment.removeProgressForAll();
            }
        };
        SketchRegisterManager.getInstance().processInterestingSketchesMetas(observer);
    }

    private void startFragment(int id, Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(id, fragment);
        fragmentTransaction.commit();
        hideAllComponents();
    }

    private void endFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.remove(fragment);
        addSketchFragment = null;
        fragmentTransaction.commit();
        showAllComponents();
    }

    private void setupPalette() {
        GridView paletteGridView = findViewById(R.id.palette);
        PaletteAdapter paletteAdapter = new PaletteAdapter(this);
        paletteGridView.setAdapter(paletteAdapter);

        paletteGridView.setOnItemClickListener((parent, v, position, id) -> {
            int color = MapsActivity.this.getResources().getColor(paletteAdapter.paletteColors[position]);
            Palette.getInstance().setColor(color);

            Button brushColor = MapsActivity.this.findViewById(R.id.brushColor);
            GradientDrawable drawable = (GradientDrawable) brushColor.getBackground();
            drawable.setColor(color);
            VisibilityUtils.toggleViewVisibility(paletteGridView);
        });
        findViewById(R.id.brushButton).setOnClickListener(view -> {
            VisibilityUtils.toggleViewVisibility(paletteGridView);
        });
    }
}
