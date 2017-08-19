package com.fwerpers.tagtime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.fwerpers.tagtime.PingService.KEY_NEXT;
import static org.junit.Assert.*;

/**
 * Created by FWerpers on 18/08/17.
 */

@RunWith(AndroidJUnit4.class)
public class PingServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule = ServiceTestRule.withTimeout(5L, TimeUnit.SECONDS);

    // Tests the case when the user hasn't started the app in a while
    @Test
    public void testPingService() throws TimeoutException {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getTargetContext());
        SharedPreferences.Editor editor = mPrefs.edit();
        long NEXT = 1503100800;
        editor.putLong(KEY_NEXT, NEXT);
        editor.commit();

        Intent serviceIntent = new Intent(getTargetContext(), PingService.class);

        mServiceRule.startService(serviceIntent);
    }


}