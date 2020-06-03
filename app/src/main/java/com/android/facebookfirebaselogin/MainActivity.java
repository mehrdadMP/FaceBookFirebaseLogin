package com.android.facebookfirebaselogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;
    private TextView tv_user;
    private ImageView logo;
    private LoginButton loginButton;
    private static final String TAG = "FaceBookAuthentication";
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        tv_user = findViewById(R.id.tv_main_user);
        logo = findViewById(R.id.image_logo);
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email" , "public_profile");
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG , "onSuccess" + loginResult);
                handleFaceBookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG , "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG , "onError" + error);
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    updateUI(user);
                }else{
                    updateUI(null);
                }
            }
        };

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(accessTokenTracker == null){
                    /*tv_user.setText("");
                    logo.setImageResource(R.drawable.facebook);
                    LoginManager.getInstance().logOut();
                    Toast.makeText(MainActivity.this , "here!!!!!" , Toast.LENGTH_LONG).show();*/
                    firebaseAuth.signOut();
                }
            }
        };
    }

    private void handleFaceBookToken(AccessToken accessToken) {
        Log.d(TAG , "handleFacebookToken" + accessToken);
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG , "sign in with credential : successful");
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUI(user);
                }else{
                    Log.d(TAG , "sign in with credential : failure" , task.getException());
                    Toast.makeText(MainActivity.this , "Authentication Failed" , Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null){
            tv_user.setText(user.getDisplayName());
            if(user.getPhotoUrl() != null){
                String photoURL = user.getPhotoUrl().toString();
                photoURL = photoURL + "?type=large";
                Picasso.get().load(photoURL).into(logo);
            }
        }else{
            tv_user.setText("");
            logo.setImageResource(R.drawable.facebookcopy);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode , resultCode , data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
