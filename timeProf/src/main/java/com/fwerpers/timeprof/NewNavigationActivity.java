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
                                Log.d("DEBUG", "Overview");
                                break;
                            case R.id.nav_log:
                                Log.d("DEBUG", "Log");
                                break;
                            case R.id.nav_stats:
                                Log.d("DEBUG", "Stats");
                                break;
                            case R.id.nav_settings:
                                Log.d("DEBUG", "Settings");
                                break;
                        }
                        getSupportActionBar().setTitle(menuItem.getTitle());
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }
}
