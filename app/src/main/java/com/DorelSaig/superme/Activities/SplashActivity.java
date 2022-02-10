package com.DorelSaig.superme.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.DorelSaig.superme.Firebase.MyDataManager;
import com.DorelSaig.superme.R;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    final int ANIM_DURATION = 5000;

    private ImageView splash_IMG_logo, splash_IMG_backdrop;
    private LottieAnimationView splash_LOTT_anim;
    private TextView splash_TXT_slogen;
    private LottieAnimationView panel_PRG_bar;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);



        if(MyDataManager.getInstance().getCurrentUser()!=null){
            Log.d("pttt", "User Already Loaded - Splash: " + MyDataManager.getInstance().getCurrentUser().getName().toString());
            goToHomeActivity();
        }
//        else if (FirebaseAuth.getInstance().getCurrentUser() == null){
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
//            finish();
//        }
//        else {
//            MyDataManager.getInstance().loadUserFromDB();
//        }



        findViews();

        Glide
                .with(this)
                .load(R.drawable.back2)
                .centerCrop()
                .into(splash_IMG_backdrop);

        progressDialog = new ProgressDialog(SplashActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");



        splash_IMG_backdrop.animate().translationY(-3000).setDuration(1000).setStartDelay(ANIM_DURATION);
        splash_IMG_logo.animate().translationY(3000).setDuration(1000).setStartDelay(ANIM_DURATION);
        splash_LOTT_anim.animate().translationY(1600).setDuration(1000).setStartDelay(ANIM_DURATION);
        splash_TXT_slogen.animate().translationY(1600).setDuration(1000).setStartDelay(ANIM_DURATION);
        panel_PRG_bar.animate().translationY(-700).setDuration(3070).setStartDelay(ANIM_DURATION)
                .setInterpolator(new AnticipateOvershootInterpolator())
                .setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if(FirebaseAuth.getInstance().getCurrentUser() != null){
                    MyDataManager.getInstance().loadUserFromDB();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animationDone();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


    }


    private void animationDone() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            Intent intent  = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }else {
            goToHomeActivity();
        }
    }

    private void goToHomeActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    private void findViews() {
        splash_IMG_backdrop = findViewById(R.id.splash_IMG_backdrop);
        splash_IMG_logo = findViewById(R.id.splash_IMG_logo);
        splash_LOTT_anim = findViewById(R.id.splash_LOTT_anim);
        splash_TXT_slogen = findViewById(R.id.splash_TXT_slogen);
        panel_PRG_bar = findViewById(R.id.panel_PRG_bar);
    }
}