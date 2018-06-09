package com.liangtee.mf.datamodel.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.ExtDataModel;
import com.liangtee.mf.datamodel.vo.AdInfo;
import com.liangtee.mf.datamodel.vo.ClickInfo;
import com.liangtee.mf.datamodel.vo.ItemPopularity;
import com.liangtee.mf.datamodel.vo.UserInfo;
import com.liangtee.mf.datamodel.vo.UserItemPair;

/**
 * BinaryDataModel stores a binary matrix (0-1 matrix) 
 * It is very useful in many practical problems
 * For example, users' browsing actions on web pages can be abstracted as a binary matrix
 * In the matrix, value 1 in row m & column n represents user m has visited  page n while value 0 
 * means the user has not visited the page
 * 
 * etc
 * 
 * @author liangtee
 *
 */

public class ExtBinaryDataModel implements ExtDataModel {

	static Logger log = LoggerFactory.getLogger(ExtBinaryDataModel.class);
	
	private Map<Long, Map<Long, ClickInfo>> dataSet = null;
	
	private Map<Long, UserInfo> userInfoMap = null;
	
	private Map<Long, AdInfo> adInfoMap = null;
	
	private Set<Long> adIDs = null;
	
	private Set<Integer> advIDs = null;
	
	private Set<Integer> picIDs = null;
	
	private Map<Integer, Integer> ageMap = null;
	
	private Map<Integer, Map<Integer, List<UserInfo>>> gender_age_UserInfo_Mapping = null;
	
	private Map<String, Set<Long>> same_ad_by_title = null;
	
	private Map<Integer, Set<Long>> same_ad_by_pic = null;
	
	private Map<Long, Set<Integer>> same_pic_by_ad = null;
	
	private Map<String, Set<Integer>> same_pic_by_title = null;
	
	private int positiveQty = 0;
	
	private int totalQty = 0;
	
//	public ExtBinaryDataModel() {
//		this.dataSet = new HashMap<Long, Map<Long, ClickInfo>>();
//		this.userInfoMap = new HashMap<Long, UserInfo>();
//	}
	
	public ExtBinaryDataModel(String trainFile, String userInfoFile, String adInfoFile) {
		log.info("Preparing to load data file : " + trainFile + "...");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(trainFile));
			
			this.dataSet = new HashMap<Long, Map<Long, ClickInfo>>();
			this.adIDs = new HashSet<Long>();
			this.advIDs = new HashSet<Integer>();
			this.picIDs = new HashSet<Integer>();
			
			this.ageMap = new HashMap<Integer, Integer>();
			this.adInfoMap = new HashMap<Long, AdInfo>();
			
			this.gender_age_UserInfo_Mapping = new HashMap<Integer, Map<Integer,List<UserInfo>>>();
			
			this.same_ad_by_title = new HashMap<String, Set<Long>>();
			this.same_pic_by_ad = new HashMap<Long, Set<Integer>>();
			this.same_ad_by_pic = new HashMap<Integer, Set<Long>>();
			this.same_pic_by_title = new HashMap<String, Set<Integer>>();
			
			String line = null;
			log.info("Starting to load data file : " + trainFile + "...");
			while((line = in.readLine()) != null) {
				String[] det = line.split(",");
				long UID = Long.parseLong(det[0]);
				long AID = Long.parseLong(det[1]);
				int advID = Integer.parseInt(det[2]);
				int picID = Integer.parseInt(det[3]);
				double clickValue = Float.parseFloat(det[5]);
				this.adIDs.add(AID);
				this.advIDs.add(advID);
				this.picIDs.add(picID);
				
				if(clickValue == 1D) this.positiveQty++;
				
				if(dataSet.containsKey(UID)) 
					dataSet.get(UID).put(AID, new ClickInfo(advID, picID, clickValue));
				else {
					Map<Long, ClickInfo> clickMap = new HashMap<Long, ClickInfo>();
					clickMap.put(AID, new ClickInfo(advID, picID, clickValue));
					dataSet.put(UID, clickMap);
				}
				this.totalQty++;
			}
			in.close();
			log.info("Successfully loaded data : " + this.totalQty + " from training set : " + trainFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		BufferedReader in2 = null;
		try {
			in2 = new BufferedReader(new FileReader(userInfoFile));
			this.userInfoMap = new HashMap<Long, UserInfo>();
			String line = null;
			int cnt = 0;
			
			gender_age_UserInfo_Mapping.put(1, new HashMap<Integer, List<UserInfo>>());
			gender_age_UserInfo_Mapping.put(2, new HashMap<Integer, List<UserInfo>>());
			
			Random random = new Random();
			while((line = in2.readLine()) != null) {
				String[] det = line.split(",");
				long UID = Long.parseLong(det[0]);
				int gender = 0;
				if(!det[1].equals("\\N")) gender = Integer.parseInt(det[1]); //1 or 2
				else gender = Math.round(random.nextFloat())+1;
				int age = 0;
				if(!det[2].equals("\\N")) age = Integer.parseInt(det[2]);
				if(ageMap.containsKey(age)) ageMap.put(age, ageMap.get(age)+1);
				else ageMap.put(age, 1);
				
				int period = Integer.parseInt(det[3]);
				int loc = 0;
				if(!det[4].equals("\\N")) loc = Integer.parseInt(det[4]);
				//else 
				
				UserInfo userInfo = new UserInfo(UID, gender, age, period, loc);
				userInfoMap.put(UID, userInfo);
				
				if(gender_age_UserInfo_Mapping.get(gender).containsKey(age)) {
					gender_age_UserInfo_Mapping.get(gender).get(age).add(userInfo);
				} else {
					List<UserInfo> users = new ArrayList<UserInfo>();
					users.add(userInfo);
					gender_age_UserInfo_Mapping.get(gender).put(age, users);
				}
				
				cnt++;
			}
			
			in2.close();
			log.info("Successfully loaded data : " + cnt + " lines from " + userInfoFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		BufferedReader in3 = null;
		try {
			in3 = new BufferedReader(new FileReader(adInfoFile));
			String line = null;
			int cnt = 0;
			while((line = in3.readLine()) != null) {
				String[] det = line.split(",");
				long adID = Integer.parseInt(det[0]);
				int picID = Integer.parseInt(det[2]);
//				String[] wordList = det[1].split("|");
				String title = det[1];
				this.adInfoMap.put(adID, new AdInfo(adID, picID, title));
				
				if(same_ad_by_title.containsKey(title)) {
					same_ad_by_title.get(title).add(adID);
				} else {
					Set<Long> adIDs = new HashSet<Long>();
					adIDs.add(adID);
					same_ad_by_title.put(title, adIDs);
				}
				
				if(same_ad_by_pic.containsKey(picID)) {
					same_ad_by_pic.get(picID).add(adID);
				} else {
					Set<Long> adIDs = new HashSet<Long>();
					adIDs.add(adID);
					same_ad_by_pic.put(picID, adIDs);
				}
				
				if(same_pic_by_ad.containsKey(adID)) {
					same_pic_by_ad.get(adID).add(picID);
				} else {
					Set<Integer> picIDs = new HashSet<Integer>();
					picIDs.add(picID);
					same_pic_by_ad.put(adID, picIDs);
				}
				
				cnt++;
			}
			
			in3.close();
			log.info("Successfully loaded data : " + cnt + " lines from " + adInfoFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public boolean contains(long UID, long AID) {
		if(this.dataSet.containsKey(UID)) return this.dataSet.get(UID).containsKey(AID);
		return false;
	}
	
	@Override
	public Set<Entry<Long, Map<Long, ClickInfo>>> entrySet() {
		return this.dataSet.entrySet();
	}


	@Override
	public int getTotalQty() {
		return this.totalQty;
	}


	@Override
	public Map<Long, ClickInfo> getClicksByID(long UID) {
		return this.dataSet.get(UID);
	}

	@Override
	public Set<Long> getAllUserIDs() {
		return this.dataSet.keySet();
	}

	@Override
	public Set<Long> getAllAdIDs() {
		return this.adIDs;
	}

	@Override
	public int getPositiveQty() {
		return this.positiveQty;
	}

	@Override
	public Set<Integer> getAdvIDs() {
		return this.advIDs;
	}

	@Override
	public Map<Integer, Integer> getAgeMap() {
		return this.ageMap;
	}

	@Override
	public Map<Long, UserInfo> getAllUserInfo() {
		return this.userInfoMap;
	}

	@Override
	public Set<Integer> getAllPicIDs() {
		return this.picIDs;
	}

	@Override
	public Map<Integer, Map<Integer, List<UserInfo>>> getGender_Age_User_Mapping() {
		return this.gender_age_UserInfo_Mapping;
	}

	@Override
	public Map<String, Set<Long>> getSameADsByTitle() {
		return this.same_ad_by_title;
	}

	@Override
	public Map<Integer, Set<Long>> getSameADsByPic() {
		return this.same_ad_by_pic;
	}

	@Override
	public Map<Long, Set<Integer>> getSamePicByAD() {
		return this.same_pic_by_ad;
	}

	@Override
	public Map<Long, AdInfo> getAllAdInfo() {
		return this.adInfoMap;
	}
	
	
}
