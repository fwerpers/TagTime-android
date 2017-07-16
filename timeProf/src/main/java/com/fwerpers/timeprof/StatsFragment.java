package com.fwerpers.timeprof;


import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment {

    private PingsDbAdapter mDbHelper;

    public StatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        LineChart mChart = (LineChart) view.findViewById(R.id.linechart);

        long period = 60*60;

        List<String> tags = new ArrayList<>();
        tags.add("OFF");

        LineData lineData = getPercentageLineData(tags, period);
        mChart.setData(lineData);
        mChart.getData().setHighlightEnabled(false);
        mChart.getData().setDrawValues(false);
        mChart.setVisibleYRangeMaximum(1.2f, YAxis.AxisDependency.LEFT);
        mChart.setVisibleYRangeMinimum(1.2f, YAxis.AxisDependency.LEFT);
        mChart.getAxisLeft().setSpaceBottom(0f);
        mChart.getAxisRight().setEnabled(false);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(true);
        mChart.setDescription(null);
        mChart.getLegend().setEnabled(false);
        mChart.invalidate();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = PingsDbAdapter.getInstance();
        mDbHelper.openDatabase();
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
        dataSet.setLineWidth(3f);
        dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.tag_selected));
        dataSet.setCircleColor(ContextCompat.getColor(getActivity(), R.color.tag_selected));
        dataSet.setCircleRadius(3f);
        LineData lineData = new LineData(dataSet);
        return(lineData);
    }

}
