
package tools;

import shared.*;

import java.io.*;

public class StateDiagramModelEditor {

    public static enum State {IDLE, MIGRATION_ELONGATION, PROLIFERATION, BRANCHING};
    public static final int NUMBER_OF_STATES = State.values().length;


    private String fileName;
    private boolean unsavedChanges;
    private boolean unnormalizedChanges;
    private StateDiagramModel model = null;
    private StateDiagramModelResult sdmr = null;
    

    private static final String[] TIP_TRANSITION_LABELS =
    {"Idle->Idle", "Idle->Migr", "Idle->Prol", "Idle->Bran",
     "Migr->Idle", "Migr->Migr", "Migr->Prol", "Migr->Bran",
     "Prol->Idle", "Prol->Migr", "Prol->Prol", "Prol->Bran",
     "Bran->Idle", "Bran->Migr", "Bran->Prol", "Bran->Bran"};
    private static final String[] STALK_TRANSITION_LABELS =
    {"Idle->Idle", "Idle->Prol", "Idle->Bran",
     "Elon->Idle", "Elon->Prol", "Elon->Bran",
     "Prol->Idle", "Prol->Prol", "Prol->Bran",
     "Bran->Idle", "Bran->Prol", "Bran->Bran"};

    private static final int TIP_TRANSITION_GROUP_SIZE = 4;
    private static final int STALK_TRANSITION_GROUP_SIZE = 3;

    private static final int NUMBER_OF_TIP_TRANSITIONS =
	TIP_TRANSITION_LABELS.length;

    private static final int NUMBER_OF_STALK_TRANSITIONS =
	STALK_TRANSITION_LABELS.length;

    private double[] tipTransitionArray = null;
    private double[] stalkTransitionArray = null;


    public StateDiagramModelEditor() {
    }

    public StateDiagramModelEditor(String fileName) {
	String status = read(fileName);
	if (status != null) {
	    System.out.println(status);
	}
	else {
	    tipTransitionArray = createTipTransitionRepresentation(model);
	    stalkTransitionArray = createStalkTransitionRepresentation(model);
	    unnormalizedChanges = false;
	    unsavedChanges = false;
	    this.fileName = fileName;
	    System.out.println("Model read from file");
	}
    }

    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }

    private String read(String fileName) {
	String status = null;
	Object obj = null;
	try {
	    FileInputStream fis = new FileInputStream(fileName);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    obj = ois.readObject();
	    ois.close();
	}
	catch (Exception e) {
	    status = "Unable to read file: " + fileName + " " + e.toString();
	}
	if (status == null) {
	    String className = obj.getClass().getName();
	    if (className.equals("shared.StateDiagramModel")) {
		sdmr = null;
		model = (StateDiagramModel) obj;
	    }
	    else {
		if (className.equals("shared.StateDiagramModelResult")) {
		    sdmr = (StateDiagramModelResult) obj;
		    model = sdmr.model;
		}
		else {
		    status =
			"File " + fileName + " contains an object of class "
			+ className;
		}
	    }
	}
	return status;
    }
    


    private static String write(String fileName, Object obj) {
	String status = null;
	try {
	    FileOutputStream fos = new FileOutputStream(fileName);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(obj);
	    oos.close();
	}
	catch (Exception e) {
	    status =
		"Unable to write to file " + fileName + "   " + e.toString();
	}
	return status;
    }
    
    

    private double[] createTipTransitionRepresentation(StateDiagramModel model) {
	double[] rep = new double[NUMBER_OF_TIP_TRANSITIONS];
	rep[0] = model.tipQuiescentToQuiescent;
	rep[1] = model.tipQuiescentToMigration;
	rep[2] = model.tipQuiescentToProliferation;
	rep[3] = model.tipQuiescentToBranching;

	rep[4] = model.tipMigrationToQuiescent;
	rep[5] = model.tipMigrationToMigration;
	rep[6] = model.tipMigrationToProliferation;
	rep[7] = model.tipMigrationToBranching;
	
	rep[8] = model.tipProliferationToQuiescent;
	rep[9] = model.tipProliferationToMigration;
	rep[10] = model.tipProliferationToProliferation;
	rep[11] = model.tipProliferationToBranching;

	rep[12] = model.tipBranchingToQuiescent;
	rep[13] = model.tipBranchingToMigration;
	rep[14] = model.tipBranchingToProliferation;
	rep[15] = model.tipBranchingToBranching;

	return rep;
    }


    private double[] createStalkTransitionRepresentation(StateDiagramModel model) {
	double[] rep = new double[NUMBER_OF_STALK_TRANSITIONS];
	rep[0] = model.stalkQuiescentToQuiescent;
	rep[1] = model.stalkQuiescentToProliferation;
	rep[2] = model.stalkQuiescentToBranching;

	rep[3] = model.stalkElongationToQuiescent;
	rep[4] = model.stalkElongationToProliferation;
	rep[5] = model.stalkElongationToBranching;
	
	rep[6] = model.stalkProliferationToQuiescent;
	rep[7] = model.stalkProliferationToProliferation;
	rep[8] = model.stalkProliferationToBranching;

	rep[9] = model.stalkBranchingToQuiescent;
	rep[10] = model.stalkBranchingToProliferation;
	rep[11] = model.stalkBranchingToBranching;

	return rep;
    }

    private void convert(StateDiagramModel model,
			 double[] tipTransitionArray,
			 double[] stalkTransitionArray) {
	
	model.tipQuiescentToQuiescent = tipTransitionArray[0];
	model.tipQuiescentToMigration = tipTransitionArray[1];
	model.tipQuiescentToProliferation = tipTransitionArray[2];
	model.tipQuiescentToBranching = tipTransitionArray[3];
	model.tipMigrationToQuiescent = tipTransitionArray[4];
	model.tipMigrationToMigration = tipTransitionArray[5];
	model.tipMigrationToProliferation = tipTransitionArray[6];
	model.tipMigrationToBranching = tipTransitionArray[7];
	model.tipProliferationToQuiescent = tipTransitionArray[8];
	model.tipProliferationToMigration = tipTransitionArray[9];
	model.tipProliferationToProliferation = tipTransitionArray[10];
	model.tipProliferationToBranching = tipTransitionArray[11];
	model.tipBranchingToQuiescent = tipTransitionArray[12];
	model.tipBranchingToMigration = tipTransitionArray[13];
	model.tipBranchingToProliferation = tipTransitionArray[14];
	model.tipBranchingToBranching = tipTransitionArray[15];
       
	model.stalkQuiescentToQuiescent = stalkTransitionArray[0];
	model.stalkQuiescentToProliferation = stalkTransitionArray[1];
	model.stalkQuiescentToBranching = stalkTransitionArray[2];
	model.stalkElongationToQuiescent = stalkTransitionArray[3];
	model.stalkElongationToProliferation = stalkTransitionArray[4];
	model.stalkElongationToBranching = stalkTransitionArray[5];
	model.stalkProliferationToQuiescent = stalkTransitionArray[6];
	model.stalkProliferationToProliferation = stalkTransitionArray[7];
	model.stalkProliferationToBranching = stalkTransitionArray[8];
	model.stalkBranchingToQuiescent = stalkTransitionArray[9];
	model.stalkBranchingToProliferation = stalkTransitionArray[10];
	model.stalkBranchingToBranching = stalkTransitionArray[11];
    }


    private void score() {
	if (model == null) {
	    System.out.println("No model loaded");
	}
	else {
	    if (sdmr == null) {
		System.out.println("No score for model");
	    }
	    else {
		System.out.println("Score: " + sdmr.score);
		System.out.print("Score precomponents: ");
		for (int i = 0; i < sdmr.individualScores.length; i++) {
		    double[] initialConditionScores = sdmr.individualScores[i];
		    for (int j = 0; j < initialConditionScores.length; j++) {
			if (j == 0) {
			    System.out.print("(");
			}
			else {
			    System.out.print(", ");
			}
			System.out.print(initialConditionScores[j]);
		    }
		    System.out.print(") ");
		}
		System.out.println();
	    }
	}
    }

    private void stats() {
	if (model == null) {
	    System.out.println("No model loaded");
	}
	else {
	    if (sdmr == null) {
		System.out.println("No statistics for model (sdmr=null");
	    }
	    else {
		if (sdmr.stats == null) {
		    System.out.println("No statistics for model (stats=null)");
		}
		else {
		    SimulationStats[] stats = sdmr.stats;
		    int numStats = stats.length;
		    int numInitialConditions = 
			StateDiagramModelResult.InitialConditions.values().length;
		    if (numStats != numInitialConditions) {
			System.out.println("Expected stats for "
					   + numInitialConditions
					   + " initial conditions, but found "
					   + numStats);
		    }
		    else {
			for (StateDiagramModelResult.InitialConditions ic : StateDiagramModelResult.InitialConditions.values()) {
			    BasicStats avg = stats[ic.ordinal()].average;
			    BasicStats stDev = 
				stats[ic.ordinal()].standardDeviation;
			    System.out.println(ic
					       + " limitedXYSproutLengthMicrons av="
					       + avg.limitedXYSproutLengthMicrons
					       + " sd="
					       + stDev.limitedXYSproutLengthMicrons);
			    System.out.println(ic
					       + " limitedXYBranchCount av="
					       + avg.limitedXYBranchCount
					       + " sd="
					       + stDev.limitedXYBranchCount);
			    System.out.println(ic
					       + " individualLimitedXYBranchlengthMicrons av="
					       + avg.individualLimitedXYBranchLengthsMicrons.get(0)
					       + " sd="
					       + stDev.individualLimitedXYBranchLengthsMicrons.get(0));
			}
		    }
		}
	    }
	}
    }

    private static void displayTransitions(double[] tipTransitions,
					   double[] stalkTransitions) {
	System.out.println("Tip Cell");
	for (int i = 0; i < tipTransitions.length; i ++) {
	    System.out.println("[" + i + "] " + TIP_TRANSITION_LABELS[i] + ": "
			       + tipTransitions[i]);
	}
	System.out.println();
	System.out.println("Stalk Cell");
	for (int i = 0; i < stalkTransitions.length; i ++) {
	    System.out.println("[" + (i + tipTransitions.length) + "] "
			       + STALK_TRANSITION_LABELS[i] + ": "
			       + stalkTransitions[i]);
	}
    }

    private String normalizeTransitions(double[] rep, double newRep[],
					int groupSize, int offset) {
	String status = null;
	for (int i = 0; i <= rep.length - groupSize; i += groupSize) {
	    double total = 0;
	    for (int j = i; j < i + groupSize; j++) {
		double p = rep[j];
		if (p < 0) {
		    status =
			"Value at index " + (j + offset) + " is negative.";
		    return status;
		}
		total += p;
	    }
	    if (total <= 0) {
		System.out.println("groupSize=" + groupSize + " offset="
				   + offset);
		status =
		    groupSize + " transition probabilities starting at index "
		    + (i + offset) + " do not sum to a positive number";
		return status;
	    }
	    for (int j = i; j < i + groupSize; j++) {
		newRep[j] = rep[j] / total;
	    }
	}
	return status;
    }


    private static String promptRead(String s) {
	String reply = "";
	int bufferSize = 20;
	byte[] b = new byte[bufferSize];
	int count = 0;
	System.out.print(s);
	do {
	    try {
		count = System.in.read(b);
		String replyPortion = new String(b, 0, count); 
		reply += replyPortion;
	    }
	    catch (Exception e) {
		die ("[StateDiagramModelEditor.prompt] unable to read.  "
		     + e.toString());
	    }
	} while (count == bufferSize);
	return reply.trim();
    }

    private void open() {
	if (unnormalizedChanges) {
	    System.out.println("The current model is not normalized");
	    return;
	}
	if (unsavedChanges) {
	    System.out.println("There are unsaved changes");
	    return;
	}
	String localFileName = promptRead("File name: ");
	String status = read(localFileName);
	if (status != null) {
	    System.out.println(status);
	}
	else {
	    tipTransitionArray = createTipTransitionRepresentation(model);
	    stalkTransitionArray = createStalkTransitionRepresentation(model);
	    unnormalizedChanges = false;
	    unsavedChanges = false;
	    fileName = localFileName;
	    System.out.println("Model read from file");
	}
    }

    private void display() {
	if (tipTransitionArray != null) {
	    displayTransitions(tipTransitionArray, stalkTransitionArray);
	}
    }

    private void create() {
	if (unsavedChanges) {
	    System.out.println("There are unsaved changes");
	    return;
	}
	sdmr = null;
	model = new StateDiagramModel();
	tipTransitionArray = new double[NUMBER_OF_TIP_TRANSITIONS];
	stalkTransitionArray = new double[NUMBER_OF_STALK_TRANSITIONS];
	unnormalizedChanges = false;
	unsavedChanges = false;
	System.out.println("New model created");
    }

    private void edit() {
	if (tipTransitionArray == null) {
	    System.out.println("There is no model to change");
	    return;
	}
	String prompt = "Enter index and value ('x' to exit): ";
	boolean valid = true;
	do {
	    String indexAndValue = promptRead(prompt);
	    if (indexAndValue.equalsIgnoreCase("x")) {
		valid = true;
		continue;
	    }
	    int spaceIndex = indexAndValue.indexOf(' ');
	    if (spaceIndex == -1) {
		valid = false;
		continue;
	    }
	    String indexStr = indexAndValue.substring(0, spaceIndex);
	    String valueStr = indexAndValue.substring(spaceIndex + 1);
	    int index;
	    double value;
	    try {
		index = Integer.parseInt(indexStr);
		value = Double.parseDouble(valueStr);
	    }
	    catch (Exception e) {
		System.out.println("Invalid input.");
		valid = false;
		continue;
	    }
	    if (index < 0
		|| index >= NUMBER_OF_TIP_TRANSITIONS + NUMBER_OF_STALK_TRANSITIONS) {
		System.out.println("Index " + index 
				   + " is out of bounds");
		valid = false;
		continue;
	    }
	    if (value < 0) {
		System.out.println("Value " + value + " is negative");
		valid = false;
		continue;
	    }
	    if (index < NUMBER_OF_TIP_TRANSITIONS) {
		tipTransitionArray[index] = value;
	    }
	    else {
		stalkTransitionArray[index - NUMBER_OF_TIP_TRANSITIONS] =
		    value;
	    }
	    valid = true;
	    unnormalizedChanges = true;
	    unsavedChanges = true;
	    System.out.println("Model updated");
	} while (!valid);
    }

    private void normalize() {
	if (tipTransitionArray == null) {
	    System.out.println("There is no model being edited");
	}
	else {
	    double[] newTipTransitionArray =
		new double[NUMBER_OF_TIP_TRANSITIONS];
	    double[] newStalkTransitionArray =
		new double[NUMBER_OF_STALK_TRANSITIONS];
	    String status =
		normalizeTransitions(tipTransitionArray, newTipTransitionArray,
				     TIP_TRANSITION_GROUP_SIZE, 0);
	    if (status != null) {
		System.out.println(status);
	    }
	    else {
		status =
		    normalizeTransitions(stalkTransitionArray,
					 newStalkTransitionArray, 
					 STALK_TRANSITION_GROUP_SIZE,
					 NUMBER_OF_TIP_TRANSITIONS);
		if (status != null) {
		    System.out.println(status);
		}
		else {
		    tipTransitionArray = newTipTransitionArray;
		    stalkTransitionArray = newStalkTransitionArray;
		    unnormalizedChanges = false;
		    System.out.println("Model normalized");
		}
	    }
	}
    }
							  

    private void save() {
	if (model == null) {
	    System.out.println("There is no model to save");
	    return;
	}
	if (unnormalizedChanges) {
	    System.out.println("The current model is not normalized");
	    return;
	}
	convert(model, tipTransitionArray, stalkTransitionArray);
	boolean retry = false;
	String reply;
	do {
	    String prompt;
	    if (fileName == null) {
		prompt = "Save to file: ";
	    }
	    else {
		prompt = "Save to file [default " + fileName + "]: ";
	    }
	    reply = promptRead(prompt);
	} while (fileName == null && reply.equals(""));


	String localFileName = reply.equals("")? fileName : reply;
	
	String status;
	if (sdmr == null) {
	    status = write(localFileName, model);
	}
	else {
	    sdmr.model = model;
	    status = write(localFileName, sdmr);
	}
	if (status != null) {
	    System.out.println(status);
	}
	else {
	    System.out.println("Model saved to file " + localFileName);
	    fileName = localFileName;
	    unsavedChanges = false;
	}
    }


    private boolean quit() {
	if (unnormalizedChanges) {
	    System.out.println("The current model is not normalized");
	    return false;
	}
	if (unsavedChanges) {
	    System.out.println("There unsaved changes");
	    return false;
	}
	return true;
    }


    private void predict() {
	if (model == null) {
	    System.out.println("There is no model for prediction");
	    return;
	}
	if (unnormalizedChanges) {
	    System.out.println("The current model is not normalized");
	    return;	    
	}
	String prompt = "Number of time steps: ";
	boolean validInput;
	do {
	    validInput = true;
	    String reply = promptRead(prompt);
	    int steps = 0;
	    try {
		steps = Integer.parseInt(reply);
	    }
	    catch (Exception e) {
		validInput = false;
	    }
	    if (validInput && steps <= 0) {
		validInput = false;
	    }
	    if (validInput) {
		StateDiagramModel m = new StateDiagramModel();
		convert(m, tipTransitionArray, stalkTransitionArray);
		Examine.computeStateBreakdown(m, steps);
	    }
	    else {
		System.out.println("Number of time steps must be a positive integer");
	    }
	} while (!validInput);		
    }
	
	private void commandResponseLoop() {
	String prompt = "s[C]ore [D]isplay, [E]dit, [N]ew, [O]pen, [P]redict, [Q]uit, no[R]malize, [S]ave, s[T]ats: ";
	char commandChar;
	boolean quitStatus = false;
	do {
	    String command = promptRead(prompt);
	    if (command.length() == 1) {
		commandChar = Character.toUpperCase(command.charAt(0));
		switch (commandChar) {
		case 'C': score(); break;
		case 'D': display(); break;
		case 'E': edit(); break;
		case 'N': create(); break;
		case 'O': open(); break;
		case 'P': predict(); break;
		case 'R': normalize(); break;
		case 'S': save(); break;
		case 'T': stats(); break;
		case 'Q': quitStatus = quit(); break;
		default:
		}
	    }
	}
	while (!quitStatus);
    }
		    
	    



    public static void main(String[] args) throws Exception {
	StateDiagramModelEditor sdme = null;
	switch (args.length) {
	case 0:
	    sdme = new StateDiagramModelEditor();
	    break;
	case 1:
	    sdme = new StateDiagramModelEditor(args[0]);
	    break;
	default:
	    die("[StateDiagramModelEditor.main] Unexpected number of arguments: "
		+ args.length);
	}
	sdme.commandResponseLoop();
    }


}