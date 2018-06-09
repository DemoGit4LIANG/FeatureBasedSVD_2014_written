package com.liangtee.mf.datamodel.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.vo.ItemPopularity;
import com.liangtee.mf.datamodel.vo.UserItemPair;

public class GenericDataModel implements DataModel {
	
	private Map<Long, Map<Long, Double>> genericDataModel = null;
	
	private List<UserItemPair> userItemPairs = null;
	
	private Map<Long, Integer> itemsPopularity = null;
	
	private List<ItemPopularity> itemsPopularityArray = null;
	
	private int totalLines = 0;
	
	private static Logger log = LoggerFactory.getLogger(GenericDataModel.class);
	
	public GenericDataModel() {
		this.genericDataModel = new HashMap<Long, Map<Long, Double>>();
		this.userItemPairs = new ArrayList<UserItemPair>();
		this.itemsPopularity = new HashMap<Long, Integer>();
		this.itemsPopularityArray = new ArrayList<ItemPopularity>();
	}
	
	
	public GenericDataModel(String filePath) {
		log.info("Preparing to load data file : " + filePath + "...");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(filePath));
			this.genericDataModel = new HashMap<Long, Map<Long,Double>>();
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
				double rating = Long.parseLong(det[2]);
				
				itemsPopularity.put(IID, itemsPopularity.containsKey(IID) ? itemsPopularity.get(IID)+1 : 1);

				if(genericDataModel.containsKey(UID)) 
					genericDataModel.get(UID).put(IID, rating);
				else {
					Map<Long, Double> ratingMap = new HashMap<Long, Double>();
					ratingMap.put(IID, rating);
					genericDataModel.put(UID, ratingMap);
				}
				userItemPairs.add(new UserItemPair(UID, IID, rating));
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

	public void put(long UID, long itemID, double rating) {
		if(this.genericDataModel.containsKey(UID))
			genericDataModel.get(UID).put(itemID, rating);
		else {
			Map<Long, Double> ratingMap = new HashMap<Long, Double>();
			ratingMap.put(itemID, rating);
			genericDataModel.put(UID, ratingMap);
		}
		this.userItemPairs.add(new UserItemPair(UID, itemID, rating));
		
		itemsPopularity.put(itemID, (itemsPopularity.containsKey(itemID) ? itemsPopularity.get(itemID)+1 : 1));
		
		this.totalLines++;
	}
	
	@Override
	public List<ItemPopularity> getItemsPopularityArray() {
		return itemsPopularityArray;
	}

	@Override
	public Set<Entry<Long, Map<Long, Double>>> entrySet() {
		return this.genericDataModel.entrySet();
	}

	@Override
	public List<UserItemPair> getAllRatingsList() {
		return this.userItemPairs;
	}

	@Override
	public Set<Long> getAllUserIDs() {
		return this.genericDataModel.keySet();
	}

	@Override
	public Map<Long, Double> getRatingsByID(long UID) {
		return this.genericDataModel.get(UID);
	}


	@Override
	public void remove(long UID, long itemID) {
		this.genericDataModel.get(UID).remove(itemID);
	}


	@Override
	public Map<Long, Integer> getItemPopularity() {
		return this.itemsPopularity;
	}


	@Override
	public int getTotLines() {
		return this.totalLines;
	}


	@Override
	public boolean contains(long UID, long itemID) {
		return genericDataModel.get(UID).containsKey(itemID);
	}
	
	
}
