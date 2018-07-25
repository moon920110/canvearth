package com.canvearth.canvearth.server;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.canvearth.canvearth.MapsActivity;
import com.canvearth.canvearth.client.Photo;
import com.canvearth.canvearth.pixel.Pixel;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.sketch.Sketch;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.DatabaseUtils;
import com.canvearth.canvearth.utils.MathUtils;
import com.canvearth.canvearth.utils.PixelUtils;
import com.canvearth.canvearth.utils.SphericalMercator;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import junit.framework.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BitmapDrawer {
    private final String TAG = "BitmapDrawer";
    private static final BitmapDrawer ourInstance = new BitmapDrawer();
    private Disposable drawBitmapDisposable = null;
    private Queue<GroundOverlay> groundOverlays = new LinkedList<>();

    public static BitmapDrawer getInstance() {
        return ourInstance;
    }

    private BitmapDrawer() {
    }

    public Disposable drawBitmap(int showingZoomLevel, CameraPosition currentPosition, Activity activity) {

        if (drawBitmapDisposable != null && !drawBitmapDisposable.isDisposed()) {
            drawBitmapDisposable.dispose();
        }
        while (!groundOverlays.isEmpty()) {
            groundOverlays.poll().remove();
        }

        final Consumer<Pair<LatLngBounds, Uri>> drawBitmap = new Consumer<Pair<LatLngBounds, Uri>>() {
            @Override
            public void accept(Pair<LatLngBounds, Uri> param) throws Exception {
                Glide.with(activity).asBitmap().load(param.second)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                // To prevent anti-alias
                                resource = Bitmap.createScaledBitmap(resource,
                                        resource.getWidth() * MathUtils.intPow(2, 7 - Constants.BITMAP_CACHE_RESOLUTION_FACTOR),
                                        resource.getHeight() * MathUtils.intPow(2, 7 - Constants.BITMAP_CACHE_RESOLUTION_FACTOR), false);

                                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resource);
                                GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions();
                                groundOverlayOptions.positionFromBounds(param.first);
                                groundOverlayOptions.image(bitmapDescriptor);
                                GroundOverlay groundOverlay = MapsActivity.Map.addGroundOverlay(groundOverlayOptions);
                                groundOverlays.add(groundOverlay);
                            }
                        });
            }
        };

        final io.reactivex.functions.Function<PixelData, Observable<PixelData>> getExistPixel
                = new io.reactivex.functions.Function<PixelData, Observable<PixelData>>() {
            @Override
            public Observable<PixelData> apply(final PixelData pixelData) {
                return Observable.create(new ObservableOnSubscribe<PixelData>() {
                    @Override
                    public void subscribe(ObservableEmitter<PixelData> emitter) throws Exception {
                        DatabaseUtils
                                .getBitmapExistReference().child(pixelData.getFirebaseId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Boolean exist = dataSnapshot.getValue(Boolean.class);
                                        if (exist != null) {
                                            emitter.onNext(pixelData);
                                        }
                                        emitter.onComplete();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        emitter.onComplete();
                                    }
                                });
                    }
                });
            }
        };

        final io.reactivex.functions.Function<PixelData, Observable<Pair<LatLngBounds, Uri>>> getBitmapUri
                = new io.reactivex.functions.Function<PixelData, Observable<Pair<LatLngBounds, Uri>>>() {
            @Override
            public Observable<Pair<LatLngBounds, Uri>> apply(final PixelData pixelData) {
                return Observable.create(new ObservableOnSubscribe<Pair<LatLngBounds, Uri>>() {
                    @Override
                    public void subscribe(ObservableEmitter<Pair<LatLngBounds, Uri>> emitter) throws Exception {
                        LatLngBounds latLngBounds = PixelUtils.pixdata2bbox(pixelData).toLatLngBounds();
                        DatabaseUtils
                                .getBitmapReference(pixelData.getFirebaseId())
                                .getDownloadUrl()
                                .addOnSuccessListener((Uri uri) -> {
                                    emitter.onNext(new Pair<>(latLngBounds, uri));
                                    Log.i(TAG, "exist " + pixelData.getFirebaseId());
                                    emitter.onComplete();
                                })
                                .addOnFailureListener((Exception e) -> {
                                    Log.i(TAG, "Not exist " + pixelData.getFirebaseId());
                                    emitter.onComplete();
                                });
                    }
                });
            }
        };

        final Projection projection = MapsActivity.Map.getProjection();
        final LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        final int bitmapTargetZoomLevel = showingZoomLevel - Constants.BITMAP_CACHE_RESOLUTION_FACTOR;
        final double pixSideLen = PixelUtils.latlng2bbox(currentPosition.target, bitmapTargetZoomLevel).getSideLength();

        drawBitmapDisposable = Observable.create((ObservableEmitter<PixelData> emitter) -> {

            double minY = -180 + pixSideLen * (int) (SphericalMercator.scaleLatitude(bounds.southwest.latitude) / pixSideLen) - 5 * (pixSideLen / 2);
            double minX = -180 + pixSideLen * (int) (SphericalMercator.scaleLongitude(bounds.southwest.longitude) / pixSideLen) - 5 * (pixSideLen / 2);
            double maxY = -180 + pixSideLen * (int) (SphericalMercator.scaleLatitude(bounds.northeast.latitude) / pixSideLen) + 5 * (pixSideLen / 2);
            double maxX = -180 + pixSideLen * (int) (SphericalMercator.scaleLongitude(bounds.northeast.longitude) / pixSideLen) + 5 * (pixSideLen / 2);

            for (double y = minY; y < maxY; y += pixSideLen) {
                if (minX <= maxX) {
                    for (double x = minX; x < maxX; x += pixSideLen) {
                        Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, bitmapTargetZoomLevel);
                        emitter.onNext(pixel.data);
                    }
                } else {
                    for (double x = -180; x < minX; x += pixSideLen) {
                        Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, bitmapTargetZoomLevel);
                        emitter.onNext(pixel.data);
                    }
                    for (double x = maxX; x < 180; x += pixSideLen) {
                        Pixel pixel = PixelUtils.latlng2pix(SphericalMercator.toLatitude(y), x, bitmapTargetZoomLevel);
                        emitter.onNext(pixel.data);
                    }
                }
            }
            emitter.onComplete();
        })
                .flatMap(getExistPixel)
                .flatMap(getBitmapUri)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(drawBitmap);

        return drawBitmapDisposable;
    }
}
