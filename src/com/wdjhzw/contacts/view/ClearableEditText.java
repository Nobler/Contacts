package com.wdjhzw.contacts.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.wdjhzw.contacts.R;

public class ClearableEditText extends EditText implements
        OnFocusChangeListener, TextWatcher {
    private Drawable mCLearIcon;

    private boolean mHasFocused;

    public ClearableEditText(Context context) {
        this(context, null);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCLearIcon();
    }

    private void initCLearIcon() {
        mCLearIcon = getCompoundDrawables()[2];
        if (mCLearIcon == null) {
            mCLearIcon = getResources().getDrawable(R.drawable.ic_search_clear);
        }

        mCLearIcon.setBounds(0, 0, mCLearIcon.getIntrinsicWidth(),
                mCLearIcon.getIntrinsicHeight());

        setClearIconVisible(false);
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }
    
    protected void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mCLearIcon : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], right, getCompoundDrawables()[3]);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                boolean touchable = event.getX() > (getWidth() - getTotalPaddingRight())
                        && (event.getX() < ((getWidth() - getPaddingRight())));

                if (touchable) {
                    setText("");
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        mHasFocused = hasFocus;
        if (mHasFocused) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int count, int after) {
        if (mHasFocused) {
            setClearIconVisible(s.length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
