package com.liangtee.mf.implicitfeedback.algoritms.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.ExtDataModel;
import com.liangtee.mf.datamodel.vo.ClickInfo;
import com.liangtee.mf.datamodel.vo.UserItemPair;
import com.liangtee.mf.evaluator.RMSEvaluator;
import com.liangtee.mf.implicitfeedback.MFRecommender;


/**
 *
 * Linear Regression Implementation
 *
 * @author liangtee
 *
 */
public class LinearRegressionMF implements MFRecommender {

	private ExtDataModel dataSet = null;
	
	private Map<Long, double[]> userFeatureVecs = null;
	
	private Map<Long, double[]> itemFeatureVecs = null;
	
	private double[] genderFeatureVecs = null;
	
	private Map<Integer, double[]> ageFeatureVecs = null;
	
	private int featureQty = 0;
	
	private int iteration = 0;
	
	private double learningRate = 0D;
	
	private double overfitting = 0D;
	
	private double globalAvgCTR = 0D;
	
	private Map<Long, Double> userBiAvgCTR = null;
	
	private Map<Long, Double> adBiAvgCTR = null;
	
	private double[] genderBias = null;	//v[0] = weight of male, v[1] = weight of female
	
	private Map<Long, Double> pseudoUserCTR = null;
	
	private Map<Long, Double> pseudoAdCTR = null;
	
	private static Logger log = LoggerFactory.getLogger(LinearRegressionMF.class);
	
	/**
	 * 
	 * @param dataSet
	 * @param featureQty: the number of feature
	 * @param iteration
	 * @param learningRate
	 * @param overfitting
	 */
	public LinearRegressionMF(ExtDataModel dataSet, int featureQty, int iteration, double learningRate, double overfitting) {
		this.dataSet = dataSet;
		this.featureQty = featureQty;
		this.iteration = iteration;
		this.learningRate = learningRate;
		this.overfitting = overfitting;
		this.globalAvgCTR = 1.0 * dataSet.getPositiveQty()/dataSet.getTotalQty();
		log.info("Global Average CTR : " + this.globalAvgCTR);
		initUserBiAvgCTR();
		initADBiAvgCTR();
		initAllFeatureVecs();
		SGDTrain();
	}
	
	private void initUserBiAvgCTR() {
		this.userBiAvgCTR = new HashMap<Long, Double>();
		Set<Long> UIDs = this.dataSet.getAllUserIDs();
		for(long UID : UIDs) {
			this.userBiAvgCTR.put(UID, 0D);
		}
	}
	
	private void initADBiAvgCTR() {
		this.adBiAvgCTR = new HashMap<Long, Double>();
		Set<Long> adIDs = this.dataSet.getAllAdIDs();
		for(long AID : adIDs) {
			this.adBiAvgCTR.put(AID, 0D);
		}
	}

	/**
	 * Using average value to fulfill some missing values in the data-set
	 */
	private void initAllFeatureVecs() {
		this.userFeatureVecs = new HashMap<Long, double[]>();
		this.itemFeatureVecs = new HashMap<Long, double[]>();
		Set<Long> UIDs = dataSet.getAllUserIDs();
		Random random = new Random();
		for(long UID : UIDs) {
			double[] userFeature = new double[featureQty];
			for(int k=0; k<featureQty; k++) {
				userFeature[k] = random.nextDouble()/Math.sqrt(featureQty);
			}
			userFeatureVecs.put(UID, userFeature);
		}
		random = new Random();
		Set<Long> adIDs = dataSet.getAllAdIDs();
		for(long AID : adIDs) {
			double[] itemFeature = new double[featureQty];
			for(int k=0; k<featureQty; k++) {
				itemFeature[k] = random.nextDouble()/Math.sqrt(featureQty);
			}
			itemFeatureVecs.put(AID, itemFeature);
		}
	}
	
	private void SGDTrain() {
		Set<Long> UIDs = this.dataSet.getAllUserIDs();
		
		try {
			for(int iter=0; iter<iteration; iter++) {
				log.info("Iteration " + iter + " is running ... ");
				for(long UID : UIDs) {
					Map<Long, ClickInfo> clickRecords = this.dataSet.getClicksByID(UID);
					for(Entry<Long, ClickInfo> entry : clickRecords.entrySet()) {
						double userFeature[] = userFeatureVecs.get(UID);
						double itemFeature[] = itemFeatureVecs.get(entry.getKey());
						double ubi = userBiAvgCTR.get(UID);
						double abi = adBiAvgCTR.get(entry.getKey());
						
						double eu = entry.getValue().clickValue - predict(userFeature, itemFeature, globalAvgCTR, ubi, abi);
						
						ubi += learningRate*(eu - overfitting*ubi);
						abi += learningRate*(eu - overfitting*abi);
						userBiAvgCTR.put(UID, ubi);
						adBiAvgCTR.put(entry.getKey(), abi);
						
						for(int k=0; k<featureQty; k++) {
							userFeature[k] += learningRate*(eu*itemFeature[k] - overfitting*userFeature[k]);
							itemFeature[k] += learningRate*(eu*userFeature[k] - overfitting*itemFeature[k]);
						}
						userFeatureVecs.put(UID, userFeature);
						itemFeatureVecs.put(entry.getKey(), itemFeature);
					}
					
				}
				RMSEvaluator rmse = new RMSEvaluator(this.dataSet, this);
				learningRate *= 0.9;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private double predict(double[] userFeature, double[] itemFeature, double globalAvg, double userBi, double adBi) {
		double prediction = globalAvg + userBi + adBi;
		for(int i=0; i<featureQty; i++) {
			prediction += userFeature[i]*itemFeature[i];
		}
		
		return 1.0/(1.0 + Math.pow(Math.E, -1.0*prediction));
	}
	
	
	@Override
	public DataModel getTrainSet() {
//		return this.dataSet;
		return null;
	}

	@Override
	public Map<Long, double[]> getUserFactorVectors() {
		return this.userFeatureVecs;
	}

	@Override
	public Map<Long, double[]> getItemFactorVectors() {
		return this.itemFeatureVecs;
	}

	@Override
	public double[] getUserFactorVectorByID(long UID) {
		return this.userFeatureVecs.get(UID);
	}

	@Override
	public double[] getItemFactorVectorByID(long itemID) {
		return this.itemFeatureVecs.get(itemID);
	}

	@Override
	public List<UserItemPair> recommend4UserByID(long UID, int N) {
//		Set<Long> itemIDs = this.dataSet.getItemPopularity().keySet();
//		List<UserItemPair> recList = new ArrayList<UserItemPair>();
//		Map<Long, Double> ratings = this.dataSet.getRatingsByID(UID);
//		try {
//			for(long itemID : itemIDs) {
//				if(ratings.containsKey(itemID)) continue;
//				else {
//					double prediction = estimatePreference(UID, itemID);
////					double prediction = similarity(UID, itemID);
//					recList.add(new UserItemPair(UID, itemID, prediction));
//				}
//			}
//			Collections.sort(recList, new Comparator<UserItemPair>() {
//				@Override
//				public int compare(UserItemPair o1, UserItemPair o2) {
//					if(o1.rating != o2.rating) return o1.rating > o2.rating ? -1 : 1;
//					return 0;
//				}
//			});
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return recList.subList(0, N);
		return null;
	}

	@Override
	public Map<Long, List<UserItemPair>> getRecommendations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double estimatePreference(long UID, long itemID) throws Exception {
		if(!this.userFeatureVecs.containsKey(UID) || !this.itemFeatureVecs.containsKey(itemID)) {
			if(!this.userFeatureVecs.containsKey(UID)) log.info("UserID : " + UID + " does not exist in training set ...");
			if(!this.itemFeatureVecs.containsKey(itemID)) log.info("ItemID : " + itemID + " does not exist in training set ...");
			return globalAvgCTR;
		}
			
		double preference = 0D;
		double[] userFeature = this.userFeatureVecs.get(UID);
		double[] itemFeature = this.itemFeatureVecs.get(itemID);
		double ubi = this.userBiAvgCTR.get(UID);
		double abi = this.adBiAvgCTR.get(itemID);
		for(int k=0; k<featureQty; k++) {
			preference += userFeature[k]*itemFeature[k];
		}
		preference += ubi;
		preference += abi;
		preference += globalAvgCTR;
		
		return 1.0/(1.0 + Math.pow(Math.E, -1.0*preference));
	}

	@Override
	public double similarity(long UID, long itemID) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
