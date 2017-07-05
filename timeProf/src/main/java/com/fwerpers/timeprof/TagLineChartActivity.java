package com.fwerpers.timeprof;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class TagLineChartActivity extends AppCompatActivity {

    private PingsDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_line_chart);

        mDbHelper = PingsDbAdapter.getInstance();
        mDbHelper.openDatabase();

        LineChart mChart = (LineChart) findViewById(R.id.linechart);

        long period = 60*60;

        List<String> tags = new ArrayList<>();
        tags.add("OFF");

        LineData lineData = getPercentageLineData(tags, period);
        mChart.setData(lineData);
        mChart.invalidate();
    }

    private LineData getPercentageLineData(List<String> tags, long periodSeconds) {
        List<Entry> entries = new ArrayList<Entry>();
        Cursor pingCursor = mDbHelper.fetchAllPings(false);
        int pingTimeColumnIndex = pingCursor.getColumnIndex(PingsDbAdapter.KEY_PING);
        int pingIdColumnIndex = pingCursor.getColumnIndex(PingsDbAdapter.KEY_ROWID);

        long pingTime;
        int pingCounter = 0;
        int pingWithTagsCounter = 0;
        int dataPointCounter = 0;
        List<String> pingTags;

        try {
            pingCursor.moveToFirst();
            pingTime = pingCursor.getLong(pingTimeColumnIndex);
            long timeBoundary = pingTime + periodSeconds;
            while (!pingCursor.isAfterLast()) {
                while (pingTime <= timeBoundary && !pingCursor.isAfterLast()) {
                    pingCounter++;
                    int pingId = pingCursor.getInt(pingIdColumnIndex);
                    try {
                        pingTags = mDbHelper.fetchTagNamesForPing(pingId);
                    } catch (Exception e) {
                        return(null);
                    }
                    if (pingTags.containsAll(tags)) {
                        pingWithTagsCounter++;
                    }
                    pingTime = pingCursor.getLong(pingTimeColumnIndex);
                    pingCursor.moveToNext();
                }

                entries.add(new Entry(dataPointCounter, (float)pingWithTagsCounter/pingCounter));
                dataPointCounter++;
                pingCounter = 0;
                pingWithTagsCounter = 0;
                timeBoundary += periodSeconds;
            }
        } finally {
            pingCursor.close();
        }

        LineDataSet dataSet = new LineDataSet(entries, "Ping times");
        LineData lineData = new LineData(dataSet);
        return(lineData);
    }
}
