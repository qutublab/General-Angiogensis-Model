
/*
 * 4-15-2011 Created. Normally, one would just alter SimulationStats
 * instead of creating a subclass.  However, since SimulationStats is
 * serializable, and hence can be written to a file, changing the
 * class would make the file unreadable (I think).  Therefore, to keep
 * existing files readable, this subclass was created.
 *
 */

package shared;

import java.io.*;

public class SimulationStats2 extends SimulationStats implements Serializable {

    public double branchCountStdDev = 0;
    public double sproutLengthMicronsStdDev = 0;

    //    public double branchCount = 0;
    //    public double sproutLengthMicrons = 0;
    //    public double sproutVolumeCubicMicrons = 0;
    //    public double initialSproutCount = 0;
    //    public double baseSphereCellCount = 0;
    //    public double sproutCellCount = 0;

    //    public StateDiagramModelResult.InitialConditions initialConditions = null;

    //    public double simulatedHours;
    //    public String simulatorVersion;


    public SimulationStats2() {
    }


    public SimulationStats2(long branchCount,
			   double sproutLengthMicrons) {
	super(branchCount, sproutLengthMicrons);
    }

    public SimulationStats2(long branchCount,
			    double sproutLengthMicrons,
			    double sproutVolumeCubicMicrons,
			    double initialSproutCount,
			    double baseSphereCellCount,
			    double sproutCellCount) {
	super(branchCount, sproutLengthMicrons, sproutVolumeCubicMicrons,
	      initialSproutCount, baseSphereCellCount, sproutCellCount);
    }

    public String toString() {
	String retStr =
	    "SimulationStats["
	    + "branchCount=" + branchCount
	    + ",branchCountStdDev=" + branchCountStdDev
	    + ",sproutLengthMicrons=" + sproutLengthMicrons
	    + ",sproutLengthMicronsStdDev=" + sproutLengthMicronsStdDev
	    + ",sproutVolumeCubicMicrons=" + sproutVolumeCubicMicrons
	    + ",initialSproutCount=" + initialSproutCount
	    + ",baseSphereCellCount=" + baseSphereCellCount
	    + ",sproutCellCount=" + sproutCellCount
	    + ",initialConditions=" + initialConditions
	    + ",simulatedHours=" + simulatedHours
	    + ",simulatorVersion=" + simulatorVersion
	    + "]";
	return retStr;
    }


    public static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }

    public static SimulationStats2 computeStats(SimulationStats[] stats) {
	SimulationStats2 avgStats = new SimulationStats2();
	avgStats.initialConditions = stats[0].initialConditions;
	avgStats.simulatedHours = stats[0].simulatedHours;
	avgStats.simulatorVersion = stats[0].simulatorVersion;
	for (SimulationStats s : stats) {
	    avgStats.addTo(s);
	    if (avgStats.initialConditions != s.initialConditions) {
		die("[SimulationStats2.computeStats] Two different initial conditions found: "
		    + avgStats.initialConditions + " and " + s.initialConditions);
		//		avgStats.initialConditions = null;
	    }
	    if (avgStats.simulatedHours != s.simulatedHours) {
		die("[SimulationStats2.computeStats] Two different simulation lengths (in hours) found: "
		    + avgStats.simulatedHours + " and " + s.simulatedHours);
	    }
	    if (avgStats.simulatorVersion != s.simulatorVersion) {
		die("[SimulationStats2.computeStats] Two different simulator versions used: "
		    + avgStats.simulatorVersion + " and " + s.simulatorVersion);
	    }
	    
	}
	avgStats.divideBy(stats.length);
	double branchCountVal = 0;
	double sproutLengthMicronsVal = 0;
	for (SimulationStats s : stats) {
	    branchCountVal += Math.pow(s.branchCount - avgStats.branchCount, 2);
	    sproutLengthMicronsVal +=
		Math.pow(s.sproutLengthMicrons - avgStats.sproutLengthMicrons, 2);
	}
	avgStats.branchCountStdDev = Math.sqrt(branchCountVal / (stats.length - 1));
	avgStats.sproutLengthMicronsStdDev =
	    Math.sqrt(sproutLengthMicronsVal / (stats.length - 1));
	return avgStats; 
    }


}