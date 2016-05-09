
package angiogenesis;

public class StateMachineRuleStorage extends Storage {

    public boolean forcedElongation;
    public StateMachineRuleSet.State state = StateMachineRuleSet.State.IDLE;

    public StateMachineRuleStorage() {
	stateMachine = true;
    }

    public String toString() {
	String retStr;
	retStr =
	    "StateMachineRuleStorage[" + super.toInternalString()
	    + ",forcedElongation=" + forcedElongation
	    + ",state=" + state
	    + "]";
	return retStr;
    }

    public void copyTo(StateMachineRuleStorage s) {
	super.copyTo(s);
	s.forcedElongation = forcedElongation;
	s.state = state;
    }

    public static void testCopyTo() {
	StateMachineRuleStorage s1 = new StateMachineRuleStorage();
	s1.stateMachine = true;
	s1.divided = true;
	s1.isBranch = true;
	s1.lastSpecialBranchStep = 100;
	s1.forcedElongation = true;
	s1.state = StateMachineRuleSet.State.BRANCHING;

	StateMachineRuleStorage s2 = new StateMachineRuleStorage();
	s1.copyTo(s2);
	if (s1.stateMachine != s2.stateMachine
	    || s1.divided != s2.divided
	    || s1.isBranch != s2.isBranch
	    || s1.lastSpecialBranchStep != s2.lastSpecialBranchStep
	    || s1.forcedElongation != s2.forcedElongation
	    || s1.state != s2.state) {
	    SimpleRuleSet.die("[StateMAchineRuleStorage.testCopyTo] objects are note the same: "
			      + s1 + "  " + s2);
	}
	System.out.println("[StateMachineRuleStorage.testCopyTo] passed");
    }

    public static void main(String[] args) {
	testCopyTo();
    }

}