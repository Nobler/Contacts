package com.wdjhzw.contacts.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {

    private boolean mPagingEnabled;

    public CustomViewPager(Context context) {
        super(context);
        mPagingEnabled = true;
    }
    
    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPagingEnabled = true;
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (mPagingEnabled) {
            return super.onInterceptTouchEvent(arg0);
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (mPagingEnabled) {
            return super.onTouchEvent(arg0);
        }
        return false;
    }

    public void setPagingEnabled(boolean enable) {
        mPagingEnabled = enable;
    }
    
}
