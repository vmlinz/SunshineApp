package com.example.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private static final String TAG = ForecastFragment.class.getSimpleName();

    private ArrayList<String> forecastStrings = null;
    private ArrayAdapter<String> forecastAdapter = null;

    public ForecastFragment() {
        String[] forecastStringsArray = {
        };
        forecastStrings = new ArrayList<>(Arrays.asList(forecastStringsArray));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        forecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                forecastStrings);
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        ListView lv = (ListView) root.findViewById(R.id.listview_forecast);
        lv.setAdapter(forecastAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String forcast = forecastAdapter.getItem(i);
                Intent intent = new Intent(getContext(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forcast);
                startActivity(intent);
            }
        });
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String post = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        Log.d(TAG, post);
        new FetchWeatherTask(this.getActivity(), forecastAdapter).execute(post);
    }

//    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
//        @Override
//        protected String[] doInBackground(String... params) {
//            WeatherDataParser parser = new WeatherDataParser(getActivity());
//            String weatherData = getWeatherData(params);
//            try {
//                Log.d(TAG, weatherData);
//                return parser.getWeatherDataFromJson(weatherData, 7);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String[] strings) {
//            if (strings != null) {
//                forecastAdapter.clear();
//                forecastAdapter.addAll(new ArrayList<String>(Arrays.asList(strings)));
//            }
//        }
//    }
//
//    private String getWeatherData(String... params) {
//        if (params.length == 0) {
//            return null;
//        }
//
//// These two need to be declared outside the try/catch
//// so that they can be closed in the finally block.
//        HttpURLConnection urlConnection = null;
//        BufferedReader reader = null;
//
//// Will contain the raw JSON response as a string.
//        String forecastJsonStr = null;
//
//        try {
//            // Construct the URL for the OpenWeatherMap query
//            // Possible parameters are available at OWM's forecast API page, at
//            // http://openweathermap.org/API#forecast
//            final String QUERY_PARAM = "q";
//            final String FORMAT_PARAM= "mode";
//            final String UNITS_PARAM = "units";
//            final String DAYS_PARAM = "cnt";
//            final String APPID_PARAM = "APPID";
//            String format = "json";
//            String units = "metric";
//            int numDays = 7;
//
//            Uri builtUri = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily?").buildUpon()
//                    .appendQueryParameter(QUERY_PARAM, params[0])
//                    .appendQueryParameter(FORMAT_PARAM, format)
//                    .appendQueryParameter(UNITS_PARAM, units)
//                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                    .appendQueryParameter(APPID_PARAM, "3046ad11a19532df369035b6239c44cb")
//                    .build();
//
//            Log.d(TAG, builtUri.toString());
//            URL url = new URL(builtUri.toString());
//
//            // Create the request to OpenWeatherMap, and open the connection
//            urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setRequestMethod("GET");
//            urlConnection.connect();
//
//            // Read the input stream into a String
//            InputStream inputStream = urlConnection.getInputStream();
//            StringBuffer buffer = new StringBuffer();
//            if (inputStream == null) {
//                // Nothing to do.
//                forecastJsonStr = null;
//            }
//            reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
//                // But it does make debugging a *lot* easier if you print out the completed
//                // buffer for debugging.
//                buffer.append(line + "\n");
//            }
//
//            if (buffer.length() == 0) {
//                // Stream was empty.  No point in parsing.
//                forecastJsonStr = null;
//            }
//            forecastJsonStr = buffer.toString();
//            Log.d(TAG, "getWeatherData: forecastJson: " + forecastJsonStr);
//            return forecastJsonStr;
//        } catch (IOException e) {
//            Log.e("PlaceholderFragment", "Error ", e);
//            // If the code didn't successfully get the weather data, there's no point in attempting
//            // to parse it.
//            forecastJsonStr = null;
//        } finally{
//            if (urlConnection != null) {
//                urlConnection.disconnect();
//            }
//            if (reader != null) {
//                try {
//                    reader.close();
//                } catch (final IOException e) {
//                    Log.e("PlaceholderFragment", "Error closing stream", e);
//                }
//            }
//        }
//        return null;
//    }
}
