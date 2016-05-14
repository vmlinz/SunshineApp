package com.example.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.example.sunshine.app.R;
import com.example.sunshine.app.data.WeatherContract;
import com.example.sunshine.app.features.main.MainActivity;
import com.example.sunshine.app.services.UpdateWidgetTodayService;
import com.example.sunshine.app.utils.CommonUtils;
import com.example.sunshine.app.utils.WeatherUtils;
import com.orhanobut.logger.Logger;

import java.util.concurrent.ExecutionException;

/**
 * Created by vmlinz on 3/15/16.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = SyncAdapter.class.getSimpleName();
    private static final int SYNC_INTERVAL = 60 * 30;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private Context mContext;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
    };

    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

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

        String location = CommonUtils.getPreferredLocation(mContext);

        WeatherUtils.handleActionFetchWeather(mContext, location);

        // send weather notification
        notifyWeather();

        // broadcast to update widget service
        Intent intent = new Intent(UpdateWidgetTodayService.ACTION_WEATHER_DATA_UPDATE)
                .setPackage(mContext.getPackageName());
        mContext.sendBroadcast(intent);
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

    private void notifyWeather() {
        Context context = mContext;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = preferences.getLong(lastNotificationKey, 0);

        String sendNotificationsKey = context.getString(R.string.pref_notifications_new);
        boolean sendNotifications = preferences.getBoolean(sendNotificationsKey, false);

        // check if we are to send notifications
        if ((System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) && sendNotifications) {
            String location = CommonUtils.getPreferredLocation(context);

            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, System.currentTimeMillis());

            Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

            if (cursor.moveToFirst()) {
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double high = cursor.getDouble(INDEX_MAX_TEMP);
                double low = cursor.getDouble(INDEX_MAX_TEMP);
                String desc = cursor.getString(INDEX_SHORT_DESC);

                int iconId = CommonUtils.getIconResourceForWeatherCondition(weatherId);
                String title = context.getString(R.string.app_name);
                Resources resources = context.getResources();
                int artResourceId = CommonUtils.getArtResourceForWeatherCondition(weatherId);
                String artUrl = CommonUtils.getArtResourceUrlForWeatherCondition(context, weatherId);

                int largIconWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
                        : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

                int largIconHeight = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
                        : resources.getDimensionPixelSize(R.dimen.notification_large_icon_default);

                // retrieve the large icon
                Bitmap largeIcon;
                try {
                    largeIcon = Glide.with(context)
                            .load(artUrl)
                            .asBitmap()
                            .error(artResourceId)
                            .fitCenter()
                            .into(largIconWidth, largIconHeight)
                            .get();
                } catch (InterruptedException | ExecutionException e) {
                    Logger.e(e, "Error retrieving large icon from " + artUrl);
                    largeIcon = BitmapFactory.decodeResource(resources, artResourceId);
                }

                // get the content text
                String text = String.format(context.getString(R.string.format_notification),
                        desc,
                        CommonUtils.formatTemperature(context, high, CommonUtils.isMetric(context)),
                        CommonUtils.formatTemperature(context, low, CommonUtils.isMetric(context)));

                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                // set the notification intent class
                Intent result = new Intent(mContext, MainActivity.class);

                // set the back stack for navigating back
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(result);

                PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                // build the notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setContentTitle(title)
                        .setContentIntent(pendingIntent)
                        .setContentText(text)
                        .setSmallIcon(iconId)
                        .setLargeIcon(largeIcon);

                // send the notification
                nm.notify(WEATHER_NOTIFICATION_ID, builder.build());

                // edit preference
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.apply();
            }

        }
    }
}
