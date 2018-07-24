package com.canvearth.canvearth.server;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.canvearth.canvearth.authorization.UserInformation;
import com.canvearth.canvearth.client.Photo;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.pixel.PixelDataSquare;
import com.canvearth.canvearth.sketch.Sketch;
import com.canvearth.canvearth.utils.Constants;
import com.canvearth.canvearth.utils.DatabaseUtils;
import com.canvearth.canvearth.utils.concurrency.Function;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import junit.framework.Assert;

import org.apache.commons.lang3.tuple.Triple;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

//TODO delete registered sketch
public class SketchRegisterManager {
    private static final SketchRegisterManager ourInstance = new SketchRegisterManager();
    private static final String TAG = "SketchRegisterManager";

    public static SketchRegisterManager getInstance() {
        return ourInstance;
    }

    private SketchRegisterManager() {
    }

    private class RegisterSketchAsyncTask extends AsyncTask<Triple<Uri, String, PixelDataSquare>, Void, Void> {
        private Function<Void> callback;

        private RegisterSketchAsyncTask(Function<Void> callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // TODO consider when duplicate registration occurs at same pixel data
        @Override
        protected Void doInBackground(Triple<Uri, String, PixelDataSquare>[] params) {
            Uri file = params[0].getLeft();
            String sketchName = params[0].getMiddle();
            PixelDataSquare pixelDataSquare = params[0].getRight();
            try {
                DatabaseReference sketchDatabaseReference = DatabaseUtils.getSketchRootReference();
                String registeredKey = sketchDatabaseReference.push().getKey();
                if (registeredKey == null) {
                    throw new Exception("couldn't push sketch");
                }
                UserInformation userInformation = UserInformation.getInstance();
                String userToken = userInformation.getToken();
                RegisteredSketch newSketch = new RegisteredSketch(userToken, new Date(), sketchName, pixelDataSquare.toFB()); // TODO consider when timezone differs, or abusing current datetime
                sketchDatabaseReference.child(registeredKey).setValue(newSketch);
                DatabaseUtils.getSketchReference(registeredKey).putFile(file);
                Iterator iterator = pixelDataSquare.pixelDataIterator();
                while (iterator.hasNext()) {
                    PixelData pixelData = (PixelData) iterator.next();
                    DatabaseUtils.getSketchRootReference().child(pixelData.getFirebaseId())
                            .child(registeredKey).setValue(newSketch);
                    Log.i("registring sketch", pixelData.x + ", " + pixelData.y + ", " + pixelData.zoom);
                }
                Log.e("registring sketch", "done");
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(Void object) {
            callback.run(null);
        }
    }

    public void registerSketchAsync(Uri file, String sketchName, PixelDataSquare pixelDataSquare, Function<Void> callback) {
        Assert.assertEquals(pixelDataSquare.zoom(), Constants.REGISTRATION_ZOOM_LEVEL);
        new RegisterSketchAsyncTask(callback).execute(Triple.of(file, sketchName, pixelDataSquare));
    }

    public Disposable processRegisteredSketchMetas(List<PixelData> pixelDatas, Consumer<Pair<String, RegisteredSketch>> onNext) {
        final io.reactivex.functions.Function<PixelData, Observable<Pair<String, RegisteredSketch>>> flatMapFunc
                = new io.reactivex.functions.Function<PixelData, Observable<Pair<String, RegisteredSketch>>>()
        {
            @Override
            public Observable<Pair<String, RegisteredSketch>> apply(final PixelData pixelData)
            {
                return Observable.create(new ObservableOnSubscribe<Pair<String, RegisteredSketch>>()
                {
                    @Override
                    public void subscribe(ObservableEmitter<Pair<String, RegisteredSketch>> emitter) throws Exception
                    {
                        String key = pixelData.getFirebaseId();
                        DatabaseUtils.getSketchPixelReference(key)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Iterator registeredKeyIterator = dataSnapshot.getChildren().iterator();
                                        while (registeredKeyIterator.hasNext()) {
                                            DataSnapshot keyDataSnapShot = (DataSnapshot) registeredKeyIterator.next();
                                            emitter.onNext(new Pair<>(keyDataSnapShot.getKey(), keyDataSnapShot.getValue(RegisteredSketch.class)));
                                        }
                                        emitter.onComplete();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(TAG, databaseError.getDetails());
                                        emitter.onComplete();
                                    }
                                });
                    }
                });
            }
        };

        return Observable.fromIterable(pixelDatas)
                .flatMap(flatMapFunc)
                .distinct((Pair<String, RegisteredSketch> pair) -> pair.first)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    private class GetSketchImage extends AsyncTask<Sketch, String, Sketch> {
        private Function<Sketch> callback;

        private GetSketchImage(Function<Sketch> callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected Sketch doInBackground(Sketch[] params) {
            try {
                Sketch emptySketch = params[0];

                final CountDownLatch waitForAllFinish = new CountDownLatch(1);
                DatabaseUtils.getSketchReference(emptySketch.id).getDownloadUrl().addOnSuccessListener(
                        (Uri uri) -> {
                            emptySketch.photo = new Photo(uri);
                            waitForAllFinish.countDown();
                        }
                );
                waitForAllFinish.await();
                return emptySketch;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.i("GetSketchImage", values[0]);
        }

        @Override
        protected void onPostExecute(Sketch sketch) {
            Log.i("GetSketchImage", "Post executing");
            callback.run(sketch);
        }
    }

    public void getSketchImage(Sketch sketch, Function<Sketch> callback) {
        new GetSketchImage(callback).execute(sketch);
    }

    public Disposable processInterestingSketchesMetas(Consumer<Pair<String, RegisteredSketch>> onNext) {
        final io.reactivex.functions.Function<String, Observable<Pair<String, RegisteredSketch>>> getRegistedSketch
                = new io.reactivex.functions.Function<String, Observable<Pair<String, RegisteredSketch>>>()
        {
            @Override
            public Observable<Pair<String, RegisteredSketch>> apply(final String key)
            {
                return Observable.create(new ObservableOnSubscribe<Pair<String, RegisteredSketch>>()
                {
                    @Override
                    public void subscribe(ObservableEmitter<Pair<String, RegisteredSketch>> emitter) throws Exception
                    {
                        DatabaseUtils.getSketchPixelReference(key)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        RegisteredSketch registeredSketch = dataSnapshot.getValue(RegisteredSketch.class);
                                        if (registeredSketch != null)
                                            emitter.onNext(new Pair<>(key, registeredSketch));
                                        emitter.onComplete();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(TAG, databaseError.getDetails());
                                        emitter.onComplete();
                                    }
                                });
                    }
                });
            }
        };

        return Observable.create((ObservableEmitter<String> emitter) -> {
            final DatabaseReference myInfoReference = DatabaseUtils.getMyInfoReference();
            if (myInfoReference == null) {
                emitter.onComplete();
                return;
            }
            myInfoReference
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Map<String, String> keys = (Map<String, String>) dataSnapshot.getValue();
                            if (keys == null || keys.isEmpty()) {
                                emitter.onComplete();
                                return;
                            }
                            for (String key : keys.keySet()) {
                                emitter.onNext(key);
                            }
                            emitter.onComplete();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e(TAG, databaseError.getDetails());
                            emitter.onComplete();
                        }
                    });
        }).flatMap(getRegistedSketch)
                .distinct((Pair<String, RegisteredSketch> pair) -> pair.first)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    public void addInterestingSketch(String key, String name) {
        DatabaseUtils.getMyInfoReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> sketches;
                sketches = (Map<String, String>) dataSnapshot.getValue();
                if (sketches == null) {
                    sketches = new HashMap<>();
                }
                sketches.put(key, name);
                DatabaseUtils.getMyInfoReference().setValue(sketches);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "add canceled " + databaseError.getDetails());
            }
        });
    }
}
