package com.fwerpers.timeprof;

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
                        menuItem.setChecked(true);
                        getSupportActionBar().setTitle(menuItem.getTitle());
                        int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.nav_overview:
                                switchToOverviewFragment();
                                break;
                            case R.id.nav_log:
                                switchToLogFragment();
                                break;
                            case R.id.nav_stats:
                                switchToStatsFragment();
                                break;
                            case R.id.nav_settings:
                                switchToSettingsFragment();
                                break;
                        }
                        mDrawerLayout.closeDrawers();
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
        Fragment fragment = new SettingsFragment();
        switchToFragment(fragment);
        Log.d("DEBUG", "Settings");
    }
}
