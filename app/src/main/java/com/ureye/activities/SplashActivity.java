package com.ureye.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ureye.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    public void initComponents() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finishAffinity();
        }, 1000);
    }

}