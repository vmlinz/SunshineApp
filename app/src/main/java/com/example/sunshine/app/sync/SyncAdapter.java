package com.example.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.example.sunshine.app.R;
import com.orhanobut.logger.Logger;

/**
 * Created by vmlinz on 3/15/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter{
    private static final String TAG = SyncAdapter.class.getSimpleName();

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        Logger.d(TAG, "onPerformSync Called");
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
