package com.fwerpers.tagtime;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by FWerpers on 25/08/17.
 */

final public class SharedPrefUtil {

    public static final String KEY_NEXT = "nextping";
    public static final String KEY_SEED = "RNG_seed";
    public static final String KEY_GAP = "pingGap";

    public static long getSeed() {
        Context context = TagTime.getAppContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long seed = preferences.getLong(KEY_SEED, -1);
        return(seed);
    }

    public static void setSeed(long seed) {
        Context context = TagTime.getAppContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_SEED, seed);
        editor.commit();
    }

    public static long getNextPingTime() {
        Context context = TagTime.getAppContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long nextPingTime = preferences.getLong(KEY_NEXT, -1);
        return(nextPingTime);
    }

    public static void setNextPingTime(long nextPingTime) {
        Context context = TagTime.getAppContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(KEY_NEXT, nextPingTime);
        editor.commit();
    }

    public static int getPingGap() {
        Context context = TagTime.getAppContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int gap;
        try {
            gap = Integer.parseInt(preferences.getString(KEY_GAP, "5"));
        } catch (NumberFormatException e) {
            // If the string entered wasn't parsable
            gap = 5;
        }
        return(gap);
    }

}
