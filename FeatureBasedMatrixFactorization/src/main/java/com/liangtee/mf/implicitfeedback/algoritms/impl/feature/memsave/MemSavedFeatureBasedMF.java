package com.liangtee.mf.implicitfeedback.algoritms.impl.feature.memsave;

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
import com.liangtee.mf.datamodel.ExtDataModel;
import com.liangtee.mf.datamodel.impl.BinaryDataModel;
import com.liangtee.mf.datamodel.vo.AdInfo;
import com.liangtee.mf.datamodel.vo.ClickInfo;
import com.liangtee.mf.datamodel.vo.ItemPopularity;
import com.liangtee.mf.datamodel.vo.UserInfo;
import com.liangtee.mf.datamodel.vo.UserItemPair;
import com.liangtee.mf.evaluator.RMSEvaluator;
import com.liangtee.mf.implicitfeedback.MFRecommender;
import com.liangtee.mf.implicitfeedback.accessories.NegativeInstanceSampling;

/**
 * 
 * Feature based Matrix Factorizer
 * 
 * @author liangtee
 *
 */

public class MemSavedFeatureBasedMF {

	public ExtDataModel dataSet = null;
	
	private int featureQty = 0;
	
	private int iteration = 0;
	
	private float learningRate = 0F;
	
	private float overfitting = 0F;
	
	private float adjust = 1F;
	
	private float globalAvgCTR = 0F;
	
	private Map<Long, float[]> userFeatureVecs = null;
	
	private Map<Long, float[]> itemFeatureVecs = null;
	
	private float[][] genderFeatureVecs = null;
	
	private Map<Integer, float[]> ageFeatureVecs = null;
	
	private Map<Integer, float[]> advFeatureVecs = null;
	
	private Map<Integer, float[]> picFeatureVecs = null;
	
	private float[] userAvgFeature = null;
	
	private float[] itemAvgFeature = null;
	
	private float[] picAvgFeature = null;
	
	private float[] advtAvgFeature = null;
	
	private Map<Long, Float> userBiAvgCTR = null;
	
	private Map<Long, Float> adBiAvgCTR = null;
	
	private float[] genderBias = null;	//v[genderValue-1]
	
	private float[] ageBias = null;
	
	private float userBiAvg = 0F;
	
	private float adBiAvg = 0F;
	
	private float picBiAvg = 0F;
	
	private float advtBiAvg = 0F;
	
	private Map<Integer, Float> advBias = null;
	
	private Map<Integer, Float> picBias = null;
	
	public int unExistUserQty = 0;
	public int unExistAdQty = 0;
	public int unExistAdvtQty = 0;
	
//	private Map<Long, Double> pseudoUserCTR = null;
	
//	private Map<Long, Double> pseudoAdCTR = null;
	
	private static Logger log = LoggerFactory.getLogger(MemSavedFeatureBasedMF.class);
	
	/**
	 * 
	 * @param dataSet
	 * @param featureQty: the number of feature
	 * @param iteration
	 * @param learnRate
	 * @param overfitting
	 */
	public MemSavedFeatureBasedMF(ExtDataModel dataSet, int featureQty, int iteration, float learningRate, 
			float overfitting, float adjust) {
		this.dataSet = dataSet;
		this.featureQty = featureQty;
		this.iteration = iteration;
		this.learningRate = learningRate;
		this.overfitting = overfitting;
		this.adjust = adjust;
		this.globalAvgCTR = 1F * dataSet.getPositiveQty()/dataSet.getTotalQty();
		log.info("Global Average CTR : " + this.globalAvgCTR);
		initUserBiAvgCTR();
		initAdBiAvgCTR();
		initAdvBias();
		initPicBias();
		this.ageBias = new float[120];
		this.genderBias = new float[2];
		
		this.userAvgFeature = new float[featureQty];
		this.itemAvgFeature = new float[featureQty];
		this.picAvgFeature = new float[featureQty];
		this.advtAvgFeature = new float[featureQty];
		
		initAllFeatureVecs();	//user and items
		initGenderFeatureVecs();
		initAdvFeatureVecs();
		initAgeFeatureVecs();
		initPicFeatureVecs();
		
		SGDTrain();
		
//		pseudoUserFeatureAndBias();
		userAvgFeature();
		itemAvgFeature();
		picAvgFeature();
		advtAvgFeature();
	}
	
	private void initPicFeatureVecs() {
		this.picFeatureVecs = new HashMap<Integer, float[]>();
		Set<Integer> picIDs = this.dataSet.getAllPicIDs();
		Random random = new Random();
		for(int picID : picIDs) {
			float[] picFeature = new float[featureQty];
			for(int k=0; k<featureQty; k++) {
				picFeature[k] = (float) (random.nextDouble()/Math.sqrt(featureQty));
			}
			this.picFeatureVecs.put(picID, picFeature);
		}
	}
	
	private void initAgeFeatureVecs() {
		this.ageFeatureVecs = new HashMap<Integer, float[]>();
		Set<Integer> ages = this.dataSet.getAgeMap().keySet();
		Random random = new Random();
		for(int age : ages) {
			float[] ageFeature = new float[featureQty];
			for(int k=0; k<ageFeature.length; k++) {
				ageFeature[k] = (float) (random.nextDouble()/Math.sqrt(featureQty));
			}
			this.ageFeatureVecs.put(age, ageFeature);
		}
	}
	
	private void initAdvFeatureVecs() {
		this.advFeatureVecs = new HashMap<Integer, float[]>();
		Set<Integer> advIDs = this.dataSet.getAdvIDs();
		Random random = new Random();
		for(int advID : advIDs) {
			float[] advFeature = new float[featureQty];
			for(int k=0; k<advFeature.length; k++) {
				advFeature[k] = (float) (random.nextDouble()/Math.sqrt(featureQty));
			}
			this.advFeatureVecs.put(advID, advFeature);
		}
	}
	
	private void initGenderFeatureVecs() {
		Random random = new Random();
		float[] gender0 = new float[featureQty];
		float[] gender1 = new float[featureQty];
		for(int k=0; k<featureQty; k++) {
			gender0[k] = (float) (random.nextDouble()/Math.sqrt(featureQty));
			gender1[k] = (float) (random.nextDouble()/Math.sqrt(featureQty));
		}
		this.genderFeatureVecs = new float[2][featureQty];
		this.genderFeatureVecs[0] = gender0;
		this.genderFeatureVecs[1] = gender1;
	}
	
	private void initPicBias() {
		this.picBias = new HashMap<Integer, Float>();
		Set<Integer> picIDs = this.dataSet.getAllPicIDs();
		for(int picID : picIDs) {
			picBias.put(picID, 0F);
		}
	}
	
	private void initAdvBias() {
		this.advBias = new HashMap<Integer, Float>();
		for(int advID : this.dataSet.getAdvIDs()) {
			this.advBias.put(advID, 0F);
		}
		
	}
	
	private void initUserBiAvgCTR() {
		this.userBiAvgCTR = new HashMap<Long, Float>();
		Set<Long> UIDs = this.dataSet.getAllUserIDs();
		for(long UID : UIDs) {
			this.userBiAvgCTR.put(UID, 0F);
		}
	}
	
	private void initAdBiAvgCTR() {
		this.adBiAvgCTR = new HashMap<Long, Float>();
		Set<Long> adIDs = this.dataSet.getAllAdIDs();
		for(long AID : adIDs) {
			this.adBiAvgCTR.put(AID, 0F);
		}
	}
	
	private void initAllFeatureVecs() {
		this.userFeatureVecs = new HashMap<Long, float[]>();
		this.itemFeatureVecs = new HashMap<Long, float[]>();
		Set<Long> UIDs = dataSet.getAllUserIDs();
		Random random = new Random();
		for(long UID : UIDs) {
			float[] userFeature = new float[featureQty];
			for(int k=0; k<featureQty; k++) {
				userFeature[k] = (float) (random.nextDouble()/Math.sqrt(featureQty));
			}
			userFeatureVecs.put(UID, userFeature);
		}
		random = new Random();
		Set<Long> adIDs = dataSet.getAllAdIDs();
		for(long AID : adIDs) {
			float[] itemFeature = new float[featureQty];
			for(int k=0; k<featureQty; k++) {
				itemFeature[k] = (float) (random.nextDouble()/Math.sqrt(featureQty));
			}
			itemFeatureVecs.put(AID, itemFeature);
		}
	}
	
	private void pseudoUserFeatureAndBias() {
		log.info("Start to compute user pseduo user feature ...");
		int passedUserQty = 0;
		for(Entry<Long, UserInfo> entry : this.dataSet.getAllUserInfo().entrySet()) {
			long UID = entry.getKey();
			if(this.userFeatureVecs.containsKey(UID)) continue;
			else {
				int gender = entry.getValue().gender;
				int age = entry.getValue().age;
				float[] pseudoUserFeature = new float[featureQty];
				float pseudoUserBias = 0F;
				List<UserInfo> sim_users = this.dataSet.getGender_Age_User_Mapping().get(gender).get(age);
				if(sim_users.size() == 0) {
					passedUserQty++;
					continue;
				}
				int cnt = 0;
				for(int i=0; i<sim_users.size(); i++) {
					if(!userFeatureVecs.containsKey(sim_users.get(i).UID)) continue;
					float[] sim_userFeature = userFeatureVecs.get(sim_users.get(i).UID);
					pseudoUserBias += userBiAvgCTR.get(sim_users.get(i).UID);
					for(int k=0; k<featureQty; k++) {
						pseudoUserFeature[k] += sim_userFeature[k];	
					}
					cnt++;
				}
				for(int k=0; k<featureQty; k++) {
					pseudoUserFeature[k] = pseudoUserFeature[k]/cnt;
				}
				pseudoUserBias = pseudoUserBias/cnt;
				
				this.userFeatureVecs.put(UID, pseudoUserFeature);
				this.userBiAvgCTR.put(UID, pseudoUserBias);
				passedUserQty++;
			}
			if(passedUserQty >0 && passedUserQty%500000 == 0) 
				log.info("Processed " + passedUserQty + " users...");
		}
		log.info("Finish to compute user pseduo user feature ...");
	}
	
	private void userAvgFeature() {
		log.info("Start to compute user average feature ...");
		int cnt = 0;
		float ubias = 0F;
		for(Entry<Long, float[]> entry : this.userFeatureVecs.entrySet()) {
			float[] feature = entry.getValue();
			ubias += this.userBiAvgCTR.get(entry.getKey());
			for(int k=0; k<featureQty; k++) {
				this.userAvgFeature[k] += feature[k];
			}
			cnt++;
		}
		for(int k=0; k<featureQty; k++) {
			this.userAvgFeature[k] = this.userAvgFeature[k]/cnt;
		}
		this.userBiAvg = ubias/cnt;
	}
	
	private void itemAvgFeature() {
		log.info("Start to compute ad average feature ...");
		int cnt = 0;
		float ibias = 0F;
		for(Entry<Long, float[]> entry : this.itemFeatureVecs.entrySet()) {
			float[] feature = entry.getValue();
			ibias += this.adBiAvgCTR.get(entry.getKey());
			for(int k=0; k<featureQty; k++) {
				this.itemAvgFeature[k] += feature[k];
			}
			cnt++;
		}
		for(int k=0; k<featureQty; k++) {
			this.itemAvgFeature[k]  = this.itemAvgFeature[k]/cnt;
		}
		this.adBiAvg = ibias/cnt;
	}
	
	private void picAvgFeature() {
		log.info("Start to compute pic average feature ...");
		int cnt = 0;
		float pbias = 0F;
		for(Entry<Integer, float[]> entry : this.picFeatureVecs.entrySet()) {
			float[] feature = entry.getValue();
			pbias += this.picBias.get(entry.getKey());
			for(int k=0; k<featureQty; k++) {
				this.picAvgFeature[k] += feature[k];
			}
			cnt++;
		}
		for(int k=0; k<featureQty; k++) {
			this.picAvgFeature[k]  = this.picAvgFeature[k]/cnt;
		}
		this.picBiAvg = pbias/cnt;
	}
	
	private void advtAvgFeature() {
		int cnt = 0;
		float advtbias = 0F;
		for(Entry<Integer, float[]> entry : this.advFeatureVecs.entrySet()) {
			float[] feature = entry.getValue();
			advtbias += this.advBias.get(entry.getKey());
			for(int k=0; k<featureQty; k++) {
				this.advtAvgFeature[k] += feature[k];
			}
			cnt++;
		}
		for(int k=0; k<featureQty; k++) {
			this.advtAvgFeature[k]  = this.advtAvgFeature[k]/cnt;
		}
		this.advtBiAvg = advtbias/cnt;
	}
	
	private long adIDMapping(String title, int picID) {
		long sameAdID = -1;
		Set<Long> sameAdID1 = this.dataSet.getSameADsByTitle().get(title);
		Set<Long> sameAdID2 = this.dataSet.getSameADsByPic().get(picID);
		
		for(long adID : sameAdID1) {
			if(this.itemFeatureVecs.containsKey(adID)) {
				sameAdID = adID;
				break;
			}
		}
		if(sameAdID == -1 && sameAdID2 != null) {
			for(long adID : sameAdID2) {
				if(this.itemFeatureVecs.containsKey(adID)) {
					sameAdID = adID;
					break;
				}
			}
		}
		
		return sameAdID;
	}
	
	private int picIDMapping(long adID) {
		Set<Integer> same_picIDs = this.dataSet.getSamePicByAD().get(adID);
		int samePicID = -1;
		for(int pid : same_picIDs) {
			if(this.picFeatureVecs.containsKey(pid)) {
				samePicID = pid;
				break;
			}
		}
		
		return samePicID;
	}
	
	private void SGDTrain() {
		Set<Long> UIDs = this.dataSet.getAllUserIDs();
		Map<Long, UserInfo> userInfoMap = this.dataSet.getAllUserInfo();
		
		try {
			for(int iter=0; iter<iteration; iter++) {
				log.info("Iteration " + iter + " is running ... ");
				for(long UID : UIDs) {
					Map<Long, ClickInfo> clickRecords = this.dataSet.getClicksByID(UID);
					for(Entry<Long, ClickInfo> entry : clickRecords.entrySet()) {
							
						float userFeature[] = userFeatureVecs.get(UID);
						float itemFeature[] = itemFeatureVecs.get(entry.getKey());
						
						UserInfo userInfo = userInfoMap.get(UID);
						float genderFeature[] = genderFeatureVecs[userInfo.gender-1];
						float ageFeature[] = ageFeatureVecs.get(userInfo.age);
						float advFeature[] = advFeatureVecs.get(entry.getValue().advID);
						float picFeature[] = picFeatureVecs.get(entry.getValue().picID);
						
						float ubi = userBiAvgCTR.get(UID);
						float abi = adBiAvgCTR.get(entry.getKey());
						
						float advBi = advBias.get(entry.getValue().advID);
						float genderBi = genderBias[userInfo.gender-1];
						float ageBi = ageBias[userInfo.age];

						float picBi = picBias.get(entry.getValue().picID);
						
						float eu = ((float) entry.getValue().clickValue) - predict(userFeature, itemFeature, genderFeature, ageFeature, advFeature, 
								picFeature, globalAvgCTR, ubi, abi, genderBi, ageBi, advBi, picBi);
						
						ubi += learningRate*(eu - overfitting*ubi);
						abi += learningRate*(eu - overfitting*abi);
						userBiAvgCTR.put(UID, ubi);
						adBiAvgCTR.put(entry.getKey(), abi);
						
						genderBi += learningRate*(eu - overfitting*genderBi);
						genderBias[userInfo.gender-1] = genderBi;
						advBi += learningRate*(eu - overfitting*advBi);
						advBias.put(entry.getValue().advID, advBi);
						ageBi += learningRate*(eu - overfitting*ageBi);
						ageBias[userInfo.age] = ageBi;
						
						picBi += learningRate*(eu - overfitting*picBi);
						picBias.put(entry.getValue().picID, picBi);
						
						for(int k=0; k<featureQty; k++) {
							userFeature[k] += learningRate*(eu*(itemFeature[k] + advFeature[k] + picFeature[k]) - overfitting*userFeature[k]);
							itemFeature[k] += learningRate*(eu*(userFeature[k] + genderFeature[k] + ageFeature[k]) - overfitting*itemFeature[k]);
							
							genderFeature[k] += learningRate*(eu*(itemFeature[k] + advFeature[k] + picFeature[k]) - overfitting*genderFeature[k]);
							ageFeature[k] += learningRate*(eu*(itemFeature[k] + advFeature[k] + picFeature[k]) - overfitting*ageFeature[k]);
							
							advFeature[k] += learningRate*(eu*(userFeature[k] + genderFeature[k] + ageFeature[k]) - overfitting*advFeature[k]);
							picFeature[k] += learningRate*(eu*(userFeature[k] + genderFeature[k] + ageFeature[k]) - overfitting*picFeature[k]);
						}
						
						userFeatureVecs.put(UID, userFeature);
						itemFeatureVecs.put(entry.getKey(), itemFeature);
						
						genderFeatureVecs[userInfo.gender-1] = genderFeature;
						ageFeatureVecs.put(userInfo.age, ageFeature);
						advFeatureVecs.put(entry.getValue().advID, advFeature);
						picFeatureVecs.put(entry.getValue().picID, picFeature);
					}
					
				}
				RMSEvaluator rmse = new RMSEvaluator(this.dataSet, this);
				learningRate *= adjust;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private float predict(float[] userFeature, float[] itemFeature, float[] genderFeature, float[] ageFeature, float[] advFeature, float[] picFeature, float globalAvg, 
			float userBi, float adBi, float genderBi, float ageBi, float advBi, float picBi) {
		double prediction = globalAvg + userBi + adBi + genderBi + ageBi + advBi + picBi;
		for(int i=0; i<featureQty; i++) {
			prediction += (itemFeature[i] + advFeature[i] + picFeature[i])*(genderFeature[i] + ageFeature[i] + userFeature[i]);
		}
		
		return (float) (1.0/(1.0 + Math.pow(Math.E, -1.0*prediction)));
	}
	
	public ExtDataModel getTrainSet() {
		return this.dataSet;
	}

	public Map<Long, float[]> getUserFactorVectors() {
		return this.userFeatureVecs;
	}

	public Map<Long, float[]> getItemFactorVectors() {
		return this.itemFeatureVecs;
	}

	public float[] getUserFactorVectorByID(long UID) {
		return this.userFeatureVecs.get(UID);
	}

	public float[] getItemFactorVectorByID(long itemID) {
		return this.itemFeatureVecs.get(itemID);
	}

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

	public Map<Long, List<UserItemPair>> getRecommendations() {
		// TODO Auto-generated method stub
		return null;
	}

	public float estimatePreference(long UID, long itemID, int advtID, int picID) throws Exception {
		
		boolean UEtag = true;
		
		if(!this.userFeatureVecs.containsKey(UID) || !this.itemFeatureVecs.containsKey(itemID) || !this.advFeatureVecs.containsKey(advtID) || !this.picFeatureVecs.containsKey(picID)) {
			if(!this.userFeatureVecs.containsKey(UID)) {
				UEtag = false;
				unExistUserQty++;
			}
			
			if(!this.picBias.containsKey(picID)) {
				picID = picIDMapping(itemID);
			}
			
			if(!this.itemFeatureVecs.containsKey(itemID)) {
				unExistAdQty++;
				String title = this.dataSet.getAllAdInfo().get(itemID).title;
				itemID = adIDMapping(title, picID);
			}
			if(!this.advFeatureVecs.containsKey(advtID)) {
				unExistAdvtQty++;
				advtID = -1;
			}
		}
			
		UserInfo userInfo = this.dataSet.getAllUserInfo().get(UID);
		
		float preference = 0F;
		
		float[] userFeature = null;
		if(UEtag) userFeature = this.userFeatureVecs.get(UID);
		else userFeature = this.userAvgFeature;
		
		float[] itemFeature = null;
		if(itemID != -1) itemFeature = this.itemFeatureVecs.get(itemID);
		else itemFeature = this.itemAvgFeature;
		
		float[] genderFeature = this.genderFeatureVecs[userInfo.gender-1];
		
		float[] ageFeature = this.ageFeatureVecs.get(userInfo.age);
		
		float[] advFeature = null;
		if(advtID != -1) advFeature = this.advFeatureVecs.get(advtID);
		else advFeature = this.advtAvgFeature;
		
		float[] picFeature = null;
		if(picID != -1) picFeature = this.picFeatureVecs.get(picID);
		else picFeature = this.picAvgFeature;
		
		
		float ubi = 0F;
		if(UEtag) ubi = this.userBiAvgCTR.get(UID);
		else ubi = this.userBiAvg;
		
		float abi = 0F;
		if(itemID != -1) abi = this.adBiAvgCTR.get(itemID);
		else abi = this.adBiAvg;

		float genderBi = this.genderBias[userInfo.gender-1];
		double ageBi = this.ageBias[userInfo.age];
		
		float advBi = 0F;
		if(advtID != -1) advBi = this.advBias.get(advtID);
		else advBi = this.advtBiAvg;
		
		float picBi = 0F;
		if(picID != -1) picBi = this.picBias.get(picID);
		else picBi = this.picBiAvg;
		
		for(int k=0; k<featureQty; k++) {
			preference += (itemFeature[k] + advFeature[k] + picFeature[k])*(genderFeature[k] + ageFeature[k] + userFeature[k]);
		}
		
		preference += globalAvgCTR + ubi + abi + genderBi + ageBi + advBi + picBi;
		
		return (float) (1.0/(1.0 + Math.pow(Math.E, -1.0*preference)));
	}
	
}
