/*
 Instantiate rules as objects and allocate an array of rule objects in order of rule execution

Each rule object returns its name string and abbreviation string.

*/


package angiogenesis;

//import sim.util.Double3D;

//import interfaces.*;
import shared.*;

import java.util.*;

public class SimpleRuleSet implements RuleSetInterface {

    public static boolean debugFlag = false;
    public void setDebugFlag() {
	debugFlag = true;
    }

    

    EnvironmentInterface env;

    //    public static final String VERSION_STRING = "0.3";

    public static OutputBufferInterface buffer;

    public static String ruleOrder = "Undefined";
    //	"Migration, Proliferation, Branching";

    // short version for including on output file names
    public static String ruleSetIdentifier = "Undefined";
    // long version
    public static String ruleSetName = "Undefined";

    public String getRuleSetIdentifier() {
	return ruleSetIdentifier;
    }

    public String getRuleSetName() {
	return ruleSetName;
    }

    
    protected Rule[] ruleSequence;
    protected Rule activationRule;
    protected Rule migrationRule;
    protected Rule proliferationRule;
    protected Rule branchingRule;

    protected String versionString;

    public LogStreamInterface log;

    public void initialize(EnvironmentInterface env) {
	this.env = env;
	Rule.setLog(env.getLog());
	buffer = env.getOutputBuffer();
	log = env.getLog();

	ruleOrder = "";
	ruleSetIdentifier = "";
	boolean first = true;
	for (Rule r : ruleSequence) {
	    if (first) {
		first = false;
	    }
	    else {
		ruleOrder += ", ";
	    }
	    ruleOrder += r.getRuleName();
	    ruleSetIdentifier += r.getRuleIdentifier();
	}
	ruleSetName = ruleSetIdentifier;
	buffer.println("RuleSet: " + ruleOrder + " version " + versionString);

	/*
	activationRule.initialize(env);
	branchingRule.initialize(env);
       	proliferationRule.initialize(env);
	migrationRule.initialize(env);
	*/

	//	System.out.println("[angiogenesis.SimpleRuleSet.initialize]");
    }

    public Object createLocalStorage(CellInterface c) {
	/*
	System.out.println("[angiogenesis.SimpleRuleSet.createLocalStorage]");
	Storage s = new Storage();
	return s;
	*/
	return new Storage();
    }

    public void actCell(CellInterface c,
			Object localStorage,
			EnvironmentInterface e) {
	Storage s = (Storage) localStorage;

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
	    die("[SimpleRuleSet.actCell] Unknown cell state: " + state);
	}
    }

    private void actSprout(CellInterface c,
			   Storage s,
			   EnvironmentInterface e) {
	// Cell does not resize itself after division check before
	// rules are applied in order to catch newly created cell
	// after division.
	if (s.divided) {
	    c.resize();
	    s.divided = false;
	}
	for (Rule r : ruleSequence) {
	    r.act(c, s, e);
	}
	//	System.out.println("[SimpleRuleSet.actSprout] " + c);
	//	MigrationRule.act(c, s, e);
	//	System.out.println("[SimpleRuleSet.actSprout] starting ProliferationRule");
	//       	ProliferationRule.act(c, s, e);
	//       	BranchingRule.act(c, s, e);
	//	System.out.println("[SimpleRuleSet.actSprout] finished ProliferationRule");

	//	ApoptosisRule.act(c, s, e);
	//	LumenRule.act(c, as, e);


	//	e.getLog().println("[angiogenesis.SimpleRuleSet.actCell]" + c);
	//	c.translate(10, 10, 10, e);
	//	c.remove(e);

	/*
	if (c.getIdNumber() < 3) {
	    Double3D pos = c.getFrontNodeLocation();
	    double newX = pos.x + 1;
	    double newY = pos.y + 1;
	    double newZ = pos.z + 1;
	    double radius = 1;
	    double[] params = new double[] {newX, newY, newZ, radius};
	    //	    NodeData nd = new NodeData(NodeData.ShapeType.SPHERE, params);
	    System.out.println("[angiogenesis.SimpleRuleSet.actCell] reproducing cell "
			       + c);
	    c.divide(e);
	}
	*/

	/*
	LinkedList<NodeData> nodeDataList = new LinkedList<NodeData>();
	NodeData nd = new NodeData(NodeData.ShapeType.CAPSULE,
				   new double[] {0, 0, 0, 1, 1, 1, 1});
	nodeDataList.add(nd);
	c.create(nodeDataList, e);
	*/
	/*
	Storage s = (Storage) localStorage;
	int old = s.p1;
	s.p1 = e.getP1();
	System.out.println("[angiogenesis.SimpleRuleSet.act] old=" + old 
			   + "  new=" + s.p1);
	c.act("arg is " + s.p1);
	*/
    }


    //    public void actNode(NodeInterface n,
    //			Object localStorage,
    //			EnvironmentInterface e) {
    //	//	System.out.println("[angiogenesis.SimpleRuleSet.actNode]" + n);
    //    }



    public boolean tipCellsHavePrecedence() {
	return false;
    }


    public int getStateInt(CellInterface c) {
	return 0;
    }

    public static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }






}
