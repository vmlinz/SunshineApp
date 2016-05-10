package com.example.sunshine.app.features.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.sunshine.app.R;
import com.example.sunshine.app.features.detail.DetailActivity;
import com.example.sunshine.app.features.detail.DetailFragment;
import com.example.sunshine.app.features.settings.SettingsActivity;
import com.example.sunshine.app.services.RegistrationIntentService;
import com.example.sunshine.app.sync.SyncAdapter;
import com.example.sunshine.app.utils.CommonUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.orhanobut.logger.Logger;

public class MainActivity extends AppCompatActivity
        implements ForecastFragment.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    public static final String TWO_PANE = "TWO_PANE";
    private static final int PLAY_SERVEICES_RESOLUTION_REQUEST = 9000;
    private String mLocation;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0f);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mLocation = CommonUtils.getPreferredLocation(this);
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

        if (!checkPlayServices(this)) {
            finish();
        } else {
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

    }

    private boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(activity);

        if (result != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(result)) {
                googleApiAvailability.getErrorDialog(activity,
                        result,
                        PLAY_SERVEICES_RESOLUTION_REQUEST).show();
            } else {
                Logger.d("This device is not supported.");
            }

            return false;
        }

        Logger.d("Play Services are available on this device");
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        String location = CommonUtils.getPreferredLocation(this);
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if (null != ff) {
                ff.onLocationChanged();
            }

            DetailFragment df = (DetailFragment) getSupportFragmentManager()
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
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
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
        }

        return super.onOptionsItemSelected(item);
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
    public void onItemSelected(Uri weatherUri, ForecastAdapter.ViewHolder holder) {
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

            ActivityOptionsCompat activityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                            new Pair<View, String>(holder.iconImageView, getString(R.string.detail_icon_transition_name)));
            ActivityCompat.startActivity(this, intent, activityOptionsCompat.toBundle());
        }
    }
}
