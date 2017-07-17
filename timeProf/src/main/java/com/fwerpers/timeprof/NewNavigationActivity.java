package com.fwerpers.timeprof;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

public class NewNavigationActivity extends AppCompatActivity {

    public static final String KEY_RUNNING = "running";

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private SharedPreferences mSettings;
    public static boolean mRunning;

    private ToggleButton mNotificationToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_navigation);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        mRunning = mSettings.getBoolean(KEY_RUNNING, true);

        mNotificationToggle = (ToggleButton) mNavigationView.getMenu().findItem(R.id.nav_notification_toggle).getActionView();
        mNotificationToggle.setChecked(mRunning);
        mNotificationToggle.setOnClickListener(mTogListener);

        // TODO: verify that reinstall should be the only time
        // that mRunning would be stored as "On" without having an alarm set
        // for the next ping time...
        // if (mRunning) {
        Integer stored = Integer.parseInt(mSettings.getString("KEY_APP_VERSION", "-1"));
        Integer manifest = Integer.parseInt(getText(R.string.app_version).toString());
        if (stored < manifest || mSettings.getLong(PingService.KEY_NEXT, -1) < 0
                || mSettings.getLong(PingService.KEY_SEED, -1) < 0) {
            startService(new Intent(this, PingService.class));
        }
        // }

        //setting up selected item listener
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.nav_overview:
                                menuItem.setChecked(true);
                                getSupportActionBar().setTitle(menuItem.getTitle());
                                switchToOverviewFragment();
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_log:
                                menuItem.setChecked(true);
                                getSupportActionBar().setTitle(menuItem.getTitle());
                                switchToLogFragment();
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_stats:
                                menuItem.setChecked(true);
                                getSupportActionBar().setTitle(menuItem.getTitle());
                                switchToStatsFragment();
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_settings:
                                //switchToSettingsFragment();
                                startSettingsActivity();
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_notification_toggle:
                                break;
                        }
                        return true;
                    }
                });
    }

    private View.OnClickListener mTogListener = new View.OnClickListener() {
        public void onClick(View v) {
            SharedPreferences.Editor editor = mSettings.edit();

            // Perform action on clicks
            if (mNotificationToggle.isChecked()) {
                Toast.makeText(NewNavigationActivity.this, "Pings ON", Toast.LENGTH_SHORT).show();
                mRunning = true;
                editor.putBoolean(KEY_RUNNING, mRunning);
                setAlarm();
            } else {
                Toast.makeText(NewNavigationActivity.this, "Pings OFF", Toast.LENGTH_SHORT).show();
                mRunning = false;
                editor.putBoolean(KEY_RUNNING, mRunning);
                cancelAlarm();
            }
            editor.commit();
        }
    };

    public void setAlarm() {
        startService(new Intent(this, PingService.class));
    }

    public void cancelAlarm() {
        AlarmManager alarum = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarum.cancel(PendingIntent.getService(this, 0, new Intent(this, PingService.class), 0));
    }

    private void switchToFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private void switchToOverviewFragment() {
        Fragment fragment = new OverviewFragment();
        switchToFragment(fragment);
        Log.d("DEBUG", "Overview");
    }

    private void switchToLogFragment() {
        Fragment fragment = new LogFragment();
        switchToFragment(fragment);
        Log.d("DEBUG", "Log");
    }

    private void switchToStatsFragment() {
        Fragment fragment = new StatsFragment();
        switchToFragment(fragment);
        Log.d("DEBUG", "Stats");
    }

    private void startSettingsActivity() {
        Intent pref = new Intent();
        pref.setClass(this, Preferences.class);
        startActivity(pref);
    }
}
