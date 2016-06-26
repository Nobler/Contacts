package com.wdjhzw.contacts.fragment;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdjhzw.contacts.R;
import com.wdjhzw.contacts.activity.ContactDetailActivity;
import com.wdjhzw.contacts.contact.Contact;

public class ContactDialogFragment extends DialogFragment implements
		LoaderCallbacks<Cursor>, OnClickListener {
	private Context mContext;
	private Contact mContact;

	private LinearLayout mContactInfo;
	private TextView mContactPhoto;
	private TextView mContactDisplayName;

	private LinearLayout mContactDetail;

	private int mScreenWidth;
	private int mScreenHeight;

	public ContactDialogFragment(Context context, Contact contact) {
		super();

		mContext = context;
		mContact = contact;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

		View view = inflater.inflate(R.layout.dialog_contact, container);
		mContactInfo = (LinearLayout) view.findViewById(R.id.contact_info);
		mContactInfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();

				Intent intent = new Intent(getActivity(),
						ContactDetailActivity.class);
				Bundle bundle = new Bundle();

				bundle.putParcelable("contact", mContact);
				intent.putExtras(bundle);

				startActivity(intent);
			}
		});
		mContactPhoto = (TextView) view.findViewById(R.id.contact_image);
		mContactDisplayName = (TextView) view.findViewById(R.id.contact_name);

		String name = mContact.getDisplayName();

		if (mContact.getPhotoImage() != null) {
			mContactPhoto.setBackground(new BitmapDrawable(getResources(),
					mContact.getPhotoImage()));
		} else {
			mContactPhoto
					.setText(String.valueOf(name.charAt(name.length() - 1)));
			mContactPhoto.setBackgroundColor(getResources().getColor(
					android.R.color.holo_blue_light));
		}

		mContactDisplayName.setText(name);

		mContactDetail = (LinearLayout) view.findViewById(R.id.contact_detail);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		getDialog().getWindow().setLayout(mScreenWidth, mScreenHeight);

		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onStop() {
		super.onStop();
		dismiss();
	}

	public void setScreenWidth(int mScreenWidth) {
		this.mScreenWidth = mScreenWidth;
	}

	public void setScreenHeight(int mScreenHeight) {
		this.mScreenHeight = mScreenHeight;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle bundle) {
		return new CursorLoader(
				mContext,
				ContactsContract.RawContactsEntity.CONTENT_URI,
				new String[] { "data1", "data2", "data3" },
				ContactsContract.RawContactsEntity.CONTACT_ID + "=? AND "
						+ ContactsContract.RawContactsEntity.MIMETYPE + "=?",
				new String[] {
						String.valueOf(mContact.getId()),
						ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE },
				"data_id");
	}

	@SuppressLint("InflateParams")
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		while (cursor.moveToNext()) {
			final String mainValue = cursor.getString(cursor
					.getColumnIndex("data1"));
			int type = cursor.getInt(cursor.getColumnIndex("data2"));
			String label = cursor.getString(cursor.getColumnIndex("data3"));

			final String typeLabel = (String) ContactsContract.CommonDataKinds.Phone
					.getTypeLabel(getResources(), type, label);

			View item = LayoutInflater.from(mContext).inflate(
					R.layout.item_contact_phone_info, null);
			TextView phoneNumberView = (TextView) item
					.findViewById(R.id.phone_number);
			TextView typeLabelView = (TextView) item
					.findViewById(R.id.type_label);
			ImageView featureIcon = (ImageView) item
					.findViewById(R.id.feature_icon);

			item.setTag(mainValue);
			item.setOnClickListener(this);
			item.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					ClipboardManager clipboard = (ClipboardManager) mContext
							.getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData clip = ClipData.newPlainText("PhoneNumber",
							mContact.getDisplayName() + "," + typeLabel + "," + mainValue);
					clipboard.setPrimaryClip(clip);

					Toast.makeText(
							mContext,
							getResources().getString(
									R.string.copy_to_clipboard_successfully),
							Toast.LENGTH_SHORT).show();
					return true;
				}
			});
			featureIcon.setTag(mainValue);
			featureIcon.setOnClickListener(this);

			phoneNumberView.setText(mainValue);
			typeLabelView.setText(typeLabel);

			mContactDetail.addView(item);
		}

		if (cursor != null) {
			cursor.close();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursor) {
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		String tag = (String) v.getTag();

		switch (v.getId()) {
		case R.id.item:
			intent.setAction(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:" + tag));
			break;
		case R.id.feature_icon:
			intent.setAction(Intent.ACTION_SENDTO);
			intent.setData(Uri.parse("sms:" + tag));

		default:
			break;
		}

		startActivity(intent);

		dismiss();
	}
}
