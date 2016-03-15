package com.example.sunshine.app.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.sunshine.app.data.WeatherUtils;
import com.orhanobut.logger.Logger;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FetchWeatherService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FETCH_WEATHER = "com.example.sunshine.app.action.FETCH_WEATHER";
    public static final String EXTRA_LOCATION_QUERY = "com.example.sunshine.app.action.EXTRA_LOCATION_QUERY";
    private static final String LOG_TAG = FetchWeatherService.class.getSimpleName();

    public FetchWeatherService() {
        super("FetchWeatherService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFetchWeather(Context context, String location) {
        Intent intent = getIntentFetchWeather(context, location);
        context.startService(intent);
    }

    public static Intent getIntentFetchWeather(Context context, String location) {
        Intent intent = new Intent(context, FetchWeatherService.class);
        intent.setAction(ACTION_FETCH_WEATHER);
        intent.putExtra(EXTRA_LOCATION_QUERY, location);
        return intent;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_WEATHER.equals(action)) {
                String location = intent.getStringExtra(EXTRA_LOCATION_QUERY);
                WeatherUtils.handleActionFetchWeather(this, location);
            }
        }
    }

    static public class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.d("AlarmReceiver: " + "Alarm received");
            Intent sendingIntent = new Intent(intent).setClass(context, FetchWeatherService.class);
            context.startService(sendingIntent);
        }
    }
}
