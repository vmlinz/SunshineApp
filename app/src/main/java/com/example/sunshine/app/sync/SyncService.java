package com.example.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.orhanobut.logger.Logger;

/**
 * Created by vmlinz on 3/15/16.
 */
public class SyncService extends Service{
    private static final Object sSyncAdapterLock = new Object();
    private static final String TAG = SyncService.class.getSimpleName();
    private static SyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        Logger.d(TAG, "onCreate");
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
