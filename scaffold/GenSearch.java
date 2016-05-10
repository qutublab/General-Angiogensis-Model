/*
remove genalg interface; use genalg directly


* 9-6-2011 Timestamp and random seed passed in GenAlg.initialize
* method.
*
*/
package scaffold;

import shared.*;
import search.*;
import ec.util.MersenneTwisterFast;
import java.util.*;
import java.io.*;

public class GenSearch {

    private static final String OUTPUT_FILE_PREFIX = "ga";


    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }



    private static ArrayList<StateDiagramModelResult> search(long randomSeed,
							     String initialModelsDirectory,
							     int repetitions,
							     String paramFileName,
							     String ruleSetName,
							     StateDiagramModelResult.InitialConditions initialCondition,
							     String concentrationsManagerName,
							     String timeStamp,
							     File dir) {
	
	PublicRandom random = new PublicRandom(new MersenneTwisterFast(randomSeed));

					    
	GenAlg ga = new GenAlg();
	ga.useLimitedXYStats();
	//	ga.useAverageAsDenominator();
	//	ga.useInVitroZero();
	if (initialCondition != null) {
	    ga.useSingleInitialCondition(initialCondition);
	}
	//	System.out.println("[GenSearch.search] Disabling genetic algorithm decay");
	//	ga.disableDecay();

	System.out.println("[GenSearch.search] Setting maximum iterations to 100");
	ga.setMaxIterations(100);



	// Initialization is the last step in preparing the GenAlg object
	ga.initialize(random, randomSeed, dir, timeStamp);

	ArrayList<StateDiagramModelResult> models =
	    ga.createPop(initialModelsDirectory);

	int generationNumber = 0;

	StateDiagramModelResult bestModelResult = null;
	String[] args = new String[] {paramFileName, ruleSetName, concentrationsManagerName, "", ""};
	Parameters params = new Parameters(args);
	do {
	    int skipCount = 0;
	    int modelNumber = 0;
	    for (Iterator<StateDiagramModelResult> i = models.iterator(); i.hasNext();) {
		
		System.out.println("[GenSearch.search] Begin simulating model number "
				   + modelNumber);

		boolean flag = false;
		StateDiagramModelResult sdmr = i.next();

		// If the model already has a score assigned to it, skip it
		if (sdmr.scoreAlreadyCalculated) {
		    skipCount++;
		    modelNumber++;
		    continue;
		}
		SimulationStats[] statsTotals =
		    new SimulationStats[StateDiagramModelResult.InitialConditions.values().length];


		if (initialCondition != null) {
		    // force a random seed
		    long seed = random.nextLong();
		    SimulationStats stats = 
			Environment.simulate(params, seed, sdmr.model,
					     repetitions, initialCondition);
		    int index = stats.initialConditions.ordinal();
		    if (initialCondition != stats.initialConditions) {
			Environment.die("[GenSearch.search] stats.initialConditions="
					+ stats.initialConditions
					+ " but initialCondition="
					+ initialCondition);
		    }
		    if (statsTotals[index] != null) {
			Environment.die("[GenSearch.search] " + stats.initialConditions
					+ " case has been simulated twice.");
		    }
		    statsTotals[index] = stats;
		    // get rid of stats for individual runs
		    stats.stats = null;
		}
		else {
		    for (StateDiagramModelResult.InitialConditions initCond : StateDiagramModelResult.InitialConditions.values()) {
			
			// force a random seed
			long seed = random.nextLong();
			SimulationStats stats = 
			    Environment.simulate(params, seed, sdmr.model,
						 repetitions, initCond);
			int index = stats.initialConditions.ordinal();
			if (initCond != stats.initialConditions) {
			    Environment.die("[GenSearch.search] stats.initialConditions="
					    + stats.initialConditions + " but initCond="
					    + initCond);
			}
			if (statsTotals[index] != null) {
			    Environment.die("[GenSearch.search] " + stats.initialConditions
					    + " case has been simulated twice.");
			}
			statsTotals[index] = stats;
			// get rid of stats for individual runs
			stats.stats = null;
		    }
		}

		if (initialCondition == null) {
		    // verify that all initial conditions were used
		    for (int j = 0; j < statsTotals.length; j++) {
			if (statsTotals[j] == null) {
			    die("[GenSearch.search] Case "
				+ StateDiagramModelResult.InitialConditions.values()[j]
				+ " was not simulated");
			}
		    }
		}

		sdmr.stats = statsTotals;
		//		System.out.println(sdmr);
		
		
		modelNumber++;
	    }
	    System.gc();


	    bestModelResult = ga.updatePop(models);
	    
	    generationNumber++;
	    //	    System.out.println("[GenSearch.search] Prematurely ending loop");
	    //	    if (true) {return null;}
	} while (bestModelResult == null);
	for (Iterator<StateDiagramModelResult> i = models.iterator(); i.hasNext();) {
	    StateDiagramModelResult sdmr = i.next();
	    sdmr.randomSeed = randomSeed;
	    sdmr.repetitions = repetitions;
	    sdmr.simulatorParametersFileName = paramFileName;
	    sdmr.ruleSetName = ruleSetName;
	    sdmr.concentrationsManagerName = concentrationsManagerName;
	    sdmr.timeStamp = timeStamp;
	}
	//	bestModelResult.randomSeed = randomSeed;
	//	bestModelResult.repetitions = repetitions;
	//	bestModelResult.simulatorParametersFileName = paramFileName;
	//	bestModelResult.ruleSetName = ruleSetName;
	//        bestModelResult.concentrationsManagerName = concentrationsManagerName;
	//        bestModelResult.timeStamp = timeStamp;

	return models;
    }

    //    private static StateDiagramModelResult searchOLD(long randomSeed,
    //						  int repetitions,
    //						  String[] paramFileNames,
    //						  String ruleSetName,
    //						  String concentrationsManagerName) {
    //	PublicRandom random = new PublicRandom(new MersenneTwisterFast(randomSeed));
    //
    //					    
    //	GenAlg ga = new GenAlg();
    //
    //	ga.initialize(random);
    //
    //	ArrayList<StateDiagramModelResult> models = ga.createPop();
    //
    //	int generationNumber = 0;
    //
    //	//       	ArrayList<StateDiagramModelResult> models = new ArrayList<StateDiagramModelResult>();
    //	//	StateDiagramModel sdmod = createModel();
    //	//	StateDiagramModelResult sdmodres = new StateDiagramModelResult();
    //	//	sdmodres.model = sdmod;
    //	//	models.add(sdmodres);
    //
    //	StateDiagramModelResult bestModelResult = null;
    //	String[] args = new String[] {"", ruleSetName, concentrationsManagerName, ""};
    //	do {
    //	    int skipCount = 0;
    //	    int modelNumber = 0;
    //	    for (Iterator<StateDiagramModelResult> i = models.iterator(); i.hasNext();) {
    //		StateDiagramModelResult sdmr = i.next();
    //		// If the model already has a score assigned to it, skip it
    //		if (sdmr.scoreAlreadyCalculated) {
    //		    skipCount++;
    //		    continue;
    //		}
    //		if (paramFileNames.length
    //		    != StateDiagramModelResult.InitialConditions.values().length) {
    //		    Environment.die("[GenAlg.search] Number of parameter files: "
    //				    + paramFileNames.length
    //				    + " does not match number of expected input cases: "
    //				    + StateDiagramModelResult.InitialConditions.values().length);
    //		}
    //		SimulationStats[] statsTotals = new SimulationStats[paramFileNames.length];
    //		for (int j = 0; j < paramFileNames.length; j ++) {
    //		    //		    System.out.println("[GenSearch.search] generation=" + generationNumber
    //		    //				       + " model=" + modelNumber + " initial-condition=" + j);
    //
    //		    // force a random seed
    //		    String seed = "" + random.nextLong();
    //		    args[0] = paramFileNames[j];
    //		    args[3] = seed;
    //		    //		    System.out.println("******* starting simulator with " + args[0]);
    //
    //		    SimulationStats stats = 
    //			Environment.simulate(args, sdmr.model, repetitions,
    //					     Environment.SimulationMode.GENETIC_ALGORITHM);
    //		    int index = stats.initialConditions.ordinal();
    //		    if (statsTotals[index] != null) {
    //			Environment.die("[GenSearch.search] " + stats.initialConditions
    //					+ " case has been simulated twice.");
    //		    }
    //		    statsTotals[index] = stats;
    //		}
    //		for (int j = 0; j < statsTotals.length; j++) {
    //		    if (statsTotals[j] == null) {
    //			die("[GenSearch.search] Case "
    //			    + StateDiagramModelResult.InitialConditions.values()[j]
    //			    + " was not simulated");
    //		    }
    //		}
    //		sdmr.stats = statsTotals;
    //		//		System.out.println(sdmr);
    //		modelNumber++;
    //	    }
    //	    bestModelResult = ga.updatePop(models);
    //	    generationNumber++;
    //	    //	    System.out.println("[GenSearch.search] Prematurely ending loop");
    //	    //	    if (true) {return null;}
    //	} while (bestModelResult == null);
    //	bestModelResult.randomSeed = randomSeed;
    //	bestModelResult.repetitions = repetitions;
    //	bestModelResult.simulatorParameterFileNames = paramFileNames;
    //	bestModelResult.ruleSetName = ruleSetName;
    //        bestModelResult.concentrationsManagerName = concentrationsManagerName;
    //
    //	return bestModelResult;
    //    }

    /*
     * Arguments: seed, repetitions, parameter file, ruleset, concentrations
     * manager
     */
    public static void main(String[] args) {

	Calendar cal = Calendar.getInstance();
	String timeStamp = Environment.createTimeStamp(cal);

	int expectedNumberOfArguments = 7;
	int numberOfArgs = args.length;
	if (numberOfArgs != expectedNumberOfArguments) {
	    die("[GenSerach.main] Expected " + expectedNumberOfArguments + " arguments; received "
		+ numberOfArgs);
	}

	String randomSeedArg = args[0];
	String initialConditionsArg = args[1];
	String initialModelsDirectory = args[2];
	String repetitionsArg = args[3];
	String paramFileName = args[4];
	String ruleSetName = args[5];
	String concentrationsManagerName = args[6];


	long randomSeed;
	if (randomSeedArg.equals("")) {
	    randomSeed = System.currentTimeMillis();
	}
	else {
	    randomSeed = Long.parseLong(randomSeedArg);
	}
	System.out.println("[GenSearch.main] Random seed=" + randomSeed);

	StateDiagramModelResult.InitialConditions initCond = null;
	if (!initialConditionsArg.equals("")) {
	    for (StateDiagramModelResult.InitialConditions ic : StateDiagramModelResult.InitialConditions.values()) {
		if (initialConditionsArg.equalsIgnoreCase(ic.toString())) {
		    initCond = ic;
		    break;
		}
	    }
	    if (initCond == null) {
		Environment.die("Unknown initial conditions specifier: "
				+ initialConditionsArg);
	    }
	}

	int repetitions = Integer.parseInt(repetitionsArg);

	String outputName = OUTPUT_FILE_PREFIX + timeStamp;
	if (initCond != null) {
	    outputName += initCond;
	}
	File dir = new File(outputName);
	boolean success = dir.mkdir();
	if (!success) {
	    Environment.die("[GenSearch.search] Unable to create "
			    + outputName + " directory");
	}

	ArrayList<StateDiagramModelResult> models = 
	    search(randomSeed,
		   initialModelsDirectory,
		   repetitions,
		   paramFileName,
		   ruleSetName,
		   initCond,
		   concentrationsManagerName,
		   timeStamp,
		   dir);

	//	System.out.println("Best state diagram model is: " + bestModel); 
	StateDiagramModelResult best = models.get(0);
	File outputFile = new File(dir, outputName);
	best.write(outputFile);


	PrintStream ps = null;
	String fileName = "gaNotes" + timeStamp;
	File f = new File(dir, fileName);
	try {
	    ps = new PrintStream(f);
	}
	catch (Exception e) {
	    die("[GenSearch.main] Unable to create PrintStream for file "
		+ fileName);
	}
	if (!initialModelsDirectory.equals("")) {
	    ps.println("Initial models from directory "
		       + initialModelsDirectory);
	}
	double score = 0;
	int length = numberOfDigits(models.size());
	int index = 0;
	for (Iterator<StateDiagramModelResult> i = models.iterator(); i.hasNext();) {
	    StateDiagramModelResult sdmr = i.next();
	    if (score > sdmr.score) {
		Environment.die("[GenSearch.search] models are out of order");
	    }
	    score = sdmr.score;
	    String lastGenFileName = outputName + "last." + pad(index, length);
	    File lastGenFile = new File(dir, lastGenFileName);
	    sdmr.write(lastGenFile);
	    ps.println(lastGenFileName + "    " + score);
	    index++;
	}
	ps.close();
    }
    

    private static void testDir() {
	String dirName = "DirTest";
	File dir = new File(dirName);
	boolean success = dir.mkdir();
	if (!success) {
	    Environment.die("[GenSearch.testDir] Unable to create " + dirName
			    + " directory");
	}
	String fileName = "temp";
	File f = new File(dir, fileName);
	PrintStream ps = null;
	try {
	    ps = new PrintStream(f);
	}
	catch (Exception e) {
	    Environment.die("[GenSearch.testDir]" + e.toString());
	}
	ps.println("test line");
	ps.close();
    }


    private static int numberOfDigits(long n) {
	if (n < 0) {
	    n = -n;
	}
	int count = 1;
	while (n > 9) {
	    count++;
	    n = n / 10;
	}
	return count;
    }

    private static void testPad() {
	for (int i = 0; i < 20; i++) {
	    System.out.println(pad(i, 2));
	}
    }


    private static String pad(int n, int length) {
	int nLength = numberOfDigits(n);
	String s = "" + n;
	while (nLength < length) {
	    s = "0" + s;
	    nLength++;
	}
	return s;
    }


    private static StateDiagramModel createModel() {
	StateDiagramModel sdm = new StateDiagramModel();
	sdm.tipQuiescentToQuiescent = 1.0 / 4.0;
	sdm.tipQuiescentToMigration = 1.0 / 4.0;
	sdm.tipQuiescentToProliferation = 1.0 / 4.0;
	sdm.tipQuiescentToBranching = 1.0 / 4.0;
	sdm.tipMigrationToQuiescent = 1.0 / 4.0;
	sdm.tipMigrationToMigration = 1.0 / 4.0;
	sdm.tipMigrationToProliferation = 1.0 / 4.0;
	sdm.tipMigrationToBranching = 1.0 / 4.0;
	sdm.tipProliferationToQuiescent = 1.0 / 4.0;
	sdm.tipProliferationToMigration = 1.0 / 4.0;
	sdm.tipProliferationToProliferation = 1.0 / 4.0;
	sdm.tipProliferationToBranching = 1.0 / 4.0;
	sdm.tipBranchingToQuiescent = 1.0 / 4.0;
	sdm.tipBranchingToMigration = 1.0 / 4.0;
	sdm.tipBranchingToProliferation = 1.0 / 4.0;
	sdm.tipBranchingToBranching = 1.0 / 4.0;
	
	sdm.stalkQuiescentToQuiescent = 1.0 / 3.0;
	sdm.stalkQuiescentToProliferation = 1.0 / 3.0;
	sdm.stalkQuiescentToBranching = 1.0 / 3.0;
	sdm.stalkElongationToQuiescent = 1.0 / 3.0;
	sdm.stalkElongationToProliferation = 1.0 / 3.0;
	sdm.stalkElongationToBranching = 1.0 / 3.0;
	sdm.stalkProliferationToQuiescent = 1.0 / 3.0;
	sdm.stalkProliferationToProliferation = 1.0 / 3.0;
	sdm.stalkProliferationToBranching = 1.0 / 3.0;
	sdm.stalkBranchingToQuiescent = 1.0 / 3.0;
	sdm.stalkBranchingToProliferation = 1.0 / 3.0;
	sdm.stalkBranchingToBranching = 1.0 / 3.0;
	
	//	sdm.rearQuiescentToQuiescent = 1.0 / 3.0;
	//	sdm.rearQuiescentToProliferation = 1.0 / 3.0;
	//	sdm.rearQuiescentToBranching = 1.0 / 3.0;
	//	sdm.rearProliferationToQuiescent = 1.0 / 3.0;
	//	sdm.rearProliferationToProliferation = 1.0 / 3.0;
	//	sdm.rearProliferationToBranching = 1.0 / 3.0;
	//	sdm.rearBranchingToQuiescent = 1.0 / 3.0;
	//	sdm.rearBranchingToProliferation = 1.0 / 3.0;
	//	sdm.rearBranchingToBranching = 1.0 / 3.0;
	//	System.out.println("[EnvironmentgetStateDiagramModel] *** Forced model");
	
	sdm.tipQuiescentToQuiescent = 1.11;
	sdm.tipQuiescentToMigration = 1.12;
	sdm.tipQuiescentToProliferation = 1.13;
	sdm.tipQuiescentToBranching = 1.14;
	sdm.tipMigrationToQuiescent = 1.21;
	sdm.tipMigrationToMigration = 1.22;
	sdm.tipMigrationToProliferation = 1.23;
	sdm.tipMigrationToBranching = 1.24;
	sdm.tipProliferationToQuiescent = 1.31;
	sdm.tipProliferationToMigration = 1.32;
	sdm.tipProliferationToProliferation = 1.33;
	sdm.tipProliferationToBranching = 1.34;
	sdm.tipBranchingToQuiescent = 1.41;
	sdm.tipBranchingToMigration = 1.42;
	sdm.tipBranchingToProliferation = 1.43;
	sdm.tipBranchingToBranching = 1.44;
	
	sdm.stalkQuiescentToQuiescent = 2.11;
	sdm.stalkQuiescentToProliferation = 2.12;
	sdm.stalkQuiescentToBranching = 2.13;
	sdm.stalkElongationToQuiescent = 2.21;
	sdm.stalkElongationToProliferation = 2.22;
	sdm.stalkElongationToBranching = 2.23;
	sdm.stalkProliferationToQuiescent = 2.31;
	sdm.stalkProliferationToProliferation = 2.32;
	sdm.stalkProliferationToBranching = 2.33;
	sdm.stalkBranchingToQuiescent = 2.41;
	sdm.stalkBranchingToProliferation = 2.42;
	sdm.stalkBranchingToBranching = 2.43;
	
	//	sdm.rearQuiescentToQuiescent = 3.11;
	//	sdm.rearQuiescentToProliferation = 3.12;
	//	sdm.rearQuiescentToBranching = 3.13;;
	//	sdm.rearProliferationToQuiescent = 3.21;
	//	sdm.rearProliferationToProliferation = 3.22;
	//	sdm.rearProliferationToBranching = 3.23;
	//	sdm.rearBranchingToQuiescent = 3.31;
	//	sdm.rearBranchingToProliferation = 3.32;
	//	sdm.rearBranchingToBranching = 3.33;

	return sdm;
    }



}