package com.example.sunshine.app.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.sunshine.app.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.orhanobut.logger.Logger;

import java.io.IOException;

/**
 * Created by vmlinz on 4/27/16.
 */
public class RegistrationIntentService extends IntentService {
    private static final String SENT_TOKEN_TO_SERVER = "sent_token_to_server";
    private static final String TAG = "RegistrationIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        synchronized (TAG) {
            // get the instance id
            InstanceID instanceID = InstanceID.getInstance(this);

            // get the sender id
            String authorizeEntity = getString(R.string.gcm_defaultSenderId);
            Logger.d("authorizeEntity: " + authorizeEntity);

            if (authorizeEntity.length() != 0) {
                try {
                    // get token
                    String token = instanceID.getToken(authorizeEntity,
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    // send token to server
                    sendRegistrationToServer(token);

                    // save token sent status as true
                    preferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, true).apply();
                } catch (IOException e) {
                    e.printStackTrace();

                    // save token sent status as false
                    preferences.edit().putBoolean(SENT_TOKEN_TO_SERVER, false).apply();
                }
            }
        }
    }

    private void sendRegistrationToServer(String token) {
        Logger.d("Token: " + token);
    }
}
