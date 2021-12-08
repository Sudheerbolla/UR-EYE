package com.ureye.activities;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.ureye.BaseApplication;
import com.ureye.R;
import com.ureye.databinding.ActivityMainBinding;
import com.ureye.interfaces.TextToSpeechListener;
import com.ureye.interfaces.VoiceRecognisationListener;
import com.ureye.utils.Constants;
import com.ureye.utils.StaticUtils;
import com.ureye.utils.UREyeAppStorage;
import com.ureye.utils.common.LocationsModel;

import java.util.List;

public class MainActivity extends BaseActivity implements TextToSpeechListener, View.OnClickListener, VoiceRecognisationListener {

    private static final String TAG = "MainActivity";
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private ActivityMainBinding activityMainBinding;
    Location currentLocation;
    LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    private void getCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean hasGps = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean hasNetwork = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location lastKnownLocationByGps = null, lastKnownLocationByNetwork = null;
        if (hasGps) {
            lastKnownLocationByGps = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } else {
            StaticUtils.turnOnGPSInSystem(this);
        }

        if (hasNetwork) {
            lastKnownLocationByNetwork = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (lastKnownLocationByGps != null && lastKnownLocationByNetwork != null) {
            currentLocation = lastKnownLocationByGps.getAccuracy() > lastKnownLocationByNetwork.getAccuracy() ? lastKnownLocationByGps : lastKnownLocationByNetwork;
        } else if (lastKnownLocationByGps == null && lastKnownLocationByNetwork != null) {
            currentLocation = lastKnownLocationByNetwork;
        } else if (lastKnownLocationByGps != null && lastKnownLocationByNetwork == null) {
            currentLocation = lastKnownLocationByGps;
        }
        if (currentLocation != null) {
            StaticUtils.showToast(this, "Your Location: " + "\n" + "Latitude: " + currentLocation.getLatitude() + "\n" + "Longitude: " + currentLocation.getLongitude());
            BaseApplication.getInstance().runTextToSpeech("Saved your current Location");
            UREyeAppStorage.getInstance(this).insertLocationToSP(new LocationsModel(currentLocation));
        }
    }

    @Override
    public void initComponents() {
        checkForPermissions();
        setListeners();
    }

    private void setListeners() {
        activityMainBinding.txtFaceRecognition.setOnClickListener(this);
        activityMainBinding.txtObjectDetection.setOnClickListener(this);
        activityMainBinding.txtTextRecognition.setOnClickListener(this);
        activityMainBinding.txtViewSavedData.setOnClickListener(this);
        activityMainBinding.rootLayout.setOnClickListener(this);
    }

    private void checkForPermissions() {
        if (!StaticUtils.allPermissionsGranted(this)) {
            StaticUtils.showToast(this, "Please allow all permissions to open the app functionality");
            List<String> allNeededPermissions = StaticUtils.getRuntimePermissions(this);
            if (!allNeededPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, allNeededPermissions.toArray(new String[0]), Constants.PERMISSION_REQUESTS);
            }
        } else {
            if (SpeechRecognizer.isRecognitionAvailable(this))
                setUpVoiceRecognition();
            textToSpeech = BaseApplication.getInstance().getTextToSpeechClient(this, this);
        }
    }

    private void setUpVoiceRecognition() {
        speechRecognizer = BaseApplication.getInstance().getVoiceRecognizer(this);
        if (speechRecognizer == null)
            StaticUtils.showToast(this, "No Speech Recognizer is available. Please install it in device with Speech Recognition available");
    }

    private void openFaceDetection() {
        startActivity(new Intent(this, FaceRecognitionActivity.class));
    }

    private void openTextDetection() {
        startActivity(new Intent(this, TextRecognitionActivity.class));
    }

    private void openObjectDetection() {
        startActivity(new Intent(this, ObjectDetectionActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            speechRecognizer.destroy();
            BaseApplication.getInstance().stopVoiceRecognizer();
            textToSpeech.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/*
    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (speechRecognizer != null) speechRecognizer.destroy();
            BaseApplication.getInstance().stopVoiceRecognizer();
            if (textToSpeech != null) textToSpeech.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkForPermissions();
    }

    @Override
    public void onStartTTS() {

    }

    @Override
    public void proceedSpeaking(String data) {
//        Log.e(TAG, "proceedSpeaking: " + data);
    }

    @Override
    public void errorDetectingText() {

    }

    @Override
    public void completedSpeaking() {
        if (textToSpeech != null) {
            textToSpeech.stop();
//            textToSpeech.shutdown();
        }
        BaseApplication.getInstance().startListening(this);
    }

    @Override
    public void onClick(View v) {
        if (StaticUtils.allPermissionsGranted(this))
            switch (v.getId()) {
                case R.id.txtFaceRecognition:
                    openFaceDetection();
                    break;
                case R.id.txtObjectDetection:
                    openObjectDetection();
                    break;
                case R.id.txtTextRecognition:
                    openTextDetection();
                    break;
                case R.id.txtViewSavedData:
                    StaticUtils.showSavedLocations(this);
                    break;
                case R.id.rootLayout:
                    BaseApplication.getInstance().runTextToSpeech("Starting voice recognizer");
                    break;
                default:
                    break;
            }
        else {
            checkForPermissions();
        }
    }

    /**
     * These are callbacks of voice to text
     */
    @Override
    public void startListening() {

    }

    @Override
    public void errorDetecting(String message, int errorCode) {

    }

    @Override
    public void completedListening(String data) {
        Log.e(TAG, "voice data from user: " + data);
        if (!TextUtils.isEmpty(data)) {
            switch (StaticUtils.getCatFromSpeech(data)) {
                case Constants.SELECTION_GENERAL_CATEGORY:
                    performAppropriateAction(data);
                    break;
                case Constants.SELECTION_CATEGORY_OBJECT:
                    activityMainBinding.txtObjectDetection.callOnClick();
                    break;
                case Constants.SELECTION_CATEGORY_TEXT:
                    activityMainBinding.txtTextRecognition.callOnClick();
                    break;
                case Constants.SELECTION_CATEGORY_FACE:
                    activityMainBinding.txtFaceRecognition.callOnClick();
                    break;
                case Constants.SELECTION_CATEGORY_SAVED:
                    activityMainBinding.txtViewSavedData.callOnClick();
                    break;
                default:
                    activityMainBinding.rootLayout.callOnClick();
                    break;
            }
        }
    }

    private void performAppropriateAction(String data) {
        if (data.contains("quit") || data.contains("close") || data.contains("stop"))
            finishAffinity();
        else if (data.contains("apphelp")) {
            BaseApplication.getInstance().startHelpNotation();
        } else if (data.contains("location") || data.contains("place")) {
            if (StaticUtils.allPermissionsGranted(this)) {
                getCurrentLocation();
            } else {
                checkForPermissions();
            }
        } else if (data.contains("help") || data.contains("emergency")) {
            StaticUtils.showToast(this, R.string.emergency_alert);
            BaseApplication.getInstance().stopListening(this);
            BaseApplication.getInstance().runTextToSpeech(getString(R.string.emergency_alert));
        } else if (data.contains("back")) {
            onBackPressed();
        }
    }

}