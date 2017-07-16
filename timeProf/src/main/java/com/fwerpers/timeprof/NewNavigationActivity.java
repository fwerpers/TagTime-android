package com.fwerpers.timeprof;

import android.support.design.widget.NavigationView;
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
                        getSupportActionBar().setTitle(menuItem.getTitle());
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void switchToOverviewFragment() {
        Log.d("DEBUG", "Overview");
    }

    private void switchToLogFragment() {
        Log.d("DEBUG", "Log");
    }

    private void switchToStatsFragment() {
        Log.d("DEBUG", "Stats");
    }

    private void switchToSettingsFragment() {
        Log.d("DEBUG", "Settings");
    }
}
