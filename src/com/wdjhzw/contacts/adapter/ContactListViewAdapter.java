package com.wdjhzw.contacts.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.wdjhzw.contacts.R;
import com.wdjhzw.contacts.activity.MainActivity;
import com.wdjhzw.contacts.contact.Contact;
import com.wdjhzw.contacts.fragment.ContactDialogFragment;
import com.wdjhzw.contacts.fragment.CustomFragment;
import com.wdjhzw.contacts.utils.ImageLoader;
import com.wdjhzw.contacts.view.PinnedHeaderListView.PinnedHeaderAdapter;

public class ContactListViewAdapter extends ArrayAdapter<Contact> implements
		PinnedHeaderAdapter, SectionIndexer, Filterable, OnClickListener {
	private List<String> mSections = null;
	private List<Integer> mPositions = null;

	private Context mContext;
	private int mResourceId;
	private LayoutInflater mInflater;

	private ImageLoader mLoader;

	/**
	 * 标识当前是否是过滤模式
	 */
	private boolean mFilterMode = false;

	private List<Contact> mObjects;
	private List<Contact> mOriginalValues;
	private CustomFilter mFilter;

	public ContactListViewAdapter(Context context, int resource,
			List<Contact> objects) {
		super(context, resource, objects);

		mContext = context;
		mResourceId = resource;
		mInflater = LayoutInflater.from(mContext);

		mLoader = ImageLoader.getInstance();
	}

	public boolean isFilterMode() {
		return mFilterMode;
	}

	public List<Contact> getObjects() {
		return mObjects;
	}

	/**
	 * 通过<b>已排序的</b>联系人来初始化SectionIndexer
	 * 
	 * @param list
	 */
	public void initSectionIndexer(List<Contact> list) {
		mObjects = list;
		mOriginalValues = list;

		mSections = new ArrayList<String>();
		mPositions = new ArrayList<Integer>();

		char oldChar = '\0';
		for (int i = 0; i < list.size(); ++i) {
			// 将sort_key统一转换为大写进行分段索引
			char ch = list.get(i).getSortKey().toUpperCase(Locale.ENGLISH)
					.charAt(0);
			if (oldChar != ch) {
				mSections.add(String.valueOf(ch));
				mPositions.add(Integer.valueOf(i));
			}
			oldChar = ch;
		}
	}

	@Override
	public int getCount() {
		if (mObjects == null) {
			return 0;
		}
		return mObjects.size();
	}

	@Override
	public Contact getItem(int arg0) {
		return mObjects.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		ViewHolder holder;
		final Contact contact = mObjects.get(position);
		final CustomFragment fragment = ((MainActivity) mContext)
				.getPrimaryPage();

		if (convertView == null) {
			view = mInflater.inflate(mResourceId, null);

			holder = new ViewHolder();
			holder.section = (TextView) view.findViewById(R.id.header);
			holder.divider = (ImageView) view.findViewById(R.id.divider);
			holder.contactImage = (ImageView) view
					.findViewById(R.id.contact_image);
			holder.contactWord = (TextView) view
					.findViewById(R.id.contact_word);
			holder.contactWord.setOnClickListener(this);
			holder.displayName = (TextView) view
					.findViewById(R.id.contact_name);
			holder.check = (CheckBox) view.findViewById(R.id.check);

			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		// 当前item是header则显示section部分；否则显示分割线
		int section = getSectionForPosition(position);
		if (position == getPositionForSection(section) && !mFilterMode) {
			holder.section.setVisibility(View.VISIBLE);
			holder.section.setText(String.valueOf(getSections()[section]));

			holder.divider.setVisibility(View.GONE);
		} else {
			holder.section.setVisibility(View.GONE);

			holder.divider.setVisibility(View.VISIBLE);
		}

		// 编辑状态下显示复选框，并根据其是否在选中列表中来选中或不选中；否则不显示复选框
		if (fragment != null && fragment.isEditState()) {
			holder.check.setVisibility(View.VISIBLE);
			holder.check.setChecked(fragment.onGetItemSelected().contains(
					contact));
		} else {
			holder.check.setVisibility(View.INVISIBLE);
		}

		// 为头像设置Tag，当点击头像的时候以此显示对应联系人的内容
		holder.contactWord.setTag(contact);
		if (contact.getPhotoId() > 0) {// 联系人有头像
			holder.contactWord.setText(null);
			if (contact.getPhotoImage() == null) {
				mLoader.loadImage(contact, holder.contactImage);
			}
			holder.contactImage.setImageBitmap(contact.getPhotoImage());
		} else {// 联系人没有头像，显示动态文字头像
			String name = contact.getDisplayName();
			holder.contactWord
					.setText(String.valueOf(name.charAt(name.length() - 1)));
			holder.contactImage
					.setImageResource(android.R.color.holo_blue_light);
		}

		holder.displayName.setText(contact.getDisplayName());

		return view;
	}

	class ViewHolder {
		TextView section;
		ImageView divider;
		ImageView contactImage;
		TextView contactWord;
		TextView displayName;
		CheckBox check;
	}

	@Override
	public void onClick(View v) {
		// 如果当前fragment正处于编辑状态，则不弹出Dialog
		if (((MainActivity) mContext).getPrimaryPage().isEditState()) {
			return;
		}

		ContactDialogFragment f = new ContactDialogFragment(mContext,
				(Contact) v.getTag());

		f.setScreenWidth((int) (((MainActivity) mContext).getScreenWidth() / 1.1));
		f.setScreenHeight((int) (((MainActivity) mContext).getScreenHeight() / 1.9));

		f.show(((FragmentActivity) mContext).getSupportFragmentManager(),
				"Number");
	}

	@Override
	public int getPinnedHeaderState(int position) {
		if (position < 0 || mFilterMode) {
			return PINNED_HEADER_GONE;
		}

		int section = getSectionForPosition(position);
		int nextSectionPosition = getPositionForSection(section + 1);
		if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
			// 如果当前首个item为其所属的section中的最后一个item，则header处于将要上移的状态
			// 至于是否需要上移，由PinnedHeaderListView中的configureHeaderView方法按实际情况决定
			return PINNED_HEADER_PUSHED_UP;
		}
		return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View header, int position, int alpha) {
		if (mSections != null && mPositions != null && !mFilterMode) {
			int section = getSectionForPosition(position);
			String title = (String) getSections()[section];
			((TextView) header.findViewById(R.id.header)).setText(title);
		}
	}

	@Override
	public Object[] getSections() {
		return mSections != null ? mSections.toArray() : null;
	}

	@Override
	public int getPositionForSection(int sectionIndex) {
		if (mPositions == null
				|| (sectionIndex < 0 || sectionIndex >= mPositions.size())) {
			return -1;
		}
		return mPositions.get(sectionIndex);
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mPositions == null || position < 0 || position >= getCount()) {
			return -1;
		}
		// the non-negative index of the element, or a negative index which is
		// -index - 1 where the element would be inserted.
		int index = Arrays.binarySearch(mPositions.toArray(), position);
		// index = -index'-1,index'-1=-index-2
		return index >= 0 ? index : -index - 2;
	}

	public CustomFilter getFilter() {
		if (mFilter == null) {
			mFilter = new CustomFilter();
		}

		return mFilter;
	}

	public class CustomFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();

			if (constraint == null || constraint.length() == 0) {
				mFilterMode = false;

				results.values = mOriginalValues;
				results.count = mOriginalValues.size();
			} else {
				mFilterMode = true;

				List<Contact> list = new ArrayList<Contact>();

				for (Contact c : mOriginalValues) {
					if (c.getDisplayName()
							.toUpperCase(Locale.ENGLISH)
							.startsWith(
									constraint.toString().toUpperCase(
											Locale.ENGLISH))
							|| c.getPinyin()
									.toUpperCase(Locale.ENGLISH)
									.startsWith(
											constraint.toString().toUpperCase(
													Locale.ENGLISH))
							|| c.getFirstPinyin()
									.toUpperCase(Locale.ENGLISH)
									.startsWith(
											constraint.toString().toUpperCase(
													Locale.ENGLISH))) {
						// 在对过滤结果进行排序的时候，保证名字中首先出现关键词的联系人排在前面
						c.setFilterId(0);
						list.add(c);
					} else if (c
							.getDisplayName()
							.toUpperCase(Locale.ENGLISH)
							.indexOf(
									constraint.toString().toUpperCase(
											Locale.ENGLISH)) != -1
							|| c.getPinyin()
									.toUpperCase(Locale.ENGLISH)
									.indexOf(
											constraint.toString().toUpperCase(
													Locale.ENGLISH)) != -1
							|| c.getFirstPinyin()
									.toUpperCase(Locale.ENGLISH)
									.indexOf(
											constraint.toString().toUpperCase(
													Locale.ENGLISH)) != -1) {
						c.setFilterId(1);
						list.add(c);
					}
				}

				Collections.sort(list, new Comparator<Contact>() {
					@Override
					public int compare(Contact arg0, Contact arg1) {
						return Integer.valueOf(arg0.getFilterId()).compareTo(
								arg1.getFilterId());
					}
				});

				results.values = list;
				results.count = list.size();
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			mObjects = (List<Contact>) results.values;

			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
