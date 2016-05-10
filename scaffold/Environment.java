/*
 * 3-24-2011 Number of initial sprouts now tracked as a statistic.
 *
 * 4-17-2011 Simulate method calls SimulationStats2.computeStats to
 * compute average and std dev.
 */



/*
 * information to embed in file name:
 *   rule pattern
 *   simulated time
 *   Vegf/bdnf
 *   implementation version
 *  date
 */


/*
 * command line arguments
 *   parameters file name
 *   rule set class name
 *   concentrations manager class name
 */


package scaffold;


import concentrations.*;
//import angiogenesisMPMB.*;

import sim.engine.*;
import sim.util.Int3D;
import ec.util.MersenneTwisterFast;

import shared.*;
import sharedMason.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

import spheroid.*;

import search.*;

public class Environment extends SimState implements EnvironmentInterface {

    private final static String VERSION_STRING = "0.1";
    
    private final static String SPHERE_FILE_PREFIX = "sphere";
    public final static String SPROUT_FILE_PREFIX = "sprout";
    public final static String LOG_FILE_PREFIX = "log";

    private final static String COMMENT_PREFIX = "// ";

    private static final int SECONDS_PER_HOUR = 60 * 60;



    // relative order of processing within a time step; smaller
    // numbers done before larger.
    public static final int TIP_CELL_ORDER = 0;
    public static final int CELL_ORDER = 1;
    public static final int CONCENTRATION_ORDER = 2;
    public static final int LAST_ORDER = 10;

    // capturing sprout growth is the last thing to do in a time-step
    public static final int GROWTH_CAPTURE_ORDER = 3;

    private static final double EVERY_STEP_INTERVAL = 1.0;

    //    public static final double MIGRATION_MAGNITUDE_FACTOR = 1;


    //    private String parametersFileName;
    //    private String ruleSetClassName;
    //    private Environment env;

    private SimulationMode simMode = SimulationMode.NORMAL;


    SimpleGrid grid;



    PublicRandom publicRandom;

    private String parametersFileName;
    private String ruleSetClassName;     // includes package name
    private String concentrationsManagerClassName; // includes package name

    private boolean printStatsPerTimeStep;
    private long randomSeed;
    private int simulationLengthInTimeSteps;
    private double timeStepLengthInSeconds;
    private double voxelXOrigin;
    private double voxelYOrigin;
    private double voxelZOrigin;
    private double voxelLengthMicrons;
    private int voxelGridMinX;
    private int voxelGridMaxX;
    private int voxelGridMinY;
    private int voxelGridMaxY;
    private int voxelGridMinZ;
    private int voxelGridMaxZ;
    private String logFileName;
    private int inhibitionRange;
    private boolean dll4Present;
    private String concentrationsParametersFileName;
    private String angiogenesisParametersFileName;
    private String guiParametersFileName;
    private int growthCaptureInterval;
    private boolean outputGeometryPrecisionSpecified;
    private int outputGeometryPrecision;
    private boolean ignoreDiscretizedSprouts;
    private double spheroidDiameterMicrons;
    private double sphereCellDiameterMicrons;
    //    private double minimumInitialCellRadiusMicrons;
    //    private double maximumInitialCellRadiusMicrons;
    //    private double minimumCellRadiusFactor;
    //    private double maximumCellRadiusFactor;
    private double initialSproutRadiusMicrons;
    //    private double minimumCellRadiusMicrons;
    //    private double maximumCellRadiusMicrons;
    //    private double maximumCellLengthMicrons;
    private double initialStalkCellLengthMicrons;
    private double initialTipCellLengthMicrons;

    private double minimumCellLengthToWidthRatio;
    private double maximumCellLengthToWidthRatio;
    private int lengthToWidthRatioTransitionDistanceCells;

    //    private double minimumTipCellLengthToWidthRatio;
    //    private double maximumTipCellLengthToWidthRatio;

    private double maximumElongationFactor;

    private double minimumDivisionLengthMicrons;
    private double divisionProbabilityConstant1;
    private double divisionProbabilityConstant2;

    private int numberOfSproutColors;

    private double sproutPrintRangeLo;
    private double sproutPrintRangeHi;

    private boolean tipCellPrecedence;

    private Point3D spheroidCenter;

    private LinkedList<LinkedList<Node>> cellDataList;

    //    private spheroid.Sphere[] sphereData;
    private Cell[] spheroidCells;

    private LinkedList<Cell> registeredCellList = new LinkedList<Cell>();

    private RuleSetInterface ruleSet;

    private ConcentrationsInterface concentrationsManager;

    public LogStream log;

    private Calendar cal;

    private OutputBuffer buffer;

    private double totalSimulationHours;

    private boolean writeOutputFiles = true;

    private StateDiagramModel stateDiagramModel = null;

    private String timeStamp;

    private int registeredCellCount = 0;

    private long currentStepNumber = 0;


    public static boolean GenSearchFlag = false;

    public static boolean debugFlag = false;
    public static boolean debugFlag2 = false;

    private GrowthCapture growthCapture;


    private StateDiagramModelResult.InitialConditions forcedInitialConditions = null;

    public Environment(long seed) {
	super(seed);
	publicRandom = new PublicRandom(random);
    }




    // for test purposes only
    private static LinkedList<Cell> testCellList;

    /*
    public Environment(long randomSeed,
		       String parametersFileName,
		       String ruleSetClassName) {
	super(randomSeed);
	//	this.parametersFileName = parametersFileName;
	this.ruleSetClassName = ruleSetClassName;
    }
    */


    //    public Environment(Parameters p, MersenneTwisterFast random) {
    //	super(random);
    //	p.setRandomSeedSource(Parameters.RandomSeedSource.CONTINUATION);
    //	continueConstructingEnvironment(p);
    //    }

    public Environment(Parameters p, long randomSeedOverride) {
	super(randomSeedOverride);
	continueConstructingEnvironment(p, randomSeedOverride);
    }
    
    public Environment(Parameters p) {
	super(p.getRandomSeed());
	continueConstructingEnvironment(p, p.getRandomSeed());
    }

    public Environment(Parameters p, long randomSeedOverride, StateDiagramModel sdm,
		       StateDiagramModelResult.InitialConditions conc) {
	super(randomSeedOverride);
	stateDiagramModel = sdm;
	simMode = SimulationMode.GENETIC_ALGORITHM;
	forcedInitialConditions = conc;
	continueConstructingEnvironment(p, randomSeedOverride);
	//	System.out.println("[Environment] Simulation mode is " + simMode);
    }


    public void continueConstructingEnvironment(Parameters p, long randomSeed) {
	//	super(p.getRandomSeed());	
	publicRandom = new PublicRandom(random);
	logFileName = p.getLogFileName();
	int logDetailLevel = p.getLogDetailLevel();
	boolean echoLog = p.getEchoLogFlag();
	log = new LogStream(logFileName, echoLog, logDetailLevel);
	cal = Calendar.getInstance();
	timeStamp = createTimeStamp(cal);
	log.println(cal.getTime().toString(), LogStreamInterface.MINIMUM_LOG_DETAIL);
	this.randomSeed = randomSeed; //p.getRandomSeed();
	log.println("Random seed: " + randomSeed, // + " (" + p.getRandomSeedSource() + ")",
		    LogStreamInterface.MINIMUM_LOG_DETAIL);

	printStatsPerTimeStep = p.printStatsPerTimeStep();
	ruleSetClassName = p.getRuleSetClassName();
	concentrationsManagerClassName = p.getConcentrationsManagerClassName();
	simulationLengthInTimeSteps = p.getSimulationLengthInTimeSteps();
	timeStepLengthInSeconds = p.getTimeStepLengthInSeconds();
	voxelXOrigin = p.getVoxelXOrigin();
	voxelYOrigin = p.getVoxelYOrigin();
	voxelZOrigin = p.getVoxelZOrigin();
	voxelLengthMicrons = p.getVoxelLengthMicrons();
	voxelGridMinX = p.getVoxelGridMinX();
	voxelGridMaxX = p.getVoxelGridMaxX();
	voxelGridMinY = p.getVoxelGridMinY();
	voxelGridMaxY = p.getVoxelGridMaxY();
	voxelGridMinZ = p.getVoxelGridMinZ();
	voxelGridMaxZ = p.getVoxelGridMaxZ();
	inhibitionRange = p.getInhibitionRange();
	dll4Present = p.getDll4Flag();
	concentrationsParametersFileName = p.getConcentrationsParametersFileName();
	angiogenesisParametersFileName = p.getAngiogenesisParametersFileName();
	guiParametersFileName = p.getGuiParametersFileName();
	growthCaptureInterval = p.getGrowthCaptureInterval();
	outputGeometryPrecisionSpecified = p.outputGeometryPrecisionSpecified();
	outputGeometryPrecision = p.getOutputGeometryPrecision();
	ignoreDiscretizedSprouts = p.ignoreDiscretizedSprouts();
	spheroidDiameterMicrons = p.getSpheroidDiameterMicrons();
	sphereCellDiameterMicrons = p.getSphereCellDiameterMicrons();
	spheroidCenter = new Point3D(0, 0, 0);
	//	minimumInitialCellRadiusMicrons = p.getMinimumInitialCellRadiusMicrons();
	//	maximumInitialCellRadiusMicrons = p.getMaximumInitialCellRadiusMicrons();
	//	minimumCellRadiusFactor = p.getMinimumCellRadiusFactor();
	//	maximumCellRadiusFactor = p.getMaximumCellRadiusFactor();
	initialSproutRadiusMicrons = p.getInitialSproutRadiusMicrons();
	//	minimumCellRadiusMicrons = p.getMinimumCellRadiusMicrons();
	//	maximumCellRadiusMicrons = p.getMaximumCellRadiusMicrons();
	//	maximumCellLengthMicrons = p.getMaximumCellLengthMicrons();
	initialStalkCellLengthMicrons = p.getInitialStalkCellLengthMicrons();
	initialTipCellLengthMicrons = p.getInitialTipCellLengthMicrons();

	minimumCellLengthToWidthRatio = p.getMinimumCellLengthToWidthRatio();
	maximumCellLengthToWidthRatio = p.getMaximumCellLengthToWidthRatio();
	lengthToWidthRatioTransitionDistanceCells =
	    p.getLengthToWidthRatioTransitionDistanceCells();

	//	minimumTipCellLengthToWidthRatio =
	//	    p.getMinimumTipCellLengthToWidthRatio();
	//	maximumTipCellLengthToWidthRatio =
	//	    p.getMaximumTipCellLengthToWidthRatio();

	maximumElongationFactor = p.getMaximumElongationFactor();
	
	minimumDivisionLengthMicrons =
	    p.getMinimumDivisionLengthMicrons();
	divisionProbabilityConstant1 = p.getDivisionProbabilityConstant1();
	divisionProbabilityConstant2 = p.getDivisionProbabilityConstant2();

	numberOfSproutColors = p.getNumberOfSproutColors();
	double[] sproutPrintRange = p.getSproutPrintRange();
	sproutPrintRangeLo = sproutPrintRange[0];
	sproutPrintRangeHi = sproutPrintRange[1];


	if (p.getForcedInitialConditions() != null) {
	    forcedInitialConditions = p.getForcedInitialConditions();
	}

	//	cellDataList = p.getCellDataList();
	//	sphereData = p.getSphereData();
	//	System.out.println("[Environment] sphereData=" + sphereData);


	// **suffix-info**
	double hoursPerTimeStep = timeStepLengthInSeconds / SECONDS_PER_HOUR;
	totalSimulationHours = 
	    hoursPerTimeStep * simulationLengthInTimeSteps;

	buffer = new OutputBuffer();

	if (!ignoreDiscretizedSprouts) {
	    System.out.println("[scaffold.Environment.start] Creating grid...");
	}

	grid = new LazySimpleGrid(this);
	
       	ruleSet = (RuleSetInterface) createFromClassName(ruleSetClassName);
	
	concentrationsManager =
	    (ConcentrationsInterface) createFromClassName(concentrationsManagerClassName);
	


	concentrationsManager.initialize(this);

	ruleSet.initialize(this);

	tipCellPrecedence = ruleSet.tipCellsHavePrecedence();

	bufferParameters();


	Cell.initialize(this);
	Cell.setRuleSet(ruleSet);
	Cell.setConcentrationsManager(concentrationsManager);


	// createCells and registerGrowthCaptue schedule events
	createCells();


	// Always create and schedule GrowthCapture object.  If output
	// is not wanted, then use gc.disable().  This forces the
	// GrowthCapture object to always be scheduled and thus the
	// random number generator is used in the same way regardless
	// of growth being captured or not.
	growthCapture = new GrowthCapture(this);
	if (!writeOutputFiles) {
	    growthCapture.disable();
	}
	registerGrowthCapture(growthCapture);



	
    }

    private void bufferParameters() {
	buffer.println(cal.getTime().toString());
	buffer.println("Scaffold version: " + VERSION_STRING);
	buffer.println("Random seed " + randomSeed);
	double hoursPerTimeStep = timeStepLengthInSeconds / SECONDS_PER_HOUR;
	buffer.println(simulationLengthInTimeSteps + " x "
		       + hoursPerTimeStep
		       + "-hour time steps = " + totalSimulationHours
		       + " hours total simulated time.");
	buffer.println("Parameters file " + parametersFileName);
	buffer.println("Rule set class " + ruleSetClassName);
	buffer.println("Concentrations manager class " + concentrationsManagerClassName);
	buffer.println("Inhibition range " + inhibitionRange + " cells");
	buffer.println("Ignore discretized sprouts " + ignoreDiscretizedSprouts);
	buffer.println("Spheroid diameter " + spheroidDiameterMicrons + " microns");
	buffer.println("Sphere cell diameter " + sphereCellDiameterMicrons + " microns");
	//	buffer.println("Minimum initial cell radius " + minimumInitialCellRadiusMicrons + " microns");
	//	buffer.println("Maximum initial cell radius " + maximumInitialCellRadiusMicrons + " microns");
	//	buffer.println("Minimum cell radius factor " + minimumCellRadiusFactor);
	//	buffer.println("Maximum cell radius factor " + maximumCellRadiusFactor);
	//	buffer.println("Minimum cell radius " + minimumCellRadiusMicrons + " microns");
	//	buffer.println("Maximum cell radius " + maximumCellRadiusMicrons + " microns");
	//	buffer.println("Maximum cell length " + maximumCellLengthMicrons + " microns");
	buffer.println("Tip cell precedence " + tipCellPrecedence);

	
    }



    public void setStateDiagramModel(StateDiagramModel stateDiagramModel) {
	this.stateDiagramModel = stateDiagramModel;
    }


    public StateDiagramModel getStateDiagramModel() {
	return stateDiagramModel;
    }


    public StateDiagramModel createNeutralStateDiagramModel() {
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
	//
	//	System.out.println("[EnvironmentgetStateDiagramModel] *** Forced model");
	//	
	//	sdm.tipQuiescentToQuiescent = 1.11;
	//	sdm.tipQuiescentToMigration = 1.12;
	//	sdm.tipQuiescentToProliferation = 1.13;
	//	sdm.tipQuiescentToBranching = 1.14;
	//	sdm.tipMigrationToQuiescent = 1.21;
	//	sdm.tipMigrationToMigration = 1.22;
	//	sdm.tipMigrationToProliferation = 1.23;
	//	sdm.tipMigrationToBranching = 1.24;
	//	sdm.tipProliferationToQuiescent = 1.31;
	//	sdm.tipProliferationToMigration = 1.32;
	//	sdm.tipProliferationToProliferation = 1.33;
	//	sdm.tipProliferationToBranching = 1.34;
	//	sdm.tipBranchingToQuiescent = 1.41;
	//	sdm.tipBranchingToMigration = 1.42;
	//	sdm.tipBranchingToProliferation = 1.43;
	//	sdm.tipBranchingToBranching = 1.44;
	//	
	//	sdm.stalkQuiescentToQuiescent = 2.11;
	//	sdm.stalkQuiescentToProliferation = 2.12;
	//	sdm.stalkQuiescentToBranching = 2.13;
	//	sdm.stalkElongationToQuiescent = 2.21;
	//	sdm.stalkElongationToProliferation = 2.22;
	//	sdm.stalkElongationToBranching = 2.23;
	//	sdm.stalkProliferationToQuiescent = 2.31;
	//	sdm.stalkProliferationToProliferation = 2.32;
	//	sdm.stalkProliferationToBranching = 2.33;
	//	sdm.stalkBranchingToQuiescent = 2.41;
	//	sdm.stalkBranchingToProliferation = 2.42;
	//	sdm.stalkBranchingToBranching = 2.43;
	//	
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

    public int getSimulationLengthInTimeSteps() {
	return simulationLengthInTimeSteps;
    }



    public static String createTimeStamp(Calendar cal) {
	int year = cal.get(Calendar.YEAR);
	// month is 0-based
	int month = cal.get(Calendar.MONTH) + 1;
	int day = cal.get(Calendar.DAY_OF_MONTH);
	int hour = cal.get(Calendar.HOUR_OF_DAY);
	int minute = cal.get(Calendar.MINUTE);

	String timeStamp = "";
	timeStamp += year;
	timeStamp += month < 10? "0" + month : month;
	timeStamp += day < 10? "0" + day : day;
	timeStamp += hour < 10? "0" + hour : hour;
	timeStamp += minute < 10? "0" + minute : minute;

	return timeStamp;
    }

    //    private void setTimeStamp() {
    //	int year = cal.get(Calendar.YEAR);
    //	// month is 0-based
    //	int month = cal.get(Calendar.MONTH) + 1;
    //	int day = cal.get(Calendar.DAY_OF_MONTH);
    //	int hour = cal.get(Calendar.HOUR_OF_DAY);
    //	int minute = cal.get(Calendar.MINUTE);
    //
    //	timeStamp = "";
    //	timeStamp += year;
    //	timeStamp += month < 10? "0" + month : month;
    //	timeStamp += day < 10? "0" + day : day;
    //	timeStamp += hour < 10? "0" + hour : hour;
    //	timeStamp += minute < 10? "0" + minute : minute;
    //    }

    public String getTimeStamp() {
	return timeStamp;
    }

    public double getTotalSimulationHours() {
	return totalSimulationHours;
    }

    public String getOutputFileSuffix() {
	String suffix = "";
	int simHoursRounded = (int) Math.round(totalSimulationHours);
	if (simHoursRounded == totalSimulationHours) {
	    suffix += simHoursRounded;
	}
	else {
	    suffix += totalSimulationHours;
	}
	suffix += "H";
	
	double nominalVegf =
	    concentrationsManager.getNominalConcentration(EnvironmentInterface.ConcentrationType.VEGF);
	int roundedVegf = (int) Math.round(nominalVegf);
	double nominalBdnf =
	    concentrationsManager.getNominalConcentration(EnvironmentInterface.ConcentrationType.BDNF);
	int roundedBdnf = (int) Math.round(nominalBdnf);
	double nominalAng1 =
	    concentrationsManager.getNominalConcentration(EnvironmentInterface.ConcentrationType.ANG1);
	int roundedAng1 = (int) Math.round(nominalAng1);

	double nominalAng2 =
	    concentrationsManager.getNominalConcentration(EnvironmentInterface.ConcentrationType.ANG2);
	int roundedAng2 = (int) Math.round(nominalAng2);

	if (nominalVegf != 0) {
	    suffix += "vegf";
	    if (roundedVegf == nominalVegf) {
		suffix += roundedVegf;
	    }
	    else {
		suffix += nominalVegf;
	    }
	}
	if (nominalBdnf != 0) {
	    suffix += "bdnf";
	    if (roundedBdnf == nominalBdnf) {
		suffix += roundedBdnf;
	    }
	    else {
		suffix += nominalBdnf;
	    }
	}
	if (nominalAng1 != 0) {
	    suffix += "ang1";
	    if (roundedAng1 == nominalAng1) {
		suffix += roundedAng1;
	    }
	    else {
		suffix += nominalAng1;
	    }
	}
	if (nominalAng2 != 0) {
	    suffix += "ang2";
	    if (roundedAng2 == nominalAng2) {
		suffix += roundedAng2;
	    }
	    else {
		suffix += nominalAng2;
	    }
	}

	suffix += ruleSet.getRuleSetIdentifier();
	suffix += timeStamp;

	return suffix;
    }

    public void start() {
    }


    public void finish() {
	log.println(cal.getTime().toString(), LogStreamInterface.MINIMUM_LOG_DETAIL);
	log.close();
	super.finish();
    }

    public static void die(String message) {
	System.err.println(message);
	Throwable th = new Throwable();
	th.printStackTrace();
	System.exit(1);
    }



    public String getVersionString() {
	return VERSION_STRING;
    }
				     

    public boolean dll4IsPresent() {
	return dll4Present;
    }

    public String getConcentrationsParametersFileName() {
	return concentrationsParametersFileName;
    }

    public String getAngiogenesisParametersFileName() {
	return angiogenesisParametersFileName;
    }

    public String getGuiParametersFileName() {
	return guiParametersFileName;
    }

    public double getCollagenConcentration() {
	return concentrationsManager.getCollagenConcentration();
    }

    public LogStream getLog() {
	return log;
    }


    public RandomInterface getRandom() {
	return publicRandom;
    }


    public double getTimeStepLengthInSeconds() {
	return timeStepLengthInSeconds;
    }

    double getVoxelLengthMicrons() {
	return voxelLengthMicrons;
    }

    double getVoxelXOrigin() {
	return voxelXOrigin;
    }

    double getVoxelYOrigin() {
	return voxelYOrigin;
    }

    double getVoxelZOrigin() {
	return voxelZOrigin;
    }

    int getVoxelGridMinX() {
	return voxelGridMinX;
    }

    int getVoxelGridMinY() {
	return voxelGridMinY;
    }

    int getVoxelGridMinZ() {
	return voxelGridMinZ;
    }

    int getVoxelGridMaxX() {
	return voxelGridMaxX;
    }

    int getVoxelGridMaxY() {
	return voxelGridMaxY;
    }

    int getVoxelGridMaxZ() {
	return voxelGridMaxZ;
    }

    int getInhibitionRange() {
	return inhibitionRange;
    }

    
    public double getMigrationMagnitudeFactor() {
	//	die("[Environment.getMigrationMagnitudeFactor]");
	return 1;
    }

    public OutputBuffer getOutputBuffer() {
	return buffer;
    }


    public double getMinimumCellLengthToWidthRatio() {
	return minimumCellLengthToWidthRatio;
    }
    public double getMaximumCellLengthToWidthRatio() {
	return maximumCellLengthToWidthRatio;
    }

    public double getLengthToWidthRatioTransitionDistanceCells() {
	return lengthToWidthRatioTransitionDistanceCells;
    }

    //    public double getMinimumTipCellLengthToWidthRatio() {
    //	return minimumTipCellLengthToWidthRatio;
    //    }
    //    public double getMaximumTipCellLengthToWidthRatio() {
    //	return maximumTipCellLengthToWidthRatio;
    //    }

    public double getMaximumElongationFactor() {
	return maximumElongationFactor;
    }


    public double getMinimumDivisionLengthMicrons() {
	return minimumDivisionLengthMicrons;
    }

    public double getDivisionProbabilityConstant1() {
	return divisionProbabilityConstant1;
    }

    public double getDivisionProbabilityConstant2() {
	return divisionProbabilityConstant2;
    }

    public int getNumberOfSproutColors() {
	return numberOfSproutColors;
    }


    /*
    public void clearGrid(LinkedList<Int3D> voxels, Cell c) {
	for (Iterator<Int3D> i = voxels.iterator(); i.hasNext();) {
	    Int3D v = i.next();
	    grid.removeAll(v.x, v.y, v.z, c);
	}
    }
    */

    /*
    public boolean checkGrid(LinkedList<Int3D> voxels, Cell c) {
	boolean okay = true;
	for (Iterator<Int3D> i = voxels.iterator(); i.hasNext() && okay;) {
	    Int3D v = i.next();
	    Cell content = grid.get(v.x, v.y, v.z);
	    if (content != null && content != c) {
		okay = false;
	    }
	}
	return okay;
    }
    */

    /*
    public void addGrid(LinkedList<Int3D> voxels, Cell c) {
	for (Iterator<Int3D> i = voxels.iterator(); i.hasNext();) {
	    Int3D v = i.next();
	    grid.add(v.x, v.y, v.z, c);
	}
    }
    */
    /*
    public void setGrid(LinkedList<Int3D> voxels, Cell c) {
	for (Iterator<Int3D> j = voxels.iterator(); j.hasNext();) {
	    Int3D v = j.next();
	    
	    Cell content = grid.get(v.x, v.y, v.z);
	    if (content != null) {
		die("[Environment.setGrid] Overlap during placement at voxel location ("
		    + v.x + ", " + v.y + ", " + v.z + ") " + content
		    + "  " + c);
	    }
	    grid.set(v.x, v.y, v.z, c);
	}
    }
    */

    
    public void registerCell(Cell c) {
	c.setLocalStorage(ruleSet.createLocalStorage(c));
	//	Stoppable stopObject = schedule.scheduleRepeating(c);
	//	Stoppable stopObject =
	//	    schedule.scheduleRepeating(c, CELL_ORDER, EVERY_STEP_INTERVAL);
	boolean valid;
	if (c.isTipCell()) {
	    valid = schedule.scheduleOnce(c, TIP_CELL_ORDER);
	}
	else {
	    valid = schedule.scheduleOnce(c, CELL_ORDER);
	}
	if (!valid) {
	    die("[Environment.registerCell] Unable to schedule cell " + c);
	}
	// Since cells now reschedule themselves, stop objects are no longer created
	//	c.setStopObject(stopObject);
	registeredCellList.addLast(c);
	registeredCellCount++;
    }

    public void registerConcentration(ConcentrationsInterface c) {
	if (concentrationsManager != null) {
	    die("[Environment.registerConcentrationsManager] concentrations manager already registered");
	}
	//	Stoppable stopObject = schedule.scheduleRepeating(c);
	Stoppable stopObject = 
	    schedule.scheduleRepeating(c, CONCENTRATION_ORDER, EVERY_STEP_INTERVAL);
	c.setStopObject(stopObject);
	concentrationsManager = c;
    }


    public void registerGrowthCapture(GrowthCapture gc) {
	/*
	 * An interval of 1 is used to force the growth capture object
	 * to be scheduled for every time step even if growth is not
	 * to be captured.  This ensures the same pattern of use by
	 * the random number generator regardless of capturing.
	 */
	schedule.scheduleRepeating(gc, GROWTH_CAPTURE_ORDER, 1);
    }

    public ConcentrationsInterface getConcentrationsManager() {
	return concentrationsManager;
    }

    //    public Cell createOneCellOld(LinkedList<Node> nodeDataList) {
    //	Cell c = null; // new Cell(nodeDataList);
    //	LinkedList<Int3D> voxels = c.createRepresentation(this);
    //	//	addGrid(voxels, c);
    //	registerCell(c);
    //	//	c.setLocalStorage(ruleSet.createLocalStorage(c));
    //	//	Stoppable stopObject = schedule.scheduleRepeating(c);
    //	//	c.setStopObject(stopObject);
    //	return c;
    //	/*
    //	for (Iterator<Int3D> j = voxels.iterator(); j.hasNext();) {
    //	    Int3D v = j.next();
    //	    Cell content = grid.get(v.x, v.y, v.z);
    //	    if (content != null) {
    //		die("Overlap during initial placement at voxel location ("
    //		    + v.x + ", " + v.y + ", " + v.z + ") " + content
    //		    + "  " + c);
    //	    }
    //	    grid.set(v.x, v.y, v.z, c);
    //	}
    //	*/
    //
    //    }

    /*
     * Creates, places and schedules cells.
     */
    //    private void createCellsOld() {
    //	System.out.println("[Environment.createcells] creating testCellList for testing");
    //	testCellList = new LinkedList<Cell>();
    //	for (Iterator<LinkedList<Node>> i = cellDataList.iterator();
    //	     i.hasNext();) {
    //	    LinkedList<Node> nodeList = i.next();
    //	    //	    Cell c = createOneCell(nodeDataList);
    //	    Cell c = new Cell(nodeList);
    //	    registerCell(c);
    //	    testCellList.add(c);
    //	    /*
    //	    Cell c = new Cell(dArr);
    //	    c.setLocalStorage(ruleSet.createLocalStorage(c));
    //	    LinkedList<Int3D> voxels = c.createRepresentation(env);
    //	    for (Iterator<Int3D> j = voxels.iterator(); j.hasNext();) {
    //		Int3D v = j.next();
    //		Cell content = grid.get(v.x, v.y, v.z);
    //		if (content != null) {
    //		    die("Overlap during initial placement at voxel location ("
    //			+ v.x + ", " + v.y + ", " + v.z + ") " + content
    //			+ "  " + c);
    //		}
    //		grid.set(v.x, v.y, v.z, c);
    //	    }
    //	    Stoppable stopObject = schedule.scheduleRepeating(c);
    //	    c.setStopObject(stopObject);
    //	    */
    //	}
    //    }

    /*
     * Creates, places and schedules cells.
     */
    private void createCells() {
	spheroid.Sphere[] sphereData =
	    Spheroid.getSphereData(spheroidDiameterMicrons, sphereCellDiameterMicrons, buffer);
	spheroidCells = new Cell[sphereData.length];
	for (int i = 0; i < sphereData.length; i ++) {
	    /*
	     * Cells must be placed in the array in the order
	     * specified in sphereData.  This is because each Sphere
	     * object's neighbors list specifies neighboring cells by
	     * index.
	     */
	    Cell c = new Cell(sphereData[i]);
	    spheroidCells[i] = c;
	    registerCell(c);
	}

	// set each cell's neighbors
	for (int i = 0; i < sphereData.length; i ++) {
	    for (Iterator<Integer> j = sphereData[i].neighbors.iterator(); j.hasNext();) {
		int neighborIndex = j.next();
		spheroidCells[i].addSpheroidCellNeighbor(spheroidCells[neighborIndex]);
	    }
	} 
	//	log.println("[Environment.createCells] " + sphereData.length + " initial cells created");
    }
    

    /*
     * Creates, places and schedules cells.
     */
    
    //    private void createCellsOld2() {
    //	spheroidCells = new Cell[sphereData.length];
    //	for (int i = 0; i < sphereData.length; i ++) {
    //	    /*
    //	     * Cells must be placed in the array in the order
    //	     * specified in sphereData.  This is because each Sphere
    //	     * object's neighbors list specifies neighboring cells by
    //	     * index.
    //	     */
    //	    Cell c = new Cell(sphereData[i]);
    //	    spheroidCells[i] = c;
    //	    registerCell(c);
    //	}
    //
    //	// set each cell's neighbors
    //	for (int i = 0; i < sphereData.length; i ++) {
    //	    for (Iterator<Integer> j = sphereData[i].neighbors.iterator(); j.hasNext();) {
    //		int neighborIndex = j.next();
    //		spheroidCells[i].addSpheroidCellNeighbor(spheroidCells[neighborIndex]);
    //	    }
    //	} 
    //	//	log.println("[Environment.createCells] " + sphereData.length + " initial cells created");
    //    }
    

    public static Object createFromClassName(String className) {
	//	System.out.println("[Environment.createfromClassName] attempting to find class: "
	//			   + className);
	// Retrieve class from className argument
	Class cl = null;
	try {
	    cl = Class.forName(className);
	}
	catch (Exception e) {
	    die("Unable to find class " + className + "  " + e);
	}

	// Get 0-ary constructor for class
	Constructor<Object> constructor = null;
	try {
	    constructor =
		(Constructor<Object>)cl.getConstructor(new Class[0]);
	}
	catch (Exception e) {
	    die("Unable to find 0-argument constructor for class "
		+ className);
	}

	// Create object from 0-ary constructor
	Object o = null;
	try {
	    o = constructor.newInstance(new Object[0]);
	}
	catch (Exception e) {
	    die("Unable to instantiate class " + className);
	}
	return o;
    }

    public SimpleGrid getGrid() {
	return grid;
    }

    /*
    private RuleSetInterface createRuleSet(String ruleSetClassName) {
	Class ruleSetClass = null;
	try {
	    ruleSetClass = Class.forName(ruleSetClassName);
	}
	catch (Exception e) {
	    die("Unable to find class " + ruleSetClassName);
	}
	Constructor<RuleSetInterface> ruleSetConstructor = null;
	try {
	    ruleSetConstructor = (Constructor<RuleSetInterface>)ruleSetClass.getConstructor(new Class[0]);
	}
	catch (Exception e) {
	    die("Unable to find 0-argument constructor for class "
		+ ruleSetClassName);
	}
	RuleSetInterface rs = null;
	try {
	    rs = ruleSetConstructor.newInstance(new Object[0]);
	}
	catch (Exception e) {
	    die("Unable to instantiate class " + ruleSetClassName);
	}
	return rs;
    }
    */



    private void testCellShapeChange() {
	System.out.println("[Environment.testCellShapeChange] TEST!!!!");
	spheroid.Sphere s = new spheroid.Sphere();
	s.xCoord = 0;
	s.yCoord = 0;
	s.zCoord = 0;
	s.radius = 1;
	s.projection = new Point3D(1, 0, 0);
	s.neighborCount = 0;
	s.neighbors = new LinkedList<Integer>();
	Cell c = new Cell(s);
	
	grid.printOccupiedLocations();

	c.testShapeChange();
	System.out.println();
	System.out.println();
	
	grid.printOccupiedLocations();
	
    }


    private void testBranchingRule() {
	System.out.println("[Environment.testCellBranchingRule] TEST!!!!");
	Node n0 = new Node(new Point3D(1, 0, 0));
	Node n1 = new Node(new Point3D(0, 0, 0));
	Node n2 = new Node(new Point3D(-1, 0, 0));
	Node n3 = new Node(new Point3D(-2, 0, 0));
	Cell c1 = new Cell(n0, n1, 2);
	Cell c2 = new Cell(n1, n2, 1);
	Cell c3 = new Cell(n2, n3, 1);
	
	System.out.println("n0=" + n0);
	System.out.println("n1=" + n1);

	c1.setSuccessor(c2);
	c2.setPredecessor(c1);
	c2.setSuccessor(c3);
	c3.setPredecessor(c2);

	System.out.println(c1);

	c1.step(this);
	System.out.println();
	
	System.out.println(c1);
	System.out.println();
	System.out.println(c2);
	System.out.println();
	System.out.println(c3);
	System.out.println();
	System.out.println(c2.getPredecessorB());
	
	
	//	System.out.println(c2.getPredecessorB());
    }
	

    private void testProliferationRule() {
	System.out.println("[Environment.testCellProliferationRule] TEST!!!!");
	Node n0 = new Node(new Point3D(0, 0, 0));
	Node n1 = new Node(new Point3D(1, 0, 0));
	Node n2 = new Node(new Point3D(5, 0, 0));
	Cell c1 = new Cell(n0, n1, 2);
	Cell c2 = new Cell(n1, n2, 1);
	
	System.out.println(c1);

	c1.step(this);
	
	System.out.println(c1);
	System.out.println(c1.getSuccessor());
	//	System.out.println(c2.getPredecessorB());
    }
	
    private void testCellInhibition() {
	Cell.testInhibition();
    }

    private void testCellActivation() {
	Cell.testActivation();
    }

    //    private void testMigrationRule() {
    //	System.out.println("[Environment.testMigrationRule]");
    //	Cell.testMigrationRule();
    //    }


    private void testGetNgPerMl() {
	Cell c = new Cell();
       	grid.add(0, 0, 0, c);
	grid.add(0, 0, 1, c);
	grid.add(0, 0, 2, c);
	grid.add(0, 1, 0, c);
	grid.add(0, 1, 1, c);
	grid.add(0, 1, 2, c);
	grid.add(0, 2, 0, c);
	grid.add(0, 2, 1, c);
	grid.add(0, 2, 2, c);
	grid.add(1, 0, 0, c);
	grid.add(1, 0, 1, c);
	grid.add(1, 0, 2, c);
	grid.add(1, 1, 0, c);
	grid.add(1, 1, 1, c);
	grid.add(1, 1, 2, c);
	grid.add(1, 2, 0, c);
	grid.add(1, 2, 1, c);
	grid.add(1, 2, 2, c);
	grid.add(2, 0, 0, c);
	grid.add(2, 0, 1, c);
	grid.add(2, 0, 2, c);
	grid.add(2, 1, 0, c);
	grid.add(2, 1, 1, c);
	grid.add(2, 1, 2, c);
	grid.add(2, 2, 0, c);
	grid.add(2, 2, 1, c);
	grid.add(2, 2, 2, c);
	double conc = 
	    concentrationsManager.getNgPerMl(EnvironmentInterface.ConcentrationType.VEGF, 1, 1, 1);
	System.out.println("conc=" + conc);

    }

    private Cell[] getSpheroidCells() {
	return spheroidCells;
    }


    private void testCellPrintSproutCoordinates() {
	System.out.println("[Environment.testCellPrintSproutCoordinates]");
	Cell.testPrintSproutCoordinates();
    }


    private void testCellMigrate() {
	System.out.println("[Environment.testCellMigrate]");
	Cell.testMigrate();
    }

    /*
    private BasicStats collectLimitedXYStatistics() {
	Point3D spheroidCenter = new Point3D(0, 0, 0);
	// spheroidDiameterMicrons
	int branchCount = 0;
        double totalSproutLength = 0;
        double totalSproutVolume = 0;
	int initialSproutCount = 0;
	for (Cell c : spheroidCells) {
	    if (c.baseSpeher
	}
	return null;
    }
    */

    private BasicStats collectStatistics() {
	long branchCount = Cell.getBranchCount();
	long limitedXYBranchCount = Cell.getLimitedXYBranchCount();
	double attemptedMigrationCount = Cell.getAttemptedMigrationCount();
	double attemptedMigrationDistance =
	    Cell.getAttemptedMigrationDistance();
	double actualMigrationDistance = Cell.getActualMigrationDistance();
        double totalSproutLength = 0;
        double limitedXYTotalSproutLength = 0;
        double totalSproutVolume = 0;
        double limitedXYTotalSproutVolume = 0;
	double initialSproutCount = 0;
	SproutData sproutDataAccumulator = new SproutData();
        for (Cell c : spheroidCells) {
	    //	    System.out.println("[Environment.collectStatistics] skipping collectSproutData");

	    if (false) {
		System.out.println("[Environment.collectStatistics] Begin base cell "
	    			   + c.getIdNumber());
	    }
	    c.collectSproutData(sproutDataAccumulator);
	    if (false) {
		System.out.println("[Environment.collectStatistics] End cell "
				   + c.getIdNumber());
	    }

	    //	    System.out.println("[Environment.collectStatistics] Cell " + c.getIdNumber()
	    //			       + " sprout length cell count: "
	    //			       + c.getSproutLengthCount());
            totalSproutLength += c.getSproutLength();
            totalSproutVolume += c.getSproutVolume();
	    if (c.getPredecessor() != null || c.getPredecessorB() != null) {
		initialSproutCount++;
	    }
	}

	//	double tolerance = .000000001;
	//	double diff =
	//	    Math.abs(sproutDataAccumulator.sproutLengthMicrons
	//		     - totalSproutLength)
	//	    / totalSproutLength;
	//	//	if (Math.abs(sproutDataAccumulator.sproutLengthMicrons
	//	//		     - totalSproutLength)
	//	//	    > tolerance) {
	//	if (diff > tolerance) {
	//	    Environment.die("[Environment.collectStatistics] "
	//			    + "sproutDataAccumulator.sproutLengthMicrons="
	//			    + sproutDataAccumulator.sproutLengthMicrons
	//			    + " totalSproutLength=" + totalSproutLength);
	//	}
	//	diff =
	//	    Math.abs(sproutDataAccumulator.sproutVolumeCubicMicrons
	//		     - totalSproutVolume)
	//	    / totalSproutVolume;
	//	//	if (Math.abs(sproutDataAccumulator.sproutVolumeCubicMicrons
	//	//		     - totalSproutVolume)
	//	//	    > tolerance) {
	//	if (diff > tolerance) {
	//	    Environment.die("[Environment.collectStatistics] "
	//			    + "sproutDataAccumulator.sproutVolumeCubicMicrons="
	//			    + sproutDataAccumulator.sproutVolumeCubicMicrons
	//			    + " totalSproutVolume=" + totalSproutVolume);
	//	}


	double baseSphereCellCount = Cell.getBaseSphereCellCreationCount();
	double sproutCellCount = Cell.getSproutCellCreationCount();

	if (registeredCellCount != baseSphereCellCount + sproutCellCount) {
	    die("[Enviornment.collectStatistics] Registered cell count, " + registeredCellCount
		+ ", is not equal top the sum of the base speher cell count, "
		+ baseSphereCellCount + ", and the sprout cell count, " + sproutCellCount);
	}

	BasicStats stats =
	    new BasicStats(branchCount,
			   totalSproutLength,
			   totalSproutVolume,
			   initialSproutCount,
			   baseSphereCellCount,
			   sproutCellCount);
	stats.limitedXYBranchCount = limitedXYBranchCount;
	stats.limitedXYSproutLengthMicrons =
	    sproutDataAccumulator.limitedXYSproutLengthMicrons;
	stats.limitedXYSproutAreaSquareMicrons =
	    sproutDataAccumulator.limitedXYSproutAreaSquareMicrons;
	stats.limitedXYSproutCount =
	    sproutDataAccumulator.limitedXYSproutCount;
	// The individualLimitedXYBranchLengthMicrons field is needed
	// in shared.BasicStats for computing the standard deviation
	// of individual branch lengths.
	stats.individualLimitedXYBranchLengthsMicrons =
	    Cell.computeLimitedXYBranchLengths();
	//	double totalLimitedXYBranchLengthMicrons = 0;
	//	for (Iterator<Double> i =
	//		 stats.individualLimitedXYBranchLengthsMicrons.iterator();
	//	     i.hasNext();) {
	//	    double d = i.next();
	//	    totalLimitedXYBranchLengthMicrons += d;
	//	}
	//	stats.limitedXYBranchLengthMicrons = totalLimitedXYBranchLengthMicrons;

	stats.attemptedMigrationCount = attemptedMigrationCount;
	stats.attemptedMigrationDistance = attemptedMigrationDistance;
	stats.actualMigrationDistance = actualMigrationDistance;

	//	stats.simulatedHours = totalSimulationHours;
	//	stats.initialConditions = concentrationsManager.getInitialConditionsDescriptor();
	return stats;
    }

    private BasicStats writeOutput() {
	BasicStats stats = collectStatistics();
	if (!writeOutputFiles) {
	    return stats;
	}
	String suffix = getOutputFileSuffix();
	PrintStream ps = null;
	File sphereFile = new File(SPHERE_FILE_PREFIX + suffix);
	File sproutFile = new File(SPROUT_FILE_PREFIX + suffix);

	try {
	    
	    buffer.println("Branch count = " + stats.branchCount);
	    buffer.println("Total sprout length = " + stats.sproutLengthMicrons);
	    buffer.println("Total sprout volume = " + stats.sproutVolumeCubicMicrons);
	    buffer.println("Initial sprout count = " + stats.initialSproutCount);
	    
	    ps = new PrintStream(new FileOutputStream(sphereFile));
	    buffer.dumpBuffer(COMMENT_PREFIX, ps);
	    printBaseSphereCoordinates(ps);
	    ps.close();
	    
	    ps = new PrintStream(new FileOutputStream(sproutFile));
	    buffer.dumpBuffer(COMMENT_PREFIX, ps);
	    printSproutCoordinates(ps);
	    ps.close();
	}

	catch (Exception e) {
	    Environment.die("[Environment.writeOutput] Unable to write output files:   "
			    + e.toString());
	}
	return stats;
    }


    private void testCellGetSproutLengthVolume() {
	Cell.testGetSproutLengthVolume();
    }


    public int getGrowthCaptureInterval() {
	return growthCaptureInterval;
    }

    public boolean outputGeometryPrecisionSpecified() {
	return outputGeometryPrecisionSpecified;
    }

    public int getOutputGeometryPrecision() {
	return outputGeometryPrecision;
    }

    public boolean ignoreDiscretizedSprouts() {
	return ignoreDiscretizedSprouts;
    }
    
    public double getSpheroidDiameterMicrons() {
	return spheroidDiameterMicrons;
    }

    public Point3D getSpheroidCenter() {
	return spheroidCenter;
    }

    public double getSphereCellDiameterMicrons() {
	return sphereCellDiameterMicrons;
    }

    public double getEstimatedMaximumExtentMicrons() {
	// spheroid radius + initial sprout length + (time steps * linear growth per time step)
	double spheroidRadius = spheroidDiameterMicrons / 2.0;
	double initialSproutLength = Cell.getInitialSproutLengthMicrons();
	// Constant 10 used below was estimated by running v25b50 MPB
	// simulation for 24 hours.  This resulted in 13 initial
	// sprouts with a total length of 3160 microns.  3160 / (13 *
	// 24) = 10
	double linearGrowth = simulationLengthInTimeSteps * 10;
	return spheroidRadius + initialSproutLength + linearGrowth;
    }

    //    public double getMinimumInitialCellRadiusMicrons() {
    //	return minimumInitialCellRadiusMicrons;
    //    }
    
    //    public double getMaximumInitialCellRadiusMicrons () {
    //	return maximumInitialCellRadiusMicrons ;
    //    }
    
    //    public double getMinimumCellRadiusFactor () {
    //	return minimumCellRadiusFactor ;
    //    }
    
    //    public double getMaximumCellRadiusFactor () {
    //	return maximumCellRadiusFactor ;
    //    }
    
    public double getInitialSproutRadiusMicrons() {
    	return initialSproutRadiusMicrons;
    }
    
    //    public double getMinimumCellRadiusMicrons() {
    //    	return minimumCellRadiusMicrons;
    //    }
    
    //    public double getMaximumCellRadiusMicrons () {
    //    	return maximumCellRadiusMicrons ;
    //    }
    
    //    public double getMaximumCellLengthMicrons () {
    //	return maximumCellLengthMicrons ;
    //    }
    
    public double getInitialStalkCellLengthMicrons () {
	return initialStalkCellLengthMicrons ;
    }
    
    public double getInitialTipCellLengthMicrons () {
	return initialTipCellLengthMicrons ;
    }


    public boolean tipCellsHavePrecedence() {
	return tipCellPrecedence;
    }


    public void writeFiles(boolean writeOutputFiles) {
	this.writeOutputFiles = writeOutputFiles;
	if (!writeOutputFiles) {
	    growthCapture.disable();
	}
    }

    //    public long getCurrentStepNumber() {
    //	return currentStepNumber;
    //    }



    private LinkedList<CellGeometry> getCellGeometry() {
	return getCellGeometry(false);
    }

    public LinkedList<CellGeometry> getCellGeometry(boolean onlyChanged) {
	LinkedList<CellGeometry> lst = new LinkedList<CellGeometry>();
	for (Iterator<Cell> i = registeredCellList.iterator(); i.hasNext();) {
	    Cell c = i.next();
	    if (c.changedGeometry() || !onlyChanged) {
		c.addCellGeometry(lst);
	    }
	}
	return lst;
    }


    public long stepsRemaining() {
	return Math.max(0, simulationLengthInTimeSteps - schedule.getSteps());
    }

    public long stepsCompleted() {
	return schedule.getSteps();
    }

    private void step() {
	if (stepsRemaining() <= 0) {
	    die("[Environment.step] all steps completed");
	}
	//	System.out.println("[Environment.step] stepsRemaining="
	//			   + stepsRemaining());
	//	System.out.println("[Environment.step] getSteps="
	//			   + schedule.getSteps());
	//	System.out.println("[Environment.step] getTime="
	//			   + schedule.getTime());
	//	System.out.println("[Environment.step] time="
	//			   + schedule.time());
	//	System.out.println("[Environment.step] scheduleComplete="
	//			   + schedule.scheduleComplete());
	boolean result = schedule.step(this);
	if (!result) {
	    die("[Environment.step] schedule.step returned false");
	}
    }

    private static OneRepStats simulateOnce(Environment env,
					    GuiInterface gui) {
	env.start();
	boolean saveOnlyLastTimeStep =
	    env.getSimulationMode() == SimulationMode.GENETIC_ALGORITHM;
	BasicStats[] timeStepStats;
	if (saveOnlyLastTimeStep) {
	    timeStepStats = new BasicStats[1];
	}
	else {
	    timeStepStats = new BasicStats[env.simulationLengthInTimeSteps];
	}

	long stepsRemaining = env.stepsRemaining();
	if (gui != null) {
	    gui.step(env.getCellGeometry(), (stepsRemaining == 0));
	}
	while (stepsRemaining > 0) {
	    int index = (int) env.stepsCompleted();
	    
	    //	    System.out.println("[Environment.simulateOnce] index=" + index);
	    boolean lastStep = (stepsRemaining == 1);
	    env.step();
	    if (!saveOnlyLastTimeStep
		|| lastStep) {
		BasicStats bstats = env.collectStatistics();
		if (saveOnlyLastTimeStep) {
		    timeStepStats[0] = bstats;
		}
		else {
		    timeStepStats[index] = bstats;
		}
	    }
	    env.log.println("completed steps=" + env.stepsCompleted(),
			    LogStreamInterface.MINIMUM_LOG_DETAIL);
	    // reset flag for bug-catching at end of step
	    Cell.nonTipCellPreviouslyProcessed = false;
	    if (gui != null) {
		gui.step(env.getCellGeometry(), lastStep);
	    }
	    stepsRemaining = env.stepsRemaining();
	}
	env.continuityCheck();
	OneRepStats ors = new OneRepStats(env.randomSeed, timeStepStats);
	// collect and write output
	
	if (env.getSimulationMode() != SimulationMode.GENETIC_ALGORITHM) {
	    BasicStats stats = env.writeOutput();
	    //	    Cell.printStats();
	}
	env.finish();
	
	return ors;
    }


    private static OneRepStats simulateOnceOLD(Environment env,
					    GuiInterface gui) {
	env.start();
	boolean saveOnlyLastTimeStep =
	    env.getSimulationMode() == SimulationMode.GENETIC_ALGORITHM;
	long step = env.schedule.getSteps();
	BasicStats[] timeStepStats;
	if (saveOnlyLastTimeStep) {
	    timeStepStats = new BasicStats[1];
	}
	else {
	    timeStepStats = new BasicStats[env.simulationLengthInTimeSteps];
	}
	int lastStep = env.simulationLengthInTimeSteps - 1;
	if (debugFlag) {
	    System.out.println("[Environment.simulateOnce] begin loop");
	}

	while (step <= lastStep) {
	    System.out.println("[Environment.simulateOnce] step=" + step
			       + " lastStep=" + lastStep);
	    //	    if (debugFlag) {
	    //		System.out.println("[Environment.simulateOnce] step=" + step);
	    //		if (step == 4) {
	    //		    Cell.debugFlag = true;
	    //		}
	    //		else {
	    //		    Cell.debugFlag = false;
	    //		}
	    //	    }

	    env.currentStepNumber = step;
	    if (debugFlag) {
		System.out.println("[Environment.simulateOnce] Begin schedule.step");
	    }
	    if (!env.schedule.step(env)) {
		break;
	    }
	    if (debugFlag) {
		System.out.println("[Environment.simulateOnce] End schedule.step");
	    }
	    //	    System.out.println("[Environment.simulateOnce] skipping collectStatistics");
	    BasicStats bstats = null;
	    if (!saveOnlyLastTimeStep
		|| step == lastStep) {
		if (debugFlag) {
		    System.out.println("[Environment.simulateOnce] Begin collectStatistics");
		}

		bstats = env.collectStatistics();
		if (debugFlag) {
		    System.out.println("[Environment.simulateOnce] End collectStatistics");
		}

	    }
	    int stepIndex = (int) step;
	    if (saveOnlyLastTimeStep) {
		timeStepStats[0] = bstats;
	    }
	    else {
		timeStepStats[stepIndex] = bstats;
	    }
	    step = env.schedule.getSteps();
	    //		env.continuityCheck();
	    env.log.println("completed steps="
			    + step, LogStreamInterface.MINIMUM_LOG_DETAIL);
	    // reset flag for bug-catching at end of step
	    Cell.nonTipCellPreviouslyProcessed = false;
	    if (debugFlag) {
		System.out.println("[Environment.simulateOnce] step=" + step
				   + "END");
	    }
	    boolean last = (step == lastStep);
	    if (gui != null) {
		gui.step(env.getCellGeometry(), last);
	    }
	}
	if (debugFlag) {
	    System.out.println("[Environment.simulateOnce] end loop");
	}
	env.continuityCheck();
	OneRepStats ors = new OneRepStats(env.randomSeed, timeStepStats);
	// collect and write output
	
	//	    System.out.println();
	if (env.getSimulationMode() != SimulationMode.GENETIC_ALGORITHM) {
	    if (debugFlag) {
		System.out.println("[Environment.simulateOnce] begin writeOutput");
	    }
	    BasicStats stats = env.writeOutput();
	    if (debugFlag) {
		System.out.println("[Environment.simulateOnce] end writeOutput");
	    }
	    
	    if (debugFlag) {
		System.out.println("[Environment.simulateOnce] begin printStats");
	    }

	    Cell.printStats();
	    if (debugFlag) {
		System.out.println("[Environment.simulateOnce] end printStats");
	    }

	}
	env.finish();
	
	return ors;
    }


    private static void printScore(SimulationStats stats) {
	if (stats.initialConditions == null) {
	    System.out.println("No score available because initial conditions are not recognized");
	}
	else {
	    int index = stats.initialConditions.ordinal();
	    StateDiagramModelResult sdmr = new StateDiagramModelResult();
	    SimulationStats[] statsArr =
		new SimulationStats[StateDiagramModelResult.InitialConditions.values().length];
	    statsArr[index] = stats;
	    sdmr.stats = statsArr;
	    GenAlg.useLimitedXYStats();
	    GenAlg.useSingleInitialCondition(stats.initialConditions);
	    search.GenAlg.getScore(sdmr);
	    System.out.println("score=" + sdmr.score + "  "
			       + stats.initialConditions);
	    System.out.print("Individual score components: ");
	    for (double s : sdmr.individualScores[stats.initialConditions.ordinal()]) {
		System.out.print(s + " ");
	    }
	    System.out.println();
	}
    }
	
    public static SimulationStats simulate(Parameters param,
					   String gaReproductionFile,
					   boolean useGui) {
	StateDiagramModelResult sdmr =
	    StateDiagramModelResult.read(gaReproductionFile);
	System.out.println(sdmr);
	System.out.println(sdmr);
	Environment env = null;
	StateDiagramModel sdm = sdmr.model;
	long randomSeed = sdmr.randomSeed;
	StateDiagramModelResult.InitialConditions initCond =
	    sdmr.initialConditions;

	env = new Environment(param, randomSeed, sdm, initCond);
	OneRepStats stats = simulateOnce(env, null);
	SimulationStats simStats =
	    new SimulationStats(randomSeed, new OneRepStats[] {stats}, env);
	printScore(simStats);
	if (env.printStatsPerTimeStep) {
	    simStats.printStatsPerTimeStep();
	}
	return simStats;	
    }

    public static SimulationStats simulate(Parameters param,
					   int repetitions,
					   boolean useGui) {
	//	BasicStats[][] stats = new BasicStats[repetitions][];


	//	System.out.println("*****[Environment.simulate] no stats array");

	if (useGui && repetitions > 1) {
	    // Gui is given the environment object, but a different
	    // environment object is created for each repetition.
	    // This can be resolved by a gui method that takes the new
	    // environment
	    die("Gui not allowed for more than a single repetition");
	}
	
	GuiInterface gui = null;
	if (useGui) {
	    String guiName = param.getGuiName();
	    if (guiName == null) {
		die("No gui name specifed in file "
		    + param.getParametersFileName());
	    }
	    gui = (GuiInterface) createFromClassName(guiName);
	}

	OneRepStats[] stats = new OneRepStats[repetitions];
	Environment env = null;
	long originalRandomSeed = 0;
	for (int rep = 0; rep < repetitions; rep++) {
	    //	    System.out.println("Repetition: " + rep + " (" + repetitions
	    //			       + ")");

	    //	    if (rep == 682) {
	    //	    if (rep == 1817) {
	    //		debugFlag = true;
	    //	    }
	    //	    else {
	    //		debugFlag = false;
	    //	    }

	    if (rep == 0) {
		// use whatever random number generator seed that is
		// specified in the parameters file for the first
		// repetition
		env = new Environment(param);
		if (useGui) {
		    gui.initialize(env);
		}
		originalRandomSeed = env.randomSeed;
	    }
	    else {
		// don't reuse any random number generator seed that
		// is specified in the parameters file for all other
		// repetitions
		long newSeed = env.getRandom().nextLong();  
		// the above assumes newSeed does not repeat any
		// previous seeds including the first one
		env = new Environment(param, newSeed);
	    }
	    stats[rep] = simulateOnce(env, gui);
	}
	SimulationStats simStats = new SimulationStats(originalRandomSeed, stats, env);
	//	SimulationStats avgStats = SimulationStats.computeStats(stats);
	printScore(simStats);
	if (env.printStatsPerTimeStep) {
	    simStats.printStatsPerTimeStep();
	}

	System.out.println("[Environment.simulate] Limited XY Branch Counts");
	for (int i = 0; i < repetitions; i++) {
	    BasicStats[] bArr = stats[i].timeStepStats;
	    BasicStats b = bArr[bArr.length - 1];
	    System.out.println(b.limitedXYBranchCount);
	}
	System.out.println("[Environment.simulate] Limited XY Sprout Lengths");
	for (int i = 0; i < repetitions; i++) {
	    BasicStats[] bArr = stats[i].timeStepStats;
	    BasicStats b = bArr[bArr.length - 1];
	    System.out.println(b.limitedXYSproutLengthMicrons);
	}
	System.out.println("[Environment.simulate] Limited XY Branch Lengths");
	for (int i = 0; i < repetitions; i++) {
	    BasicStats[] bArr = stats[i].timeStepStats;
	    BasicStats b = bArr[bArr.length - 1];
	    double sum = 0;
	    int count = 0;
	    for (Iterator<Double> j = b.individualLimitedXYBranchLengthsMicrons.iterator();
		 j.hasNext();) {
		sum += j.next();
		count ++;
	    }
	    double avg = count == 0? 0 : sum / count;
	    System.out.println(avg);
	}


	return simStats;
	//	return null;
	
    }

    public static SimulationStats simulate(Parameters params,
					   long randomSeed,
					   StateDiagramModel sdm,
					   int repetitions,
					   StateDiagramModelResult.InitialConditions conc) {
	System.out.println("[Environment.simulate] Begin " + conc);
	if (conc == null) {
	    die("[Environment.simulate] Initial conditions are not specified");
	}
	//	BasicStats[][] stats = new BasicStats[repetitions][];
	BasicStats[] bStats = new BasicStats[repetitions];
	MersenneTwisterFast random = new MersenneTwisterFast(randomSeed);
	Environment env = null;
	//	SimulationStats statsTotals = null;
	for (int rep = 0; rep < repetitions; rep++) {
	    if (debugFlag) {
		System.out.println("[Environment.simulate] rep=" + rep);
	    }
	    long nextRandomSeed = random.nextLong();
	    StateDiagramModelResult sdmr =
		new StateDiagramModelResult(sdm, nextRandomSeed, conc);
	    sdmr.write("galast");
	    //	    System.out.println(sdmr);
	    //	    System.out.println("[Environment.simulate] Creating environment object");
	    env = new Environment(params, nextRandomSeed, sdm, conc);
	    //	    System.out.println("[Environment.simulate] random seed: "
	    //			       + env.randomSeed + " conc: " + conc);
	    env.writeFiles(false);
	    //	    SimulationStats stats = simulateOnce(env);
	    //	    if (GenSearchFlag) {
	    //		System.out.println("[Environmant.simulate] rep=" + rep
	    //				   + " start simulateOnce");
	    //	    }
	    debugFlag2 = (debugFlag && rep == 6);
	    OneRepStats osr = simulateOnce(env, null);
	    //	    if (GenSearchFlag) {
	    //		System.out.println("[Environmant.simulate] rep=" + rep
	    //				   + " end simulateOnce");
	    //	    }
	    //	    GarbageCollector.run();
	    bStats[rep] = osr.timeStepStats[osr.timeStepStats.length - 1];
	    //	    if (statsTotals == null) {
	    //		statsTotals = stats;
	    //	    }
	    //	    else {
	    //		statsTotals.addTo(stats);
	    //	    }
	}
	//	if (repetitions > 0) {
	//	    statsTotals.divideBy(repetitions);
	//	}
	SimulationStats simStats =
	    new SimulationStats(randomSeed, bStats, env);
	//	return statsTotals;
	return simStats;
	
    }

    //    public static SimulationStats simulate(String[] args,
    //					   StateDiagramModel sdm,
    //					   int repetitions,
    //					   EnvironmentInterface.SimulationMode simMode) {
    //	boolean writeOutputFiles = (simMode == EnvironmentInterface.SimulationMode.NORMAL);
    //	SimulationStats statsTotals = new SimulationStats();
    //
    //	Parameters param = new Parameters(args);
    //	
    //	MersenneTwisterFast random = null;
    //
    //	double totalSimulationHours = -1;
    //
    //	
    //	StateDiagramModelResult.InitialConditions[] forcedInitialConditions;
    //	switch (simMode) {
    //	case NORMAL:
    //	    forcedInitialConditions = new StateDiagramModelResult.InitialConditions[] {null};
    //	    break;
    //	case GENETIC_ALGORITHM:
    //	    forcedInitialConditions = StateDiagramModelResult.InitialConditions.values();
    //	    break;
    //	default:
    //	    die("[Environment.simulate] Unexpected simulation mode: " + simMode);
    //	}
    //
    //	for (int rep = 0; rep < repetitions; rep++) {
    //	    //	    System.out.println("**** " + rep);
    //	    Environment env;
    //	    // Continue to use same the random number generator across simulation runs
    //	    // Note that this does not mean reseed it!!
    //	    if (random == null) {
    //		env = new Environment(param);
    //		random = env.random;
    //	    }
    //	    else {
    //		env = new Environment(param, random);
    //	    }
    //	    if (totalSimulationHours == -1) {
    //		totalSimulationHours = env.totalSimulationHours;
    //	    }
    //	    env.setStateDiagramModel(sdm);
    //	    env.writeFiles(writeOutputFiles);
    //	    env.start();
    //	    if (statsTotals.initialConditions == null) {
    //		statsTotals.initialConditions =
    //		    env.concentrationsManager.getInitialConditionsDescriptor();
    //	    }
    //	    long step = env.schedule.getSteps();
    //	    while (step < env.simulationLengthInTimeSteps) {
    //		env.currentStepNumber = step;
    //		if (!env.schedule.step(env)) {
    //		    break;
    //		}
    //		step = env.schedule.getSteps();
    //				//		env.continuityCheck();
    //		env.log.println("completed steps="
    //				+ step, LogStreamInterface.MINIMUM_LOG_DETAIL);
    //		// reset flag for bug-catching at end of step
    //		Cell.nonTipCellPreviouslyProcessed = false;
    //	    }
    //
    //	    env.continuityCheck();
    //
    //	    // collect and write output
    //	    SimulationStats stats = env.writeOutput();
    //	    statsTotals.addTo(stats);
    //
    //	    //	    System.out.println();
    //	    //	    Cell.printStats();
    //	    env.finish();
    //	
    //	}
    //
    //	statsTotals.divideBy(repetitions);
    //	statsTotals.simulatedHours = totalSimulationHours;
    //	return statsTotals;
    //	
    //    }


    public SimulationMode getSimulationMode() {
    	return simMode;
    }

    public StateDiagramModelResult.InitialConditions getForcedInitialConditions() {
	return forcedInitialConditions;
    }

    public String getRuleSetName() {
	return ruleSet.getRuleSetName();
    }

    public StateDiagramModelResult.InitialConditions getInitialConditionsDescriptor() {
	return concentrationsManager.getInitialConditionsDescriptor();
    }

    public String getInitialConditionsString() {
	return concentrationsManager.getInitialConditionsString();
    }


    private static void testDivide() {
	Environment e = new Environment(1);
	e.ruleSet = (RuleSetInterface) createFromClassName("angiogenesis.RuleSetMPB");
	Cell c = new Cell();  // add nodes
	c.divide(e);
	// get new cell and check its localStorage
	
    }




    // Simulation entry point
    public static void main(String[] args) {
	//	System.out.println("[Environment.main] calling Cell.testProliferate");
	//	Cell.testProliferate(new);
	//	if (true) {return;}

	int repetitions = Integer.parseInt(args[args.length - 3]);
	String guiArg = args[args.length - 1];
	String gaReproductionFile = args[args.length - 2];
	String[] simArgs = new String[args.length - 3];

	for (int i = 0; i < args.length - 3; i++) {
	    simArgs[i] = args[i];
	}

	boolean useGui = guiArg.equals("YES");

	Parameters params = new Parameters(simArgs);
	SimulationStats stats;
	if (!gaReproductionFile.equals("")) {
	    stats = simulate(params, gaReproductionFile, useGui);
	}
	else {
	    stats = simulate(params, repetitions, useGui);
	}

	//	PrintData.printStats(stats);

	System.out.println(stats);
	stats.printTimeStepStats();

	if (true) {return;}

	//	String fileName = args[0];
	//	String ruleSetClassName = args[1]; // class name prefaced by package name
	//	long randomSeed = System.currentTimeMillis();

	//	Simulator sim = new Simulator(randomSeed, fileName, ruleSetClassName);

	Parameters param = new Parameters(args);
	Environment env = new Environment(param);
	env.start();


	//	System.out.println("[Environment.main] Calling Cell.testMigrationRule");
	//	Cell.testMigrationRule(env);
	//	Cell.testGetGradient();
	//	env.concentrationsManager.testGetGradient();
	//	if (true) {return;}


	/*
	System.out.println("[Environment.main] Linking cells");
	Cell succ = null;
	for (ListIterator<Cell> i = testCellList.listIterator(0);
	     i.hasNext();) {
	    Cell c = i.next();
	    //	    if (c.getIdNumber() == 0) {
	    //		c.setGrowthRole(CellInterface.GrowthRole.TIP);
	    //	    }
	    //	    else {
	    //		c.setGrowthRole(CellInterface.GrowthRole.STALK);
	    //	    }
	    c.setSuccessor(succ); 
	    //	    c.setPredecessor(null);
	    if (succ != null) {
		succ.setPredecessor(c);
	    }
	    succ = c; 
	}
	for (Iterator<Cell> i = testCellList.iterator(); i.hasNext();) {
	    Cell c = i.next();
	    System.out.println(c);
	}
	*/

	long steps;
	//	do {
	//	    if (!env.schedule.step(env)) {
	//		break;
	//	    }
	//	    /*
	//	    for (Iterator<Cell> i = testCellList.iterator(); i.hasNext();) {
	//		Cell c = i.next();
	//		System.out.println(c);
	//	    }
	//	    */
	//	    steps = env.schedule.getSteps();
	//	    env.log.println("steps=" + steps);
	//	    env.continuityCheck();
	//
	//	} while (steps < env.simulationLengthInTimeSteps);

	
	steps = env.schedule.getSteps();
	while (steps < env.simulationLengthInTimeSteps) {
	    if (!env.schedule.step(env)) {
		break;
	    }
	    steps = env.schedule.getSteps();
	    env.continuityCheck();
	    env.log.println("completed steps=" + steps, LogStreamInterface.MINIMUM_LOG_DETAIL);
	    Cell.nonTipCellPreviouslyProcessed = false;
	}


	env.continuityCheck();


	env.writeOutput();

	int lastSteppedCellIndex = Cell.getLastSteppedCell();
	//	Cell c = env.registeredCellList.get(lastSteppedCellIndex);
	//	System.out.println(c);

	System.out.println();
	Cell.printStats();
	

	/*

	int quiescentCount = 0;
	Cell[] spheroidCells = env.getSpheroidCells();
	for (Cell c : spheroidCells) {
	    if (c.getCellState() == EnvironmentInterface.CellState.QUIESCENT) {
		System.out.println(c.getIdNumber());
		quiescentCount++;
	    }
	}

	System.out.println("There are " + quiescentCount + " quiescent cells");
	env.printSproutCoordinates();
	*/


	// 1. initialze environment
	// 2. Create RuleSet and other external agents


	// 3. Create Top level Cell agents and let them create Node agents

	

	// 3. 
    }


    

    public void emergency(LinkedList<Integer> originList) {

	System.out.println("[Environment.emergency] " + originList.size());

	// add the origin of the last stepped cell to the origin list
	int lastSteppedCellIndex = Cell.getLastSteppedCell();

	System.out.println("[Environment.emergency] Last cell stepped: "
			   + lastSteppedCellIndex);

	Cell c = registeredCellList.get(lastSteppedCellIndex);
	if (c.getIdNumber() != lastSteppedCellIndex) {
	    Environment.die("[Environment.emergency] index mismatch: "
			    + lastSteppedCellIndex
			    + " " + c.getIdNumber());
	}
	Cell origin = c.getOrigin();

	LinkedList<Cell> queue = new LinkedList<Cell>();
	queue.add(origin);
	while (queue.size() > 0) {
	    Cell d = queue.removeFirst();
	    if (d != null) {
		System.out.println(d);
		System.out.println();
		queue.add(d.getPredecessor());
		queue.add(d.getPredecessorB());
	    }
	}

	/*
	printSphereCoordinates();

	System.out.println();
	System.out.println("---");
	System.out.println();



	printSproutCoordinates();
	*/

	if (true) {return;}
	

	int originIdNumber = origin.getIdNumber();
	int searchResult =
	    Collections.binarySearch(originList, originIdNumber);
	if (searchResult < 0) {
	    int insertionPoint = -(searchResult + 1);
	    originList.add(insertionPoint, originIdNumber);
	}
	


	for (Iterator<Integer> i = originList.iterator(); i.hasNext();) {
	    int index = i.next();
	    System.out.println(index);
	}

	boolean first = true;
	for (Iterator<Integer> i = originList.iterator(); i.hasNext();) {
	    int index = i.next();
	    Cell c1 = spheroidCells[index];
	    c1.printBaseSphereCoordinates(first, System.out);
	    first = false;
	}
	System.out.println();
	boolean firstFlag = true;
	for (Iterator<Integer> i = originList.iterator(); i.hasNext();) {
	    int index = i.next();
	    Cell c2 = spheroidCells[index];
	    Cell pred = c2.getPredecessor();
	    if (pred != null) {
		firstFlag = pred.printSproutCoordinates(firstFlag, System.out);
	    }
	    Cell predB = c2.getPredecessorB();
	    if (predB != null) {
		firstFlag = predB.printSproutCoordinates(firstFlag, System.out);
	    }
	}
	



	//	printSproutCoordinates();
	System.out.println("The last id number of a spheroid cell is: " 
			   + (spheroidCells.length - 1));

	System.out.println("Last stepped cell: " + c);
	/*
	Cell c236 = registeredCellList.get(236);
	System.out.println(c236);
	System.out.println();
	System.out.println("Projected tip: " + Cell.getProjectedTip(c236));
	System.out.println();
	System.out.println();
	Cell c237 = registeredCellList.get(237);
	System.out.println(c237);
	System.out.println();
	System.out.println("Projected tip: " + Cell.getProjectedTip(c237));
	*/

	/*
	printSphereCoordinates();
	System.out.println();
	printSproutCoordinates();
	*/
    }


    private void printBaseSphereCoordinates(PrintStream ps) {
	boolean first = true;
	for (Cell c : spheroidCells) {
	    c.printBaseSphereCoordinates(first, ps);
	    first = false;
	}
    }

    private void continuityCheck() {
	boolean first = true;
	for (Cell c : spheroidCells) {
	    c.continuityCheck();
	    c.markPredecessors();
	}
	
	//	System.out.println("Continuity check passed!");

	for (Iterator<Cell> i = registeredCellList.iterator(); i.hasNext();) {
	    Cell c = i.next();
	    if (! c.isMarkedAsPredecessor()) {
		die("[Environment.continuityCheck] Unmarked as a predecessor: "
		    + c);
	    }
	}

	//	System.out.println("Predecessor check passed!");

	
	int registeredCellCount = registeredCellList.size();
	int createdCellCount = Cell.getCreatedCellCount();
	if (registeredCellCount != createdCellCount) {
	    Environment.die("[Environment.continuityCheck] Unmatched counts "
			    + "registeredCellCount=" + registeredCellCount
			    + " createdCellCount= " + createdCellCount);
	}

	//	System.out.println("Cell count check passed!");

	
    }

    

    public void printSproutCoordinates(PrintStream ps) {
	boolean firstFlag = true;
	int limitLo = (int) (spheroidCells.length * sproutPrintRangeLo);
	int limitHi = (int) (spheroidCells.length * sproutPrintRangeHi); // 
	for (int i = limitLo; i < limitHi; i++) {
	    Cell c = spheroidCells[i];
	    Cell pred = c.getPredecessor();
	    if (pred != null) {
		firstFlag =
		    pred.printSproutCoordinatesOLDOLD(firstFlag, ps);
	    }
	    Cell predB = c.getPredecessorB();
	    if (predB != null) {
		int branchColor = 1 % numberOfSproutColors;
		firstFlag =
		    predB.printSproutCoordinatesOLDOLD(firstFlag, ps);
	    }
	}
    }

    public void printSproutCoordinatesSAVE(PrintStream ps) {
	boolean firstFlag = true;
	for (Cell c : spheroidCells) {
	    Cell pred = c.getPredecessor();
	    if (pred != null) {
		firstFlag =
		    pred.printSproutCoordinatesOLDOLD(firstFlag, ps);
	    }
	    Cell predB = c.getPredecessorB();
	    if (predB != null) {
		int branchColor = 1 % numberOfSproutColors;
		firstFlag =
		    predB.printSproutCoordinatesOLDOLD(firstFlag, ps);
	    }
	}
    }


    
}
