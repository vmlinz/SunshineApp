package com.example.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by vmlinz on 2/17/16.
 */
public class ForcastAdapter extends CursorAdapter {
    public ForcastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView dateTextView = (TextView)view.findViewById(R.id.list_item_date_textview);
        TextView forecastTextView  = (TextView)view.findViewById(R.id.list_item_forecast_textview);
        TextView highTextView = (TextView)view.findViewById(R.id.list_item_high_textview);
        TextView lowTextView = (TextView)view.findViewById(R.id.list_item_low_textview);
        ImageView iconImageView = (ImageView)view.findViewById(R.id.list_item_icon);

        // get weatherId
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

        // get metric
        boolean isMetric = Utility.isMetric(context);

        // set icon
        iconImageView.setImageResource(R.mipmap.ic_launcher);

        // read date and date textview
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        dateTextView.setText(Utility.getFriendlyDayString(context, date));

        // read forecast and set forecast textview
        String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        forecastTextView.setText(forecast);

        // read max temp and set high textview
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        highTextView.setText(Utility.formatTemperature(high, isMetric));

        // read min temp and set low textview
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        lowTextView.setText(Utility.formatTemperature(low, isMetric));
    }
}
