package com.canvearth.canvearth.server;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.canvearth.canvearth.authorization.UserInformation;
import com.canvearth.canvearth.pixel.PixelData;
import com.canvearth.canvearth.pixel.PixelDataSquare;
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

//TODO delete registered sketch
public class SketchRegisterManager {
    private static final SketchRegisterManager ourInstance = new SketchRegisterManager();
    private static final String TAG = "SketchRegisterManager";

    public static SketchRegisterManager getInstance() {
        return ourInstance;
    }

    private SketchRegisterManager() {
    }

    private static class RegisterSketchAsyncTask extends AsyncTask<Pair<Uri, PixelDataSquare>, Void, Void> {
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
                }
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

    private static class GetRegisteredSketches extends AsyncTask<PixelData, Void, List<Pair<RegisteredSketch, Uri>>> {
        private Function<List<Pair<RegisteredSketch, Uri>>> callback;

        private GetRegisteredSketches(Function<List<Pair<RegisteredSketch, Uri>>> callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // TODO consider when duplicate registration occurs at same pixel data
        @Override
        protected List<Pair<RegisteredSketch, Uri>> doInBackground(PixelData[] params) {
            try {
                PixelData pixelData = params[0];
                final ArrayList<String> registeredSketchKey = new ArrayList<>();
                final CountDownLatch waitForFinish = new CountDownLatch(1);
                DatabaseUtils.getSketchPixelReference(pixelData.getFirebaseId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Iterator registeredKeyIterator = dataSnapshot.getChildren().iterator();
                                while (registeredKeyIterator.hasNext()) {
                                    DataSnapshot keyDataSnapShot = (DataSnapshot) registeredKeyIterator.next();
                                    registeredSketchKey.add(keyDataSnapShot.getKey());
                                    waitForFinish.countDown();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e(TAG, databaseError.getDetails());
                                waitForFinish.countDown();
                            }
                        });
                waitForFinish.await();
                final ArrayList<Pair<RegisteredSketch, Uri>> returnList = new ArrayList<>();
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
                                                    returnList.add(new Pair<>(registeredSketch, uri));
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
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(List<Pair<RegisteredSketch, Uri>> list) {
            callback.run(list);
        }
    }

    // get registered sketches inside pixelData
    public void getRegisteredSketches(PixelData pixelData, Function<List<Pair<RegisteredSketch, Uri>>> callback) {
        Assert.assertEquals(pixelData.zoom, Constants.RESGISTRATION_ZOOM_LEVEL);
        new GetRegisteredSketches(callback).execute(pixelData);
    }
}
