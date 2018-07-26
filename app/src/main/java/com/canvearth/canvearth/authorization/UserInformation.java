package com.canvearth.canvearth.authorization;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.canvearth.canvearth.utils.Configs;
import com.canvearth.canvearth.utils.DatabaseUtils;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class UserInformation {
    private static final String TAG = "UserInformation";
    private static final UserInformation ourInstance = new UserInformation();
    private String uid;

    public static UserInformation getInstance() {
        return ourInstance;
    }

    private UserInformation() {
    }



    public String getToken() throws InterruptedException {
        if (Configs.TESTING) {
            return "TEST_USER_TOKEN";
        }
        return uid;
    }

    public void applyToken(FirebaseUser user) {
        Log.d(TAG, "Applied Token");
        if (!Configs.TESTING)
            uid = user.getUid();

    }
}
