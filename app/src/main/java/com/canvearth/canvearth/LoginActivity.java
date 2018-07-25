package com.canvearth.canvearth;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.canvearth.canvearth.utils.Constants;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.canvearth.canvearth.databinding.ActivityLoginBinding;

import org.apache.commons.io.IOUtils;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;

    protected class MapsActivityIntent extends Intent {
        public MapsActivityIntent(FirebaseUser user) throws NullPointerException {
            super(LoginActivity.this, MapsActivity.class);

            Bundle bundle = new Bundle();
            @NonNull String urlString = user.getPhotoUrl().toString();
            bundle.putString(Constants.userPhotoUrl, urlString);
            bundle.putString(Constants.userName, user.getDisplayName());
            bundle.putString(Constants.userEmail, user.getEmail());
            this.putExtras(bundle);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding m_b = DataBindingUtil.setContentView(this, R.layout.activity_login);
        m_b.setSplash(new SplashForegroundView(this));
        try {
            final String pathString = IOUtils.toString(getAssets().open("splash/foreground.path"), "UTF-8");
            final Path path = PathParser.createPathFromPathData(pathString);

            m_b.splash.setPath(path, 400, 400);
            m_b.splash.setFillDrawable(R.drawable.splash_fill);
            m_b.splash.start();
        } catch (Exception e) {
            Log.e("ERROR", e.toString());
        }


        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            Intent intent = new MapsActivityIntent(user);
            startActivity(intent);
        }

        mCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");

        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Login callback succeed");
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.w(TAG, "Login callback canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "Login callback error" + error.getMessage());
            }
        });
    }

    public void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        final FirebaseAuth auth = FirebaseAuth.getInstance();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");

                        // TODO: Isn't this always NonNull?
                        Intent intent = new MapsActivityIntent(auth.getCurrentUser());
                        startActivity(intent);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
