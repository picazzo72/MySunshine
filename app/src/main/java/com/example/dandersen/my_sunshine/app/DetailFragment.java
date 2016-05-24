package com.example.dandersen.my_sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dandersen.my_sunshine.app.data.WeatherContract;

public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int DETAIL_LOADER_ID = 0;
    private String mForecast;
    private ShareActionProvider mShareActionProvider;
    private Uri mUri;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] DETAIL_COLUMNS = {
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID                     = 0;
    static final int COL_WEATHER_DATE                   = 1;
    static final int COL_WEATHER_DESC                   = 2;
    static final int COL_WEATHER_MAX_TEMP               = 3;
    static final int COL_WEATHER_MIN_TEMP               = 4;
    static final int COL_WEATHER_CONDITION_ID           = 5;
    static final int COL_HUMIDITY                       = 6;
    static final int COL_WIND_SPEED                     = 7;
    static final int COL_WIND_DEGREES                   = 8;
    static final int COL_PRESSURE                       = 9;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static class ViewHolder {
        public final TextView friendlyDateView;
        public final TextView dateView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final ImageView iconView;
        public final TextView descriptionView;
        public final TextView humidityView;
        public final TextView windView;
        public final TextView pressureView;

        ViewHolder(View view) {
            friendlyDateView = (TextView) view.findViewById(R.id.detail_friendly_date_textview);
            dateView = (TextView) view.findViewById(R.id.detail_date_textview);
            highTempView = (TextView) view.findViewById(R.id.detail_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
            iconView = (ImageView) view.findViewById(R.id.detail_icon);
            descriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            humidityView = (TextView) view.findViewById(R.id.detail_humidity_textview);
            windView = (TextView) view.findViewById(R.id.detail_wind_textview);
            pressureView = (TextView) view.findViewById(R.id.detail_pressure_textview);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get Uri from the arguments sent from the caller
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        // Set view holder for easy access to view items
        view.setTag(new ViewHolder(view));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null ) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
        else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) { return; }

        // Get view holder
        ViewHolder viewHolder = (ViewHolder) getView().getTag();

        // Date
        long date = data.getLong(COL_WEATHER_DATE);
        viewHolder.friendlyDateView.setText(Utility.getDayName(getActivity(), date));
        viewHolder.dateView.setText(Utility.formatDate(data.getLong(COL_WEATHER_DATE)));

        // Temperatures
        double highTemp = data.getDouble(COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(getActivity(), highTemp));
        double lowTemp = data.getDouble(COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(getActivity(), lowTemp));

        // Weather icon
        int weatherId = data.getInt(COL_WEATHER_CONDITION_ID);
        int imageResource = Utility.getArtResourceForWeatherCondition(weatherId);
        if (imageResource != -1) {
            viewHolder.iconView.setImageResource(imageResource);
        }

        // Weather description
        String weatherDescription = data.getString(COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(weatherDescription);

        // Humidity
        float humidity = data.getFloat(COL_HUMIDITY);
        viewHolder.humidityView.setText(getActivity().getString(R.string.format_humidity, humidity));

        // Wind
        float windSpeed = data.getFloat(COL_WIND_SPEED);
        float windDegrees = data.getFloat(COL_WIND_DEGREES);
        viewHolder.windView.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDegrees));

        // Pressure
        float pressure = data.getFloat(COL_PRESSURE);
        viewHolder.pressureView.setText(getActivity().getString(R.string.format_pressure, pressure));

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            Log.v(LOG_TAG, "DSA LOG - URI for detail view: " + mUri.toString());

            return new CursorLoader(getActivity(),
                    mUri,      // URI
                    DETAIL_COLUMNS,        // projection
                    null,                  // where
                    null,                  // binds
                    null);
        }

        return null;
    }

    void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        if (mUri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(mUri);
            mUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            getLoaderManager().restartLoader(DETAIL_LOADER_ID, null, this);
        }
    }
}
