package com.liangtee.mf.evaluator;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.ExtDataModel;
import com.liangtee.mf.datamodel.vo.ClickInfo;
import com.liangtee.mf.datamodel.vo.UserItemPair;
import com.liangtee.mf.implicitfeedback.MFRecommender;
import com.liangtee.mf.implicitfeedback.algoritms.impl.feature.FeatureBasedMatrixFactorizer;
import com.liangtee.mf.implicitfeedback.algoritms.impl.feature.memsave.MemSavedFeatureBasedMF;

public class RMSEvaluator {

	private static Logger log = LoggerFactory.getLogger(RMSEvaluator.class);
	
	public RMSEvaluator(DataModel dataModel, MFRecommender recommender) {
		double err = 0D;
		try {
			for(int i=0; i<dataModel.getAllRatingsList().size(); i++) {
				UserItemPair rating = dataModel.getAllRatingsList().get(i);
				err += Math.pow((rating.rating - recommender.estimatePreference(rating.UID, rating.itemID)), 2D);
			}
			
			log.info("RMSE : " + Math.sqrt(err/dataModel.getAllRatingsList().size()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public RMSEvaluator(ExtDataModel dataModel, MFRecommender recommender) {
		double err = 0D;
		try {
			for(Entry<Long, Map<Long, ClickInfo>> entry : dataModel.entrySet()) {
				for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
					err += Math.pow(entry2.getValue().clickValue - recommender.estimatePreference(entry.getKey(), entry2.getKey()), 2D);
				}
			}
			
			log.info("RMSE : " + Math.sqrt(err/dataModel.getTotalQty()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public RMSEvaluator(ExtDataModel dataModel, FeatureBasedMatrixFactorizer recommender) {
		double err = 0D;
		try {
			for(Entry<Long, Map<Long, ClickInfo>> entry : dataModel.entrySet()) {
				for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
					err += Math.pow(entry2.getValue().clickValue - recommender.estimatePreference(entry.getKey(), entry2.getKey(), entry2.getValue().advID, entry2.getValue().picID), 2D);
				}
			}
			
			log.info("RMSE : " + Math.sqrt(err/dataModel.getTotalQty()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public RMSEvaluator(ExtDataModel dataModel, Evo_FeatureBasedMatrixFactorizer recommender) {
		double err = 0D;
		try {
			for(Entry<Long, Map<Long, ClickInfo>> entry : dataModel.entrySet()) {
				for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
					err += Math.pow(entry2.getValue().clickValue - recommender.estimatePreference(entry.getKey(), entry2.getKey(), entry2.getValue().advID), 2D);
				}
			}
			
			log.info("RMSE : " + Math.sqrt(err/dataModel.getTotalQty()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public RMSEvaluator(ExtDataModel dataModel, MemSavedFeatureBasedMF recommender) {
		double err = 0D;
		try {
			for(Entry<Long, Map<Long, ClickInfo>> entry : dataModel.entrySet()) {
				for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
					err += Math.pow(entry2.getValue().clickValue - recommender.estimatePreference(entry.getKey(), entry2.getKey(), entry2.getValue().advID, entry2.getValue().picID), 2D);
				}
			}
			
			log.info("RMSE : " + Math.sqrt(err/dataModel.getTotalQty()));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
