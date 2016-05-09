

/*
 * 2/26/2011 - Created
 *
 *
 * The Parameter class is used for specifying and storing input parameters.
 *
 * To use the readParameters method, create an ArrayList of parameters
 * and make sure that the following fields are given values:
 *
 *  label - specifies the name of the parameter label in the file.
 *  values - the size of this array specifies the maximum number of
 *      values that can follow the label (on the same line) in the
 *      input file.
 *  idNumber - a (unique) number by which the ArrayList is sorted
 *      after filling by the method.  This allows easier access to the
 *      parameters by the caller.
 * 
 * The readParameters method expects all values associated with a
 * label to immediately follow the label and be on the same line as
 * the label.  File items (labels and values) are separated by
 * whitespace (spaces, tabs and end-of-lines).  More than one group of
 * label and values can be on a single line, but the readParameter
 * method associates each value with the label it follows until either
 * a) the parameter's values array is filled or b) the end of the line
 * is reached.  The readParameters method does not attempt to parse
 * the file contents and stores what it has read as strings in the
 * values array. In the case of the input line ending before teh
 * values array is filled, any values array element not filled from
 * the file is set to null.
 *
 * After using the readParameters method to read from an input file,
 * each Parameter can parse itself.  The getX(index) methods return
 * the result of parsing the parameter's value at position index
 * expecting it to be of type X.  So, for example if a file contains
 * three-dimensional coordinates that can be specified as either
 * cartesian or spherical, an input file might contain the following
 * line:
 *
 *       coordinates cartesian 2.5 3.8 1.4
 *
 * where the label is 'coordinates'.  After using the readParameters
 * method to read the file, the Parameter object p with label 'coordinates'
 * has 4 values associated with it.  One could extract them as
 * follows:
 *
 *       String coordSystem = p.getString(0);
 *       if (coordSystem.equals("cartesian")) {
 *         x = p.getDouble(1);
 *         y = p.getDouble(2);
 *         z = p.getDouble(3);
 *       }
 *       if (coordSystem.equals("spherical")) {
 *         r = p.getDouble(1);
 *         theta = p.getDouble(2);
 *         psi = p.getDouble(3);
 *       }
 *
 * One could also assume the input to have the form:
 *
 *       coordinates x 2.5 y 3.8 z 1.4
 *
 * in which case the Parameter object in the readParameter method's
 * ArrayList argument would have a values array of size 6.
 *
 * Flags by their nature are labels without associated values.  Their
 * presence can be checked by the present method:
 *
 *       boolean flag = p.present();
 *
 * This also allows optional parameters:
 *
 *       if (p.present()) {
 *         x = p.getDouble(0);
 *       }
 *       else {
 *         x = defaultValue;
 *       }
 *
 * Note that each of the getX(index) methods generate an error if the
 * there is a null value at the index position (indicating missing
 * input file content) or the parsed value is not of the correct type.
 * The error message includes the parameter label, line number and
 * file name.  Of course, extra error checking can be done and each
 * Parameter object makes available its line number and file name for
 * creating error messages:
 *
 *       int num = p.getPositiveInt(0);
 *       if (num > 10) {
 *         System.err.println(p.getLabel() + " parameter: " + num
 *                            + " is out of range on line number "
 *                            p.getLineNumber() + " of file "
 *                            p.getFileName());
 *       }
 */

/*
  
  Example use:

  public enum Label {A, B, C}

  ArrayList<Parameter> params = new ArrayList<Parameter>();
  for (Label lbl : Label.values()) {
    int index = lbl.ordinal();
    int size;
    switch {
    case A: size = 2; break;
    case B: size = 0; break;
    case C: size = 1; break;
    default: die();
    }
    String values = new String[size];
    params.add(new Parameter(lbl.toString(), values, index));
  }
    
  Parameter.readParameters(fileName, params);
    
  for (Label lbl : label.values()) {
    Parameter p = params.get(lbl.ordinal());
    switch (lbl) {
    case A:
      paramA0 = p.getNonnegativeDouble(0);
      paramA1 = p.getDouble(1);
      break;
   
    case B:    etc... 
    }
  }

  public double getparamA0() {
  return paramA0;
  }
    
    
*/
    
package shared;
import java.util.*;
import java.io.*;
import java.math.*;

// When reading synchronize on labels
    
public class Parameter {
    
    private final String label;
    private final String[] values;
    private boolean found;
    private String fileName;
    private int lineNumber;
    private final int idNumber;
    private final Object idObject;

    public Parameter(String label) {
	this(label, null, -1);
    }

    public Parameter(String label, String[] values, int idNumber) {
	this.label = label;
	this.values = values;
	this.idNumber = idNumber;
	this.idObject = null;
    }

    public Parameter(String label, String[] values, Object idObject) {
	this.label = label;
	this.values = values;
	this.idNumber = -1;
	this.idObject = idObject;
    }


    public String[] getValues() {
	return values;
    }

    public void setPresent(boolean found) {
	this.found = found;
    }

    public String getLabel() {
	return label;
    }

    public int getLineNumber() {
	return lineNumber;
    }

    public String getFileName() {
	return fileName;
    }

    public Object getIdObject() {
	return idObject;
    }

    public String toString() {
	String valuesStr;
	if (values == null) {
	    valuesStr = null;
	}
	else {
	    valuesStr = "";
	    for (String s: values) {
		if (valuesStr.equals("")) {
		    valuesStr = "" + s;   // need "" so that null is forced into a string
		}
		else {
		    valuesStr += "," + s;
		}
	    }
	}
	if (values != null) {
	    valuesStr = "[" + valuesStr + "]";
	}

	String retVal;
	retVal = "Parameter["
	    + "label=" + label
	    + ",values=" + valuesStr
	    + ",found=" + found
	    + ",fileName=" + fileName
	    + ",lineNumber=" + lineNumber
	    + ",idNumber=" + idNumber
	    + ",idObject=" + idObject
	    + "]";
	return retVal;
    }

    public static class LabelComparatorIgnoreCase implements Comparator<Parameter> {
	public int compare(Parameter p1, Parameter p2) {
	    return p1.label.compareToIgnoreCase(p2.label);
	}
	public boolean equals(Parameter p1, Parameter p2) {
	    return p1.label.equalsIgnoreCase(p2.label);
	}
    }

    public static class IdNumberComparator implements Comparator<Parameter> {
	public int compare(Parameter p1, Parameter p2) {
	    return p1.idNumber - p2.idNumber;
	}
	public boolean equals(Parameter p1, Parameter p2) {
	    return (p1.idNumber == p2.idNumber);
	}
    }

    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }


    
    /*
     * Reads the input file for the parameters specified by the params
     * argument and sorts params by the idNumber field.
     */
    public static void readParameters(String fileName, ArrayList<Parameter> params) {
	// sort params by label field
	LabelComparatorIgnoreCase labelComp = new LabelComparatorIgnoreCase();
	Collections.sort(params, labelComp);

	// set found fields to false and set filename fields
	for (Parameter p : params) {
	    p.found = false;
	    p. fileName = fileName;
	}

	// open file
	File f = new File(fileName);
	FileReader fr = null;
	try {
	    fr = new FileReader(f);
	}
	catch (Exception e) {
	    die("Unable to create file reader.   " + e.toString());
	}
	BufferedReader br = new BufferedReader(fr);

	// prep tokenizer
	MyStreamTokenizer st = new MyStreamTokenizer(br);
	st.setInputName(fileName);
	st.eolIsSignificant(true);
	// It appears that a) // and /**/ comments are always
	// recognized regardless of the use of the slashSlashComments
	// and alsahStarComments methods (even when given false as an
	// argument) and b) A single slash (/) is recognized as the
	// start of a comment.
	//
	// *** So, to get the slash character read as part of a
	// *** parameter (such as a file name), put the parameter in
	// *** quote marks, e.g. "hello/there"
	//
       	st.slashSlashComments(false);
	st.slashStarComments(false);
	st.wordChars('_', '_');
	// StreamTokenizer (the super class of MyStreamTokenizer) is
	// broken and parseNumbers() is called by default the
	// following two lines are a workaround See:
	// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4223533
	st.ordinaryChars('0', '9');
	st.wordChars('0', '9');

       	st.ordinaryChars('-', '-');
	st.wordChars('-', '-');
       	st.ordinaryChars('.', '.');
	st.wordChars('.', '.');




	// process file contents
	int tkn = st.nextNonEOLToken();
	
	//	System.out.println("[parameter.readParameter] TT_EOF=" + st.TT_EOF + " TT_EOL=" + st.TT_EOL
	//			   + " TT_NUMBER" + st.TT_NUMBER + " TT_WORD=" + st. TT_WORD);

	while (tkn != st.TT_EOF) {

	    /*
	     * Since eol tokens are skipped at this point, only word
	     * and number tokens are possible.
	     */
	
	    if (tkn == st.TT_NUMBER) {
		die("Unexpected number: " + st.nval + " on line " + st.lineno()
		    + " of file " + fileName);
	    }
	    String label = st.sval;
	    Parameter key = new Parameter(label);
	    int parameterIndex = Collections.binarySearch(params, key, labelComp);
	    if (parameterIndex < 0) {
		die("Unexpected input: " + st.sval + " on line " + st.lineno() + " of file "
		    + fileName);
	    }
	    Parameter p = params.get(parameterIndex);
	    if (p.found) {
		die("Found second occurrence of label: " + st.sval + " on line " + st.lineno()
		    + " of file " + fileName);
	    }
	    p.found = true;
	    p.lineNumber = st.lineno();
	    if (p.values != null) {
		int i = 0;
		tkn = st.nextToken();
		while (i < p.values.length && tkn != st.TT_EOL && tkn != st.TT_EOF) {
		    if (tkn != st.TT_WORD) {
			die("Unexpected input: \'" + (char) tkn + "\' (" + tkn
			    + ") on line " + st.lineno()
			    + " of file " + fileName);
		    }
		    p.values[i] = st.sval;
		    i++;
		    if (i < p.values.length) {
			tkn = st.nextToken();
		    }
		}
		// set rest of values array to null
		while (i < p.values.length) {
		    p.values[i] = null;
		    i ++;
		}
	    }
	    tkn = st.nextNonEOLToken();
	}

        // sort params by idNumber
	IdNumberComparator idNumComp = new IdNumberComparator();
	Collections.sort(params, idNumComp);

	try {
	    br.close();
	}
	catch (Exception e) {
	    die("[Parameter.readParameters] Unable to close buffered reader   "
		+ e.toString());
	}
    }
    
    
    public boolean present() {
	return found;
    }


    public String getString(int index) {
	if (!found) {
	    die("Parameter: " + label + " not found in file " + fileName);
	}
	if (index < 0 || index >= values.length) {
	    die("[Parameter.getString] index " + index + " for parameter "
		+ label + " does not exist!");
	}
	String val = values[index];
	if (val == null) {
	    die("Missing " + label + " parameter(s) on line " + lineNumber + " of file "
		+ fileName);
	}
	return values[index];
    }
    
    public double getDouble(int index) {
	String val = getString(index);
	double dbl = 0;
	try {
	    dbl = Double.parseDouble(val);
	}
	catch (Exception e) {
	    die("Expected a number on line " + lineNumber + " in file "
		+ fileName + " but found: " + val);
	}
	if (dbl == Double.NEGATIVE_INFINITY) {
	    die ("Parameter " + values[index] + " on line " + lineNumber + " of file "
		 + fileName + " is too small");
	}
	if (dbl == Double.POSITIVE_INFINITY) {
	    die ("Parameter " + values[index] + " on line " + lineNumber + " of file "
		 + fileName + " is too large");
	}
	return dbl;
    }
    
    public double getPositiveDouble(int index) {
	double dbl = getDouble(index);
	if (dbl <= 0) {
	    die("Expected a positive number; found: " + values[index] + " on line "
		+ lineNumber + " of file " + fileName);
	}
	return dbl;
    }

    public double getNonnegativeDouble(int index) {
	double dbl = getDouble(index);
	if (dbl < 0) {
	    die("Expected a nonnegative number; found: " + values[index] + " on line "
		+ lineNumber + " of file " + fileName);
	}
	return dbl;
    }

    public long getLong(int index) {
	String val = getString(index);
	long lng = 0;
	try {
	    lng = Long.parseLong(val);
	}
	catch (Exception e) {
	    die("Parameter " + val + " on line " + lineNumber + " of file "
		+ fileName + " is not a proper integer or is out of range");
	}
	return lng;
    }

    public long getNonnegativeLong(int index) {
	long num = getLong(index);
	if (num < 0) {
	    die("Expected a nonnegative integer; found: " + num + " on line "
		+ lineNumber + " of file " + fileName);
	}
	return num;
    }

    public int getInt(int index) {
	long num = getLong(index);
	if (num > Integer.MAX_VALUE || num < Integer.MIN_VALUE) {
	    die("Parameter " + num + " on line " + lineNumber
		+ " of file " + fileName + " is out of range");
	}
	return (int) num;
    }

    public int getPositiveInt(int index) {
	int num = getInt(index);
	if (num <= 0) {
	    die("Expected a positive integer; found: " + num + " on line "
		+ lineNumber + " of file " + fileName);
	}
	return num;
    }

    public int getNonnegativeInt(int index) {
	int num = getInt(index);
	if (num < 0) {
	    die("Expected a negative integer; found: " + num + " on line "
		+ lineNumber + " of file " + fileName);
	}
	return num;
    }


    public enum TestLabel {CC, BB, AA};
    private static void test(String fileName) {
	String[] v = new String[] {"1", "2"};
	Parameter a = new Parameter("LABEL", v, 100);
	System.out.println("[Parameter.test] a=" + a);

	String[] v2 = new String[2];
	Parameter a2 = new Parameter("LABEL2", v2, 200);
	System.out.println("[Parameter.test] a2=" + a2);

	ArrayList<Parameter> params = new ArrayList<Parameter>(TestLabel.values().length);
	//	System.out.println("[Parameter.test] size=" + params.size() + "  "
	//			   + TestLabel.values().length);
	int size = 3;
	for (TestLabel lbl: TestLabel.values()) {
	    int index = lbl.ordinal();
	    String[] values = new String[size];
	    Parameter p = new Parameter(lbl.toString(), values, index);
	    params.add(p);
	    //	    System.out.println("p.values=" + p.values + "  p.values.length=" + p.values.length);
	}

	Parameter.readParameters(fileName, params);
	
	for (Parameter p: params) {
	    System.out.println(p);
	}
	Parameter p;
	p = params.get(TestLabel.AA.ordinal());
	System.out.println(p.getString(0) + " " + p.getString(1) + " " + p.getInt(1) + " "
			   + p.getString(2));
	p = params.get(TestLabel.BB.ordinal());
	System.out.println(p.getInt(0) + " " + p.getString(1)  + " "
			   + p.getPositiveDouble(2));
	p = params.get(TestLabel.CC.ordinal());
	System.out.println(p.getString(0) + " " + p.getNonnegativeInt(1)  + " "
			   + p.getString(2));
    }


    public static void main(String[] args) {
	test(args[0]);
    }
}    