package com.wdjhzw.contacts.view;

import java.util.Arrays;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wdjhzw.contacts.R;

public class QuickIndexer extends View {
    private final String[] sections = { "#", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z" };

    private OnTouchEventListener mListener;

    private int mCurrentPosition = -1;
    private boolean mShowBkg = false;

    private Resources mResources;
    private Paint mPaint = new Paint();

    public QuickIndexer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mResources = getResources();
        initPaint();
    }

    public QuickIndexer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mResources = getResources();
        initPaint();
    }

    public QuickIndexer(Context context) {
        super(context);
        mResources = getResources();
        initPaint();
    }

    private void initPaint() {
        mPaint.setTypeface(Typeface.MONOSPACE);
        mPaint.setFakeBoldText(true);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mShowBkg) {
            canvas.drawColor(mResources.getColor(R.color.gray_deep));
        }

        for (int i = 0; i < sections.length; i++) {
            if (i == mCurrentPosition) {
                mPaint.setColor(mResources
                        .getColor(android.R.color.holo_blue_dark));
                mPaint.setTextSize(mResources
                        .getDimensionPixelSize(R.dimen.quickindexer_font_size_bigger));
            } else {
                mPaint.setColor(Color.GRAY);
                mPaint.setTextSize(mResources
                        .getDimensionPixelSize(R.dimen.quickindexer_font_size_normal));
            }

            float xPos = (getWidth() - mPaint.measureText(sections[i])) / 2;
            float yPos = getHeight() / sections.length * (i + 1);

            // x The x-coordinate and y-coordinate of the origin of the text
            // being drawn. The origin is interpreted based on the Align setting
            // in the paint.
            canvas.drawText(sections[i], xPos, yPos, mPaint);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final int position = (int) (event.getY() / getHeight() * sections.length);

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mShowBkg = true;
        case MotionEvent.ACTION_MOVE:
            if (position >= 0 && position < sections.length) {
                if (mListener != null) {
                    mListener.onActionDown(sections[position]);
                }
                mCurrentPosition = position;
                invalidate();
            }
            break;
        case MotionEvent.ACTION_UP:
            mShowBkg = false;
            if (mListener != null) {
                mListener.onActionUp();
            }
            invalidate();
            break;
        }

        return true;
    }

    public void setCurrentChar(String s) {
        mCurrentPosition = Arrays.binarySearch(sections, s);
        invalidate();
    }

    /**
     * Register a callback to be invoked when an character in this QuickIndexer
     * has been clicked
     * 
     * @param listener
     *            The callback that will run
     */
    public void setOnTouchEventListener(OnTouchEventListener listener) {
        mListener = listener;
    }

    public interface OnTouchEventListener {
        /**
         * 
         * @param str
         *            current focused character
         */
        void onActionDown(String str);

        void onActionUp();
    }
}
