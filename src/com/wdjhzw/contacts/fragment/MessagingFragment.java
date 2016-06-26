package com.wdjhzw.contacts.fragment;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.wdjhzw.contacts.R;

public class MessagingFragment extends CustomFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messaging, container,
                false);
        return view;
    }

    @Override
    public boolean isEditState() {
        return false;
    }
    
    @Override
    public List<Object> onGetItemSelected() {
        return null;
    }

    @Override
    public void onBackKeyDown() {
    }

    @Override
    public boolean onPrepareOptionsMenuSyn(Menu menu) {
        return false;
    }

}
