package com.wdjhzw.contacts.listener;

/**
 * Interface definition for a callback to be invoked when a view state is changed.

 * @author houzhiwei
 *
 */
public interface OnViewStateChangedListener {

    public final int NORMAL_VIEW_STATE = 0;
    
    public final int EDIT_VIEW_STATE = 1;

    public void onTitleBarTextChanged(String title);

    public void onViewStateChanged(int state, String title);

    public void onDeleteButtonEnabledStateChanged(boolean enabled);

    public void onCheckAllButtonCheckedStateChanged(boolean checked);

}