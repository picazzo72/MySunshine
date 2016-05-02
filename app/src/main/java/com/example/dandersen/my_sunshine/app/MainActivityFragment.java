package com.example.dandersen.my_sunshine.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> forecastEntries = new ArrayList<String>();
        forecastEntries.add("Today - Sunny - 88/63");
        forecastEntries.add("Tomorrow - Foggy - 70/46");
        forecastEntries.add("Weds - Cloudy - 72/62");
        forecastEntries.add("Thurs - Rainy - 64/51");
        forecastEntries.add("Fri - Foggy - 70/46");
        forecastEntries.add("Sat - Sunny - 76/83");
        forecastEntries.add("Today - Sunny - 88/63");
        forecastEntries.add("Tomorrow - Foggy - 70/46");
        forecastEntries.add("Weds - Cloudy - 72/62");
        forecastEntries.add("Thurs - Rainy - 64/51");
        forecastEntries.add("Fri - Foggy - 70/46");
        forecastEntries.add("Sat - Sunny - 76/83");
        forecastEntries.add("Today - Sunny - 88/63");
        forecastEntries.add("Tomorrow - Foggy - 70/46");
        forecastEntries.add("Weds - Cloudy - 72/62");
        forecastEntries.add("Thurs - Rainy - 64/51");
        forecastEntries.add("Fri - Foggy - 70/46");
        forecastEntries.add("Sat - Sunny - 76/83");

        mForecastAdapter = new ArrayAdapter<String>(
                // The current context (this fragment's parent activity)
                getActivity(),
                // ID of List item layout
                R.layout.list_item_forecast,
                // ID of the textview to populate
                R.id.list_item_forecast_textview,
                // Forecast data
                forecastEntries);

        ListView listView = (ListView)rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    ArrayAdapter<String> mForecastAdapter;
}
