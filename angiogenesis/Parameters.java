
/*
 * 3-24-2011
 * Changed to allow proper subclassing by Parameters2.
 *
 */


/*
 * Reads parameters from file
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

package angiogenesis;

import shared.*;

import java.util.*;
import java.lang.*;
import java.io.*;

public class Parameters {


    protected ArrayList<shared.Parameter> params;

    protected String fileName = null;

    protected boolean parametersReadFromFile = false;


    /*
     * Internal representation of parameter file labels.
     *
     * (1) Place new input labels here.
     */
    public static enum LabelType 
    {//VEGF_ACTIVATION_THRESHOLD_NG_PER_ML,
     MIGRATION_MAGNITUDE_VEGF_COEFFICIENT,
     BASELINE_MIGRATION_MICRONS_PER_HOUR, 
     MIGRATION_CONCENTRATION_VECTOR_WEIGHT, MIGRATION_PERSISTENCE_VECTOR_WEIGHT,
     MIGRATION_RANDOM_VECTOR_WEIGHT,
     MIGRATION_VARIANCE_ANGLE_DEGREES,
   
     PROLIFERATION_BASELINE_GROWTH_RATE_PER_HOUR,
     PROLIFERATION_ACTIVE_FRACTION,
     PROLIFERATION_VEGF_COEFFICIENT_1,
     PROLIFERATION_BDNF_THRESHOLD_NG_ML,
     PROLIFERATION_BDNF_COEFFICIENT_2, 
     PROLIFERATION_BDNF_COEFFICIENT_3, PROLIFERATION_BDNF_CONSTANT_4,
     DISABLE_REAR_CELL_PROLIFERATION,
     DISABLE_REAR_CELL_BRANCHING,
     DISABLE_ALL_CELL_BRANCHING,
     DISABLE_ALL_CELL_PROLIFERATION,
     DISABLE_MIGRATION,
	    //     NOMINAL_CELL_VOLUME_CUBIC_MICRONS,	    

     MINIMUM_BRANCH_ANGLE_DEGREES, MAXIMUM_BRANCH_ANGLE_DEGREES,
     INITIAL_BRANCH_LENGTH_MICRONS,
     INITIAL_BRANCH_RADIUS_MICRONS,
     CELL_BRANCH_VOLUME_RATIO_THRESHOLD,

     MAXIMUM_CHEMOTACTIC_MICRONS_PER_HOUR,
     MAXIMUM_MIGRATION_MICRONS_PER_HOUR, COLLAGEN_MIGRATION_FACTOR,

     STATE_DIAGRAM_MODEL_FILE_NAME,

     SPECIAL_BRANCH_PROLIFERATION_FACTOR,
     SPECIAL_BRANCH_TIME_HOURS,

     VEGF_0_NGML_ACTIVATION_PROBABILITY, VEGF_25_NGML_ACTIVATION_PROBABILITY,
     BDNF_0_NGML_ACTIVATION_PROBABILITY, BDNF_50_NGML_ACTIVATION_PROBABILITY,

     FORCED_BRANCH_PROLIFERATION_PROBABILITY

 

	    //	    BASELINE_GROWTH_CUBIC_MICRONS_PER_HOUR
	    };


    private boolean[] removed = new boolean[LabelType.values().length];

    private static Class LABELTYPE_CLASS = null;
    static {
    	try {
	    LABELTYPE_CLASS = Class.forName("angiogenesis.Parameters$LabelType");
    	}
    	catch (Exception e) {
    	    SimpleRuleSet.die("[Parameters] Unable to get class for LabelType   " + e.toString());
    	}
    }
    

    /*
     * File parameter values
     *
     * (2) Place new field declarations for storing file parameter values here.
     */
    //    private double vegfActivationThresholdNgPerMl;
    private double migrationMagnitudeVegfCoefficient;
    private double baselineMigrationMicronsPerHour;
    private double migrationConcentrationVectorWeight;
    private double migrationPersistenceVectorWeight;
    private double migrationRandomVectorWeight;
    private double migrationVarianceAngleDegrees;
    private double proliferationBaselineGrowthRatePerHour;
    private double proliferationActiveFraction;
    private double proliferationVegfCoefficient1;
    private double proliferationBdnfThresholdNgMl;
    private double proliferationBdnfCoefficient2;
    private double proliferationBdnfCoefficient3;
    private double proliferationBdnfConstant4;
    private boolean disableRearCellProliferation;
    private boolean disableRearCellBranching;
    private boolean disableAllCellBranching;
    private boolean disableAllCellProliferation;
    private boolean disableMigration;
    //    private boolean nominalCellVolumeSpecified;
    //    private double nominalCellVolumeCubicMicrons;

    private double minimumBranchAngleDegrees;
    private double maximumBranchAngleDegrees;
    private double initialBranchLengthMicrons;
    private double initialBranchRadiusMicrons;
    private double cellBranchVolumeRatioThreshold;

    private double maximumChemotacticMicronsPerHour;
    private double maximumMigrationMicronsPerHour;
    private double collagenMigrationFactor;
    private double concentrationVectorWeight;
    private double persistenceVectorWeight;
    private double randomVectorWeight;
    //    private double baselineGrowthCubicMicronsPerHour;
    
    private String stateDiagramModelFileName;

    private double specialBranchProliferationFactor;
    private double specialBranchTimeHours;

    private double forcedBranchProliferationProbability;

    private boolean proliferationGrowthRateAdjustmentFlag = false;

    private double vegf0NgmlActivationProbability;
    private double vegf25NgmlActivationProbability;

    private double bdnf0NgmlActivationProbability;
    private double bdnf50NgmlActivationProbability;


    public Parameters() {
    }

    public Parameters(String fileName) {
	init(fileName);
	//	readInputParameters(fileName);
    }

    //    public Parameters(String fileName, boolean readFileNow) {
    //	init(fileName);
    //	if (readFileNow) {
    //	    readInputParameters(fileName);
    //	}
    //    }

    public void init(String fileName) {
	this.fileName = fileName;
	params = new ArrayList<shared.Parameter>();
	for (LabelType label : LabelType.values()) {
	    String[] values;
	    
	    // (3) Specify the number of values asociated with each
	    // label.  The default is 1 value.
	    switch (label) {
	    case DISABLE_REAR_CELL_PROLIFERATION:
	    case DISABLE_REAR_CELL_BRANCHING:
	    case DISABLE_ALL_CELL_BRANCHING:
	    case DISABLE_ALL_CELL_PROLIFERATION:
	    case DISABLE_MIGRATION:
		values = new String[0];
		break;
	    default: 
		values = new String[1];
	    }
	    shared.Parameter p = new Parameter(label.toString(), values, label);
	    params.add(p);
	}
    }	


    public void removeParameter(Object label) {
	if (label.getClass() != LABELTYPE_CLASS) {
	    SimpleRuleSet.die("[Parameters.removeParameter] Unsupported label class: "
			      + label.getClass());
	}
	LabelType lbl = (LabelType) label;
	for (Iterator<Parameter> i = params.iterator(); i.hasNext();) {
	    Parameter p = i.next();
	    if (p.getIdObject() == lbl) {
		i.remove();
		removed[lbl.ordinal()] = true;
		return;
	    }
	}
    }


    //    public void removeParameter(String parameterLabel) {
    //	for (Iterator<Parameter> i = params.iterator(); i.hasNext();) {
    //	    Parameter p = i.next();
    //	    if (p.getLabel().equals(parameterLabel)) {
    //		i.remove();
    //		return;
    //	    }
    //	}
    //    }


    public void readInputParameters() {
	if (fileName == null) {
	    SimpleRuleSet.die("[Parameters.readInputParameters] File name not specified");
	}
	readInputParameters(fileName);
    }

    public void readInputParameters(String fileName) {
	shared.Parameter.readParameters(fileName, params);
	for (Iterator<Parameter> i = params.iterator(); i.hasNext();) {
	    Parameter p = i.next();
	    processOneParameter(p);
	}
	parametersReadFromFile = true;
    }


    // Subclasses should override this method and call their super
    // class's processOneParameter method instead of dying.
    public void processOneParameter(Parameter p) {
	Object idObject = p.getIdObject();
	if (idObject.getClass() != LABELTYPE_CLASS) {
	    SimpleRuleSet.die("[Parameters.processOneParameter] Unknown label type: "
			      + p.getIdObject().getClass() + "  " + p);
	}
	// (4) Store the values read from the input file in local
	// fields.
	LabelType label = (LabelType) idObject;
	switch (label) {
	    //	case VEGF_ACTIVATION_THRESHOLD_NG_PER_ML:
	    //	    vegfActivationThresholdNgPerMl = p.getNonnegativeDouble(0);
	    //	    break;
	case MIGRATION_MAGNITUDE_VEGF_COEFFICIENT:
	    migrationMagnitudeVegfCoefficient = p.getNonnegativeDouble(0);
	    break;
	case BASELINE_MIGRATION_MICRONS_PER_HOUR:
	    baselineMigrationMicronsPerHour = p.getNonnegativeDouble(0);
	    break;
	case MIGRATION_CONCENTRATION_VECTOR_WEIGHT:
	    migrationConcentrationVectorWeight = p.getNonnegativeDouble(0);
	    break;
	case MIGRATION_PERSISTENCE_VECTOR_WEIGHT:
	    migrationPersistenceVectorWeight = p.getNonnegativeDouble(0);
	    break;
	case MIGRATION_RANDOM_VECTOR_WEIGHT:
	    migrationRandomVectorWeight = p.getNonnegativeDouble(0);
	    break;
	case MIGRATION_VARIANCE_ANGLE_DEGREES:
	    migrationVarianceAngleDegrees = p.getNonnegativeDouble(0);
	    if (migrationVarianceAngleDegrees > 90) {
		SimpleRuleSet.die("Migration variance angle " + migrationVarianceAngleDegrees
				  + " on line "
				  + p.getLineNumber() + " of file " + p.getFileName()
				  + " cannot be greater than 90 degrees");
	    }
	    break;
	case PROLIFERATION_BASELINE_GROWTH_RATE_PER_HOUR:
	    proliferationBaselineGrowthRatePerHour = p.getNonnegativeDouble(0);
	    break;
	case PROLIFERATION_ACTIVE_FRACTION:
	    proliferationActiveFraction = p.getPositiveDouble(0);
	    if (proliferationActiveFraction > 1) {
		SimpleRuleSet.die("PROLIFERATION_ACTIVE_FRACTION parameter: " 
				  + proliferationActiveFraction + " on line "
				  + p.getLineNumber() + " of file "
				  + p.getFileName()
				  + " is greater than 1.");
	    }
	    break;
	case PROLIFERATION_VEGF_COEFFICIENT_1:
	    proliferationVegfCoefficient1 = p.getDouble(0);
	    break;
	case PROLIFERATION_BDNF_THRESHOLD_NG_ML:
	    proliferationBdnfThresholdNgMl = p.getDouble(0);
	    break;
	case PROLIFERATION_BDNF_COEFFICIENT_2:
	    proliferationBdnfCoefficient2 = p.getDouble(0);
	    break;
	case PROLIFERATION_BDNF_COEFFICIENT_3:
	    proliferationBdnfCoefficient3 = p.getDouble(0);
	    break;
	case PROLIFERATION_BDNF_CONSTANT_4:
	    proliferationBdnfConstant4 = p.getDouble(0);
	    break;
	case DISABLE_REAR_CELL_PROLIFERATION:
	    disableRearCellProliferation = p.present();
	    break;
	case DISABLE_ALL_CELL_PROLIFERATION:
	    disableAllCellProliferation = p.present();
	    break;
	case DISABLE_REAR_CELL_BRANCHING:
	    disableRearCellBranching = p.present();
	    break;
	case DISABLE_ALL_CELL_BRANCHING:
	    disableAllCellBranching = p.present();
	    break;
	case DISABLE_MIGRATION:
	    disableMigration = p.present();
	    break;
	    //	case NOMINAL_CELL_VOLUME_CUBIC_MICRONS:
	    //	    if (p.present()) {
	    //		nominalCellVolumeCubicMicrons = p.getPositiveDouble(0);
	    //		nominalCellVolumeSpecified = true;
	    //	    }
	    //	    else {
	    //		nominalCellVolumeSpecified = false;
	    //	    }
	    //	    break;
	case MINIMUM_BRANCH_ANGLE_DEGREES:
	    minimumBranchAngleDegrees = p.getDouble(0);
	    break;
	case MAXIMUM_BRANCH_ANGLE_DEGREES:
	    maximumBranchAngleDegrees = p.getPositiveDouble(0);
	    break;
	case INITIAL_BRANCH_LENGTH_MICRONS:
	    initialBranchLengthMicrons = p.getPositiveDouble(0);
	    break;
	case INITIAL_BRANCH_RADIUS_MICRONS:
	    initialBranchRadiusMicrons = p.getPositiveDouble(0);
	    break;
	case CELL_BRANCH_VOLUME_RATIO_THRESHOLD:
	    cellBranchVolumeRatioThreshold = p.getPositiveDouble(0);
	    if (cellBranchVolumeRatioThreshold <= 1) {
		SimpleRuleSet.die("Cell branch volume ratio threshold: "
				  + cellBranchVolumeRatioThreshold
				  + " on line " + p.getLineNumber() + " of file "
				  + p.getFileName()
				  + " must be greater than 1");
	    }
	    break;
	case MAXIMUM_CHEMOTACTIC_MICRONS_PER_HOUR:
	    maximumChemotacticMicronsPerHour = p.getNonnegativeDouble(0);
	    break;
	case MAXIMUM_MIGRATION_MICRONS_PER_HOUR:
	    maximumMigrationMicronsPerHour = p.getNonnegativeDouble(0);
	    break;
	case COLLAGEN_MIGRATION_FACTOR:
	    collagenMigrationFactor = p.getNonnegativeDouble(0);
	    break;
	    //	    case BASELINE_GROWTH_CUBIC_MICRONS_PER_HOUR:
	    //		baselineGrowthCubicMicronsPerHour = p.getNonnegativeDouble(0);
	    //		break;
	case STATE_DIAGRAM_MODEL_FILE_NAME:
	    if (p.present()) {
		stateDiagramModelFileName = p.getString(0);
	    }
	    else {
		stateDiagramModelFileName = null;
	    }
	    break;
	case SPECIAL_BRANCH_PROLIFERATION_FACTOR:
	    specialBranchProliferationFactor = p.getNonnegativeDouble(0);
	    break;
	case SPECIAL_BRANCH_TIME_HOURS:
	    specialBranchTimeHours = p.getNonnegativeDouble(0);
	    break;
	case FORCED_BRANCH_PROLIFERATION_PROBABILITY:
	    forcedBranchProliferationProbability = p.getNonnegativeDouble(0);
	    if (forcedBranchProliferationProbability > 1) {
		SimpleRuleSet.die("Forced branch proliferation probability parameter: "
				  + forcedBranchProliferationProbability
				  + " is greater than 1 on line "
				  + p.getLineNumber() + " of file " 
				  + p.getFileName());
	    }
	    break;
	case VEGF_0_NGML_ACTIVATION_PROBABILITY:
	    vegf0NgmlActivationProbability = p.getNonnegativeDouble(0);
	    if (vegf0NgmlActivationProbability > 1) {
		SimpleRuleSet.die("Parameter " + label + ": "
				  + vegf0NgmlActivationProbability
				  + " is greater than 1 on line "
				  + p.getLineNumber() + " of file "
				  + p.getFileName());
	    }
	    break;
	case VEGF_25_NGML_ACTIVATION_PROBABILITY:
	    vegf25NgmlActivationProbability = p.getNonnegativeDouble(0);
	    if (vegf25NgmlActivationProbability > 1) {
		SimpleRuleSet.die("Parameter " + label + ": "
				  + vegf25NgmlActivationProbability
				  + " is greater than 1 on line "
				  + p.getLineNumber() + " of file "
				  + p.getFileName());
	    }
	    break;
	case BDNF_0_NGML_ACTIVATION_PROBABILITY:
	    bdnf0NgmlActivationProbability = p.getNonnegativeDouble(0);
	    if (bdnf0NgmlActivationProbability > 1) {
		SimpleRuleSet.die("Parameter " + label + ": "
				  + bdnf0NgmlActivationProbability
				  + " is greater than 1 on line "
				  + p.getLineNumber() + " of file "
				  + p.getFileName());
	    }
	    break;
	case BDNF_50_NGML_ACTIVATION_PROBABILITY:
	    bdnf50NgmlActivationProbability = p.getNonnegativeDouble(0);
	    if (bdnf50NgmlActivationProbability > 1) {
		SimpleRuleSet.die("Parameter " + label + ": "
				  + bdnf50NgmlActivationProbability
				  + " is greater than 1 on line "
				  + p.getLineNumber() + " of file "
				  + p.getFileName());
	    }
	    break;
	default:
	    SimpleRuleSet.die("[Parameters.processOneParameters] Unsupported label type: "
			      + label);
	}
    }
    

    public void signalProliferationGrowthRateAdjustment() {
	proliferationGrowthRateAdjustmentFlag = true;
    }


    /*
     * (5) Place access methods for parameter values here.
     *
     * If a sublclass removes the parameter, then the subclass should
     * override the method to signal an error if the corresponding
     * method is called.
     */

    //    public double getVegfActivationThresholdNgPerMl() {
    //	if (!parametersReadFromFile) {	
    //	    SimpleRuleSet.die("[Parameters.getVegfActivationThresholdNgPerMl] Parameters not yet read from file");
    //	}
    //	if (removed[LabelType.VEGF_ACTIVATION_THRESHOLD_NG_PER_ML.ordinal()]) {
    //	    SimpleRuleSet.die("[Parameters.getVegfActivationThresholdNgPerMl] "
    //			      + " Parameter has been removed");
    //	}
    //	return vegfActivationThresholdNgPerMl;
    //    }
    

    public double getMigrationMagnitudeVegfCoefficient() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getMigrationMagnitudeVegfCoefficient] Parameters not yet read from file");
	}
	if (removed[LabelType.MIGRATION_MAGNITUDE_VEGF_COEFFICIENT.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getMigrationMagnitudeVegfCoefficient] "
			      + " Parameter has been removed");
	}
	return migrationMagnitudeVegfCoefficient;
    }

    public double getBaselineMigrationMicronsPerHour() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getBaselineMigrationMicronsPerHour] Parameters not yet read from file");
	}
	if (removed[LabelType.BASELINE_MIGRATION_MICRONS_PER_HOUR.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getBaselineMigrationMicronsPerHour] "
			      + " Parameter has been removed");
	}
	return baselineMigrationMicronsPerHour;
    }


    public double getProliferationBaselineGrowthRatePerHour() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getProliferationBaselineGrowthrateperHour] Parameters not yet read from file");
	}
	if (removed[LabelType.PROLIFERATION_BASELINE_GROWTH_RATE_PER_HOUR.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getProliferationBaselineGrowthRatePerHour] "
			      + " Parameter has been removed");
	}
	return proliferationBaselineGrowthRatePerHour;
    }

    public double getProliferationActiveFraction() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getProliferationActiveFraction] Parameters not yet read from file");
	}
	if (removed[LabelType.PROLIFERATION_ACTIVE_FRACTION.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getProliferationActiveFraction] "
			      + " Parameter has been removed");
	}
	return proliferationActiveFraction;
    }

    public double getProliferationVegfCoefficient1() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getProliferationVegfCoefficient1] Parameters not yet read from file");
	}
	if (removed[LabelType.PROLIFERATION_VEGF_COEFFICIENT_1.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getProliferationVegfCoefficient1] "
			      + " Parameter has been removed");
	}
	return proliferationVegfCoefficient1;
    }

    public double getProliferationBdnfThresholdNgMl() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getProliferationBdnfThresholdNgMl] Parameters not yet read from file");
	}
	if (removed[LabelType.PROLIFERATION_BDNF_THRESHOLD_NG_ML.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getProliferationBdnfThresholdNgMl] "
			      + " Parameter has been removed");
	}
	return proliferationBdnfThresholdNgMl;
    }

    public double getProliferationBdnfCoefficient2() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getProliferationBdnfCoefficient2] Parameters not yet read from file");
	}
	if (removed[LabelType.PROLIFERATION_BDNF_COEFFICIENT_2.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getProliferationBdnfCoefficient2] "
			      + " Parameter has been removed");
	}
	return proliferationBdnfCoefficient2;
    }

    public double getProliferationBdnfCoefficient3() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getProliferationBdnfCoefficient3] Parameters not yet read from file");
	}
	if (removed[LabelType.PROLIFERATION_BDNF_COEFFICIENT_3.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getProliferationBdnfCoefficient3] "
			      + " Parameter has been removed");
	}
	return proliferationBdnfCoefficient3;
    }

    public double getProliferationBdnfConstant4() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getProliferationBdnfConstant4] Parameters not yet read from file");
	}
	if (removed[LabelType.PROLIFERATION_BDNF_CONSTANT_4.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getProliferationBdnfConstant4] "
			      + " Parameter has been removed");
	}
	return proliferationBdnfConstant4;
    }

    public boolean disableRearCellProliferation() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.disableRearCellProliferation] Parameters not yet read from file");
	}
	if (removed[LabelType.DISABLE_REAR_CELL_PROLIFERATION.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.disableRearCellProliferation] "
			      + " Parameter has been removed");
	}
	return disableRearCellProliferation;
    }

    public boolean disableAllCellProliferation() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.disableAllCellProliferation] Parameters not yet read from file");
	}
	if (removed[LabelType.DISABLE_ALL_CELL_PROLIFERATION.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.disableAllCellProliferation] "
			      + " Parameter has been removed");
	}
	return disableAllCellProliferation;
    }

    public boolean disableRearCellBranching() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.disableRearCellBranching] Parameters not yet read from file");
	}
	if (removed[LabelType.DISABLE_REAR_CELL_BRANCHING.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.disableRearCellBranching] "
			      + " Parameter has been removed");
	}
	return disableRearCellBranching;
    }

    public boolean disableAllCellBranching() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.disableAllCellBranching] Parameters not yet read from file");
	}
	if (removed[LabelType.DISABLE_ALL_CELL_BRANCHING.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.disableAllCellBranching] "
			      + " Parameter has been removed");
	}
	return disableAllCellBranching;
    }

    public boolean disableMigration() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.disableMigration] Parameters not yet read from file");
	}
	if (removed[LabelType.DISABLE_MIGRATION.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.disableMigration] "
			      + " Parameter has been removed");
	}
	return disableMigration;
    }

    //    public double getNominalCellVolumeCubicMicrons() {
    //	if (removed[LabelType.NOMINAL_CELL_VOLUME_CUBIC_MICRONS.ordinal()]) {
    //	    SimpleRuleSet.die("[Parameters.getNominalCellVolumeCubicMicrons] "
    //			      + " Parameter has been removed");
    //	}
    //	if (!nominalCellVolumeSpecified) {
    //	    SimpleRuleSet.die("Parameter " + LabelType.NOMINAL_CELL_VOLUME_CUBIC_MICRONS
    //			      + " not specified in file: "
    //			      + fileName);
    //	}
    //	return nominalCellVolumeCubicMicrons;
    //    }

    public double getMinimumBranchAngleDegrees() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getMinimumBranchAngleDegrees] Parameters not yet read from file");
	}
	if (removed[LabelType.MINIMUM_BRANCH_ANGLE_DEGREES.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getMinimumBranchAngleDegrees] "
			      + " Parameter has been removed");
	}
	return minimumBranchAngleDegrees;
    }

    public double getMaximumBranchAngleDegrees() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getMaximumBranchAngleDegrees] Parameters not yet read from file");
	}
	if (removed[LabelType.MAXIMUM_BRANCH_ANGLE_DEGREES.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getMaximumBranchAngleDegrees] "
			      + " Parameter has been removed");
	}
	return maximumBranchAngleDegrees;
    }

    public double getInitialBranchLengthMicrons() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getInitialBranchLengthMicrons] Parameters not yet read from file");
	}
	if (removed[LabelType.INITIAL_BRANCH_LENGTH_MICRONS.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getInitialBranchLengthMicrons] "
			      + " Parameter has been removed");
	}
	return initialBranchLengthMicrons;
    }

    public double getInitialBranchRadiusMicrons() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getInitialBranchRadiusMicrons] Parameters not yet read from file");
	}
	if (removed[LabelType.INITIAL_BRANCH_RADIUS_MICRONS.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getInitialBranchRadiusMicrons] "
			      + " Parameter has been removed");
	}
	return initialBranchRadiusMicrons;
    }

    public double getCellBranchVolumeRatioThreshold() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters.getCellBranchVolumeThreshold] Parameters not yet read from file");
	}
	if (removed[LabelType.CELL_BRANCH_VOLUME_RATIO_THRESHOLD.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getCellBranchVolumeRatioThreshold] "
			      + " Parameter has been removed");
	}
	return cellBranchVolumeRatioThreshold;
    }

    public double getMaximumChemotacticMicronsPerHour () {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getMaximumChemotacticMicronsPerHour] Parameters not yet read from file");
	}
	if (removed[LabelType.MAXIMUM_CHEMOTACTIC_MICRONS_PER_HOUR.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getMaximumChemotacticMicronsPerHour] "
			      + " Parameter has been removed");
	}
    	return maximumChemotacticMicronsPerHour ;
    }

    public double getMaximumMigrationMicronsPerHour () {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getMaximumMigrationMicronsPerHour] Parameters not yet read from file");
	}
	if (removed[LabelType.MAXIMUM_MIGRATION_MICRONS_PER_HOUR.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getMaximumMigrationMicronsPerHour] "
			      + " Parameter has been removed");
	}
	return maximumMigrationMicronsPerHour ;
    }

    public double getCollagenMigrationFactor () {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getCollagenMigrationFactor] Parameters not yet read from file");
	}
	if (removed[LabelType.COLLAGEN_MIGRATION_FACTOR.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getCollagenMigrationFactor] "
			      + " Parameter has been removed");
	}
	return collagenMigrationFactor ;
    }

    public double getMigrationConcentrationVectorWeight () {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getMigrationConcentrationVectorWeight] Parameters not yet read from file");
	}
	if (removed[LabelType.MIGRATION_CONCENTRATION_VECTOR_WEIGHT.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getMigrationConcentrationVectorWeight] "
			      + " Parameter has been removed");
	}
	return migrationConcentrationVectorWeight ;
    }

    public double getMigrationPersistenceVectorWeight () {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getMigrationPersistenceVectorWeight] Parameters not yet read from file");
	}
	if (removed[LabelType.MIGRATION_PERSISTENCE_VECTOR_WEIGHT.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getMigrationPersistenceVectorWeight] "
			      + " Parameter has been removed");
	}
	return migrationPersistenceVectorWeight ;
    }

    public double getMigrationRandomVectorWeight () {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getMigrationRandomVectorWeight] Parameters not yet read from file");
	}
	if (removed[LabelType.MIGRATION_RANDOM_VECTOR_WEIGHT.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getMigrationRandomVectorWeight] "
			      + " Parameter has been removed");
	}
	return migrationRandomVectorWeight ;
    }


    

    //    public double getBaselineGrowthCubicMicronsPerHour() {
    //	if (removed[LabelType.BASELINE_GROWTH_CUBIC_MICRONS_PER_HOUR.ordinal()]) {
    //	    SimpleRuleSet.die("[Parameters.getBaselineGrowthCubicMicronsPerHour] "
    //			      + " Parameter has been removed");
    //	}
    //	return baselineGrowthCubicMicronsPerHour;
    //    }

    public double getMigrationVarianceAngleDegrees() {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getMigrationVarianceAngleDegrees] Parameters not yet read from file");
	}
	if (removed[LabelType.MIGRATION_VARIANCE_ANGLE_DEGREES.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getMigrationVarianceAngleDegrees] "
			      + " Parameter has been removed");
	}
	return migrationVarianceAngleDegrees;
    }

    public String getStateDiagramModelFileName() {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getStateDiagramModelFileName] Parameters not yet read from file");
	}
	if (removed[LabelType.STATE_DIAGRAM_MODEL_FILE_NAME.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getStateDiagramModelFileName] "
			      + " Parameter has been removed");
	}
	return stateDiagramModelFileName;
    }

    public double getSpecialBranchProliferationFactor() {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getSpecialBranchProliferationFactor] Parameters not yet read from file");
	}
	if (removed[LabelType.SPECIAL_BRANCH_PROLIFERATION_FACTOR.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getSpecialBranchProliferationFactor] "
			      + " Parameter has been removed");
	}
	return specialBranchProliferationFactor;
    }

    public double getSpecialBranchTimeHours() {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getSpecialBranchTimeHours] Parameters not yet read from file");
	}
	if (removed[LabelType.SPECIAL_BRANCH_TIME_HOURS.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getSpecialBranchTimeHours] "
			      + " Parameter has been removed");
	}
	return specialBranchTimeHours;
    }

    public double getForcedBranchProliferationProbability() {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getForcedBranchProliferationProbability] Parameters not yet read from file");
	}
	if (removed[LabelType.FORCED_BRANCH_PROLIFERATION_PROBABILITY.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getForcedBranchProliferationProbability] "
			      + " Parameter has been removed");
	}
	return forcedBranchProliferationProbability;
    }



    public double getVegf0NgmlActivationProbability() {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getVegf0NgmlActivationProbability] Parameters not yet read from file");
	}
	if (removed[LabelType.VEGF_0_NGML_ACTIVATION_PROBABILITY.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getVegf0NgmlActivationProbability] "
			      + " Parameter has been removed");
	}
	return vegf0NgmlActivationProbability;
    }


    public double getVegf25NgmlActivationProbability() {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getVegf25NgmlActivationProbability] Parameters not yet read from file");
	}
	if (removed[LabelType.VEGF_25_NGML_ACTIVATION_PROBABILITY.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getVegf25NgmlActivationProbability] "
			      + " Parameter has been removed");
	}
	return vegf25NgmlActivationProbability;
    }


    public double getBdnf0NgmlActivationProbability() {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getBdnf0NgmlActivationProbability] Parameters not yet read from file");
	}
	if (removed[LabelType.BDNF_0_NGML_ACTIVATION_PROBABILITY.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getBdnf0NgmlActivationProbability] "
			      + " Parameter has been removed");
	}
	return bdnf0NgmlActivationProbability;
    }


    public double getBdnf50NgmlActivationProbability() {
	if (!parametersReadFromFile) {
	    SimpleRuleSet.die("[Parameters.getBdnf50NgmlActivationProbability] Parameters not yet read from file");
	}
	if (removed[LabelType.BDNF_50_NGML_ACTIVATION_PROBABILITY.ordinal()]) {
	    SimpleRuleSet.die("[Parameters.getBdnf50NgmlActivationProbability] "
			      + " Parameter has been removed");
	}
	return bdnf50NgmlActivationProbability;
    }


    public boolean adjustProliferationGrowthRate() {
	return proliferationGrowthRateAdjustmentFlag;
    }

    public static void main(String[] args) {
	Parameters p = new Parameters(args[0]);
	p.removeParameter(Parameters.LabelType.PROLIFERATION_ACTIVE_FRACTION);
	p.readInputParameters();
	//	System.out.println(p.getNominalCellVolumeCubicMicrons());
	System.out.println("[Parameters] getSpecialBranchProliferationFactor()="
			   + p.getSpecialBranchProliferationFactor());
	System.out.println("[Parameters] getSpecialBranchTimeHours()="
			   + p.getSpecialBranchTimeHours());
	System.out.println("[Parameters] getForcedBranchProliferationProbability()="
			   + p.getForcedBranchProliferationProbability());
    }



}

