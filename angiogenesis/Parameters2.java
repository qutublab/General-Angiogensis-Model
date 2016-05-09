
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
    {MIGRATION_CONSTANT_1, MIGRATION_CONSTANT_2};
    

    
    private static Class LABELTYPE_CLASS = null;
    static {
	try {
	    LABELTYPE_CLASS = Class.forName("angiogenesis.Parameters2$LabelType");
	}
	catch (Exception e) {
	    SimpleRuleSet.die("[Parameters2] Unable to get class for LabelType");
	}
    }



    /*
     * File parameter values
     *
     * (2) Place new field declarations for storing file parameter values here.
     */
    protected double migrationConstant1;
    protected double migrationConstant2;
    
    public Parameters2(String fileName) {
	super(fileName);
	init();
	//	readInputParameters(fileName);
    }


    //    public Parameters2(String fileName, boolean readFileNow) {
    //	super(fileName, false);
    //	init();
    //	if (readFileNow) {
    //	    readInputParameters(fileName);
    //	}
    //    }

    private void init() {
	removeParameter(Parameters.LabelType.MIGRATION_MAGNITUDE_VEGF_COEFFICIENT);
        for (LabelType label : LabelType.values()) {
            String[] values;
	    
            // (3) Specify the number of values asociated with each
            // label.  The default is 1 value.
            switch (label) {
            default:
                values = new String[1];
            }
            shared.Parameter p = new Parameter(label.toString(), values, label);
	    //	    System.out.println("***[Parameters2.init] " + label + "  " + label.getClass());
	    //	    System.out.println(p);
            params.add(p);
        }
    }


    // This method must be here to override the super class's
    // readInputParameters method in order that this class's
    // processOneParameter is initially invoked instead of that of the
    // super class.
    public void readInputParameters(String fileName) {
        shared.Parameter.readParameters(fileName, params);
        for (Iterator<Parameter> i = params.iterator(); i.hasNext();) {
            Parameter p = i.next();
            processOneParameter(p);
        }
	parametersReadFromFile = true;
    }

    public void readInputParameters() {
	if (fileName == null) {
	    SimpleRuleSet.die("[Parameters2.readInputParameters] File name not specified");
	}
	readInputParameters(fileName);
    }



    // Subclasses should override this method and call their super
    // class's processOneParameter method instead of dying.
    public void processOneParameter(Parameter p) {
	Object idObject = p.getIdObject();
	if (idObject.getClass() != LABELTYPE_CLASS) {
	    super.processOneParameter(p);
	    return;
	}
	// (4) Store the values read from the input file in local
	// fields.
	LabelType label = (LabelType) idObject;
	switch (label) {
	case MIGRATION_CONSTANT_1:
	    migrationConstant1 = p.getNonnegativeDouble(0);
	    break;
	case MIGRATION_CONSTANT_2:
	    migrationConstant2 = p.getNonnegativeDouble(0);
	    break;
	default:
	    SimpleRuleSet.die("[Parameters2.processOneParameters] Unsupported label type: "
			      + label);
	}
    }


    /*
     * (5) Place access methods for parameter values here.
     */


    public double getMigrationConstant1() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters2.getMigrationConstant1] Parameters not yet read from file");
	}
	return migrationConstant1;
    }

    public double getMigrationConstant2() {
	if (!parametersReadFromFile) {	
	    SimpleRuleSet.die("[Parameters2.getMigrationConstant2] Parameters not yet read from file");
	}
	return migrationConstant2;
    }

    // Override methods for parameters that were removed when the object was created

    public double getMigrationMagnitudeVegfCoefficient() {
	SimpleRuleSet.die("[Parameters2.getMigrationMagnitudeVegfCoefficient] " 
			  + "Attempt to use removed parameter");
        return 0;
    }


    public static void main(String[] args) {
	Parameters2 p = new Parameters2(args[0]);
	System.out.println(p.getMigrationConstant1());
    }


}