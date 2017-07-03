package com.fwerpers.timeprof;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TagPercentageActivity extends AppCompatActivity {

    private PingsDbAdapter mDbHelper;
    private ArrayList<String> mTagList = new ArrayList<>();
    private HashMap<String, Float> proportions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        mDbHelper = PingsDbAdapter.getInstance();
        mDbHelper.openDatabase();

        int totalNumberOfPings = mDbHelper.getNumberOfPings();

        Log.d("TEST", Integer.toString(mDbHelper.getNumberOfPings()));

        List<String> tags = new ArrayList<>();
        tags.add("OFF");
        Log.d("TEST", Integer.toString(mDbHelper.getNumberOfPingsWithTags(tags)));

        int numberOfPingsWithTags = mDbHelper.getNumberOfPingsWithTags(tags);

        float percentage = (float) numberOfPingsWithTags / totalNumberOfPings;
        String percentageText = String.format("%.3f %%", percentage);
        TextView mPercentageText = (TextView) findViewById(R.id.percentage_text);
        mPercentageText.setText(percentageText);
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
