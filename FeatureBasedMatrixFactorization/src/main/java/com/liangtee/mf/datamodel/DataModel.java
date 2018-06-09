package com.liangtee.mf.datamodel;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.liangtee.mf.datamodel.vo.ItemPopularity;
import com.liangtee.mf.datamodel.vo.UserItemPair;

/**
 * 
 * DataModel interface
 * 
 * @author liangtee
 * 2014.10.5
 *
 */
public interface DataModel {
	
	public void put(long UID, long itemID, double rating);
	
	public void remove(long UID, long itemID);

	public Set<Long> getAllUserIDs();
	
	public Map<Long, Double> getRatingsByID(long UID);
	
	public List<ItemPopularity> getItemsPopularityArray();
	
	public Map<Long, Integer> getItemPopularity();
	
	public Set<Entry<Long, Map<Long, Double>>> entrySet();
	
	public List<UserItemPair> getAllRatingsList();
	
	public void initAccessories();
	
	public int getTotLines();

	public boolean contains(long UID, long itemID);

}
