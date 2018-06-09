package com.liangtee.mf.implicitfeedback.algoritms.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


/**
 *
 * One Class Matrix Factorizer
 * Aiming to learn the feature matrix from binary matrix
 *
 * @author liangtee
 *
 */
public class GenericOneClassMatrixFactorizer implements MFRecommender {
	
	private BinaryDataModel mixedTrainSet = null;
	
	private Map<Long, double[]> userFactorVectors = null;
	
	private Map<Long, double[]> itemFactorVectors = null;
	
	private Map<Long, List<UserItemPair>> recommendations = null;
	
	private int factorQty = -1;
	
	private static Logger log = LoggerFactory.getLogger(GenericOneClassMatrixFactorizer.class);
	
	/**
	 * 
	 * @param mixedTrainSet : containing positive and negative instances for each user
	 * @param iteration
	 * @param alpha : alpha represents the learning rate
	 * @param lambda : lambda adjusts the regularization
	 */
	public GenericOneClassMatrixFactorizer(BinaryDataModel mixedTrainSet, int factorQty, int iteration, double alpha, double lambda) {
		this.mixedTrainSet = mixedTrainSet;
		this.userFactorVectors = new HashMap<Long, double[]>();
		this.itemFactorVectors = new HashMap<Long, double[]>();
		this.factorQty = factorQty;
		initUserFactorVector(userFactorVectors, factorQty);
		initItemFactorVector(itemFactorVectors, factorQty);
		SGDtrain(mixedTrainSet, factorQty, iteration, alpha, lambda);
		this.recommendations = new HashMap<Long, List<UserItemPair>>();
	}
	
	
	/**
	 * Initializing the factor vector by random values
	 * The random values are achieved by the equation: 
	 * 	factor_ij = 0.1 * random[0-1]/sqrt(factor_qty)
	 * 
	 * @param userFactorVectors
	 * @param factorQty
	 */
	private void initUserFactorVector(Map<Long, double[]> userFactorVectors, int factorQty) {
		Random random = new Random();
		Set<Long> UIDs = this.mixedTrainSet.getAllUserIDs();
		for(long UID : UIDs) {
			double[] userFactorVector = new double[factorQty];
			for(int i=0; i<factorQty; i++) {
//				userFactorVector[i] = 0.1D*random.nextDouble()/Math.sqrt(factorQty);
				userFactorVector[i] = 1.0/factorQty;
			}
			userFactorVectors.put(UID, userFactorVector);
		}
	}
	
	private void initItemFactorVector(Map<Long, double[]> itemFactorVectors, int factorQty) {
		Random random = new Random();
		List<ItemPopularity> items = this.mixedTrainSet.getItemsPopularityArray();
		for(int i=0; i<items.size(); i++) {
			double[] itemFactorVector = new double[factorQty];
			for(int k=0; k<factorQty; k++) {
//				itemFactorVector[k] = 0.1D*random.nextDouble()/Math.sqrt(factorQty);
				itemFactorVector[k] = 1.0/factorQty;
			}
			itemFactorVectors.put(items.get(i).itemID, itemFactorVector);
		}
		
	}
	
	/**
	 * Learning the factor vectors via Stochastic Gradient Descent approach
	 */
	private void SGDtrain(BinaryDataModel mixedTrainSet, int factorQty, int iteration, double alpha, double lambda) {
		log.info("Starting to learn the facor matrix via stochastic gradient descent ... ");
		for(int iter=0; iter<iteration; iter++) {
			log.info("Iteration " + iter + " started ... ");
			for(int idx=0; idx<mixedTrainSet.getAllRatingsList().size(); idx++) {
				try {
					UserItemPair pair = mixedTrainSet.getAllRatingsList().get(idx);
					double err = pair.rating - estimatePreference(pair.UID, pair.itemID);
					double[] userFactorVector = userFactorVectors.get(pair.UID);
					double[] itemFactorVector = itemFactorVectors.get(pair.itemID);
					for(int i=0; i<factorQty; i++) {
						userFactorVector[i] += alpha*(err*itemFactorVector[i] - lambda*userFactorVector[i]);
						itemFactorVector[i] += alpha*(err*userFactorVector[i] - lambda*itemFactorVector[i]);
					}
					
//					alpha  = alpha * 0.95D;
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			log.info("iteration " + iter + " finished");
			RMSEvaluator rmse = new RMSEvaluator(mixedTrainSet, this);
		}
	}
	
	
	public double estimatePreference(long UID, long itemID) throws Exception {
		double prediction = 0D;
		double[] userFactorVector = this.userFactorVectors.get(UID);
		double[] itemFactorVector = this.itemFactorVectors.get(itemID);
		for(int i=0; i<factorQty; i++) {
			prediction += userFactorVector[i]*itemFactorVector[i];
		}
		return prediction;
	}

	@Override
	public List<UserItemPair> recommend4UserByID(long UID, int N) {
		if(this.recommendations.containsKey(UID)) return this.recommendations.get(UID).subList(0, N-1);
		List<ItemPopularity> allItems = mixedTrainSet.getItemsPopularityArray();
		List<UserItemPair> recommendList = new ArrayList<UserItemPair>();
		double prediction = 0D;
		try {
			for(int idx=0; idx<allItems.size(); idx++) {
				long itemID = allItems.get(idx).itemID;
				 if(!mixedTrainSet.contains(UID, itemID) || (mixedTrainSet.contains(UID, itemID) && mixedTrainSet.getRatingsByID(UID).get(itemID) != 0D)) {
					 prediction = estimatePreference(UID, itemID);
					 recommendList.add(new UserItemPair(UID, itemID, prediction));
				 }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		Collections.sort(recommendList, new Comparator<UserItemPair>() {
			@Override
			public int compare(UserItemPair o1, UserItemPair o2) {
				if(o1.rating != o2.rating) return o1.rating > o2.rating ? -1 : 1;
				else return 0;
			}
		});
		
		this.recommendations.put(UID, recommendList);
		
		return recommendList.subList(0, N-1);
	}

	@Override
	public Map<Long, List<UserItemPair>> getRecommendations() {
		return this.recommendations;
	}

	@Override
	public Map<Long, double[]> getUserFactorVectors() {
		return this.userFactorVectors;
	}

	@Override
	public Map<Long, double[]> getItemFactorVectors() {
		return this.itemFactorVectors;
	}

	@Override
	public DataModel getTrainSet() {
		return this.mixedTrainSet;
	}

	@Override
	public double[] getUserFactorVectorByID(long UID) {
		return this.userFactorVectors.get(UID);
	}

	@Override
	public double[] getItemFactorVectorByID(long itemID) {
		return this.itemFactorVectors.get(itemID);
	}


	@Override
	public double similarity(long UID, long itemID) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
}
