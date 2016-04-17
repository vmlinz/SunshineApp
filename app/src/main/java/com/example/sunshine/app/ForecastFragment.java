package com.example.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sunshine.app.data.WeatherContract;
import com.example.sunshine.app.data.WeatherUtils;
import com.example.sunshine.app.sync.SyncAdapter;
import com.orhanobut.logger.Logger;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ForecastFragment.class.getSimpleName();
    private static final int LOADER_ID = 1;
    public static final String FORECAST_LIST_POSITION = "FORECAST_LIST_POSITION";

    private ForecastAdapter forecastAdapter = null;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_CONDITION_ID = 5;
    static final int COL_LOCATION_SETTING = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private String mLocation;
    private Callback mCallback;
    private int mListPosition = 0;
    private ListView mListView;
    private boolean mUseSpecialToday;
    private TextView mEmptyTextView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (Callback) context;
        } catch (ClassCastException e) {
            Logger.e(e, "context is not instance of Callback");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        mListView = (ListView) root.findViewById(R.id.listview_forecast);
        mEmptyTextView = (TextView) root.findViewById(R.id.textview_forcast_empty);
        mListView.setEmptyView(mEmptyTextView);
        mListView.setAdapter(forecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) forecastAdapter.getItem(i);
                String locationSetting = Utils.getPreferredLocation(getActivity());
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting, cursor.getLong(COL_WEATHER_DATE));

                // send weather uri to callback
                mCallback.onItemSelected(weatherUri);

                mListPosition = i;
            }
        });


        if (savedInstanceState != null && savedInstanceState.containsKey(FORECAST_LIST_POSITION)) {
            mListPosition = savedInstanceState.getInt(FORECAST_LIST_POSITION);
        }

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // get list position and restore it
        if (savedInstanceState != null) {
            mListPosition = savedInstanceState.getInt(FORECAST_LIST_POSITION, 0);
            Logger.d("onActivityCreated: mListPosition = " + mListPosition);
        }

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onResume() {
        super.onResume();

        String location = Utils.getPreferredLocation(getActivity());

        if (location != null && !location.equals(mLocation)) {
            onLocationChanged();
        }

        mLocation = location;

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public ForecastFragment() {
    }

    public void setUseSpecialToday(boolean useSpecialToday) {
        mUseSpecialToday = useSpecialToday;
        if (forecastAdapter != null) {
            forecastAdapter.setUseSpecialToady(mUseSpecialToday);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mListPosition != ListView.INVALID_POSITION) {
            outState.putInt(FORECAST_LIST_POSITION, mListPosition);
            Logger.d("onSaveInstanceState: mListPosition = ", mListPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_location) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        Log.d(TAG, location);

        // FetchWeatherService.startActionFetchWeather(getContext(), location);
        // new FetchWeatherTask(getActivity()).execute(location);

//        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(getActivity(), FetchWeatherService.AlarmReceiver.class);
//        intent.setAction(FetchWeatherService.ACTION_FETCH_WEATHER);
//        intent.putExtra(FetchWeatherService.EXTRA_LOCATION_QUERY, location);
//        PendingIntent alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() + 5 * 1000, alarmIntent);

        SyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utils.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());
        Loader<Cursor> loader = new CursorLoader(
                this.getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            if (!checkNetworkStatus()) {
                mEmptyTextView.setText(getText(R.string.forecast_empty) + " " + getText(R.string.forecast_network_disconnected));
            }
        }
        forecastAdapter.swapCursor(data);

        if (mListPosition != ListView.INVALID_POSITION) {
            Logger.d("onLoadFinished: mListPosition = " + mListPosition);
            // scroll to saved list view position
            mListView.smoothScrollToPosition(mListPosition);
        }
    }

    private boolean checkNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) this.getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return (networkInfo != null) && networkInfo.isConnected();
        }

        return false;
    }

    @Override
    public void onLoaderReset(Loader loader) {
        forecastAdapter.swapCursor(null);
    }

    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void openPreferredLocationInMap() {
        if (null != forecastAdapter) {
            Cursor cursor = forecastAdapter.getCursor();
            if (null != cursor) {
                cursor.moveToPosition(0);
                String latitude = cursor.getString(COL_COORD_LAT);
                String longitude = cursor.getString(COL_COORD_LONG);

                Uri geoLocation = Uri.parse("geo:" + latitude + "," + longitude);

                // set implicit intent to view location on a map
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                Logger.t(TAG).d(geoLocation.toString());

                // try to resolve and start the map activity
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(TAG, "openPreferredLocationInMap: couldn't call " + geoLocation + "");
                }
            }
        }

    }

    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            switch (WeatherUtils.getLocationStatus(ForecastFragment.this.getContext())) {
                case WeatherUtils.LOCATION_STATUS_SERVER_DOWN:
                    ForecastFragment.this.mEmptyTextView.setText(R.string.forecast_empty_server_down);
                    break;
                case WeatherUtils.LOCATION_STATUS_SERVER_INVALID:
                    ForecastFragment.this.mEmptyTextView.setText(R.string.forecast_empty_server_invalid);
                    break;
                default:
                    break;
            }
        }
    };

    public interface Callback {
        void onItemSelected(Uri weatherUri);
    }
}
