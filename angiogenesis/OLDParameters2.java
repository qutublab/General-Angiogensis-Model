
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

public class Parameters2 extends Parameters {

    /*
     * Internal representation of parameter file labels.
     *
     * (1) Place new input labels here.
     */
    private static enum LabelType 
    {VEGF_ACTIVATION_THRESHOLD_NG_PER_ML,
	    //     MIGRATION_MAGNITUDE_BDNF_COEFFICIENT, MIGRATION_MAGNITUDE_VEGF_COEFFICIENT,
     MIGRATION_CONSTANT_1, MIGRATION_CONSTANT_2,
     BASELINE_MIGRATION_MICRONS_PER_HOUR, 
     MIGRATION_CONCENTRATION_VECTOR_WEIGHT, MIGRATION_PERSISTENCE_VECTOR_WEIGHT,
     MIGRATION_RANDOM_VECTOR_WEIGHT,
     MIGRATION_VARIANCE_ANGLE_DEGREES,
   
     PROLIFERATION_BASELINE_GROWTH_CUBIC_MICRONS_PER_HOUR,
     PROLIFERATION_VEGF_COEFFICIENT_1, PROLIFERATION_VEGF_CONSTANT_2,
     PROLIFERATION_BDNF_COEFFICIENT_3, PROLIFERATION_BDNF_CONSTANT_4,
     PROLIFERATION_BDNF_COEFFICIENT_5, PROLIFERATION_BDNF_CONSTANT_6,
	    
     MINIMUM_BRANCH_ANGLE_DEGREES, MAXIMUM_BRANCH_ANGLE_DEGREES,
     INITIAL_BRANCH_LENGTH_MICRONS,
     INITIAL_BRANCH_RADIUS_MICRONS,
     CELL_BRANCH_VOLUME_RATIO_THRESHOLD,

	    MAXIMUM_CHEMOTACTIC_MICRONS_PER_HOUR,
	    MAXIMUM_MIGRATION_MICRONS_PER_HOUR, COLLAGEN_MIGRATION_FACTOR,

	    //	    BASELINE_GROWTH_CUBIC_MICRONS_PER_HOUR
	    };
    

    /*
     * File parameter values
     *
     * (2) Place new field declarations for storing file parameter values here.
     */
    private double vegfActivationThresholdNgPerMl;
    //    private double migrationMagnitudeVegfCoefficient;
    //    private double migrationMagnitudeBdnfCoefficient;
    private double migrationConstant1;
    private double migrationConstant2;
    private double baselineMigrationMicronsPerHour;
    private double migrationConcentrationVectorWeight;
    private double migrationPersistenceVectorWeight;
    private double migrationRandomVectorWeight;
    private double migrationVarianceAngleDegrees;
    private double proliferationBaselineGrowthCubicMicronsPerHour;
    private double proliferationVegfCoefficient1;
    private double proliferationVegfConstant2;
    private double proliferationBdnfCoefficient3;
    private double proliferationBdnfConstant4;
    private double proliferationBdnfCoefficient5;
    private double proliferationBdnfConstant6;
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
    private double baselineGrowthCubicMicronsPerHour;
    
    public Parameters2(String fileName) {
	readInputParameters(fileName);
    }


    private void readInputParameters(String fileName) {

	ArrayList<shared.Parameter> params = new ArrayList<shared.Parameter>();
	for (LabelType label : LabelType.values()) {
	    String[] values;
	    
	    // (3) Specify the number of values asociated with each
	    // label.  The default is 1 value.
	    switch (label) {
	    default: 
		values = new String[1];
	    }
	    shared.Parameter p = new Parameter(label.toString(), values, label.ordinal());
	    params.add(p);
	}
	shared.Parameter.readParameters(fileName, params);

	// (4) Store the values read from the input file in local
	// fields.
	for (LabelType label : LabelType.values()) {
	    shared.Parameter p = params.get(label.ordinal());
	    switch (label) {
	    case VEGF_ACTIVATION_THRESHOLD_NG_PER_ML:
		vegfActivationThresholdNgPerMl = p.getNonnegativeDouble(0);
		break;
		//	    case MIGRATION_MAGNITUDE_VEGF_COEFFICIENT:
		//		migrationMagnitudeVegfCoefficient = p.getNonnegativeDouble(0);
		//		break;
		//	    case MIGRATION_MAGNITUDE_BDNF_COEFFICIENT:
		//		migrationMagnitudeBdnfCoefficient = p.getNonnegativeDouble(0);
		//		break;
	    case MIGRATION_CONSTANT_1:
		migrationConstant1 = p.getDouble(0);
		break;
	    case MIGRATION_CONSTANT_2:
		migrationConstant2 = p.getDouble(0);
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
	    case PROLIFERATION_BASELINE_GROWTH_CUBIC_MICRONS_PER_HOUR:
		proliferationBaselineGrowthCubicMicronsPerHour = p.getNonnegativeDouble(0);
		break;
	    case PROLIFERATION_VEGF_COEFFICIENT_1:
		proliferationVegfCoefficient1 = p.getDouble(0);
		break;
	    case PROLIFERATION_VEGF_CONSTANT_2:
		proliferationVegfConstant2 = p.getDouble(0);
		break;
	    case PROLIFERATION_BDNF_COEFFICIENT_3:
		proliferationBdnfCoefficient3 = p.getDouble(0);
		break;
	    case PROLIFERATION_BDNF_CONSTANT_4:
		proliferationBdnfConstant4 = p.getDouble(0);
		break;
	    case PROLIFERATION_BDNF_COEFFICIENT_5:
		proliferationBdnfCoefficient5 = p.getDouble(0);
		break;
	    case PROLIFERATION_BDNF_CONSTANT_6:
		proliferationBdnfConstant6 = p.getDouble(0);
		break;

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
	    default:
		SimpleRuleSet.die("[Parameters.readInputParameters] Unsupported label type: "
				  + label);
	    }
	}
    }


    /*
     * (5) Place access methods for parameter values here.
     */

    public double getVegfActivationThresholdNgPerMl() {
	return vegfActivationThresholdNgPerMl;
    }


    public double getMigrationMagnitudeVegfCoefficient() {
	SimpleRuleSet.die("[Parameters2.getMigrationMagnitudeVegfCoefficient] Not supported!");
	return 0;
    }

    public double getMigrationMagnitudeBdnfCoefficient() {
	SimpleRuleSet.die("[Parameters2.getMigrationMagnitudeBdnfCoefficient] Not supported!");
	return 0;
    }

    public double getMigrationConstant1() {
	return migrationConstant1;
    }

    public double getMigrationConstant2() {
	return migrationConstant2;
    }

    public double getBaselineMigrationMicronsPerHour() {
	return baselineMigrationMicronsPerHour;
    }


    public double getProliferationBaselineGrowthCubicMicronsPerHour() {
	return proliferationBaselineGrowthCubicMicronsPerHour;
    }

    public double getProliferationVegfCoefficient1() {
	return proliferationVegfCoefficient1;
    }

    public double getProliferationVegfConstant2() {
	return proliferationVegfConstant2;
    }

    public double getProliferationBdnfCoefficient3() {
	return proliferationBdnfCoefficient3;
    }

    public double getProliferationBdnfConstant4() {
	return proliferationBdnfConstant4;
    }

    public double getProliferationBdnfCoefficient5() {
	return proliferationBdnfCoefficient5;
    }

    public double getProliferationBdnfConstant6() {
	return proliferationBdnfConstant6;
    }

    public double getMinimumBranchAngleDegrees() {
	return minimumBranchAngleDegrees;
    }

    public double getMaximumBranchAngleDegrees() {
	return maximumBranchAngleDegrees;
    }

    public double getInitialBranchLengthMicrons() {
	return initialBranchLengthMicrons;
    }

    public double getInitialBranchRadiusMicrons() {
	return initialBranchRadiusMicrons;
    }

    public double getCellBranchVolumeRatioThreshold() {
	return cellBranchVolumeRatioThreshold;
    }

    public double getMaximumChemotacticMicronsPerHour () {
    	return maximumChemotacticMicronsPerHour ;
    }

    public double getMaximumMigrationMicronsPerHour () {
	return maximumMigrationMicronsPerHour ;
    }

    public double getCollagenMigrationFactor () {
	return collagenMigrationFactor ;
    }

    public double getMigrationConcentrationVectorWeight () {
	return migrationConcentrationVectorWeight ;
    }

    public double getMigrationPersistenceVectorWeight () {
	return migrationPersistenceVectorWeight ;
    }

    public double getMigrationRandomVectorWeight () {
	return migrationRandomVectorWeight ;
    }

    public double getBaselineGrowthCubicMicronsPerHour() {
	return baselineGrowthCubicMicronsPerHour;
    }

    public double getMigrationVarianceAngleDegrees() {
	return migrationVarianceAngleDegrees;
    }



    public static void main(String[] args) {
	Parameters p = new Parameters(args[0]);
    }



}

