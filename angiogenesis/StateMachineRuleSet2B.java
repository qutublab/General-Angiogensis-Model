/*
 * 4/2/2011
 * MigrationRule2 and ProliferationRule2 used.
 *
 * 5/13/2011 
 * Old ProliferationRule abandoned.  New ProliferationRule is now old
 * ProliferationRule2
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

public class StateMachineRuleSet2B extends StateMachineRuleSet {

    //    private static final double TABLE_TOLERANCE = .0000001;


    //    public enum State {IDLE, PROLIFERATING, BRANCHING, MIGRATING_ELONGATING};

    //    public final int NUMBER_OF_STATES = StateMchineRuleSet.State.values().length;

    //    private double[][] tipCellTransitionTable;
    //    private double[][] stalkCellTransitionTable;
    // non-tip and non-stalk cells
    //    private double[][] rearCellTransitionTable;
    

    //    private Rule[] rules;


    //    EnvironmentInterface env;

    //    public static final String VERSION_STRING = "0.3";

    //    public static OutputBufferInterface buffer;
    //    public static RandomInterface random;

    
    //    public String getRuleSetIdentifier() {
    //	return ruleSetIdentifier;
    //    }
    
    //    protected Rule activationRule;
    //    protected Rule idleRule;
    //    protected Rule migrationRule;
    //    protected Rule proliferationRule;
    //    protected Rule branchingRule;


    


    public void initialize(EnvironmentInterface env) {
	ruleSetIdentifier = "SM2b";
	versionString = "0.1";
	this.env = env;
	log = env.getLog();
	Rule.setLog(log);
	buffer = env.getOutputBuffer();
	random = env.getRandom();
	Parameters2 p  = new Parameters2(env.getAngiogenesisParametersFileName());
	p.readInputParameters();
	// Have the proliferation rule adjust its rate when it gets
	// its parameters.
	p.signalProliferationGrowthRateAdjustment();

	forcedBranchProliferationProbability = 
	    p.getForcedBranchProliferationProbability();

	activationRule = new ActivationRule(p, env);
	idleRule = new IdleRule(p, env);
	migrationRule = new MigrationRule2B(p, env);
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
	    die("[StatemachineRuleSet2B.initialize] state diagram model is null");
	}
	setTransitionTables(sdm);

	//	System.out.println("[StateMachineRuleSet.initialize] testing setTransitionTables");
	//	testSetTransitionTables(sdm);
	//	System.exit(0);
	ruleSetName = "SM2b " + modelSource;

    }


    //    private void testSetTransitionTables(StateDiagramModel sdm) {
    //	setTransitionTables(sdm);
    //	
    //	System.out.println("Tip cell transitions");
    //	System.out.print("  ");
    //	for (State s : State.values()) {
    //	    System.out.print(s + "    ");
    //	}
    //	System.out.println();
    //	for (State s : State.values()) {
    //	    System.out.print(s + "   ");
    //	    for (State d : State.values()) {
    //		System.out.print(tipCellTransitionTable[s.ordinal()][d.ordinal()] + "    ");
    //	    }
    //	    System.out.println();
    //	}
    //	System.out.println();
    //
    //	System.out.println("Stalk cell transitions");
    //	System.out.print("  ");
    //	for (State s : State.values()) {
    //	    System.out.print(s + "    ");
    //	}
    //	System.out.println();
    //	for (State s : State.values()) {
    //	    System.out.print(s + "   ");
    //	    for (State d : State.values()) {
    //		System.out.print(stalkCellTransitionTable[s.ordinal()][d.ordinal()] + "    ");
    //	    }
    //	    System.out.println();
    //	}
    //	System.out.println();
    //
    //
    //	//	System.out.println("Rear cell transitions");
    //	//	System.out.print("  ");
    //	//	for (State s : State.values()) {
    //	//	    System.out.print(s + "    ");
    //	//	}
    //	//	System.out.println();
    //	//	for (State s : State.values()) {
    //	//	    System.out.print(s + "   ");
    //	//	    for (State d : State.values()) {
    //	//		System.out.print(rearCellTransitionTable[s.ordinal()][d.ordinal()] + "    ");
    //	//	    }
    //	//	    System.out.println();
    //	//	}
    //	//	System.out.println();
    //	//
    //	//	System.out.println(sdm);
    //    }


    //    private void setTransitionTables(StateDiagramModel sdm) {
    //	tipCellTransitionTable = new double[NUMBER_OF_STATES][NUMBER_OF_STATES];
    //
    //	tipCellTransitionTable[State.IDLE.ordinal()][State.IDLE.ordinal()] =
    //	    sdm.tipQuiescentToQuiescent;
    //	tipCellTransitionTable[State.IDLE.ordinal()][State.PROLIFERATING.ordinal()] =
    //	    sdm.tipQuiescentToProliferation;
    //	tipCellTransitionTable[State.IDLE.ordinal()][State.BRANCHING.ordinal()] =
    //	    sdm.tipQuiescentToBranching;
    //	tipCellTransitionTable[State.IDLE.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
    //	    sdm.tipQuiescentToMigration;
    //
    //	tipCellTransitionTable[State.PROLIFERATING.ordinal()][State.IDLE.ordinal()] =
    //	    sdm.tipProliferationToQuiescent;
    //	tipCellTransitionTable[State.PROLIFERATING.ordinal()][State.PROLIFERATING.ordinal()] =
    //	    sdm.tipProliferationToProliferation;
    //	tipCellTransitionTable[State.PROLIFERATING.ordinal()][State.BRANCHING.ordinal()] =
    //	    sdm.tipProliferationToBranching;
    //	tipCellTransitionTable[State.PROLIFERATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
    //	    sdm.tipProliferationToMigration;
    //
    //	tipCellTransitionTable[State.BRANCHING.ordinal()][State.IDLE.ordinal()] =
    //	    sdm.tipBranchingToQuiescent;
    //	tipCellTransitionTable[State.BRANCHING.ordinal()][State.PROLIFERATING.ordinal()] =
    //	    sdm.tipBranchingToProliferation;
    //	tipCellTransitionTable[State.BRANCHING.ordinal()][State.BRANCHING.ordinal()] =
    //	    sdm.tipBranchingToBranching;
    //	tipCellTransitionTable[State.BRANCHING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
    //	    sdm.tipBranchingToMigration;
    //
    //	tipCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.IDLE.ordinal()] =
    //	    sdm.tipMigrationToQuiescent;
    //	tipCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.PROLIFERATING.ordinal()] =
    //	    sdm.tipMigrationToProliferation;
    //	tipCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.BRANCHING.ordinal()] =
    //	    sdm.tipMigrationToBranching;
    //	tipCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
    //	    sdm.tipMigrationToMigration;
    //
    //
    //	stalkCellTransitionTable = new double[NUMBER_OF_STATES][NUMBER_OF_STATES];
    //
    //	stalkCellTransitionTable[State.IDLE.ordinal()][State.IDLE.ordinal()] =
    //	    sdm.stalkQuiescentToQuiescent;
    //	stalkCellTransitionTable[State.IDLE.ordinal()][State.PROLIFERATING.ordinal()] =
    //	    sdm.stalkQuiescentToProliferation;
    //	stalkCellTransitionTable[State.IDLE.ordinal()][State.BRANCHING.ordinal()] =
    //	    sdm.stalkQuiescentToBranching;
    //	stalkCellTransitionTable[State.IDLE.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;
    //
    //	stalkCellTransitionTable[State.PROLIFERATING.ordinal()][State.IDLE.ordinal()] =
    //	    sdm.stalkProliferationToQuiescent;
    //	stalkCellTransitionTable[State.PROLIFERATING.ordinal()][State.PROLIFERATING.ordinal()] =
    //	    sdm.stalkProliferationToProliferation;
    //	stalkCellTransitionTable[State.PROLIFERATING.ordinal()][State.BRANCHING.ordinal()] =
    //	    sdm.stalkProliferationToBranching;
    //	stalkCellTransitionTable[State.PROLIFERATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;
    //
    //	stalkCellTransitionTable[State.BRANCHING.ordinal()][State.IDLE.ordinal()] =
    //	    sdm.stalkBranchingToQuiescent;
    //	stalkCellTransitionTable[State.BRANCHING.ordinal()][State.PROLIFERATING.ordinal()] =
    //	    sdm.stalkBranchingToProliferation;
    //	stalkCellTransitionTable[State.BRANCHING.ordinal()][State.BRANCHING.ordinal()] =
    //	    sdm.stalkBranchingToBranching;
    //	stalkCellTransitionTable[State.BRANCHING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
    //	    0;
    //
    //	stalkCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.IDLE.ordinal()] =
    //	    sdm.stalkElongationToQuiescent;
    //	stalkCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.PROLIFERATING.ordinal()] =
    //	    sdm.stalkElongationToProliferation;
    //	stalkCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.BRANCHING.ordinal()] =
    //	    sdm.stalkElongationToBranching;
    //	stalkCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;
    //
    //
    //	checkTransitionTable(tipCellTransitionTable, CellInterface.CellPosition.TIP, sdm);
    //	checkTransitionTable(stalkCellTransitionTable, CellInterface.CellPosition.STALK, sdm);
    //
    //	//	rearCellTransitionTable = new double[NUMBER_OF_STATES][NUMBER_OF_STATES];
    //	//
    //	//	rearCellTransitionTable[State.IDLE.ordinal()][State.IDLE.ordinal()] =
    //	//	    sdm.rearQuiescentToQuiescent;
    //	//	rearCellTransitionTable[State.IDLE.ordinal()][State.PROLIFERATING.ordinal()] =
    //	//	    sdm.rearQuiescentToProliferation;
    //	//	rearCellTransitionTable[State.IDLE.ordinal()][State.BRANCHING.ordinal()] =
    //	//	    sdm.rearQuiescentToBranching;
    //	//	rearCellTransitionTable[State.IDLE.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;
    //	//
    //	//	rearCellTransitionTable[State.PROLIFERATING.ordinal()][State.IDLE.ordinal()] =
    //	//	    sdm.rearProliferationToQuiescent;
    //	//	rearCellTransitionTable[State.PROLIFERATING.ordinal()][State.PROLIFERATING.ordinal()] =
    //	//	    sdm.rearProliferationToProliferation;
    //	//	rearCellTransitionTable[State.PROLIFERATING.ordinal()][State.BRANCHING.ordinal()] =
    //	//	    sdm.rearProliferationToBranching;
    //	//	rearCellTransitionTable[State.PROLIFERATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;
    //	//
    //	//	rearCellTransitionTable[State.BRANCHING.ordinal()][State.IDLE.ordinal()] =
    //	//	    sdm.rearBranchingToQuiescent;
    //	//	rearCellTransitionTable[State.BRANCHING.ordinal()][State.PROLIFERATING.ordinal()] =
    //	//	    sdm.rearBranchingToProliferation;
    //	//	rearCellTransitionTable[State.BRANCHING.ordinal()][State.BRANCHING.ordinal()] =
    //	//	    sdm.rearBranchingToBranching;
    //	//	rearCellTransitionTable[State.BRANCHING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] =
    //	//	    0;
    //	//
    //	//	rearCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.IDLE.ordinal()] = 0;
    //	//	rearCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.PROLIFERATING.ordinal()] =
    //	//	    0;
    //	//	rearCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.BRANCHING.ordinal()] =
    //	//	    0;
    //	//	rearCellTransitionTable[State.MIGRATING_ELONGATING.ordinal()][State.MIGRATING_ELONGATING.ordinal()] = 0;
    //    }


    //    private void checkTransitionTable(double[][] table, 
    //				      CellInterface.CellPosition cellPosition,
    //				      StateDiagramModel sdm) {
    //	if (table.length != NUMBER_OF_STATES) {
    //	    die("[StateMachineRuleSet2.checkTransitionTable] " + cellPosition + " cell table has "
    //		+ table.length + " rows; expected " + NUMBER_OF_STATES);
    //	}
    //	for (int r = 0; r < NUMBER_OF_STATES; r++) {
    //	    double[] row = table[r];
    //	    if (row.length != NUMBER_OF_STATES) {
    //		die("[StateMachineRuleSet2.checkTransitionTable] " + cellPosition + " cell "
    //		    + State.values()[r] + " row (" + r + ") has " + row.length
    //		    + " elements; expected " + NUMBER_OF_STATES + "\n" + sdm);
    //	    }
    //	    double sum = 0;
    //	    for (int c = 0; c < NUMBER_OF_STATES; c++) {
    //		double p = row[c]; 
    //		if (p < 0) {
    //		die("[StateMachineRuleSet2.checkTransitionTable] " + cellPosition + " cell "
    //		    + State.values()[r] + " to " + State.values()[c]
    //		    + " transition probability (" + r + "," + c + ") is negative: " + p
    //		    + "\n" + sdm);
    //		}
    //		sum += p;
    //	    }
    //	    if (Math.abs(sum - 1) > TABLE_TOLERANCE) {
    //		die("[StateMachineRuleSet2.checkTransitionTable] " + cellPosition
    //		    + " cell transition probabilities from state " + State.values()[r]
    //		    + " do not sum to 1: " + sum + "\n" + sdm);
    //	    }
    //	}
    //	// check that the stalk cell table has no transitions to its elongation state
    //	if (cellPosition == CellInterface.CellPosition.STALK ||
    //	    cellPosition == CellInterface.CellPosition.REAR) {
    //	    int elongIndex = State.MIGRATING_ELONGATING.ordinal();
    //	    for (int from = 0; from < NUMBER_OF_STATES; from++) {
    //		if (table[from][elongIndex] != 0) {
    //		    die("[StateMachineRuleSet2.checkTransitionTable] "
    //			+ " stalk cell has a nonzero transition probability from "
    //			+ State.values()[from] + " to elongation state: "
    //			+ table[from][elongIndex]);
    //		}
    //	    }
    //	}
    //    }



		

		   
		
    //    public Object createLocalStorage(CellInterface c) {
    //	return new StateMachineRuleStorage();
    //    }

    //    public void actCell(CellInterface c,
    //			Object localStorage,
    //			EnvironmentInterface e) {
    //	StateMachineRuleStorage s = (StateMachineRuleStorage) localStorage;
    //
    //	EnvironmentInterface.CellState state = c.getCellState();
    //	switch (state) {
    //	case IDLE:
    //	    activationRule.act(gc, s, e);
    //	    break;
    //	case QUIESCENT:
    //	    break;
    //	case ACTIVE:
    //	    actSprout(c, s, e);
    //	    break;
    //	default:
    //	    die("[StateMachineRuleSet.actCell] Unknown cell state: " + state);
    //	}
    //    }

    //    private void actSprout(CellInterface c,
    //			   StateMachineRuleStorage s,
    //			   EnvironmentInterface e) {
    //	CellInterface.CellPosition cellPos = c.getCellPosition();
    //	
    //	// When the forcedElongation flag is set for a stalk cell, its
    //	// tip cell has already migrated and elongated it.  There is
    //	// nothing more for the stalk cell to do.  
    //	//
    //	// Note that in any given time-step, tip cells are processed
    //	// before other types of cells.
    //	
    //	if (s.forcedElongation) {
    //	    if (cellPos == CellInterface.CellPosition.STALK) {
    //		s.state = State.MIGRATING_ELONGATING;
    //		s.forcedElongation = false;
    //		//		System.out.println("[StateMachineRuleSet.actSprout] elongation "
    //		//				   + c.getIdNumber());
    //		return;
    //	    }
    //	    else {
    //		die("[StateMachineRuleSet2.actSprout] Forced elongation of nonstalk cell: " + c);
    //	    }
    //	}
    //	
    //	double[][] transitionTable = null;
    //	switch (cellPos) {
    //	case TIP:
    //	    transitionTable = tipCellTransitionTable;
    //	    break;
    //	case STALK:
    //	    transitionTable = stalkCellTransitionTable;
    //	    break;
    //	case REAR:
    //	    // for now , rear cells use same tables as stalk cell; see comments at top of file
    //	    transitionTable = stalkCellTransitionTable;
    //	    break;
    //	default:
    //	    die("[StateMachineRuleSet2.actSprout] Unsupported cell position: " + cellPos);
    //	}
    //	
    //	State st = s.state;
    //	//	System.out.println("[StateMachineRuleSet.actSprout] " + st + " " + transitionTable);
    //	double[] transitionRow = transitionTable[st.ordinal()];
    //	
    //	//	if (c.getIdNumber() == 298) {
    //	//	    System.out.println("****** current state " + st + "  position " + cellPos);
    //	//	}
    //
    //	double r = random.nextDouble();
    //	int i = 0;
    //	State nextState = null;
    //	int lastStateIndex = NUMBER_OF_STATES - 1;
    //	while (i < NUMBER_OF_STATES && nextState == null) {
    //	    double prob = transitionRow[i];
    //	    //	    if (c.getIdNumber() == 298) {
    //	    //		System.out.println("****** " + State.values()[i] + "  r=" + r + "  prob=" + prob);
    //	    //	    }
    //
    //	    // Table probabilities may sum to 1 - TABLE_TOLERANCE, so
    //	    // if this is the last state, use it if r is within the
    //	    // tolerance
    //	    if (r < prob || (i == lastStateIndex && r < prob + TABLE_TOLERANCE)) {
    //		nextState = State.values()[i];
    //	    }
    //	    else {
    //		    r = r - prob;
    //	    }
    //	    i ++;
    //	}
    //
    //	if (nextState == null) {
    //	    double total = 0;
    //	    for (double p : transitionRow) {
    //		total += p;
    //		System.out.print(p + "  ");
    //	    }
    //	    System.out.println(total);
    //	    die("[StateMachineRuleSet2.actSprout] Unable to find the next state for a " 
    //		+ st + " " + cellPos
    //		+ " cell " + c.getIdNumber());
    //	}
    //	
    //	// check for illegal transitions
    //	if ((cellPos == CellInterface.CellPosition.STALK
    //	     || cellPos == CellInterface.CellPosition.REAR)
    //	    && nextState == State.MIGRATING_ELONGATING) {
    //	    die("[StateMachineRuleSet2.actSprout] Illegal " + cellPos
    //		+ " cell transition to MIGRATING_ELONGATING");
    //	}
    //	s.state = nextState;
    //	double result = rules[nextState.ordinal()].act(c, s, e);
    //
    //	// if a tip cell migrates a positive distance, change its successor state 
    //	if (cellPos == CellInterface.CellPosition.TIP && nextState == State.MIGRATING_ELONGATING
    //	    && result > 0) {
    //	    StateMachineRuleStorage smrs =
    //		(StateMachineRuleStorage) c.getSuccessor().getLocalStorage();
    //	    smrs.forcedElongation = true;
    //	    //	    System.out.println("****[StatemachineRuleSet] " + c.getIdNumber() + " "
    //	    //			       + c.getCellPosition() + " elongating "
    //	    //			       + c.getSuccessor().getIdNumber() + " "
    //	    //			       + c.getSuccessor().getCellPosition());
    //	}
    //	
    //    }

    //    public boolean tipCellsHavePrecedence() {
    //	return true;
    //    }

    //    public static void die(String s) {
    //	System.err.println(s);
    //	System.exit(1);
    //    }


}
