package com.canvearth.canvearth.server;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import junit.framework.Assert;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//TODO delete registered sketch
public class SketchRegisterManager {
    private static final SketchRegisterManager ourInstance = new SketchRegisterManager();
    private static final String TAG = "SketchRegisterManager";

    public static SketchRegisterManager getInstance() {
        return ourInstance;
    }

    private SketchRegisterManager() {
    }

    private class RegisterSketchAsyncTask extends AsyncTask<Pair<Uri, PixelDataSquare>, Void, Void> {
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
        protected Void doInBackground(Pair<Uri, PixelDataSquare>[] params) {
            Uri file = params[0].first;
            PixelDataSquare pixelDataSquare = params[0].second;
            try {
                DatabaseReference sketchDatabaseReference = DatabaseUtils.getSketchRootReference();
                String registeredKey = sketchDatabaseReference.push().getKey();
                if (registeredKey == null) {
                    throw new Exception("couldn't push sketch");
                }
                UserInformation userInformation = UserInformation.getInstance();
                String userToken = userInformation.getToken();
                RegisteredSketch newSketch = new RegisteredSketch(userToken, new Date()); // TODO consider when timezone differs, or abusing current datetime
                sketchDatabaseReference.child(registeredKey).setValue(newSketch);
                DatabaseUtils.getSketchReference(registeredKey).putFile(file);
                Iterator iterator = pixelDataSquare.pixelDataIterator();
                while (iterator.hasNext()) {
                    PixelData pixelData = (PixelData) iterator.next();
                    DatabaseUtils.getSketchRootReference().child(pixelData.getFirebaseId())
                            .child(registeredKey).setValue(registeredKey);
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

    public void registerSketchAsync(Uri file, PixelDataSquare pixelDataSquare, Function<Void> callback) {
        Assert.assertEquals(pixelDataSquare.zoom(), Constants.RESGISTRATION_ZOOM_LEVEL);
        new RegisterSketchAsyncTask(callback).execute(new Pair<>(file, pixelDataSquare));
    }

    // TODO too many concurrency control - may cause performance issues
    private class GetRegisteredSketches extends AsyncTask<List<PixelData>, String, List<NearbySketch.Sketch>> {
        private Function<List<NearbySketch.Sketch>> callback;

        private GetRegisteredSketches(Function<List<NearbySketch.Sketch>> callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected List<NearbySketch.Sketch> doInBackground(List<PixelData>[] params) {
            try {
                List<PixelData> pixelDatas = params[0];
                final ArrayList<String> registeredSketchKey = new ArrayList<>();
                final CountDownLatch waitForFinish = new CountDownLatch(pixelDatas.size());
                final Lock registeredSketchKeyLock = new ReentrantLock();
                for (PixelData pixelData: pixelDatas) {
                    DatabaseUtils.getSketchPixelReference(pixelData.getFirebaseId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Iterator registeredKeyIterator = dataSnapshot.getChildren().iterator();
                                    while (registeredKeyIterator.hasNext()) {
                                        DataSnapshot keyDataSnapShot = (DataSnapshot) registeredKeyIterator.next();
                                        registeredSketchKeyLock.lock();
                                        registeredSketchKey.add(keyDataSnapShot.getKey());
                                        registeredSketchKeyLock.unlock();
                                    }
                                    waitForFinish.countDown();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, databaseError.getDetails());
                                    waitForFinish.countDown();
                                }
                            });
                }
                waitForFinish.await();
                publishProgress("got all keys");
                final ArrayList<NearbySketch.Sketch> returnList = new ArrayList<>();
                final Lock returnListLock = new ReentrantLock();
                final CountDownLatch waitForAllFinish = new CountDownLatch(registeredSketchKey.size());
                for (String key: registeredSketchKey) {
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
                                                (Uri uri)->{
                                                    returnListLock.lock();
                                                    returnList.add(new NearbySketch.Sketch(key, new Photo(uri), key));
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
            Log.i("GetRegisteredSketches", values[0]);
        }

        @Override
        protected void onPostExecute(List<NearbySketch.Sketch> list) {
            Log.i("GetRegisteredSketches", "Post executing");
            callback.run(list);
        }
    }

    // get registered sketches inside pixelData
    public void getRegisteredSketches(List<PixelData> pixelDatas, Function<List<NearbySketch.Sketch>> callback) {
        Assert.assertEquals(pixelDatas.get(0).zoom, Constants.RESGISTRATION_ZOOM_LEVEL);
        new GetRegisteredSketches(callback).execute(pixelDatas);
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
                final ArrayList<String> registeredSketchKey = new ArrayList<>();
                final CountDownLatch waitForFinish = new CountDownLatch(1);
                final DatabaseReference myInfoReference = DatabaseUtils.getMyInfoReference();
                if (myInfoReference == null) {
                    return null;
                }
                myInfoReference
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                List<String> keys = (List<String>) dataSnapshot.getValue();
                                registeredSketchKey.addAll(keys);
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
                final CountDownLatch waitForAllFinish = new CountDownLatch(registeredSketchKey.size());
                for (String key: registeredSketchKey) {
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
                                            (Uri uri)->{
                                                returnListLock.lock();
                                                returnList.add(new NearbySketch.Sketch(key, new Photo(uri), key));
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

    public void addInterestingSketch(String key) {
        DatabaseUtils.getMyInfoReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> keys;
                keys = dataSnapshot.getValue(List.class);
                if (keys == null) {
                    keys = new ArrayList<>();
                }
                keys.add(key);
                DatabaseUtils.getMyInfoReference().setValue(keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "add canceled " + databaseError.getDetails());
            }
        });
    }
}
