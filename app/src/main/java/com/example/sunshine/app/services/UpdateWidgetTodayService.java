package com.example.sunshine.app.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import com.example.sunshine.app.R;
import com.example.sunshine.app.data.WeatherContract;
import com.example.sunshine.app.features.main.MainActivity;
import com.example.sunshine.app.features.widgets.SunshineWidgetProvider;
import com.example.sunshine.app.utils.CommonUtils;
import com.orhanobut.logger.Logger;

/**
 * Created by vmlinz on 5/14/16.
 */
public class UpdateWidgetTodayService extends IntentService{
    public static String ACTION_WEATHER_DATA_UPDATE = "com.example.sunshine.app.widget.action.WEATHER_DATA_UPDATE";

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP
    };

    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;

    public UpdateWidgetTodayService() {
        super("UpdateWidgetTodayService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logger.d("onHandleIntent: " + "update widget service");
        updateWeatherWidget();
    }

    private void updateWeatherWidget() {
        // get app widget manager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        // get app widget ids
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, SunshineWidgetProvider.class));

        // get location
        String location = CommonUtils.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, System.currentTimeMillis());

        // fetch data from database
        Cursor data = getContentResolver().query(weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");

        if (data == null) {
            return;
        }

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // extract data columns
        int weatherId = data.getInt(INDEX_WEATHER_ID);
        int weatherArtId = CommonUtils.getArtResourceForWeatherCondition(weatherId);
        String description = data.getString(INDEX_SHORT_DESC);
        double maxTemp = data.getDouble(INDEX_MAX_TEMP);
        String maxTempFormatted = CommonUtils.formatTemperature(this, maxTemp, false);

        Logger.d("maxTemp: " + maxTemp + " maxTempFormatted: " + maxTempFormatted);

        // close cursor
        data.close();

        // fill data in app widgets
        for (int id : appWidgetIds){
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.appwidget_sunshine);
            views.setImageViewResource(R.id.appwidget_sunshine_image, weatherArtId);
            views.setTextViewText(R.id.appwidget_sunshine_text, maxTempFormatted);

            views.setOnClickPendingIntent(R.id.appwidget_sunshine, pendingIntent);

            appWidgetManager.updateAppWidget(id, views);
        }
    }
}
