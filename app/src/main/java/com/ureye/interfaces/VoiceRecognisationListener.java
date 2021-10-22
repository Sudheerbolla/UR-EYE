package com.ureye.interfaces;

public interface VoiceRecognisationListener {

    void startListening();

    void errorDetecting(String message, int errorCode);

    void completedListening(String data);

}
