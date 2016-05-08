package com.example.sunshine.app.features.main;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sunshine.app.R;
import com.example.sunshine.app.data.WeatherContract;
import com.example.sunshine.app.features.main.viewmodel.Forecast;
import com.example.sunshine.app.utils.CommonUtils;
import com.orhanobut.logger.Logger;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by vmlinz on 2/17/16.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private boolean useSpecialToady;
    private WeakReference<ForecastFragment.Callback> callback;

    public void setForecasts(List<Forecast> forecasts) {
        this.forecasts = forecasts;
    }

    public List<Forecast> getForecasts() {
        return forecasts;
    }

    private List<Forecast> forecasts;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView forecastTextView;
        public TextView highTextView;
        public TextView lowTextView;
        public ImageView iconImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            dateTextView = (TextView) itemView.findViewById(R.id.list_item_date_textview);
            forecastTextView = (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
            highTextView = (TextView) itemView.findViewById(R.id.list_item_high_textview);
            lowTextView = (TextView) itemView.findViewById(R.id.list_item_low_textview);
            iconImageView = (ImageView) itemView.findViewById(R.id.list_item_icon);
        }
    }

    public ForecastAdapter(ForecastFragment.Callback callback, List<Forecast> items, boolean special) {
        useSpecialToady = special;
        forecasts = items;
        this.callback = new WeakReference<>(callback);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View forecastView;

        if (viewType == VIEW_TYPE_TODAY) {
            forecastView = inflater.inflate(R.layout.list_item_forecast_today, parent, false);
        } else {
            forecastView = inflater.inflate(R.layout.list_item_forecast, parent, false);
        }

        return new ViewHolder(forecastView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        ImageView iconImageView = holder.iconImageView;
        TextView dateTextView = holder.dateTextView;
        TextView forecastTextView = holder.forecastTextView;
        TextView highTextView = holder.highTextView;
        TextView lowTextView = holder.lowTextView;
        final Forecast forecast = forecasts.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationSetting = CommonUtils.getPreferredLocation(view.getContext());
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting, forecast.getDate());

                // send weather uri to callback
                callback.get().onItemSelected(weatherUri);
            }
        });

        // get metric
        boolean isMetric = CommonUtils.isMetric(context);

        // get weather condition id
        int weatherConditionId = forecast.getWeatherId();
        // set icon
        Logger.d("weatherConditionId: " + weatherConditionId);

        String resourceUrl;
        int fallbackResourceId;
        resourceUrl = CommonUtils.getArtResourceUrlForWeatherCondition(context, weatherConditionId);
        fallbackResourceId = CommonUtils.getArtResourceForWeatherCondition(weatherConditionId);

        Logger.d("icon resource url: " + resourceUrl);
        Logger.d("fallback resource id: " + fallbackResourceId);
        Glide.with(context)
                .load(resourceUrl)
                .fallback(fallbackResourceId)
                .into(iconImageView);

        // read date and date textview
        long date = forecast.getDate();
        dateTextView.setText(CommonUtils.getFriendlyDayString(context, date));

        // read forecast and set forecast textview
        String desc = forecast.getDesc();
        forecastTextView.setText(desc);

        // read max temp and set high textview
        float high = forecast.getMax();
        highTextView.setText(CommonUtils.formatTemperature(context, high, isMetric));

        // read min temp and set low textview
        float low = forecast.getMin();
        lowTextView.setText(CommonUtils.formatTemperature(context, low, isMetric));
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useSpecialToady) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        return forecasts.size();
    }

    public void setUseSpecialToady(boolean useSpecialToady) {
        this.useSpecialToady = useSpecialToady;
    }
}
