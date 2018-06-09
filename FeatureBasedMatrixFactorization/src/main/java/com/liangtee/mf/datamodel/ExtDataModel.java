package com.liangtee.mf.datamodel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.liangtee.mf.datamodel.vo.AdInfo;
import com.liangtee.mf.datamodel.vo.ClickInfo;
import com.liangtee.mf.datamodel.vo.ItemPopularity;
import com.liangtee.mf.datamodel.vo.UserInfo;
import com.liangtee.mf.datamodel.vo.UserItemPair;

/**
 * 
 * DataModel interface
 * 
 * @author liangtee
 * 2014.10.5
 *
 */
public interface ExtDataModel {
	
	public Map<Long, ClickInfo> getClicksByID(long UID);
	
	public Set<Entry<Long, Map<Long, ClickInfo>>> entrySet();
	
	public int getTotalQty();

	public boolean contains(long UID, long AID);
	
	public Set<Long> getAllUserIDs();
	
	public Set<Long> getAllAdIDs();
	
	public Set<Integer> getAdvIDs();
	
	public int getPositiveQty();
	
	public Map<Integer, Integer> getAgeMap();
	
	public Map<Long, UserInfo> getAllUserInfo();
	
	public Set<Integer> getAllPicIDs();
	
	public Map<Long, AdInfo> getAllAdInfo();
	
	public Map<Integer, Map<Integer, List<UserInfo>>> getGender_Age_User_Mapping();
	
	public Map<String, Set<Long>> getSameADsByTitle();
	
	public Map<Integer, Set<Long>> getSameADsByPic();
	
	public Map<Long, Set<Integer>> getSamePicByAD();

}
