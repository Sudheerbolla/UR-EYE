package com.ureye.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.ureye.BaseApplication;
import com.ureye.R;
import com.ureye.databinding.ActivityMainBinding;
import com.ureye.interfaces.TextToSpeechListener;
import com.ureye.utils.Constants;
import com.ureye.utils.StaticUtils;

import java.util.List;

public class MainActivity extends BaseActivity implements RecognitionListener, TextToSpeechListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private SpeechRecognizer speechRecognizer;
    private ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
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
        if (!allPermissionsGranted()) {
            List<String> allNeededPermissions = StaticUtils.getRuntimePermissions(this);
            if (!allNeededPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, allNeededPermissions.toArray(new String[0]), Constants.PERMISSION_REQUESTS);
            }
        } else {
            setUpVoiceRecognition();
        }
    }

    private void setUpVoiceRecognition() {
        speechRecognizer = BaseApplication.getVoiceRecognizer(this);
        if (speechRecognizer == null)
            StaticUtils.showToast(this, "No Speech Recognizer is available. Please install it in device with Speech Recognition available");
        speechRecognizer.setRecognitionListener(this);
    }

    private boolean allPermissionsGranted() {
        for (String permission : StaticUtils.getRequiredPermissions(this)) {
            if (!StaticUtils.isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkForPermissions();
    }
/*
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(TAG, "User Touched screen, Open voice to text");
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                BaseApplication.getInstance().getTextToSpeechClient(this, "Starting voice recognizer", this);
                return true;
        }
        return super.dispatchTouchEvent(ev);
    }*/

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void proceedSpeaking(String data) {

    }

    @Override
    public void errorDetectingText() {

    }

    @Override
    public void completedSpeaking() {
//        speechRecognizer.startListening();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtFaceRecognition:
//                v.getParent().requestDisallowInterceptTouchEvent(true);
                openFaceDetection();
                break;
            case R.id.txtObjectDetection:
//                v.getParent().requestDisallowInterceptTouchEvent(true);
                openObjectDetection();
                break;
            case R.id.txtTextRecognition:
//                v.getParent().requestDisallowInterceptTouchEvent(true);
                openTextDetection();
                break;
            case R.id.txtViewSavedData:
//                v.getParent().requestDisallowInterceptTouchEvent(true);
                openSavedDataScreen();
                break;
            case R.id.rootLayout:
                BaseApplication.getInstance().getTextToSpeechClient(this, "Starting voice recognizer", this);
                break;
            default:
                break;
        }
    }

    private void openFaceDetection() {
        startActivity(new Intent(this, FaceRecognitionActivity.class));
    }

    private void openSavedDataScreen() {
        StaticUtils.showToast(this, getString(R.string.module_under_development));
    }

    private void openTextDetection() {
        StaticUtils.showToast(this, getString(R.string.module_under_development));
    }

    private void openObjectDetection() {
        StaticUtils.showToast(this, getString(R.string.module_under_development));
    }

}