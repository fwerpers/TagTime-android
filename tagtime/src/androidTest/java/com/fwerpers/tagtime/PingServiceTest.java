package com.fwerpers.tagtime;

import android.content.Intent;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;

/**
 * Created by FWerpers on 18/08/17.
 */

@RunWith(AndroidJUnit4.class)
public class PingServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testPingService() throws TimeoutException {
        Intent serviceIntent = new Intent(getTargetContext(), PingService.class);

        // Data can be passed to the service via the Intent.
        mServiceRule.startService(serviceIntent);
    }


}