package com.example.sunshine.app.features.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.sunshine.app.R;
import com.example.sunshine.app.features.main.MainActivity;
import com.example.sunshine.app.utils.CommonUtils;

/**
 * Created by vmlinz on 5/13/16.
 */
public class SunshineWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        int weatherArtId = R.drawable.art_clear;
        String description = "Clear";
        double maxTemp = 24;
        String maxTempFormatted = CommonUtils.formatTemperature(context, maxTemp, true);


        for (int id : appWidgetIds){
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget_sunshine);
            views.setImageViewResource(R.id.appwidget_sunshine_image, weatherArtId);
            views.setTextViewText(R.id.appwidget_sunshine_text, maxTempFormatted);

            views.setOnClickPendingIntent(R.id.appwidget_sunshine, pendingIntent);

            appWidgetManager.updateAppWidget(id, views);
        }
    }
}
