package com.example.sunshine.app.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.example.sunshine.app.R;
import com.example.sunshine.app.data.WeatherContract;
import com.example.sunshine.app.features.main.MainActivity;
import com.example.sunshine.app.features.widgets.TodayWidigetProvider;
import com.example.sunshine.app.utils.CommonUtils;
import com.orhanobut.logger.Logger;

/**
 * Created by vmlinz on 5/14/16.
 */
public class UpdateWidgetTodayService extends IntentService{

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };

    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEMP = 3;

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
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TodayWidigetProvider.class));

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
        double minTemp = data.getDouble(INDEX_MIN_TEMP);
        String minTempFormatted = CommonUtils.formatTemperature(this, minTemp, false);

        // close cursor
        data.close();

        // fill data in app widgets
        for (int id : appWidgetIds){
            int widgetWidth = getWidgetWidth(appWidgetManager, id);
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_today_large_width);
            int layoutId;

            Logger.d("widgetWidth: " + widgetWidth + " defaultWidth: " + defaultWidth + " largeWidth: " + largeWidth);

            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.appwidget_sunshine_large;
            } else if (widgetWidth >= defaultWidth) {
                layoutId = R.layout.appwidget_sunshine;
            } else {
                layoutId = R.layout.appwidget_sunshine_small;
            }

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            RemoteViews views = new RemoteViews(this.getPackageName(), layoutId);
            views.setImageViewResource(R.id.appwidget_sunshine_image, weatherArtId);
            views.setTextViewText(R.id.appwidget_sunshine_high_temp_text, maxTempFormatted);
            views.setTextViewText(R.id.appwidget_sunshine_low_temp_text, minTempFormatted);

            views.setOnClickPendingIntent(R.id.appwidget_sunshine, pendingIntent);

            appWidgetManager.updateAppWidget(id, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int id) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
        }

        return getWidgetWidthFromOptions(appWidgetManager, id);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int id) {
        Bundle options = appWidgetManager.getAppWidgetOptions(id);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp, displayMetrics);
        }

        return getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
    }
}
