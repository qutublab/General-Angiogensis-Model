
/*
 * 8-22-2011 Changed computeAverage and computeStandardMethods to
 * accomodate individualLimitedXYBranchLengthMicrons
 *
 * 9-19-2011 When there are no branches, the average
 * individualLimitedXYBranchLengthMicrons is set to 0.
 */



package shared;

import java.io.*;
import java.util.*;

public class BasicStats implements Serializable {

    public double branchCount = 0;
    public double sproutLengthMicrons = 0;
    public double sproutVolumeCubicMicrons = 0;
    public double initialSproutCount = 0;
    public double baseSphereCellCount = 0;
    public double sproutCellCount = 0;

    public double limitedXYBranchCount = 0;
    public double limitedXYSproutLengthMicrons = 0;
    public double limitedXYSproutAreaSquareMicrons = 0;
    public double limitedXYSproutCount = 0;

    //    public double limitedXYBranchLengthMicrons = 0;
    public LinkedList<Double> individualLimitedXYBranchLengthsMicrons = null;
    
    public double attemptedMigrationCount = 0;
    public double attemptedMigrationDistance = 0;
    public double actualMigrationDistance = 0;

    //    public StateDiagramModelResult.InitialConditions initialConditions = null;

    //    public double simulatedHours;
    //    public String simulatorVersion;


    public BasicStats() {
    }


    public BasicStats(long branchCount,
		      double sproutLengthMicrons) {
	this.branchCount = branchCount;
	this.sproutLengthMicrons = sproutLengthMicrons;
    }

    public BasicStats(long branchCount,
		      double sproutLengthMicrons,
		      double sproutVolumeCubicMicrons,
		      double initialSproutCount,
		      double baseSphereCellCount,
		      double sproutCellCount) {
	this.branchCount = branchCount;
	this.sproutLengthMicrons = sproutLengthMicrons;
	this.sproutVolumeCubicMicrons = sproutVolumeCubicMicrons;
	this.initialSproutCount = initialSproutCount;
	this.baseSphereCellCount = baseSphereCellCount;
	this.sproutCellCount = sproutCellCount;
    }



    public void addTo(BasicStats s) {
	branchCount += s.branchCount;
	sproutLengthMicrons += s.sproutLengthMicrons;
	sproutVolumeCubicMicrons += s.sproutVolumeCubicMicrons;
	initialSproutCount += s.initialSproutCount;
	baseSphereCellCount += s.baseSphereCellCount;
	sproutCellCount += s.sproutCellCount;
	limitedXYBranchCount += s.limitedXYBranchCount;
	limitedXYSproutLengthMicrons += s.limitedXYSproutLengthMicrons;
	//	limitedXYBranchLengthMicrons += s.limitedXYBranchlengthMicrons;
	limitedXYSproutAreaSquareMicrons += s.limitedXYSproutAreaSquareMicrons;
	limitedXYSproutCount += s.limitedXYSproutCount;
	attemptedMigrationCount += s.attemptedMigrationCount;
	attemptedMigrationDistance += s.attemptedMigrationDistance;
	actualMigrationDistance += s.actualMigrationDistance;
    }

    public void divideBy(double n) {
	branchCount = branchCount / n;
	sproutLengthMicrons = sproutLengthMicrons / n;
	sproutVolumeCubicMicrons = sproutVolumeCubicMicrons / n;
	initialSproutCount = initialSproutCount / n;
	baseSphereCellCount = baseSphereCellCount / n;
	sproutCellCount = sproutCellCount / n;
	limitedXYBranchCount = limitedXYBranchCount / n;
	limitedXYSproutLengthMicrons = limitedXYSproutLengthMicrons / n;
	//	limitedXYBranchLengthMicrons = limitedXYBranchLengthMicrons / n;
	limitedXYSproutAreaSquareMicrons = limitedXYSproutAreaSquareMicrons / n;
	limitedXYSproutCount = limitedXYSproutCount / n;
	attemptedMigrationCount = attemptedMigrationCount / n;
	attemptedMigrationDistance = attemptedMigrationDistance / n;
	actualMigrationDistance = actualMigrationDistance / n;

    }

    public BasicStats difference(BasicStats s) {
	BasicStats diff = new BasicStats();
	diff.branchCount = branchCount - s.branchCount;
	diff.sproutLengthMicrons = sproutLengthMicrons - s.sproutLengthMicrons;
	diff.sproutVolumeCubicMicrons = sproutVolumeCubicMicrons - s.sproutVolumeCubicMicrons;
	diff.initialSproutCount = initialSproutCount - s.initialSproutCount;
	diff.baseSphereCellCount = baseSphereCellCount - s.baseSphereCellCount;
	diff.sproutCellCount = sproutCellCount - s.sproutCellCount;
	diff.limitedXYBranchCount =
	    limitedXYBranchCount - s.limitedXYBranchCount;
	diff.limitedXYSproutLengthMicrons =
	    limitedXYSproutLengthMicrons - s.limitedXYSproutLengthMicrons;
	//	diff.limitedXYBranchLengthMicrons =
	//	    limitedXYBranchLengthMicrons - s.limitedXYBranchLengthMicrons;
	diff.limitedXYSproutAreaSquareMicrons =
	    limitedXYSproutAreaSquareMicrons
	    - s.limitedXYSproutAreaSquareMicrons;
	diff.limitedXYSproutCount =
	    limitedXYSproutCount - s.limitedXYSproutCount;
	diff.attemptedMigrationCount =
	    attemptedMigrationCount - s.attemptedMigrationCount;
	diff.attemptedMigrationDistance =
	    attemptedMigrationDistance - s.attemptedMigrationDistance;
	diff.actualMigrationDistance =
	    actualMigrationDistance - s.actualMigrationDistance;

	return diff;
    }

    public BasicStats pow(double expt) {
	BasicStats s = new BasicStats();
	s.branchCount = Math.pow(branchCount, expt);
	s.sproutLengthMicrons = Math.pow(sproutLengthMicrons, expt);
	s.sproutVolumeCubicMicrons = Math.pow(sproutVolumeCubicMicrons, expt);
	s.initialSproutCount = Math.pow(initialSproutCount, expt);
	s.baseSphereCellCount = Math.pow(baseSphereCellCount, expt);
	s.sproutCellCount = Math.pow(sproutCellCount, expt);
	s.limitedXYBranchCount =
	    Math.pow(limitedXYBranchCount, expt);
	s.limitedXYSproutLengthMicrons =
	    Math.pow(limitedXYSproutLengthMicrons, expt);
	//	s.limitedXYBranchLengthMicrons =
	//	    Math.pow(limitedXYBranchLengthMicrons, expt);
	s.limitedXYSproutAreaSquareMicrons =
	    Math.pow(limitedXYSproutAreaSquareMicrons, expt);
	s.limitedXYSproutCount =
	    Math.pow(limitedXYSproutCount, expt);
	s.attemptedMigrationCount = Math.pow(attemptedMigrationCount, expt);
	s.attemptedMigrationDistance = Math.pow(attemptedMigrationDistance, expt);
	s.actualMigrationDistance = Math.pow(actualMigrationDistance, expt);

	return s;
    }


    public static void testComputeAverageStandardDeviation() {
	System.out.println("[BasicStats.testComputeAverageStandardDeviation] Begin");
	BasicStats b1 = new BasicStats();
	b1.individualLimitedXYBranchLengthsMicrons = new LinkedList<Double>();
	b1.individualLimitedXYBranchLengthsMicrons.addLast(1.0);
	b1.individualLimitedXYBranchLengthsMicrons.addLast(2.0);
	b1.individualLimitedXYBranchLengthsMicrons.addLast(3.0);
	b1.limitedXYBranchCount = 3;
	BasicStats b2 = new BasicStats();
	b2.individualLimitedXYBranchLengthsMicrons = new LinkedList<Double>();
	b2.individualLimitedXYBranchLengthsMicrons.addLast(4.0);
	b2.individualLimitedXYBranchLengthsMicrons.addLast(5.0);
	b2.limitedXYBranchCount = 2;
	BasicStats b3 = new BasicStats();
	b3.individualLimitedXYBranchLengthsMicrons = new LinkedList<Double>();
	b3.individualLimitedXYBranchLengthsMicrons.addLast(6.0);
	b3.limitedXYBranchCount = 1;
	
	BasicStats[] bArr = new BasicStats[] {b1, b2, b3};

	BasicStats bAvg = computeAverage(bArr);
	if (bAvg.individualLimitedXYBranchLengthsMicrons.size() != 1) {
	    die("[basicStats.testComputeAverageStandardDeviation] individualLimitedXYBranchLengthsMicrons.size()="
		+ bAvg.individualLimitedXYBranchLengthsMicrons.size()
		+ "; expected 1");
	}
	if (bAvg.individualLimitedXYBranchLengthsMicrons.getFirst() != 3.5) {
	    die("[basicStats.testComputeAverageStandardDeviation] individualLimitedXYBranchLengthsMicrons.getFirst()="
		+ bAvg.individualLimitedXYBranchLengthsMicrons.getFirst()
		+ "; expected 3.5");
	}

	BasicStats bSD = computeStandardDeviation(bArr, bAvg);

	if (bSD.individualLimitedXYBranchLengthsMicrons.size() != 1) {
	    die("[basicStats.testComputeAverageStandardDeviation] individualLimitedXYBranchLengthsMicrons.size()="
		+ bSD.individualLimitedXYBranchLengthsMicrons.size()
		+ "; expected 1");
	}
	double sdDiff =
	    bSD.individualLimitedXYBranchLengthsMicrons.getFirst() - 1.870829;
	if (Math.abs(sdDiff) > 0.00001) {
	    die("[basicStats.testComputeAverageStandardDeviation] individualLimitedXYBranchLengthsMicrons.getFirst()="
		+ bSD.individualLimitedXYBranchLengthsMicrons.getFirst()
		+ "; expected 1.87029");
	}
	System.out.println("[BasicStats.testComputeAverageStandardDeviation] Passed");
    }

    public static BasicStats computeAverage(BasicStats[] stats) {
	BasicStats accumulator = new BasicStats();
	for (BasicStats s : stats) {
	    accumulator.addTo(s);
	}
	accumulator.divideBy(stats.length);

	// Handle individualLimitedXYBranchLengthsMicrons as a special case
	double sum = 0;
	double count = 0;
	for (BasicStats s : stats) {
	    double localCount = 0;
	    for (Iterator<Double> i = s.individualLimitedXYBranchLengthsMicrons.iterator();
		 i.hasNext();) {
		double d = i.next();
		sum += d;
		count++;
		localCount++;
	    }
	    if (localCount != s.limitedXYBranchCount) {
		die("[BasicStats.computeAverage] local branch count: "
		    + localCount + " is not " + s.limitedXYBranchCount);
	    }
	}
	double tolerance = 00000000001;
	if (Math.abs(count - (accumulator.limitedXYBranchCount * stats.length)) > tolerance) {
	    die("[BasicStats.computeAverage] branch count: "
		+ count + " is not "
		+ (accumulator.limitedXYBranchCount * stats.length));
	}
	double avg = count != 0? sum / count: 0;
	accumulator.individualLimitedXYBranchLengthsMicrons =
	    new LinkedList<Double>();
	accumulator.individualLimitedXYBranchLengthsMicrons.addFirst(avg);
	return accumulator;
    }

    public static BasicStats computeStandardDeviation(BasicStats[] stats, BasicStats average) {
	BasicStats accumulator = new BasicStats();
	for (BasicStats s : stats) {
	    accumulator.addTo(average.difference(s).pow(2));
	}
	accumulator.divideBy(stats.length - 1);
	BasicStats stDev = accumulator.pow(.5);
	
	// Handle special case of
	// individualLimitedXYBranchlengthsMicrons
	double avgBranchLength =
	    average.individualLimitedXYBranchLengthsMicrons.getFirst();
	double diffSqrSum = 0;
	double count = 0;
	for (BasicStats s : stats) {
	    for (Iterator<Double> i = s.individualLimitedXYBranchLengthsMicrons.iterator();
		 i.hasNext();) {
		double d = i.next();
		diffSqrSum += Math.pow(d - avgBranchLength, 2);
		count++;
	    }
	}
	double branchLengthStDev = Math.pow(diffSqrSum / (count - 1), 0.5);
	stDev.individualLimitedXYBranchLengthsMicrons =
	    new LinkedList<Double>();
	stDev.individualLimitedXYBranchLengthsMicrons.add(branchLengthStDev);

	return stDev;
    }

    public static BasicStats computeMinimum(BasicStats[] stats) {
	if (stats.length == 0) {
	    die("[BasicStats.computeMinimum] Array argument is of 0 length");
	}
	BasicStats accumulator = new BasicStats();
	accumulator.addTo(stats[0]);
	for (int i = 1; i < stats.length; i++) {
	    BasicStats s = stats[i];
	    accumulator.branchCount = Math.min(accumulator.branchCount, s.branchCount);
	    accumulator.sproutLengthMicrons =
		Math.min(accumulator.sproutLengthMicrons, s.sproutLengthMicrons);
	    accumulator.sproutVolumeCubicMicrons =
		Math.min(accumulator.sproutVolumeCubicMicrons, s.sproutVolumeCubicMicrons);
	    accumulator.baseSphereCellCount =
		Math.min(accumulator.baseSphereCellCount, s.baseSphereCellCount);
	    accumulator.sproutCellCount =
		Math.min(accumulator.sproutCellCount, s.sproutCellCount);
	    accumulator.initialSproutCount =
		Math.min(accumulator.initialSproutCount, s.initialSproutCount);

	    accumulator.limitedXYBranchCount =
		Math.min(accumulator.limitedXYBranchCount,
			 s.limitedXYBranchCount);
	    accumulator.limitedXYSproutLengthMicrons =
		Math.min(accumulator.limitedXYSproutLengthMicrons,
			 s.limitedXYSproutLengthMicrons);
	    //	    accumulator.limitedXYBranchLengthMicrons =
	    //		Math.min(accumulator.limitedXYBranchLengthMicrons,
	    //			 s.limitedXYBranchLengthMicrons);
	    accumulator.limitedXYSproutAreaSquareMicrons =
		Math.min(accumulator.limitedXYSproutAreaSquareMicrons,
			 s.limitedXYSproutAreaSquareMicrons);
	    accumulator.limitedXYSproutCount =
		Math.min(accumulator.limitedXYSproutCount,
			 s.limitedXYSproutCount);
	    accumulator.attemptedMigrationCount =
		Math.min(accumulator.attemptedMigrationCount,
			 s.attemptedMigrationCount);
	    accumulator.attemptedMigrationDistance =
		Math.min(accumulator.attemptedMigrationDistance,
			 s.attemptedMigrationDistance);
	    accumulator.actualMigrationDistance =
		Math.min(accumulator.actualMigrationDistance,
			 s.actualMigrationDistance);
	    
	}

	// Compute minimum of individualLimitedXYBranchLengthsMicrons
	double min = Double.MAX_VALUE;
	boolean minExists = false;
	for (BasicStats s : stats) {
	    for (Iterator<Double> i = s.individualLimitedXYBranchLengthsMicrons.iterator();
		 i.hasNext();) {
		double d = i.next();
		if (d <= min) {
		    min = d;
		    minExists = true;
		}
	    }
	}
	if (!minExists) {
	    min = 0;
	}
	accumulator.individualLimitedXYBranchLengthsMicrons =
	    new LinkedList<Double>();
	accumulator.individualLimitedXYBranchLengthsMicrons.add(min);

	return accumulator;
    }


    public static BasicStats computeMaximum(BasicStats[] stats) {
	if (stats.length == 0) {
	    die("[BasicStats.computeMaximum] Array argument is of 0 length");
	}
	BasicStats accumulator = new BasicStats();
	accumulator.addTo(stats[0]);
	for (int i = 1; i < stats.length; i++) {
	    BasicStats s = stats[i];
	    accumulator.branchCount = Math.max(accumulator.branchCount, s.branchCount);
	    accumulator.sproutLengthMicrons =
		Math.max(accumulator.sproutLengthMicrons, s.sproutLengthMicrons);
	    accumulator.sproutVolumeCubicMicrons =
		Math.max(accumulator.sproutVolumeCubicMicrons, s.sproutVolumeCubicMicrons);
	    accumulator.baseSphereCellCount =
		Math.max(accumulator.baseSphereCellCount, s.baseSphereCellCount);
	    accumulator.sproutCellCount =
		Math.max(accumulator.sproutCellCount, s.sproutCellCount);
	    accumulator.initialSproutCount =
		Math.max(accumulator.initialSproutCount, s.initialSproutCount);

	    accumulator.limitedXYBranchCount =
		Math.max(accumulator.limitedXYBranchCount,
			 s.limitedXYBranchCount);
	    accumulator.limitedXYSproutLengthMicrons =
		Math.max(accumulator.limitedXYSproutLengthMicrons,
			 s.limitedXYSproutLengthMicrons);
	    //	    accumulator.limitedXYBranchLengthMicrons =
	    //		Math.max(accumulator.limitedXYBranchLengthMicrons,
	    //			 s.limitedXYSproutLengthMicrons);
	    accumulator.limitedXYSproutAreaSquareMicrons =
		Math.max(accumulator.limitedXYSproutAreaSquareMicrons,
			 s.limitedXYSproutAreaSquareMicrons);
	    accumulator.limitedXYSproutCount =
		Math.max(accumulator.limitedXYSproutCount,
			 s.limitedXYSproutCount);
	    accumulator.attemptedMigrationCount =
		Math.max(accumulator.attemptedMigrationCount,
			 s.attemptedMigrationCount);
	    accumulator.attemptedMigrationDistance =
		Math.max(accumulator.attemptedMigrationDistance,
			 s.attemptedMigrationDistance);
	    accumulator.actualMigrationDistance =
		Math.max(accumulator.actualMigrationDistance,
			 s.actualMigrationDistance);
	}

	// Compute maximum of individualLimitedXYBranchLengthsMicrons
	double max = 0;
	for (BasicStats s : stats) {
	    for (Iterator<Double> i = s.individualLimitedXYBranchLengthsMicrons.iterator();
		 i.hasNext();) {
		double d = i.next();
		max = Math.max(d, max);
	    }
	}
	accumulator.individualLimitedXYBranchLengthsMicrons =
	    new LinkedList<Double>();
	accumulator.individualLimitedXYBranchLengthsMicrons.add(max);

	return accumulator;
    }


    public String toString() {
	String individualBranchLengths = "[";
	boolean first = true;
	if (individualLimitedXYBranchLengthsMicrons != null) {
	    for (double b : individualLimitedXYBranchLengthsMicrons) {
		if (first) {
		    first = false;
		    individualBranchLengths += "" + b;
		}
		else {
		individualBranchLengths += ", " + b;
		}
	    }
	}
	individualBranchLengths += "]";
	String retStr =
	    "BasicStats[" 
	    + "branchCount=" + branchCount
	    + ",sproutLengthMicrons=" + sproutLengthMicrons
	    + ",sproutVolumeCubicMicrons=" + sproutVolumeCubicMicrons
	    + ",initialSproutCount=" + initialSproutCount
	    + ",baseSphereCellCount=" + baseSphereCellCount
	    + ",sproutCellCount=" + sproutCellCount
	    + ",limitedXYBranchCount=" + limitedXYBranchCount
	    + ",limitedXYSproutLengthMicrons=" + limitedXYSproutLengthMicrons
	    //	    + ",limitedXYBranchLengthMicrons=" + limitedXYBranchLengthMicrons
	    + ",individualLimitedXYBranchLengthsMicrons=" + individualBranchLengths
	    + ",limitedXYSproutAreaSquareMicrons=" + limitedXYSproutAreaSquareMicrons
	    + ",limitedXYSproutCount=" + limitedXYSproutCount
	    + ",attemptedMigrationCount=" + attemptedMigrationCount
	    + ",attemptedMigrationDistance=" + attemptedMigrationDistance
	    + ",actualMigrationDistance=" + actualMigrationDistance
	    + "]";
	return retStr;
    }

    public static void singleColumnPrint(BasicStats[] stats) {
	String[] header
	    = new String[] {"Branch Count", "Sprout Length", "Sprout Volume",
			    "Sprout Cell Count",
			    "Limited XY BranchCount",
			    "Limited XY Sprout Length",
			    //			    "Limited XY Branch Length",
			    "Limited XY Sprout Area",
			    "Limited XY SproutCount",
			    "Attempted Migration Count",
			    "Attempted Migration Distance",
			    "Actual Migration Distance"	};
	int numberOfFields;
	double[][] convertedStats = new double[stats.length][];
	for (int i = 0; i < stats.length; i++) {
	    double[] fields = new double[header.length];
	    fields[0] = stats[i].branchCount;
	    fields[1] = stats[i].sproutLengthMicrons;
	    fields[2] = stats[i].sproutVolumeCubicMicrons;
	    fields[3] = stats[i].sproutCellCount;
	    fields[4] = stats[i].limitedXYBranchCount;
	    fields[5] = stats[i].limitedXYSproutLengthMicrons;
	    //	    fields[6] = stats[i].limitedXYBranchLengthMicrons;
	    fields[6] = stats[i].limitedXYSproutAreaSquareMicrons;
	    fields[7] = stats[i].limitedXYSproutCount;
	    fields[8] = stats[i].attemptedMigrationCount;
	    fields[9] = stats[i].attemptedMigrationDistance;
	    fields[10] = stats[i].actualMigrationDistance;
	    convertedStats[i] = fields;
	}
	for (int j = 0; j < header.length; j++) {
	    System.out.println(header[j]);
	    for (int i = 0; i < stats.length; i++) {
		System.out.println(convertedStats[i][j]);
	    }
	}
    }


    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }

    public static void main(String[] args) {
	testComputeAverageStandardDeviation();
    }


}