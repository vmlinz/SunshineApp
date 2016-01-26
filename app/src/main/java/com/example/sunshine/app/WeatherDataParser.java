package com.example.sunshine.app;

import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by vmlinz on 1/26/16.
 */
public class WeatherDataParser {
    private static final String TAG = WeatherDataParser.class.getSimpleName();

    private static WeatherDataParser parser = null;

    private WeatherDataParser() {
    }

    public static WeatherDataParser getInstance() {
        if (parser == null) {
            parser = new WeatherDataParser();
        }

        return parser;
    }


    private String getReadableDateString(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd");
        return simpleDateFormat.format(time);
    }

    private String formatHighLows(double high, double low) {
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

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + desc + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            Log.v(TAG, "Forecast entry: " + s);
        }

        return resultStrs;
    }
}
