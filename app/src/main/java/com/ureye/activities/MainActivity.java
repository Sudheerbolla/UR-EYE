package com.ureye.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements RecognitionListener, TextToSpeechListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
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
            if (SpeechRecognizer.isRecognitionAvailable(this))
                setUpVoiceRecognition();
            textToSpeech = BaseApplication.getInstance().getTextToSpeechClient(this, this);
        }
    }

    private void setUpVoiceRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        if (speechRecognizer == null)
            StaticUtils.showToast(this, "No Speech Recognizer is available. Please install it in device with Speech Recognition available");
        else speechRecognizer.setRecognitionListener(this);
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
        String message;
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Speech Recognizer is busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Server error";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Speech Recognizer cannot understand you";
                break;
        }
        Log.e(TAG, "onError: " + message);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String res = "";
        for (String line : data) {
            res += line;
        }
        Log.e(TAG, "results: " + res);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.e(TAG, "partialResults: " + partialResults);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    @Override
    public void onStartTTS() {

    }

    @Override
    public void proceedSpeaking(String data) {
        Log.e(TAG, "proceedSpeaking: " + data);
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
        startListening();
    }

    public void startListening() {
        runOnUiThread(() -> speechRecognizer.startListening(BaseApplication.getSpeechRecognizerIntent()));
    }

    @Override
    public void onClick(View v) {
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
                openSavedDataScreen();
                break;
            case R.id.rootLayout:
                BaseApplication.getInstance().runTextToSpeech("Starting voice recognizer");
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
        startActivity(new Intent(this, TextRecognitionActivity.class));
    }

    private void openObjectDetection() {
        startActivity(new Intent(this, ObjectDetectionActivity.class));
    }

}