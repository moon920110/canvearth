package com.canvearth.canvearth.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseUtils {
    public static DatabaseReference getPixelReference(String pixelId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        if (Configs.TESTING) {
            return database.child(Constants.FIREBASE_DEV_PREFIX).child(pixelId);
        } else {
            return database.child(Constants.FIREBASE_PROD_PREFIX).child(pixelId);
        }
    }
}
