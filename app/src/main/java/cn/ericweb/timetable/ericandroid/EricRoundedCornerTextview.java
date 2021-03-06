package cn.ericweb.timetable.ericandroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import cn.ericweb.timetable.R;

/**
 * 带圆角的TextView
 * Created by Eric on 2016/10/11.
 */

@RemoteViews.RemoteView
public class EricRoundedCornerTextview extends TextView {
    private Context mContext;
    private int mBgColor = 0;
    private int mCornerSize = 0;
    private int borderWidth = 0;
    private int weekday;
    private int startIndex;

    public EricRoundedCornerTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        getAttrs(context, attrs);
    }

    public EricRoundedCornerTextview(Context context) {
        super(context);
        this.mContext = context;
        this.mCornerSize = 8;
        mBgColor = getResources().getColor(R.color.colorApparent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        setBackgroundRounded(canvas, this.getMeasuredWidth(), this.getMeasuredHeight(), this);
        super.onDraw(canvas);
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EricRoundedCornerTextview);
        mBgColor = ta.getColor(R.styleable.EricRoundedCornerTextview_bgColor, getResources().getColor(R.color.colorApparent));

        mCornerSize = (int) ta.getDimension(R.styleable.EricRoundedCornerTextview_cornerSize, 8);

        ta.recycle();
    }

    public void setBackgroundRounded(Canvas c, int w, int h, View v) {
        if (w <= 0 || h <= 0) {
            return;
        }

        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        paint.setAntiAlias(true);
        paint.setColor(mBgColor);

        RectF rec = new RectF(borderWidth, borderWidth, w - 2 * borderWidth, h - 2 * borderWidth);

        c.drawRoundRect(rec, mCornerSize, mCornerSize, paint);
    }

    public void setmBgColor(int bgColor) {
        this.mBgColor = bgColor;
    }

    public void setmCornerSize(int cornerSize) {
        this.mCornerSize = cornerSize;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
