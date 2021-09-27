package com.ureye.interfaces;

public interface TextToSpeechListener {

    void proceedSpeaking(String data);

    void errorDetectingText();

    void completedSpeaking();

}
