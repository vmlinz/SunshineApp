package com.example.sunshine.app.features.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.sunshine.app.R;
import com.example.sunshine.app.features.main.MainActivity;

/**
 * Created by vmlinz on 5/13/16.
 */
public class SunshineWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds){
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_sunshine);
            views.setOnClickPendingIntent(R.id.button_start_main_activity, pendingIntent);

            appWidgetManager.updateAppWidget(id, views);
        }
    }
}
