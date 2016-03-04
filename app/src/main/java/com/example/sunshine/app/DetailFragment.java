package com.example.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sunshine.app.data.WeatherContract;
import com.orhanobut.logger.Logger;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private ShareActionProvider mShareActionProvider;
    private String mForecast;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_WIND_SPEED = 6;
    private static final int COL_WEATHER_DEGREES = 7;
    private static final int COL_WEATHER_PRESSURE = 8;
    private static final int COL_WEATHER_CONDITION_ID = 9;

    private static final int LOADER_ID = 0;

    private String mLocation;

    // child views
    private TextView dateNameTextView;
    private TextView highTextView;
    private TextView lowTextView;
    private TextView descTextView;
    private TextView humidityTextView;
    private TextView windTextView;
    private TextView pressureTextView;
    private ImageView iconImageView;
    private TextView monthDayTextView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        dateNameTextView = (TextView) root.findViewById(R.id.details_date_name_text_view);
        monthDayTextView = (TextView) root.findViewById(R.id.details_month_day_text_view);
        highTextView = (TextView) root.findViewById(R.id.details_high_text_view);
        lowTextView = (TextView) root.findViewById(R.id.details_low_text_view);
        descTextView = (TextView) root.findViewById(R.id.details_desc_text_view);
        humidityTextView = (TextView) root.findViewById(R.id.details_humidity_text_view);
        windTextView = (TextView) root.findViewById(R.id.details_wind_speed_text_view);
        pressureTextView = (TextView) root.findViewById(R.id.details_pressure_text_view);
        iconImageView = (ImageView) root.findViewById(R.id.details_icon_image_view);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);

        // get the share action provider
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // create the share intent and set it to share action provider
        setShareIntent(getShareIntent());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Logger.d("In onCreateLoader");
        Intent intent = getActivity().getIntent();

        if (intent == null || intent.getData() == null) {
            return null;
        }
        return new CursorLoader(this.getActivity(),
                intent.getData(),
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Logger.d("In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        Logger.d("updateDetailsView");
        updateDetailsView(getContext(), data);
    }

    private void updateDetailsView(Context context, Cursor cursor) {
        boolean isMetric = Utility.isMetric(context);

        // set date
        String date = Utility.getDayName(context,
                cursor.getLong(COL_WEATHER_DATE));
        dateNameTextView.setText(date);

        // set month day
        String monthDay = Utility.getFormattedMonthDay(context,
                cursor.getLong(COL_WEATHER_DATE));
        monthDayTextView.setText(monthDay);

        // set high temp
        String high = Utility.formatTemperature(context,
                cursor.getFloat(COL_WEATHER_MAX_TEMP), isMetric);
        highTextView.setText(high);

        // set low temp
        String low = Utility.formatTemperature(context,
                cursor.getFloat(COL_WEATHER_MIN_TEMP), isMetric);
        lowTextView.setText(low);

        // set icon
        iconImageView.setImageResource(Utility.getArtResourceForWeatherCondition(cursor.getInt(COL_WEATHER_CONDITION_ID)));

        // set desc
        String desc = cursor.getString(COL_WEATHER_DESC);
        descTextView.setText(desc);

        // set humidity
        String humidity = getString(R.string.format_humidity,
                cursor.getFloat(COL_WEATHER_HUMIDITY));
        humidityTextView.setText(humidity);

        // set wind speed and direction
        String wind = Utility.getFormattedWind(context,
                cursor.getFloat(COL_WEATHER_WIND_SPEED),
                cursor.getFloat(COL_WEATHER_DEGREES));
        windTextView.setText(wind);

        // set pressure
        String pressure = getString(R.string.format_pressure,
                cursor.getFloat(COL_WEATHER_PRESSURE));
        pressureTextView.setText(pressure);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private Intent getShareIntent() {
        final String HASH_TAG = " #";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Share");

        return intent;
    }

    private void setShareIntent(Intent intent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(intent);
        }
    }
}
