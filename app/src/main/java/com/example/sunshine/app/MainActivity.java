package com.example.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.sunshine.app.sync.SyncAdapter;

public class MainActivity extends AppCompatActivity
        implements ForecastFragment.Callback{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    public static final String WEATHER_URI = "WEATHER_URI";
    public static final String TWO_PANE = "TWO_PANE";
    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mLocation = Utils.getPreferredLocation(this);
        if (findViewById(R.id.weather_detail_container) != null) {
            mTwoPane = true;

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            } else {
                mTwoPane = false;
                getSupportActionBar().setElevation(0f);
            }
        }

        // set two pane to forecast fragment
        ForecastFragment ff = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
        ff.setUseSpecialToday(!mTwoPane);

        // initialize sync adapter
        SyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String location = Utils.getPreferredLocation(this);
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
            }

            DetailFragment df = (DetailFragment)getSupportFragmentManager()
                    .findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if (null != df) {
                df.onLocationChanged(location);
            }

            mLocation = location;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_location) {
            openPreferredLocationInMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // get default location from shared preferences
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        // set implicit intent to view location on a map
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        // try to resolve and start the map activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "openPreferredLocationInMap: couldn't call " + location + "");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TWO_PANE, mTwoPane);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mTwoPane = savedInstanceState.getBoolean(TWO_PANE);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onItemSelected(Uri weatherUri) {
        if (mTwoPane) {
            DetailFragment detailFragment = new DetailFragment();
            Bundle bundle = new Bundle();

            bundle.putParcelable(DetailFragment.DETAIL_URI, weatherUri);
            detailFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(weatherUri);
            startActivity(intent);
        }
    }
}
