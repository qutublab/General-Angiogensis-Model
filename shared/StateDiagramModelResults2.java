package shared;

import java.util.ArrayList;
import java.io.*;

public class StateDiagramModelResult2 extends StateDiagramModelResult2 implements Comparable<StateDiagramModelResult2>, Serializable
{
    public SimulationStats2[] stats;

    public String toString() {
	String statsStr = null;
	if (stats != null) {
	    statsStr = "[";
	    for (int i = 0; i < stats.length; i++) {
		if (i != 0) {
		    statsStr += ",";
		}
		statsStr += stats[i].toString();
	    }
	    statsStr += "]";
	}
	String growthStr = "[";
	for (int i = 0; i < averageInVitroGrowth.length; i++) {
	    if (i != 0) {
		growthStr += ",";
	    }
	    growthStr += averageInVitroGrowth[i];
	}
	growthStr += "]";
	String branchingStr = "[";
	for (int i = 0; i < averageInVitroBranching.length; i++) {
	    if (i != 0) {
		branchingStr += ",";
	    }
	    branchingStr += averageInVitroBranching[i];
	}
	branchingStr += "]";
	String retStr;
	retStr =
	    "StateDiagramModelResult["
	    + "model=" + model
	    + ",stats=" + statsStr
	    + ",score=" + score
	    + ",randomSeed=" + randomSeed
	    + ",repetitions=" + repetitions
	    + ",simulatorParametersFileName=" + simulatorParametersFileName
	    + ",ruleSetName=" + ruleSetName
	    + ",concentrationsManagerName=" + concentrationsManagerName
	    + ",averageInVitroGrowth=" + growthStr
	    + ",averageInVitroBranching=" + branchingStr
	    + "]";
	return retStr;
    }

	
    public void write(String fileName) {
	try {
	    FileOutputStream fos = new FileOutputStream(fileName);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(this);
	    oos.close();
	}
	catch (Exception e) {
	    die("[StateDiagramModelresult.write] Unable to write value:  " + e.toString());
	}
    }
	
    public static StateDiagramModelResult read(String fileName) {
	StateDiagramModelResult sdmr = null;
	try {
	    FileInputStream fis = new FileInputStream(fileName);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    sdmr = (StateDiagramModelResult) ois.readObject();
	    ois.close();
	}
	catch (Exception e) {
	    die("[StateDiagramModelResult.read] Unable to read value:  " + e.toString());
	}
	return sdmr;
    }


    private static void testSerializability() {
	StateDiagramModelResult sdmr = new StateDiagramModelResult();
	sdmr.model = new StateDiagramModel();
	sdmr.model.tipQuiescentToQuiescent = 99.9;
	try {
	    FileOutputStream fos = new FileOutputStream("temp");
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(sdmr);
	    oos.close();
	}
	catch (Exception e) {
	    System.out.println(e.toString());
	    System.exit(1);
	}

	try {
	    FileInputStream fis = new FileInputStream("temp");
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    StateDiagramModelResult sdmr2 = (StateDiagramModelResult) ois.readObject();
	    ois.close();
	    System.out.println("sdmr2:\n" + sdmr2);
	}
	catch (Exception e) {
	    System.out.println(e.toString());
	    System.exit(1);
	}

	
    }


    
	public static void main(String[]args)
	{
	    testSerializability();
		//System.out.println(InitialConditions.V0B0);
		
		/*public int compareTo(StateDiagramModelResult r2)
		{
			double score1 = Math.pow(this.branchCount - actualBranching,2)/averageBranching 
			+ Math.pow(this.totalGrowth - actualGrowth, 2)/averageGrowth;
			double score2 = Math.pow(r2.branchCount - actualBranching,2)/averageBranching 
			+ Math.pow(r2.totalGrowth - actualGrowth, 2)/averageGrowth;
			
			int ret = 0;
			
			if(score1<score2)
				ret = 1;
			else if (score1>score2)
				ret = -1;
			
			return ret;
		}*/	
		
		/*
		 * public double getScore()
		{
			return Math.pow(this.branchCount - actualBranching,2)/averageBranching 
			+ Math.pow(this.totalGrowth - actualGrowth, 2)/averageGrowth;
		}
		 */
	}
}
	