package com.canvearth.canvearth.server;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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
                RegisteredSketch newSketch = new RegisteredSketch(userToken, new Date(), sketchName); // TODO consider when timezone differs, or abusing current datetime
                sketchDatabaseReference.child(registeredKey).setValue(newSketch);
                DatabaseUtils.getSketchReference(registeredKey).putFile(file);
                Iterator iterator = pixelDataSquare.pixelDataIterator();
                while (iterator.hasNext()) {
                    PixelData pixelData = (PixelData) iterator.next();
                    DatabaseUtils.getSketchRootReference().child(pixelData.getFirebaseId())
                            .child(registeredKey).setValue(sketchName);
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
                final HashMap<String, String> registeredSketchKey = new HashMap<>();
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
                                        registeredSketchKey.put(keyDataSnapShot.getKey(), keyDataSnapShot.getValue(String.class));
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
                for (String key: registeredSketchKey.keySet()) {
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
                                                    returnList.add(new NearbySketch.Sketch(key, new Photo(uri), registeredSketchKey.get(key)));
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
        Assert.assertEquals(pixelDatas.get(0).zoom, Constants.REGISTRATION_ZOOM_LEVEL);
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
                for (String key: registeredSketchs.keySet()) {
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
                                                returnList.add(new NearbySketch.Sketch(key, new Photo(uri), registeredSketchs.get(key)));
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
                sketches = dataSnapshot.getValue(Map.class);
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
