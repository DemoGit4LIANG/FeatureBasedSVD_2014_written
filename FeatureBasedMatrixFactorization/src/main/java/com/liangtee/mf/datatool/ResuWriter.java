package com.liangtee.mf.datatool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liangtee.mf.datamodel.DataModel;
import com.liangtee.mf.datamodel.ExtDataModel;
import com.liangtee.mf.datamodel.vo.ClickInfo;
import com.liangtee.mf.implicitfeedback.MFRecommender;
import com.liangtee.mf.implicitfeedback.algoritms.impl.feature.FeatureBasedMatrixFactorizer;
import com.liangtee.mf.implicitfeedback.algoritms.impl.feature.memsave.MemSavedFeatureBasedMF;

/**
 * Write the prediction according to test set
 * 
 * 
 * @author liangtee
 *
 */
public class ResuWriter {

	static Logger log = LoggerFactory.getLogger(ResuWriter.class);
	
	public static void write(MFRecommender mf, DataModel testSet) {
		BufferedWriter out = null;
		try {
			log.info("Start to write resu ... ");
			int cnt = 0;
			out = new BufferedWriter(new FileWriter("resu.csv"));
			for(Entry<Long, Map<Long, Double>> entry : testSet.entrySet()) {
				long UID = entry.getKey();
				for(Entry<Long, Double> entry2 : entry.getValue().entrySet()) {
					long itemID = entry2.getKey();
					double pre = mf.estimatePreference(UID, itemID);
					String resu = UID + "," + itemID + "," + pre + "," + entry2.getValue();
					out.write(resu);
					out.newLine();
					cnt++;
					if(cnt >= 500000 && cnt%500000==0) out.flush();
				}
			}
			out.close();
			log.info("Successfully writed : " + cnt + " lines ..." );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public static void write(FeatureBasedMatrixFactorizer mf, ExtDataModel testSet) {
//		BufferedWriter out = null;
//		try {
//			log.info("Start to write resu ... ");
//			int cnt = 0;
//			out = new BufferedWriter(new FileWriter("resu.csv"));
//			for(Entry<Long, Map<Long, ClickInfo>> entry : testSet.entrySet()) {
//				long UID = entry.getKey();
//				for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
//					long itemID = entry2.getKey();
//					double pre = mf.estimatePreference(UID, itemID, entry2.getValue().advID);
//					String resu = UID + "," + itemID + "," + pre + "," + entry2.getValue().clickValue;
//					out.write(resu);
//					out.newLine();
//					cnt++;
//					if(cnt >= 500000 && cnt%500000==0) out.flush();
//				}
//			}
//			out.close();
//			log.info("Successfully writed : " + cnt + " lines ..." );
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public static void write(Evo_FeatureBasedMatrixFactorizer mf, ExtDataModel testSet) {
		BufferedWriter out = null;
		try {
			log.info("Start to write resu ... ");
			int cnt = 0;
			out = new BufferedWriter(new FileWriter("resu.csv"));
			for(Entry<Long, Map<Long, ClickInfo>> entry : testSet.entrySet()) {
				long UID = entry.getKey();
				for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
					long itemID = entry2.getKey();
					double pre = mf.estimatePreference(UID, itemID, entry2.getValue().advID);
					String resu = UID + "," + itemID + "," + pre + "," + entry2.getValue().clickValue;
					out.write(resu);
					out.newLine();
					cnt++;
					if(cnt >= 500000 && cnt%500000==0) out.flush();
				}
			}
			out.close();
			log.info("Successfully writed : " + cnt + " lines ..." );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void write(MFRecommender mf, ExtDataModel testSet) {
		BufferedWriter out = null;
		try {
			log.info("Start to write resu ... ");
			int cnt = 0;
			out = new BufferedWriter(new FileWriter("resu.csv"));
			for(Entry<Long, Map<Long, ClickInfo>> entry : testSet.entrySet()) {
				long UID = entry.getKey();
				for(Entry<Long, ClickInfo> entry2 : entry.getValue().entrySet()) {
					long itemID = entry2.getKey();
					double pre = mf.estimatePreference(UID, itemID);
					String resu = UID + "," + itemID + "," + pre + "," + entry2.getValue().clickValue;
					out.write(resu);
					out.newLine();
					cnt++;
					if(cnt >= 500000 && cnt%500000==0) out.flush();
				}
			}
			out.close();
			log.info("Successfully writed : " + cnt + " lines ..." );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void write(FeatureBasedMatrixFactorizer mf, String testFile, String featureQty, String iter,
			String learningRate, String overfitting, String adjust) {
		BufferedReader in = null;
		BufferedWriter out = null;
		try {
			log.info("Start to write resu ... ");
			in = new BufferedReader(new FileReader(testFile));
			out = new BufferedWriter(new FileWriter(featureQty+"_"+iter+"_"+learningRate+"_"+overfitting+"_"+adjust));
			String line = null;
			int cnt = 0;
			while((line = in.readLine()) != null) {
				String[] det = line.split(",");
				long UID = Long.parseLong(det[0]);
				long adID = Long.parseLong(det[1]);
				int advtID = Integer.parseInt(det[2]);
				int picID = Integer.parseInt(det[3]);
//				int click = Integer.parseInt(det[5]);
				double pCTR = mf.estimatePreference(UID, adID, advtID, picID);
//				out.write(click + " " + pCTR);
				out.write(Double.toString(pCTR));
				out.newLine();
				cnt++;
			}
			
			out.close();
			in.close();
			
			log.info("Successfully writed : " + cnt + " lines ..." );
			log.info(mf.unExistUserQty + " users do not exist in training...");
			log.info(mf.unExistAdQty + " ads do not exist in training...");
			log.info(mf.unExistAdvtQty + " advts do not exist in training...");
			log.info(mf.unExistPic + " pics do not exist in training...");
			log.info(mf.unExistTitle + " titles do not exist in training...");
			
			log.info("Render ADs : " + mf.renderAdQty);
			log.info("Render Pics : " + mf.renderPicQty);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void write(MemSavedFeatureBasedMF mf, String testFile, String featureQty, String iter,
			String learningRate, String overfitting, String adjust) {
		BufferedReader in = null;
		BufferedWriter out = null;
		try {
			log.info("Start to write resu ... ");
			in = new BufferedReader(new FileReader(testFile));
			out = new BufferedWriter(new FileWriter(featureQty+"_"+iter+"_"+learningRate+"_"+overfitting+"_"+adjust));
			String line = null;
			int cnt = 0;
			while((line = in.readLine()) != null) {
				String[] det = line.split(",");
				long UID = Long.parseLong(det[0]);
				long adID = Long.parseLong(det[1]);
				int advtID = Integer.parseInt(det[2]);
				int picID = Integer.parseInt(det[3]);
				int click = Integer.parseInt(det[5]);
				float pCTR = mf.estimatePreference(UID, adID, advtID, picID);
				out.write(click + " " + pCTR);
				out.newLine();
				cnt++;
			}
			
			out.close();
			in.close();
			
			log.info("Successfully writed : " + cnt + " lines ..." );
			log.info(mf.unExistUserQty + " users do not exist in training...");
			log.info(mf.unExistAdQty + " ads do not exist in training...");
			log.info(mf.unExistAdvtQty + " advts do not exist in training...");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	public static void write(FeatureBasedMatrixFactorizer mf, String testFile, String featureQty, String iter,
//			String learningRate, String overfitting, String adjust, int current_iter) {
//		BufferedReader in = null;
//		BufferedWriter out = null;
//		try {
//			log.info("Start to write resu ... ");
//			in = new BufferedReader(new FileReader(testFile));
//			out = new BufferedWriter(new FileWriter(featureQty+"_"+iter+"_"+learningRate+"_"+overfitting+"_"+adjust+"_"+current_iter));
//			String line = null;
//			int cnt = 0;
//			while((line = in.readLine()) != null) {
//				String[] det = line.split(",");
//				long UID = Long.parseLong(det[0]);
//				long adID = Long.parseLong(det[1]);
//				int advtID = Integer.parseInt(det[2]);
//				int picID = Integer.parseInt(det[3]);
//				int click = Integer.parseInt(det[5]);
//				double pCTR = mf.estimatePreference(UID, adID, advtID, picID);
//				out.write(click + " " + pCTR);
//				out.newLine();
//				cnt++;
//			}
//			
//			out.close();
//			in.close();
//			
//			log.info("Successfully writed : " + cnt + " lines ..." );
//			log.info(mf.unExistUserQty + " users do not exist in training...");
//			log.info(mf.unExistAdQty + " ads do not exist in training...");
//			log.info(mf.unExistAdvtQty + " advts do not exist in training...");
//			log.info(mf.unExistPic + " pics do not exist in training...");
//			log.info(mf.unExistTitle + " titles do not exist in training...");
//			
//			log.info("Render ADs : " + mf.renderAdQty);
//			log.info("Render Pics : " + mf.renderPicQty);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
}
