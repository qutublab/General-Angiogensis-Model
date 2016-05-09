package angiogenesis;

//import sim.util.Double3D;

//import interfaces.*;
import shared.*;

import java.util.*;

public class RuleSetM2PM2B extends SimpleRuleSet {

    /*
    EnvironmentInterface e;

    public static final String VERSION_STRING = "0.2";

    public static OutputBufferInterface buffer;

    public static final String RULE_ORDER =
	"Migration2, Proliferation, Migration2, Branching";

    public String getRuleSetIdentifier() {
	return "M2PM2B";
    }
    */

    public RuleSetM2PM2B() {
	versionString = "0.2";

    }


    public void initialize(EnvironmentInterface e) {

	Parameters2 p = new Parameters2(e.getAngiogenesisParametersFileName());
	// PROLIFERATION_ACTIVE_FRACTION parameter used by state
	// machine models
	p.removeParameter(Parameters.LabelType.PROLIFERATION_ACTIVE_FRACTION);
	p.removeParameter(Parameters.LabelType.FORCED_BRANCH_PROLIFERATION_PROBABILITY);
	p.readInputParameters();
	

	activationRule = new ActivationRule(p, e);
	migrationRule = new MigrationRule2(p, e, .5);
	proliferationRule = new ProliferationRule(p, e);
	branchingRule = new BranchingRule(p, e);

	ruleSequence = new Rule[] {migrationRule, proliferationRule, migrationRule, branchingRule};

	super.initialize(e);

    }


}
