package com.wdjhzw.contacts.contact;

import java.util.Locale;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable, Comparable<Contact> {

	private long id;
	private String displayName;
	private String sortKey;
	private int photoId;
	private int photoVersion;
	private Bitmap photoImage;
	
	/**
	 *对过滤结果进行排序时用到的ID 
	 */
	private int filterId = 0;

	public Contact(long id, String displayName) {
		this.id = id;
		this.displayName = displayName;
	}

	public Contact setSortKey(String sortKey) {
		this.sortKey = sortKey;
		return this;
	}

	public Contact setPhoto(int photoId, int photoVersion) {
		this.photoId = photoId;
		this.photoVersion = photoVersion;

		return this;
	}

	public Contact setPhotoImage(Bitmap photoImage) {
		this.photoImage = photoImage;

		return this;
	}
	
	public void setFilterId(int filterId) {
		this.filterId = filterId;
	}

	public long getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getSortKey() {
		return sortKey;
	}

	public int getPhotoId() {
		return photoId;
	}

	public int getPhotoThumbUri() {
		return photoVersion;
	}

	public Bitmap getPhotoImage() {
		return photoImage;
	}

	public String getSection() {
		return sortKey.substring(0, 1);
	}

	public String getPinyin() {
		return sortKey.replaceAll("[\\u4E00-\\u9FA5]", "").replace(" ", "");
	}

	public String getFirstPinyin() {
		return sortKey.replaceAll("[\\u4E00-\\u9FA5]", "")
				.replaceAll("[a-z]", "").replace(" ", "");
	}

	public int getFilterId() {
		return filterId;
	}
	
	@Override
	public int compareTo(Contact another) {
		// 字母转换为大写进行比较
		return getSortKey().toUpperCase(Locale.ENGLISH).compareTo(
				another.getSortKey().toUpperCase(Locale.ENGLISH));
	}

	@Override
	public String toString() {
		return "Contact [id=" + id + ", displayName=" + displayName
				+ ", sortKey=" + sortKey + ", photoId=" + photoId
				+ ", photoUri=" + photoVersion + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + photoId;
		result = prime * result + photoVersion;
		result = prime * result + ((sortKey == null) ? 0 : sortKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contact other = (Contact) obj;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (id != other.id)
			return false;
		if (photoId != other.photoId)
			return false;
		if (photoVersion != other.photoVersion)
			return false;
		if (sortKey == null) {
			if (other.sortKey != null)
				return false;
		} else if (!sortKey.equals(other.sortKey))
			return false;
		return true;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(displayName);
		dest.writeString(sortKey);
		dest.writeInt(photoId);
		dest.writeInt(photoVersion);
		if (photoImage != null) {
			photoImage.writeToParcel(dest, flags);
		}
	}

	public final static Parcelable.Creator<Contact> CREATOR = new Creator<Contact>() {

		@Override
		public Contact[] newArray(int size) {
			return new Contact[size];
		}

		@Override
		public Contact createFromParcel(Parcel source) {
			Contact contact = new Contact(source.readLong(),
					source.readString());
			contact.sortKey = source.readString();
			contact.photoId = source.readInt();
			contact.photoVersion = source.readInt();
			if (contact.photoId > 0) {
				contact.photoImage = Bitmap.CREATOR.createFromParcel(source);
			} else {
				contact.photoImage = null;
			}

			return contact;
		}
	};

}
