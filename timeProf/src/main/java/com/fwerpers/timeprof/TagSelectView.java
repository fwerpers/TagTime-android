package com.fwerpers.timeprof;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: document your custom view class.
 */
public class TagSelectView extends ScrollView {
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private List<String> mCurrentTags;
    private LinearLayout llTags;
    private ViewTreeObserver mVto;
    private Cursor mTagsCursor;
    private PingsDbAdapter mPingsDB;
    private String mOrdering;
    private Context mContext;

    private final static String TAG = "TagSelectView";
    private static final boolean LOCAL_LOGV = true && !TimeProf.DISABLE_LOGV;
    private int FIXTAGS = R.layout.tagtime_editping;

    public TagSelectView(Context context) {
        super(context);
        init(null, 0);
    }

    public TagSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs, 0);
    }

    public TagSelectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        llTags = new LinearLayout(mContext);

        llTags.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        llTags.setBackgroundColor(Color.RED);
        llTags.setOrientation(LinearLayout.VERTICAL);
        addView(llTags);
        mVto = llTags.getViewTreeObserver();

        mPingsDB = PingsDbAdapter.getInstance();
        mPingsDB.openDatabase();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mOrdering = prefs.getString("sortOrderPref", "FREQ");

        mTagsCursor = mPingsDB.fetchAllTags(mOrdering);

        mCurrentTags = new ArrayList<>();
        refreshTags();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
//        int paddingLeft = getPaddingLeft();
//        int paddingTop = getPaddingTop();
//        int paddingRight = getPaddingRight();
//        int paddingBottom = getPaddingBottom();
//
//        int contentWidth = getWidth() - paddingLeft - paddingRight;
//        int contentHeight = getHeight() - paddingTop - paddingBottom;
//
//        // Draw the text.
//        canvas.drawText(mExampleString,
//                paddingLeft + (contentWidth - mTextWidth) / 2,
//                paddingTop + (contentHeight + mTextHeight) / 2,
//                mTextPaint);
//
//        // Draw the example drawable on top of the text.
//        if (mExampleDrawable != null) {
//            mExampleDrawable.setBounds(paddingLeft, paddingTop,
//                    paddingLeft + contentWidth, paddingTop + contentHeight);
//            mExampleDrawable.draw(canvas);
//        }
        //llTags.draw(canvas);
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }

    /**
     * This method refreshes the list of tag buttons based on the contents of
     * the tag database.
     */
    private void refreshTags() {
        llTags.removeAllViews();
        mTagsCursor.moveToFirst();

        LinearLayout ll = new LinearLayout(mContext);
        ll.setId(FIXTAGS);
        ll.setOrientation(1);
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        while (!mTagsCursor.isAfterLast()) {
            String tag = mTagsCursor.getString(mTagsCursor.getColumnIndex(PingsDbAdapter.KEY_TAG));
            long id = mTagsCursor.getLong(mTagsCursor.getColumnIndex(PingsDbAdapter.KEY_ROWID));
            boolean on = false;
            if (mCurrentTags != null) on = mCurrentTags.contains(tag);
            TagToggle tog = new TagToggle(mContext, tag, id, on);
            tog.setOnClickListener(mTogListener);
            ll.addView(tog);

            mTagsCursor.moveToNext();
        }
        llTags.addView(ll);
        mVto = llTags.getViewTreeObserver();
        mVto.addOnPreDrawListener(mDrawListener);
        invalidate();
        requestLayout();
    }

    private OnClickListener mTogListener = new OnClickListener() {
        public void onClick(View v) {
            TagToggle tog = (TagToggle) v;
            String tag = tog.getText().toString();
            if (tog.isSelected()) {
                if (LOCAL_LOGV) Log.v(TAG, "OnClickListener: Toggling " + tag);
                mCurrentTags.add(tag);
            } else {
                mCurrentTags.remove(tag);
            }
            //mCurrentTagString = TextUtils.join(" ", mCurrentTags);
        }
    };

    private ViewTreeObserver.OnPreDrawListener mDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        public boolean onPreDraw() {
            fixTags();
            ViewTreeObserver vto = llTags.getViewTreeObserver();
            vto.removeOnPreDrawListener(mDrawListener);
            return true;
        }
    };

    /**
     * This method fixes the layout of the tag buttons to make them fit into the
     * screen width
     */
    private void fixTags() {
        LinearLayout ll = (LinearLayout) llTags.getChildAt(0);
        llTags.removeAllViews();
        int pwidth = llTags.getWidth() - 15;
        int twidth = 0;
        LinearLayout tagrow = new LinearLayout(mContext);
        tagrow.setOrientation(0);
        tagrow.setGravity(Gravity.CENTER_HORIZONTAL);
        tagrow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        while (ll.getChildCount() > 0) {
            View tog = ll.getChildAt(0);
            ll.removeViewAt(0);
            int leftover = pwidth - twidth;
            if (tog.getWidth() > leftover) {
                llTags.addView(tagrow);
                tagrow = new LinearLayout(mContext);// .removeAllViews();
                tagrow.setOrientation(0);
                tagrow.setGravity(Gravity.CENTER_HORIZONTAL);
                tagrow.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                twidth = 0;
            }
            tagrow.addView(tog);
            twidth += tog.getWidth();
        }
        llTags.addView(tagrow);
    }
}
