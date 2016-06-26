package com.wdjhzw.contacts.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import com.wdjhzw.contacts.contact.Contact;
import com.wdjhzw.contacts.contact.RawContact;

public class ContactsManager {
	private static final String[] CONTACT_INFO = {
			ContactsContract.Contacts._ID,
			ContactsContract.Contacts.DISPLAY_NAME,
			ContactsContract.Contacts.PHOTO_ID };

	private static final String[] PHOTO_INFO = { ContactsContract.Data._ID,
			ContactsContract.Data.DATA_VERSION };

	private static final String[] RAW_CONTACT_INFO = {
			ContactsContract.RawContacts._ID,
			ContactsContract.RawContacts.CONTACT_ID,
			ContactsContract.RawContacts.ACCOUNT_TYPE,
			ContactsContract.RawContacts.ACCOUNT_NAME };

	private static Context mContext = null;
	private static volatile ContactsManager mInstance = null;

	private ContentResolver mResolver;

	private ContactsManager() {
		mResolver = mContext.getContentResolver();
	}

	public static void initManager(Context context) {
		if (mContext == null) {
			mContext = context;
		}
	}

	public static ContactsManager getInstance() {
		if (mInstance == null) {
			synchronized (ContactsManager.class) {
				if (mInstance == null) {
					mInstance = new ContactsManager();
				}
			}
		}

		return mInstance;
	}

	public void readContacts(List<Contact> list) {
		if (list == null) {
			return;
		}

		list.clear();

		Cursor contactsCursor = mResolver
				.query(ContactsContract.Contacts.CONTENT_URI, CONTACT_INFO,
						null, null, ContactsContract.Contacts.PHOTO_ID/* "sort_key COLLATE LOCALIZED asc " */);

		Cursor photoCursor = mResolver
				.query(ContactsContract.Data.CONTENT_URI,
						PHOTO_INFO,
						ContactsContract.Data.MIMETYPE + "=?",
						new String[] { ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE },
						ContactsContract.Data._ID);

		// 将查询到的联系人数据和头像数据进行联合
		CursorJoiner joiner = new CursorJoiner(contactsCursor,
				new String[] { ContactsContract.Contacts.PHOTO_ID },
				photoCursor, new String[] { ContactsContract.Data._ID });

		for (CursorJoiner.Result joinerResult : joiner) {
			int photoId = 0;
			int photoVersion = 0;

			switch (joinerResult) {
			case BOTH:
				// handle case where a row with the same key is in both cursors
				// 该联系人有头像，则拿到其PHOTO_ID和DATA_VERSION
				photoId = contactsCursor.getInt(contactsCursor
						.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
				photoVersion = photoCursor.getInt(photoCursor
						.getColumnIndex(ContactsContract.Data.DATA_VERSION));
			case LEFT:
				// handle case where a row in cursorA is unique
				int id = contactsCursor.getInt(contactsCursor
						.getColumnIndex(ContactsContract.Contacts._ID));
				String displayName = contactsCursor
						.getString(contactsCursor
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				// 因为不同平台对于联系人的数据库存储实现不同，sort_key并不一定是形如“z hang 张 s an 三”
				// 这种形式，可能只有汉字形式“张三”，而且还要考虑多音字问题，所以要把联系人名字转换为拼音进行一次排序
				String sortKey = HanziToPinyin.getInstance()
						.getPinyinFromHanzi(displayName, true);
				
				if (displayName != null) {
					list.add(new Contact(id, displayName).setSortKey(sortKey)
							.setPhoto(photoId, photoVersion));
				}

				break;
			case RIGHT:
				// handle case where a row in cursorB is unique
				break;

			default:
				break;
			}
		}

		if (contactsCursor != null) {
			contactsCursor.close();
		}
		if (photoCursor != null) {
			photoCursor.close();
		}

	}

	public void readRawContacts(long contactId, List<RawContact> list) {
		if (contactId < 0 || list == null) {
			return;
		}

		Cursor cursor = null;

		list.clear();

		try {
			cursor = mResolver.query(ContactsContract.RawContacts.CONTENT_URI,
					RAW_CONTACT_INFO, ContactsContract.RawContacts.CONTACT_ID
							+ "=?", new String[] { String.valueOf(contactId) },
					null);

			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor
						.getColumnIndex(ContactsContract.RawContacts._ID));
				String accountType = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
				String accoutnName = cursor
						.getString(cursor
								.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));

				list.add(new RawContact(id).setContacId(contactId)
						.setAccountType(accountType)
						.setAccountName(accoutnName).setDataList(null));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public boolean deleteContacts(List<Contact> list) {
		if (list == null) {
			return false;
		}

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		Contact contact = null;
		for (int i = 0; i < list.size(); i++) {
			contact = list.get(i);
			ops.add(ContentProviderOperation
					.newDelete(ContactsContract.RawContacts.CONTENT_URI)
					.withSelection(
							ContactsContract.RawContacts.CONTACT_ID + "=?",
							new String[] { String.valueOf(contact.getId()) })
					.build());
		}

		try {
			mResolver.applyBatch(ContactsContract.AUTHORITY, ops);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public String getGroupTitle(long groupId) {
		if (groupId > 0) {
			Cursor cursor = null;

			try {
				cursor = mResolver.query(ContactsContract.Groups.CONTENT_URI,
						new String[] { ContactsContract.Groups.TITLE },
						ContactsContract.Groups._ID + "=?",
						new String[] { String.valueOf(groupId) }, null);

				if (cursor.moveToNext()) {
					return cursor.getString(cursor
							.getColumnIndex(ContactsContract.Groups.TITLE));
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return null;
	}

	public Bitmap getContactPhoto(long contactId, boolean preferHighres) {
		Uri uri = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, contactId);
		InputStream input = ContactsContract.Contacts
				.openContactPhotoInputStream(mResolver, uri, preferHighres);

		return BitmapFactory.decodeStream(input);
	}
}
