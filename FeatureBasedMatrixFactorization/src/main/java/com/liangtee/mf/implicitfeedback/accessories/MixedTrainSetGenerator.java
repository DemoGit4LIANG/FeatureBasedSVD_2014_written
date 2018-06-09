package com.liangtee.mf.implicitfeedback.accessories;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.impl.BinaryDataModel;
import com.liangtee.mf.datamodel.vo.ItemPopularity;

public class MixedTrainSetGenerator {

	private DataModel originalDataModel = null;
	
	private BinaryDataModel mixedTrainSet = null;
	
	private static Logger log = LoggerFactory.getLogger(MixedTrainSetGenerator.class);
	
	public MixedTrainSetGenerator(DataModel dataModel) {
		this.originalDataModel = dataModel;
		System.out.println(originalDataModel.getTotLines());
		this.mixedTrainSet = new BinaryDataModel();
		log.info("Starting to mix positive and negative instances ... ");
		List<ItemPopularity> itemsPopularityArray = originalDataModel.getItemsPopularityArray();
		for(Entry<Long, Map<Long, Double>> entry : originalDataModel.entrySet()) {
			long UID = entry.getKey();
			Map<Long, Double> ratingMap = entry.getValue();
			for(Entry<Long, Double> entry2 : ratingMap.entrySet()) {
				mixedTrainSet.put(UID, entry2.getKey(), 1D);
			}
			negativeInstanceSampling(UID, mixedTrainSet, itemsPopularityArray);
		}
		mixedTrainSet.initAccessories();
		
		log.info("Finishing to mix posivtive and negavie instances ... ");
	}
	
	private void check(Map<Long, Integer> or, Map<Long, Integer> ne) {
		System.out.println("###launching checking...");
		Set<Long> diff = new HashSet<Long>();
		for(Entry<Long, Integer> entry : ne.entrySet()) {
			if(!or.containsKey(entry.getKey())) {
				diff.add(entry.getKey());
			}
		}
		for(long itemID : diff) {
			System.out.println("ItemID : " + itemID + ", " + ne.get(itemID));
		}
	}
	
	private void negativeInstanceSampling(long UID, BinaryDataModel mixedTrainSet, List<ItemPopularity> itemsPopularityArray) {
		int postiveQty = mixedTrainSet.getRatingsByID(UID).size();
		Random random = new Random();
		int selection = -1;
		int cnt = 0;
		for(int i=0; i<postiveQty*3; i++) {
			selection = random.nextInt(postiveQty);
			if(mixedTrainSet.contains(UID, itemsPopularityArray.get(selection).itemID)) continue;
			else {
				mixedTrainSet.put(UID, itemsPopularityArray.get(selection).itemID, 0D);
				cnt++;
			}
			if(cnt >= postiveQty) break;
		}
		
	}
	
	public BinaryDataModel getMixedTrainSet() {
		return this.mixedTrainSet;
	}
	
}
