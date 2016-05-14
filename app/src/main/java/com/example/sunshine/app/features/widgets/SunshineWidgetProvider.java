package com.example.sunshine.app.features.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.sunshine.app.services.UpdateWidgetTodayService;

/**
 * Created by vmlinz on 5/13/16.
 */
public class SunshineWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        startService(context);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        startService(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (UpdateWidgetTodayService.ACTION_WEATHER_DATA_UPDATE.equals(intent.getAction())) {
            startService(context);
        }
    }

    private void startService(Context context) {
        context.startService(new Intent(context, UpdateWidgetTodayService.class));
    }
}
