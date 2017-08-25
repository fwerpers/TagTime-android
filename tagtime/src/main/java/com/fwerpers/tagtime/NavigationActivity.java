package com.fwerpers.tagtime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import static com.fwerpers.tagtime.PingService.KEY_NEXT;

public class NavigationActivity extends AppCompatActivity {

    public static final String KEY_RUNNING = "running";

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private SharedPreferences mSettings;
    private Runnable mFragmentSwitchRunnable;
    private Handler mHandler;
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
        mHandler = new Handler();

        mNotificationToggle = (ToggleButton) mNavigationView.getMenu().findItem(R.id.nav_notification_toggle).getActionView();
        mNotificationToggle.setChecked(mRunning);
        mNotificationToggle.setOnClickListener(mTogListener);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (mFragmentSwitchRunnable != null) {
                    mHandler.post(mFragmentSwitchRunnable);
                    mFragmentSwitchRunnable= null;
                }
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerToggle.syncState();

        // Simulate the case for which the app freezes on start
        if (Constants.DEBUG_DATABASE_FREEZE) {
            SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = mPrefs.edit();
            long NEXT = PingService.now() - 60*60*24;
            editor.putLong(KEY_NEXT, NEXT);
            editor.commit();
        }

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

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    private View.OnClickListener mTogListener = new View.OnClickListener() {
        public void onClick(View v) {
            SharedPreferences.Editor editor = mSettings.edit();

            // Perform action on clicks
            if (mNotificationToggle.isChecked()) {
                Toast.makeText(NavigationActivity.this, "Pings ON", Toast.LENGTH_SHORT).show();
                mRunning = true;
                editor.putBoolean(KEY_RUNNING, mRunning);
                setAlarm();
            } else {
                Toast.makeText(NavigationActivity.this, "Pings OFF", Toast.LENGTH_SHORT).show();
                mRunning = false;
                editor.putBoolean(KEY_RUNNING, mRunning);
            }
            editor.commit();
        }
    };

    public void setAlarm() {
        startService(new Intent(this, PingService.class));
    }

    private void switchToFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().
                replace(R.id.content_frame, fragment).
                commit();
    }

    private void switchToFragmentOnDrawerClosed(final Fragment fragment) {
        mFragmentSwitchRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.content_frame, fragment);
                fragmentTransaction.commit();
            }
        };
    }

    private void switchToOverviewFragment() {
        Fragment fragment = new OverviewFragment();
        switchToFragment(fragment);
        Log.d("DEBUG", "Overview");
    }

    private void switchToLogFragment() {
        FrameLayout content = (FrameLayout)findViewById(R.id.content_frame);
        content.removeAllViews();
        Fragment fragment = new LogFragment();
        switchToFragmentOnDrawerClosed(fragment);
        Log.d("DEBUG", "Log");
    }

    private void switchToStatsFragment() {
        FrameLayout content = (FrameLayout)findViewById(R.id.content_frame);
        content.removeAllViews();
        Fragment fragment = new StatsFragment();
        switchToFragmentOnDrawerClosed(fragment);
        Log.d("DEBUG", "Stats");
    }

    private void startSettingsActivity() {
        Intent pref = new Intent();
        pref.setClass(this, Preferences.class);
        startActivity(pref);
    }
}
