package com.fwerpers.timeprof;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class ChartsActivity extends AppCompatActivity {

    private PingsDbAdapter mDbHelper;
    private ArrayList<String> mTagList = new ArrayList<>();
    private HashMap<String, Float> proportions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        mDbHelper = PingsDbAdapter.getInstance();
        mDbHelper.openDatabase();

        Log.d("TEST", Integer.toString(mDbHelper.getNumberOfPings()));
    }

    private void refreshTagList() {
        Cursor cursor = mDbHelper.fetchAllPings(true);
        proportions.clear();
        int idxping = cursor.getColumnIndex(PingsDbAdapter.KEY_ROWID);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                mTagList.add(cursor.getString(idxping));
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
    }
}
