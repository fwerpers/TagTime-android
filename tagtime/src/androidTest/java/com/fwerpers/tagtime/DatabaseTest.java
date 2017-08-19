package com.fwerpers.tagtime;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;

/**
 * Created by FWerpers on 18/08/17.
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {
    private PingsDbAdapter mPingsAdapter;

    @Before
    public void setUp() throws Exception {
        getTargetContext().deleteDatabase(PingsDbAdapter.DATABASE_NAME);
        PingsDbAdapter.initializeInstance(getTargetContext());
        mPingsAdapter = PingsDbAdapter.getInstance();
        mPingsAdapter.openDatabase();
    }

    @After
    public void tearDown() throws Exception {
        mPingsAdapter.closeDatabase();
    }

    @Test
    public void testDb() throws Exception {
        String newTag = "test";
        mPingsAdapter.newTag(newTag);
        long tid = mPingsAdapter.getTID(newTag);
        Log.d("TEST", "tet");
        assertFalse(tid == -1);
    }
}