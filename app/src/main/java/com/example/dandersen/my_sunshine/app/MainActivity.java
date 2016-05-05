package com.example.dandersen.my_sunshine.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Start settings activity with explicit intent
            Intent openSettingsActivityIntent = new Intent(this, SettingsActivity.class);
            startActivity(openSettingsActivityIntent);
            return true;
        }
        else if (id == R.id.action_view_preferred_location) {
            openPreferredLocationInMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        // Get location from settings
        String location = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        // Build geo location
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();
        Log.v(LOG_TAG, "Map url: " + geoLocation.toString());

        // Create implicit intent
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        mapIntent.setData(geoLocation);

        // Verify that the intent will resolve to an activity
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
        else {
            Toast.makeText(this, getString(R.string.error_msg_no_map), Toast.LENGTH_SHORT).show();
        }
    }
}
