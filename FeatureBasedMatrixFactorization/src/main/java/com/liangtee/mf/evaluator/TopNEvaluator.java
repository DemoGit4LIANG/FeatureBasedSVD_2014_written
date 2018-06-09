package com.liangtee.mf.evaluator;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.vo.UserItemPair;
import com.liangtee.mf.implicitfeedback.MFRecommender;

/**
 * TopN evaluator implement Precision, Recall, and F1-Measure three metrics 
 * 
 * 
 * @author liangtee
 *
 */
public class TopNEvaluator {

	private static Logger log = LoggerFactory.getLogger(TopNEvaluator.class);
	
	public TopNEvaluator(DataModel testSet, MFRecommender recommender, int N) {
		int hits = 0;
		int recall = 0;
		int precision = 0;
		Set<Long> UIDs = testSet.getAllUserIDs();
		for(long UID : UIDs) {
			List<UserItemPair> recList = recommender.recommend4UserByID(UID, N);
			for(int i=0; i<recList.size(); i++) {
				long itemID = recList.get(i).itemID;
				if(testSet.contains(UID, itemID)) hits++;
			}
			precision += N;
			recall += testSet.getRatingsByID(UID).size();
		}
		
		log.info("Finished to compute recall and precision ... ");
		System.out.println(hits);
		double r_recall = 1.0 * hits/recall;
		double r_precision = 1.0 * hits/precision;
		log.info("Recall = " + r_recall + ", Presion = " + r_precision);
		log.info("F1-measure: " + 2/(1/r_recall + 1/r_precision));
	}
	
}
