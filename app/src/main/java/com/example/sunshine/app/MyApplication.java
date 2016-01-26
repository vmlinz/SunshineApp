package com.example.sunshine.app;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by vmlinz on 1/20/16.
 */
public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
