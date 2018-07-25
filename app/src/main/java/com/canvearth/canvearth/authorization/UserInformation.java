package com.canvearth.canvearth.authorization;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.canvearth.canvearth.utils.Configs;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class UserInformation {
    private static final String TAG = "UserInformation";
    private static final UserInformation ourInstance = new UserInformation();
    private GetTokenResult mGetTokenResult = null;
    private CountDownLatch tokenApplyLatch = new CountDownLatch(1);

    public static UserInformation getInstance() {
        return ourInstance;
    }

    private UserInformation() {
    }



    public String getToken() throws TimeoutException, InterruptedException {
        if (Configs.TESTING) {
            return "TEST_USER_TOKEN";
        }
        tokenApplyLatch.await();
        return mGetTokenResult.getToken();
    }

    public void applyToken(FirebaseUser user) {
        Log.d(TAG, "Applied Token");
        if (!Configs.TESTING)
            user.getIdToken(false).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Applied Token Succeed");
                    mGetTokenResult = task.getResult();
                    tokenApplyLatch.countDown();
                } else {
                    throw new RuntimeException("Could not get token");
                }
            });
    }
}
