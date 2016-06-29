// https://github.com/firebase/quickstart-android/blob/master/auth/app/src/main/java/com/google/firebase/quickstart/auth/GoogleSignInActivity.java

package com.example.tobias.recipist.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tobias.recipist.R;
import com.example.tobias.recipist.activity.BaseActivity;
import com.example.tobias.recipist.activity.MainActivity;
import com.example.tobias.recipist.util.FirebaseUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by Tobias on 22-06-2016.
 */
public class GoogleSignInActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = GoogleSignInActivity.class.getSimpleName();
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 9001;

    // FirebaseAuth
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    // GoogleApiClient
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);

        // Button listeners
        findViewById(R.id.google_sign_in_sign_in_button).setOnClickListener(this);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        mAuth = FirebaseUtil.getAuth();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (FirebaseUtil.getCurrentUser() != null) {
                    Log.d(TAG, "onCreate:onAuthStateChanged:signed_in: " + FirebaseUtil.getCurrentUserId());
                } else {
                    Log.d(TAG, "onCreate:onAuthStateChanged:signed_out");
                }

                updateUi(FirebaseUtil.getCurrentUser());
            }
        };

        if (FirebaseUtil.getCurrentUserId() != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount googleAccount = result.getSignInAccount();
                firebaseAuthWithGoogle(googleAccount);
            } else {
                updateUi(null);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.google_sign_in_sign_in_button:
                signIn();
                break;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
        Toast.makeText(this, getString(R.string.error_google_play_services_error), Toast.LENGTH_SHORT).show();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount googleAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + googleAccount.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(googleAccount.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "firebaseAuthWithGoogle:signInWithCredential:onComplete:" + task.isSuccessful());

                if (!task.isSuccessful()) {
                    Log.w(TAG, "firebaseAuthWithGoogle:signInWithCredential:error", task.getException());
                    Toast.makeText(GoogleSignInActivity.this, getString(R.string.error_authentication_failed), Toast.LENGTH_SHORT).show();
                }

                hideProgressDialog();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUi(null);
            }
        });
    }

    private void disconnect() {
        // Firebase sign out
        mAuth.signOut();

        // Google disconnect (revoke access)
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                updateUi(null);
            }
        });
    }

    private void updateUi(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            findViewById(R.id.google_sign_in_sign_in_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.google_sign_in_sign_in_button).setVisibility(View.VISIBLE);
        }
    }
}
