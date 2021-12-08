package com.ureye;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.multidex.MultiDexApplication;

import com.ureye.activities.BaseActivity;
import com.ureye.interfaces.TextToSpeechListener;
import com.ureye.interfaces.VoiceRecognisationListener;
import com.ureye.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Locale;

public class BaseApplication extends MultiDexApplication {

    private static BaseApplication baseApplication;
    private static SpeechRecognizer speechRecognizer;
    private static TextToSpeech textToSpeech;
    private static final String TAG = "BaseApplication";
    private Intent speechRecognizerIntent;
    private TextToSpeechListener textToSpeechListener;
    private AlertDialog alertDialog;

    public synchronized static BaseApplication getInstance() {
        if (baseApplication == null) baseApplication = new BaseApplication();
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
    }

    public SpeechRecognizer getVoiceRecognizer(VoiceRecognisationListener voiceRecognisationListener) {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                speechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {

                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        voiceRecognisationListener.startListening();
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
                        voiceRecognisationListener.errorDetecting(message, error);
                    }

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        String res = "";
                        for (String line : data) {
                            res += line;
                        }
                        voiceRecognisationListener.completedListening(res);
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                        Log.e(TAG, "partial: " + partialResults);
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {

                    }
                });
                getSpeechRecognizerIntent();
            }
        }
        return speechRecognizer;
    }

    public void stopListening(BaseActivity activity) {
        activity.runOnUiThread(() -> speechRecognizer.stopListening());
    }

    public void startListening(BaseActivity activity) {
        activity.runOnUiThread(() -> {
            if (speechRecognizer != null) speechRecognizer.startListening(speechRecognizerIntent);
        });
    }

    private void getSpeechRecognizerIntent() {
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        /*"en-CA" or "en-US" for both language and language model*/
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_WEB_SEARCH_ONLY, false);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, "3000");
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, true);
//        return speechRecognizerIntent;
    }

    public TextToSpeech getTextToSpeechClient(Context context, TextToSpeechListener textToSpeechListener) {
        this.textToSpeechListener = textToSpeechListener;
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    StaticUtils.showToast(context, "This language is not supported!");
                    if (textToSpeechListener != null) {
                        textToSpeechListener.errorDetectingText();
                    }
                } else {
                    textToSpeech.setPitch(0.6f);
                    textToSpeech.setSpeechRate(1.0f);
                }
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        if (textToSpeechListener != null) {
                            textToSpeechListener.onStartTTS();
                        }
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        if (textToSpeechListener != null) {
                            textToSpeechListener.completedSpeaking();
                        }
                        if (alertDialog != null && alertDialog.isShowing()) alertDialog.cancel();
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (textToSpeechListener != null) {
                            textToSpeechListener.errorDetectingText();
                        }
                    }
                });
            }
        });
        return textToSpeech;
    }

    public void runTextToSpeech(String data) {
        if (textToSpeechListener != null) {
            textToSpeechListener.proceedSpeaking(data);
        }
        if (textToSpeech == null)
            getTextToSpeechClient(this, textToSpeechListener);
        textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
    }

    public void continuousTextToSpeech(String data) {
        if (textToSpeechListener != null) {
            textToSpeechListener.proceedSpeaking(data);
        }
        if (textToSpeech == null)
            getTextToSpeechClient(this, textToSpeechListener);
        if (!textToSpeech.isSpeaking())
            textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
    }

    public void addTextToSpeech(String data) {
        if (textToSpeechListener != null) {
            textToSpeechListener.proceedSpeaking(data);
        }
        if (textToSpeech == null)
            getTextToSpeechClient(this, textToSpeechListener);
        textToSpeech.speak(data, TextToSpeech.QUEUE_ADD, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
    }

    public void stopSpeaking() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) textToSpeech.stop();
    }

    public void stopVoiceRecognizer() {
        if (speechRecognizer != null) speechRecognizer = null;
    }

    public void startHelpNotation(BaseActivity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter Name");
        builder.setMessage(R.string.app_help_intro);
        builder.setPositiveButton("Skip", (dialog, which) -> {
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
                if (textToSpeechListener != null) {
                    textToSpeechListener.completedSpeaking();
                }
            }
        });
        alertDialog = builder.show();
        runTextToSpeech(getString(R.string.app_help_intro));
    }

}
