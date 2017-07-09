package com.fwerpers.timeprof;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ChartSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_select);

        Button mPercentageButton = (Button) findViewById(R.id.percentage_button);
        mPercentageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPercentageActivity();
            }
        });

        Button mLineChartButton = (Button) findViewById(R.id.linechart_button);
        mLineChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLineChartActivity();
            }
        });

        Button mTestChartButton = (Button) findViewById(R.id.linechart_test_button);
        mTestChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTestLineChartActivity();
            }
        });

    }

    private void startPercentageActivity() {
        Intent intent = new Intent();
        intent.setClass(this, TagPercentageActivity.class);
        startActivity(intent);
    }

    private void startLineChartActivity() {
        Intent intent = new Intent();
        intent.setClass(this, TagLineChartActivity.class);
        startActivity(intent);
    }

    private void startTestLineChartActivity() {
        Intent intent = new Intent();
        intent.setClass(this, TestLineChartActivity.class);
        startActivity(intent);
    }
}
