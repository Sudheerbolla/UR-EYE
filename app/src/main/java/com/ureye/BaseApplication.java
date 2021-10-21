package com.ureye;

import android.content.Context;
import android.content.Intent;
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
        if (SpeechRecognizer.isRecognitionAvailable(context))
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            }
        return speechRecognizer;
    }

    public static Intent getSpeechRecognizerIntent() {
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        /*"en-CA" or "en-US" for both language*/
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_WEB_SEARCH_ONLY, false);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, "3000");
//        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, true);
        return speechRecognizerIntent;
    }

    static TextToSpeechListener textToSpeechListener;

    public TextToSpeech getTextToSpeechClient(Context context, TextToSpeechListener textToSpeechListener) {
        BaseApplication.textToSpeechListener = textToSpeechListener;
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
            getTextToSpeechClient(BaseApplication.baseApplication, textToSpeechListener);
        textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED);
    }

}
