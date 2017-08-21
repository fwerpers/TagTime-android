package com.fwerpers.tagtime;

import android.database.Cursor;

import static com.fwerpers.tagtime.PingsDbAdapter.KEY_TAG;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Created by FWerpers on 21/08/17.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DatabaseTest {
    private PingsDbAdapter mPingsAdapter;

    @Before
    public void setUp() throws Exception {
        RuntimeEnvironment.application.deleteDatabase(PingsDbAdapter.DATABASE_NAME);
        PingsDbAdapter.initializeInstance(RuntimeEnvironment.application);
        mPingsAdapter = PingsDbAdapter.getInstance();
        mPingsAdapter.openDatabase();
    }

    @After
    public void tearDown() throws Exception {
        mPingsAdapter.closeDatabase();
    }

    @Test
    public void testTagInsert2() throws Exception {
        String newTag = "testTag";
        mPingsAdapter.newTag(newTag);
        Cursor tagCursor = mPingsAdapter.fetchAllTags();
        tagCursor.moveToFirst();
        int idx = tagCursor.getColumnIndex(KEY_TAG);
        String tag = tagCursor.getString(idx);
        assertEquals(tag, newTag);
    }
}