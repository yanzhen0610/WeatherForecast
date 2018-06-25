/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tw.edu.tku.csie.weatherforecast.utilities;

import android.content.ContentValues;
//import android.content.Context;

import tw.edu.tku.csie.weatherforecast.data.WeatherAppContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * Utility functions to handle OpenWeatherMap JSON data.
 */
public final class OpenWeatherJsonUtils {

    /* Date time information */
    private static final String OWN_DATE_TIME = "dt";

    /* Weather information. Each day's forecast info is an element of the "list" array */
    private static final String OWM_LIST = "list";

    private static final String OWM_PRESSURE = "pressure";
    private static final String OWM_HUMIDITY = "humidity";

    private static final String OWN_WIND_INFO = "wind";
    private static final String OWM_WIND_SPEED = "speed";
    private static final String OWM_WIND_DIRECTION = "deg";

    private static final String OWN_MAIN_INFO = "main";

    /* Max temperature for the day */
    private static final String OWM_MAX = "temp_max";
    private static final String OWM_MIN = "temp_min";

    private static final String OWM_WEATHER = "weather";
    private static final String OWM_WEATHER_ID = "id";

    private static final String OWM_MESSAGE_CODE = "cod";

    /**
     * This method parses JSON from a web response and returns an array of Strings
     * describing the weather over various days from the forecast.
     * <p/>
     * Later on, we'll be parsing the JSON into structured data within the
     * getFullWeatherDataFromJson function, leveraging the data we have stored in the JSON. For
     * now, we just convert the JSON into human-readable strings.
     *
     * @param forecastJsonStr JSON response from server
     *
     * @return Array of Strings describing weather data
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ContentValues[] getWeatherContentValuesFromJson(String forecastJsonStr)
            throws JSONException {

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        /* Is there an error? */
        if (forecastJson.has(OWM_MESSAGE_CODE)) {
            int statusCode = forecastJson.getInt(OWM_MESSAGE_CODE);

            switch (statusCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray jsonWeatherArray = forecastJson.getJSONArray(OWM_LIST);

        ContentValues[] weatherContentValues = new ContentValues[jsonWeatherArray.length()];

        for (int i = 0; i < jsonWeatherArray.length(); i++) {

            long dateTimeMillis;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            int weatherId;

            /* Get the JSON object representing the day */
            JSONObject dayForecast = jsonWeatherArray.getJSONObject(i);

            /*
             * We ignore all the datetime values embedded in the JSON and assume that
             * the values are returned in-order by day (which is not guaranteed to be correct).
             */
            dateTimeMillis = dayForecast.getInt(OWN_DATE_TIME) * WeatherAppDateUtils.SEC_IN_MILLIS;

            JSONObject mainInfo = dayForecast.getJSONObject(OWN_MAIN_INFO);
            JSONObject windInfo = dayForecast.getJSONObject(OWN_WIND_INFO);

            pressure = mainInfo.getDouble(OWM_PRESSURE);
            humidity = mainInfo.getInt(OWM_HUMIDITY);

            windSpeed = windInfo.getDouble(OWM_WIND_SPEED);
            windDirection = windInfo.getDouble(OWM_WIND_DIRECTION);

            /*
             * Description is in a child array called "weather", which is 1 element long.
             * That element also contains a weather code.
             */
            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);

            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            /*
             * Temperatures are sent by Open Weather Map in a child object called "temp".
             *
             * Editor's Note: Try not to name variables "temp" when working with temperature.
             * It confuses everybody. Temp could easily mean any number of things, including
             * temperature, temporary variable, temporary folder, temporary employee, or many
             * others, and is just a bad variable name.
             */
            high = mainInfo.getDouble(OWM_MAX);
            low = mainInfo.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherAppContract.WeatherEntry.COLUMN_DATE_TIME, dateTimeMillis);
            weatherValues.put(WeatherAppContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherAppContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherAppContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherAppContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherAppContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherAppContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherAppContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            weatherContentValues[i] = weatherValues;
        }

        return weatherContentValues;
    }
}