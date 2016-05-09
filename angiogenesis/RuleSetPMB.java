package angiogenesis;

//import sim.util.Double3D;

//import interfaces.*;
import shared.*;

import java.util.*;

public class RuleSetPMB extends SimpleRuleSet {

    /*
    EnvironmentInterface e;

    public static final String VERSION_STRING = "0.2";

    public static OutputBufferInterface buffer;

    public static final String RULE_ORDER =
	"Proliferation, Migration, Branching";

    public String getRuleSetIdentifier() {
	return "MPB";
    }
    */

    public RuleSetPMB() {
	versionString = "0.2";
    }

    public void initialize(EnvironmentInterface e) {

	Parameters p = new Parameters(e.getAngiogenesisParametersFileName());
	// PROLIFERATION_ACTIVE_FRACTION parameter used by state
	// machine models
	p.removeParameter(Parameters.LabelType.PROLIFERATION_ACTIVE_FRACTION);
	p.removeParameter(Parameters.LabelType.FORCED_BRANCH_PROLIFERATION_PROBABILITY);
	p.readInputParameters();
	

	activationRule = new ActivationRule(p, e);
	migrationRule = new MigrationRule(p, e);
	proliferationRule = new ProliferationRule(p, e);
	branchingRule = new BranchingRule(p, e);

	ruleSequence = new Rule[] {proliferationRule, migrationRule, branchingRule};

	super.initialize(e);

    }


}
