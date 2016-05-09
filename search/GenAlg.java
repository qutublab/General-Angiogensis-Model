
/*
 * 6-24-2011 Model scoring incorporates branch length
 *
 * 8-22-2011 Changed getScore to use individualLimitedXYBranchLengthsMicrons
 *
 * 9-6-2011 Timestamp and random seed passed as parameters to initialize method
 *
 * 9-21-2011 All created models are saved to individual files
 */

package search;

import shared.*;

import java.util.*;
import java.io.*;

public class GenAlg //implements GenAlgInterface
{
    private static final String STATS_FILE_PREFIX = "gaStats";
    private static final String MODEL_FILE_PREFIX = "ga";

    
        /**
         * @param args
         */
    
        static double allowableError = 0;
    
        public int numIterations = 1;
    static public int maxIterations = 40; //30; //20; //10; // 100;
    static public int popSize = 20; //20; //100;
	static public double fractionKept = 0.5;
	static RandomInterface rand;
        public static int genomeSize = 28;    //37;
	public static double mutationFactor;
	
	public static double maxAOrBThreshold = 1.0;
	public static double minAOrBThreshold = 0.5;
	public static double maxMutationFactor = 0.1;
	public static double minMutationFactor = 0.05;
	public static double exponentialScalingFactor = 2;

    static public int maxNumberOfModels;
    static public boolean saveModels = true;
    static public int savedModelCount = 0;
    static public File dir = null;
    static public String modelOutputName;

    /*
     * The exponential constants are used for implementing exponential
     * growth/decay in the choice of a parent and the amount of
     * mutation during the update of a population.  It is used in the
     * following equation:
     *
     * N(t) = N(0) e^(t/c)
     *
     * where: N is the quantity undegoing decay
     *        N(t) is the quantity at time t
     *        N(0) is the initial quantity
     *        c is the growth/decay constant
     *
     * For decay the equation usually uses -t instead of t, but we use
     * this equation for both growth and decay, and hence for decay, c
     * < 0.
     *
     * N(0) is provided above as minAOrBThreshold and
     * maxMutationfactor.  When t is maxIterations, we want to have
     * grown to maxAorBThreshold and decayed to minMutationfactor.
     *
     * In general if we want N(T) = V:
     *
     * V = N(0) e^(T/c)
     *
     * ln(V/N(0)) = T/c
     *
     * c = T/ln(V/N(0))
     *
     */ 
        public double expParentConstant;
        public double expMutationConstant;

    static final int NUMBER_OF_INITIAL_CONDITIONS = 
	StateDiagramModelResult.InitialConditions.values().length;
    static {
	if (NUMBER_OF_INITIAL_CONDITIONS != 6) {
	    die("[GenAlg] Update the GenAlg class to support "
		+ NUMBER_OF_INITIAL_CONDITIONS + " initial conditions");
	}
    }


    static final double GROWTH_WEIGHT = 1;
    static final double BRANCH_COUNT_WEIGHT = 1;
    static final double BRANCH_LENGTH_WEIGHT = 1;
    

    //  All numbers are for 24 hours of growth
    static double[] averageGrowth = new double[NUMBER_OF_INITIAL_CONDITIONS];
    static double[] stdDevGrowth = new double[NUMBER_OF_INITIAL_CONDITIONS];
    static double[] averageBranching = new double[NUMBER_OF_INITIAL_CONDITIONS];
    static double[] stdDevBranching = new double[NUMBER_OF_INITIAL_CONDITIONS];
    static double[] averageBranchLength = new double[NUMBER_OF_INITIAL_CONDITIONS];
    static double[] stdDevBranchLength = new double[NUMBER_OF_INITIAL_CONDITIONS];

    static {
        averageGrowth[StateDiagramModelResult.InitialConditions.V0B0.ordinal()] = 110.92;
	averageGrowth[StateDiagramModelResult.InitialConditions.V50B0.ordinal()] = 668.33; 
	averageGrowth[StateDiagramModelResult.InitialConditions.V25B50.ordinal()] = 1035.28; 
	averageGrowth[StateDiagramModelResult.InitialConditions.V0B50.ordinal()] = 258.28;
	averageGrowth[StateDiagramModelResult.InitialConditions.V0B100.ordinal()] = 345.68;
	averageGrowth[StateDiagramModelResult.InitialConditions.V25B25.ordinal()] = 670.58; 
	
	stdDevGrowth[StateDiagramModelResult.InitialConditions.V0B0.ordinal()] = 46.79;
	stdDevGrowth[StateDiagramModelResult.InitialConditions.V50B0.ordinal()] = 334.43;
	stdDevGrowth[StateDiagramModelResult.InitialConditions.V25B50.ordinal()] =423.28;
	stdDevGrowth[StateDiagramModelResult.InitialConditions.V0B50.ordinal()] = 167.61;
	stdDevGrowth[StateDiagramModelResult.InitialConditions.V0B100.ordinal()] = 240.35;
	stdDevGrowth[StateDiagramModelResult.InitialConditions.V25B25.ordinal()] = 264.25;
	
	
	
	averageBranching[StateDiagramModelResult.InitialConditions.V0B0.ordinal()] = 0.4;
	averageBranching[StateDiagramModelResult.InitialConditions.V50B0.ordinal()] = 3.4545;
	averageBranching[StateDiagramModelResult.InitialConditions.V25B50.ordinal()] = 4;  // 4.7272 is for 48 hours!
	averageBranching[StateDiagramModelResult.InitialConditions.V0B50.ordinal()] =  0.7273;
	averageBranching[StateDiagramModelResult.InitialConditions.V0B100.ordinal()] =  1.1667;
	averageBranching[StateDiagramModelResult.InitialConditions.V25B25.ordinal()] = 3.0000;
	
	stdDevBranching[StateDiagramModelResult.InitialConditions.V0B0.ordinal()] = 0.5164;
	stdDevBranching[StateDiagramModelResult.InitialConditions.V50B0.ordinal()] = 2.8413;
	stdDevBranching[StateDiagramModelResult.InitialConditions.V25B50.ordinal()] = 2.4494;
	stdDevBranching[StateDiagramModelResult.InitialConditions.V0B50.ordinal()] = 0.9045;
	stdDevBranching[StateDiagramModelResult.InitialConditions.V0B100.ordinal()] = 0.9374;
	stdDevBranching[StateDiagramModelResult.InitialConditions.V25B25.ordinal()] = 1.6997;


	// Branch length averages and standard deviations come from
	// SimulationImages/Results/Excel_Charts/Spheroid_Branch_Measurements
	// in Dropbox

	averageBranchLength[StateDiagramModelResult.InitialConditions.V0B0.ordinal()] = 8.774;
	averageBranchLength[StateDiagramModelResult.InitialConditions.V50B0.ordinal()] = 19.3848837;
	averageBranchLength[StateDiagramModelResult.InitialConditions.V25B50.ordinal()] = 19.1418868;
	averageBranchLength[StateDiagramModelResult.InitialConditions.V0B50.ordinal()] =  25.9115385;
	averageBranchLength[StateDiagramModelResult.InitialConditions.V0B100.ordinal()] = 30.4468182;
	averageBranchLength[StateDiagramModelResult.InitialConditions.V25B25.ordinal()] = 13.2596078;
	
	stdDevBranchLength[StateDiagramModelResult.InitialConditions.V0B0.ordinal()] = 2.46109822;
	stdDevBranchLength[StateDiagramModelResult.InitialConditions.V50B0.ordinal()] = 10.2889861;
	stdDevBranchLength[StateDiagramModelResult.InitialConditions.V25B50.ordinal()] = 10.7720834;
	stdDevBranchLength[StateDiagramModelResult.InitialConditions.V0B50.ordinal()] = 28.1297366;
	stdDevBranchLength[StateDiagramModelResult.InitialConditions.V0B100.ordinal()] = 44.2938077;
	stdDevBranchLength[StateDiagramModelResult.InitialConditions.V25B25.ordinal()] = 8.40843623;


	//	System.out.println("[GenAlg] Using averages instead of standard deviations for scoring");
	//	stdDevGrowth = averageGrowth;
	//	stdDevBranching = averageBranching;
    }
	

    //	static double[] averageGrowth = {110.92, 668.33, 1035.28, 258.28, 345.68, 670.58}; //average total growth, um, 24 hrs
    //	static double[] averageBranching = {0.4, 3.4545, 4.7272, 0.7273, 1.1667, 3.0000}; //average total branching, um, 24 hrs
	

    static boolean decayDisabled = false;
    static boolean limitedXYStats = false;
    static StateDiagramModelResult.InitialConditions singleInitialCondition = null;
    static boolean averageAsDenominator = false;
    static boolean inVitroZero = false;

    private PrintStream statsStream;



    public static void setMaxIterations(int maxIterations) {
	GenAlg.maxIterations = maxIterations;
	System.out.println("[GenAlg.setMaxIterations] maxIterations set to "
			   + maxIterations);
    }

    public static void disableDecay() {
	decayDisabled = true;
	System.out.println("[GenAlg.disableDecay] decay disabled");
    }

    public static void useLimitedXYStats() {
	limitedXYStats = true;
	System.out.println("[GenAlg.useLimitedXYStats] using limited XY stats");
    }

    public static void useSingleInitialCondition(StateDiagramModelResult.InitialConditions initCond) {
	singleInitialCondition = initCond;
	allowableError =
	    allowableError
	    / StateDiagramModelResult.InitialConditions.values().length;
	System.out.println("[GenAlg.useSingleInitialCondition] using single initial condition: "
			   + initCond);
    }

    public static void useInVitroZero() {
	inVitroZero = true;
	System.out.println("[GenAlg.useInVitroZero] Forcing in vitro measurements to 0 in all dimensions");
    }


    public static void useAverageAsDenominator() {
	averageAsDenominator = true;
	System.out.println("[GenAlg.useAverageAsDeniminator] using average instead of std dev as denominator in score computations ");
    }


    

    //    public static String createTimeStamp(Calendar cal) {
    //	int year = cal.get(Calendar.YEAR);
    //	// month is 0-based
    //	int month = cal.get(Calendar.MONTH) + 1;
    //	int day = cal.get(Calendar.DAY_OF_MONTH);
    //	int hour = cal.get(Calendar.HOUR_OF_DAY);
    //	int minute = cal.get(Calendar.MINUTE);
    //
    //	String timeStamp = "";
    //	timeStamp += year;
    //	timeStamp += month < 10? "0" + month : month;
    //	timeStamp += day < 10? "0" + day : day;
    //	timeStamp += hour < 10? "0" + hour : hour;
    //	timeStamp += minute < 10? "0" + minute : minute;
    //
    //	return timeStamp;
    //    }


    public void initialize(RandomInterface rand) {
	initialize(rand, 0, null, "");
    }
    
    
    public void initialize(RandomInterface rand,
			   long randomSeed,
			   File dir,
			   String timeStamp) {
	GenAlg.rand = rand;
	GenAlg.dir = dir;

	//	Calendar cal = Calendar.getInstance();
	//	    String timeStamp = createTimeStamp(cal);
	String statsFileName = STATS_FILE_PREFIX + timeStamp;
	modelOutputName = MODEL_FILE_PREFIX + timeStamp;
	if (singleInitialCondition != null) {
	    statsFileName += singleInitialCondition;
	    modelOutputName += singleInitialCondition;
	}
	File f;
	if (dir == null) {
	    f = new File(statsFileName);
	}
	else {
	    f = new File(dir, statsFileName);
	}

	statsStream = null;	
	try {
	    statsStream = new PrintStream(f);
	}
	catch (Exception e) {
	    die("[GenAlg.initialize] Unable to open stream for file "
		+ statsFileName + "     " + e.toString());
	}
	statsStream.println("randomSeed=" + randomSeed);
	
	
	//createPop();
	//cull(modelResult);
	
	expParentConstant =
	    (double) maxIterations / Math.log(maxAOrBThreshold / minAOrBThreshold);
	expMutationConstant =
	    maxIterations / Math.log(minMutationFactor / maxMutationFactor);
	
	// Calculate the maximum number of models that will be
	// created.  For the first generation, popSize models are
	// created.  For each subsequent generation, ((1 -
	// fractionKept) * popSize) models are created.
	maxNumberOfModels =
	    popSize
	    + ((maxIterations - 1)
	       * ((int)Math.round((1 - fractionKept) * popSize))); 
    }
    

    private static int numberOfDigits(int n) {
	if (n < 0) {
	    die("[GenAlg.length] argument " + n
		+ " is a negative number");
	}
	int len = 1;
	while (n > 9) {
	    n = n / 10;
	    len++;
	}
	return len;
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



	public GenAlg()
	{
		
	}
	


    public ArrayList<StateDiagramModelResult> createPop(String initialModelsDirectoryName) {
	if (initialModelsDirectoryName.equals("")) {
	    return createPop();
	}

	File initModelsDir = new File(initialModelsDirectoryName);
	if (!initModelsDir.isDirectory()) {
	    die("[GenAlg.createPop] " + initialModelsDirectoryName
		+ " is not a directory");
	}
	ArrayList<StateDiagramModelResult> initialModels =
	    new ArrayList<StateDiagramModelResult>();
	File[] dirContents = initModelsDir.listFiles();
	for (File f : dirContents) {
	    if (!f.isFile()) {
		System.err.println("[GenAlg.createPop] ignoring " + f.getName()
				   + " because it is not a file");
		continue;
	    }
	    boolean successfulRead = true;
	    Object obj = null;
	    try {
		FileInputStream fis = new FileInputStream(f);
		ObjectInputStream ois = new ObjectInputStream(fis);
		obj = ois.readObject();
		ois.close();
	    }
	    catch (Exception e) {
		successfulRead = false;
		System.err.println("[GenAlg.createPop] Ignoring "
				   + f.getName()
				   + " because it cannot be read   "
				   + e.toString());
	    }
	    if (successfulRead) {
		String className = obj.getClass().getName();
		if (className.equals("shared.StateDiagramModelResult")) {
		    StateDiagramModelResult sdmr =
			(StateDiagramModelResult) obj;
		    initialModels.add(sdmr);
		}
		else {
		    System.err.println("[GenAlg.createPop] Ignoring "
				       + f.getName()
				       + " because object in file is of class "
				       + className);
		}
	    }
	}
	if (initialModels.size() != popSize) {
	    die("[GenAlg.createPop] Found " + initialModels.size()
		+ " model files in directory " + initialModelsDirectoryName
		+ "; but expected " + popSize);
	}
	statsStream.println("[GenAlg.createPop] Initial models from directory "
			    + initialModelsDirectoryName);
	return initialModels;
    }





	//method 1: create initial population; return arrayList of state diagram model results
	public ArrayList<StateDiagramModelResult> createPop()
	{
		ArrayList<StateDiagramModelResult> result = new ArrayList<StateDiagramModelResult>();
		for(int i=0; i<popSize; i++)
		{
			StateDiagramModelResult res = new StateDiagramModelResult();
			res.allowableError = allowableError;
			res.numIterations = numIterations;
			res.maxIterations = maxIterations;
			res.popSize = popSize; 
			res.fractionKept = fractionKept;
			//res.rand = rand;
			res.genomeSize = genomeSize;    
			res.mutationFactor = mutationFactor;
			res.maxAOrBThreshold = maxAOrBThreshold;
			res.minAOrBThreshold = minAOrBThreshold;
			res.maxMutationFactor = maxMutationFactor;
			res.minMutationFactor = minMutationFactor;
			res.exponentialScalingFactor = exponentialScalingFactor;
			res.averageGrowth = averageGrowth;
			res.averageBranching = averageBranching;
			
			double[]genome = new double[genomeSize];
			
			for(int j=0; j<genomeSize; j++)
			{
				genome[j] = rand.nextDouble();
			}
			//double[]genomex = genome;
			
			genome = normalizeGenome(genome);
			//System.out.println(Arrays.toString(genome));
			//tip cell reassignments
			res.model.tipQuiescentToQuiescent = genome[0];
			res.model.tipQuiescentToMigration = genome[1];
			res.model.tipQuiescentToProliferation = genome[2];
			res.model.tipQuiescentToBranching = genome[3];
			
			res.model.tipMigrationToQuiescent = genome[4];
			res.model.tipMigrationToMigration = genome[5];
			res.model.tipMigrationToProliferation = genome[6];
			res.model.tipMigrationToBranching = genome[7];
			
			res.model.tipProliferationToQuiescent = genome[8];
			res.model.tipProliferationToMigration = genome[9];
			res.model.tipProliferationToProliferation = genome[10];
			res.model.tipProliferationToBranching = genome[11];
			
			res.model.tipBranchingToQuiescent = genome[12];
			res.model.tipBranchingToMigration = genome[13];
			res.model.tipBranchingToProliferation = genome[14];
			res.model.tipBranchingToBranching = genome[15];
			
			//stalk cell reassignments
			res.model.stalkElongationToQuiescent = genome[16];
			res.model.stalkElongationToProliferation = genome[17];
			res.model.stalkElongationToBranching = genome[18];
			
			res.model.stalkProliferationToQuiescent = genome[19];
			res.model.stalkProliferationToProliferation = genome[20];
			res.model.stalkProliferationToBranching = genome[21];
			
			res.model.stalkBranchingToQuiescent = genome[22];
			res.model.stalkBranchingToProliferation = genome[23];
			res.model.stalkBranchingToBranching = genome[24];
			
			res.model.stalkQuiescentToQuiescent = genome[25];
			res.model.stalkQuiescentToProliferation = genome[26];
			res.model.stalkQuiescentToBranching = genome[27];
			
			//rear cell reassignments
			//			res.model.rearQuiescentToQuiescent = genome[28];
			//			res.model.rearQuiescentToProliferation = genome[29];
			//			res.model.rearQuiescentToBranching = genome[30];
			//			
			//			res.model.rearProliferationToQuiescent = genome[31];
			//			res.model.rearProliferationToProliferation = genome[32];
			//			res.model.rearProliferationToBranching = genome[33];
			//
			//			res.model.rearBranchingToQuiescent = genome[34];
			//			res.model.rearBranchingToProliferation = genome[35];
			//			res.model.rearBranchingToBranching = genome[36];
			
			//int sum = res.model.tipQuiescentToQuiescent + res.model.tipQuiescentToMigration + res.model.tipQuiescentToMigration
			//repeat for every other variable in StateDiagramModel;
			//then, normalize each set to 1
			result.add(res);
		}
		return result;
	}
	

    private static void testSort() {
	System.out.println("Begin testSort");
	int scoreLimit = 10;
	long randomSeed = System.currentTimeMillis();
	myRandom rand = new myRandom(randomSeed);
	System.out.println("[GenAlg.testSort] randomSeed=" + randomSeed);

	ArrayList<StateDiagramModelResult> sdmrList = new ArrayList<StateDiagramModelResult>();
	for (int i = 0; i < 100; i++) {
	    StateDiagramModelResult sdmr = new StateDiagramModelResult();
	    sdmr.score = rand.nextInt(scoreLimit);
	    sdmrList.add(sdmr);
	}

	Collections.sort(sdmrList);
	//  sdmrList should now be sorted from least to greatest
	//  score.  This means that each score is not less than the
	//  score before it.  Note that the minimum score is 0 due to
	//  the use of rand.nextInt above.
	double lastScore = -1;
	for (Iterator<StateDiagramModelResult> i = sdmrList.iterator(); i.hasNext();) {
	    StateDiagramModelResult sdmr = i.next();
	    if (sdmr.score < lastScore) {
		die("[GenAlg.testSort] scores out of order: score " + lastScore
		    + " precedes score " + sdmr.score);
	    }
	    lastScore = sdmr.score;
	}
	System.out.println("testSort passed!");
    }


        private static void testUpdatePop() {
	System.out.println("Begin testUpdatePop");
	myRandom rand = new myRandom(0);
	GenAlg ga = new GenAlg();
	ga.initialize(rand);
	ga.averageBranching = new double[]{20, 20, 20, 20, 20, 20};
	ga.averageGrowth = new double[]{100, 100, 100, 100, 100, 100};
	ga.averageBranchLength = new double[]{70, 70, 70, 70, 70, 70};

	allowableError = 1;

	int numberOfCases = 6;

	int numberOfModels = 100;

	StateDiagramModelResult expectedModelResult = null;


	boolean oldLimitedXYStats = limitedXYStats;
	limitedXYStats = true;

	ArrayList<StateDiagramModelResult> sdmrList =
	    new ArrayList<StateDiagramModelResult>();
	for (int i = 0; i < numberOfModels; i++) {
	    StateDiagramModelResult sdmr = new StateDiagramModelResult();
	    sdmr.stats = new SimulationStats[numberOfCases];
	    for (int j = 0; j < numberOfCases; j++) {
		// put best model at the end
		if (i == (numberOfModels - 1)) {
		    sdmr.stats[j] =
			new SimulationStats(ga.averageBranching[j],
					    ga.averageGrowth[j],
					    ga.averageBranchLength[j]);
		    expectedModelResult = sdmr;
		}
		else {
		    sdmr.stats[j] = new SimulationStats(1, 0, 0);
		}
	    }
	    sdmrList.add(sdmr);

	    //	    getScore(sdmr);
	    //	    System.out.print(i + ":  ");
	    //	    System.out.println(sdmr.score);
	}

	GenAlg.saveModels = false;
	StateDiagramModelResult result = ga.updatePop(sdmrList);

	limitedXYStats = oldLimitedXYStats;

	if (result != null && result.model != expectedModelResult.model) {
	    StateDiagramModel.disableToString();
	    die("[GenAlg.testUpdatePop] upDatePop did not recognize acceptable model (" +
		expectedModelResult.model + "); instead returned: " + result.model
		+ "\nallowableError=" + allowableError
		+ "\nExpected StateDiagramModelResult object: " + expectedModelResult);
	}

	System.out.println("testUpdatePop passed!");

    }

    private static void testUpdatePop2() {
	System.out.println("Begin testUpdatePop2 ");
	GenAlg ga = new GenAlg();
	ga.maxIterations = 10;
	ga.popSize = 50;
	ga.fractionKept = .5;
	ga.allowableError = .1;
	ga.averageBranching = new double[]{100, 100, 100, 100, 100, 100};
	ga.averageGrowth = new double[]{10, 10, 10, 10, 10, 10};
	long seed = System.currentTimeMillis();
	myRandom rand = new myRandom(seed);
	System.out.println("[GenAlg.testUpdatePop2] random seed: " + seed);
	ga.initialize(rand);
	
	ArrayList<StateDiagramModelResult> sdmrArrList = ga.createPop();
	ArrayList<StateDiagramModelResult> savedSdmrArrList =
	    (ArrayList<StateDiagramModelResult>) sdmrArrList.clone();
	int numberOfCases = 1;
	int firstSurvivingIndex = ga.popSize / 2;
	int index = 0;
	int goodScoreIndex = 0;
	StateDiagramModel[] goodScoreSDM =
	    new StateDiagramModel[ga.popSize - firstSurvivingIndex];
	StateDiagramModel[] goodScoreSDM2 =
	    new StateDiagramModel[ga.popSize - firstSurvivingIndex];
	for (Iterator<StateDiagramModelResult> i = sdmrArrList.iterator();
	     i.hasNext();) {
	    StateDiagramModelResult sdmr = i.next();
	    sdmr.stats = new SimulationStats[numberOfCases];
	    for (int j = 0; j < sdmr.stats.length; j++) {
		sdmr.stats[j] = new SimulationStats();
		sdmr.stats[j].average.individualLimitedXYBranchLengthsMicrons =
		    new LinkedList<Double>();
		// Force a fraction of the models to have better
		// scores, but not good enough to terminate the search
		if (index >= firstSurvivingIndex) {
		    sdmr.stats[j].average.branchCount =
			ga.averageBranching[j] * 1.9;
		    sdmr.stats[j].average.sproutLengthMicrons =
			ga.averageGrowth[j] * 1.9;
		    sdmr.stats[j].average.individualLimitedXYBranchLengthsMicrons.add(50.0);
			
		    // Assume that updatePop does not replace/create
		    // new StateDiagramModel objects for those models
		    // that are kept and used to fill the next
		    // generation.  Thus we can save the good models
		    // and check that they are preserved after the
		    // population is updated .
		    goodScoreSDM[goodScoreIndex] = sdmr.model;
		    goodScoreSDM2[goodScoreIndex] = sdmr.model;
		    goodScoreIndex++;
		}
		else {
		    sdmr.stats[j].average.individualLimitedXYBranchLengthsMicrons.add(0.0);
		}
	    }
	    index++;
	}

	StateDiagramModel.disableToString();

	GenAlg.saveModels = false;
	ga.updatePop(sdmrArrList);
	verifyPop(sdmrArrList, ga);

	if (sdmrArrList.size() != ga.popSize) {
	    die("[GenAlg.testUpdate2] Updated population is size "
		+ sdmrArrList.size() + "; expected size " + ga.popSize);
	}
	// Assume that the models kept from the last generation are
	// still at the front of sdmrArrList.  Look at the models at
	// the beginning of the updated population, and check that
	// they are the same as the ones in goodScoreSDM (different
	// order okay).
	for (int i = 0; i < goodScoreSDM.length; i++) {
	    StateDiagramModel sdm = sdmrArrList.get(i).model;
	    if (sdm == null) {
		die("[GenAlg.testUpdatePop2] No model in updated population at index: "
		    + i);
	    }
	    boolean found = false;
	    for (int j = 0; j < goodScoreSDM.length && !found; j++) {
		if (sdm == goodScoreSDM[j]) {
		    found = true;
		    goodScoreSDM[j] = null;
		}
	    }
	    if (!found) {
		die("[GenAlg.testUpdatePop2] Possible problem: Model at index " + i
		    + " of updated population is not a low score model");
	    }
	}
	// Check that the rest of sdmrArrList contains none of the
	// models at the front of sdmrArrList
	for (int i = goodScoreSDM2.length; i < ga.popSize; i ++) {
	    StateDiagramModel sdm = sdmrArrList.get(i).model;
	    boolean found = false;
	    for (int j = 0; j < goodScoreSDM2.length; j++) {
		if (sdm == goodScoreSDM2[j]) {
		    die("[GenAlg.testUpdatePop2] Possible problem: (assumed) new model at index "
			+ i + " already occurs in updated population");
		}
	    }
	}

	// updatePop has been called once.  Check its maxIterations
	// stopping condition
	for (int i = 2; i < ga.maxIterations; i++) {
	    for (Iterator<StateDiagramModelResult> j = sdmrArrList.iterator();
		 j.hasNext();) {
		StateDiagramModelResult sdmr = j.next();
		sdmr.stats = new SimulationStats[numberOfCases];
		for (int k = 0; k < numberOfCases; k++) {
		    sdmr.stats[k] = new SimulationStats();
		}
	    }
	    GenAlg.saveModels = false;
	    StateDiagramModelResult sdmr = ga.updatePop(sdmrArrList);
	    verifyPop(sdmrArrList, ga);

	    if (sdmr != null) {
		die("[GenAlg.testUpdatePop2] premature end of search: " + sdmr);
	    }
	}
	for (Iterator<StateDiagramModelResult> j = sdmrArrList.iterator();
	     j.hasNext();) {
	    StateDiagramModelResult sdmr = j.next();
	    sdmr.stats = new SimulationStats[numberOfCases];
	    for (int k = 0; k < numberOfCases; k++) {
		sdmr.stats[k] = new SimulationStats();
	    }
	}
	
	GenAlg.saveModels = false;
	if (ga.updatePop(sdmrArrList) == null) {
	    die("[GenAlg.testUpdatePop2] updatePop does not signal termination by returning a noin-null value");
	}
	verifyPop(sdmrArrList, ga);
	System.out.println("testUpdatePop2 passed!");
    }
	


    private double computeExponentialChange(double n0, double c, int iteration) {
	if (decayDisabled) {
	    return n0;
	}
	double d = n0 * Math.exp(iteration / c);
	if (Double.isNaN(d)) {
	    die("[genAlg.computeExponentialChange] n0=" + n0 + " c=" + c + " iteration="
		+ iteration + " does not yield a number");
	}
	return d;

    }

    private double computeBetterParentProbability(int iteration) {
	return computeExponentialChange(minAOrBThreshold, expParentConstant, iteration);
    }
    
    private double computeMutationFactor(int iteration) {
	return computeExponentialChange(maxMutationFactor, expMutationConstant, iteration);
    }
    
    private static void testComputeExponentialChange() {
	System.out.println("Begin testComputeExponentialChange");
	myRandom rand = new myRandom(0);
	GenAlg ga = new GenAlg();
	ga.minAOrBThreshold = 0.5;
	ga.maxAOrBThreshold = 1.0;
	ga.minMutationFactor = .05;
	ga.maxMutationFactor = .75;
	ga.maxIterations = 100;
	ga.initialize(rand);

	//	for (int i = 0; i <= maxIterations; i++) {
	//	    System.out.println("[GenAlg.testComputeExponentialChange] computeParentAProbability(" + i
	//			       + ")=" + ga.computeParentAProbability(i));
	//	}
	//	for (int i = 0; i <= maxIterations; i++) {
	//	    System.out.println("[GenAlg.testComputeExponentialChange] computeParentMutationFactor(" + i
	//			       + ")=" + ga.computeMutationFactor(i));
	//	}
	

	double prob = ga.computeBetterParentProbability(0);
	double tolerance = .00000000001;
	if (Math.abs(prob - ga.minAOrBThreshold) > tolerance) {
	    die("[GenAlg.testComputeExponentialChange] computeBetterParentProbability(0)=" + prob
		+ ";  expected: " + ga.maxAOrBThreshold);
	}
	prob = ga.computeBetterParentProbability(maxIterations);
	if (Math.abs(prob - ga.maxAOrBThreshold) > tolerance) {
	    die("[GenAlg.testComputeExponentialChange] computeParentBetterProbability(" + maxIterations
		+")="
		+ prob + ";  expected: " + ga.minAOrBThreshold);
	}
	int halfway = maxIterations / 2;
	prob = ga.computeBetterParentProbability(halfway);
	if (prob > ga.maxAOrBThreshold + tolerance || prob < ga.minAOrBThreshold - tolerance) {
	    die("[GenAlg.computeComputeExponentialChange] computeBetterParentProbability("  + halfway
		+")="
		+ prob + "  expected a value between " + ga.minAOrBThreshold + " and "
		+ ga.maxAOrBThreshold);
	}

	double factor = ga.computeMutationFactor(0);
	if (Math.abs(factor - ga.maxMutationFactor) > tolerance) {
	    die("[GenAlg.testComputeExponentialChange] computeMutationFactor(0)=" + factor
		+ ";  expected: " + ga.maxMutationFactor);
	}
	factor = ga.computeMutationFactor(maxIterations);
	if (Math.abs(factor - ga.minMutationFactor) > tolerance) {
	    die("[GenAlg.testComputeExponentialChange] computeMutationFactor(" + maxIterations
		+ ")="
		+ factor + ";  expected: " + ga.minMutationFactor);
	}
	factor = ga.computeMutationFactor(halfway);
	if (factor > ga.maxMutationFactor + tolerance
	    || factor < ga.minMutationFactor - tolerance) {
	    die("[GenAlg.testComputeExponentialChange] computeMutationFactor("  + halfway +")="
		+ factor + "  expected a value between " + ga.minMutationFactor + " and "
		+ ga.maxMutationFactor);
	}
	System.out.println("testComputeExponentialChange passed!");
    }


    private static void displayGenome(double[] genome) {
	for (int i = 0; i + 3 < 16; i += 4) {
	    for (int j = i; j < i + 4; j++) {
		System.out.println("[" + j + "] = " + genome[j]);
	    }
	    System.out.println("--------");
	}
	for (int i = 16; i + 2 < 28; i += 3) {
	    for (int j = i; j < i + 3; j++) {
		System.out.println("[" + j + "] = " + genome[j]);
	    }
	    System.out.println("--------");
	}
    }

    //    static {
    //	System.out.println("[GenAlg.updatePop] updatePop disabled");
    //    }


    public int getIterationNumber() {
	return numIterations;
    }


    public StateDiagramModelResult updatePop(ArrayList<StateDiagramModelResult> oldResults) {
	

	// Start disable
	//	numIterations++;
	//	System.out.println(numIterations);
	//	for(int i=0; i<oldResults.size(); i++) {
	//	    oldResults.get(i).scoreAlreadyCalculated = false;
	//	}
	//	if (true) {
	//	    if (numIterations < maxIterations) {
	//		return null;
	//	    }
	//	    else {
	//		return oldResults.get(0);
	//	    }
	//	}
	// End disable


	int length = numberOfDigits(maxNumberOfModels);
	for(int i=0; i<oldResults.size(); i++) {
	    //	    System.out.print(i + ": ");
	    StateDiagramModelResult sdmr = oldResults.get(i);
	    if (sdmr.scoreAlreadyCalculated) {
		continue;
	    }
	    //	    getScore(oldResults.get(i));
	    getScore(sdmr);
	    if (saveModels) {
		String outputFileName =
		    modelOutputName + "." + pad(savedModelCount, length);
		File outputFile = new File(dir, outputFileName);
		sdmr.write(outputFile);
		savedModelCount++;
	    }
	}
	
	Collections.sort(oldResults);
	//	StateDiagramModel.disableToString();
	//	System.out.println(numIterations);
	String stats =
	    "Iteration number: " +  numIterations + "  best score: " 
	    + oldResults.get(0).score +  "    worst score: "
	    + oldResults.get(popSize - 1).score;
	statsStream.println(stats);
	System.out.println(stats);
	for(int i=0; i<oldResults.size(); i++)
	{
	    //		System.out.println(oldResults.get(i));
	}
	
	//if the score is within 1 of the actual (0), then return it. else return null
	if(oldResults.get(0).score <= allowableError || numIterations >= maxIterations) {
	    numIterations++;
	    StateDiagramModelResult sdmr = oldResults.get(0);
	    sdmr.averageInVitroGrowth = averageGrowth;
	    sdmr.averageInVitroBranching = averageBranching;
	    statsStream.close();
	    // Note that at this point, oldResults is sorted and is
	    // expected to be so by the caller
	    return oldResults.get(0);
	}
	else {
	    int firstNewModelIndex = (int) Math.round(fractionKept * popSize);
    
	    double betterParentProbability =
		computeBetterParentProbability(numIterations);
	    double mutationFactor = computeMutationFactor(numIterations);
	    //ArrayList<StateDiagramModelResult> newPop = new ArrayList<StateDiagramModelResult>();
	    for(int i = firstNewModelIndex; i < popSize; i++) {	
		// Select two surviving models (i.e. models with
		// indices less than firstNewModelIndex)
		int indexA = GenAlg.rand.nextInt(firstNewModelIndex);
		int indexB = GenAlg.rand.nextInt(firstNewModelIndex - 1);
		
		//keeps repeats from happening
		if(indexA == indexB) {
		    indexB = firstNewModelIndex - 1;
		}
		
		StateDiagramModelResult parentA = oldResults.get(indexA);
		StateDiagramModelResult parentB = oldResults.get(indexB);

		StateDiagramModelResult kid =
		    createModel(parentA, parentB,
				betterParentProbability, mutationFactor,
				numIterations);
		
		//rear cell reassignments
		//				kid.model.rearQuiescentToQuiescent = genome[28];
		//				kid.model.rearQuiescentToProliferation = genome[29];
		//				kid.model.rearQuiescentToBranching = genome[30];
		//				
		//				kid.model.rearProliferationToQuiescent = genome[31];
		//				kid.model.rearProliferationToProliferation = genome[32];
		//				kid.model.rearProliferationToBranching = genome[33];
		//
		//				kid.model.rearBranchingToQuiescent = genome[34];
		//				kid.model.rearBranchingToProliferation = genome[35];
		//				kid.model.rearBranchingToBranching = genome[36];
		
		//		oldResults.add(kid);
		oldResults.set(i, kid);
	    }
	    //oldResults.addAll(newPop);			
	    numIterations++;
	    return null;
	}
    }
    

    public static StateDiagramModelResult createModel(StateDiagramModelResult parentA,
						      StateDiagramModelResult parentB,
						      double betterParentProbability,
						      double mutationFactor,
						      int numIterations) {
	double[] genome =
	    createGenome(parentA, parentB,
			 betterParentProbability, mutationFactor);

	StateDiagramModelResult kid = new StateDiagramModelResult();
	kid.model = arrayToModel(genome);
	
	kid.allowableError = allowableError;
	kid.numIterations = numIterations;
	kid.maxIterations = maxIterations;
	kid.popSize = popSize; 
	kid.fractionKept = fractionKept;
	kid.genomeSize = genomeSize;    
	kid.mutationFactor = mutationFactor;
	kid.maxAOrBThreshold = maxAOrBThreshold;
	kid.minAOrBThreshold = minAOrBThreshold;
	kid.maxMutationFactor = maxMutationFactor;
	kid.minMutationFactor = minMutationFactor;
	kid.exponentialScalingFactor = exponentialScalingFactor;
	kid.averageGrowth = averageGrowth;
	kid.averageBranching = averageBranching;
	return kid;
    }
    

    private static double[] createGenome(StateDiagramModelResult parentA,
					 StateDiagramModelResult parentB,
					 double betterParentProbability,
					 double mutationFactor) {
	StateDiagramModelResult betterParent;
	StateDiagramModelResult worseParent;
	if (parentA.score < parentB.score) {
	    betterParent = parentA;
	    worseParent = parentB;
	}
	else {
	    betterParent = parentB;
	    worseParent = parentA;
	}
	
	double [] betterGenome = assembleGenome(betterParent);
	double [] worseGenome = assembleGenome(worseParent);
	
	double [] genome = new double[genomeSize];
	
	boolean[] valueFromBetterParent = new boolean[genomeSize];
	for(int j=0;j<genomeSize;j++) {	
	    double r = rand.nextDouble();
	    if (r < betterParentProbability) {
		genome[j] = betterGenome[j];
		valueFromBetterParent[j] = true;
	    }
	    else {
		genome[j] = worseGenome[j];
		valueFromBetterParent[j] = false;
	    }
	    double mutationExtreme = mutationFactor * genome[j];
	    genome[j] =
		(genome[j] - mutationExtreme) + (rand.nextDouble() * 2 * mutationExtreme);
	    //		    genome[j] += (rand.nextDouble()/mutationFactor) - (mutationFactor/2);
	}
	
	// Check for 0 probability sums
	for (int j = 0; j + 3 < 16; j += 4) {
	    double sum = genome[j] + genome[j + 1] + genome[j + 2] + genome[j + 3];
	    if (sum == 0) {
		boolean nonZeroFound = false;
		for (int k = j; k < j + 4 && !nonZeroFound; k++) {
		    double replacement = valueFromBetterParent[k]? worseGenome[k] : betterGenome[k];
		    if (replacement != 0) {
			genome[k] = replacement;
			nonZeroFound = true;
		    }
		}
		if (!nonZeroFound) {
		    die("[GenAlg.createGenome] Unable to create genome starting at index "
			+ j);
		}
	    }
	}
	for (int j = 16; j + 2 < 28; j += 3) {
	    double sum = genome[j] + genome[j + 1] + genome[j + 2];
	    if (sum == 0) {
		boolean nonZeroFound = false;
		for (int k = j; k < j + 3 && !nonZeroFound; k++) {
		    double replacement = valueFromBetterParent[k]? worseGenome[k] : betterGenome[k];
		    if (replacement != 0) {
			genome[k] = replacement;
			nonZeroFound = true;
		    }
		}
		if (!nonZeroFound) {
		    die("[GenAlg.createGenome] Unable to create genome starting at index "
			+ j);
		}
	    }
	}

	genome = normalizeGenome(genome);
	return genome;
    }
	
       
    private static void testCreateGenome() {
	System.out.println("Begin testCreateGenome");
	if (genomeSize != 28) {
	    die("[GenAlg.testCreateGenome] genomeSize: " + genomeSize
		+ " is not 28!");
	}
	double[] genomeA = new double[genomeSize];
	double[] genomeB = new double[genomeSize];
	for (int i = 0; i < 16; i++) {
	    if (i % 4 == 0) {
		genomeA[i] = 1;
	    }
	    if (i % 4 == 1) {
		genomeB[i] = 1;
	    }
	}
	for (int i = 16; i < 28; i++) {
	    if (i % 3 == 0) {
		genomeA[i] = 1;
	    }
	    if (i % 3 == 1) {
		genomeB[i] = 1;
	    }
	}
	
	StateDiagramModelResult parentA = new StateDiagramModelResult();
	StateDiagramModelResult parentB = new StateDiagramModelResult();
	parentA.model = arrayToModel(genomeA);
	parentB.model = arrayToModel(genomeB);
	parentA.score = 1;
	parentB.score = 2;
	double[] genome;
	genome = createGenome(parentA, parentB, 0, 0);
	for (int i = 0; i < genomeSize; i++) {
	    if (genome[i] != genomeB[i]) {
		die("Fail: genome[" + i + "]=" + genome[i]
		    + "  genomeB[" + i + "]=" + genomeB[i]);
	    }
	}
	genome = createGenome(parentA, parentB, 1, 0);
	for (int i = 0; i < genomeSize; i++) {
	    if (genome[i] != genomeA[i]) {
		die("Fail: genome[" + i + "]=" + genome[i]
		    + "  genomeA[" + i + "]=" + genomeA[i]);
	    }
	}
	System.out.println("testCreateGenome passed!");
    }


    private static StateDiagramModel arrayToModel(double[] genome) {
	StateDiagramModel sdm = new StateDiagramModel();

	//tip cell reassignments
	sdm.tipQuiescentToQuiescent = genome[0];
	sdm.tipQuiescentToMigration = genome[1];
	sdm.tipQuiescentToProliferation = genome[2];
	sdm.tipQuiescentToBranching = genome[3];
	
	sdm.tipMigrationToQuiescent = genome[4];
	sdm.tipMigrationToMigration = genome[5];
	sdm.tipMigrationToProliferation = genome[6];
	sdm.tipMigrationToBranching = genome[7];
	
	sdm.tipProliferationToQuiescent = genome[8];
	sdm.tipProliferationToMigration = genome[9];
	sdm.tipProliferationToProliferation = genome[10];
	sdm.tipProliferationToBranching = genome[11];
	
	sdm.tipBranchingToQuiescent = genome[12];
	sdm.tipBranchingToMigration = genome[13];
	sdm.tipBranchingToProliferation = genome[14];
	sdm.tipBranchingToBranching = genome[15];
	
	//stalk cell reassignments
	sdm.stalkElongationToQuiescent = genome[16];
	sdm.stalkElongationToProliferation = genome[17];
	sdm.stalkElongationToBranching = genome[18];
	
	sdm.stalkProliferationToQuiescent = genome[19];
	sdm.stalkProliferationToProliferation = genome[20];
	sdm.stalkProliferationToBranching = genome[21];
	
	sdm.stalkBranchingToQuiescent = genome[22];
	sdm.stalkBranchingToProliferation = genome[23];
	sdm.stalkBranchingToBranching = genome[24];
	
	sdm.stalkQuiescentToQuiescent = genome[25];
	sdm.stalkQuiescentToProliferation = genome[26];
	sdm.stalkQuiescentToBranching = genome[27];
	
	return sdm;
    }


    public static void getScore(StateDiagramModelResult state)
    {
	//double score = 0;
	if (state.scoreAlreadyCalculated) {
	    return;
	}
	state.score = 0;
	if (singleInitialCondition == null) {
	    for(int i=0; i<state.stats.length; i++)
		{
		    state.score += getScore(state, i);
		}
	}
	else {
	    System.out.println("[GenAlg.getScore] Scoring only for "
			       + singleInitialCondition);
	    state.score = getScore(state, singleInitialCondition.ordinal());
	}
	state.score = Math.sqrt(state.score);
	state.scoreAlreadyCalculated = true;
	//	System.out.println("score=" + state.score);
	//return score;
    }
    

    private static void testGetScore2() {
	System.out.println("Begin testGetScore2");
	myRandom rand = new myRandom(0);
	GenAlg ga = new GenAlg();
	ga.initialize(rand);
	ga.averageBranching = new double[]{20, 20, 20, 20, 20, 20};
	ga.averageGrowth = new double[]{100, 100, 100, 100, 100, 100};
	StateDiagramModelResult sdmr = new StateDiagramModelResult();
	SimulationStats[] stats = new SimulationStats[6];
	sdmr.stats = stats;
	for (int i = 0; i < stats.length; i++) {
	    stats[i] =
		new SimulationStats(10 + i, 10 * (10 + i), 100 * (10 + i)); 
	}
	ga.getScore(sdmr);
	double expectedScore = 0;
	for (int i = 0; i < stats.length; i++) {
	    expectedScore +=
		(Math.pow((sdmr.stats[i].average.branchCount - ga.averageBranching[i]), 2)
		 / Math.pow(ga.stdDevBranching[i], 2))
		+ (Math.pow((sdmr.stats[i].average.sproutLengthMicrons - ga.averageGrowth[i]), 2)
		   / Math.pow(ga.stdDevGrowth[i], 2));
	}
	expectedScore = Math.sqrt(expectedScore);
	if (sdmr.score != expectedScore) {
	    die("[GenAlg.testGetScore2] score: " + sdmr.score + " expected score: "
		+ expectedScore);
	}
	System.out.println("testGetScore2 passed!");

    }


    private static void testGetScore() {
	System.out.println("Begin testGetScore");
	myRandom rand = new myRandom(0);
	GenAlg ga = new GenAlg();
	ga.initialize(rand);
	//	ga.averageBranching = new double[]{10, 10, 10, 10, 10, 10};
	//	ga.averageGrowth = new double[]{50, 50, 50, 50, 50, 50};
	StateDiagramModelResult sdmr = new StateDiagramModelResult();
	SimulationStats[] stats = new SimulationStats[6];
	sdmr.stats = stats;
	for (int i = 0; i < stats.length; i++) {
	    stats[i] =
		new SimulationStats(10 + i, 10 * (10 + i), 100 * (10 + i)); 
	}
	boolean oldLimitedXYStats = limitedXYStats;
	limitedXYStats = true;
	for (int i = 0; i < stats.length; i++) {
	    double score = ga.getScore(sdmr, i);
	    double branchCountComp =
		Math.pow((sdmr.stats[i].average.limitedXYBranchCount
			  - ga.averageBranching[i])
			 / ga.stdDevBranching[i],
			 2);
	    double sproutLengthComp =
		Math.pow((sdmr.stats[i].average.limitedXYSproutLengthMicrons
			  - ga.averageGrowth[i])
			 / ga.stdDevGrowth[i],
			 2);
	    double branchLengthComp =
		Math.pow((sdmr.stats[i].average.individualLimitedXYBranchLengthsMicrons.getFirst()
			  - ga.averageBranchLength[i])
			 / ga.stdDevBranchLength[i],
			 2);
	    //	    double branchLengthComp =
	    //		Math.pow(((sdmr.stats[i].average.limitedXYBranchLengthMicrons
	    //			   / sdmr.stats[i].average.limitedXYBranchCount)
	    //			  - ga.averageBranchLength[i])
	    //			 / ga.stdDevBranchLength[i],
	    //			 2);
	    double expectedScore = 
		branchCountComp + sproutLengthComp + branchLengthComp;
		
	    //	    System.out.println("branchCountComp=" + branchCountComp
	    //			       + " sproutLengthComp=" + sproutLengthComp
	    //			       + " branchLengthComp=" + branchLengthComp);
	    if (score != expectedScore) {
		limitedXYStats = oldLimitedXYStats;
		die("[GenAlg.testGetScore] score: " + score
		    + " expected score: " + expectedScore);
	    }
	}
	limitedXYStats = oldLimitedXYStats;
	System.out.println("testGetScore passed!");
    }


    public static double getScore(StateDiagramModelResult state, int index)
    {
	BasicStats average = state.stats[index].average;

	double simulationBranchCount;
	double simulationSproutLength;
	double simulationBranchLength = 0;


	if (average.individualLimitedXYBranchLengthsMicrons.size() != 1) {
	    die("[GenSearch.getScore] average.individualLimitedXYBranchLengthsMicrons.size()="
		+ average.individualLimitedXYBranchLengthsMicrons.size());
	}

	if (limitedXYStats) {
	    simulationBranchCount = average.limitedXYBranchCount;
	    simulationSproutLength = average.limitedXYSproutLengthMicrons;
	    simulationBranchLength =
		average.individualLimitedXYBranchLengthsMicrons.getFirst();
	}
	else {
	    simulationBranchCount = average.branchCount;
	    simulationSproutLength = average.sproutLengthMicrons;
	}

	if (Double.isNaN(simulationBranchCount)) {
	    die("[GenAlg.getScore] simulationBranchCount is not a number!");
	}

	if (Double.isNaN(simulationSproutLength)) {
	    die("[GenAlg.getScore] simulationSproutLength is not a number!");
	}

	if (Double.isNaN(simulationBranchLength)) {
	    die("[GenAlg.getScore] simulationBranchLength is not a number!");
	}


	double branchCountPrecomponent =
	    (simulationBranchCount - averageBranching[index])
	    / (averageAsDenominator? averageBranching[index]: stdDevBranching[index]);
	
	double sproutLengthPrecomponent =
	    (simulationSproutLength - averageGrowth[index])
	    / (averageAsDenominator? averageGrowth[index]: stdDevGrowth[index]);

	double branchLengthPrecomponent = 0;
	if (limitedXYStats) {
	    branchLengthPrecomponent =
		(simulationBranchLength - averageBranchLength[index])
		/ (averageAsDenominator? averageBranchLength[index]: stdDevBranchLength[index]);
	}

	if (inVitroZero) {
	    branchCountPrecomponent = simulationBranchCount;
	    sproutLengthPrecomponent = simulationSproutLength;
	    if (limitedXYStats) {
		branchLengthPrecomponent =
		    (simulationBranchLength / simulationBranchCount); 
	    }
	}
	



	double branchCountComponent =
	    BRANCH_COUNT_WEIGHT * Math.pow(branchCountPrecomponent, 2);
	double sproutLengthComponent =
	    GROWTH_WEIGHT * Math.pow(sproutLengthPrecomponent, 2);
	double branchLengthComponent =
	    BRANCH_LENGTH_WEIGHT * Math.pow(branchLengthPrecomponent, 2);

	state.individualScores[index] =
	    new double[] {branchCountPrecomponent,
			  sproutLengthPrecomponent,
			  branchLengthPrecomponent};

	double score =
	    branchCountComponent + sproutLengthComponent
	    + branchLengthComponent;
	return score;
    }

    static {
	System.out.println("[GenAlg] Individual score component order: branch count, sprout length, branch length (positive amounts indicate simulation overages)");
    }



    public static double[] assembleGenome(StateDiagramModelResult kid)
    {
	//named 'kid' b/c i was too lazy to change it
	//tip cell reassignments
	double []genome = new double[genomeSize];
	genome[0] = kid.model.tipQuiescentToQuiescent;
	genome[1] = kid.model.tipQuiescentToMigration;
	genome[2] = kid.model.tipQuiescentToProliferation;
	genome[3] = kid.model.tipQuiescentToBranching;
	
	genome[4] = kid.model.tipMigrationToQuiescent;
	genome[5] = kid.model.tipMigrationToMigration;
	genome[6] = kid.model.tipMigrationToProliferation;
	genome[7] = kid.model.tipMigrationToBranching;
	
	genome[8] = kid.model.tipProliferationToQuiescent;
	genome[9] = kid.model.tipProliferationToMigration;
	genome[10] = kid.model.tipProliferationToProliferation;
	genome[11] = kid.model.tipProliferationToBranching;
	
	genome[12] = kid.model.tipBranchingToQuiescent;
	genome[13] = kid.model.tipBranchingToMigration;
	genome[14] = kid.model.tipBranchingToProliferation;
	genome[15] = kid.model.tipBranchingToBranching;
	
	//stalk cell reassignments
	genome[16] = kid.model.stalkElongationToQuiescent;
	genome[17] = kid.model.stalkElongationToProliferation;
	genome[18] = kid.model.stalkElongationToBranching;
	
	genome[19] = kid.model.stalkProliferationToQuiescent;
	genome[20] = kid.model.stalkProliferationToProliferation;
	genome[21] = kid.model.stalkProliferationToBranching;
	
	genome[22] = kid.model.stalkBranchingToQuiescent;
	genome[23] = kid.model.stalkBranchingToProliferation;
	genome[24] = kid.model.stalkBranchingToBranching;
	
	genome[25] = kid.model.stalkQuiescentToQuiescent;
	genome[26] = kid.model.stalkQuiescentToProliferation;
	genome[27] = kid.model.stalkQuiescentToBranching;
	
	//rear cell reassignments
	//		genome[28] = kid.model.rearQuiescentToQuiescent;
	//		genome[29] = kid.model.rearQuiescentToProliferation;
	//		genome[30] = kid.model.rearQuiescentToBranching;
	//		
	//		genome[31] = kid.model.rearProliferationToQuiescent;
	//		genome[32] = kid.model.rearProliferationToProliferation;
	//		genome[33] = kid.model.rearProliferationToBranching;
	//
	//		genome[34] = kid.model.rearBranchingToQuiescent;
	//		genome[35] = kid.model.rearBranchingToProliferation;
	//		genome[36] = kid.model.rearBranchingToBranching;
	return genome;
    }
	

    public static void testNormalizeGenome() {
	System.out.println("Begin testNormalizeGenome");
	GenAlg ga = new GenAlg();
	double[] genome = new double[ga.genomeSize];
	double[] expectedGenome = new double[ga.genomeSize];

	genome[0] = 1.1;
	genome[1] = 1.2;
	genome[2] = 1.3;
	genome[3] = 1.4;
	expectedGenome[0] = 0.22;
	expectedGenome[1] = 0.24;
	expectedGenome[2] = 0.26;
	expectedGenome[3] = 0.28;

	genome[4] = 4;
	genome[5] = 3;
	genome[6] = 2;
	genome[7] = 1;
	expectedGenome[4] = 0.4;
	expectedGenome[5] = 0.3;
	expectedGenome[6] = 0.2;
	expectedGenome[7] = 0.1;

	genome[8] = 2.8;
	genome[9] = 2.6;
	genome[10] = 2.4;
	genome[11] = 2.2;
	expectedGenome[8] = 0.28;
	expectedGenome[9] = 0.26;
	expectedGenome[10] = 0.24;
	expectedGenome[11] = 0.22;

	genome[12] = 10;
	genome[13] = 10;
	genome[14] = 10;
	genome[15] = 10;
	expectedGenome[12] = 0.25;
	expectedGenome[13] = 0.25;
	expectedGenome[14] = 0.25;
	expectedGenome[15] = 0.25;

	genome[16] = 3;
	genome[17] = 0;
	genome[18] = 0;
	expectedGenome[16] = 1.0;
	expectedGenome[17] = 0;
	expectedGenome[18] = 0;

	genome[19] = 0;
	genome[20] = 9;
	genome[21] = 0;
	expectedGenome[19] = 0;
	expectedGenome[20] = 1.0;
	expectedGenome[21] = 0;

	genome[22] = 0;
	genome[23] = 0;
	genome[24] = .2;
	expectedGenome[22] = 0;
	expectedGenome[23] = 0;
	expectedGenome[24] = 1.0;

	genome[25] = .1;
	genome[26] = .2;
	genome[27] = .1;
	expectedGenome[25] = .25;
	expectedGenome[26] = .5;
	expectedGenome[27] = .25;
	
	
	double[] normalizedGenome = ga.normalizeGenome(genome);
	if (normalizedGenome == null) {
	    die("[GenAlg.testNormalizeGenome] null returned");
	}
	if (normalizedGenome.length != expectedGenome.length) {
	    die("[GenAlg.testNormalizeGenome] returned array of size " + normalizedGenome.length
		+ " expected size " + expectedGenome.length);
	}
	double tolerance = .00000000001;
	for (int i = 0; i < expectedGenome.length; i++) {
	    if (Math.abs(normalizedGenome[i] - expectedGenome[i]) > tolerance) {
		die("[GenAlg.testNormalizeGenome] value at index " + i + " is "
		    + normalizedGenome[i] + "; expected " + expectedGenome[i]);
	    }
	}
	
	System.out.println("testNormalizeGenome passed!");

    }

    public static double[] normalizeGenome(double[] genome)
    {
	//correcting the tip cell edge weights to sum to 1
	for(int a=0; a<13; a+=4)
	    {
		double sum = genome[a] + genome[a+1] + genome[a+2] + genome[a+3];
		genome[a] = genome[a]/sum;
		genome[a+1] = genome[a+1]/sum;
		genome[a+2] = genome[a+2]/sum;
		genome[a+3] = genome[a+3]/sum;
		//System.out.println();
	    }
	//correcting the stalk cell edge weights to sum to 1
	for(int a=16; a<26; a+=3)
	    {
		double sum = genome[a] + genome[a+1] + genome[a+2];
		genome[a] = genome[a]/sum;
		genome[a+1] = genome[a+1]/sum;
		genome[a+2] = genome[a+2]/sum;
	    }
	
	//correcting the rear cell edge weights to sum to 1
	for(int a=28; a<genomeSize-2; a+=3)
	    {
		double sum = genome[a] + genome[a+1] + genome[a+2];
		genome[a] = genome[a]/sum;
		genome[a+1] = genome[a+1]/sum;
		genome[a+2] = genome[a+2]/sum;
	    }
	return genome;
    }
    
    
    public static void testCreatePop() {
	System.out.println("Begin testCreatePop");
	int testIterations = 100;
	long randomSeed = System.currentTimeMillis();
	myRandom rand = new myRandom(randomSeed);
	System.out.println("[GenAlg.testCreatePop] randomSeed=" + randomSeed);
	for (int i = 0; i < testIterations; i++) {
	    //	    System.out.println("[GenAlg.testCreatePop] begin test iteration: " + i);
	    testCreatePopOnce(rand);
	}
	System.out.println("testCreatePop passed!");
    }
	
    public static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }


    private static void verifyPop(ArrayList <StateDiagramModelResult> pop,
				  GenAlg g) {
	for(Iterator <StateDiagramModelResult> i = pop.iterator(); i.hasNext();) {
	    StateDiagramModelResult sdmr = i.next();
	    double sum;
	    double tolerance = 0.00000001;
	    
	    double[] genome = g.assembleGenome(sdmr);

	    for(int n=0;n<4;n++) {
		for (int j = 0; j < 4; j++) {
		    if (genome[(4 * n) + j] < 0) {
			die("[GenAlg.verifyPop] negative probability: " + genome[(4 * n) + j]
			    + " at n=" + n + " j=" + j);
		    }
		}
		sum = genome[4*n] + genome[4*n+1] + genome[4*n+2] + genome[4*n+3];
		if (Math.abs(sum-1)>tolerance) {
		    System.err.println("tolerance exceeded " + n + " " + sum);
		    System.err.printf("sum = %s + %s + %s + %s\n",
				      genome[4*n], genome[4*n+1], genome[4*n+2], genome[4*n+3]);
		    //		    displayGenome(genome);
		    System.exit(1);
		}
	    }
	    for (int n = 16; n <= 25; n += 3) {
		sum = 0;
		for (int j = 0; j < 3; j++) {
		    sum += genome[n + j];
		    if (genome[n + j] < 0) {
			die("[GenAlg.verifyPop] negative probability: "
			    + genome[n + j] + " at genome index: " + (n + j));
		    }
		}
		if (Math.abs(sum - 1) > tolerance) {
		    die("[GenAlg.verifyPop] "
			+ "Outgoing edge probabilities at genome indices " + n + " through "
			+ (n + 2) + " do not sum to 1: " + sum);
		    
		}		    
	    }	
	}
    }
	
    public static void testCreatePopOnce(myRandom rand)
    {
	//long seed = System.time
	//	popSize = 100;
	GenAlg g = new GenAlg();
	g.initialize(rand);
	ArrayList <StateDiagramModelResult> r =  g.createPop();
	if (r.size() != popSize) {
	    die("[GenAlg.testCreatePopOnce] Population is of size: " + r.size()
		+ "; expected size: " + popSize);
	}
	
	verifyPop(r, g);
    }
    

    //	public static void testUpdatePop()
    //	{
    //		//create dummy list of StateModelDiagramResults
    //		StateDiagramModelResult result = new StateDiagramModelResult();
    //		
    //	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("GenAlg");
		
		testCreatePop();
		testGetScore();
		testGetScore2();
		testSort();
		testComputeExponentialChange();
		testNormalizeGenome();
		testUpdatePop();
		testUpdatePop2();
		testCreateGenome();
		if (true) {return;}

		
		
		/*
		 * //method 2: takes arraylist of results, culls it, creates the next generation, passes that out for the model
				public StateDiagramModel cull(ArrayList<StateDiagramModelResult> results)
				{
					//sort, get the highest scoring/smallest error StateDiagramModel, if it's within X amount of the
					//in vitro results, pass it; otherwise, pass null + create the next generation
					//if you want to continue with the optimization, pass null; otherwise, pass the results
					
					
					//results.sort();
					//first, let's assume that they have been scored and sorted
					
					Collections.sort(results);
					//if the score is within 1 of the actual (0), then return it. else return null
					if(results.get(1).getScore()<=1)
						return results.get(1).model;
					else
					{
						results.subList((int) (GenAlg.fractionKept*results.size()), results.size()-1).clear();
						updatePop(results);
						return null;
						//NOW MUST MATE AND REINITIALIZE
					}
				}
		 */
		
		
		/*correcting the tip cell edge weights to sum to 1
		int sum = genome[0] + genome[1]  + genome[2] + genome[3];
		genome[0] = genome[0]/sum;
		genome[1] = genome[1]/sum;
		genome[2] = genome[2]/sum;
		genome[3] = genome[3]/sum;
		
		sum = genome[4] + genome[5]  + genome[6] + genome[7];
		genome[4] = genome[4]/sum;
		genome[5] = genome[5]/sum;
		genome[6] = genome[6]/sum;
		genome[7] = genome[7]/sum;
		
		sum = genome[8] + genome[9]  + genome[10] + genome[11];
		genome[8] = genome[8]/sum;
		genome[9] = genome[9]/sum;
		genome[10] = genome[10]/sum;
		genome[11] = genome[11]/sum;
		
		sum = genome[12] + genome[13]  + genome[14] + genome[15];
		genome[12] = genome[12]/sum;
		genome[13] = genome[13]/sum;
		genome[14] = genome[14]/sum;
		genome[15] = genome[15]/sum;
		
		//correcting the stalk cell edge weights to sum to 1
		sum = genome[16] + genome[17]  + genome[18];
		genome[16] = genome[16]/sum;
		genome[17] = genome[17]/sum;
		genome[18] = genome[18]/sum;
		
		sum = genome[19] + genome[20]  + genome[21];
		genome[19] = genome[19]/sum;
		genome[20] = genome[20]/sum;
		genome[21] = genome[21]/sum;
		
		sum = genome[22] + genome[23]  + genome[24];
		genome[22] = genome[22]/sum;
		genome[23] = genome[23]/sum;
		genome[24] = genome[24]/sum;
		
		//correcting the rear cell edge weights to sum to 1
		sum = genome[25] + genome[26]  + genome[27];
		genome[25] = genome[25]/sum;
		genome[26] = genome[26]/sum;
		genome[27] = genome[27]/sum;
		
		sum = genome[28] + genome[29]  + genome[30];
		genome[28] = genome[28]/sum;
		genome[29] = genome[29]/sum;
		genome[30] = genome[30]/sum;
		
		sum = genome[31] + genome[32]  + genome[33];
		genome[31] = genome[31]/sum;
		genome[32] = genome[32]/sum;
		genome[33] = genome[33]/sum;*/
		
		
		/*
		 * int[]scores = new int[results.size()];
		
		for(int i=0; i<results.size(); i++)
		{
			//replace with actual model output, fix averageGrowth and averageBranching
			scores[i] = ((results.get(i).branchCount - averageBranching)*(results.get(i).branchCount - averageBranching))/averageBranching
						+ (results.get(i).totalGrowth - averageGrowth)*(results.get(i).totalGrowth - averageGrowth)/averageGrowth;
		}
		Arrays.sort(scores);
		 */
	}

}
