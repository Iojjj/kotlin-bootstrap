package com.github.iojjj.app.core;

import android.app.Application;
import android.os.StrictMode;

public final class CoreApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.enableDefaults();
    }
}
