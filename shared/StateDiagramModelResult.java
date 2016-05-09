
/*
 * 9-21-2011 Overloaded the write method to take a file parameter.
 *
 */

package shared;

import java.util.ArrayList;
import java.io.*;

public class StateDiagramModelResult implements Comparable<StateDiagramModelResult>, Serializable
{
    public enum InitialConditions {V0B0, V50B0, V25B50, V0B50, V0B100, V25B25};
    
    public StateDiagramModel model;
    public SimulationStats[] stats;
    
    // Set by the simulator
    public String timeStamp;
    public long randomSeed;
    public InitialConditions initialConditions;
    public int repetitions;
    public String simulatorParametersFileName;
    public String ruleSetName;
    public String concentrationsManagerName;
    
    // Set by the genetic algorithm
    public String geneticAlgorithmVersion;
    
    public double[] averageInVitroGrowth;
    public double[] averageInVitroBranching;
    
    //public int creationIterationNumber;
    public boolean scoreAlreadyCalculated = false;
    
    public double score;
    public double[][] individualScores =
	new double[InitialConditions.values().length][];

    
    //Genetic algorithm-set parameters
    public double allowableError;
    public int numIterations; //iteration number upon creation
    public int maxIterations; //maximum iterations set by GenAlg
    public int popSize; 
    public double fractionKept;
    //public RandomInterface rand;
    public int genomeSize;    
    public double mutationFactor;
    public double maxAOrBThreshold;
    public double minAOrBThreshold;
    public double maxMutationFactor;
    public double minMutationFactor;
    public double exponentialScalingFactor;
    public double[] averageGrowth;
    public double[] averageBranching;
    
    public StateDiagramModelResult()
    {
	model = new StateDiagramModel();
    }
    
    public StateDiagramModelResult(StateDiagramModel model,
				   long randomSeed,
				   InitialConditions initialConditions) {
	this.model = model;
	this.randomSeed = randomSeed;
	this.initialConditions = initialConditions;
    }

    public String toString() {
	String statsStr = null;
	if (stats != null) {
	    statsStr = "[";
	    for (int i = 0; i < stats.length; i++) {
		if (i != 0) {
		    statsStr += ",";
		}
		if (stats[i] != null) {
		    statsStr += stats[i].toString();
		}
		else {
		    statsStr += "null";
		}
	    }
	    statsStr += "]";
	}
	String growthStr = "[";
	if (averageInVitroGrowth != null) {
	    for (int i = 0; i < averageInVitroGrowth.length; i++) {
		if (i != 0) {
		    growthStr += ",";
		}
		growthStr += averageInVitroGrowth[i];
	    }
	}
	growthStr += "]";
	String branchingStr = "[";
	if (averageInVitroBranching != null) {
	    for (int i = 0; i < averageInVitroBranching.length; i++) {
		if (i != 0) {
		    branchingStr += ",";
		}
		branchingStr += averageInVitroBranching[i];
	    }
	}
	branchingStr += "]";
	    
	String scoreString = "{";
	for (int i = 0; i < individualScores.length; i++) {
	    if (i > 0) {
		scoreString += ",";
	    }
	    double[] componentScores = individualScores[i];
	    if (componentScores == null) {
		scoreString += "null";
	    }
	    else {
		scoreString += "{" + InitialConditions.values()[i].toString();
		for (int j = 0; j < componentScores.length; j++) {
		    scoreString += "," + componentScores[j];
		}
		scoreString += "}";
	    }
	}
	scoreString += "}";


	//	String paramsStr = null;
	//if (simulatorParameterFileNames != null) {
	//paramsStr = "[";
	//for (int i = 0; i < simulatorParameterFileNames.length; i++) {
	//if (i != 0) {
	//paramsStr += ",";
	//}
	//paramsStr += simulatorParameterFileNames[i];
	//}
	//paramsStr += "]";
	//	}
	String retStr;
	retStr =
	    "StateDiagramModelResult["
	    + "model=" + model
	    + ",stats=" + statsStr
	    + ",score=" + score
	    + ",individualScores=" + scoreString
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

	
	public int compareTo(StateDiagramModelResult r2)
	{
		double score1 = this.score;
		double score2 = r2.score;
		int ret = 0;
		if(score1>score2)
			ret = 1;
		else if (score1<score2)
			ret = -1;	
		return ret;
	}
	

    private static void die(String s) {
	System.err.println(s);
	Throwable th = new Throwable();
	th.printStackTrace();
	System.exit(1);
    }

    public void write(String fileName) {
	write(new File(fileName));
	//	try {
	//	    FileOutputStream fos = new FileOutputStream(fileName);
	//	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	//	    oos.writeObject(this);
	//	    oos.close();
	//	}
	//	catch (Exception e) {
	//	    die("[StateDiagramModelResult.write] Unable to write value:  " + e.toString());
	//	}
    }
	
    public void write(File f) {
	try {
	    FileOutputStream fos = new FileOutputStream(f);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(this);
	    oos.close();
	}
	catch (Exception e) {
	    die("[StateDiagramModelResult.write] Unable to write value:  " + e.toString());
	}
    }
	
    public static StateDiagramModelResult read(String fileName) {
	StateDiagramModelResult sdmr = null;
	try {
	    FileInputStream fis = new FileInputStream(fileName);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    Object obj = ois.readObject();
	    //	    System.out.println("[StateDiagramModelResult.read] read "
	    //			       + obj.getClass() + " object");
	    sdmr = (StateDiagramModelResult) obj;
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


    public static void printModel(String fileName) {
	StateDiagramModelResult sdmr = read(fileName);
	System.out.println(sdmr.model);
    }

    
    public static void main(String[]args)
    {
	printModel(args[0]);
	if (true) {return;}


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
