package com.liangtee.mf.implicitfeedback.accessories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.vo.ItemPopularity;

public class NegativeInstanceSampling {

	static Logger log = LoggerFactory.getLogger(NegativeArraySizeException.class);
	
	public static Map<Long, Double> sampling(Map<Long, Double> ratings, List<ItemPopularity> popularity, int negProportion) {
		Map<Long, Double> samples = new HashMap<Long, Double>();
		for(Entry<Long, Double> entry : ratings.entrySet()) {
			samples.put(entry.getKey(), 1D);
		}
		int postiveQty = ratings.size();
		int cnt = 0;
		Random random = new Random(System.currentTimeMillis());
		for(int i=0; i<postiveQty*negProportion*3; i++) {
			long random_itemID = popularity.get(random.nextInt(postiveQty)).itemID;
			if(samples.containsKey(random_itemID)) continue;
			else {
				samples.put(random_itemID, 0D);
				cnt++;
			}
			if(cnt/negProportion > postiveQty) break;
		}
		
//		log.info("Finished to sample negative instances, positvie instances : " + postiveQty + ", " +
//		 "negative instances : " + cnt);
		
		return samples;
	}
	
}
