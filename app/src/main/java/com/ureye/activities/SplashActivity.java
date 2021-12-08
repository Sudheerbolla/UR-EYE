package com.ureye.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.ureye.BaseApplication;
import com.ureye.R;
import com.ureye.interfaces.TextToSpeechListener;
import com.ureye.utils.Constants;
import com.ureye.utils.StaticUtils;
import com.ureye.utils.UREyeAppStorage;

import java.util.List;

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
            checkForPermissions();
        } else {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finishAffinity();
            }, 1000);
        }
    }

    public void checkForPermissions() {
        if (!StaticUtils.allPermissionsGranted(this)) {
            StaticUtils.showToast(this, "Please allow all permissions to open the app functionality");
            List<String> allNeededPermissions = StaticUtils.getRuntimePermissions(this);
            if (!allNeededPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, allNeededPermissions.toArray(new String[0]), Constants.PERMISSION_REQUESTS);
            }
        } else {
            new Handler().postDelayed(() -> BaseApplication.getInstance().startHelpNotation(this), 750);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkForPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApplication.getInstance().stopSpeaking();
    }

}