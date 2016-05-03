package com.example.dandersen.my_sunshine.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dan on 03-05-2016.
 */
public class WeatherDataParser {

    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieves the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {
        final String kDayList = "list";
        final String kTemperature = "temp";
        final String kMaxTemperature = "max";

        // create JSONObect from string
        JSONObject jsonObject = new JSONObject(weatherJsonStr);
        // Retrieve day list as an array
        JSONArray dayArray = jsonObject.getJSONArray(kDayList);
        // Grab the requested index from the array
        assert dayIndex < dayArray.length();
        JSONObject singleDay = dayArray.getJSONObject(dayIndex);
        // Get the temperature properties from the JSONObject
        JSONObject tempObject = singleDay.getJSONObject(kTemperature);
        // Retrieve the max temperature from the temperature object
        return tempObject.getDouble(kMaxTemperature);
    }

}
