
package scaffold;

import shared.*;
import java.io.*;
import java.util.*;

public class PrintData {

    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }

    private static StateDiagramModelResult read(String fileName) {
	Object obj = null;
	try {
	    FileInputStream fis = new FileInputStream(fileName);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    obj = ois.readObject();
	    ois.close();
	}
	catch (Exception e) {
	    die("Unable to read file: " + fileName + " " + e.toString());
	}
	String className = obj.getClass().getName();
	if (!className.equals("shared.StateDiagramModelResult")) {
	    die("File contains object of class " + className);
	}
	return (StateDiagramModelResult) obj;
    }


    public static void printStats(SimulationStats ss) {
	System.out.println("Initial Conditions: " + ss.initialConditionsStr);
	OneRepStats[] stats = ss.stats;
	System.out.println("Limited XY Branch Count");
	for (OneRepStats ors : stats) {
	    BasicStats[] timeStepStats = ors.timeStepStats;
	    int lastStep = timeStepStats.length - 1;
	    BasicStats b = timeStepStats[lastStep];
	    System.out.println(b.limitedXYBranchCount);
	}
	System.out.println("Limited XY Sprout Length");
	for (OneRepStats ors : stats) {
	    BasicStats[] timeStepStats = ors.timeStepStats;
	    int lastStep = timeStepStats.length - 1;
	    BasicStats b = timeStepStats[lastStep];
	    System.out.println(b.limitedXYSproutLengthMicrons);
	}
	System.out.println("Limited XY Branch Length");
	for (OneRepStats ors : stats) {
	    BasicStats[] timeStepStats = ors.timeStepStats;
	    int lastStep = timeStepStats.length - 1;
	    BasicStats b = timeStepStats[lastStep];
	    LinkedList<Double> branchLengths =
		b.individualLimitedXYBranchLengthsMicrons;
	    int count = 0;
	    double sum = 0;
	    for (Iterator<Double> i = branchLengths.iterator();
		 i.hasNext();) {
		double len = i.next();
		count++;
		sum += len;
	    }
	    double avg = count==0? 0 : sum/count;
	    System.out.println(avg);
	}
    }
    
    private static void printStats(String fileName) {
	StateDiagramModelResult sdmr = read(fileName);
	SimulationStats[] stats = sdmr.stats;
	System.out.println("Limited XY Branch Counts");
	for (SimulationStats ss : stats) {
	    if (ss != null) {
		System.out.println(ss.initialConditionsStr);
		System.out.println(ss.stats);
		//		SimulationStats2 ss2 = (SimulationStats2) ss;
		//		for (BasicStats bs : ss2.individualResults) {
		//		//		    System.out.println(bs.limitedXYBranchCount);
		//	    }
	    }
	}
	System.out.println("Limited XY Sprout Lengths");
	for (SimulationStats ss : stats) {
	    if (ss != null) {
		System.out.println(ss.initialConditionsStr);
		//		SimulationStats2 ss2 = (SimulationStats2) ss;
		//		for (BasicStats bs : ss2.individualResults) {
		//		    System.out.println(bs.limitedXYSproutLengthMicrons);
		//		}
	    }
	}

	System.out.println("Limited XY Branch Lengths");
	for (SimulationStats ss : stats) {
	    if (ss != null) {
		System.out.println(ss.initialConditionsStr);
		//		SimulationStats2 ss2 = (SimulationStats2) ss;
		/*
		for (BasicStats bs : ss2.individualResults) {
		    LinkedList<Double> branchLengths =
			bs.individualLimitedXYBranchLengthsMicrons;
		    int count = 0;
		    double sum = 0;
		    for (Iterator<Double> i = branchLengths.iterator();
			 i.hasNext();) {
			double len = i.next();
			count++;
			sum += len;
		    }
		    double avg = count==0? 0 : sum/count;
		    System.out.println(avg);
		}
		*/
	    }
	}

    }
    
    public static void main(String[] args) {
	printStats(args[0]);


    }





}