package com.carefor.membermanage;

import android.graphics.drawable.Drawable;

public class MemberInfo {

    //TODO 请使用同一的用户类型， 所有数据实体被存放在data/entity中

	private Drawable member_photo;
	private String member_name;
	private String member_phone;

	public Drawable getMember_photo() {
		return member_photo;
	}

	public void setMember_photo(Drawable member_photo) {
		this.member_photo = member_photo;
	}

	public String getMember_name() {
		return member_name;
	}

	public void setMember_name(String member_name) {
		this.member_name = member_name;
	}

	public String getMember_phone() {
		return member_phone;
	}

	public void setMember_phone(String member_phone) {
		this.member_phone = member_phone;
	}
}
