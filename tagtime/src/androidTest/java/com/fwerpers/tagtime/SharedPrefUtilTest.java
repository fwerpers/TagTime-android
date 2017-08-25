package com.fwerpers.tagtime;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.preference.PreferenceManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;

/**
 * Created by FWerpers on 25/08/17.
 */

@RunWith(AndroidJUnit4.class)
public class SharedPrefUtilTest {

    @Before
    public void setUp() throws Exception {
        Context context = getTargetContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    @Test
    public void testSetSeed() {
        long newSeed = 123;
        SharedPrefUtil.setSeed(newSeed);
        long seed = SharedPrefUtil.getSeed();
        assertEquals(newSeed, seed);
    }

    @Test
    public void testSetNextPingTime() {
        long newPingTime = 123;
        SharedPrefUtil.setNextPingTime(newPingTime);
        long pingTime = SharedPrefUtil.getNextPingTime();
        assertEquals(newPingTime, pingTime);
    }

    @Test
    public void testGetPingGap() {
        int gap = SharedPrefUtil.getPingGap();
        assertEquals(5, gap);
    }

}