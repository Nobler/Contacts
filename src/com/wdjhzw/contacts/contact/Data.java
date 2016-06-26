package com.wdjhzw.contacts.contact;

import java.util.List;

public class Data implements Comparable<Data> {
	private MimetypeId id;
	private String mimetype;
	private List<String> valueList;

	public enum MimetypeId {

		PHONE(1),

		EMAIL(2),

		IM(3),

		NICKNAME(4),

		WEBSITE(5),

		SIP_ADDRESS(6),

		EVENT(7),

		GROUP_MEMBERSHIP(8),

		RELATION(9),

		NOTE(10),

		ORGANIZATION(11),

		STRUCTURED_POSTAL(12),

		STRUCTURED_NAME(13),

		QQ_PSTN(14),

		QQ_VOICE_CALL_PROFILE(15),

		MM_CHATTING_PROFILE(16),

		MM_CHATTING_VOIP_VIDEO(17),

		MM_PLUGIN_SNS_TIMELINE(18),
		
		IDENTITY(19),
		
		PHOTO(20),
		
		OTHERS(21);

		MimetypeId(int ni) {
			nativeInt = ni;
		}

		final int nativeInt;
	}

	public MimetypeId getId() {
		return id;
	}

	public String getMimetype() {
		return mimetype;
	}

	public List<String> getValueList() {
		return valueList;
	}

	public Data setId(MimetypeId id) {
		this.id = id;

		return this;
	}

	public Data setMimetype(String mimetype) {
		this.mimetype = mimetype;

		return this;
	}

	public Data setValueList(List<String> valueList) {
		this.valueList = valueList;
		return this;
	}

	@Override
	public String toString() {
		return "Data [mimetypeId=" + id + ", mimetype=" + mimetype + ", value="
				+ valueList + "]";
	}

	@Override
	public int compareTo(Data another) {
		return getId().compareTo(another.getId());
	}

}
