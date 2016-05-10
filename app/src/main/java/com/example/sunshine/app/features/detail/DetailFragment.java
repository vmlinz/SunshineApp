package com.example.sunshine.app.features.detail;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sunshine.app.R;
import com.example.sunshine.app.data.WeatherContract;
import com.example.sunshine.app.utils.CommonUtils;
import com.orhanobut.logger.Logger;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String DETAIL_URI = "DETAIL_URI";
    public static final String DETAIL_TRANSITION_ANIMATION = "DETAIL_TRANSITION_ANIMATION";
    private ShareActionProvider mShareActionProvider;

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

    // child views
    private Uri weatherUri;
    private TextView detailDateTextView;
    private ImageView detailIconImageView;
    private TextView detailForecastTextView;
    private TextView detailHighTempTextView;
    private TextView detailExtraHumidity;
    private TextView detailExtraPressure;
    private TextView detailExtraWind;
    private TextView detailLowTempTextView;
    private boolean mTransitionAnimation;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            weatherUri = bundle.getParcelable(DETAIL_URI);
            mTransitionAnimation = bundle.getBoolean(DETAIL_TRANSITION_ANIMATION);
        }

        View root = inflater.inflate(R.layout.fragment_detail_start, container, false);

        // find details info views
        detailDateTextView = (TextView) root.findViewById(R.id.detail_date_textview);
        detailIconImageView = (ImageView) root.findViewById(R.id.detail_icon_imageview);
        detailForecastTextView = (TextView) root.findViewById(R.id.detail_forecast_textview);
        detailHighTempTextView = (TextView) root.findViewById(R.id.detail_high_textview);
        detailLowTempTextView = (TextView) root.findViewById(R.id.detail_low_textview);

        // find details extra info
        detailExtraHumidity = (TextView) root.findViewById(R.id.detail_humidity_value_textview);
        detailExtraPressure = (TextView) root.findViewById(R.id.detail_pressure_value_textview);
        detailExtraWind = (TextView) root.findViewById(R.id.detail_wind_value_textview);

        return root;
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // create the share intent and set it to share action provider
        setShareIntent(getShareIntent());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_fragment, menu);

        finishCreatingMenu(menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void toggleParentCardView(boolean status) {
        ViewParent viewParent = getView().getParent();
        if (viewParent instanceof CardView) {
            if (status) {
                ((CardView) viewParent).setVisibility(View.VISIBLE);
            } else {
                ((CardView) viewParent).setVisibility(View.INVISIBLE);
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (weatherUri == null) {

            toggleParentCardView(false);
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

        toggleParentCardView(true);

        // update details view with data from cursor
        updateDetailsView(getContext(), data);

        // update toolbar and menubar with transition animation
        updateToolbarAndMenuWithAnimation(mTransitionAnimation);
    }

    private void updateToolbarAndMenuWithAnimation(boolean enableTransitionAnimation) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (enableTransitionAnimation) {
            activity.supportStartPostponedEnterTransition();

            if (null != toolbarView) {
                activity.setSupportActionBar(toolbarView);

                activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        } else {
            if (null != toolbarView) {
                Menu menu = toolbarView.getMenu();
                if (null != menu) menu.clear();
                toolbarView.inflateMenu(R.menu.menu_detail_fragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }
    }

    private void updateDetailsView(Context context, Cursor cursor) {
        boolean isMetric = CommonUtils.isMetric(context);

        // get info data for detail view
        String date = CommonUtils.getDayName(context,
                cursor.getLong(COL_WEATHER_DATE));
        String monthDay = CommonUtils.getFormattedMonthDay(context,
                cursor.getLong(COL_WEATHER_DATE));
        String high = CommonUtils.formatTemperature(context,
                cursor.getFloat(COL_WEATHER_MAX_TEMP), isMetric);
        String low = CommonUtils.formatTemperature(context,
                cursor.getFloat(COL_WEATHER_MIN_TEMP), isMetric);
        String desc = cursor.getString(COL_WEATHER_DESC);
        String artResourceUrl = CommonUtils.getArtResourceUrlForWeatherCondition(this.getContext(), cursor.getInt(COL_WEATHER_CONDITION_ID));


        // set info data for detail view
        detailDateTextView.setText(date + ", " + monthDay);
        Glide.with(this.getContext())
                .load(artResourceUrl)
                .into(detailIconImageView);
        detailHighTempTextView.setText(high);
        detailLowTempTextView.setText(low);
        detailForecastTextView.setText(desc);

        // get extra info data
        String humidity = getString(R.string.format_humidity,
                cursor.getFloat(COL_WEATHER_HUMIDITY));
        String wind = CommonUtils.getFormattedWind(context,
                cursor.getFloat(COL_WEATHER_WIND_SPEED),
                cursor.getFloat(COL_WEATHER_DEGREES));
        String pressure = getString(R.string.format_pressure,
                cursor.getFloat(COL_WEATHER_PRESSURE));

        // set extra info to extra views
        detailExtraHumidity.setText(humidity);
        detailExtraWind.setText(wind);
        detailExtraPressure.setText(pressure);
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
