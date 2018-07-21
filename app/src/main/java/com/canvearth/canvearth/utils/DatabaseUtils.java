package com.canvearth.canvearth.utils;

import android.util.Log;

import com.canvearth.canvearth.authorization.UserInformation;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DatabaseUtils {
    private static String TAG = "DATABASEUTILS";

    public static DatabaseReference getPixelReference(String pixelId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        if (Configs.TESTING) {
            return database.child(Constants.FIREBASE_DEV_PREFIX).child(pixelId);
        } else {
            return database.child(Constants.FIREBASE_PROD_PREFIX).child(pixelId);
        }
    }

    public static void clearDev() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child(Constants.FIREBASE_DEV_PREFIX).removeValue();
    }

    public static StorageReference getBitmapReference(String pixelId) {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        if (Configs.TESTING) {
            return storage.child(Constants.FIREBASE_DEV_PREFIX).child(pixelId);
        } else {
            return storage.child(Constants.FIREBASE_PROD_PREFIX).child(pixelId);
        }
    }

    public static DatabaseReference getSketchPixelReference(String pixelId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        if (Configs.TESTING) {
            return database.child(Constants.FIREBASE_DEV_PREFIX).child("Sketches").child(pixelId);
        } else {
            return database.child(Constants.FIREBASE_PROD_PREFIX).child("Sketches").child(pixelId);
        }
    }

    public static DatabaseReference getSketchRootReference() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        if (Configs.TESTING) {
            return database.child(Constants.FIREBASE_DEV_PREFIX).child("Sketches");
        } else {
            return database.child(Constants.FIREBASE_PROD_PREFIX).child("Sketches");
        }
    }

    public static DatabaseReference getUserInfoReference() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        if (Configs.TESTING) {
            return database.child(Constants.FIREBASE_DEV_PREFIX).child("UserInfo");
        } else {
            return database.child(Constants.FIREBASE_PROD_PREFIX).child("UserInfo");
        }
    }

    public static DatabaseReference getMyInfoReference() {
        try {
            UserInformation userInformation = UserInformation.getInstance();
            String userToken = userInformation.getToken();
            return getUserInfoReference().child(userToken);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    public static StorageReference getSketchReference(String sketchId) {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        if (Configs.TESTING) {
            return storage.child(Constants.FIREBASE_DEV_PREFIX).child(sketchId);
        } else {
            return storage.child(Constants.FIREBASE_PROD_PREFIX).child(sketchId);
        }
    }
}
