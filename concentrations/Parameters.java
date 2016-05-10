
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

package concentrations;

import shared.*;

import java.util.*;
import java.lang.*;
import java.io.*;

public class Parameters {

    /*
     * Internal representation of parameter file labels.
     *
     * (1) Place new input labels here.
     */
    private static enum LabelType {VEGF_NG_PER_ML, BDNF_NG_PER_ML};
    

    /*
     * File parameter values
     *
     * (2) Place new field declarations for storing file parameter values here.
     */
    private double vegfNgPerMl;
    private double bdnfNgPerMl;


    public Parameters(String fileName) {
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
	    case VEGF_NG_PER_ML:
		vegfNgPerMl = p.getNonnegativeDouble(0);
		break;
	    case BDNF_NG_PER_ML:
		bdnfNgPerMl = p.getNonnegativeDouble(0);
		break;

	    default:
		ConcentrationsManager.die("[Parameters.readInputParameters] Unsupported label type: "
					  + label);
	    }
	}
    }


    /*
     * (5) Place access methods for parameter values here.
     */

    public double getVegfNgPerMl() {
	return vegfNgPerMl;
    }

    public double getBdnfNgPerMl() {
	return bdnfNgPerMl;
    }





}

