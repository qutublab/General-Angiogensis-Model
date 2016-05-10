/*

p(A, B) = probability of transitition from state A to state B

p(t, A) = probability of being in state A at time t

For tip and stalk cells:

p(0, IDLE) = 1
p(0, B) = 0  when B is not IDLE

For tip cells:

p(t+1, B) = Summation_(state A) p(t, A)p(A, B)   

For stalk cells, use the formula above when the tip cell state at time
t+1 is not MIGRATION and when the tip cell state is MIGRATION have the
formula reflect the fact that the stalk cell state is forced to
ELONGATION:

When B is not stalk ELONGATION:
p(t+1, B) = (1-p(t+1,MIGRATION)) Summation_(state A) p(t, A)p(A, B)   

For ELONGATION:
p(t+1, ELONGATION) = p(t+1,MIGRATION)


The expected number of times that the state B is reached in the first
t steps of a simulation is:

Summation_(i <= t) p(i, B)





*/


package tools;

import shared.*;

public class Examine {

    public static final int NUMBER_OF_STEPS = 24;

    public static enum State {IDLE, MIGRATION_ELONGATION, PROLIFERATION, BRANCHING};
    public static final int NUMBER_OF_STATES = State.values().length;

    //    public static enum PairedState {II, IE, IP, IB, MI, ME, MP, MB, PI, PE, PP, PB, BI, BE, BP, BB};
    //    public static final NUMBER_OF_PAIRED_STATES = PairedState.values().length;

    public static void main(String[] args) {
	for (String fileName : args) {
	    StateDiagramModelResult sdmr = StateDiagramModelResult.read(fileName);
	    System.out.println(fileName + "  score=" + sdmr.score);
	    prettyPrintModel(sdmr.model);
	    computeStateBreakdown(sdmr.model, NUMBER_OF_STEPS);
	}
    }


    private static double[][] createTipCellStateTable(StateDiagramModel model) {
	double[][] tipStateMachine = new double[NUMBER_OF_STATES][NUMBER_OF_STATES];
	tipStateMachine[State.IDLE.ordinal()][State.IDLE.ordinal()] = model.tipQuiescentToQuiescent;
	tipStateMachine[State.IDLE.ordinal()][State.MIGRATION_ELONGATION.ordinal()] = model.tipQuiescentToMigration;
	tipStateMachine[State.IDLE.ordinal()][State.PROLIFERATION.ordinal()] = model.tipQuiescentToProliferation;
	tipStateMachine[State.IDLE.ordinal()][State.BRANCHING.ordinal()] = model.tipQuiescentToBranching;

	tipStateMachine[State.MIGRATION_ELONGATION.ordinal()][State.IDLE.ordinal()] = model.tipMigrationToQuiescent;
	tipStateMachine[State.MIGRATION_ELONGATION.ordinal()][State.MIGRATION_ELONGATION.ordinal()] =
	    model.tipMigrationToMigration;
	tipStateMachine[State.MIGRATION_ELONGATION.ordinal()][State.PROLIFERATION.ordinal()] =
	    model.tipMigrationToProliferation;
	tipStateMachine[State.MIGRATION_ELONGATION.ordinal()][State.BRANCHING.ordinal()] =
	    model.tipMigrationToBranching;

	tipStateMachine[State.PROLIFERATION.ordinal()][State.IDLE.ordinal()] = model.tipProliferationToQuiescent;
	tipStateMachine[State.PROLIFERATION.ordinal()][State.MIGRATION_ELONGATION.ordinal()] =
	    model.tipProliferationToMigration;
	tipStateMachine[State.PROLIFERATION.ordinal()][State.PROLIFERATION.ordinal()] =
	    model.tipProliferationToProliferation;
	tipStateMachine[State.PROLIFERATION.ordinal()][State.BRANCHING.ordinal()] = model.tipProliferationToBranching;

	tipStateMachine[State.BRANCHING.ordinal()][State.IDLE.ordinal()] = model.tipBranchingToQuiescent;
	tipStateMachine[State.BRANCHING.ordinal()][State.MIGRATION_ELONGATION.ordinal()] =
	    model.tipBranchingToMigration;
	tipStateMachine[State.BRANCHING.ordinal()][State.PROLIFERATION.ordinal()] = model.tipBranchingToProliferation;
	tipStateMachine[State.BRANCHING.ordinal()][State.BRANCHING.ordinal()] = model.tipBranchingToBranching;

	return tipStateMachine;

	//	return new double[][]{{.1, .2, .3, .4},
	//			      {.25, .25, .25, .25},
	//			      {.4, .3, .2, .1},
	//			      {.2, .3, .4, .1}};
    }

    private static double[][] createStalkCellStateTable(StateDiagramModel model) {
	double[][] stalkStateMachine = new double[NUMBER_OF_STATES][NUMBER_OF_STATES];

	stalkStateMachine[State.IDLE.ordinal()][State.IDLE.ordinal()] = model.stalkQuiescentToQuiescent;
	stalkStateMachine[State.IDLE.ordinal()][State.MIGRATION_ELONGATION.ordinal()] = 0;
	    //	    model.stalkQuiescentToElongation;
	stalkStateMachine[State.IDLE.ordinal()][State.PROLIFERATION.ordinal()] = model.stalkQuiescentToProliferation;
	stalkStateMachine[State.IDLE.ordinal()][State.BRANCHING.ordinal()] = model.stalkQuiescentToBranching;

	stalkStateMachine[State.MIGRATION_ELONGATION.ordinal()][State.IDLE.ordinal()] =
	    model.stalkElongationToQuiescent;
	stalkStateMachine[State.MIGRATION_ELONGATION.ordinal()][State.MIGRATION_ELONGATION.ordinal()] = 0;
	    //	    model.stalkElongationToElongation;
	stalkStateMachine[State.MIGRATION_ELONGATION.ordinal()][State.PROLIFERATION.ordinal()] =
	    model.stalkElongationToProliferation;
	stalkStateMachine[State.MIGRATION_ELONGATION.ordinal()][State.BRANCHING.ordinal()] =
	    model.stalkElongationToBranching;

	stalkStateMachine[State.PROLIFERATION.ordinal()][State.IDLE.ordinal()] = model.stalkProliferationToQuiescent;
	stalkStateMachine[State.PROLIFERATION.ordinal()][State.MIGRATION_ELONGATION.ordinal()] = 0;
	    //	    model.stalkProliferationToElongation;
	stalkStateMachine[State.PROLIFERATION.ordinal()][State.PROLIFERATION.ordinal()] =
	    model.stalkProliferationToProliferation;
	stalkStateMachine[State.PROLIFERATION.ordinal()][State.BRANCHING.ordinal()] =
	    model.stalkProliferationToBranching;

	stalkStateMachine[State.BRANCHING.ordinal()][State.IDLE.ordinal()] = model.stalkBranchingToQuiescent;
	stalkStateMachine[State.BRANCHING.ordinal()][State.MIGRATION_ELONGATION.ordinal()] = 0;
	    //	    model.stalkBranchingToElongation;
	stalkStateMachine[State.BRANCHING.ordinal()][State.PROLIFERATION.ordinal()] =
	    model.stalkBranchingToProliferation;
	stalkStateMachine[State.BRANCHING.ordinal()][State.BRANCHING.ordinal()] = model.stalkBranchingToBranching;

	return stalkStateMachine;

	// elongation state ordinal: 1
	//	return new double[][]{{.2, 0, .3, .5},
	//			      {.1, 0, .2, .7},
	//			      {.3, 0, .5, .2},
	//			      {.5, 0, .2, .3}};

    }

    private static void checkTable(double[][] table) {
	double tolerance = .00000000000001;
	for (State fromState : State.values()) {
	    double sum = 0;
	    for (State toState : State.values()) {
		sum += table[fromState.ordinal()][toState.ordinal()];
	    }
	    if (Math.abs(sum - 1) > tolerance) {
		String probStr = "";
		for (double p : table[fromState.ordinal()]) {
		    probStr += p + "  ";
		}
		die("Transitions from state " + fromState + " sum to " + sum + "  (" + probStr
		    + ")");
	    }
	}
    }


    public static void computeStateBreakdown(StateDiagramModel model,
					      int numberOfSteps) {
	double[][] tipCellTransitionTable = createTipCellStateTable(model);
	double[][] stalkCellTransitionTable = createStalkCellStateTable(model);
	double[][] rearCellTransitionTable = stalkCellTransitionTable;

	//	System.out.println("Checking tipCellTransitionTable...");
	checkTable(tipCellTransitionTable);
	//	System.out.println("Checking stalkCellTransitionTable...");
	checkTable(stalkCellTransitionTable);

	//	System.out.println("Tip Cell Transition Table");
	//	prettyPrintTable(tipCellTransitionTable);
	//	System.out.println();
	//	System.out.println("Stalk/Rear Cell Transition Table");
	//	prettyPrintTable(stalkCellTransitionTable);
	//	System.out.println();

	double[][] tipProb = new double[numberOfSteps][NUMBER_OF_STATES];
	double[][] stalkProb = new double[numberOfSteps][NUMBER_OF_STATES];
	double[][] rearProb = new double[numberOfSteps][NUMBER_OF_STATES];

	tipProb[0][State.IDLE.ordinal()] = 1;
	stalkProb[0][State.IDLE.ordinal()] = 1;
	rearProb[0][State.IDLE.ordinal()] = 1;

	for (int t = 1; t < numberOfSteps; t++) {
	    // tip cell
	    for (State toState : State.values()) {
		double prob = 0;
		for (State fromState : State.values()) {
		    prob +=
			tipProb[t - 1][fromState.ordinal()]
			* tipCellTransitionTable[fromState.ordinal()][toState.ordinal()];
		}
		tipProb[t][toState.ordinal()] = prob;
	    }
	    
	    // stalk cell
	    for (State toState : State.values()) {
		double prob = 0;
		if (toState == State.MIGRATION_ELONGATION) {
		    prob = tipProb[t][State.MIGRATION_ELONGATION.ordinal()];
		}
		else {
		    for (State fromState : State.values()) {
			prob +=
			    stalkProb[t - 1][fromState.ordinal()]
			    * stalkCellTransitionTable[fromState.ordinal()][toState.ordinal()];
		    }
		    //		    System.out.println("t=" + t + "  stalk cell preprob " + prob);
		    prob = prob * (1 - tipProb[t][State.MIGRATION_ELONGATION.ordinal()]);
		}
		stalkProb[t][toState.ordinal()] = prob;
	    }

	    // rear cell uses stalkCellTransitionTable
	    for (State toState : State.values()) {
		double prob = 0;
		for (State fromState : State.values()) {
		    prob +=
			rearProb[t - 1][fromState.ordinal()]
			* stalkCellTransitionTable[fromState.ordinal()][toState.ordinal()];
		}
		rearProb[t][toState.ordinal()] = prob;
	    }
	}


	//	printProbabilities(tipProb);
	//	System.out.println();
	//
	//	printProbabilities(stalkProb);
	//	System.out.println();


	// Sanity check: currentRearCellStateProbabilities should
	// total 1 and expectedRearCellVisits should total step+1
	double tolerance = 0.0000000001;
	for (int t = 0; t < numberOfSteps; t++) {
	    double tipSum = 0;
	    double stalkSum = 0;
	    double rearSum = 0;
	    for (State s : State.values()) {
		tipSum += tipProb[t][s.ordinal()];
		stalkSum += stalkProb[t][s.ordinal()];
		rearSum += rearProb[t][s.ordinal()];
	    }
	    //	    System.out.println("Step: " + t + "  tipSum=" + tipSum + "  stalkSum=" + stalkSum
	    //			       + "  rearSum=" + rearSum);
	    if (Math.abs(tipSum - 1) > tolerance) {
		die("tipSum " + tipSum + " is not 1 at step " + t);
	    }
	    /*
	    if (Math.abs(stalkSum - 1) > tolerance) {
		die("stalkSum " + stalkSum + " is not 1 at step " + t);
	    }
	    if (Math.abs(rearSum - 1) > tolerance) {
		die("rearSum " + rearSum + " is not 1 at step " + t);
	    }
	    */
	}

	double[] expectedTipCellValues = new double[NUMBER_OF_STATES];
	double[] expectedStalkCellValues = new double[NUMBER_OF_STATES];
	double[] expectedRearCellValues = new double[NUMBER_OF_STATES];
	for (int t = 0; t < numberOfSteps; t++) {
	    for (State s : State.values()) {
		expectedTipCellValues[s.ordinal()] += tipProb[t][s.ordinal()];
		expectedStalkCellValues[s.ordinal()] += stalkProb[t][s.ordinal()];
		expectedRearCellValues[s.ordinal()] += rearProb[t][s.ordinal()];
	    }
	}
	System.out.println("Expected tip cell actions after " + numberOfSteps + " steps");
	for (State s : State.values()) {
	    double expected = expectedTipCellValues[s.ordinal()];
	    double pct = round((expected / numberOfSteps) * 100, 2);
	    System.out.println(s + " = " + expected + "  (" + pct + "%)");
	}
	System.out.println();

	System.out.println("Expected stalk cell actions after " + numberOfSteps + " steps");
	for (State s : State.values()) {
	    double expected = expectedStalkCellValues[s.ordinal()];
	    double pct = round((expected / numberOfSteps) * 100, 2);
	    System.out.println(s + " = " + expected + "  (" + pct + "%)");
	}
	System.out.println();

	System.out.println("Expected rear cell actions after " + numberOfSteps + " steps");
	for (State s : State.values()) {
	    double expected = expectedRearCellValues[s.ordinal()];
	    double pct = round((expected / numberOfSteps) * 100, 2);
	    System.out.println(s + " = " + expected + "  (" + pct + "%)");
	}
	System.out.println();

    }
    


    private static double round(double n, int places) {
	double shift = Math.pow(10, places);
	double r = Math.round(n * shift) / shift;
	return r;
    }

    private static void printProbabilities(double[][] prob) {
	for (int t = 0; t < NUMBER_OF_STEPS; t++) {
	    System.out.print("Step: " + t + "  ");
	    for (State s : State.values()) {
		System.out.print(s + "=" + prob[t][s.ordinal()] + "  ");
	    }
	    System.out.println();
	}
    }
		

    
    
    private static void prettyPrintTable(double[][] table) {
	for (State s : State.values()) {
	    System.out.print(s + "  ");
	}
	System.out.println();
	for (State fromState : State.values()) {
	    System.out.print(fromState);
	    for (State toState : State.values()) {
		System.out.print("   " + table[fromState.ordinal()][toState.ordinal()]);
	    }
	    System.out.println();
	}
    }


    
    private static void prettyPrintModel(StateDiagramModel sdm) {
	System.out.println("Tip Cell");
	System.out.println("Current State        To Idle  To Migration  To Proliferation  To Branching");
	System.out.println("Idle    " + sdm.tipQuiescentToQuiescent + "  "
			   + sdm.tipQuiescentToMigration + " " + sdm.tipQuiescentToProliferation
			   + " " + sdm.tipQuiescentToBranching);
	System.out.println("Migration    " + sdm.tipMigrationToQuiescent + "  "
			   + sdm.tipMigrationToMigration + " " + sdm.tipMigrationToProliferation +
			   " " + sdm.tipMigrationToBranching);
	System.out.println("Proliferation    " + sdm.tipProliferationToQuiescent + "  "
			   + sdm.tipProliferationToMigration + " "
			   + sdm.tipProliferationToProliferation + " " + sdm.tipProliferationToBranching);
	System.out.println("Branching    " + sdm.tipBranchingToQuiescent + "  "
			   + sdm.tipBranchingToMigration + " " + sdm.tipBranchingToProliferation
			   + " " + sdm.tipBranchingToBranching);
	System.out.println();

	System.out.println("Stalk/Rear Cell");
	System.out.println("Current State        To Idle  To Proliferation  To Branching");
	System.out.println("Idle    " + sdm.stalkQuiescentToQuiescent + "  "
			   + sdm.stalkQuiescentToProliferation
			   + " " + sdm.stalkQuiescentToBranching);
	System.out.println("Elongation    " + sdm.stalkElongationToQuiescent + "  "
			   + sdm.stalkElongationToProliferation
			   + " " + sdm.stalkElongationToBranching);
	System.out.println("Proliferation    " + sdm.stalkProliferationToQuiescent + "  "
			   + sdm.stalkProliferationToProliferation + "  "
			   + sdm.stalkProliferationToBranching);
	System.out.println("Branching    " + sdm.stalkBranchingToQuiescent + "  "
			   + sdm.stalkBranchingToProliferation
			   + " " + sdm.stalkBranchingToBranching);
	System.out.println();
    }


    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }

}