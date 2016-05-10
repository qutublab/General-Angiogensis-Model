
package scaffold;

import java.util.*;

public class SproutData {

    public double sproutLengthMicrons = 0;
    public double sproutVolumeCubicMicrons = 0;
    public double limitedXYSproutLengthMicrons = 0;
    //    public double limitedXYBranchLengthMicrons = 0;
    public double limitedXYSproutAreaSquareMicrons = 0;
    public int limitedXYSproutCount = 0;
    //    public LinkedList<Double> branchLengthList = new LinkedList<Double>();

    public String toString() {
	//	String branchLengths = "[";
	//	boolean first = true;
	//	for (Iterator<Double> i = branchLengthList.iterator(); i.hasNext();) {
	//	    double d = i.next();
	//	    if (first) {
	//		first = false;
	//		branchLengths += "," + d;
	//	    }
	//	    else {
	//		branchLengths += "" + d;
	//	    }
	//	}
	//	branchLengths += "]";
	String retStr;
	retStr = "SproutData["
	    + "sproutLengthMicrons=" + sproutLengthMicrons
	    + ",sproutVolumeCubicMicrons=" + sproutVolumeCubicMicrons
	    + ",limitedXYSproutLengthMicrons=" + limitedXYSproutLengthMicrons
	    //	    + ",limitedXYBranchLengthMicrons=" + limitedXYBranchLengthMicrons
	    + ",limitedXYSproutAreaSquareMicrons=" + limitedXYSproutAreaSquareMicrons
	    + ",limitedXYSproutCount=" + limitedXYSproutCount
	    //	    + ",branchLengthList=" + branchLengths
	    + "]";
	return retStr;
    }
}