package com.example.sunshine.app.ui.details;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sunshine.app.R;
import com.example.sunshine.app.data.WeatherContract;
import com.example.sunshine.app.utils.CommonUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String DETAIL_URI = "DETAIL_URI";
    private ShareActionProvider mShareActionProvider;
    private String mForecast;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "" + WeatherContract.WeatherEntry._ID,
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
    private RecyclerView weatherInfoRecyclerView;
    private DetailsInfoAdapter detailsInfoAdapter;
    private Uri weatherUri;
    private TextView dateMonthDayTextView;
    private RecyclerView weatherExtraInfoRecyclerView;
    private DetailsExtraInfoAdapter detailsExtraInfoAdapter;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            weatherUri = bundle.getParcelable(DETAIL_URI);
        }

        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        dateMonthDayTextView = (TextView) root.findViewById(R.id.details_date_and_month_day_text_view);
        weatherInfoRecyclerView = (RecyclerView) root.findViewById(R.id.details_weather_info_recycler_view);
        weatherExtraInfoRecyclerView = (RecyclerView) root.findViewById(R.id.details_weather_info_extra_recycler_view);

        // setup details info
        detailsInfoAdapter = new DetailsInfoAdapter();

        RecyclerView.LayoutManager detailsInfolayoutManager = new GridLayoutManager(getContext(), 2);

        weatherInfoRecyclerView.setLayoutManager(detailsInfolayoutManager);
        weatherInfoRecyclerView.setAdapter(detailsInfoAdapter);

        // setup details extra info
        detailsExtraInfoAdapter = new DetailsExtraInfoAdapter();

        RecyclerView.LayoutManager detailsExtraInfoLayoutManager = new GridLayoutManager(getContext(), 2);

        weatherExtraInfoRecyclerView.setLayoutManager(detailsExtraInfoLayoutManager);
        weatherExtraInfoRecyclerView.setAdapter(detailsExtraInfoAdapter);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);

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
        if (weatherUri == null) {
            return null;
        }

        return new CursorLoader(this.getActivity(),
                weatherUri,
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Logger.d("In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }

        Logger.d("updateDetailsView");
        updateDetailsView(getContext(), data);
    }

    private void updateDetailsView(Context context, Cursor cursor) {
        boolean isMetric = CommonUtils.isMetric(context);

        // set date
        String date = CommonUtils.getDayName(context,
                cursor.getLong(COL_WEATHER_DATE));
        // set month day
        String monthDay = CommonUtils.getFormattedMonthDay(context,
                cursor.getLong(COL_WEATHER_DATE));
        // set high temp
        String high = CommonUtils.formatTemperature(context,
                cursor.getFloat(COL_WEATHER_MAX_TEMP), isMetric);
        // set low temp
        String low = CommonUtils.formatTemperature(context,
                cursor.getFloat(COL_WEATHER_MIN_TEMP), isMetric);
        // set desc
        String desc = cursor.getString(COL_WEATHER_DESC);
        // set icon
        String artResourceUrl = CommonUtils.getArtResourceUrlForWeatherCondition(this.getContext(), cursor.getInt(COL_WEATHER_CONDITION_ID));

        List<String> detailsInfoItems = new ArrayList<>();
        detailsInfoItems.add(artResourceUrl);
        detailsInfoItems.add(high);
        detailsInfoItems.add(desc);
        detailsInfoItems.add(low);

        detailsInfoAdapter.setItems(detailsInfoItems);

        dateMonthDayTextView.setText(date + ", " + monthDay);

        // set humidity
        String humidity = getString(R.string.format_humidity,
                cursor.getFloat(COL_WEATHER_HUMIDITY));

        // set wind speed and direction
        String wind = CommonUtils.getFormattedWind(context,
                cursor.getFloat(COL_WEATHER_WIND_SPEED),
                cursor.getFloat(COL_WEATHER_DEGREES));

        // set pressure
        String pressure = getString(R.string.format_pressure,
                cursor.getFloat(COL_WEATHER_PRESSURE));

        List<String> extraInfoItems = new ArrayList<>();
        extraInfoItems.add("Humidity");
        extraInfoItems.add(humidity);
        extraInfoItems.add("Pressure");
        extraInfoItems.add(pressure);
        extraInfoItems.add("Wind");
        extraInfoItems.add(wind);

        detailsExtraInfoAdapter.setItems(extraInfoItems);
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

    public void onLocationChanged(String newLocation) {
        Uri uri = weatherUri;

        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }
}
