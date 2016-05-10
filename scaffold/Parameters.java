
/*
 * 2/26/2011
 * Input file parameters read using shared.Parameter
 *
 * 
 * Reads parameters from files and the command line for the package
 * and provides them to the Environment class.
 *
 * This module reads files where input is formatted as follows.
 * 
 * 1) Input values are preceded by labels.
 * 2) Values associated with the label immediately follow it on the same line
 * 3) Only white space is used to separate labels and values 
 *
 * To add new parameter reading cabability do the following. (To find
 * the relevant locations in this file, search for the parenthesized
 * step number)
 *
 * (1) Add new labels to the LabelType enum.
 * (2) Declare new fields to hold values associated with the label.
 * (3) If necessary, Update the switch statement in the
 *     readInputParameters method to specify how many values follow
 *     each label.
 * (4) Add case statements to store values in local fields.
 * (5) Create a get method so that the value can be accessed by other
 *     classes.

 */

package scaffold;

import shared.*;

import spheroid.*;

import java.util.*;
import java.lang.*;
import java.io.*;

public class Parameters {

    public enum RandomSeedSource {FILE, SELF_GENERATED, OTHER};

    // consider moving these constants somewhere else
    private static final int NUMBER_OF_SPHERE_PARAMETERS = 4;
    private static final int NUMBER_OF_CAPSULE_PARAMETERS = 7;


    /*
     * (1) Internal representation of parameter file labels.
     */
    private static enum LabelType
    {PRINT_STATS_PER_TIME_STEP,
     RANDOM_SEED, SIMULATION_LENGTH_IN_TIME_STEPS, TIME_STEP_LENGTH_IN_SECONDS,
     VOXEL_X_ORIGIN, VOXEL_Y_ORIGIN, VOXEL_Z_ORIGIN,
     VOXEL_LENGTH_MICRONS, VOXEL_GRID_MIN_X, VOXEL_GRID_MAX_X, 
     VOXEL_GRID_MIN_Y, VOXEL_GRID_MAX_Y, VOXEL_GRID_MIN_Z, VOXEL_GRID_MAX_Z,
     LOG_FILE_NAME, LOG_DETAIL_LEVEL, ECHO_LOG, INHIBITION_RANGE, DLL4_PRESENT,
     CONCENTRATIONS_PARAMETERS_FILE_NAME, GROWTH_CAPTURE_INTERVAL,
     OUTPUT_GEOMETRY_PRECISION, ANGIOGENESIS_PARAMETERS_FILE_NAME,
     GUI_PARAMETERS_FILE_NAME,
     IGNORE_DISCRETIZED_SPROUTS, SPHEROID_DIAMETER_MICRONS, SPHERE_CELL_DIAMETER_MICRONS,
	    //     MINIMUM_INITIAL_CELL_RADIUS_MICRONS, MAXIMUM_INITIAL_CELL_RADIUS_MICRONS,
	    //     MINIMUM_CELL_RADIUS_FACTOR, MAXIMUM_CELL_RADIUS_FACTOR, 
     INITIAL_SPROUT_RADIUS_MICRONS,
	    //     MINIMUM_CELL_RADIUS_MICRONS, MAXIMUM_CELL_RADIUS_MICRONS, 
	    //     MAXIMUM_CELL_LENGTH_MICRONS,
     INITIAL_STALK_CELL_LENGTH_MICRONS, INITIAL_TIP_CELL_LENGTH_MICRONS,

     MINIMUM_CELL_LENGTH_TO_WIDTH_RATIO, MAXIMUM_CELL_LENGTH_TO_WIDTH_RATIO,
     LENGTH_TO_WIDTH_RATIO_TRANSITION_DISTANCE_CELLS,
	    //     MINIMUM_TIP_CELL_LENGTH_TO_WIDTH_RATIO, 
	    //     MAXIMUM_TIP_CELL_LENGTH_TO_WIDTH_RATIO,
     MAXIMUM_ELONGATION_FACTOR,

     MINIMUM_DIVISION_LENGTH_MICRONS,
     DIVISION_PROBABILITY_CONSTANT_1, DIVISION_PROBABILITY_CONSTANT_2,

     NUMBER_OF_SPROUT_COLORS, GUI_NAME,

     SPROUT_PRINT_RANGE

	    

     //CELLS
	    }; 


    // command line parameter values
    private String parametersFileName;
    private String ruleSetClassName;     // includes package name
    private String concentrationsManagerClassName; // includes package name

    // (2) file parameter values
    private boolean printStatsPerTimeStep;
    private long randomSeed = -1; 
    private RandomSeedSource randomSeedSource = null;
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
    private int logDetailLevel;
    private boolean echoLog;
    private int inhibitionRange;
    private boolean dll4Flag = false;
    private String concentrationsParametersFileName;
    private String angiogenesisParametersFileName;
    private String guiParametersFileName;
    private int growthCaptureInterval = 0;
    private boolean outputGeometryPrecisionSpecified = false;
    private int outputGeometryPrecision;
    private boolean ignoreDiscretizedSprouts = false;
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

    private double[] sproutPrintRange;

    private String guiName;

    private StateDiagramModelResult.InitialConditions forcedInitialConditions;


    private boolean growthCaptureIntervalForced;


    // initial sphere/cell data
    private spheroid.Sphere[] sphereData;

    
    public Parameters(String fileName) {
	readInputParameters(fileName);
	if (randomSeedSource == null) {
	    randomSeed = System.currentTimeMillis();
	    randomSeedSource = RandomSeedSource.SELF_GENERATED;
	}
	//	sphereData = Spheroid.getSphereData();
	System.out.println("[Parameters] sphereData=" + sphereData);
    }


    public Parameters(String[] args) {
	int expectedNumberOfArgs = 5;
	if (args.length != expectedNumberOfArgs) {
	    Environment.die("[Parameters] Expected " + expectedNumberOfArgs
			    + " arguments; received " + args.length + " arguments");
	}
	parametersFileName = args[0];
	ruleSetClassName = args[1];
	concentrationsManagerClassName = args[2];
	String forcedInitialConditionsArg = args[3];
	String forcedGrowthCaptureIntervalArg = args[4];

	forcedInitialConditions = null;
	if (!forcedInitialConditionsArg.equals("")) {
	    for (StateDiagramModelResult.InitialConditions ic : StateDiagramModelResult.InitialConditions.values()) {
		if (forcedInitialConditionsArg.equalsIgnoreCase(ic.toString())) {
		    forcedInitialConditions = ic;
		    break;
		}
	    }
	    if (forcedInitialConditions == null) {
		Environment.die("Unknown forced initial conditions option: "
				+ forcedInitialConditionsArg);
	    }
	}

	growthCaptureIntervalForced = false;
	if (!forcedGrowthCaptureIntervalArg.equals("")) {
	    growthCaptureIntervalForced = true;
	    boolean validInterval = true;
	    try {
		growthCaptureInterval =
		    Integer.parseInt(forcedGrowthCaptureIntervalArg);
	    }
	    catch (Exception e) {
		validInterval = false;
	    }
	    validInterval = validInterval && (growthCaptureInterval >= 0);
	    if (!validInterval) {
		Environment.die("Forced growth capture interval must be a nonnegative integer");
	    }
	}


	//	String randomSeedOverride = args[3];
	//	if (!(randomSeedOverride == null || randomSeedOverride.equals(""))) {
	//	    randomSeedSource = RandomSeedSource.OTHER;
	//	    randomSeed = Long.parseLong(randomSeedOverride);
	//	}
	readInputParameters(parametersFileName);
	if (randomSeedSource == null) {
	    randomSeed = System.currentTimeMillis();
	    randomSeedSource = RandomSeedSource.SELF_GENERATED;
	}
	//	sphereData = Spheroid.getSphereData();
	//	System.out.println("[Parameters] sphereData=" + sphereData);
    }



    public long getRandomSeed() {
	return randomSeed;
    }

    public RandomSeedSource getRandomSeedSource() {
	return randomSeedSource;
    }

    //    public void setRandomSeedSource(RandomSeedSource source) {
    //	randomSeedSource = source;
    //    }


    private void readInputParameters(String fileName) {
	ArrayList<shared.Parameter> params = new ArrayList<Parameter>();
	for (LabelType label : LabelType.values()) {
	    
	    // (3) Specify the number of values qassociated with each
	    // label.  The default is 1 value.
	    int size;
	    switch (label) {
	    case DLL4_PRESENT:
	    case IGNORE_DISCRETIZED_SPROUTS:
	    case PRINT_STATS_PER_TIME_STEP:
	    case ECHO_LOG:
		size = 0;
		break;
	    case SPROUT_PRINT_RANGE:
		size = 2;
		break;
	    default:
		size = 1;
	    }
	    shared.Parameter p =
		new Parameter(label.toString(), new String[size], label.ordinal());
	    params.add(p);
	}
	shared.Parameter.readParameters(fileName, params);

	// (4) Store the values read from the input file in local
	// fields.
	
	for (LabelType lt : LabelType.values()) {
	    shared.Parameter p = params.get(lt.ordinal());
	    switch (lt) {
	    case PRINT_STATS_PER_TIME_STEP:
		printStatsPerTimeStep = p.present();
		break;
	    case RANDOM_SEED:
		// Attempt to read seed only if not already overriden
		if (randomSeedSource == null) {
		    if (p.present()) {
			randomSeed = p.getLong(0);
			randomSeedSource = RandomSeedSource.FILE;
		    }
		}
		break;
	    case SIMULATION_LENGTH_IN_TIME_STEPS:
		simulationLengthInTimeSteps = p.getPositiveInt(0);
		break;
	    case TIME_STEP_LENGTH_IN_SECONDS:
		timeStepLengthInSeconds = p.getPositiveInt(0);
		break;
	    case VOXEL_X_ORIGIN:
		voxelXOrigin = p.getDouble(0);
		break;
	    case VOXEL_Y_ORIGIN:
		voxelYOrigin = p.getDouble(0);
		break;
	    case VOXEL_Z_ORIGIN:
		voxelZOrigin = p.getDouble(0);
		break;
	    case VOXEL_LENGTH_MICRONS:
		voxelLengthMicrons = p.getPositiveInt(0);
		break;
	    case VOXEL_GRID_MIN_X:
		voxelGridMinX = p.getInt(0);
		break;
	    case VOXEL_GRID_MAX_X:
		voxelGridMaxX = p.getInt(0);
		break;
	    case VOXEL_GRID_MIN_Y:
		voxelGridMinY = p.getInt(0);
		break;
	    case VOXEL_GRID_MAX_Y:
		voxelGridMaxY = p.getInt(0);
		break;
	    case VOXEL_GRID_MIN_Z:
		voxelGridMinZ = p.getInt(0);
		break;
	    case VOXEL_GRID_MAX_Z:
		voxelGridMaxZ = p.getInt(0);
		break;
	    case LOG_FILE_NAME:
		logFileName = p.getString(0);
		break;
	    case LOG_DETAIL_LEVEL:
		// default: no logging
		logDetailLevel = -1;
		if (p.present()) {
		    logDetailLevel = p.getInt(0);
		}
		break;
	    case ECHO_LOG:
		echoLog = p.present();
		break;
	    case INHIBITION_RANGE:
		inhibitionRange = p.getNonnegativeInt(0);
		break;
	    case DLL4_PRESENT:
		dll4Flag = p.present();
		break;
	    case CONCENTRATIONS_PARAMETERS_FILE_NAME:
		concentrationsParametersFileName = p.getString(0);
		break;
	    case ANGIOGENESIS_PARAMETERS_FILE_NAME:
		angiogenesisParametersFileName = p.getString(0);
		break;
	    case GUI_PARAMETERS_FILE_NAME:
		guiParametersFileName = p.getString(0);
		break;
	    case GROWTH_CAPTURE_INTERVAL:
		if (!growthCaptureIntervalForced) {
		    growthCaptureInterval = p.getNonnegativeInt(0);
		}
		break;
	    case OUTPUT_GEOMETRY_PRECISION:
		if (p.present()) {
		    outputGeometryPrecision = p.getInt(0);
		    outputGeometryPrecisionSpecified = true;
		}
		break;
	    case IGNORE_DISCRETIZED_SPROUTS:
		ignoreDiscretizedSprouts = p.present();
		break;
	    case SPHEROID_DIAMETER_MICRONS:
		spheroidDiameterMicrons = p.getPositiveDouble(0);
		break;
	    case SPHERE_CELL_DIAMETER_MICRONS:
		sphereCellDiameterMicrons = p.getPositiveDouble(0);
		break;
		//	    case MINIMUM_INITIAL_CELL_RADIUS_MICRONS:
		//		minimumInitialCellRadiusMicrons = p.getPositiveDouble(0);
		//		break;
		//	    case MAXIMUM_INITIAL_CELL_RADIUS_MICRONS:
		//		maximumInitialCellRadiusMicrons = p.getPositiveDouble(0);
		//		break;
		//	    case MINIMUM_CELL_RADIUS_FACTOR:
		//		minimumCellRadiusFactor = p.getNonnegativeDouble(0);
		//		break;
		//	    case MAXIMUM_CELL_RADIUS_FACTOR:
		//		maximumCellRadiusFactor = p.getPositiveDouble(0);
		//		break;
	    case INITIAL_SPROUT_RADIUS_MICRONS:
		initialSproutRadiusMicrons = p.getPositiveDouble(0);
		break;
		//	    case MINIMUM_CELL_RADIUS_MICRONS:
		//		minimumCellRadiusMicrons = p.getPositiveDouble(0);
		//		break;
		//	    case MAXIMUM_CELL_RADIUS_MICRONS:
		//		maximumCellRadiusMicrons = p.getPositiveDouble(0);
		//		break;
		//	    case MAXIMUM_CELL_LENGTH_MICRONS:
		//		maximumCellLengthMicrons = p.getPositiveDouble(0);
		//		break;
	    case INITIAL_STALK_CELL_LENGTH_MICRONS:
		initialStalkCellLengthMicrons = p.getPositiveDouble(0);
		break;
	    case INITIAL_TIP_CELL_LENGTH_MICRONS:
		initialTipCellLengthMicrons = p.getPositiveDouble(0);
		break;
	    case MINIMUM_CELL_LENGTH_TO_WIDTH_RATIO:
		minimumCellLengthToWidthRatio = p.getPositiveDouble(0);
		break;
	    case MAXIMUM_CELL_LENGTH_TO_WIDTH_RATIO:
		maximumCellLengthToWidthRatio = p.getPositiveDouble(0);
		break;
	    case LENGTH_TO_WIDTH_RATIO_TRANSITION_DISTANCE_CELLS:
		lengthToWidthRatioTransitionDistanceCells =
		    p.getNonnegativeInt(0);
		break;
		//	    case MINIMUM_TIP_CELL_LENGTH_TO_WIDTH_RATIO:
		//		minimumTipCellLengthToWidthRatio = p.getPositiveDouble(0);
		//		break;
		//	    case MAXIMUM_TIP_CELL_LENGTH_TO_WIDTH_RATIO:
		//		maximumTipCellLengthToWidthRatio = p.getPositiveDouble(0);
		//		break;
	    case MAXIMUM_ELONGATION_FACTOR:
		maximumElongationFactor = p.getNonnegativeDouble(0);
		break;
	    case MINIMUM_DIVISION_LENGTH_MICRONS:
		minimumDivisionLengthMicrons = p.getPositiveDouble(0);
		break;
	    case DIVISION_PROBABILITY_CONSTANT_1:
		divisionProbabilityConstant1 = p.getDouble(0);
		break;
	    case DIVISION_PROBABILITY_CONSTANT_2:
		divisionProbabilityConstant2 = p.getDouble(0);
		break;
	    case NUMBER_OF_SPROUT_COLORS:
		if (p.present()) {
		    numberOfSproutColors = p.getPositiveInt(0);
		}
		else {
		    numberOfSproutColors = 1;
		}
		break;
	    case SPROUT_PRINT_RANGE:
		if (p.present()) {
		    double lo = p.getNonnegativeDouble(0);
		    double hi = p.getNonnegativeDouble(1);
		    if (hi > 1) {
			Environment.die("Second SPROUT_PRINT_RANGE parameter value: "
					+ hi + " is greater than 1 on line "
					+ p.getLineNumber() + " of file "
					+ p.getFileName());
		    }
		    else {
			if (lo >= hi) {
			    Environment.die("First SPROUT_PRINT_RANGE parameter value: "
					    + lo + " is not less than second: "
					    + hi + " on line number " 
					    + p.getLineNumber()
					    + " of file " + p.getFileName());
			}
		    }
		    sproutPrintRange = new double[] {lo, hi};
		}
		else {
		    sproutPrintRange = new double[] {0, 1};
		}
		break;
	    case GUI_NAME:
		if (p.present()) {
		    guiName = p.getString(0);
		}
		else {
		    guiName = null;
		}
		break;
	    default:
		Environment.die("[Paramaters.readInputParameters] Unsupported label type: " + lt);
	    }
	}

    }


    public String getParametersFileName() {
	return parametersFileName;
    }

    public boolean printStatsPerTimeStep() {
	return printStatsPerTimeStep;
    }

    public String getRuleSetClassName() {
	return ruleSetClassName;
    }

    public String getConcentrationsManagerClassName() {
	return concentrationsManagerClassName;
    }

    // (5) Methods for returning parameters read from the input file

    public int getSimulationLengthInTimeSteps() {
	return simulationLengthInTimeSteps;
    }

    public double getTimeStepLengthInSeconds() {
	return timeStepLengthInSeconds;
    }

    public double getVoxelXOrigin() {
	return voxelXOrigin;
    }

    public double getVoxelYOrigin() {
	return voxelYOrigin;
    }

    public double getVoxelZOrigin() {
	return voxelZOrigin;
    }

    public double getVoxelLengthMicrons() {
	return voxelLengthMicrons;
    }

    public int getVoxelGridMinX() {
	return voxelGridMinX;
    }

    public int getVoxelGridMaxX() {
	return voxelGridMaxX;
    }

    public int getVoxelGridMinY() {
	return voxelGridMinY;
    }

    public int getVoxelGridMaxY() {
	return voxelGridMaxY;
    }

    public int getVoxelGridMinZ() {
	return voxelGridMinZ;
    }

    public int getVoxelGridMaxZ() {
	return voxelGridMaxZ;
    }

    public String getLogFileName() {
	return logFileName;
    }

    public int getLogDetailLevel() {
	return logDetailLevel;
    }

    public boolean getEchoLogFlag() {
	return echoLog;
    }

    public int getInhibitionRange() {
	return inhibitionRange;
    }


    public boolean getDll4Flag() {
	return dll4Flag;
    }


    public StateDiagramModelResult.InitialConditions getForcedInitialConditions() {
	return forcedInitialConditions;
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

    public double getSphereCellDiameterMicrons() {
	return sphereCellDiameterMicrons;
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
    
    public double getInitialSproutRadiusMicrons () {
	return initialSproutRadiusMicrons ;
    }
    
    //    public double getMinimumCellRadiusMicrons () {
    //	return minimumCellRadiusMicrons ;
    //    }
    
    //    public double getMaximumCellRadiusMicrons () {
    //	return maximumCellRadiusMicrons ;
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

    public double getMinimumCellLengthToWidthRatio() {
	return minimumCellLengthToWidthRatio;
    }

    public double getMaximumCellLengthToWidthRatio() {
	return maximumCellLengthToWidthRatio;
    }

    public int getLengthToWidthRatioTransitionDistanceCells() {
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

    public double[] getSproutPrintRange() {
	return new double[] {sproutPrintRange[0], sproutPrintRange[1]};
    }

    public String getGuiName() {
	return guiName;
    }

    
    public static void main(String[] args) {
	Parameters p = new Parameters(args[0]);
	System.out.println("Random seed: " + p.getRandomSeed());
	System.out.println("Random seed source: " + p.getRandomSeedSource());
	System.out.println("Simulation length in time steps: "
			   + p.getSimulationLengthInTimeSteps());
	System.out.println("Time step length in seconds: "
			   + p.getTimeStepLengthInSeconds());
	System.out.println("Voxel grid x-origin: " + p.getVoxelXOrigin());
	System.out.println("Voxel grid y-origin: " + p.getVoxelYOrigin());
	System.out.println("Voxel grid z-origin: " + p.getVoxelZOrigin());
	System.out.println("Voxel length in microns: " + p.getVoxelLengthMicrons());
	System.out.println("Voxel grid min x: " + p.getVoxelGridMinX());
	System.out.println("Voxel grid max x: " + p.getVoxelGridMaxX());
	System.out.println("Voxel grid min y: " + p.getVoxelGridMinY());
	System.out.println("Voxel grid max y: " + p.getVoxelGridMaxY());
	System.out.println("Voxel grid min z: " + p.getVoxelGridMinZ());
	System.out.println("Voxel grid max z: " + p.getVoxelGridMaxZ());
	System.out.println("Log file name: " + p.getLogFileName());
	System.out.println("Inhibition range: " + p.getInhibitionRange());
	System.out.println("DLL4 flag: " + p.getDll4Flag());
	System.out.println("Concentration parameters: " + p.getConcentrationsParametersFileName());
	System.out.println("Angiogenesis parameters: " + p.getAngiogenesisParametersFileName());
	System.out.println("Growth capture interval " + p.getGrowthCaptureInterval());
	System.out.println("Output geometry precision flag "
			   + p.outputGeometryPrecisionSpecified());
	System.out.println("Output geometry precision " + p.getOutputGeometryPrecision());
	System.out.println("Ignore discretized sprouts flag " + p.ignoreDiscretizedSprouts());
	System.out.println("Spheroid diameter (microns) " + p.getSpheroidDiameterMicrons());
	System.out.println("Sphere cell diameter (microns) " + p.getSphereCellDiameterMicrons());
	//	System.out.println("Minimum initial cell radius (microns) "
	//			   + p.getMinimumInitialCellRadiusMicrons());
	//	System.out.println("Maximum initial cell radius (microns) "
	//			   + p.getMaximumInitialCellRadiusMicrons());
	//	System.out.println("Minimum cell radius factor " + p.getMinimumCellRadiusFactor());
	//	System.out.println("Maximum cell radius factor " + p.getMaximumCellRadiusFactor());
	//	System.out.println("Minimum cell radius (microns) " + p.getMinimumCellRadiusMicrons());
	//	System.out.println("Maximum cell radius (microns) " + p.getMaximumCellRadiusMicrons());
	//	System.out.println("Maximum cell length (microns) " + p.getMaximumCellLengthMicrons());
	System.out.println("Initial stalk cell length (microns) "
			   + p.getInitialStalkCellLengthMicrons());
	System.out.println("Initial tip cell length (microns) "
			   + p.getInitialTipCellLengthMicrons());
	System.out.println("Minimum cell length to width ratio "
			   + p.getMinimumCellLengthToWidthRatio());
	System.out.println("Maximum cell length to width ratio "
			   + p.getMaximumCellLengthToWidthRatio());
	System.out.println("Length to width ratio transition distance in cells" 
			   + p.getLengthToWidthRatioTransitionDistanceCells());
	//	System.out.println("Minimum tip cell length to width ratio "
	//			   + p.getMinimumTipCellLengthToWidthRatio());
	//	System.out.println("Maximum tip cell length to width ratio "
	//			   + p.getMaximumTipCellLengthToWidthRatio());

	System.out.println("Minimum division length (microns) "
			   + p.getMinimumDivisionLengthMicrons());
	System.out.println("Division probability constant 1 "
			   + p.getDivisionProbabilityConstant1());
	System.out.println("Division probability constant 2 "
			   + p.getDivisionProbabilityConstant2());
	System.out.println("GUI parameters file name " +
			   p.getGuiParametersFileName());

    }



}

