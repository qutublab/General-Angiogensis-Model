
package angiogenesis;

import shared.*;

public class IdleRule extends Rule {


    public IdleRule(Parameters p, EnvironmentInterface e) {
	ruleName = "Idle";
	ruleIdentifier = "I";
	versionString = "0.1";
    }

    public RuleResult act(CellInterface c, Storage s, EnvironmentInterface e) {
	log.println("[IdleRule.act] Cell "
		    + c.getIdNumber() + " is idle",
		    LogStreamInterface.BASIC_LOG_DETAIL);	

	//	System.out.println("[IdleRule.act] " + c.getIdNumber());
	return null;
    }

}