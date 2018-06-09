package com.liangtee.mf.implicitfeedback;

import java.util.List;
import java.util.Map;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.vo.UserItemPair;

public interface MFRecommender {

	public DataModel getTrainSet();
	
	public Map<Long, double[]> getUserFactorVectors();
	
	public Map<Long, double[]> getItemFactorVectors();
	
	public double[] getUserFactorVectorByID(long UID);
	
	public double[] getItemFactorVectorByID(long itemID);
	
	public List<UserItemPair> recommend4UserByID(long UID, int N);
	
	public Map<Long, List<UserItemPair>> getRecommendations();
	
	public double estimatePreference(long UID, long itemID) throws Exception;
	
	public double similarity(long UID, long itemID) throws Exception;
	
}
