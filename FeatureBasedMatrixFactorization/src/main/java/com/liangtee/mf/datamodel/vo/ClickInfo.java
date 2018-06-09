package com.liangtee.mf.datamodel.vo;

public class ClickInfo {

	public int advID = 0;
	
	public int picID = 0;
	
	public double clickValue = -1;
	
	public ClickInfo(int advID, int picID, double clickValue) {
		this.advID = advID;
		this.picID = picID;
		this.clickValue = clickValue;
	}
	
}
