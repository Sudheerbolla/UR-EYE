package com.ureye;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import androidx.multidex.MultiDexApplication;

import com.ureye.interfaces.TextToSpeechListener;
import com.ureye.utils.StaticUtils;

import java.util.Locale;

public class BaseApplication extends MultiDexApplication {

    private static BaseApplication baseApplication;
    private static SpeechRecognizer speechRecognizer;
    private static TextToSpeech textToSpeech;

    public synchronized static BaseApplication getInstance() {
        if (baseApplication == null) baseApplication = new BaseApplication();
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
    }

    public static SpeechRecognizer getVoiceRecognizer(Context context) {
        if (SpeechRecognizer.isRecognitionAvailable(context.getApplicationContext()))
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.getApplicationContext());
                final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
                }
//                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, true);
            }
        return speechRecognizer;
    }

    public void getTextToSpeechClient(Context context, String data, TextToSpeechListener textToSpeechListener) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {

                    }

                    @Override
                    public void onDone(String utteranceId) {
                        if (textToSpeechListener != null) {
                            textToSpeechListener.completedSpeaking();
                        }
                        textToSpeech.stop();
                        textToSpeech.shutdown();
                    }

                    @Override
                    public void onError(String utteranceId) {
                        if (textToSpeechListener != null) {
                            textToSpeechListener.errorDetectingText();
                        }
                    }
                });
                int result = textToSpeech.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    StaticUtils.showToast(context, "This language is not supported!");
                    if (textToSpeechListener != null) {
                        textToSpeechListener.errorDetectingText();
                    }
                } else {
                    textToSpeech.setPitch(0.6f);
                    textToSpeech.setSpeechRate(1.0f);
                    if (textToSpeechListener != null) {
                        textToSpeechListener.proceedSpeaking(data);
                    }
                    textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        });
    }

}
