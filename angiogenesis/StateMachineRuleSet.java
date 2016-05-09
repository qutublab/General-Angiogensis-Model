
/*
 * 4/2/2011 
 * ProliferationRule2 used.
 *
 * 5/13/2011 
 * Old ProliferationRule abandoned.  New ProliferationRule is now old
 * ProliferationRule2
 *
 * 6/28/2011
 * Cells on branch can be forced to proliferate.
 */

/*

  The original state machine model recognized three types of sprout
  cells: tip, stalk and rear.  Stalk cells are the cells immediately
  behind tip cells and rear cells are any cells behind stalk cells.
  This distinction was made because stalk cells can elongate when
  their tip cells migrate, but cells behind stalk cells do not
  elongate.  However, it is possible for rear cells to be in the
  elongating state as follows: A tip cell migrates and forces its
  stalk cell to elongate.  In the next time step, a tip cell divides
  and the old stalk cell then becomes a rear cell, but is in the
  elongation state.  This caused a problem because the state diagram
  for rear cells did not have an elongation state.

  The current work-around is for rear cells to use the stalk cell
  state machine.  However, a migrating tip cell only forces its
  immediate succesor cell (the stalk cell) into an elongation state,
  so the rear cell will not move to the elongation state except as
  described above.

*/


package angiogenesis;

import shared.*;

import java.util.*;

public class StateMachineRuleSet implements RuleSetInterface {

    public static boolean debugFlag = false;
    public void setDebugFlag() {
	debugFlag = true;
    }

    private static final double TABLE_TOLERANCE = .0000001;


    public enum State {IDLE, PROLIFERATING, BRANCHING, MIGRATING_ELONGATING};

    public final int NUMBER_OF_STATES = State.values().length;

    private double[][] tipCellTransitionTable;
    private double[][] stalkCellTransitionTable;
    // non-tip and non-stalk cells
    //    private double[][] rearCellTransitionTable;
    

    protected Rule[] rules;


    protected EnvironmentInterface env;
    protected LogStreamInterface log;


    //    public static final String VERSION_STRING = "0.3";

    public static OutputBufferInterface buffer;
    public static RandomInterface random;

    public static String ruleSetIdentifier = "SM";
    public static String ruleSetName;


    public String getRuleSetIdentifier() {
	return ruleSetIdentifier;
    }
    
    public String getRuleSetName() {
	return ruleSetName;
    }
    
    protected Rule activationRule;
    protected Rule idleRule;
    protected Rule migrationRule;
    protected Rule proliferationRule;
    protected Rule branchingRule;

    protected String versionString = "0.1";

    protected double forcedBranchProliferationProbability = 0;


    public void initialize(EnvironmentInterface env) {
	this.env = env;
	log = env.getLog();
	Rule.setLog(log);
	buffer = env.getOutputBuffer();
	random = env.getRandom();
	Parameters p  = new Parameters(env.getAngiogenesisParametersFileName());
	p.readInputParameters();
	// Have the proliferation rule adjust its rate when it gets
	// its parameters.
	p.signalProliferationGrowthRateAdjustment();

	forcedBranchProliferationProbability = 
	    p.getForcedBranchProliferationProbability();

	activationRule = new ActivationRule(p, env);
	idleRule = new IdleRule(p, env);
	migrationRule = new MigrationRule(p, env);
	proliferationRule = new ProliferationRule(p, env);
	branchingRule = new BranchingRule(p, env);

	rules = new Rule[NUMBER_OF_STATES];
	rules[State.IDLE.ordinal()] = idleRule;
	rules[State.PROLIFERATING.ordinal()] = proliferationRule;
	rules[State.BRANCHING.ordinal()] = branchingRule;
	rules[State.MIGRATING_ELONGATING.ordinal()] = migrationRule;

	String stateDiagramModelFileName = p.getStateDiagramModelFileName();
	String modelSource = null;
	StateDiagramModel sdm;
	if (env.getSimulationMode() == EnvironmentInterface.SimulationMode.GENETIC_ALGORITHM
            || stateDiagramModelFileName == null) {
	    sdm = env.getStateDiagramModel();
	    modelSource = " from Environment";
	    if (env.getSimulationMode() == EnvironmentInterface.SimulationMode.GENETIC_ALGORITHM) {
		modelSource = " (genetic algorithm)";
	    }
	}
	else {
	    sdm = StateDiagramModelResult.read(stateDiagramModelFileName).model;
	    buffer.println("Model file: " + stateDiagramModelFileName);
	    modelSource = " from file " + stateDiagramModelFileName;
	}
	if (sdm == null) {
	    die("[StatemachineRuleSet.initialize] state diagram model is null");
	}
	setTransitionTables(sdm);

	//	System.out.println("[StateMachineRuleSet.initialize] testing setTransitionTables");
	//	testSetTransitionTables(sdm);
	//	System.exit(0);

	ruleSetName = "SM " + modelSource;


	buffer.println("Cell state code:");
	buffer.println("  " + State.IDLE.ordinal() + ": IDLE");
	buffer.println("  " + State.PROLIFERATING.ordinal()
		       + ": PROLIFERATING");
	buffer.println("  " + State.BRANCHING.ordinal() + ": BRANCHING");
	buffer.println("  " + State.MIGRATING_ELONGATING.ordinal()
		       + ": MIGRATING");
	buffer.println("  " + NUMBER_OF_STATES + ": ELONGATING");
    }


    private void testSetTransitionTables(StateDiagramModel sdm) {
	setTransitionTables(sdm);
	
	System.out.println("Tip cell transitions");
	System.out.print("  ");
	for (State s : State.values()) {
	    System.out.print(s + "    ");
	}
	System.out.println();
	for (State s : State.values()) {
	    System.out.print(s + "   ");
	    for (State d : State.values()) {
		System.out.print(tipCellTransitionTable[s.ordinal()][d.ordinal()] + "    ");
	    }
	    System.out.println();
	}
	System.out.println();

	System.out.println("Stalk cell transitions");
	System.out.print("  ");
	for (State s : State.values()) {
	    System.out.print(s + "    ");
	}
	System.out.println();
	for (State s : State.values()) {
	    System.out.print(s + "   ");
	    for (State d : State.values()) {
		System.out.print(stalkCellTransitionTable[s.ordinal()][d.ordinal()] + "    ");
	    }
	    System.out.println();
	}
	System.out.println();


	//	System.out.println("Rear cell transitions");
	//	System.out.print("  ");
	//	for (State s : State.values()) {
	//	    System.out.print(s + "    ");
	//	}
	//	System.out.println();
	//	for (State s : State.values()) {
	//	    System.out.print(s + "   ");
	//	    for (State d : State.values()) {
	//		System.out.print(rearCellTransitionTable[s.ordinal()][d.ordinal()] + "    ");
	//	    }
	//	    System.out.println();
	//	}
	//	System.out.println();
	//
	//	System.out.println(sdm);
    }


    protected void setTransitionTables(StateDiagramModel sdm) {
	tipCellTransitionTable = new double[NUMBER_OF_STATES][NUMBER_OF_STATES];

	tipCellTransitionTable[State.IDLE.ordinal()][State.IDLE.ordinal()] =
	    sdm.tipQuiescentToQuiescent;
	tipCellTransitionTable[State.IDLE.ordinal()][State.PROLIFERATING.ordinal()] =
	    sdm.tipQuiescentToProliferation;
	tipCellTransitionTable[State.IDLE.ordinal()][State.BRANCHING.ordinal()] =
	    sdm.tipQuiescentToBranching;
	tipCellTransitionTable[State.IDLE.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
	    sdm.tipQuiescentToMigration;

	tipCellTransitionTable[State.PROLIFERATING.ordinal()][State.IDLE.ordinal()] =
	    sdm.tipProliferationToQuiescent;
	tipCellTransitionTable[State.PROLIFERATING.ordinal()][State.PROLIFERATING.ordinal()] =
	    sdm.tipProliferationToProliferation;
	tipCellTransitionTable[State.PROLIFERATING.ordinal()][State.BRANCHING.ordinal()] =
	    sdm.tipProliferationToBranching;
	tipCellTransitionTable[State.PROLIFERATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
	    sdm.tipProliferationToMigration;

	tipCellTransitionTable[State.BRANCHING.ordinal()][State.IDLE.ordinal()] =
	    sdm.tipBranchingToQuiescent;
	tipCellTransitionTable[State.BRANCHING.ordinal()][State.PROLIFERATING.ordinal()] =
	    sdm.tipBranchingToProliferation;
	tipCellTransitionTable[State.BRANCHING.ordinal()][State.BRANCHING.ordinal()] =
	    sdm.tipBranchingToBranching;
	tipCellTransitionTable[State.BRANCHING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
	    sdm.tipBranchingToMigration;

	tipCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.IDLE.ordinal()] =
	    sdm.tipMigrationToQuiescent;
	tipCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.PROLIFERATING.ordinal()] =
	    sdm.tipMigrationToProliferation;
	tipCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.BRANCHING.ordinal()] =
	    sdm.tipMigrationToBranching;
	tipCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
	    sdm.tipMigrationToMigration;


	stalkCellTransitionTable = new double[NUMBER_OF_STATES][NUMBER_OF_STATES];

	stalkCellTransitionTable[State.IDLE.ordinal()][State.IDLE.ordinal()] =
	    sdm.stalkQuiescentToQuiescent;
	stalkCellTransitionTable[State.IDLE.ordinal()][State.PROLIFERATING.ordinal()] =
	    sdm.stalkQuiescentToProliferation;
	stalkCellTransitionTable[State.IDLE.ordinal()][State.BRANCHING.ordinal()] =
	    sdm.stalkQuiescentToBranching;
	stalkCellTransitionTable[State.IDLE.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;

	stalkCellTransitionTable[State.PROLIFERATING.ordinal()][State.IDLE.ordinal()] =
	    sdm.stalkProliferationToQuiescent;
	stalkCellTransitionTable[State.PROLIFERATING.ordinal()][State.PROLIFERATING.ordinal()] =
	    sdm.stalkProliferationToProliferation;
	stalkCellTransitionTable[State.PROLIFERATING.ordinal()][State.BRANCHING.ordinal()] =
	    sdm.stalkProliferationToBranching;
	stalkCellTransitionTable[State.PROLIFERATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;

	stalkCellTransitionTable[State.BRANCHING.ordinal()][State.IDLE.ordinal()] =
	    sdm.stalkBranchingToQuiescent;
	stalkCellTransitionTable[State.BRANCHING.ordinal()][State.PROLIFERATING.ordinal()] =
	    sdm.stalkBranchingToProliferation;
	stalkCellTransitionTable[State.BRANCHING.ordinal()][State.BRANCHING.ordinal()] =
	    sdm.stalkBranchingToBranching;
	stalkCellTransitionTable[State.BRANCHING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
	    0;

	stalkCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.IDLE.ordinal()] =
	    sdm.stalkElongationToQuiescent;
	stalkCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.PROLIFERATING.ordinal()] =
	    sdm.stalkElongationToProliferation;
	stalkCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.BRANCHING.ordinal()] =
	    sdm.stalkElongationToBranching;
	stalkCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;


	checkTransitionTable(tipCellTransitionTable, CellInterface.CellPosition.TIP, sdm);
	checkTransitionTable(stalkCellTransitionTable, CellInterface.CellPosition.STALK, sdm);

	//	rearCellTransitionTable = new double[NUMBER_OF_STATES][NUMBER_OF_STATES];
	//
	//	rearCellTransitionTable[State.IDLE.ordinal()][State.IDLE.ordinal()] =
	//	    sdm.rearQuiescentToQuiescent;
	//	rearCellTransitionTable[State.IDLE.ordinal()][State.PROLIFERATING.ordinal()] =
	//	    sdm.rearQuiescentToProliferation;
	//	rearCellTransitionTable[State.IDLE.ordinal()][State.BRANCHING.ordinal()] =
	//	    sdm.rearQuiescentToBranching;
	//	rearCellTransitionTable[State.IDLE.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;
	//
	//	rearCellTransitionTable[State.PROLIFERATING.ordinal()][State.IDLE.ordinal()] =
	//	    sdm.rearProliferationToQuiescent;
	//	rearCellTransitionTable[State.PROLIFERATING.ordinal()][State.PROLIFERATING.ordinal()] =
	//	    sdm.rearProliferationToProliferation;
	//	rearCellTransitionTable[State.PROLIFERATING.ordinal()][State.BRANCHING.ordinal()] =
	//	    sdm.rearProliferationToBranching;
	//	rearCellTransitionTable[State.PROLIFERATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;
	//
	//	rearCellTransitionTable[State.BRANCHING.ordinal()][State.IDLE.ordinal()] =
	//	    sdm.rearBranchingToQuiescent;
	//	rearCellTransitionTable[State.BRANCHING.ordinal()][State.PROLIFERATING.ordinal()] =
	//	    sdm.rearBranchingToProliferation;
	//	rearCellTransitionTable[State.BRANCHING.ordinal()][State.BRANCHING.ordinal()] =
	//	    sdm.rearBranchingToBranching;
	//	rearCellTransitionTable[State.BRANCHING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
	//	    0;
	//
	//	rearCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.IDLE.ordinal()] = 0;
	//	rearCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.PROLIFERATING.ordinal()] =
	//	    0;
	//	rearCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.BRANCHING.ordinal()] =
	//	    0;
	//	rearCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;
    }


    private void checkTransitionTable(double[][] table, 
				      CellInterface.CellPosition cellPosition,
				      StateDiagramModel sdm) {
	if (table.length != NUMBER_OF_STATES) {
	    die("[StateMachineRuleSet.checkTransitionTable] " + cellPosition + " cell table has "
		+ table.length + " rows; expected " + NUMBER_OF_STATES);
	}
	for (int r = 0; r < NUMBER_OF_STATES; r++) {
	    double[] row = table[r];
	    if (row.length != NUMBER_OF_STATES) {
		die("[StateMachineRuleSet.checkTransitionTable] " + cellPosition + " cell "
		    + State.values()[r] + " row (" + r + ") has " + row.length
		    + " elements; expected " + NUMBER_OF_STATES + "\n" + sdm);
	    }
	    double sum = 0;
	    for (int c = 0; c < NUMBER_OF_STATES; c++) {
		double p = row[c]; 
		//		System.out.print(p + "  ");
		if (p < 0) {
		die("[StateMachineRuleSet.checkTransitionTable] " + cellPosition + " cell "
		    + State.values()[r] + " to " + State.values()[c]
		    + " transition probability (" + r + "," + c + ") is negative: " + p
		    + "\n" + sdm);
		}
		sum += p;
	    }
	    //	    System.out.println(sum);
	    if (Math.abs(sum - 1) > TABLE_TOLERANCE) {
		die("[StateMachineRuleSet.checkTransitionTable] " + cellPosition
		    + " cell transition probabilities from state " + State.values()[r]
		    + " do not sum to 1: " + sum + "\n" + sdm);
	    }
	}
	// check that the stalk cell table has no transitions to its elongation state
	if (cellPosition == CellInterface.CellPosition.STALK ||
	    cellPosition == CellInterface.CellPosition.REAR) {
	    int elongIndex = State.MIGRATING_ELONGATING.ordinal();
	    for (int from = 0; from < NUMBER_OF_STATES; from++) {
		if (table[from][elongIndex] != 0) {
		    die("[StateMachineRuleSet.checkTransitionTable] "
			+ " stalk cell has a nonzero transition probability from "
			+ State.values()[from] + " to elongation state: "
			+ table[from][elongIndex]);
		}
	    }
	}
    }

		

		   
		
    public Object createLocalStorage(CellInterface c) {
	return new StateMachineRuleStorage();
    }

    public void actCell(CellInterface c,
			Object localStorage,
			EnvironmentInterface e) {
	StateMachineRuleStorage s = (StateMachineRuleStorage) localStorage;
	// resize newly divided cell in the time step after division
	if (s.divided) {
	    c.resize();
	    s.divided = false;
	}

	EnvironmentInterface.CellState state = c.getCellState();
	switch (state) {
	case IDLE:
	    activationRule.act(c, s, e);
	    break;
	case QUIESCENT:
	    break;
	case ACTIVE:
	    actSprout(c, s, e);
	    break;
	default:
	    die("[StateMachineRuleSet.actCell] Unknown cell state: " + state);
	}
    }

    private void actSprout(CellInterface c,
			   StateMachineRuleStorage s,
			   EnvironmentInterface e) {
	//	log.println("[StateMachineRuleSet.actSprout] Begin cell "
	//		    + c.getIdNumber() + " activity",
	//		    LogStreamInterface.BASIC_LOG_DETAIL);
	CellInterface.CellPosition cellPos = c.getCellPosition();
	
	// When the forcedElongation flag is set for a stalk cell, its
	// tip cell has already migrated and elongated it.  There is
	// nothing more for the stalk cell to do.  
	//
	// Note that in any given time-step, tip cells are processed
	// before other types of cells.
	
	if (s.forcedElongation) {
	    if (cellPos == CellInterface.CellPosition.STALK) {
		s.state = State.MIGRATING_ELONGATING;
		s.forcedElongation = false;
		//		System.out.println("[StateMachineRuleSet.actSprout] elongation "
		//				   + c.getIdNumber());
		return;
	    }
	    else {
		die("[StateMachineRuleSet.actSprout] Forced elongation of nonstalk cell: " + c);
	    }
	}

	double[][] transitionTable = null;
	switch (cellPos) {
	case TIP:
	    transitionTable = tipCellTransitionTable;
	    break;
	case STALK:
	    transitionTable = stalkCellTransitionTable;
	    break;
	case REAR:
	    // for now , rear cells use same tables as stalk cell;
	    // see comments at top of file
	    transitionTable = stalkCellTransitionTable;
	    break;
	default:
	    die("[StateMachineRuleSet.actSprout] Unsupported cell position: "
		+ cellPos);
	}
	
	State st = s.state;
	double[] transitionRow = transitionTable[st.ordinal()];
	
	double r = random.nextDouble();
	int i = 0;
	int lastStateIndex = NUMBER_OF_STATES - 1;
	State nextState = null;	
	while (i < NUMBER_OF_STATES && nextState == null) {
	    double prob = transitionRow[i];
	    
	    // Table probabilities may sum to 1 - TABLE_TOLERANCE, so
	    // if this is the last state, use it if r is within the
	    // tolerance.  if it is not whithin tolerance, then an
	    // error is signaled after the loop by testing nextState.
	    if (r < prob
		|| (i == lastStateIndex && r < prob + TABLE_TOLERANCE)) {
		nextState = State.values()[i];
	    }
	    else {
		r = r - prob;
	    }
	    i ++;
	}
	
	if (nextState == null) {
	    double total = 0;
	    for (double p : transitionRow) {
		total += p;
		System.out.print(p + "  ");
	    }
	    System.out.println(total);
	    die("[StateMachineRuleSet.actSprout] Unable to find the next state for a "
		+ st + " " + cellPos
		+ " cell " + c.getIdNumber());
	}
	
	// check for illegal transitions
	if ((cellPos == CellInterface.CellPosition.STALK
	     || cellPos == CellInterface.CellPosition.REAR)
	    && nextState == State.MIGRATING_ELONGATING) {
	    die("[StateMachineRuleSet.actSprout] Illegal " + cellPos
		+ " cell transition to MIGRATING_ELONGATING");
	}
	
	// Record the new state in the cell's storage.
	s.state = nextState;
	
	// Check if a branch cell should be forced to proliferate
	if (s.isBranch && e.stepsCompleted() <= s.lastSpecialBranchStep) {
	    switch (nextState) {
	    case IDLE:
		r = random.nextDouble();
		if (r < forcedBranchProliferationProbability) {
		    nextState = State.PROLIFERATING;
		    log.println("[StateMachineRuleSet.actSprout] Cell "
				+ c.getIdNumber()
				+ " on branch is forced to proliferate from idle",
				LogStreamInterface.BASIC_LOG_DETAIL);
		}
		break;
	    case MIGRATING_ELONGATING:
		if (c.isTipCell() && !c.hasBranchStalkCell()) {
		    r = random.nextDouble();
		    nextState = State.PROLIFERATING;
		    log.println("[StateMachineRuleSet.actSprout] Cell "
				+ c.getIdNumber()
				+ " on branch is forced to proliferate from migration",
				LogStreamInterface.BASIC_LOG_DETAIL);

		}
		break;
	    default:
		// otherwise do nothing
	    }
	}
	
	if (debugFlag) {
	    System.out.println("[StateMachineRuleSet.actSprout] nextState="
			       + nextState);
	}
	RuleResult result = rules[nextState.ordinal()].act(c, s, e);
	
	// If a tip cell migrates a positive distance, force its
	// successor's next state to elongation.  If a new cell is
	// created (via proliferation or branching), set its state.
	StateMachineRuleStorage smrs;
	switch (nextState) {
	case MIGRATING_ELONGATING:
	    if (cellPos == CellInterface.CellPosition.TIP
		&& result != null && result.stalkElongationDistance > 0) {
		smrs =
		    (StateMachineRuleStorage) c.getSuccessor().getLocalStorage();
		smrs.forcedElongation = true;
	    }
	    break;
	case PROLIFERATING:
	case BRANCHING:
	    if (result != null) {
		LinkedList<CellInterface> newCellList = result.newCellList;
		for (Iterator<CellInterface> j = newCellList.iterator();
		     j.hasNext();) {
		    CellInterface newCell = j.next();
		    smrs = (StateMachineRuleStorage) newCell.getLocalStorage();
		    smrs.state = nextState;
		}
	    }
	    break;
	default:
	}
	//	log.println("[StateMachineRuleSet.actSprout] End cell "
	//		    + c.getIdNumber() + " activity",
	//		    LogStreamInterface.BASIC_LOG_DETAIL);
    }

    public boolean tipCellsHavePrecedence() {
	return true;
    }

    public int getStateInt(CellInterface c) {
	int stateInt;
	StateMachineRuleStorage localStorage =
	    (StateMachineRuleStorage) c.getLocalStorage();
	State st = localStorage.state;
	if (st == State.MIGRATING_ELONGATING && !c.isTipCell()) {
	    // Differantiate between migration and elongation states
	    stateInt = NUMBER_OF_STATES;
	}
	else {
	    stateInt = st.ordinal();
	}
	return stateInt;
    }
	    

    public static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }


}
