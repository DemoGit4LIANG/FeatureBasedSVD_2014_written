package com.liangtee.mf.datatool;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.impl.GenericDataModel;
import com.liangtee.mf.datamodel.vo.UserItemPair;

public class DataSetSplit {

	private DataModel dataSet = null;
	
	private int realTrainQty = 0;
	
	private int realTestQty = 0;
	
	private static Logger log = LoggerFactory.getLogger(DataSetSplit.class);
	
	public DataSetSplit(String filePath) {
		DataModel dataSet = new GenericDataModel(filePath);
	}
	
	public DataSetSplit(DataModel dataSet) {
		this.dataSet = dataSet;
	}
	
	public DataModel[] SplitDataSet(double ratio) {
		DataModel[] dataSets = new GenericDataModel[2];
		DataModel trainSet = new GenericDataModel();
		DataModel testSet = new GenericDataModel();
		int trainQty = -1;
		int cnt = -1;
		for(Entry<Long, Map<Long, Double>> entry : dataSet.entrySet()) {
			trainQty = (int)Math.round(entry.getValue().size() * ratio); 
			cnt = 0;
			for(Entry<Long, Double> entry2 : entry.getValue().entrySet()) {
				if(cnt < trainQty) {trainSet.put(entry.getKey(), entry2.getKey(), entry2.getValue()); this.realTrainQty++;}
				else {testSet.put(entry.getKey(), entry2.getKey(), entry2.getValue()); this.realTestQty++;}
				cnt++;
			}
		}
		
		trainSet.initAccessories();
		testSet.initAccessories();

		check(trainSet, testSet);
		
		dataSets[0] = trainSet;
		dataSets[1] = testSet;
		
		log.info("Pre-setting ratio: " + ratio);
		log.info("Real ratio(test/train): " + (1D-1.0D*realTestQty/realTrainQty));
		
		return dataSets;
	}
	
	private void check(DataModel trainSet, DataModel testSet) {
		
		Set<Long> UIDs = testSet.getAllUserIDs();
		Set<UserItemPair> deletedRatings = new HashSet<UserItemPair>();
		for(long UID : UIDs) {
			Map<Long, Double> ratings = testSet.getRatingsByID(UID);
			Set<Long> itemIDs = ratings.keySet();
			for(long itemID : itemIDs) {
				if(!trainSet.getItemPopularity().containsKey(itemID)) {
					trainSet.put(UID, itemID, ratings.get(itemID));
					this.realTrainQty++;
					deletedRatings.add(new UserItemPair(UID, itemID, 1));
				}
			}
		}
		for(UserItemPair uip : deletedRatings) {
			testSet.remove(uip.UID, uip.itemID);
			this.realTestQty--;
		}
		deletedRatings = null;
		
	}
	
}
