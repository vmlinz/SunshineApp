package com.example.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

import com.example.sunshine.app.R;
import com.example.sunshine.app.Utils;
import com.example.sunshine.app.data.WeatherUtils;
import com.orhanobut.logger.Logger;

/**
 * Created by vmlinz on 3/15/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();
    private static final int SYNC_INTERVAL = 60 * 30;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        mContext = context;
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Logger.t(LOG_TAG).d("onPerformSync Called");

        String location = Utils.getPreferredLocation(mContext);

        WeatherUtils.handleActionFetchWeather(mContext, location);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority),
                bundle);
    }

    private static Account getSyncAccount(Context context) {
        AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account account = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));

        if (null == am.getPassword(account)) {
            if (!am.addAccountExplicitly(account, "", null)) {
                return null;
            }

            onAccountCreated(context, account);
        }
        return account;
    }

    public static void configurePeriodicSync(Context context, int interval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);

        Logger.t(LOG_TAG).d("configurePeriodicSync: " + interval + " " + flexTime);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(interval, flexTime)
                    .setSyncAdapter(account, authority)
                    .setExtras(new Bundle())
                    .build();

            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority,
                    new Bundle(), interval);
        }
    }

    private static void onAccountCreated(Context context, Account account) {
        Logger.t(LOG_TAG).d("onAccountCreated");
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        ContentResolver.setSyncAutomatically(account, context.getString(R.string.content_authority), true);

        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        Logger.t(LOG_TAG).d("initializeSyncAdapter");
        getSyncAccount(context);
    }
}
