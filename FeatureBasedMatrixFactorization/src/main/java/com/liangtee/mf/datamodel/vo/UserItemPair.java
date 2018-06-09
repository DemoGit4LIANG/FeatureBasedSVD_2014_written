package com.liangtee.mf.datamodel.vo;

public class UserItemPair {

	public long UID;
	
	public long itemID;
	
	public double rating;
	
	public UserItemPair(long UID, long itemID, double rating) {
		this.UID = UID;
		this.itemID = itemID;
		this.rating = rating;
	}
	
	@Override
	public int hashCode() {
		return new Long(UID).hashCode() + new Long(itemID).hashCode();
	}
}
