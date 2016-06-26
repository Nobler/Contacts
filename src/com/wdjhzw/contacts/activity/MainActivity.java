package com.wdjhzw.contacts.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.wdjhzw.contacts.R;
import com.wdjhzw.contacts.adapter.CustomPagerAdapter;
import com.wdjhzw.contacts.fragment.CustomFragment;
import com.wdjhzw.contacts.listener.OnViewStateChangedListener;
import com.wdjhzw.contacts.utils.AssetsDatabaseManager;
import com.wdjhzw.contacts.utils.ContactsManager;
import com.wdjhzw.contacts.view.CustomViewPager;

public class MainActivity extends FragmentActivity implements OnClickListener,
		OnLongClickListener, OnViewStateChangedListener {

	private CustomViewPager mViewPager;
	private CustomPagerAdapter mViewPagerAdapter;

	// main tab
	private LinearLayout mMainTab;
	private LinearLayout mPhoneTab;
	private LinearLayout mContactsTab;
	private LinearLayout mMessagingTab;
	private ImageView mTabCursor;
	private LayoutParams mCursorLayoutParams;

	private int mDensityDpi;
	private int mScreenWidth;
	private int mScreenHeight;
	private int mTabCursorWidth;

	// title bar under edit state
	private LinearLayout mTitleBar;
	private LinearLayout mTitleBarBack;
	private TextView mTitleBarText;

	// dock in bottom
	private List<RelativeLayout> mDockList;
	private View mDockVisibile;
	private RelativeLayout mDockPhone;
	private RelativeLayout mDockContacts;
	private RelativeLayout mDockMessaging;
	private RelativeLayout mDockEdit;

	private LinearLayout mDialPadUp;
	private LinearLayout mDialPadDown;
	private LinearLayout mClearInput;
	private LinearLayout mCall;

	private LinearLayout mGroups;
	private LinearLayout mAddContact;
	private LinearLayout mBlockedContacts;

	private LinearLayout mNewMessage;

	private LinearLayout mDelete;
	private ImageView mDeleteImage;
	private CheckBox mCheckedAll;

	// Dialog
	private ProgressDialog mProgressDialog;
	private boolean mResult = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		getDisplayMetrics();

		initView();
		setViewPager();

		// 用Application实例初始化AssetsDatabaseManager，只需调用一次
		AssetsDatabaseManager.initManager(getApplication());

		ContactsManager.initManager(getApplication());
	}

	/**
	 * 得到屏幕显示的相关参数，用于动态创建顶部tab下方的cursor，以及ViewPager中item的间隔
	 */
	private void getDisplayMetrics() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		mScreenWidth = metrics.widthPixels;
		mScreenHeight = metrics.heightPixels;
		mDensityDpi = metrics.densityDpi;
	}

	public int getScreenWidth() {
		return mScreenWidth;
	}

	public int getScreenHeight() {
		return mScreenHeight;
	}

	private void initView() {
		initTabCursor();

		// main_tab
		mMainTab = (LinearLayout) findViewById(R.id.main_tab);
		mPhoneTab = (LinearLayout) findViewById(R.id.phone_tab);
		mPhoneTab.setOnClickListener(this);
		mContactsTab = (LinearLayout) findViewById(R.id.contacts_tab);
		mContactsTab.setOnClickListener(this);
		mMessagingTab = (LinearLayout) findViewById(R.id.messaging_tab);
		mMessagingTab.setOnClickListener(this);

		// title bar under edit state
		mTitleBar = (LinearLayout) findViewById(R.id.title_bar_edit);
		mTitleBarBack = (LinearLayout) findViewById(R.id.back);
		mTitleBarBack.setOnClickListener(this);
		mTitleBarText = (TextView) findViewById(R.id.title);

		// dock in bottom
		mDockList = new ArrayList<RelativeLayout>();
		mDockList
				.add(mDockPhone = (RelativeLayout) findViewById(R.id.dock_phone));
		mDockList
				.add(mDockContacts = (RelativeLayout) findViewById(R.id.dock_contacts));
		mDockList
				.add(mDockMessaging = (RelativeLayout) findViewById(R.id.dock_messaging));
		mDockList
				.add(mDockEdit = (RelativeLayout) findViewById(R.id.dock_edit));

		mDialPadUp = (LinearLayout) findViewById(R.id.dial_pad_up);
		mDialPadUp.setOnClickListener(this);
		mDialPadDown = (LinearLayout) findViewById(R.id.dial_pad_down);
		mDialPadDown.setOnClickListener(this);
		mClearInput = (LinearLayout) findViewById(R.id.clear_input);
		mClearInput.setOnClickListener(this);
		mClearInput.setOnLongClickListener(this);
		mCall = (LinearLayout) findViewById(R.id.call);
		mCall.setOnClickListener(this);

		mGroups = (LinearLayout) findViewById(R.id.groups);
		mGroups.setOnClickListener(this);
		mAddContact = (LinearLayout) findViewById(R.id.add_contact);
		mAddContact.setOnClickListener(this);
		mBlockedContacts = (LinearLayout) findViewById(R.id.blocked_contacts);
		mBlockedContacts.setOnClickListener(this);

		mNewMessage = (LinearLayout) findViewById(R.id.new_message);
		mNewMessage.setOnClickListener(this);

		mDelete = (LinearLayout) findViewById(R.id.delete);
		mDelete.setOnClickListener(this);
		mDeleteImage = (ImageView) findViewById(R.id.delete_image);
		mCheckedAll = (CheckBox) findViewById(R.id.check_all);
		mCheckedAll.setOnClickListener(this);
	}

	/**
	 * 创建顶部tab下方的cursor
	 */
	private void initTabCursor() {
		mTabCursor = ((ImageView) findViewById(R.id.cursor));
		mCursorLayoutParams = (LayoutParams) mTabCursor.getLayoutParams();

		// 从资源文件中解析出用于动态创建cursor的位图（一个1*1的像素）
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.tab_cursor);
		if (bitmap != null) {
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();

			Matrix matrix = new Matrix();
			// 设置cursor的缩放比例：1/3屏幕宽度，5dp高度
			matrix.postScale(mTabCursorWidth = mScreenWidth / 3 / width, 5
					/ height * mDensityDpi);
			// 用单位像素来创建cursor
			mTabCursor.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, width,
					height, matrix, true));
		}
	}

	private void setViewPager() {
		mViewPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());

		mViewPager = (CustomViewPager) findViewById(R.id.view_pager);
		mViewPager.setAdapter(mViewPagerAdapter);
		mViewPager.setOnPageChangeListener(new CustomOnPageChangeListener());
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setPageMargin(mScreenWidth / 100);// item间隔为屏宽的1/100
		mViewPager.setPageMarginDrawable(R.color.divider_bg);
		mViewPager.setCurrentItem(CustomPagerAdapter.ITEM_INDEX_CONTACTS);
	}

	public void setPagingEnabled(boolean enabled) {
		mViewPager.setPagingEnabled(enabled);
	}

	public CustomFragment getPrimaryPage() {
		return mViewPagerAdapter == null ? null : mViewPagerAdapter
				.getPrimaryItem(mViewPager);
	}

	private View getVisibleDock() {
		for (RelativeLayout layout : mDockList) {
			if (layout.getVisibility() == View.VISIBLE) {
				return layout;
			}
		}

		return null;
	}

	private void setVisibleDock(View dock) {
		if (!mDockList.contains(dock)) {
			return;
		}

		for (RelativeLayout layout : mDockList) {
			layout.setVisibility(View.GONE);
		}
		dock.setVisibility(View.VISIBLE);
	}

	private void displayTitleBar(boolean displayable, String title) {
		if (displayable) {
			mTitleBar.setVisibility(View.VISIBLE);
			mMainTab.setVisibility(View.GONE);
			if (title != null) {
				onTitleBarTextChanged(title);
			}
		} else {
			mTitleBar.setVisibility(View.GONE);
			mMainTab.setVisibility(View.VISIBLE);
		}
	}

	private void displayDockEdit(boolean displayable) {
		if (displayable) {
			if (!getVisibleDock().equals(mDockEdit)) {
				mDockVisibile = getVisibleDock();
				setVisibleDock(mDockEdit);
			}
		} else {
			if (mDockVisibile != null) {
				setVisibleDock(mDockVisibile);
			}
		}
	}

	private void displayDialPad(boolean displayable) {
		int visibility = displayable ? View.VISIBLE : View.GONE;

		mDialPadDown.setVisibility(visibility);
		mCall.setVisibility(visibility);
		mClearInput.setVisibility(visibility);

		mDialPadUp.setVisibility(!displayable ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onClick(View v) {
		CustomFragment fg = getPrimaryPage();

		switch (v.getId()) {
		case R.id.phone_tab:
			mViewPager.setCurrentItem(CustomPagerAdapter.ITEM_INDEX_PHONE);
			break;
		case R.id.contacts_tab:
			mViewPager.setCurrentItem(CustomPagerAdapter.ITEM_INDEX_CONTACTS);
			break;
		case R.id.messaging_tab:
			mViewPager.setCurrentItem(CustomPagerAdapter.ITEM_INDEX_MESSAGING);
			break;
		case R.id.dial_pad_up:
			displayDialPad(true);
			fg.onDialPadUpClicked();
			break;
		case R.id.dial_pad_down:
			displayDialPad(false);
			fg.onDialPadDownClicked();
			break;
		case R.id.call:
			fg.onCallClicked();
			break;
		case R.id.clear_input:
			fg.onClearInputClicked();
			break;
		case R.id.delete:
			// mProgressDialog = ProgressDialog.show(this, "正在操作…", "请稍等",
			// false, false);
			// mProgressDialog.setOnDismissListener(new OnDismissListener() {
			// @Override
			// public void onDismiss(DialogInterface dialog) {
			// if (mResult) {
			// Toast.makeText(MainActivity.this, "操作成功", Toast.LENGTH_SHORT)
			// .show();
			// } else {
			// Toast.makeText(MainActivity.this, "操作失败", Toast.LENGTH_SHORT)
			// .show();
			// }
			// }
			// });

			fg.onDeleteButtonClicked();
		case R.id.back:
			fg.onRestoreViewState();
			onViewStateChanged(OnViewStateChangedListener.NORMAL_VIEW_STATE,
					null);
			break;
		case R.id.check_all:
			fg.onCheckAllButtonChecked(mCheckedAll.isChecked());
			onDeleteButtonEnabledStateChanged(mCheckedAll.isChecked());
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()) {
		case R.id.clear_input:
			getPrimaryPage().onClearInputLongClicked();
			return true;

		default:
			return false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_MENU) && (event.getRepeatCount() > 0)) {
			return true;
		}

		if ((keyCode == KeyEvent.KEYCODE_BACK) && (isTaskRoot())) {
			if (getPrimaryPage().isEditState()) {
				onViewStateChanged(
						OnViewStateChangedListener.NORMAL_VIEW_STATE, null);
				getPrimaryPage().onBackKeyDown();
			} else {
				moveTaskToBack(false);
			}

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			getPrimaryPage().onPrepareOptionsMenuSyn(null);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onTitleBarTextChanged(String title) {
		mTitleBarText.setText(title);
	}

	@Override
	public void onViewStateChanged(int state, String title) {
		boolean displayable = (state == OnViewStateChangedListener.NORMAL_VIEW_STATE ? false
				: (state == OnViewStateChangedListener.EDIT_VIEW_STATE ? true
						: null));

		displayTitleBar(displayable, title);
		displayDockEdit(displayable);
		setPagingEnabled(!displayable);
	}

	@Override
	public void onDeleteButtonEnabledStateChanged(boolean enabled) {
		mDelete.setEnabled(enabled);
		mDeleteImage.setEnabled(enabled);
	}

	@Override
	public void onCheckAllButtonCheckedStateChanged(boolean checked) {
		mCheckedAll.setChecked(checked);
	}

	private class CustomOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int state) {

		}

		/**
		 * 让顶部tab的cursor跟随ViewPager的item滑动并定位
		 */
		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			Log.d("MainActivity", position + ":" + positionOffsetPixels);

			// automatically move the cursor followed by ViewPager's item
			mCursorLayoutParams.leftMargin = (int) (mTabCursorWidth * position + (double) positionOffsetPixels
					/ mScreenWidth * mTabCursorWidth);
			mTabCursor.setLayoutParams(mCursorLayoutParams);
		}

		/**
		 * ViewPager中item更改之后，底部切换到对应dock
		 */
		@Override
		public void onPageSelected(int position) {
			switch (position) {
			case CustomPagerAdapter.ITEM_INDEX_PHONE:
				setVisibleDock(mDockPhone);
				break;
			case CustomPagerAdapter.ITEM_INDEX_CONTACTS:
				setVisibleDock(mDockContacts);
				break;
			case CustomPagerAdapter.ITEM_INDEX_MESSAGING:
				setVisibleDock(mDockMessaging);
				break;

			default:
				break;
			}
		}
	}

}
