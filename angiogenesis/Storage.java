
/*
 * 6-8-2011 
 * Record the fact that a cell is on a branch
 *
 */

package angiogenesis;

public class Storage {
    
    public boolean stateMachine = false;
    public boolean divided = false;
    public boolean isBranch = false;
    public long lastSpecialBranchStep;


    public String toInternalString() {
	String retStr;
	retStr =
	    "stateMachine=" + stateMachine
	    + ",divided=" + divided
	    + ",isBranch=" + isBranch
	    + ",lastSpecialBranchStep=" + lastSpecialBranchStep;
	return retStr;
    }

    public String toString() {
	return "Storage[" + toInternalString() + "]";
    }

    public void copyTo(Storage s) {
	s.stateMachine = stateMachine;
	s.divided = divided;
	s.isBranch = isBranch;
	s.lastSpecialBranchStep = lastSpecialBranchStep;
    }

    public static void testCopyTo() {
	Storage s1 = new Storage();
	s1.stateMachine = true;
	s1.divided = true;
	s1.isBranch = true;
	s1.lastSpecialBranchStep = 100;

	Storage s2 = new Storage();
	s1.copyTo(s2);
	if (s1.stateMachine != s2.stateMachine
	    || s1.divided != s2.divided
	    || s1.isBranch != s2.isBranch
	    || s1.lastSpecialBranchStep != s2.lastSpecialBranchStep) {
	    SimpleRuleSet.die("[Storage.testCopyTo] objects are note the same: "
			      + s1 + "  " + s2);
	}
	System.out.println("[Storage.testCopyTo] passed");
    }

    public static void main(String[] args) {
	testCopyTo();
    }
}
