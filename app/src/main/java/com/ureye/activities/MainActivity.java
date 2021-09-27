package com.ureye.activities;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.ureye.BaseApplication;
import com.ureye.R;
import com.ureye.interfaces.TextToSpeechListener;
import com.ureye.utils.Constants;
import com.ureye.utils.StaticUtils;

import java.util.List;

public class MainActivity extends BaseActivity implements RecognitionListener, TextToSpeechListener {

    private static final String TAG = "MainActivity";
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initComponents() {
        checkForPermissions();
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e(TAG, "User Touched screen, Open voice to text");
        BaseApplication.getInstance().getTextToSpeechClient(this, "Starting voice recognizer", this);
        return super.dispatchTouchEvent(ev);
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

}