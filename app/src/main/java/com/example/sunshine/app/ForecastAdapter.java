package com.example.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

/**
 * Created by vmlinz on 2/17/16.
 */
public class ForecastAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean useSpecialToady;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        useSpecialToady = true;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useSpecialToady) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int itemViewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        layoutId = (itemViewType == VIEW_TYPE_TODAY)
                ? R.layout.list_item_forecast_today : R.layout.list_item_forecast;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // get viewHolder from view tag
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // get cached view memebers from view holder
        TextView dateTextView = viewHolder.dateView;
        TextView forecastTextView  = viewHolder.forcastView;
        TextView highTextView = viewHolder.highView;
        TextView lowTextView = viewHolder.lowView;
        ImageView iconImageView = viewHolder.iconView;

        // get metric
        boolean isMetric = Utils.isMetric(context);

        // get weather condition id
        int weatherConditionId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        // set icon
        Logger.d("weatherConditionId: " + weatherConditionId);
        if (getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY) {
            iconImageView.setImageResource(Utils.getArtResourceForWeatherCondition(weatherConditionId));
        } else {
            iconImageView.setImageResource(Utils.getIconResourceForWeatherCondition(weatherConditionId));
        }

        // read date and date textview
        long date = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        dateTextView.setText(Utils.getFriendlyDayString(context, date));

        // read forecast and set forecast textview
        String forecast = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        forecastTextView.setText(forecast);

        // read max temp and set high textview
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        highTextView.setText(Utils.formatTemperature(context, high, isMetric));

        // read min temp and set low textview
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        lowTextView.setText(Utils.formatTemperature(context, low, isMetric));
    }

    public void setUseSpecialToady(boolean useSpecialToady) {
        this.useSpecialToady = useSpecialToady;
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView forcastView;
        public final TextView highView;
        public final TextView lowView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            forcastView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}
