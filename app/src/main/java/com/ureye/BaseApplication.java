package com.ureye;

import android.app.Application;

public class BaseApplication extends Application {

    private static BaseApplication baseApplication;

    synchronized private BaseApplication getInstance() {
        if (baseApplication == null) baseApplication = new BaseApplication();
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
    }
}
