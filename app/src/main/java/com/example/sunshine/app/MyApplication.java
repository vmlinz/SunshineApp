package com.example.sunshine.app;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.orhanobut.logger.Logger;

/**
 * Created by vmlinz on 1/20/16.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                        .build());
        Logger.init();
    }
}
