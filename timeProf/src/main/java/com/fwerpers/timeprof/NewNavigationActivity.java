package com.fwerpers.timeprof;

import android.content.Intent;
import android.preference.PreferenceFragment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

public class NewNavigationActivity extends AppCompatActivity {

    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_navigation);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation);

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
                            case R.id.nav_notification_switch:
                                break;
                        }
                        return true;
                    }
                });
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

    private void switchToSettingsFragment() {
        SettingsFragment fragment = new SettingsFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
        Log.d("DEBUG", "Settings");
    }

    private void startSettingsActivity() {
        Intent pref = new Intent();
        pref.setClass(this, Preferences.class);
        startActivity(pref);
    }
}
