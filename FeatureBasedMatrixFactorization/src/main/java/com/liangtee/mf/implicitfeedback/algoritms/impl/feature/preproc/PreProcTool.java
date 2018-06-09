package com.liangtee.mf.implicitfeedback.algoritms.impl.feature.preproc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.liangtee.mf.datamodel.ExtDataModel;
import com.liangtee.mf.datamodel.impl.ExtBinaryDataModel;
import com.liangtee.mf.datamodel.vo.ClickInfo;

public class PreProcTool {

	public static Map<Long, Double> userAvgCTR(ExtDataModel data, double a, int b) {
		Map<Long, Double> userAvgCTR = new HashMap<Long, Double>();
		Map<Long, Integer> userImpression = new HashMap<Long, Integer>();
		Map<Long, Integer> userClick = new HashMap<Long, Integer>();
		
		for(Entry<Long, Map<Long, ClickInfo>> entry : data.entrySet()) {
			long UID = entry.getKey();
			for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
				if(userImpression.containsKey(UID)) {
					userImpression.put(UID, userImpression.get(UID)+1);
				} else {
					userImpression.put(UID, 1);
				}
				if(entry2.getValue().clickValue == 1D) {
					if(userClick.containsKey(UID)) {
						userClick.put(UID, userClick.get(UID)+1);
					} else {
						userClick.put(UID, 1);
					}
				}
			}
		}
		
		
		for(Entry<Long, Integer> entry : userImpression.entrySet()) {
			int impression = entry.getValue();
			int click = 0;
			if(userClick.containsKey(entry.getKey())) click = userClick.get(entry.getKey());
			userAvgCTR.put(entry.getKey(), (click+a*b)/(impression+b));
		}
		
		return userAvgCTR;
	}
	
	public static Map<Long, Double> ADAvgCTR(ExtDataModel data, double a, int b) {
		Map<Long, Double> ADAvgCTR = new HashMap<Long, Double>();
		Map<Long, Integer> ADImpression = new HashMap<Long, Integer>();
		Map<Long, Integer> ADClick = new HashMap<Long, Integer>();
		
		for(Entry<Long, Map<Long, ClickInfo>> entry : data.entrySet()) {
			for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
				long AID = entry2.getKey();
				if(ADImpression.containsKey(AID)) {
					ADImpression.put(AID, ADImpression.get(AID)+1);
				} else {
					ADImpression.put(AID, 1);
				}
				if(entry2.getValue().clickValue == 1D) {
					if(ADClick.containsKey(AID)) {
						ADClick.put(AID, ADClick.get(AID)+1);
					} else {
						ADClick.put(AID, 1);
					}
				}
			}
		}
		
		for(Entry<Long, Integer> entry : ADImpression.entrySet()) {
			int impression = ADImpression.get(entry.getKey());
			int click = 0;
			if(ADClick.containsKey(entry.getKey())) click = ADClick.get(entry.getKey());
			ADAvgCTR.put(entry.getKey(), (click+a*b)/(impression+b));
		}
		
		return ADAvgCTR;
	}
	
	public static Map<Integer, Double> advtAvgCTR(ExtDataModel data, double a, int b) {
		Map<Integer, Double> advtAvgCTR = new HashMap<Integer, Double>();
		Map<Integer, Integer> advtImpression = new HashMap<Integer, Integer>();
		Map<Integer, Integer> advtClick = new HashMap<Integer, Integer>();
		
		for(Entry<Long, Map<Long, ClickInfo>> entry : data.entrySet()) {
			for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
				int advtID = entry2.getValue().advID;
				if(advtImpression.containsKey(advtID)) {
					advtImpression.put(advtID, advtImpression.get(advtID)+1);
				} else {
					advtImpression.put(advtID, 1);
				}
				if(entry2.getValue().clickValue == 1D) {
					if(advtClick.containsKey(advtID)) {
						advtClick.put(advtID, advtClick.get(advtID)+1);
					} else {
						advtClick.put(advtID, 1);
					}
				}
			}
		}
		
		for(Entry<Integer, Integer> entry : advtImpression.entrySet()) {
			int impression = entry.getValue();
			int click = 0;
			if(advtClick.containsKey(entry.getKey())) click = advtClick.get(entry.getKey());
			advtAvgCTR.put(entry.getKey(), (click+a*b)/(impression+b));
		}
		
		return advtAvgCTR;
	}
	
	public static Map<Integer, Double> picAvgCTR(ExtDataModel data, double a, int b) {
		Map<Integer, Double> picAvgCTR = new HashMap<Integer, Double>();
		
		
		
		return picAvgCTR;
	}
	
	public static double[] ageAvgCTR(ExtDataModel data, double a, int b) {
		double[] ageAvgCTR = new double[120];
		int[] ageImpression = new int[120];
		int[] ageClick = new int[120];
		
		for(Entry<Long, Map<Long, ClickInfo>> entry : data.entrySet()) {
			int age = data.getAllUserInfo().get(entry.getKey()).age;
			for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
				ageImpression[age]++;
				if(entry2.getValue().clickValue == 1D) ageClick[age]++;
			}
		}

		for(int i=0; i<ageAvgCTR.length; i++) {
			ageAvgCTR[i] = (ageClick[i] + a*b)/(ageImpression[i] + b);
		}
		
		return ageAvgCTR;
	}
	
	public static double[] genderAvgCTR(ExtDataModel data, double a, int b) {
		double[] genderAvgCTR = new double[2];
		int[] genderImpression = new int[2];
		int[] genderClick = new int[2];
		
		for(Entry<Long, Map<Long, ClickInfo>> entry : data.entrySet()) {
			int gender = data.getAllUserInfo().get(entry.getKey()).gender;
			for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
				genderImpression[gender-1]++;
				if(entry2.getValue().clickValue == 1D) genderClick[gender-1]++;
			}
		}
		
		for(int i=0; i<genderAvgCTR.length; i++) {
			genderAvgCTR[i] = (genderClick[i] + a*b)/(genderImpression[i] + b);
		}
		
		return genderAvgCTR;
	}
	
}
