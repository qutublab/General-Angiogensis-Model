package shared;

import java.io.*;
import java.util.*;

public class SimulationStats implements Serializable {

    public long randomSeed;
    public OneRepStats[] stats;

    public int repetitions = 0;
    public String modelName;
    

    public BasicStats average;
    public BasicStats standardDeviation;

    //    public BasicStats[] timeStepAverage;
    //    public BasicStats[] timeStandardDeviation;

    public StateDiagramModelResult.InitialConditions initialConditions = null;

    public double simulatedHours;
    public String simulatorVersion;

    public String angiogenesisRuleSet;
    public String initialConditionsStr;  


    public SimulationStats() {
	average = new BasicStats();
	standardDeviation = new BasicStats();
    }


    public void printTimeStepStats() {
	// aggregate individual time step stats
	int numberOfTimeSteps = stats[0].timeStepStats.length;
	BasicStats[] timeStepAverage =
	    new BasicStats[numberOfTimeSteps];
	BasicStats[] timeStepStandardDeviation =
	    new BasicStats[numberOfTimeSteps];
	BasicStats[] timeStepStats = new BasicStats[stats.length];
	for (int t = 0; t < numberOfTimeSteps; t++) {
	    for (int i = 0; i < stats.length; i++) {
		timeStepStats[i] = stats[i].timeStepStats[t];
	    }
	    timeStepAverage[t] = BasicStats.computeAverage(timeStepStats);
	    timeStepStandardDeviation[t] =
		BasicStats.computeStandardDeviation(timeStepStats,
						    timeStepAverage[t]);
	}
	System.out.println("Individual time step averages");
	for (int t = 0; t < numberOfTimeSteps; t++) {
	    System.out.println(timeStepAverage[t]);
	}
    }

    public SimulationStats(long randomSeed, OneRepStats[] stats, EnvironmentInterface env) {
    	// Don't store env.randomSeed.  The env parameter has the
    	// random seed used by the last repetition.
    	this.randomSeed = randomSeed;
    	this.stats = stats;
	repetitions = stats.length;
    	initialConditions = env.getInitialConditionsDescriptor();
    	initialConditionsStr = env.getInitialConditionsString();
    	simulatedHours = env.getTotalSimulationHours();
    	simulatorVersion = env.getVersionString();
    	angiogenesisRuleSet = env.getRuleSetName();
	
    	// get final statistics from each simulation run
    	BasicStats[] finalStats = new BasicStats[stats.length];
    	for (int i = 0; i < stats.length; i++) {
    	    BasicStats[] timeStepStats = stats[i].timeStepStats;
    	    finalStats[i] = timeStepStats[timeStepStats.length - 1];
    	}
    	average = BasicStats.computeAverage(finalStats);
    	standardDeviation = BasicStats.computeStandardDeviation(finalStats, average);
    }
    
    public SimulationStats(long randomSeed, BasicStats[] bStats, EnvironmentInterface env) {
	// Don't store env.randomSeed.  The env parameter has the
	// random seed used by the last repetition.
	this.randomSeed = randomSeed;
	this.stats = null;
	repetitions = bStats.length;
	initialConditions = env.getInitialConditionsDescriptor();
	initialConditionsStr = env.getInitialConditionsString();
	simulatedHours = env.getTotalSimulationHours();
	simulatorVersion = env.getVersionString();
	angiogenesisRuleSet = env.getRuleSetName();

	average = BasicStats.computeAverage(bStats);
	standardDeviation =
	    BasicStats.computeStandardDeviation(bStats, average);

    }



    public SimulationStats(double averageBranchCount, 
			   double averageSproutLengthMicrons,
			   double averageIndividualLimitedXYBranchLengthsMicrons) {
	average = new BasicStats();
	average.limitedXYBranchCount = averageBranchCount;
	average.limitedXYSproutLengthMicrons = averageSproutLengthMicrons;
	average.individualLimitedXYBranchLengthsMicrons =
	    new LinkedList<Double>();
	average.individualLimitedXYBranchLengthsMicrons.add(averageIndividualLimitedXYBranchLengthsMicrons);
	standardDeviation = new BasicStats();
    }


    public void printStatsPerTimeStep() {
	System.out.println(angiogenesisRuleSet);
	System.out.println(initialConditionsStr);
	System.out.println("Random Seed=" + randomSeed);

	// for each time-step and computed statistic, create a BasicStats object
	int numberOfTimeSteps = stats[0].timeStepStats.length;
	BasicStats[] avg = new BasicStats[numberOfTimeSteps];
	BasicStats[] stdDev = new BasicStats[numberOfTimeSteps];
	BasicStats[] min = new BasicStats[numberOfTimeSteps];
	BasicStats[] max = new BasicStats[numberOfTimeSteps];
	
	BasicStats[] s = new BasicStats[stats.length];
	for (int step = 0; step < numberOfTimeSteps; step++) {
	    for (int rep = 0; rep < stats.length; rep++) {
		s[rep] = stats[rep].timeStepStats[step];
	    }
	    avg[step] = BasicStats.computeAverage(s);
	    stdDev[step] = BasicStats.computeStandardDeviation(s, avg[step]);
	    min[step] = BasicStats.computeMinimum(s);
	    max[step] = BasicStats.computeMaximum(s);
	}
	System.out.println("Averages");
	BasicStats.singleColumnPrint(avg);
	System.out.println("Standard Deviations");
	BasicStats.singleColumnPrint(stdDev);
	System.out.println("Minimums");
	BasicStats.singleColumnPrint(min);
	System.out.println("Maximums");
	BasicStats.singleColumnPrint(max);
    }
    
    public String toString() {
	String statsStr = null;
	if (stats != null) {
	    for (OneRepStats osr : stats) {
		if (statsStr == null) {
		    statsStr = osr.toString();
		}
		else {
		    statsStr += "," + osr.toString();
		}
	    }
	    statsStr = "[" + statsStr + "]";
	}
	String retStr =
	    "SimulationStats["
	    + "randomSeed=" + randomSeed
	    + ",repetitions=" + repetitions
	    + ",angiogenesisRuleSet=" + angiogenesisRuleSet
	    + ",intialConditionsStr=" + initialConditionsStr
	    + ",simulatedHours=" + simulatedHours
	    + ",simulatorVersion=" + simulatorVersion
	    + ",average=" + average.toString()
	    + ",standardDeviation=" + standardDeviation.toString()
	    + ",stats=" + statsStr
	    //	    + "branchCount=" + branchCount
	    //	    + ",sproutLengthMicrons=" + sproutLengthMicrons
	    //	    + ",sproutVolumeCubicMicrons=" + sproutVolumeCubicMicrons
	    //	    + ",initialSproutCount=" + initialSproutCount
	    //	    + ",baseSphereCellCount=" + baseSphereCellCount
	    //	    + ",sproutCellCount=" + sproutCellCount
	    //	    + ",initialConditions=" + initialConditions
	    + "]";
	return retStr;
    }



}