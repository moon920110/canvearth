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
import com.canvearth.canvearth.sketch.NearbySketch;
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

    private class GetSketchImage extends AsyncTask<NearbySketch.Sketch, String, NearbySketch.Sketch> {
        private Function<NearbySketch.Sketch> callback;

        private GetSketchImage(Function<NearbySketch.Sketch> callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected NearbySketch.Sketch doInBackground(NearbySketch.Sketch[] params) {
            try {
                NearbySketch.Sketch emptySketch = params[0];

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
        protected void onPostExecute(NearbySketch.Sketch sketch) {
            Log.i("GetSketchImage", "Post executing");
            callback.run(sketch);
        }
    }

    public void getSketchImage(NearbySketch.Sketch sketch, Function<NearbySketch.Sketch> callback) {
        new GetSketchImage(callback).execute(sketch);
    }

    // TODO too many concurrency control - may cause performance issues
    private class GetInterestingSketches extends AsyncTask<Void, String, List<NearbySketch.Sketch>> {
        private Function<List<NearbySketch.Sketch>> callback;

        private GetInterestingSketches(Function<List<NearbySketch.Sketch>> callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected List<NearbySketch.Sketch> doInBackground(Void[] params) {
            try {
                final Map<String, String> registeredSketchs = new HashMap<>();
                final CountDownLatch waitForFinish = new CountDownLatch(1);
                final DatabaseReference myInfoReference = DatabaseUtils.getMyInfoReference();
                if (myInfoReference == null) {
                    return null;
                }
                myInfoReference
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String, String> keys = (Map<String, String>) dataSnapshot.getValue();
                                registeredSketchs.putAll(keys);
                                waitForFinish.countDown();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, databaseError.getDetails());
                                waitForFinish.countDown();
                            }
                        });
                waitForFinish.await();
                publishProgress("got all keys");

                final ArrayList<NearbySketch.Sketch> returnList = new ArrayList<>();
                final Lock returnListLock = new ReentrantLock();
                final CountDownLatch waitForAllFinish = new CountDownLatch(registeredSketchs.size());
                for (String key : registeredSketchs.keySet()) {
                    DatabaseUtils.getSketchRootReference().child(key).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    RegisteredSketch registeredSketch = dataSnapshot.getValue(RegisteredSketch.class);
                                    if (registeredSketch == null) {
                                        waitForAllFinish.countDown();
                                        return;
                                    }
                                    DatabaseUtils.getSketchReference(key).getDownloadUrl().addOnSuccessListener(
                                            (Uri uri) -> {
                                                returnListLock.lock();
                                                String sketchName = registeredSketch.sketchName;
                                                PixelDataSquare boundingPixelDataSquare = registeredSketch.fbPixelDataSquare.toPixelDataSquare();
                                                returnList.add(new NearbySketch.Sketch(key, new Photo(uri),
                                                        sketchName, boundingPixelDataSquare));
                                                returnListLock.unlock();
                                                waitForAllFinish.countDown();
                                            }
                                    );
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, databaseError.getDetails());
                                    waitForAllFinish.countDown();
                                }
                            }
                    );
                }
                waitForAllFinish.await();
                return returnList;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Log.i("GetInterestingSketches", values[0]);
        }

        @Override
        protected void onPostExecute(List<NearbySketch.Sketch> list) {
            Log.i("GetInterestingSketches", "Post executing");
            callback.run(list);
        }
    }

    public void getInterestingSketches(Function<List<NearbySketch.Sketch>> callback) {
        new GetInterestingSketches(callback).execute();
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
