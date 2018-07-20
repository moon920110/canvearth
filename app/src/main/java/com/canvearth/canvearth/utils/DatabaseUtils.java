package com.canvearth.canvearth.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DatabaseUtils {
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

    public static StorageReference getSketchReference(String sketchId) {
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        if (Configs.TESTING) {
            return storage.child(Constants.FIREBASE_DEV_PREFIX).child(sketchId);
        } else {
            return storage.child(Constants.FIREBASE_PROD_PREFIX).child(sketchId);
        }
    }
}
