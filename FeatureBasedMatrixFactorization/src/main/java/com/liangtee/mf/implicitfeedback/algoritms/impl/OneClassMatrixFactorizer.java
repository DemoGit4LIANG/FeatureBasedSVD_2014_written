package com.liangtee.mf.implicitfeedback.algoritms.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import com.liangtee.mf.datamodel.vo.UserItemPair;
import com.liangtee.mf.evaluator.RMSEvaluator;
import com.liangtee.mf.implicitfeedback.MFRecommender;
import com.liangtee.mf.implicitfeedback.accessories.NegativeInstanceSampling;

/**
 * 
 * One Class Matrix Factorizer
 * Aiming to learn the feature matrix from binary matrix
 * 
 * @author liangtee
 *
 */

public class OneClassMatrixFactorizer implements MFRecommender {

	private DataModel dataSet = null;
	
	private Map<Long, double[]> userFeatureVecs = null;
	
	private Map<Long, double[]> itemFeatureVecs = null;
	
	private int featureQty = 0;
	
	private int iteration = 0;
	
	private double learningRate = 0D;
	
	private double overfitting = 0D;
	
	private int negPropotrion = 0;
	
	private static Logger log = LoggerFactory.getLogger(OneClassMatrixFactorizer.class);
	
	/**
	 * 
	 * @param dataSet
	 * @param featureQty: the number of feature
	 * @param iteration
	 * @param learningRate
	 * @param overfitting
	 */
	public OneClassMatrixFactorizer(DataModel dataSet, int featureQty, int iteration, double learningRate, double overfitting, int negPropotrion) {
		this.dataSet = dataSet;
		this.featureQty = featureQty;
		this.iteration = iteration;
		this.learningRate = learningRate;
		this.overfitting = overfitting;
		this.negPropotrion = negPropotrion;
		initAllFeatureVecs();
		SGDTrain();
	}
	
	private void initAllFeatureVecs() {
		this.userFeatureVecs = new HashMap<Long, double[]>();
		this.itemFeatureVecs = new HashMap<Long, double[]>();
		Set<Long> UIDs = dataSet.getAllUserIDs();
		Random random = new Random(System.currentTimeMillis());
		for(long UID : UIDs) {
			double[] userFeature = new double[featureQty];
			for(int k=0; k<featureQty; k++) {
				userFeature[k] = random.nextDouble()/Math.sqrt(featureQty);
			}
			userFeatureVecs.put(UID, userFeature);
		}
		Set<Long> itemIDs = dataSet.getItemPopularity().keySet();
		for(long itemID : itemIDs) {
			double[] itemFeature = new double[featureQty];
			for(int k=0; k<featureQty; k++) {
				itemFeature[k] = random.nextDouble()/Math.sqrt(featureQty);
			}
			itemFeatureVecs.put(itemID, itemFeature);
		}
	}
	
	private void SGDTrain() {
		Set<Long> UIDs = this.dataSet.getAllUserIDs();
		List<ItemPopularity> popularity = this.dataSet.getItemsPopularityArray();
		
//		BinaryDataModel mixedTrainDataSet = new BinaryDataModel();
//		for(long UID : UIDs) {
//			Map<Long, Double> ratings = dataSet.getRatingsByID(UID);
//			Map<Long, Double> samples = NegativeInstanceSampling.sampling(ratings, popularity, this.negPropotrion);
//			mixedTrainDataSet.put(UID, samples);
//		}
		
		try {
			for(int iter=0; iter<iteration; iter++) {
				log.info("Iteration " + iter + " is running ... ");
				for(long UID : UIDs) {
					Map<Long, Double> mixed = this.dataSet.getRatingsByID(UID);
//					Map<Long, Double> mixed = mixedTrainDataSet.getRatingsByID(UID);
//					Map<Long, Double> samples = NegativeInstanceSampling.sampling(ratings, popularity);
					for(Entry<Long, Double> entry : mixed.entrySet()) {
						double eu = entry.getValue() - estimatePreference(UID, entry.getKey());
						double userFeature[] = userFeatureVecs.get(UID);
						double itemFeature[] = itemFeatureVecs.get(entry.getKey());
						for(int k=0; k<featureQty; k++) {
							userFeature[k] += learningRate*(eu*itemFeature[k] - overfitting*userFeature[k]);
							itemFeature[k] += learningRate*(eu*userFeature[k] - overfitting*itemFeature[k]);
						}
						userFeatureVecs.put(UID, userFeature);
						itemFeatureVecs.put(entry.getKey(), itemFeature);
					}
//					samples = null;
					
				}
				RMSEvaluator rmse = new RMSEvaluator(this.dataSet, this);
				learningRate *= 0.9;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	@Override
	public DataModel getTrainSet() {
		return this.dataSet;
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
		Set<Long> itemIDs = this.dataSet.getItemPopularity().keySet();
		List<UserItemPair> recList = new ArrayList<UserItemPair>();
		Map<Long, Double> ratings = this.dataSet.getRatingsByID(UID);
		try {
			for(long itemID : itemIDs) {
				if(ratings.containsKey(itemID)) continue;
				else {
					double prediction = estimatePreference(UID, itemID);
//					double prediction = similarity(UID, itemID);
					recList.add(new UserItemPair(UID, itemID, prediction));
				}
			}
			Collections.sort(recList, new Comparator<UserItemPair>() {
				@Override
				public int compare(UserItemPair o1, UserItemPair o2) {
					if(o1.rating != o2.rating) return o1.rating > o2.rating ? -1 : 1;
					return 0;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return recList.subList(0, N);
	}

	@Override
	public Map<Long, List<UserItemPair>> getRecommendations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double estimatePreference(long UID, long itemID) throws Exception {
		if(!this.userFeatureVecs.containsKey(UID) || !this.itemFeatureVecs.containsKey(itemID)) {
//			throw new Exception("No this UserID or ItemID");
			if(!this.userFeatureVecs.containsKey(UID)) log.info("UserID : " + UID + " does not exist in training set ...");
			if(!this.itemFeatureVecs.containsKey(itemID)) log.info("ItemID : " + itemID + " does not exist in training set ...");
			return 0D;
		}
			
		double preference = 0D;
		double[] userFeature = this.userFeatureVecs.get(UID);
		double[] itemFeature = this.itemFeatureVecs.get(itemID);
		for(int k=0; k<featureQty; k++) {
			preference += userFeature[k]*itemFeature[k];
		}
		
		return 1.0/(1.0 + Math.pow(Math.E, -1.0*preference));
//		return preference;
	}

	public double similarity(long UID, long itemID) throws Exception {
//		if(!this.userFeatureVecs.containsKey(UID) || !this.itemFeatureVecs.containsKey(itemID))
//			throw new Exception("No this UserID or ItemID");
//		double[] userFeature = this.userFeatureVecs.get(UID);
//		double[] itemFeature = this.itemFeatureVecs.get(itemID);
//		double dist = 0D;
//		
//		for(int k=0; k<featureQty; k++) {
//			dist += Math.pow((userFeature[k] - itemFeature[k]), 2D);
//		}
//		
//		return 1/Math.sqrt(dist);
		return 0D;
	}
	
}
