package com.DorelSaig.superme.Activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.Misc.Constants;
import com.DorelSaig.superme.Objects.MyUser;
import com.DorelSaig.superme.R;
import com.airbnb.lottie.LottieAnimationView;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends AppCompatActivity {

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

    MaterialButton panel_BTN_login;

    FirebaseFirestore db;

    LottieAnimationView panel_PRG_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        panel_BTN_login = findViewById(R.id.panel_BTN_login);
        panel_PRG_bar = findViewById(R.id.panel_PRG_bar);
        db = FirebaseFirestore.getInstance();


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            loadUserFromDB();
        }

        panel_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.LoginTheme)
                        .build();
                signInLauncher.launch(signInIntent);
            }
        });

    }

    private void loadUserFromDB() {
        // Successfully signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = db.collection(Constants.KEY_USERS).document(user.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Log.d("pttt", "DocumentSnapshot data: " + documentSnapshot.getData());
                    MyUser loadedUser = documentSnapshot.toObject(MyUser.class);
                    MyDataManager.getInstance().setCurrentUser(loadedUser);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                } else {
                    Log.d("pttt", "No such document");
                    Log.d("pttt", user.getUid());
                    startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                }
                finish();
            }

        });
    }

    /**
     * When the sign-in flow is complete, you will receive the result in
     *
     * @param result The result received from sign-in flow
     */
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            panel_BTN_login.setVisibility(View.INVISIBLE);
            panel_PRG_bar.setVisibility(View.VISIBLE);
            loadUserFromDB();
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

}