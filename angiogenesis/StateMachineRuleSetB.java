
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

public class StateMachineRuleSetB extends StateMachineRuleSet implements RuleSetInterface {

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
	migrationRule = new MigrationRuleB(p, env);
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
	    die("[StatemachineRuleSetB.initialize] state diagram model is null");
	}
	setTransitionTables(sdm);

	//	System.out.println("[StateMachineRuleSet.initialize] testing setTransitionTables");
	//	testSetTransitionTables(sdm);
	//	System.exit(0);

	ruleSetName = "SMb " + modelSource;


	buffer.println("Cell state code:");
	buffer.println("  " + State.IDLE.ordinal() + ": IDLE");
	buffer.println("  " + State.PROLIFERATING.ordinal()
		       + ": PROLIFERATING");
	buffer.println("  " + State.BRANCHING.ordinal() + ": BRANCHING");
	buffer.println("  " + State.MIGRATING_ELONGATING.ordinal()
		       + ": MIGRATING");
	buffer.println("  " + NUMBER_OF_STATES + ": ELONGATING");
    }

}
