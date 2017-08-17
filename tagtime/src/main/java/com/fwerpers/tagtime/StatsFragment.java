package com.fwerpers.tagtime;


import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatsFragment extends Fragment {

    private PingsDbAdapter mDbHelper;
    private long bucketSize;
    private int bucketSizeSelection;

    private static int ONE_HOUR_POSITION = 0;
    private static int ONE_DAY_POSITION = 1;
    private static int ONE_WEEK_POSITION = 2;
    private static String[] bucketSizeStrings = {"1 hour", "1 day", "1 week"};
    private static long[] bucketSizes = {60*60, 60*60*24, 60*60*24*7};

    private LineChart mChart;
    private List<String> mTags;

    public StatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        mChart = (LineChart) view.findViewById(R.id.linechart);

        bucketSize = bucketSizes[bucketSizeSelection];

        mTags = new ArrayList<>();
        mTags.add("OFF");

        updateChartData(mTags, bucketSize);
        if (mChart.getData() != null) {
            mChart.getData().setHighlightEnabled(false);
            mChart.getData().setDrawValues(false);
            mChart.getAxisLeft().setSpaceBottom(0f);
            mChart.getAxisRight().setEnabled(false);
            mChart.setDoubleTapToZoomEnabled(false);
            mChart.setBackgroundColor(Color.WHITE);
            mChart.getAxisLeft().setDrawGridLines(false);
            mChart.getXAxis().setDrawGridLines(true);
            mChart.setDescription(null);
            mChart.getLegend().setEnabled(false);
            mChart.invalidate();
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = PingsDbAdapter.getInstance();
        mDbHelper.openDatabase();

        // TODO: Keep in shared preferences?
        bucketSizeSelection = ONE_WEEK_POSITION;

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_bucket_size:
                showBucketSizeDialog();
                break;
            case R.id.action_tag_select:
                showTagSelectionDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showBucketSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose bucket size");

        builder.setSingleChoiceItems(bucketSizeStrings, bucketSizeSelection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int selection) {
                bucketSizeSelection = selection;
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (bucketSize != bucketSizes[bucketSizeSelection]) {
                    bucketSize = bucketSizes[bucketSizeSelection];
                    updateChartData(mTags, bucketSize);
                    mChart.invalidate();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showTagSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose tags");

        final TagSelectView tagSelectView = new TagSelectView(getActivity(), mTags);
        builder.setView(tagSelectView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> selectedTags = tagSelectView.getTags();
                Collections.sort(selectedTags);
                Collections.sort(mTags);
                if (!selectedTags.equals(mTags)) {
                    mTags = selectedTags;
                    updateChartData(mTags, bucketSize);
                    mChart.invalidate();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
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
                        return (null);
                    }
                    if (pingTags.containsAll(tags)) {
                        pingWithTagsCounter++;
                    }
                    pingTime = pingCursor.getLong(pingTimeColumnIndex);
                    pingCursor.moveToNext();
                }

                entries.add(new Entry(dataPointCounter, (float) pingWithTagsCounter / pingCounter));
                dataPointCounter++;
                pingCounter = 0;
                pingWithTagsCounter = 0;
                timeBoundary += periodSeconds;
            }
        } catch (android.database.CursorIndexOutOfBoundsException e) {
            return (null);
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

    private void updateChartData(List<String> tags, long bucketSize) {
        LineData lineData = getPercentageLineData(tags, bucketSize);
        mChart.setData(lineData);
    }

}
