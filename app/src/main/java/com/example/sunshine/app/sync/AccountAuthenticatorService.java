package com.example.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by vmlinz on 3/15/16.
 */
public class AccountAuthenticatorService extends Service {
    private AccountAuthenticator accountAuthenticator;

    @Override
    public void onCreate() {
        accountAuthenticator = new AccountAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return accountAuthenticator.getIBinder();
    }
}
