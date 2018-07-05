package com.canvearth.canvearth.authorization;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

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

    public void handleFacebookAccessToken(AppCompatActivity currentActivity, AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        final AppCompatActivity activity = currentActivity;
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            Toast.makeText(activity, "Authentication succeed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(activity, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public String getToken() throws TimeoutException, InterruptedException {
        int requestCount = 0;
        tokenApplyLatch.await();
        return mGetTokenResult.getToken();
    }

    public void applyToken() {
        Log.d(TAG, "Applied Token");
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        user.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Applied Token Succeed");
                    mGetTokenResult = task.getResult();
                    tokenApplyLatch.countDown();
                } else {
                    throw new RuntimeException("Could not get token");
                }
            }
        });
    }
}
