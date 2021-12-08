package com.ureye.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ureye.BaseApplication;
import com.ureye.R;
import com.ureye.interfaces.TextToSpeechListener;
import com.ureye.utils.UREyeAppStorage;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    public void initComponents() {
        if (UREyeAppStorage.getInstance(this).getValue(UREyeAppStorage.SP_IS_FIRST_TIME, true)) {
            BaseApplication.getInstance().getTextToSpeechClient(this, new TextToSpeechListener() {
                @Override
                public void proceedSpeaking(String data) {
                }

                @Override
                public void errorDetectingText() {
                }

                @Override
                public void completedSpeaking() {
                    UREyeAppStorage.getInstance(SplashActivity.this).setValue(UREyeAppStorage.SP_IS_FIRST_TIME, false);
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finishAffinity();
                }

                @Override
                public void onStartTTS() {

                }
            });
            new Handler().postDelayed(() -> BaseApplication.getInstance().startHelpNotation(), 500);
        } else {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finishAffinity();
            }, 1000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApplication.getInstance().stopSpeaking();
    }

}