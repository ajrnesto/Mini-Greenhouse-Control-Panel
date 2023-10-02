package com.greenhousecontrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import kotlin.Suppress;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startLoading();
    }

    private void startLoading() {
        findViewById(R.id.clLogo).startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));

        final Handler handler = new Handler();
        handler.postDelayed(this::loadingComplete, 3000);
    }

    private void loadingComplete() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }
}