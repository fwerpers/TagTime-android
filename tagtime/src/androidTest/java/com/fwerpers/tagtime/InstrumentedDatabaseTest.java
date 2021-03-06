package com.fwerpers.tagtime;

import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.fwerpers.tagtime.PingsDbAdapter.KEY_TAG;
import static org.junit.Assert.*;

/**
 * Created by FWerpers on 18/08/17.
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedDatabaseTest {
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
    public void testTagInsert() throws Exception {
        String newTag = "testTag";
        mPingsAdapter.insertTag(newTag);
        Cursor tagCursor = mPingsAdapter.fetchAllTags();
        assertTrue(tagCursor.getCount() == 1);
        tagCursor.moveToFirst();
        int idx = tagCursor.getColumnIndex(KEY_TAG);
        String tag = tagCursor.getString(idx);
        assertEquals(tag, newTag);
    }

    @Test
    public void testPingInsert() throws Exception {
        long newTime = 123;
        String newNotes = "hejhopp";
        List<String> newTags = Arrays.asList(new String[] { "OFF" });
        int newPeriod = 45;
        long pid = mPingsAdapter.insertPingWithTags(newTime, newNotes, newTags, newPeriod);
        List<String> tags = mPingsAdapter.fetchTagNamesForPing(pid);
        assertEquals(tags, newTags);
    }
}