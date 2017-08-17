package com.fwerpers.tagtime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private static final int ACTIVITY_EDIT = 0;
    private static final String TAG = "ViewLog";
    private static final boolean LOCAL_LOGV = true && !TagTime.DISABLE_LOGV;

    private PingsDbAdapter mDbHelper;
    private LogFragment.PingCursorAdapter mPingAdapter;

    private SimpleDateFormat mSDF;
    private Map<Long, String> mTagList = new HashMap<Long, String>();
    private ListView mListView;
    private ProgressBar mProgress;
    private TextView mNoData;

    private ActionBar mAction;

    public LogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogFragment newInstance(String param1, String param2) {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void refreshTagList() {
        Cursor c = mDbHelper.fetchAllTags("ROWID");
        mTagList.clear();

        int idxrow = c.getColumnIndex(PingsDbAdapter.KEY_ROWID);
        int idxtag = c.getColumnIndex(PingsDbAdapter.KEY_TAG);

        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                mTagList.put(c.getLong(idxrow), c.getString(idxtag));
                c.moveToNext();
            }
        } finally {
            c.close();
        }
    }

    public static final class PingsCursorLoader extends SimpleCursorLoader {

        private PingsDbAdapter mHelper;

        public PingsCursorLoader(Context context, PingsDbAdapter helper) {
            super(context);
            mHelper = helper;
        }

        @Override
        public Cursor loadInBackground() {
            return mHelper.fetchAllPings(true);
        }

    }

    public final class PingCursorAdapter extends CursorAdapter {

        private Context mContext;

        public class ViewHolder {
            TextView pingText;
            TextView tagText;
            TextView yellowBeeText;
            TextView redBeeText;
        }

        public PingCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
            mContext = context;
        }

        public PingCursorAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            mContext = context;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.tagtime_viewlog_ping_row, parent, false);
            LogFragment.PingCursorAdapter.ViewHolder viewHolder = new LogFragment.PingCursorAdapter.ViewHolder();
            viewHolder.pingText = (TextView) view.findViewById(R.id.viewlog_row_time);
            viewHolder.tagText = (TextView) view.findViewById(R.id.viewlog_row_tags);
            viewHolder.yellowBeeText = (TextView) view.findViewById(R.id.viewlog_row_beeminder);
            viewHolder.redBeeText = (TextView) view.findViewById(R.id.viewlog_row_beeminder_red);
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            LogFragment.PingCursorAdapter.ViewHolder vh = (LogFragment.PingCursorAdapter.ViewHolder) view.getTag();

            // Convert ping time to readable text
            int pingidx = cursor.getColumnIndex(PingsDbAdapter.KEY_PING);
            long pingtime = cursor.getLong(pingidx);
            vh.pingText.setText(mSDF.format(new Date(pingtime * 1000)));

            // Figure out Beeminder submission status and update icons, also
            // setting the tag string
            try {
                List<Long> tags = mDbHelper.fetchTagsForPing(cursor.getLong(0));

                String tagstr = "";
                for (long tag : tags)
                    tagstr = tagstr + " " + mTagList.get(tag);
                vh.tagText.setText(tagstr);
            } catch (Exception e) {}
        }

    }

    private BroadcastReceiver pingUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LOCAL_LOGV) Log.v(TAG, "ping update");
            Bundle extras = intent.getExtras();
            boolean newping = false;
            if (extras != null) newping = intent.getBooleanExtra(TagTime.KEY_PING_ISNEW, true);
            if (newping) getActivity().getSupportLoaderManager().restartLoader(0, null, LogFragment.this);
            else mPingAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.tagtime_viewlog);

//        mAction = getSupportActionBar();
//        mAction.setHomeButtonEnabled(true);
//        mAction.setIcon(R.drawable.tagtime_03);

        mDbHelper = PingsDbAdapter.getInstance();
        mDbHelper.openDatabase();

        mSDF = new SimpleDateFormat("yyyy.MM.dd'\n'HH:mm:ss", Locale.getDefault());
        mPingAdapter = new LogFragment.PingCursorAdapter(getActivity(), null, true);

        getActivity().getSupportLoaderManager().initLoader(0, null, this);
        refreshTagList();

        getActivity().registerReceiver(pingUpdateReceiver, new IntentFilter(TagTime.PING_UPDATE_EVENT));
    }

    @Override
    public void onResume() {
        super.onResume();
        //getActivity().getSupportLoaderManager().initLoader(0, null, this);
        getActivity().getSupportLoaderManager().restartLoader(0, null, LogFragment.this);
        refreshTagList();
    }

    @Override
    public void onStart() {
        super.onStart();
        mListView = (ListView) getView().findViewById(R.id.listview);
        mListView.setFastScrollEnabled(true);
        mListView.setAdapter(mPingAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
                Intent i = new Intent(getContext(), EditPing.class);
                i.putExtra(PingsDbAdapter.KEY_ROWID, id);
                startActivityForResult(i, ACTIVITY_EDIT);
            }
        });
        mNoData = (TextView) getView().findViewById(R.id.nodata);
        mProgress = (ProgressBar) getView().findViewById(R.id.progressbar);
        mListView.setEmptyView(mProgress);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        refreshTagList();
        // mPingAdapter.notifyDataSetChanged(); // Uluc: Subsumed by
        // pingUpdateReceiver
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(pingUpdateReceiver);
        mDbHelper.closeDatabase();
        super.onDestroy();
    }

//    /** Handles menu item selections */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                // app icon in action bar clicked; go home
//                Intent intent = new Intent(this, TPController.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        if (mProgress != null) {
            mProgress.setVisibility(View.GONE);
        }

        if (mListView != null) {
            mListView.setEmptyView(mNoData);
        }
        mPingAdapter.swapCursor(data);
    }

    // Called when a previously created loader is reset, making the data
    // unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mPingAdapter.swapCursor(null);
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new PingsCursorLoader(getActivity(), mDbHelper);
    }
}
