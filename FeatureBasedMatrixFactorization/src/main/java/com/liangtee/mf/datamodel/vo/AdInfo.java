package com.liangtee.mf.datamodel.vo;

import java.util.List;

public class AdInfo {

	public long adID;
	
	public int picID;
	
	public String[] wordList = null;
	
	public String title = null;
	
	public AdInfo(long adID, int picID, String title) {
		this.adID = adID;
		this.picID = picID;
//		this.wordList = wordList;
		this.title = title;
	}
}
