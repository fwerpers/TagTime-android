package com.fwerpers.tagtime;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class OverviewFragment extends Fragment {

    private SharedPreferences mSettings;
    private TextView mNextPingText;

    public OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        mNextPingText = (TextView) view.findViewById(R.id.NextPing);
        // Inflate the layout for this fragment
        return(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault());
        Long nextPingTime = mSettings.getLong(PingService.KEY_NEXT, -1);
        Date pingDate = new Date(nextPingTime * 1000);
        mNextPingText.setText(sdf.format(pingDate));
    }
}
