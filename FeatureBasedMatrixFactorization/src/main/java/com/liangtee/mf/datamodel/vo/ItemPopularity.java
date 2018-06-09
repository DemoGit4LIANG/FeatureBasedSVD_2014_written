package com.liangtee.mf.datamodel.vo;

public class ItemPopularity {

	public long itemID;
	
	public int popularity = 0;
	
	public ItemPopularity(long itemID, int popularity) {
		this.itemID = itemID;
		this.popularity = popularity;
	}

	@Override
	public String toString() {
		return "itemID : " + itemID + ", popularity : " + popularity + "\n";
	}
	
}
