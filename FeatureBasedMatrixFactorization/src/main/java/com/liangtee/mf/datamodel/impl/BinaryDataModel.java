package com.liangtee.mf.datamodel.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.vo.ItemPopularity;
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

public class BinaryDataModel implements DataModel {

	Logger log = LoggerFactory.getLogger(BinaryDataModel.class);
	
	private Map<Long, Map<Long, Double>> dataSet = null;
	
	private List<UserItemPair> userItemPairs = null;
	
	private Map<Long, Integer> itemsPopularity = null;
	
	private List<ItemPopularity> itemsPopularityArray = null;
	
	private int totalLines = 0;
	
	public BinaryDataModel() {
		this.dataSet = new HashMap<Long, Map<Long, Double>>();
		this.userItemPairs = new ArrayList<UserItemPair>();
		this.itemsPopularity = new HashMap<Long, Integer>();
		this.itemsPopularityArray = new ArrayList<ItemPopularity>();
	}
	
	public BinaryDataModel(String filePath) {
		log.info("Preparing to load data file : " + filePath + "...");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filePath));
			this.dataSet = new HashMap<Long, Map<Long, Double>>();
			this.itemsPopularity = new HashMap<Long, Integer>();
			this.userItemPairs = new ArrayList<UserItemPair>();
			this.itemsPopularityArray = new ArrayList<ItemPopularity>();
			
			
			String line = null;
			log.info("Starting to load data file : " + filePath + "...");
			int cnt = 0;
			while((line = in.readLine()) != null) {
				String[] det = line.split(",");
				long UID = Long.parseLong(det[0]);
				long IID = Long.parseLong(det[1]);
				double binaryRating = Long.parseLong(det[2]);
				
//				int popularity = itemsPopularity.containsKey(IID) ? itemsPopularity.get(IID)+1 : 1;
				itemsPopularity.put(IID, itemsPopularity.containsKey(IID) ? itemsPopularity.get(IID)+1 : 1);
				
				if(dataSet.containsKey(UID)) 
					dataSet.get(UID).put(IID, binaryRating);
				else {
					Map<Long, Double> binaryRatingMap = new HashMap<Long, Double>();
					binaryRatingMap.put(IID, binaryRating);
					dataSet.put(UID, binaryRatingMap);
				}
				userItemPairs.add(new UserItemPair(UID, IID, binaryRating));
				cnt++;
			}
			in.close();
			this.totalLines = cnt;
			initAccessories();
			log.info("Successfully loaded data : " + cnt + " .. ");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Sorting items according to their popularity
	 * The most popular item lies in the frontier of array
	 * 
	 */
	public void initAccessories() {
		System.out.println(itemsPopularity.size());
		for(Entry<Long, Integer> entry : itemsPopularity.entrySet()) {
			itemsPopularityArray.add(new ItemPopularity(entry.getKey(), entry.getValue()));
		}
		
		Collections.sort(itemsPopularityArray, new Comparator<ItemPopularity>() {
			@Override
			public int compare(ItemPopularity o1, ItemPopularity o2) {
				if(o1.popularity != o2.popularity) return o1.popularity > o2.popularity ? -1 : 1;
				else return 0;
			}
		});
		
	}

	
	public void put(long UID, long itemID, double binary) {
		if(this.dataSet.containsKey(UID))
			dataSet.get(UID).put(itemID, binary);
		else {
			Map<Long, Double> binaryRatingMap = new HashMap<Long, Double>();
			binaryRatingMap.put(itemID, binary);
			dataSet.put(UID, binaryRatingMap);
		}
		this.userItemPairs.add(new UserItemPair(UID, itemID, binary));
		
		itemsPopularity.put(itemID, (itemsPopularity.containsKey(itemID) ? itemsPopularity.get(itemID)+1 : 1));
		
		this.totalLines++;
	}
	
	public void put(long UID, Map<Long, Double> ratings) {
		this.dataSet.put(UID, ratings);
	}
	
	@Override
	public Map<Long, Double> getRatingsByID(long UID) {
		return this.dataSet.get(UID);
	}
	
	public boolean contains(long UID, long itemID) {
		return this.dataSet.get(UID).containsKey(itemID);
	}
	
	@Override
	public List<UserItemPair> getAllRatingsList() {
		return this.userItemPairs;
	}
	
	@Override
	public List<ItemPopularity> getItemsPopularityArray() {
		return this.itemsPopularityArray;
	}

	@Override
	public Set<Entry<Long, Map<Long, Double>>> entrySet() {
		return this.dataSet.entrySet();
	}

	@Override
	public Set<Long> getAllUserIDs() {
		return this.dataSet.keySet();
	}

	@Override
	public void remove(long UID, long itemID) {
		this.dataSet.get(UID).remove(itemID);
	}

	@Override
	public Map<Long, Integer> getItemPopularity() {
		return this.itemsPopularity;
	}

	@Override
	public int getTotLines() {
		return this.totalLines;
	}
	
	
}
