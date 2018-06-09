import com.liangtee.mf.datamodel.ExtDataModel;
import com.liangtee.mf.datamodel.impl.ExtBinaryDataModel;
import com.liangtee.mf.datatool.ResuWriter;
import com.liangtee.mf.implicitfeedback.algoritms.impl.feature.FeatureBasedMatrixFactorizer;

/**
 * 
 * @author liangtee
 *
 */
public class Launcher {

	public static void main(String[] args) {
		ExtDataModel trainSet = new ExtBinaryDataModel(args[0], args[1], args[2]);
		FeatureBasedMatrixFactorizer fbmf = new FeatureBasedMatrixFactorizer(trainSet, Integer.parseInt(args[4]), Integer.parseInt(args[5]), Double.parseDouble(args[6]), Double.parseDouble(args[7]), Double.parseDouble(args[8]));
		ResuWriter.write(fbmf, args[3], args[4], args[5], args[6], args[7], args[8]);
	}
	
}
