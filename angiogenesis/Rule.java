
package angiogenesis;

import shared.*;

public class Rule {

    protected static LogStreamInterface log;

    protected String ruleName = "Undefined";
    protected String ruleIdentifier = "Undefined";

    protected String versionString = "Undefined";

    /*
    public void initialize(EnvironmentInterface e) {
	SimpleRuleSet.die("[Rule.initialize]")
	e.getOutputBuffer().println(getRuleName() + "  " + versionString);
    }
    */

    public static void setLog(LogStreamInterface log) {
	Rule.log = log;
    }

    public RuleResult act(CellInterface c, Storage s, EnvironmentInterface e) {
	return null;
    }

    public String getRuleName() {
	return ruleName;
    }

    public String getRuleIdentifier() {
	return ruleIdentifier;
    }

}