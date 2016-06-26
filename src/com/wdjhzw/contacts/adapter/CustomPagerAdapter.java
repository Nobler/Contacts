package com.wdjhzw.contacts.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.wdjhzw.contacts.fragment.ContactsFragment;
import com.wdjhzw.contacts.fragment.CustomFragment;
import com.wdjhzw.contacts.fragment.MessagingFragment;
import com.wdjhzw.contacts.fragment.PhoneFragment;

public class CustomPagerAdapter extends FragmentPagerAdapter {

    public static final int ITEM_COUNT = 3;
    public static final int ITEM_INDEX_PHONE = 0;
    public static final int ITEM_INDEX_CONTACTS = 1;
    public static final int ITEM_INDEX_MESSAGING = 2;

    private FragmentManager mFragmentManager = null;

    private CustomFragment mPhoneFragment = new PhoneFragment();
    private CustomFragment mContactsFragment = new ContactsFragment();
    private CustomFragment mMessagingFragment = new MessagingFragment();

    private int mPrimaryItem = 0;

    public CustomPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragmentManager = fm;
    }

    @Override
    public Fragment getItem(int arg0) {
        switch (arg0) {
        case ITEM_INDEX_PHONE:
            return (Fragment) mPhoneFragment;

        case ITEM_INDEX_CONTACTS:
            return (Fragment) mContactsFragment;

        case ITEM_INDEX_MESSAGING:
            return (Fragment) mMessagingFragment;

        default:
            return null;
        }
    }

    @Override
    public int getCount() {
        return ITEM_COUNT;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        mPrimaryItem = position;
    }

    public CustomFragment getPrimaryItem(ViewPager viewPager) {
        return getFragmentById(viewPager, mPrimaryItem);
    }

    public CustomFragment getFragmentById(ViewPager viewPager, int fragmentId) {
        String tag = makeFragmentName(viewPager.getId(), fragmentId);
        return (CustomFragment) mFragmentManager.findFragmentByTag(tag);
    }

    private static String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }
    //
    // public void Code(ViewPager viewPager, Message msg) {
    // if (mPhoneFragment == null)
    // mPhoneFragment = getFragmentByTag(viewPager, TAB_INDEX_PHONE);
    // if (mContactsFragment == null)
    // mContactsFragment = getFragmentByTag(viewPager, TAB_INDEX_CONTACTS);
    // if (mMessagingFragment == null)
    // mMessagingFragment = getFragmentByTag(viewPager,
    // TAB_INDEX_MESSAGING);
    //
    // switch (msg.what) {
    // case 1101: {
    // if (mPhoneFragment != null) {
    // mPhoneFragment.handleMessage(msg);
    // }
    // return;
    // }
    // case 1003:
    // case 1102: {
    // if (mContactsFragment != null) {
    // mContactsFragment.handleMessage(msg);
    // }
    // return;
    // }
    // case 1001: {
    // if (mPhoneFragment != null) {
    // mPhoneFragment.handleMessage(msg);
    // }
    // if (mContactsFragment != null) {
    // mContactsFragment.handleMessage(msg);
    // }
    // if (mMessagingFragment != null) {
    // mMessagingFragment.handleMessage(msg);
    // }
    // return;
    // }
    // case 1009:
    // case 1010: {
    // if (mContactsFragment != null) {
    // mContactsFragment.handleMessage(msg);
    // }
    // return;
    // }
    // case 1002: {
    // if (mContactsFragment != null) {
    // mContactsFragment.handleMessage(msg);
    // }
    // return;
    // }
    // case 1107: {
    // if (mContactsFragment != null) {
    // mContactsFragment.handleMessage(msg);
    // }
    // return;
    // }
    // case 1108: {
    // if (mPhoneFragment != null) {
    // mPhoneFragment.handleMessage(msg);
    // }
    // return;
    // }
    // case 1109: {
    // if (mMessagingFragment != null) {
    // mMessagingFragment.handleMessage(msg);
    // break;
    // }
    // }
    // }
    // }
}
