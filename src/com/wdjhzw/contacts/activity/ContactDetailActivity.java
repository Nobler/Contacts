package com.wdjhzw.contacts.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdjhzw.contacts.R;
import com.wdjhzw.contacts.contact.Contact;
import com.wdjhzw.contacts.contact.Data;
import com.wdjhzw.contacts.contact.Data.MimetypeId;
import com.wdjhzw.contacts.contact.RawContact;
import com.wdjhzw.contacts.utils.ContactsManager;
import com.wdjhzw.contacts.view.CustomScrollView;
import com.wdjhzw.contacts.view.CustomScrollView.OnScrollChangedListener;

public class ContactDetailActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, OnClickListener {
	private final static String[] RAW_CONTACT_ENTITY_INFO = {
			ContactsContract.RawContacts._ID,
			ContactsContract.RawContacts.ACCOUNT_TYPE,
			ContactsContract.RawContacts.ACCOUNT_NAME,

			ContactsContract.RawContactsEntity.MIMETYPE,
			ContactsContract.RawContactsEntity.DATA1,
			ContactsContract.RawContactsEntity.DATA2,
			ContactsContract.RawContactsEntity.DATA3,
			ContactsContract.RawContactsEntity.DATA3,
			ContactsContract.RawContactsEntity.DATA4,
			ContactsContract.RawContactsEntity.DATA5,
			ContactsContract.RawContactsEntity.DATA6,
			ContactsContract.RawContactsEntity.DATA7,
			ContactsContract.RawContactsEntity.DATA8,
			ContactsContract.RawContactsEntity.DATA9,
			ContactsContract.RawContactsEntity.DATA10,
			ContactsContract.RawContactsEntity.DATA11,
			ContactsContract.RawContactsEntity.DATA12,
			ContactsContract.RawContactsEntity.DATA13,
			ContactsContract.RawContactsEntity.DATA14,
			ContactsContract.RawContactsEntity.DATA15 };

	private Contact mContact;
	private List<RawContact> mRawContactsList = new ArrayList<RawContact>();

	private LinearLayout mTitleBar;
	private LinearLayout mTitleBarBg;
	private ImageView mBack;
	private TextView mContactName;
	private TextView mContactNameBg;

	private CustomScrollView mInfoScrollView;
	private LinearLayout mContactDetail;
	private ImageView mContactPhoto;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContact = (Contact) getIntent().getParcelableExtra("contact");

		setContentView(R.layout.activity_contact_detail);

		initTitleBar();
		initScrollView();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// 异步查询对应contact_id的联系人，并按account_id进行排序，以此让同一RawContact的相关数据排列在一起
		return new CursorLoader(this,
				ContactsContract.RawContactsEntity.CONTENT_URI,
				RAW_CONTACT_ENTITY_INFO,
				ContactsContract.RawContactsEntity.CONTACT_ID + "=?",
				new String[] { String.valueOf(mContact.getId()) }, "account_id");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// 数据没有更新时，cursor指向的是最后一行之后
		Log.d("ContactDetailActivity", cursor.isAfterLast() + "");
		if (cursor.isAfterLast()) {
			return;
		}

		if (cursor.getCount() == 0) {
			// 如果已查询不到该联系人的相关信息，即该联系人已从外部删除，则结束该Activity
			finish();

			return;
		}

		// 初始化该contact的所有raw contact信息
		initRawContactsList(cursor);

		// 显示raw contact信息之前，先将之前显示的信息清除掉
		mContactDetail.removeViews(1, mContactDetail.getChildCount() - 1);

		for (RawContact rawContact : mRawContactsList) {
			displayAccountItem(rawContact);

			List<Data> list = rawContact.getDataList();
			for (Data data : list) {
				displayInfoItem(data);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	private void initTitleBar() {
		mTitleBar = (LinearLayout) findViewById(R.id.title_bar);
		mTitleBarBg = (LinearLayout) findViewById(R.id.title_bar_bg);
		mContactName = (TextView) findViewById(R.id.contact_name);
		mContactNameBg = (TextView) findViewById(R.id.contact_name_bg);
		mBack = (ImageView) findViewById(R.id.back);

		final String displayName = mContact.getDisplayName();
		mContactName.setText(displayName);
		mContactNameBg.setText(displayName);
		mContactName.setOnClickListener(new OnClickListener() {
			@SuppressLint("InflateParams")
			@Override
			public void onClick(View v) {
				View view = LayoutInflater.from(ContactDetailActivity.this)
						.inflate(R.layout.dialog_display_name, null);

				final AlertDialog dialog = new AlertDialog.Builder(
						ContactDetailActivity.this).setView(view).create();

				TextView content = (TextView) view.findViewById(R.id.content);
				Button positive = (Button) view.findViewById(R.id.positive);
				Button copy = (Button) view
						.findViewById(R.id.copy_to_clipboard);

				content.setText(displayName);
				positive.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				copy.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						copyToClipboard("DisplayName", displayName);

						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});

		// 测量文字长度
		Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		float textWidth = mTextPaint.measureText(displayName);
		if (textWidth > 120.0f) {
			// 如果文字长度超过120px，则将尾部截断
			mContactName.setEllipsize(TextUtils.TruncateAt.END);
			mContactNameBg.setEllipsize(TextUtils.TruncateAt.END);
		}

		mBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ContactDetailActivity.this.finish();
			}
		});
	}

	private void initScrollView() {
		mInfoScrollView = (CustomScrollView) findViewById(R.id.contact_detail_scroll_view);
		mInfoScrollView.setOnScrollListener(new OnScrollChangedListener() {
			@Override
			public void onScrollChanged(int x, int y, int oldX, int oldY) {
				// 当ScrollView滑动时，实现TitleBar淡入淡出效果
				float alpha = y / 100f;
				mTitleBarBg.setAlpha(alpha);
				mTitleBar.setAlpha(1 - alpha);
			}
		});

		mContactDetail = (LinearLayout) findViewById(R.id.contact_detail);

		mContactPhoto = (ImageView) findViewById(R.id.contact_photo);

		// 将头像ImageView的高度设为屏幕高度的一半
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		LayoutParams layoutParams = mContactPhoto.getLayoutParams();
		layoutParams.height = metrics.heightPixels / 2;
		mContactPhoto.setLayoutParams(layoutParams);

		if (mContact.getPhotoId() > 0) {
			// 读取联系人高清头像
			Bitmap bitmap = ContactsManager.getInstance().getContactPhoto(
					mContact.getId(), true);

			if (bitmap.getWidth() < 100) {
				// 如果联系人没有高清头像，则为其缩略图绘制圆形边框
				mContactPhoto.setImageBitmap(setCircleFrame(bitmap));
			} else {
				mContactPhoto.setImageBitmap(bitmap);
			}
		} else {
			// 如果联系人没有头像，则显示Android logo
			mContactPhoto.setImageResource(R.drawable.ic_android);
			mContactPhoto.setScaleType(ScaleType.FIT_END);
		}
	}

	private void initRawContactsList(Cursor cursor) {
		long oldId = -1;
		RawContact rawContact = null;

		mRawContactsList.clear();

		while (cursor.moveToNext()) {
			long id = cursor.getInt(cursor
					.getColumnIndex(ContactsContract.RawContacts._ID));

			if (oldId != id) {
				oldId = id;

				String accountType = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
				String accoutnName = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));

				rawContact = new RawContact(id).setContacId(mContact.getId())
						.setAccountType(accountType)
						.setAccountName(accoutnName)
						.setDataList(new ArrayList<Data>());
				mRawContactsList.add(rawContact);
			} else {
				rawContact = mRawContactsList.get(mRawContactsList.size() - 1);
			}

			Data data = new Data()
					.setMimetype(cursor.getString(cursor
							.getColumnIndex(ContactsContract.RawContactsEntity.MIMETYPE)));
			setData(cursor, data);

			rawContact.getDataList().add(data);
		}

		// 最后对所有raw contact的信息进行排序
		for (RawContact c : mRawContactsList) {
			Log.d("ContactDetailActivity", c.toString() + "\n");

			Collections.sort(c.getDataList());
		}
	}

	private void setData(Cursor cursor, Data data) {
		if (cursor == null || data == null) {
			return;
		}

		String mimetype = data.getMimetype();

		if (mimetype == null) {
			return;
		}

		String mainValue = cursor.getString(cursor.getColumnIndex("data1"));
		int type = cursor.getInt(cursor.getColumnIndex("data2"));
		String label = cursor.getString(cursor.getColumnIndex("data3"));
		String typeLabel = null;

		List<String> valueList = new ArrayList<String>();

		switch (mimetype) {
		case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.PHONE);
			typeLabel = (String) ContactsContract.CommonDataKinds.Phone
					.getTypeLabel(getResources(), type, label);
			break;
		case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.EMAIL);
			typeLabel = (String) ContactsContract.CommonDataKinds.Email
					.getTypeLabel(getResources(), type, label);
			break;
		case ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.SIP_ADDRESS);
			typeLabel = (String) ContactsContract.CommonDataKinds.SipAddress
					.getTypeLabel(getResources(), type, label);
			break;
		case ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.RELATION);
			typeLabel = (String) ContactsContract.CommonDataKinds.Relation
					.getTypeLabel(getResources(), type, label);
			break;
		case ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.STRUCTURED_POSTAL);
			typeLabel = (String) ContactsContract.CommonDataKinds.StructuredPostal
					.getTypeLabel(getResources(), type, label);
			// Also has other value STREET, CITY .etc
			break;
		case ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.ORGANIZATION);
			typeLabel = (String) ContactsContract.CommonDataKinds.Organization
					.getTypeLabel(getResources(), type, label);
			// Also has other value TITLE, DEPARTMENT .etc
			break;
		case ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.EVENT);
			// Call requires API level 21
			// typeLabel = (String) ContactsContract.CommonDataKinds.Event
			// .getTypeLabel(getResources(), typeId, label);
			break;
		case ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.GROUP_MEMBERSHIP);
			mainValue = ContactsManager.getInstance().getGroupTitle(
					Integer.valueOf(mainValue));
			break;
		case ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.WEBSITE);
			// 没有getTypeLabel()方法
			break;
		case ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.NICKNAME);
			// 没有getTypeLabel()方法
			break;
		case ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.NOTE);
			// 只有data1
			break;
		case ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.IM);
			int protocol = cursor
					.getInt(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
			String customProtocol = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL));
			typeLabel = ContactsContract.CommonDataKinds.Im.getProtocolLabel(
					getResources(), protocol, customProtocol).toString();
			break;

		case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.STRUCTURED_NAME);
			String givenName = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
			String familyName = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
			String prefix = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX));
			String middleName = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
			String sufix = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX));
			valueList.add(givenName);
			valueList.add(familyName);
			valueList.add(prefix);
			valueList.add(middleName);
			valueList.add(sufix);
			// Also has other value
			break;
		case ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.IDENTITY);
			break;
		case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
			data.setId(MimetypeId.PHOTO);
			break;

		// data1:电话号码，data2:QQ，data3:免费通话
		case "vnd.android.cursor.item/vnd.com.tencent.mobileqq.pstn":
			typeLabel = label;
			data.setId(MimetypeId.QQ_PSTN);
			break;

		// data1:电话号码，data2:QQ，data3:语音通话(WiFi下免费)
		case "vnd.android.cursor.item/vnd.com.tencent.mobileqq.voicecall.profile":
			typeLabel = label;
			data.setId(MimetypeId.QQ_VOICE_CALL_PROFILE);
			break;

		// data1:电话号码，data2:微信，data3:发送消息，data4:串号
		case "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.profile":
			typeLabel = label;
			data.setId(MimetypeId.MM_CHATTING_PROFILE);
			break;

		// data1:电话号码，data2:微信，data3:免费视频聊天，data4:串号
		case "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.voip.video":
			typeLabel = label;
			data.setId(MimetypeId.MM_CHATTING_VOIP_VIDEO);
			break;

		// data1:电话号码，data2:微信，data3:查看朋友圈，data4:串号
		case "vnd.android.cursor.item/vnd.com.tencent.mm.plugin.sns.timeline":
			typeLabel = label;
			data.setId(MimetypeId.MM_PLUGIN_SNS_TIMELINE);
			break;

		default:
			data.setId(MimetypeId.OTHERS);
			break;
		}

		if (data.getId() != MimetypeId.STRUCTURED_NAME) {
			if (mainValue != null) {
				valueList.add(mainValue);
			}

			if (typeLabel != null) {
				valueList.add(typeLabel);
			}
		}

		data.setValueList(valueList);
	}

	/**
	 * 根据raw contact在info list中显示一个账户item
	 * 
	 * @param rawContact
	 */
	@SuppressLint("InflateParams")
	private void displayAccountItem(RawContact rawContact) {
		View item = LayoutInflater.from(this).inflate(
				R.layout.item_contact_detail_account, null);
		TextView accountIcon = (TextView) item.findViewById(R.id.account_icon);
		final TextView accoutnType = (TextView) item
				.findViewById(R.id.account_type);
		final TextView accountName = (TextView) item
				.findViewById(R.id.account_name);

		String type = rawContact.getAccountType();
		if (type.contains("com.xiaomi")) {
			type = getResources().getString(R.string.xiaomi_account);
			accountIcon.setBackgroundResource(R.drawable.ic_xiaomi);
		} else if (type.contains("com.tencent.mobileqq")) {
			if (type.contains("com.tencent.mobileqqi")) {
				type = getResources().getString(R.string.qqi_account);
			} else {
				type = getResources().getString(R.string.qq_account);
			}
			accountIcon.setBackgroundResource(R.drawable.ic_qq);
		} else if (type.contains("com.tencent.mm")) {
			type = getResources().getString(R.string.wechat_account);
			accountIcon.setBackgroundResource(R.drawable.ic_weixin);
		} else {
			type = getResources().getString(R.string.other_account);
			accountIcon.setBackgroundColor(getResources().getColor(
					android.R.color.holo_blue_light));
			accountIcon.setText("?");
		}
		accoutnType.setText(type);
		accountName.setText(rawContact.getAccountName());

		item.setTag(type);
		item.setOnClickListener(this);
		item.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				StringBuilder builder = new StringBuilder();

				builder.append(accoutnType.getText().toString()).append(',')
						.append(accountName.getText().toString());

				copyToClipboard("AccountInfo", builder.toString());

				return true;
			}
		});

		mContactDetail.addView(item);
	}

	@SuppressLint("InflateParams")
	private void displayInfoItem(Data data) {
		if (data == null) {
			return;
		}

		View item = LayoutInflater.from(this).inflate(
				R.layout.item_contact_detail_info, null);
		ImageView typeIcon = (ImageView) item.findViewById(R.id.type_icon);
		final TextView mainValue = (TextView) item
				.findViewById(R.id.main_value);
		final TextView typeLabel = (TextView) item
				.findViewById(R.id.type_label);

		String mimetype = data.getMimetype();

		if (mimetype == null) {
			return;
		}

		List<String> valueList = data.getValueList();

		switch (mimetype) {
		case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_phone);
			item.setTag("tel:" + valueList.get(0));
			
			ImageView featureIcon = (ImageView) item
					.findViewById(R.id.feature_icon);
			featureIcon.setVisibility(View.VISIBLE);
			featureIcon.setTag("sms:" + valueList.get(0));
			featureIcon.setOnClickListener(this);
			break;
		case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_email);
			item.setTag("mailto:" + valueList.get(0));
			break;
		case ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_sip_address);
			break;
		case ContactsContract.CommonDataKinds.Relation.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_relation);
			break;
		case ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_structured_postal);
			item.setTag("geo:0,0?q=" + valueList.get(0));
			// Also has other value STREET, CITY .etc
			break;
		case ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_orgnization);
			// Also has other value TITLE, DEPARTMENT .etc
			break;

		case ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_event);
			// Call requires API level 21
			// typeLabel = (String) ContactsContract.CommonDataKinds.Event
			// .getTypeLabel(getResources(), typeId, label);
			break;
		case ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_group);
			break;
		case ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_website);
			if (!valueList.get(0).contains("http://")) {
				item.setTag("http://" + valueList.get(0));
			} else {
				item.setTag(valueList.get(0));
			}
			// 没有getTypeLabel()方法
			break;
		case ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_nickname);
			// 没有getTypeLabel()方法
			break;
		case ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_note);
			// 只有data1
			break;

		case ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE:
			typeIcon.setBackgroundResource(R.drawable.type_icon_im);
			break;

		case ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE:
			// Also has other value
		case ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE:
		case ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE:
			return;

			// data1:电话号码，data2:QQ，data3:免费通话
		case "vnd.android.cursor.item/vnd.com.tencent.mobileqq.pstn":
			// data1:电话号码，data2:QQ，data3:语音通话(WiFi下免费)
		case "vnd.android.cursor.item/vnd.com.tencent.mobileqq.voicecall.profile":
			// data1:电话号码，data2:微信，data3:发送消息，data4:串号
		case "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.profile":
			// data1:电话号码，data2:微信，data3:免费视频聊天，data4:串号
		case "vnd.android.cursor.item/vnd.com.tencent.mm.chatting.voip.video":
			// data1:电话号码，data2:微信，data3:查看朋友圈，data4:串号
		case "vnd.android.cursor.item/vnd.com.tencent.mm.plugin.sns.timeline":
			mainValue.setText(valueList.get(1));
			typeLabel.setVisibility(View.GONE);

			item.setOnClickListener(this);

			mContactDetail.addView(item);
			return;

		default:
			return;
		}

		if (valueList.size() > 1) {
			mainValue.setText(valueList.get(0));
			typeLabel.setText(valueList.get(1));
		} else {
			mainValue.setText(valueList.get(0));
			typeLabel.setVisibility(View.GONE);
		}

		item.setOnClickListener(this);
		item.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				StringBuilder builder = new StringBuilder();

				// 在该项信息之前添加联系人姓名
				builder.append(mContact.getDisplayName()).append(',');

				if (typeLabel.getText() != "") {
					builder.append(typeLabel.getText()).append(',');
				}

				builder.append(mainValue.getText().toString());

				copyToClipboard("ContactInfo", builder.toString());

				return true;
			}
		});

		mContactDetail.addView(item);
	}

	@Override
	public void onClick(View v) {
		String tag = (String) v.getTag();
		Intent intent = new Intent();
		ComponentName cmp = null;

		if (tag == null) {
			return;
		}

		if (tag.equals(getResources().getString(R.string.wechat_account))) {
			cmp = new ComponentName("com.tencent.mm",
					"com.tencent.mm.ui.LauncherUI");
		} else if (tag.equals(getResources().getString(R.string.qq_account))) {
			cmp = new ComponentName("com.tencent.mobileqq",
					"com.tencent.mobileqq.activity.SplashActivity");
		} else if (tag.equals(getResources().getString(R.string.qqi_account))) {
			cmp = new ComponentName("com.tencent.mobileqqi",
					"com.tencent.mobileqq.activity.SplashActivity");
		} else {
			String protocol = (tag.split(":", 2))[0];

			switch (protocol) {
			case "tel":
				intent.setAction(Intent.ACTION_CALL);
				break;
			case "sms":
			case "mailto":
				intent.setAction(Intent.ACTION_SENDTO);
				break;
			case "http":
			case "https":
			case "geo":
				intent.setAction(Intent.ACTION_VIEW);
				break;

			default:
				return;
			}
			intent.setData(Uri.parse(tag));
			startActivity(intent);

			return;
		}

		// String url11 =
		// "mqqwpa://im/chat?chat_type=wpa&uin=100000&version=1";
		// startActivity(new Intent(Intent.ACTION_VIEW,
		// Uri.parse(url11)));

		intent.setAction(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(cmp);
		startActivityForResult(intent, 0);
	}

	/**
	 * 给bitmap周围绘制白色圆形边框
	 * 
	 * @param bitmap
	 * @return
	 */
	private Bitmap setCircleFrame(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		int radius = Math.min(height / 2, width / 2);
		Bitmap output = Bitmap.createBitmap(width + 8, height + 8,
				Config.ARGB_8888);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		Canvas canvas = new Canvas(output);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setStyle(Style.FILL);

		canvas.drawCircle((width / 2) + 4, (height / 2) + 4, radius, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		canvas.drawBitmap(bitmap, 4, 4, paint);
		paint.setXfermode(null);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(3);
		canvas.drawCircle((width / 2) + 4, (height / 2) + 4, radius, paint);

		return output;
	}

	/**
	 * 将value复制到剪贴板
	 * 
	 * @param label
	 * @param value
	 */
	private void copyToClipboard(CharSequence label, String value) {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(label, value);
		clipboard.setPrimaryClip(clip);

		Toast.makeText(
				this,
				getResources().getString(
						R.string.copy_to_clipboard_successfully),
				Toast.LENGTH_SHORT).show();
	}
}
