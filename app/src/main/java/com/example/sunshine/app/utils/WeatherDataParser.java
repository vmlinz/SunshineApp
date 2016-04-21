package com.example.sunshine.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import com.example.sunshine.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by vmlinz on 1/26/16.
 */
public class WeatherDataParser {
    private static final String TAG = WeatherDataParser.class.getSimpleName();
    private Context context;

    public WeatherDataParser(Context context) {
        this.context = context;
    }

    private String getReadableDateString(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd");
        return simpleDateFormat.format(time);
    }

    private String formatHighLows(double high, double low, String unit) {
        if (unit.equals(context.getString(R.string.pref_units_value_imperial))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unit.equals(context.getString(R.string.pref_units_value_metric))) {
            Log.d(TAG, "Unit type not found: " + unit);
        }

        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    public String[] getWeatherDataFromJson(String forcastJsonStr, int numDays)
            throws JSONException {
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forcastJsonObject = new JSONObject(forcastJsonStr);
        JSONArray weatherArray = forcastJsonObject.getJSONArray(OWM_LIST);

        Time dayTime = new Time();
        dayTime.setToNow();

        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        dayTime =  new Time();

        String[] resultStrs = new String[numDays];

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String unit = sharedPreferences.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_value_metric));
        for (int i = 0; i < weatherArray.length(); i++) {
            String day;
            String desc;
            String highAndLow;

            JSONObject dayForcast = weatherArray.getJSONObject(i);

            long dateTime;
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            JSONObject weather = dayForcast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            desc = weather.getString(OWM_DESCRIPTION);

            JSONObject temperature = dayForcast.getJSONObject(OWM_TEMPERATURE);
            double high = temperature.getDouble(OWM_MAX);
            double low = temperature.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low, unit);
            resultStrs[i] = day + " - " + desc + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            Log.v(TAG, "Forecast entry: " + s);
        }

        return resultStrs;
    }
}
