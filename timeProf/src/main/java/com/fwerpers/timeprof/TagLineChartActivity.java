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
        List<Entry> entries = new ArrayList<Entry>();

        long period = 60*60;
        Cursor pingCursor = mDbHelper.fetchAllPings(false);
        int pingTimeColumnIndex = pingCursor.getColumnIndex(PingsDbAdapter.KEY_PING);
        Log.d("TEST", "Column index: " + pingTimeColumnIndex);
        Log.d("TEST", "Column count: " + pingCursor.getColumnCount());
        Log.d("TEST", "Column name: " + pingCursor.getColumnName(pingTimeColumnIndex));
        long pingTime;
        int pingCounter = 0;
        int dataPointCounter = 0;

        try {
            pingCursor.moveToFirst();
            pingTime = pingCursor.getLong(pingTimeColumnIndex);
            long timeBoundary = pingTime + period;
            Log.d("TEST", "Time boundary: " + timeBoundary);
            Log.d("TEST", "Ping time: " + pingTime);
            while (!pingCursor.isAfterLast()) {
                while (pingTime <= timeBoundary && !pingCursor.isAfterLast()) {
                    pingCounter++;
                    pingTime = pingCursor.getLong(pingTimeColumnIndex);
                    pingCursor.moveToNext();
                }
                entries.add(new Entry(dataPointCounter, pingCounter));
                dataPointCounter++;
                pingCounter = 0;
                timeBoundary += period;
            }
        } finally {
            pingCursor.close();
        }

        LineDataSet dataSet = new LineDataSet(entries, "Ping times");
        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);
        mChart.invalidate();
    }
}
