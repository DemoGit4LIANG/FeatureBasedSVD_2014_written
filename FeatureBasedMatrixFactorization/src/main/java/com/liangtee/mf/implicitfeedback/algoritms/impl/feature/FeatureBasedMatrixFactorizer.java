package com.liangtee.mf.implicitfeedback.algoritms.impl.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.ExtDataModel;
import com.liangtee.mf.datamodel.vo.ClickInfo;
import com.liangtee.mf.datamodel.vo.UserInfo;
import com.liangtee.mf.datamodel.vo.UserItemPair;
import com.liangtee.mf.evaluator.RMSEvaluator;

/**
 * 
 * Feature based Matrix Factorizer
 * 
 * @author liangtee
 *
 */

public class FeatureBasedMatrixFactorizer {

	public ExtDataModel dataSet = null;
	
	private int featureQty = 0;
	
	private int iteration = 0;
	
	private double learningRate = 0D;
	
	private double overfitting = 0D;
	
	private double adjust = 1D;
	
	private double globalAvgCTR = 0D;
	
	
	private Map<Long, double[]> userFeatureVecs = null;
	
	private Map<Long, double[]> itemFeatureVecs = null;
	
	private double[][] genderFeatureVecs = null;
	
	private Map<Integer, double[]> ageFeatureVecs = null;
	
	private Map<Integer, double[]> advFeatureVecs = null;
	
	private Map<Integer, double[]> picFeatureVecs = null;
	
	private Map<String, double[]> titleFeatureVecs = null;
	
	private Map<Integer, double[]> locationFeatureVecs = null;
	
	private double[] userAvgFeature = null;
	
	private double[] itemAvgFeature = null;
	
	private double[] picAvgFeature = null;
	
	private double[] advtAvgFeature = null;
	
	private double[] titleAvgFeature = null;
	
	private double[] locationAvgFeature = null;
	
	private Map<Long, Double> userBiAvgCTR = null;
	
	private Map<Long, Double> adBiAvgCTR = null;
	
	private double[] genderBias = null;	//v[genderValue-1]
	
	private double[] ageBias = null;
	
	private double userBiAvg = 0D;
	
	private double adBiAvg = 0D;
	
	private double picBiAvg = 0D;
	
	private double advtBiAvg = 0D;
	
	private double titleBiAvg = 0D;
	
	private double locationBiAvg = 0D;
	
	private Map<Integer, Double> advBias = null;
	
	private Map<Integer, Double> picBias = null;
	
	private Map<String, Double> titleBias = null;
	
	private Map<Integer, Double> locationBias = null;
	
	public int unExistUserQty = 0;
	public int unExistAdQty = 0;
	public int unExistAdvtQty = 0;
	public int unExistPic = 0;
	public int unExistTitle = 0;
	
	public int renderAdQty = 0;
	public int renderPicQty = 0;
	

	private static Logger log = LoggerFactory.getLogger(FeatureBasedMatrixFactorizer.class);
	
	/**
	 * 
	 * @param dataSet
	 * @param featureQty: the number of feature
	 * @param iteration
	 * @param learningRate
	 * @param overfitting
	 */
	public FeatureBasedMatrixFactorizer(ExtDataModel dataSet, int featureQty, int iteration, double learningRate, 
			double overfitting, double adjust) {
		this.dataSet = dataSet;
		this.featureQty = featureQty;
		this.iteration = iteration;
		this.learningRate = learningRate;
		this.overfitting = overfitting;
		this.adjust = adjust;

		this.globalAvgCTR = 1.0 * dataSet.getPositiveQty()/dataSet.getTotalQty();
		log.info("Global Average CTR : " + this.globalAvgCTR);
		initUserBiAvgCTR();
		initAdBiAvgCTR();
		initAdvBias();
		initPicBias();
		this.ageBias = new double[120];
		this.genderBias = new double[2];
		initTitleBias();
		initLocationBias();
		
		this.userAvgFeature = new double[featureQty];
		this.itemAvgFeature = new double[featureQty];
		this.picAvgFeature = new double[featureQty];
		this.advtAvgFeature = new double[featureQty];
		this.titleAvgFeature = new double[featureQty];
		this.locationAvgFeature = new double[featureQty];
		
		initAllFeatureVecs();	//user and items
		initGenderFeatureVecs();
		initAdvFeatureVecs();
		initAgeFeatureVecs();
		initPicFeatureVecs();
		initTitleFeatureVecs();
		initLocationFeatureVecs();
		
		SGDTrain();
		
		userAvgFeature_userAvgBias();
		itemAvgFeature_itemAvgBias();
		picAvgFeature_picAvgBias();
		advtAvgFeature_advtAvgBias();;
		titleAvgFeature_titleAvgBias();
		locationAvgFeature_locationAvgBias();
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

						double userFeature[] = userFeatureVecs.get(UID);
						double itemFeature[] = itemFeatureVecs.get(entry.getKey());

						UserInfo userInfo = userInfoMap.get(UID);
						double genderFeature[] = genderFeatureVecs[userInfo.gender-1];
						double ageFeature[] = ageFeatureVecs.get(userInfo.age);
						double advFeature[] = advFeatureVecs.get(entry.getValue().advID);
						double picFeature[] = picFeatureVecs.get(entry.getValue().picID);

						//add title feature
						double titleFeature[] = titleFeatureVecs.get(this.dataSet.getAllAdInfo().get(entry.getKey()).title);
						//add location feature
						double locationFeature[] = locationFeatureVecs.get(userInfo.loc);

						double ubi = userBiAvgCTR.get(UID);
						double abi = adBiAvgCTR.get(entry.getKey());

						double advBi = advBias.get(entry.getValue().advID);
						double genderBi = genderBias[userInfo.gender-1];
						double ageBi = ageBias[userInfo.age];

						double picBi = picBias.get(entry.getValue().picID);

						// add title bias
						double titleBi = titleBias.get(this.dataSet.getAllAdInfo().get(entry.getKey()).title);
						//add location bias
						double locationBi = locationBias.get(userInfo.loc);

						double eu = entry.getValue().clickValue - predict(userFeature, itemFeature, genderFeature, ageFeature, advFeature,
								picFeature, titleFeature, locationFeature, globalAvgCTR, ubi, abi, genderBi, ageBi, advBi, picBi, titleBi, locationBi);

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

						//add title bias
						titleBi += learningRate*(eu - overfitting*titleBi);
						titleBias.put(this.dataSet.getAllAdInfo().get(entry.getKey()).title, titleBi);
						//add location bias
						locationBi += learningRate*(eu - overfitting*locationBi);
						locationBias.put(userInfo.loc, locationBi);

						for(int k=0; k<featureQty; k++) {
							userFeature[k] += learningRate*(eu*(itemFeature[k] + advFeature[k] + picFeature[k] + titleFeature[k]) - overfitting*userFeature[k]);
							itemFeature[k] += learningRate*(eu*(userFeature[k] + genderFeature[k] + ageFeature[k] + locationFeature[k]) - overfitting*itemFeature[k]);

							genderFeature[k] += learningRate*(eu*(itemFeature[k] + advFeature[k] + picFeature[k] + titleFeature[k]) - overfitting*genderFeature[k]);
							ageFeature[k] += learningRate*(eu*(itemFeature[k] + advFeature[k] + picFeature[k] + titleFeature[k]) - overfitting*ageFeature[k]);

							advFeature[k] += learningRate*(eu*(userFeature[k] + genderFeature[k] + ageFeature[k] + locationFeature[k]) - overfitting*advFeature[k]);
							picFeature[k] += learningRate*(eu*(userFeature[k] + genderFeature[k] + ageFeature[k] + locationFeature[k]) - overfitting*picFeature[k]);

							titleFeature[k] += learningRate*(eu*(userFeature[k] + genderFeature[k] + ageFeature[k] + locationFeature[k]) - overfitting*titleFeature[k]);
							locationFeature[k] += learningRate*(eu*(itemFeature[k] + advFeature[k] + picFeature[k] + titleFeature[k]) - overfitting*locationFeature[k]);
						}

						userFeatureVecs.put(UID, userFeature);
						itemFeatureVecs.put(entry.getKey(), itemFeature);

						genderFeatureVecs[userInfo.gender-1] = genderFeature;
						ageFeatureVecs.put(userInfo.age, ageFeature);
						advFeatureVecs.put(entry.getValue().advID, advFeature);
						picFeatureVecs.put(entry.getValue().picID, picFeature);

						//add title feature
						titleFeatureVecs.put(this.dataSet.getAllAdInfo().get(entry.getKey()).title, titleFeature);
						//add location feature
						locationFeatureVecs.put(userInfo.loc, locationFeature);
					}

				}
				RMSEvaluator rmse = new RMSEvaluator(this.dataSet, this);
				learningRate *= adjust;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private double predict(double[] userFeature, double[] itemFeature, double[] genderFeature, double[] ageFeature, double[] advFeature, double[] picFeature,
						   double[] titleFeature, double[] locationFeature, double globalAvg, double userBi, double adBi, double genderBi, double ageBi, double advBi, double picBi,
						   double titleBi, double locationBi) {
		double prediction = globalAvg + userBi + adBi + genderBi + ageBi + advBi + picBi + titleBi + locationBi;
		for(int i=0; i<featureQty; i++) {
			prediction += (userFeature[i] + genderFeature[i] + ageFeature[i] + locationFeature[i])*(itemFeature[i] + advFeature[i] + picFeature[i] + titleFeature[i]);
		}

		return 1.0/(1.0 + Math.pow(Math.E, -1.0*prediction));
	}

	public double estimatePreference(long UID, long itemID, int advtID, int picID) throws Exception {

		boolean UEtag = true;
		boolean ADEtag = true;
		boolean PICEtag = true;
		boolean ADVTEtag = true;
		boolean TitleEtag = true;

		long original_adID = itemID;

		if(!this.userFeatureVecs.containsKey(UID) || !this.itemFeatureVecs.containsKey(itemID) || !this.advFeatureVecs.containsKey(advtID) || !this.picFeatureVecs.containsKey(picID) ||
				!this.titleFeatureVecs.containsKey(this.dataSet.getAllAdInfo().get(itemID).title)) {
			if(!this.userFeatureVecs.containsKey(UID)) {
				UEtag = false;
				unExistUserQty++;
			}

			if(!this.titleFeatureVecs.containsKey(this.dataSet.getAllAdInfo().get(original_adID).title)) {
				TitleEtag = false;
				this.unExistTitle++;
			}

			if(!this.itemFeatureVecs.containsKey(itemID)) {
				unExistAdQty++;
				String title = this.dataSet.getAllAdInfo().get(itemID).title;
				long tmp_ad = adIDMapping(title, picID);
				if(tmp_ad == -1) {
					ADEtag = false;
				} else {
					itemID = tmp_ad;
					this.renderAdQty++;
				}
//				itemID = adIDMapping(title, picID);
			}

			if(!this.picBias.containsKey(picID)) {
				this.unExistPic++;
				int tmp_pic = picIDMapping(itemID);
				if(tmp_pic == -1) {
					PICEtag = false;
				} else {
					picID = tmp_pic;
					this.renderPicQty++;
				}
			}

			if(!this.advFeatureVecs.containsKey(advtID)) {
				unExistAdvtQty++;
				ADVTEtag = false;
			}

		}

		UserInfo userInfo = this.dataSet.getAllUserInfo().get(UID);

		double preference = 0D;

		double[] userFeature = null;
		if(UEtag) userFeature = this.userFeatureVecs.get(UID);
		else userFeature = this.userAvgFeature;

		double[] itemFeature = null;
		if(ADEtag) itemFeature = this.itemFeatureVecs.get(itemID);
		else itemFeature = this.itemAvgFeature;

		double[] genderFeature = this.genderFeatureVecs[userInfo.gender-1];

		double[] ageFeature = this.ageFeatureVecs.get(userInfo.age);

		double[] advFeature = null;
		if(ADVTEtag) advFeature = this.advFeatureVecs.get(advtID);
		else advFeature = this.advtAvgFeature;

		double[] picFeature = null;
		if(PICEtag) picFeature = this.picFeatureVecs.get(picID);
		else picFeature = this.picAvgFeature;

		double[] titleFeature = null;
		if(TitleEtag) titleFeature = this.titleFeatureVecs.get(this.dataSet.getAllAdInfo().get(original_adID).title);
		else titleFeature = this.titleAvgFeature;

		double[] locationFeature = this.locationFeatureVecs.get(userInfo.loc);

		double ubi = 0D;
		if(UEtag) ubi = this.userBiAvgCTR.get(UID);
		else ubi = this.userBiAvg;

		double abi = 0D;
		if(ADEtag) abi = this.adBiAvgCTR.get(itemID);
		else abi = this.adBiAvg;

		double genderBi = this.genderBias[userInfo.gender-1];
		double ageBi = this.ageBias[userInfo.age];

		double advBi = 0D;
		if(ADVTEtag) advBi = this.advBias.get(advtID);
		else advBi = this.advtBiAvg;

		double picBi = 0D;
		if(PICEtag) picBi = this.picBias.get(picID);
		else picBi = this.picBiAvg;

		double titleBi = 0D;
		if(TitleEtag) titleBi = this.titleBias.get(this.dataSet.getAllAdInfo().get(original_adID).title);
		else titleBi = this.titleBiAvg;

		double locationBi = this.locationBias.get(userInfo.loc);

		for(int k=0; k<featureQty; k++) {
			preference += (userFeature[k] + genderFeature[k] + ageFeature[k] + locationFeature[k])*(itemFeature[k] + advFeature[k] + picFeature[k] + titleFeature[k]);
		}

		preference += globalAvgCTR + ubi + abi + genderBi + ageBi + advBi + picBi + titleBi + locationBi;

		return 1.0/(1.0 + Math.pow(Math.E, -1.0*preference));
	}
	
	private void initLocationFeatureVecs() {
		this.locationFeatureVecs = new HashMap<Integer, double[]>();
		Set<Long> UIDs = this.dataSet.getAllUserIDs();
		Random random = new Random();
		for(long UID : UIDs) {
			int locationID = this.dataSet.getAllUserInfo().get(UID).loc;
			if(!locationFeatureVecs.containsKey(locationID)) {
				double[] locationFeature = new double[featureQty];
				for(int k=0; k<featureQty; k++) {
					locationFeature[k] = random.nextDouble()/Math.sqrt(featureQty);
				}
				locationFeatureVecs.put(locationID, locationFeature);
			}
		}
	}
	
	private void initTitleFeatureVecs() {
		this.titleFeatureVecs = new HashMap<String, double[]>();
		Set<Long> adIDs = this.dataSet.getAllAdIDs();
		Random random = new Random();
		for(long adID : adIDs) {
			String title = this.dataSet.getAllAdInfo().get(adID).title;
			double[] titleFeature = new double[featureQty];
			for(int k=0; k<featureQty; k++) {
				titleFeature[k] = random.nextDouble()/Math.sqrt(featureQty);
			}
			titleFeatureVecs.put(title, titleFeature);
		}
	}
	
	private void initPicFeatureVecs() {
		this.picFeatureVecs = new HashMap<Integer, double[]>();
		Set<Integer> picIDs = this.dataSet.getAllPicIDs();
		Random random = new Random();
		for(int picID : picIDs) {
			double[] picFeature = new double[featureQty];
			for(int k=0; k<featureQty; k++) {
				picFeature[k] = random.nextDouble()/Math.sqrt(featureQty);
			}
			this.picFeatureVecs.put(picID, picFeature);
		}
	}
	
	private void initAgeFeatureVecs() {
		this.ageFeatureVecs = new HashMap<Integer, double[]>();
		Set<Integer> ages = this.dataSet.getAgeMap().keySet();
		Random random = new Random();
		for(int age : ages) {
			double[] ageFeature = new double[featureQty];
			for(int k=0; k<ageFeature.length; k++) {
				ageFeature[k] = random.nextDouble()/Math.sqrt(featureQty);
			}
			this.ageFeatureVecs.put(age, ageFeature);
		}
	}
	
	private void initAdvFeatureVecs() {
		this.advFeatureVecs = new HashMap<Integer, double[]>();
		Set<Integer> advIDs = this.dataSet.getAdvIDs();
		Random random = new Random();
		for(int advID : advIDs) {
			double[] advFeature = new double[featureQty];
			for(int k=0; k<advFeature.length; k++) {
				advFeature[k] = random.nextDouble()/Math.sqrt(featureQty);
			}
			this.advFeatureVecs.put(advID, advFeature);
		}
	}
	
	private void initGenderFeatureVecs() {
		Random random = new Random();
		double[] gender0 = new double[featureQty];
		double[] gender1 = new double[featureQty];
		for(int k=0; k<featureQty; k++) {
			gender0[k] = random.nextDouble()/Math.sqrt(featureQty);
			gender1[k] = random.nextDouble()/Math.sqrt(featureQty);
		}
		this.genderFeatureVecs = new double[2][featureQty];
		this.genderFeatureVecs[0] = gender0;
		this.genderFeatureVecs[1] = gender1;
	}
	
	
	private void initLocationBias() {
		this.locationBias = new HashMap<Integer, Double>();
		Set<Long> UIDs = this.dataSet.getAllUserIDs();
		for(long UID : UIDs) {
			int locationID = this.dataSet.getAllUserInfo().get(UID).loc;
			if(!locationBias.containsKey(locationID)) this.locationBias.put(locationID, 0D);
		}
	}
	
	
	private void initTitleBias() {
		this.titleBias = new HashMap<String, Double>();
		Set<Long> adIDs = this.dataSet.getAllAdIDs();
		Random random = new Random();
		for(long adID : adIDs) {
			String title = this.dataSet.getAllAdInfo().get(adID).title;
			titleBias.put(title, 0D);
		}
		
	}
	
	private void initPicBias() {
		this.picBias = new HashMap<Integer, Double>();
		Set<Integer> picIDs = this.dataSet.getAllPicIDs();
		for(int picID : picIDs) {
			picBias.put(picID, 0D);
		}
	}
	
	private void initAdvBias() {
		this.advBias = new HashMap<Integer, Double>();
		for(int advID : this.dataSet.getAdvIDs()) {
			this.advBias.put(advID, 0D);
		}
		
	}
	
	private void initUserBiAvgCTR() {
		this.userBiAvgCTR = new HashMap<Long, Double>();
		Set<Long> UIDs = this.dataSet.getAllUserIDs();
		for(long UID : UIDs) {
			this.userBiAvgCTR.put(UID, 0D);
		}
	}
	
	private void initAdBiAvgCTR() {
		this.adBiAvgCTR = new HashMap<Long, Double>();
		Set<Long> adIDs = this.dataSet.getAllAdIDs();
		for(long AID : adIDs) {
			this.adBiAvgCTR.put(AID, 0D);
		}
	}
	
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
	
	private void pseudoUserFeatureAndBias() {
		log.info("Start to compute user pseduo user feature ...");
		int passedUserQty = 0;
		for(Entry<Long, UserInfo> entry : this.dataSet.getAllUserInfo().entrySet()) {
			long UID = entry.getKey();
			if(this.userFeatureVecs.containsKey(UID)) continue;
			else {
				int gender = entry.getValue().gender;
				int age = entry.getValue().age;
				double[] pseudoUserFeature = new double[featureQty];
				double pseudoUserBias = 0D;
				List<UserInfo> sim_users = this.dataSet.getGender_Age_User_Mapping().get(gender).get(age);
				if(sim_users.size() == 0) {
					passedUserQty++;
					continue;
				}
				int cnt = 0;
				for(int i=0; i<sim_users.size(); i++) {
					if(!userFeatureVecs.containsKey(sim_users.get(i).UID)) continue;
					double[] sim_userFeature = userFeatureVecs.get(sim_users.get(i).UID);
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
	
	private void userAvgFeature_userAvgBias() {
		log.info("Start to compute user average feature ...");
		int cnt = 0;
		double ubias = 0D;
		for(Entry<Long, double[]> entry : this.userFeatureVecs.entrySet()) {
			double[] feature = entry.getValue();
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
	
	private void itemAvgFeature_itemAvgBias() {
		log.info("Start to compute ad average feature ...");
		int cnt = 0;
		double ibias = 0D;
		for(Entry<Long, double[]> entry : this.itemFeatureVecs.entrySet()) {
			double[] feature = entry.getValue();
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
	
	private void picAvgFeature_picAvgBias() {
		log.info("Start to compute pic average feature ...");
		int cnt = 0;
		double pbias = 0D;
		for(Entry<Integer, double[]> entry : this.picFeatureVecs.entrySet()) {
			double[] feature = entry.getValue();
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
	
	private void advtAvgFeature_advtAvgBias() {
		log.info("Start to compute advt average feature ...");
		int cnt = 0;
		double advtbias = 0D;
		for(Entry<Integer, double[]> entry : this.advFeatureVecs.entrySet()) {
			double[] feature = entry.getValue();
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
	
	private void titleAvgFeature_titleAvgBias() {
		log.info("Start to compute title average feature ...");
		int cnt = 0;
		double titlebias = 0D;
		for(Entry<String, double[]> entry : this.titleFeatureVecs.entrySet()) {
			double[] feature = entry.getValue();
			titlebias += this.titleBias.get(entry.getKey());
			for(int k=0; k<featureQty; k++) {
				this.titleAvgFeature[k] += feature[k];
			}
			cnt++;
		}
		for(int k=0; k<featureQty; k++) {
			this.titleAvgFeature[k] = this.titleAvgFeature[k]/cnt;
		}
		this.titleBiAvg = titlebias/cnt;
		log.info("Finish to compute title average feature, cnt : " + cnt);
	}
	
	private void locationAvgFeature_locationAvgBias() {
		int cnt = 0;
		double locationbias = 0D;
		for(Entry<Integer, double[]> entry : this.locationFeatureVecs.entrySet()) {
			double[] feature = entry.getValue();
			locationbias += this.locationBias.get(entry.getKey());
			for(int k=0; k<featureQty; k++) {
				this.locationAvgFeature[k] += feature[k];
			}
			cnt++;
		}
		for(int k=0; k<featureQty; k++) {
			this.locationAvgFeature[k] = this.locationAvgFeature[k]/cnt;
		}
		this.locationBiAvg = locationbias/cnt;
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
	

	
	public ExtDataModel getTrainSet() {
		return this.dataSet;
	}

	public Map<Long, double[]> getUserFactorVectors() {
		return this.userFeatureVecs;
	}

	public Map<Long, double[]> getItemFactorVectors() {
		return this.itemFeatureVecs;
	}

	public double[] getUserFactorVectorByID(long UID) {
		return this.userFeatureVecs.get(UID);
	}

	public double[] getItemFactorVectorByID(long itemID) {
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



}
