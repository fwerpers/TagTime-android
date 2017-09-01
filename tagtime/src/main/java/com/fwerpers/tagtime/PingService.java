package com.fwerpers.tagtime;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/* 
 * the Ping service is in charge of maintaining random number
 * generator state on disk, sending ping notifications, and
 * setting ping alarms.
 * 
 */

public class PingService extends Service {

	private static final String TAG = "PingService";
	private static final boolean LOCAL_LOGV = true && !TagTime.DISABLE_LOGV;

	private SharedPreferences mPrefs;
	private PingsDbAdapter pingsDB;
	private static PingService sInstance = null;

	// this gives a layout id which is a unique id
	private static int PING_NOTES = R.layout.tagtime_editping;

	public static final String KEY_NEXT = "nextping";
	public static final String KEY_SEED = "RNG_seed";
	private boolean mNotify;
	private int mGap;

	// seed is a variable that is really the state of the RNG.
	private long mSeed;
	private long mNext;
	private RandomTimeGenerator timeGenerator;

	private static final long RETROTHRESH = 60;
	private PowerManager.WakeLock mWakeLock;

	public static PingService getInstance() {
		return sInstance;
	}

	// ////////////////////////////////////////////////////////////////////
	@Override
	public void onCreate() {
		if (LOCAL_LOGV) Log.v(TAG, "onCreate()");
		sInstance = this;
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pingservice");
		mWakeLock.acquire();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("DEBUG", "Ping service started");
		if (intent.getAction() != null && intent.getAction().equals(Constants.ACTION_GAP_CHANGED)) {
			rescheduleAlarm();
		} else {
			handleAlarm();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private void rescheduleAlarm() {
		AlarmManager alarum = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent alit = new Intent(this, TPStartUp.class);
		alit.putExtra("ThisIntentIsTPStartUpClass", true);
		alarum.cancel(PendingIntent.getBroadcast(this, 0, alit, 0));

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putLong(KEY_NEXT, -1);
		editor.commit();

		handleAlarm();
	}

	private void handleAlarm() {
		Date launch = new Date();
		long launchTime = launch.getTime() / 1000;

		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mNotify = mPrefs.getBoolean(Constants.KEY_RUNNING, true);

		mNext = mPrefs.getLong(KEY_NEXT, -1);
		mSeed = mPrefs.getLong(KEY_SEED, -1);

		timeGenerator = new RandomTimeGenerator(mSeed);

		Log.d("DEBUG", "mNext at start: "+mNext);

		try {
			mGap = Integer.parseInt(mPrefs.getString("pingGap", "5"));
		} catch (NumberFormatException e) {
			Log.w(TAG, "onCreate: Invalid gap: " + mPrefs.getString("pingGap", "not set"));
			mGap = 5;
		}

		// First do a quick check to see if next ping is still in the future...
		if (mNext > launchTime) {
			// note: if we already set an alarm for this ping, it's
			// no big deal because this set will cancel the old one
			// ie the system enforces only one alarm at a time per setter
			setAlarm(mNext);
			this.stopSelf();
			return;
		}

		// If we make it here then it's time to do something
		// ---------------------
		if (mNext == -1 || mSeed == -1) { // then need to recalc from beg.
			mNext = timeGenerator.recalculateNextTime(launchTime, mGap);
		}

		pingsDB = PingsDbAdapter.getInstance();
		pingsDB.openDatabase();

		// First, if we missed any pings by more than $retrothresh seconds for
		// no
		// apparent reason, then assume the computer was off and auto-log them.
		while (mNext < launchTime - RETROTHRESH) {
			Log.d("TESTING", "Logging ping with time: " + mNext);
			logPing(mNext, "", Arrays.asList(new String[] { "OFF" }));
			mNext = timeGenerator.nextping(mNext, mGap);
		}
		// Next, ping for any pings in the last retrothresh seconds.
		do {
			while (mNext <= now()) {
				if (mNext < now() - RETROTHRESH) {
					logPing(mNext, "", Arrays.asList(new String[] { "OFF" }));
				} else {
					String tag = (mNotify) ? "" : "OFF";
					long rowID = logPing(mNext, "", Arrays.asList(new String[] { tag }));
					sendNote(mNext, rowID);
				}
				mNext = timeGenerator.nextping(mNext, mGap);
			}
		} while (mNext <= now());

		Log.d("DEBUG", ""+mNext);

		SharedPrefUtil.setNextPingTime(mNext);
		SharedPrefUtil.setSeed(timeGenerator.getSeed());

		setAlarm(mNext);
		pingsDB.closeDatabase();
		this.stopSelf();
	}

	@Override
	public void onDestroy() {
		Log.d("DEBUG", "Ping service done");
		mWakeLock.release();
		super.onDestroy();
	}

	private long logPing(long time, String notes, List<String> tags) {
		if (LOCAL_LOGV) Log.v(TAG, "logPing(" + tags + ")");
		return pingsDB.createPing(time, notes, tags, mGap);
	}

	// ////////////////////////////////////////////////////////////////////
	// just cuz original timepie uses unixtime, which is in seconds,
	// not universal time, which is in milliseconds
	public static long now() {
		long time = System.currentTimeMillis() / 1000;
		return time;
	}

	public void sendNote(long pingtime, long rowID) {

		if (!mNotify) return;

		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
		Date ping = new Date(pingtime * 1000);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		Intent editIntent = new Intent(this, EditPing.class);

		editIntent.putExtra(PingsDbAdapter.KEY_ROWID, rowID);
		editIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, editIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		mBuilder.setContentTitle("Ping!");
		mBuilder.setContentText(simpleDateFormat.format(ping));
		mBuilder.setSmallIcon(R.drawable.stat_ping);
		mBuilder.setContentIntent(contentIntent);

		boolean suppress_noises = false;
		if (mPrefs.getBoolean("pingQuietCharging", false)) {
			IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			Intent batteryStatus = registerReceiver(null, ifilter);
			suppress_noises = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
		}

		if (!suppress_noises) {
			if (mPrefs.getBoolean("pingVibrate", true)) {
				mBuilder.setVibrate(new long[] { 0, 200, 50, 100, 50, 200, 50, 200, 50, 100 });
			}
			String sound_uri = mPrefs
					.getString("pingRingtonePref", Settings.System.DEFAULT_NOTIFICATION_URI.toString());
			if (!sound_uri.equals("")) {
				//note.sound = Uri.parse(sound_uri);
				mBuilder.setSound(Uri.parse(sound_uri));
			} else {
				// "Silent" choice returns uri="", so no defaults
				mBuilder.setDefaults(0);
				// note.defaults |= Notification.DEFAULT_SOUND;
			}
		}

		if (mPrefs.getBoolean("pingLedFlash", false)) {
			mBuilder.setLights(0xff0033ff, 200, 1000);
		}

		mBuilder.setAutoCancel(true);

		// The layout ID is used as the notification ID
		notificationManager.notify(PING_NOTES, mBuilder.build());
	}

	// TODO: RTC_WAKEUP and appropriate perms into manifest
	private void setAlarm(long PING) {
		AlarmManager alarum = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent alit = new Intent(this, TPStartUp.class);
		alit.putExtra("ThisIntentIsTPStartUpClass", true);
		if (android.os.Build.VERSION.SDK_INT >= 23) {
			alarum.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, PING * 1000, PendingIntent.getBroadcast(this, 0, alit, 0));
		} else {
			alarum.set(AlarmManager.RTC_WAKEUP, PING * 1000, PendingIntent.getBroadcast(this, 0, alit, 0));
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new Binder() {
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
			return super.onTransact(code, data, reply, flags);
		}
	};
}
