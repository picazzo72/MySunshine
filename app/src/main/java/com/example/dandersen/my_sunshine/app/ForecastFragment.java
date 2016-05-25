package com.example.dandersen.my_sunshine.app;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dandersen.my_sunshine.app.data.WeatherContract;

import java.util.List;


/**
 * Created by Dan on 02-05-2016.
 */
public class ForecastFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private ForecastAdapter mForecastAdapter;

    private boolean mTwoPaneLayout = false;

    // Our forecast list view
    private ListView mListView;

    // Keeping track of the selected item
    private int mPosition = ListView.INVALID_POSITION;

    // Tag for save instance bundle
    private static final String SELECTED_KEY = "selected_position";

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private static final int CURSOR_LOADER_ID = 0;

    private boolean mRestartingLoader = false;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
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
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }


    public ForecastFragment() {
    }

    public void setTwoPaneLayout(boolean twoPaneLayout) {
        mTwoPaneLayout = twoPaneLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setTwoPaneLayout(twoPaneLayout);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The following line makes this fragment handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mForecastAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "DSA LOG - onLoadFinished");

        // Select first item or last selected if this is available
        int listSelection = 0;
        if (mPosition != ListView.INVALID_POSITION) {
            listSelection = mPosition;
        }
        class SelectListItem implements Runnable {
            int mListSelection;
            SelectListItem(int sel) { mListSelection = sel; }
            public void run() {
                if (!mRestartingLoader && mListView.getCount() == 0) {
                    // If there are no items in the list we update the database with weather info
                    mRestartingLoader = true; // Boolean to ensure that we do not loop indefinitely
                    onLocationChanged();
                }
                else {
                    mRestartingLoader = false;
                    if (mListSelection < mListView.getCount()) {
                        mPosition = mListSelection;
                        if (mTwoPaneLayout) {
                            mListView.performItemClick(
                                    mListView.getChildAt(mPosition),
                                    mPosition,
                                    mListView.getChildAt(mPosition).getId());
                        }
                        mListView.smoothScrollToPosition(mPosition);
                    }
                }
            }
        }
        mListView.post(new SelectListItem(listSelection));

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order: Acending, by date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        // Build Uri
        String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Log.v(LOG_TAG, "DSA LOG - onCreateLoader - URI for weather location w. start date: " + weatherForLocationUri.toString());

        return new CursorLoader(getActivity(),
                weatherForLocationUri, // URI
                FORECAST_COLUMNS,      // projection
                null,                  // where
                null,                  // binds
                sortOrder);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    // TODO: Remove this which would always fetch weather from the web service
//    @Override
//    public void onStart() {
//        super.onStart();
//        updateWeather();
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The CursorAdapter will take data from a source and
        // use it to populate the ListView it's attached to
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setTwoPaneLayout(mTwoPaneLayout);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it
        mListView = (ListView)rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);

        // Create on item click listener for the ListView
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor c = (Cursor) adapterView.getItemAtPosition(position);
                if (c != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Callback cb = (Callback) getActivity();
                    cb.onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, c.getLong(COL_WEATHER_DATE)
                            ));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things. It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        // Prepare the loader.  Either re-connect with an existing one, or start a new one.
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        return rootView;
    }

    public void onLocationChanged() {
        // Update location in action bar
        String location = Utility.getPreferredLocation(getActivity());
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setSubtitle(location);

        // Update weather data
        updateWeather();

        // Restart loader to update UI
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    private void updateWeather() {
        Log.v(LOG_TAG, "DSA LOG - Weather is being updated");

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());

        // Execute fetch weather task
        weatherTask.execute(location);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mPosition != ListView.INVALID_POSITION) {
            savedInstanceState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

}
