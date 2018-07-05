package com.canvearth.canvearth.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseUtils {
    public static DatabaseReference getPixelReference(String pixelId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        return database.child(pixelId);
    }
}
