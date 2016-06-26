package com.wdjhzw.contacts.fragment;

import java.util.List;

import android.support.v4.app.Fragment;
import android.os.Message;
import android.view.Menu;

public abstract class CustomFragment extends Fragment {

    /**
     * Get the current state of the fragment's view.
     * 
     * @return true if the view is in edit state, false otherwise
     */
    public abstract boolean isEditState();

    /**
     * Called to get items selected in list view.
     */
    public abstract <E> List<E> onGetItemSelected();

    /**
     * Called when the back key down.
     */
    public abstract void onBackKeyDown();

    public abstract boolean onPrepareOptionsMenuSyn(Menu menu);

    public void handleMessage(Message msg) {
    }

    /**
     * Called when the call button in phone dock clicked.
     */
    public void onCallClicked() {
    }

    /**
     * Called when the clear input button in phone dock clicked.
     */
    public void onClearInputClicked() {
    }

    /**
     * Called when the clear input button in phone dock long clicked.
     */
    public void onClearInputLongClicked() {
    }

    /**
     * Called when the dial pad down button in phone dock clicked.
     */
    public void onDialPadDownClicked() {
    }

    /**
     * Called when the dial pad up button in phone dock clicked.
     */
    public void onDialPadUpClicked() {
    }

    /**
     * Called when the check all button in edit dock clicked.
     * 
     * @param clicked
     */
    public void onCheckAllButtonChecked(boolean checked) {
    }

    /**
     * Called when the delete button in edit dock clicked.
     */
    public void onDeleteButtonClicked() {
    }

    /**
     * Called when the UI is needed to be restored.
     */
    public void onRestoreViewState() {
    }

}
