package com.canvearth.canvearth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.canvearth.canvearth.authorization.UserInformation;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private UserInformation mUserInformation;

    protected class MapsActivityIntent extends Intent {
        public MapsActivityIntent(FirebaseUser user) throws NullPointerException {
            super(LoginActivity.this, MapsActivity.class);

            Bundle bundle = new Bundle();
            @NonNull String urlString = user.getPhotoUrl().toString();
            bundle.putString("userPhotoUrl", urlString);
            bundle.putString("userName", user.getDisplayName());
            this.putExtras(bundle);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mUserInformation = UserInformation.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "Login succeed");
                    mUserInformation.applyToken();
                    Intent intent = new MapsActivityIntent(user);

                    startActivity(intent);
                    finish();
                } else {
                    //TODO
                }
            }
        };
        mCallbackManager = CallbackManager.Factory.create();
        final LoginButton loginButton = findViewById(R.id.button_facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        // Commenting out for dev purposes. Will not need to type email and password to go to MapsActivity
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "Login callback succeed");
                mUserInformation.handleFacebookAccessToken(LoginActivity.this, loginResult.getAccessToken());

                @NonNull FirebaseUser user = mAuth.getCurrentUser();
                Intent intent = new MapsActivityIntent(user);
                startActivity(intent);
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
//        loginButton.setOnClickListener(new Button.OnClickListener() {
////            @Override
////            public void onClick(View view) {
////                Log.d("gimun", "onClick");
////                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
////                startActivity(intent);
////            }
//
//            public void onSuccess(LoginResult loginResult) {
//                Log.d(TAG, "Login callback succeed");
//                mUserInformation.handleFacebookAccessToken(LoginActivity.this, loginResult.getAccessToken());
//                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
//                startActivity(intent);
//            }
//        });
        Button fakeLoginButton = findViewById(R.id.facebook_fake_login);
        fakeLoginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                loginButton.performClick();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
