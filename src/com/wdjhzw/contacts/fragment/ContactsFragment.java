package com.wdjhzw.contacts.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Filter.FilterListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdjhzw.contacts.R;
import com.wdjhzw.contacts.activity.ContactDetailActivity;
import com.wdjhzw.contacts.activity.MainActivity;
import com.wdjhzw.contacts.adapter.ContactListViewAdapter;
import com.wdjhzw.contacts.contact.Contact;
import com.wdjhzw.contacts.listener.OnViewStateChangedListener;
import com.wdjhzw.contacts.utils.ContactsManager;
import com.wdjhzw.contacts.view.ClearableEditText;
import com.wdjhzw.contacts.view.PinnedHeaderListView;
import com.wdjhzw.contacts.view.QuickIndexer;

public class ContactsFragment extends CustomFragment implements
		OnItemLongClickListener, OnItemClickListener {
	private Activity mActivity;

	private boolean mIsEditState = false;
	private OnViewStateChangedListener mListener;

	private LinearLayout mNoContactsView;
	private LinearLayout mContactsView;

	private ClearableEditText mSearchEdit;

	/**
	 * 保存读取到的联系人
	 */
	private List<Contact> mContactsList = new ArrayList<Contact>();

	/**
	 * 保存编辑状态下被选中的联系人
	 */
	private List<Contact> mContactsSelected = new ArrayList<Contact>();

	private ContactListViewAdapter mContactsListViewAdapter;
	private PinnedHeaderListView mContactsListView;
	private QuickIndexer mQuickIndexer;
	private TextView mCenterAlpha;

	private Handler mHandler = new Handler();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
		mListener = (OnViewStateChangedListener) mActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContactsListViewAdapter = new ContactListViewAdapter(mActivity,
				R.layout.item_contacts, mContactsList);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_contacts, container,
				false);
		mNoContactsView = (LinearLayout) view.findViewById(R.id.no_contact);
		mContactsView = (LinearLayout) view.findViewById(R.id.contacts);

		mSearchEdit = (ClearableEditText) view
				.findViewById(R.id.contacts_search);
		mSearchEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mContactsListViewAdapter.getFilter().filter(s,new FilterListener() {
					
					@Override
					public void onFilterComplete(int count) {
						// 当过滤完成后，更新全选按钮的全选与否状态
						mListener.onCheckAllButtonCheckedStateChanged(mContactsSelected
								.containsAll(mContactsListViewAdapter.getObjects()));
						
					}
				});

				// 输入框中有内容的时候，则隐藏QuickIndexer，并将ListView的首个item置顶
				if (!TextUtils.isEmpty(s)) {
					// 将ListView定位到首个item
					mContactsListView.setSelection(0);
					// 隐藏QuickIndexer
					mQuickIndexer.setVisibility(View.INVISIBLE);
				} else {
					mQuickIndexer.setCurrentChar(mContactsList.get(
							mContactsListView.getFirstVisiblePosition())
							.getSection());
					mQuickIndexer.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		mCenterAlpha = (TextView) view.findViewById(R.id.contacts_center_alpha);
		initContactsListView(view);
		initQuickIndexer(view);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d("ContactsFragment", "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d("ContactsFragment", "onResume");

		// 如果是编辑状态，则存储之前的联系人数据，之后用来筛选出已被删除的联系人
		List<Contact> oldContactsList = null;
		if (mIsEditState) {
			oldContactsList = new ArrayList<Contact>();
			oldContactsList.addAll(mContactsList);
		}

		ContactsManager.getInstance().readContacts(mContactsList);

		if (mContactsList.size() > 0) {
			mSearchEdit.setHint(getResources().getString(R.string.search_from)
					+ mContactsList.size()
					+ getResources().getString(R.string.contacts));
			mSearchEdit.setHintTextColor(getResources().getColor(
					android.R.color.darker_gray));

			Collections.sort(mContactsList); // 对联系人进行排序

			for (Contact contact : mContactsList) {
				Log.d("ContactsFragment", contact.getSortKey());
			}

			// 初始化分段索引，用于联系人的按名字拼音首字母分组显示
			mContactsListViewAdapter.initSectionIndexer(mContactsList);
			mContactsListViewAdapter.notifyDataSetChanged();

			// 将QuickIndexer定位到ListView中当前首个section对应的字母处
			mQuickIndexer.setCurrentChar(mContactsList.get(
					mContactsListView.getFirstVisiblePosition()).getSection());

			// 因为存在联系人被从外部修改的可能性，在编辑状态下需要刷新联系人的选中与否状态
			if (mIsEditState) {
				// 得到被删除的联系人
				oldContactsList.removeAll(mContactsList);

				// 从已选择的联系人当中移除已经不存在的联系人
				mContactsSelected.removeAll(oldContactsList);

				updateEditStateView();
			}

			mSearchEdit.clearFocus();

			showContactsView(true);
		} else {
			// 如果没有联系人，则不显示联系人视图
			showContactsView(false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d("ContactsFragment", "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		// 该界面不可见时，清空搜索框
		mSearchEdit.setText("");
		Log.d("ContactsFragment", "onStop");
	}

	private void initContactsListView(View view) {
		mContactsListView = (PinnedHeaderListView) view
				.findViewById(R.id.contacts_list_view);
		mContactsListView.setAdapter(mContactsListViewAdapter);
		mContactsListView.setOnItemClickListener(this);
		mContactsListView.setOnItemLongClickListener(this);
		mContactsListView.setPinnedHeaderView(LayoutInflater.from(mActivity)
				.inflate(R.layout.item_header_contacts, mContactsListView,
						false));
		mContactsListView.setOnScrollListener(new CustomOnScrollListener());
	}

	private void initQuickIndexer(View view) {
		mQuickIndexer = (QuickIndexer) view
				.findViewById(R.id.contacts_quick_indexer);
		mQuickIndexer.setOnTouchEventListener(new CustomOnTouchEventListener());
	}

	/**
	 * 隐藏联系人列表快滑时显示出来的中央字母图片
	 */
	private void dismissCenterAlpha() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mCenterAlpha != null) {
					mCenterAlpha.setVisibility(View.GONE);
				}
			}
		}, 500);
	}

	/**
	 * 设置是否显示联系人视图
	 * 
	 * @param shown
	 */
	private void showContactsView(boolean shown) {
		mNoContactsView.setVisibility(!shown ? View.VISIBLE : View.GONE);
		mContactsView.setVisibility(shown ? View.VISIBLE : View.GONE);
	}

	/**
	 * 更新编辑状态下的标题栏、底部全选按钮和删除按钮状态
	 */
	private void updateEditStateView() {
		if (mListener == null) {
			return;
		}

		if (mIsEditState) {
			mListener.onTitleBarTextChanged(getResources().getString(
					R.string.total_selected)
					+ "(" + mContactsSelected.size() + ")");
		} else {// 由非编辑状态进入编辑状态
			mIsEditState = true;

			mListener.onViewStateChanged(
					OnViewStateChangedListener.EDIT_VIEW_STATE, getResources()
							.getString(R.string.total_selected)
							+ "("
							+ mContactsSelected.size() + ")");
		}

		mListener.onCheckAllButtonCheckedStateChanged(mContactsSelected
				.containsAll(mContactsListViewAdapter.getObjects()));

		mListener
				.onDeleteButtonEnabledStateChanged(mContactsSelected.size() != 0);
	}

	@Override
	public boolean isEditState() {
		return mIsEditState;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Contact> onGetItemSelected() {
		return mContactsSelected;
	}

	@Override
	public void onBackKeyDown() {
		onRestoreViewState();
	}

	@Override
	public void onDeleteButtonClicked() {
		if (ContactsManager.getInstance().deleteContacts(mContactsSelected) == true) {
			onResume();
			Toast.makeText(mActivity, "delete successful!", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(mActivity, "delete failed!", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public boolean onPrepareOptionsMenuSyn(Menu menu) {
		return false;
	}

	@Override
	public void onRestoreViewState() {
		mIsEditState = false;
		mContactsSelected.clear();

		mContactsListViewAdapter.notifyDataSetChanged();
	}

	@Override
	public void onCheckAllButtonChecked(boolean checked) {
		mContactsSelected.removeAll(mContactsListViewAdapter.getObjects());
		if (checked) {
			mContactsSelected.addAll(mContactsListViewAdapter.getObjects());
		}

		mContactsListViewAdapter.notifyDataSetChanged();

		mListener.onTitleBarTextChanged(getResources().getString(
				R.string.total_selected)
				+ "(" + mContactsSelected.size() + ")");
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (mIsEditState) {
			// do nothing
		} else {
			mContactsSelected.add(mContactsListViewAdapter.getItem(position));
			mContactsListViewAdapter.notifyDataSetChanged();

			updateEditStateView();
		}

		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Contact contact = mContactsListViewAdapter.getItem(position);

		if (isEditState()) {
			if (mContactsSelected.contains(contact)) {
				mContactsSelected.remove(contact);
			} else {
				mContactsSelected.add(contact);
			}

			mContactsListViewAdapter.notifyDataSetChanged();

			updateEditStateView();
		} else {
			Intent intent = new Intent(mActivity, ContactDetailActivity.class);
			Bundle bundle = new Bundle();

			bundle.putParcelable("contact", contact);
			intent.putExtras(bundle);

			startActivity(intent);
		}
	}

	private class CustomOnScrollListener implements OnScrollListener {
		private boolean stateFling = false;
		private boolean stateTouchScroll = false;

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
				stateFling = true;
			} else if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
				stateTouchScroll = true;
			} else if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				stateFling = false;
				stateTouchScroll = false;

				dismissCenterAlpha();
			}
		}

		@Override
		public void onScroll(AbsListView view, final int firstVisibleItem,
				final int visibleItemCount, int totalItemCount) {
			// 在联系人数据被初始化完毕之前，section的数据为空，而在这之前，
			// 该方法会被调用，所以要进行一次判空处理
			if (mContactsListViewAdapter.getSections() != null) {
				// 调用configureHeaderView方法更新header的位置
				if (view instanceof PinnedHeaderListView) {
					((PinnedHeaderListView) view)
							.configureHeaderView(firstVisibleItem);
				}

				// 当ListView正处于查找模式的时候，section不起作用，直接返回
				if (mContactsListViewAdapter.isFilterMode()) {
					return;
				}

				// 得到当前ListView首个item的section
				String str = mContactsListViewAdapter.getSections()[mContactsListViewAdapter
						.getSectionForPosition(firstVisibleItem)].toString();
				// 因为QuickIndexer在滑动时也会带动ListView滑动，也会回调到该方法
				// 所以保证该方法是被用户手动滑动ListView的时候回调的，不然会混乱
				if (stateFling) {
					mCenterAlpha.setText(str);
					mCenterAlpha.setVisibility(View.VISIBLE);
				}
				// 让QuickIndexer定位到当前ListView中首个item所在的section对应的字母处
				if (stateTouchScroll && mQuickIndexer != null) {
					mQuickIndexer.setCurrentChar(str);
				}
			}
		}
	}

	private class CustomOnTouchEventListener implements
			QuickIndexer.OnTouchEventListener {

		@Override
		public void onActionDown(String str) {
			// 当按下QuickIndexer时，要将ViewPager设为不可滑动，
			// 否则ViewPager的滑动会使QuickIndexer失去焦点，不能相应抬起事件
			((MainActivity) mActivity).setPagingEnabled(false);

			// 让ListView定位到QuickIndexer正被点击的字母对应的section处
			if (mContactsListViewAdapter.getSections() != null) {
				int index = Arrays.binarySearch(
						mContactsListViewAdapter.getSections(), str);
				mContactsListView
						.setSelection(mContactsListViewAdapter
								.getPositionForSection(index >= 0 ? index
										: -index - 2));

				mCenterAlpha.setText(str);
				mCenterAlpha.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onActionUp() {
			// 只有在非编辑状态下从QuickIndexer上抬起，才将ViewPager设为可用
			if (!mIsEditState) {
				((MainActivity) mActivity).setPagingEnabled(true);
			}

			dismissCenterAlpha();
		}
	}
}
