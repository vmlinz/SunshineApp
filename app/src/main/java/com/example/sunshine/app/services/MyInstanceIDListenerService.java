package com.example.sunshine.app.services;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;
import com.orhanobut.logger.Logger;

/**
 * Created by vmlinz on 4/27/16.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Logger.d("MyInstanceIDListenerService: onTokenRefresh");
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }
}
