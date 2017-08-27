package com.fwerpers.tagtime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.*;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.fwerpers.tagtime.PingService.KEY_NEXT;
import static com.fwerpers.tagtime.R.id.next;
import static org.junit.Assert.*;

/**
 * Created by FWerpers on 18/08/17.
 */

@RunWith(AndroidJUnit4.class)
public class PingServiceTest {

    private PingsDbAdapter mPingsAdapter;

    @Rule
    public final ServiceTestRule mServiceRule = ServiceTestRule.withTimeout(5L, TimeUnit.SECONDS);

    @Before
    public void clearDatabase() throws Exception {
        getTargetContext().deleteDatabase(PingsDbAdapter.DATABASE_NAME);
        PingsDbAdapter.initializeInstance(getTargetContext());
        mPingsAdapter = PingsDbAdapter.getInstance();
        mPingsAdapter.openDatabase();
    }

    @Before
    public void clearPreferences() {
        Context context = TagTime.getAppContext();
        SharedPreferences preferences = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    @After
    public void tearDown() throws Exception {
        mPingsAdapter.closeDatabase();
    }

    // Tests the case when the user hasn't started the app in a while
    // which means there are a lot of pings to store in the database
    //@Test
    public void testNextIsInDistantPast() throws TimeoutException {
        long nextPingTime = PingService.now() - 60*60*24;
        long seed = 666;
        SharedPrefUtil.setNextPingTime(nextPingTime);
        SharedPrefUtil.setSeed(seed);
        Intent serviceIntent = new Intent(getTargetContext(), PingService.class);

        mServiceRule.startService(serviceIntent);
    }

    @Test
    public void testNextIsInFuture() throws TimeoutException {
        long nextPingTime = PingService.now() + 60*60*24;
        SharedPrefUtil.setNextPingTime(nextPingTime);
        Intent serviceIntent = new Intent(getTargetContext(), PingService.class);
        mServiceRule.startService(serviceIntent);
        long next = SharedPrefUtil.getNextPingTime();
        assertEquals(nextPingTime, next);
    }

    @Test
    public void testNextAndSeedIsNotSet() throws TimeoutException {
        Intent serviceIntent = new Intent(getTargetContext(), PingService.class);
        long nextBefore = SharedPrefUtil.getNextPingTime();
        long seedBefore = SharedPrefUtil.getSeed();
        assertEquals(-1, nextBefore);
        assertEquals(-1, seedBefore);

        mServiceRule.startService(serviceIntent);
        long next = SharedPrefUtil.getNextPingTime();
        long seed = SharedPrefUtil.getSeed();
        assertNotEquals(next, -1);
    }


}