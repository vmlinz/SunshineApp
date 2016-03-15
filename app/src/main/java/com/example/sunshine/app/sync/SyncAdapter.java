package com.example.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.sunshine.app.R;
import com.example.sunshine.app.data.WeatherUtils;
import com.orhanobut.logger.Logger;

/**
 * Created by vmlinz on 3/15/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = sharedPreferences.getString(mContext.getString(R.string.pref_location_key), mContext.getString(R.string.pref_location_default));

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
        }
        return account;
    }
}
