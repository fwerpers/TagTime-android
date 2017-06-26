package com.fwerpers.timeprof;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.fwerpers.timeprof.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TPController extends AppCompatActivity {
	private ToggleButton tog;
	private SharedPreferences mSettings;

	public static boolean mRunning;
	public static final String KEY_RUNNING = "running";

	public static boolean DEBUG = false;

	private ActionBar mAction;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tagtime_mainscreen);



		mAction = getSupportActionBar();
        mAction.setHomeButtonEnabled( false );
        mAction.setIcon( R.drawable.tagtime_03 );

        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mRunning = mSettings.getBoolean(KEY_RUNNING, true);

		tog = (ToggleButton) findViewById(R.id.btnTog);
		tog.setChecked(mRunning);
		tog.setOnClickListener(mTogListener);

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

		TextView view = (TextView) findViewById(R.id.Viewlog);
		view.setClickable(true);
		view.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				startLog();
			}
		});
		TextView exp = (TextView) findViewById(R.id.ExportLink);
		exp.setClickable(true);
		exp.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				startExport();
			}
		});
		TextView pref = (TextView) findViewById(R.id.PreferencesLink);
		pref.setClickable(true);
		pref.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				startPreferences();
			}
		});
		TextView charts = (TextView) findViewById(R.id.ChartsLink);
		charts.setClickable(true);
		charts.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				startCharts();
			}
		});
		TextView nextPing = (TextView) findViewById(R.id.NextPing);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
		Long nextPingTime = mSettings.getLong(PingService.KEY_NEXT, -1);
		Date pingDate = new Date(nextPingTime * 1000);
		nextPing.setText(sdf.format(pingDate));

//		boolean gotBeeminder = TimeProf.checkBeeminder();
//		TextView beeminder = (TextView) findViewById(R.id.BeeminderLink);
//		beeminder.setClickable(true);
//		if (gotBeeminder) {
//			beeminder.setOnClickListener(new OnClickListener() {
//				public void onClick(View arg0) {
//					startBeeminderLinks();
//				}
//			});
//		} else {
//			beeminder.setOnClickListener(new OnClickListener() {
//				public void onClick(View arg0) {
//					Toast toast = Toast.makeText(TPController.this,
//							"You must install the Beeminder app to use this feature", Toast.LENGTH_SHORT);
//					TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
//					if (v != null) v.setGravity(Gravity.CENTER);
//					toast.show();
//
//				}
//			});
//		}
	}

	public void startExport() {
		Intent exp = new Intent();
		exp.setClass(this, ManageData.class);
		startActivity(exp);
	}

	public void startLog() {
		Intent log = new Intent();
		log.setClass(this, ViewLog.class);
		startActivity(log);
	}

	public void startPreferences() {
		Intent pref = new Intent();
		pref.setClass(this, Preferences.class);
		startActivity(pref);
	}

//	public void startBeeminderLinks() {
//		Intent goals = new Intent();
//		goals.setClass(this, ViewGoals.class);
//		startActivity(goals);
//	}

	public void startCharts() {
		Log.d("DEBUG","TEST");
		Intent chartsIntent = new Intent(this, ChartsActivity.class);
		startActivity(chartsIntent);
	}

	public void setAlarm() {
		startService(new Intent(this, PingService.class));
	}

	public void cancelAlarm() {
		AlarmManager alarum = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarum.cancel(PendingIntent.getService(this, 0, new Intent(this, PingService.class), 0));
	}

	private OnClickListener mTogListener = new OnClickListener() {
		public void onClick(View v) {
			SharedPreferences.Editor editor = mSettings.edit();

			// Perform action on clicks
			if (tog.isChecked()) {
				Toast.makeText(TPController.this, "Pings ON", Toast.LENGTH_SHORT).show();
				mRunning = true;
				editor.putBoolean(KEY_RUNNING, mRunning);
				setAlarm();
			} else {
				Toast.makeText(TPController.this, "Pings OFF", Toast.LENGTH_SHORT).show();
				mRunning = false;
				editor.putBoolean(KEY_RUNNING, mRunning);
				cancelAlarm();
			}
			editor.commit();
		}
	};

}