package com.fwerpers.tagtime;

import java.util.Arrays;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

public class Preferences extends PreferenceActivity {

	private ActionBar mAction;
	
    // Dialog IDs
    public static final int ABOUT_DIALOG = 0;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

//		mAction = getSupportActionBar();
//		mAction.setHomeButtonEnabled(true);
//		mAction.setIcon(R.drawable.tagtime_03);

		EditTextPreference gapTimePreference = (EditTextPreference) findPreference("pingGap");
		gapTimePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int oldVal = Integer.parseInt(((EditTextPreference)preference).getText());
				int newVal = Integer.parseInt(newValue.toString());
				Log.d("DEBUG", Integer.toString(oldVal));
				Log.d("DEBUG", Integer.toString(newVal));
				Intent intent = new Intent(Preferences.this, PingService.class);
				intent.setAction(Constants.ACTION_GAP_CHANGED);
				startService(intent);
				return(true);
			}
		});

		ListPreference order = (ListPreference) findPreference("sortOrderPref");

		order.setSummary(order.getEntry());

		// Set the summary text of the order when it changes
		order.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				ListPreference x = (ListPreference) preference;
				int index = Arrays.asList(x.getEntryValues()).indexOf(newValue);
				preference.setSummary(x.getEntries()[index]);
				return true;
			}
		});

		CheckBoxPreference vibrate = (CheckBoxPreference) findPreference("pingVibrate");

		vibrate.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ((Boolean) newValue) {
					Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
					v.vibrate(25);
				}
				return true;
			}
		});

        Preference backup = (Preference) findPreference( "backupPref" );
        if (backup != null) {
        	backup.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick( Preference preference ) {
                    return true;
                }
            } );
        }

        Preference restore = (Preference) findPreference( "restorePref" );
        if (restore!= null) {
        	restore.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick( Preference preference ) {
                    return true;
                }
            } );
        }

        Preference about = (Preference) findPreference( "aboutPref" );
        if (about != null) {
            about.setOnPreferenceClickListener( new Preference.OnPreferenceClickListener() {

                public boolean onPreferenceClick( Preference preference ) {
                    showDialog( ABOUT_DIALOG );
                    return true;
                }
            } );
        }
	}
	
    @Override
    protected Dialog onCreateDialog( int id, Bundle args ) {
        switch (id) {
        case ABOUT_DIALOG:
            Dialog about = new DialogAbout( Preferences.this, R.style.about_dialog );
            return about;
        }
        return null;
    }

    /** Handles menu item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, TPController.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
