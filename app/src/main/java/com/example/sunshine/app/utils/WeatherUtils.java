package com.example.sunshine.app.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.text.format.Time;

import com.example.sunshine.app.BuildConfig;
import com.example.sunshine.app.R;
import com.example.sunshine.app.data.WeatherContract;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

/**
 * Created by vmlinz on 3/15/16.
 */
public class WeatherUtils {

    private static final float DEFAULT_LATITUDE = 0f;
    private static final float DEFAULT_LONGITUDE = 0f;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LOCATION_STATUS_OK, LOCATION_STATUS_SERVER_DOWN,
            LOCATION_STATUS_SERVER_INVALID, LOCATION_STATUS_LOCATION_INVALID,
            LOCATION_STATUS_UNKNOWN})
    public @interface LocationStatus {
    }

    public static final int LOCATION_STATUS_OK = 0;
    public static final int LOCATION_STATUS_SERVER_DOWN = 1;
    public static final int LOCATION_STATUS_SERVER_INVALID = 2;
    public static final int LOCATION_STATUS_LOCATION_INVALID = 3;
    public static final int LOCATION_STATUS_UNKNOWN = 4;

    private static final String LOG_TAG = WeatherUtils.class.getSimpleName();

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     *
     * @param context
     * @param location
     */
    public static void handleActionFetchWeather(Context context, String location) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        String locationQuery = location;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 14;

        String locationLatitude = String.valueOf(WeatherUtils.getLocationLatitude(context));
        String locationLongitude = String.valueOf(WeatherUtils.getLocationLongitude(context));

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String LAT_PARAM = "lat";
            final String LON_PARAM = "lon";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";

            Uri.Builder builder = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY);

            if (WeatherUtils.isLocationLatLonAvailable(context)) {
                builder.appendQueryParameter(LAT_PARAM, locationLatitude)
                        .appendQueryParameter(LON_PARAM, locationLongitude);
            } else {
                builder.appendQueryParameter(QUERY_PARAM, locationQuery);
            }

            Uri builtUri = builder.build();

            resetLocationStatus(context);

            URL url = null;
            try {
                url = new URL(builtUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                setLocationStatus(context, LOCATION_STATUS_SERVER_INVALID);
                return;
            }
            Logger.t(LOG_TAG).d(url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Logger.t(LOG_TAG).e(e, "Error");
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            setLocationStatus(context, LOCATION_STATUS_LOCATION_INVALID);
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Logger.t(LOG_TAG).e(e, "Error closing stream");
                }
            }
        }

        Logger.t(LOG_TAG).json(forecastJsonStr);

        try {
            getWeatherDataFromJson(context, forecastJsonStr, locationQuery);
        } catch (JSONException e) {
            Logger.t(LOG_TAG).e(e, e.getMessage());
            e.printStackTrace();
            setLocationStatus(context, LOCATION_STATUS_LOCATION_INVALID);
            return;
        }

        setLocationStatus(context, LOCATION_STATUS_OK);
    }


    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param locationSetting The location string used to request updates from the server.
     * @param cityName        A human-readable city name, e.g "Mountain View"
     * @param lat             the latitude of the city
     * @param lon             the longitude of the city
     * @return the row ID of the added location.
     */
    public static long addLocation(Context context, String locationSetting, String cityName, double lat, double lon) {
        // Students: First, check if the location with this city name exists in the db
        // If it exists, return the current ID
        // Otherwise, insert it using the content resolver and the base URI
        long row = -1;
        Uri uri;

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(WeatherContract.LocationEntry.CONTENT_URI,
                null, WeatherContract.LocationEntry.COLUMN_CITY_NAME + "=?", new String[]{cityName}, null);

        ContentValues cv = new ContentValues();
        cv.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
        cv.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
        cv.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
        cv.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);

        if (cursor != null && cursor.moveToFirst()) {
            // update the old record
            row = contentResolver.update(WeatherContract.LocationEntry.CONTENT_URI,
                    cv, WeatherContract.LocationEntry.COLUMN_CITY_NAME + "=?", new String[]{cityName});
        } else {
            uri = context.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, cv);
            row = ContentUris.parseId(uri);
        }

        if (cursor != null) {
            cursor.close();
        }
        return row;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private static void getWeatherDataFromJson(Context context, String forecastJsonStr,
                                               String locationSetting)
            throws JSONException {

        // Now we have a String representing the complete forecast in JSON Format.
        // Fortunately parsing is easy:  constructor takes the JSON string and converts it
        // into an Object hierarchy for us.

        // These are the names of the JSON objects that need to be extracted.

        // Location information
        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_MESSAGE_CODE = "cod";

        // Location coordinate
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);

        JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = cityCoord.getDouble(OWM_LATITUDE);
        double cityLongitude = cityCoord.getDouble(OWM_LONGITUDE);

        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int messageCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (messageCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    setLocationStatus(context, LOCATION_STATUS_LOCATION_INVALID);
                    return;
                default:
                    setLocationStatus(context, LOCATION_STATUS_SERVER_DOWN);
                    return;
            }
        }

        long locationId = addLocation(context, locationSetting, cityName, cityLatitude, cityLongitude);

        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<>(weatherArray.length());

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        for (int i = 0; i < weatherArray.length(); i++) {
            // These are the values that will be collected.
            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String description;
            int weatherId;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay + i);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            // Description is in a child array called "weather", which is 1 element long.
            // That element also contains a weather code.
            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTime);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);
        }

        // add to database
        int inserted = 0;
        if (cVVector.size() > 0) {
            // Student: call bulkInsert to add the weatherEntries to the database here
            ContentValues[] array = cVVector.toArray(new ContentValues[cVVector.size()]);
            inserted = context.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, array);
        }

        // clean up old data
        int deleted = deleteOldData(context);

        Logger.t(LOG_TAG).d("Database records inserted: " + inserted + " and old records deleted: " + deleted);
    }

    private static int deleteOldData(Context context) {
        Time dayTime = new Time();
        dayTime.setToNow();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        return context.getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?",
                new String[]{Long.toString(dayTime.setJulianDay(julianStartDay - 1))});
    }

    public static void setLocationStatus(Context context, @LocationStatus int locationStatus) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putInt(context.getString(R.string.pref_location_status_key), locationStatus).apply();
    }

    public static void resetLocationStatus(Context context) {
        setLocationStatus(context, LOCATION_STATUS_UNKNOWN);
    }

    public static int getLocationStatus(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(context.getString(R.string.pref_location_status_key), LOCATION_STATUS_UNKNOWN);
    }

    public static boolean isLocationLatLonAvailable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.contains(context.getString(R.string.pref_location_lat))
                && preferences.contains(context.getString(R.string.pref_location_lon));
    }

    public static float getLocationLatitude(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat(context.getString(R.string.pref_location_lat), DEFAULT_LATITUDE);
    }

    public static float getLocationLongitude(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getFloat(context.getString(R.string.pref_location_lon), DEFAULT_LONGITUDE);
    }
}
