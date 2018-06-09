package com.liangtee.mf.datamodel.vo;

public class UserInfo {

	public long UID;
	
	public int gender;
	
	public int age;
	
	public int period;
	
	public int loc;
	
	public UserInfo(long UID, int gender, int age, int period, int loc) {
		this.UID = UID;
		this.gender = gender;
		this.age = age;
		this.period = period;
		this.loc = loc;
	}
	
}
