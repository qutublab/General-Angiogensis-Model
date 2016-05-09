
package shared;

import java.io.*;

public class OneRepStats implements Serializable {

    public long randomSeed;
    public BasicStats[] timeStepStats;

    public OneRepStats(long randomSeed, BasicStats[] timeStepStats) {
	this.randomSeed = randomSeed;
	this.timeStepStats = timeStepStats;
    }

    public String toString() {
	String statsStr = null;
	for (BasicStats s : timeStepStats) {
	    if (statsStr == null) {
		statsStr = s.toString();
	    }
	    else {
		statsStr += "," + s.toString();
	    }
	}
	statsStr = "[" + statsStr + "]";
	String retStr =
	    "OneRepStats["
	    + "randomSeed=" + randomSeed
	    + ",timeStepStats=" + statsStr
	    + "]";
	return retStr;
	}

}