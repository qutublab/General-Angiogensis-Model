
/*
    constructor summary

    Cell()
      used for testing

    Cell(spheroid.Sphere baseSphere)
      for initial sphere cells
      createCells ***

    Cell(Node front, Node rear, double initialRadius)
      max and min radius computed
      activate ***
        initialTipCellLength
        initialStalkCellLength
        initialSproutRadius
      branch ***

    Cell((Node front, Node rear) 
      calls  Cell(Node front, Node rear, double initialRadius) with a generated radius

    Cell(LinkedList<Node> nodeList, double initialRadius)
      max and miun radius computed
      divide ***

*/


/*
 * 2/18/2011 - getNodeLocations returns a list of COPIES of node
 * locations.
 *
 * 2/25/2011 - branch method no longer splits cell lengthwise, instead
 * it starts a small branch cell using part of the cell's volume.
 *
 * 3/10/2011 - cells reschedule themselves.
 *
 * 3/31/2011 - printSproutCordinates method rewritten to eliminate
 * recursive calls.
 *
 * 4/1/2011 - New cells created via divide method take on minimum and
 * maximum radii of cells from which they are created.
 *
 * 4/13/2011 - Cell inhibition done by breadth-first search instead of
 * depth-first.
 *
 * 6/15/2011 - 1) Detect and handle floating point computation
 * problems.  Example: a tip cell with front location (0, 0, 0) is to
 * migrate in the relative direction (1, 1, 2) for an extremely short
 * distance.  The resulting new front location is so close to the
 * original point that only a change in the z direction appears: (0,
 * 0, 1E-7).  This results in a 90 direction change regardless of the
 * initial relative direction.
 *
 * 2) Length to width ratio is a function of a cell's distance (as
 * measured by number of cells) from the tip cell.
 *
 * 8/22/2011 - limited XY branch lengths computation corrected
 *
 * 10/8/2011 - setColorInts includes value indicating cell state
 */


package scaffold;

import sim.engine.*;
import sim.util.Int3D;
import sim.util.Double3D;

import shared.*;
import sharedMason.*;

import spheroid.*;

import java.util.*;
import java.io.*;

public class Cell implements CellInterface, Steppable {

    private static final boolean INDICATE_BRANCHES = false;
    private static final boolean PRINT_ONLY_LIMITED_XY_BRANCHES = false;
    private static final boolean PRINT_SPROUT_COLOR = true;

    private static final double DECIMAL_TOLERANCE = .0000000001;

    private static final double ONE_THIRD = 1.0 / 3.0;

    private static boolean useSpecifiedOutputGeometryPrecision = false;
    private static double outputGeometryPrecisionFactor = 0;

    private static double initialSproutRadius; 
    //    private static double minimumCellRadius;
    //    private static double maximumCellRadius;
    //    private static double maximumCellLength = 60;  // from Qutub paper tipMax = 60
    //    private static double lengthToRadiusRatio;
    //    private static double tipLengthToRadiusRatio;
    //    private static double maximumCellVolume;

    private static double minimumLengthToRadiusRatio;
    private static double maximumLengthToRadiusRatio;
    private static double lengthToRadiusRatioTransitionDistanceCells;
    //    private static double minimumTipLengthToRadiusRatio;
    //    private static double maximumTipLengthToRadiusRatio;

    private static double maximumElongationFactor;


    private static double minimumDivisionLength;
    private static double divisionProbConst1;
    private static double divisionProbConst2;


    public static boolean debugFlag = false;
    public static boolean debugFlag2 = false;


    /*
     * Cell volume changes
     *
     * The maximum volume of any cell object is determined by the
     * maximum radius and maximum length.
     *
     *
     * Cells created by division take their radius and length from
     * their parent cell and the rules of division.
     *
     * Cells created by the initiation of a sprout have their length
     * as an argument (either explicitly or implicitly) expressed as
     * an argument to the Cell constructor.  Each cell's radius is
     * determined by the length-to-radius ratio except when the
     * minimum radius would not be met.
     *
     * When a cell's volume increases, its radius and length are
     * increased so that the length-to-radius ratio is reached or
     * maintained.  If a cell's volume surpasses the maximum volume,
     * then the radius is set to the maximum radius and the cell's
     * length is determined by this radius and the cell's volume.
     * This temporarily exceeds the maximum length, but the cell will
     * then divide and effectively reduce its length.
     *
     * When the volume of a cell decreases (for example, due to
     * branching), the cell's length does not change.  Thus a sprout
     * does not change in length.  Instead the volume decrease is
     * accomodated in its entirety by a radius decrease.  Signal an
     * error if the radius is decreased below the minimum.
     *
     * When a cell is elongated, its volume does not change but its
     * radius decreases.  Signal an error if the new radius is below
     * the minimum.
     */


    // initial stalk cell length of a sprout
    private static double initialStalkCellLength = 7.0;

    // initial tip cell length of a sprout
    private static double initialTipCellLength = 1.0;

    /*    
    private static double maximumRadius = 2;
    */

    private static int lastSteppedCell;


    private static RuleSetInterface ruleSet;
    private static int nextIdNumber = 0;

    private static int baseSphereCellsCreated = 0;
    private static int sproutCellsCreated = 0;


    private static ConcentrationsInterface concentrationsManager;

    private static int inhibitionRange;

    private static LogStream log;
    
    private static Environment env;

    private static boolean ignoreDiscretizedSprouts = false;
    
    private static boolean tipCellPrecedence;


    private static long divisionCount = 0;
    private static long migrationCount = 0;
    private static long attemptedMigrationCount = 0;
    private static long redirectionCount = 0;

    private static long branchCount = 0;
    private static long limitedXYBranchCount = 0;
    private static LinkedList<Cell> limitedXYBranchStarts =
	new LinkedList<Cell>();
    

    private static double attemptedMigrationDistance;
    private static double actualMigrationDistance;

    private static Point3D spheroidCenter;
    private static double spheroidRadius;


    private static int numberOfSproutColors;
    
    private int idNumber;
    private LinkedList<Node> nodeList;

    //    private LinkedList<Cell> neighborList;

    private Object localStorage;
    private Stoppable stopObject;

    //    private CellInterface.GrowthRole role;

    private Cell successor = null;
    private Cell predecessor = null;
    private Cell predecessorB = null;

    
    private boolean dead = false;

    private boolean branched = false;

    private double initialRadius;
    //    private double maximumRadius;
    //    private double minimumRadius;

    //    private double maximumVolume;



    //    private double length;
    private double radius = 0;
    //    private double volume;


    private boolean predecessorMark = false;


    private int cellColorInt = -1;  // -1 indicates color not yet assigned
    private int stateColorInt = -1;  // -1 indicates color not yet assigned

    public void setColorInts() {
	stateColorInt = ruleSet.getStateInt(this);
	//	if (branched) {
	//	    colorInt = 0;
	//	}
	//	else {
	//	    colorInt = 1;
	//	}
	//	if (true) {return;}
	if (cellColorInt != -1) {
	    return;
	}
	boolean[] neighborColorsSet = new boolean[numberOfSproutColors];
	if (successor != null && successor.cellColorInt != -1) {
	    neighborColorsSet[successor.cellColorInt] = true;
	}
	Cell sibling = null;
	if (successor.predecessor != null && successor.predecessorB != null) {
	    sibling =
		(successor.predecessor == this)? successor.predecessorB : successor.predecessor;
	    if (sibling.cellColorInt != -1) {
		neighborColorsSet[sibling.cellColorInt] = true;
	    }
	}
	if (predecessor != null && predecessor.cellColorInt != -1) {
	    neighborColorsSet[predecessor.cellColorInt] = true;
	}
	if (predecessorB != null && predecessorB.cellColorInt != -1) {
	    neighborColorsSet[predecessorB.cellColorInt] = true;
	}

	// use the first unused color
	for (int i = 0; i < numberOfSproutColors; i++) {
	    if (!neighborColorsSet[i]) {
		cellColorInt = i;
		break;
	    }
	}

	if (cellColorInt == -1) {
	    // Try to reuse the sibling color
	    if (sibling != null && sibling.cellColorInt != -1) {
		cellColorInt = sibling.cellColorInt;
	    }
	    else {
		// otherwise use the color that comes after the color
		// of its neighbor cells in the following order:
		// successor, predecessor, predecessorB
		int neighborColor = -1;
		if (successor != null && successor.cellColorInt != -1) {
		    neighborColor = successor.cellColorInt;
		}
		else {
		    if (predecessor != null && predecessor.cellColorInt != -1) {
			neighborColor = predecessor.cellColorInt;
		    }
		    else {
			if (predecessorB != null
			    && predecessorB.cellColorInt != -1) {
			    neighborColor = predecessorB.cellColorInt;
			}
			else {
			    Environment.die("[Cell.setColorInts] Unable to assign a color to cell "
					    + idNumber);
			}
		    }
		}
		cellColorInt = (neighborColor + 1) % numberOfSproutColors;
	    }
	}
    }

    private EnvironmentInterface.CellState state;

    // changed only by inhibitNeighbors and propagateInhibition methods
    //    private boolean ignoreInhibition = false;

    public EnvironmentInterface.CellState getCellState() {
	return state;
    }

    private static double computeRadius(double vol, double len) {
	return Math.sqrt(vol / (Math.PI * len));
    }

    private static void testComputeProportionedRadius() {
	double vol = 100;
	double lrRatio = 4;
	double r = computeProportionedRadius(vol, lrRatio);
	double l = r * lrRatio;
	double computedVol = computeVolume(r, l);
	double tolerance = 0.0000000001;
	if (Math.abs(vol - computedVol) > tolerance) {
	    Environment.die("[Cell.testComputeProportionedRadius] "
			    + "vol=" + vol + "  lrRatio=" + lrRatio
			    + " r=" + r + " computedVol=" + computedVol);
	}
	System.out.println("[Cell.testComputeProportionedRadius] test passed!");
    }
	

    private static double computeProportionedRadius(double vol,
						    double lengthToRadiusRatio) {
	/*
	 * vol = pi * r * r * l
	 * vol = pi * r * r * (r * lengthToRadiusRatio)
	 * r^3 = vol / (pi * lengthToRadiusRatio)
	 * r = (vol / (pi * lengthToRadiusRatio))^1/3
	 */
	double r = Math.pow(vol / (Math.PI * lengthToRadiusRatio), ONE_THIRD);
	return r;

    }


    private static double computeLength(double vol, double radius) {
	return vol / (Math.PI * radius * radius);
    }

    private static double computeVolume(double radius, double len) {
	return Math.PI * radius * radius * len;
    }

    private boolean inhibited = false;

    private LinkedList<Cell> spheroidCellNeighbors = new LinkedList<Cell>();
    private spheroid.Sphere baseSphere = null;


    public boolean canBranch() {
	boolean branchingOk;
	if (successor == null) {
	    branchingOk = false;
	}
	else {
	    if (successor.getPredecessorB() == null) {
		branchingOk = true;
	    }
	    else {
		branchingOk = false;
	    }
	}
	return branchingOk;
    }

    private boolean onBranch = false;
    private boolean onLimitedXYBranch = false;
    private boolean branchAhead = false;
    private boolean junctionAhead = false;

    private static SimpleGrid grid;
    /*
     * List of voxel indices
     */
    private LinkedList<Int3D> gridRepresentation;
    private boolean changedShape = false;
    
    // flag used for internal error detection
    public static boolean nonTipCellPreviouslyProcessed = false;


    // Do-nothing 0-ary constructor is here because of AlignedCell
    // subclass. And for testing.
    public Cell() {
	idNumber = nextIdNumber++;
	sproutCellsCreated ++;
    }


    public Cell(spheroid.Sphere baseSphere) {
	//	if (nextIdNumber == 286) {Environment.die("[Cell] 286 created");};

	idNumber = nextIdNumber++;
	baseSphereCellsCreated++;
	this.baseSphere = baseSphere;
	nodeList = new LinkedList<Node> ();
	//	double x = baseSphere.projection.x;
	//	double y = baseSphere.projection.y;
	//	double z = baseSphere.projection.z;
	//	double[] params = new double[] {x, y, z, 0};
	//	Node n = new Node(shared.NodeInterface.ShapeType.SPHERE, params);  //&&
	Node n = new Node(baseSphere.projection);
	nodeList.add(n);
	

	/*
	x = baseSphere.projection.x;
	y = baseSphere.projection.y;
	z = baseSphere.projection.z;
	params = new double[] {x, y, z, 0};
	n = new Node(shared.NodeInterface.ShapeType.SPHERE, params);
	nodeList.addFirst(n);
	*/

	gridRepresentation = createGridRepresentation();
	addToGrid(gridRepresentation);

	// all sphere cells start as idle
	state = EnvironmentInterface.CellState.IDLE;
    }
    
    public Cell(Node front, Node rear, double initialRadius) {
	//	if (nextIdNumber == 286) {Environment.die("[Cell] 286 created");};

	idNumber = nextIdNumber++;
	baseSphere = null;
	sproutCellsCreated++;
	nodeList = new LinkedList<Node>();
	nodeList.addFirst(front);
	nodeList.addLast(rear);
	this.initialRadius = initialRadius;
	radius = initialRadius;
	//	maximumRadius = computeMaximumRadius(initialRadius);	
	//	minimumRadius = computeMinimumRadius(initialRadius);	
	//	maximumVolume = computeMaximumVolume(maximumRadius);	
	//	length = front.getLocation().distance(rear.getLocation());
	//	volume = computeVolume(radius, computedLength());
	gridRepresentation = createGridRepresentation();
	addToGrid(gridRepresentation);

	// nonsphere cells start as active
	state = EnvironmentInterface.CellState.ACTIVE;
    }
    

    //    private static double computeInitialRadius(double length) {
    //	double r = Math.max(minimumCellRadius, length /  lengthToRadiusRatio);
    //	return r;
    //    }

    // Called by activate to create initial sprout cells
    //    public Cell(Node front, Node rear) {
    //	this(front, rear,
    //	     computeInitialRadius(front.getLocation().distance(rear.getLocation())));
    //    }


    // Assume nodes in listed are already marked as inflections
    // because a cell's boundary nodes may be recognized as
    // inflections only by examining its adjacent cell's nodes.
    public Cell(LinkedList<Node> nodeList, double initialRadius) {
	//	System.out.println("[Cell] starting cell creation");
	//	if (nextIdNumber == 286) {Environment.die("[Cell] 286 created");};


	idNumber = nextIdNumber++;
	sproutCellsCreated++;
	this.nodeList = nodeList;
	this.initialRadius = initialRadius;
	radius = initialRadius;
	//	maximumRadius = computeMaximumRadius(initialRadius);
	//	minimumRadius = computeMinimumRadius(initialRadius);	
	//	maximumVolume = computeMaximumVolume(maximumRadius);	
	if (nodeList.size() < 2) {
	    Environment.die("[Cell] node list has " + nodeList.size()
			    + " nodes");
	}
	//	length = 0;
	//	Point3D last = null;
	//	Point3D p = null;

	//	System.out.println("[Cell] starting node list iteration");


	//	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	//	    Node n = i.next();
	//	    p = n.getLocation();
	//	    if (last != null) {
	//		//		length += distance(last, p);
	//	    }
	//	    last = p;
	//	}


	//	volume = computedLength() * Math.PI * sqr(radius);
	/*
	if (volume > maximumVolume) {
	    Environment.die("[Cell] excessive volume for " + toString());
	}
	*/

	//	System.out.println("[Cell] starting createGridRepresentation");


	gridRepresentation = createGridRepresentation();

	//	System.out.println("[Cell] starting addToGrid");

	addToGrid(gridRepresentation);

	// nonsphere cells start as active
	state = EnvironmentInterface.CellState.ACTIVE;

    }

    //    public Cell(LinkedList<Node> nodeList,
    //		double initialRadius, double minimumRadius, double maximumRadius) {
    //	idNumber = nextIdNumber++;
    //	sproutCellsCreated++;
    //	this.nodeList = nodeList;
    //	this.initialRadius = initialRadius;
    //	radius = initialRadius;
    //	this.maximumRadius = maximumRadius;
    //	this.minimumRadius = minimumRadius;
    //	maximumVolume = computeMaximumVolume(maximumRadius);	
    //	if (nodeList.size() < 2) {
    //	    Environment.die("[Cell] node list has " + nodeList.size()
    //			    + " nodes");
    //	}
    //	gridRepresentation = createGridRepresentation();
    //	addToGrid(gridRepresentation);
    //	// nonsphere cells start as active
    //	state = EnvironmentInterface.CellState.ACTIVE;
    //    }

    //    public Cell(LinkedList<Node> nodeList) {
    //	this(nodeList, generateInitialRadius());
    //    }


    public static int getLastSteppedCell() {
	return lastSteppedCell;
    }

    public static int getBaseSphereCellCreationCount() {
	return baseSphereCellsCreated;
    }

    public static int getSproutCellCreationCount() {
	return sproutCellsCreated;
    }


    //    private static double generateInitialRadius() {
    //	double initialRadiusRange = maximumInitialRadius - minimumInitialRadius;
    //    //	double rand = env.random.nextDouble();
    //	return minimumInitialRadius + (rand * initialRadiusRange);
    //    }

    //    private static double computeMaximumRadius(double initialRadius) {
    //	return initialRadius * maximumRadiusFactor;
    //    }

    //    static {
    //    	System.out.println("**** [Cell] minimum cell radius forced to 1.8");
    //    }

    //    private static double computeMinimumRadius(double initialRadius) {
    //	return initialRadius * minimumRadiusFactor;
    //	return 1.8;
    //    }


    //    private static double computeMaximumVolume(double maximumRadius) {
    //	return computeVolume(maximumRadius, maximumLength);
    //    }

    public int activate() {
	//	if (baseSphere.zCoord != 0) {
	//	    System.out.println("***** [Cell.activate]  Ignoring cell at z="
	//			       + baseSphere.zCoord);
	//	    return 0;
	//	}
	//	System.out.println("[Cell.activate] Activating cell at ("
	//			   + baseSphere.xCoord + "," + baseSphere.yCoord
	//			   + "," + baseSphere.zCoord + ")");

	if (isInhibited()) {
	    return 0;
	}
	if (baseSphere == null) {
	    Environment.die("[Cell.activate] cell is not in the spheroid: "
			    + this);
	}
	if (state != EnvironmentInterface.CellState.IDLE) {
	    Environment.die("[Cell.activate] cell is not idle: "
			    + this);
	}
	if (nodeList.size() != 1) {
	    Environment.die("[Cell.activate] node list has " + nodeList.size()
			    + " nodes:    " + this);
	}
	
	Point3D p0 = new Point3D(baseSphere.xCoord, baseSphere.yCoord, baseSphere.zCoord);
	Node n1 = nodeList.getFirst();
	Point3D p1 = n1.getLocation();
	Point3D p2 = p1.projectAway(p0, initialStalkCellLength);
	//	Point3D p2b = p1.projectAway2(p0, initialTipCellLength);
	Point3D p3 = p2.projectAway(p1, initialTipCellLength);
	//	Point3D p3b = p2.projectAway2(p1, initialStalkCellLength);
	//	Node n2 = new Node(new double[] {p2.x, p2.y, p2.z, 0});
	//	Node n3 = new Node(new double[] {p3.x, p3.y, p3.z, 0});
	Node n2 = new Node(p2);
	Node n3 = new Node(p3);
	Cell stalk = new Cell(n2, n1, initialSproutRadius);
	Cell tip = new Cell(n3, n2, initialSproutRadius);
	predecessor = stalk;
	stalk.setSuccessor(this);
	stalk.setPredecessor(tip);
	tip.setSuccessor(stalk);
	// register cells after predecessors have been set
	env.registerCell(stalk);
	env.registerCell(tip);
	inhibitNeighborsBFS(inhibitionRange);
	state = EnvironmentInterface.CellState.QUIESCENT;

	if (!stalk.nodeList.getLast().getLocation().equals(nodeList.getFirst().getLocation())) {
	    Environment.die("[Cell.activate] mismatched stalk: " + stalk
			    + " to spheroid cell: " + this);
	}

	/*
	if (idNumber == 103 && stalk.getIdNumber() == 270) {
	    System.out.println("[Cell.activate] 103 properly joined to 270 "
			       + this + "      " + stalk);
	}
	*/
	// check that p0, p1, p2, and p3 are collinear

	/*
	if (!(p0.isCollinear(p1, p2))) {
	    Environment.die("[Cell.activate] p0 p1 p2 not collinear "
			    + p0 + "     " + p1 + "     " + p2);
	}
	if (!(p1.isCollinear(p2, p3))) {
	    Environment.die("[Cell.activate] p1 p2 p3 not collinear "
			    + p1 + "     " + p2 + "     " + p3);
	}
	if (!(p2.isCollinear(p3, p0))) {
	    Environment.die("[Cell.activate] p2 p3 p0 not collinear "
			    + p2 + "     " + p3 + "     " + p0);
	}
	if (!(p3.isCollinear(p0, p1))) {
	    Environment.die("[Cell.activate] p3 p0 p1 not collinear "
			    + p3 + "     " + p0 + "     " + p1);
	}
	*/

	/*
	System.out.println("[Cell.activate] " + p2);
	System.out.println("[Cell.activate] " + p2b);
    	System.out.println("[Cell.activate] " + p3);
	System.out.println("[Cell.activate] " + p3b);
	*/

	double len = nodeListLength(nodeList);
	if (Math.abs(len - computedLength()) > .00000001) {
	    Environment.die("Mismatch: length=" + computedLength() + " node list length=" + len
			    + "   " + this);
	}

	log.println("[Cell.activate] tip: " + tip + "\nstalk: " + stalk,
		    LogStreamInterface.BASIC_LOG_DETAIL);

	return 1;
    }
    

    // for test purposes only
    public static void testInhibition() {
	System.out.println("[Cell.testInhibition] start");
	spheroid.Sphere s1 = new spheroid.Sphere();
	s1.xCoord = 0;
	s1.yCoord = 0;
	s1.zCoord = 0;
	s1.radius = 1;
	s1.projection = new Point3D(1, 0, 0);
	Cell c1 = new Cell(s1);
	Cell c2 = new Cell(s1);
	Cell c3 = new Cell(s1);
	c1.spheroidCellNeighbors.add(c2);
	c1.spheroidCellNeighbors.add(c3);
	c2.spheroidCellNeighbors.add(c1);
	c2.spheroidCellNeighbors.add(c3);
	c3.spheroidCellNeighbors.add(c1);
	c3.spheroidCellNeighbors.add(c2);

	c1.inhibitNeighborsBFS(2);

	System.out.println(c1);
	System.out.println();
	System.out.println(c2);
	System.out.println();
	System.out.println(c3);
	System.out.println();

	c3.inhibitNeighborsBFS(1);

	System.out.println(c1);
	System.out.println();
	System.out.println(c2);
	System.out.println();
	System.out.println(c3);
	System.out.println();


    }

    // for test purposes only
    public static void testActivation() {
	System.out.println("[Cell.testActivation] inhibitionRange=" + inhibitionRange);
	spheroid.Sphere s1 = new spheroid.Sphere();
	s1.xCoord = 0;
	s1.yCoord = 0;
	s1.zCoord = 0;
	s1.radius = 1;
	s1.projection = new Point3D(1, 0, 0);
	Cell c1 = new Cell(s1);
	Cell c2 = new Cell(s1);
	Cell c3 = new Cell(s1);
	c1.addSpheroidCellNeighbor(c2);
	c1.addSpheroidCellNeighbor(c3);
	c2.addSpheroidCellNeighbor(c1);
	c2.addSpheroidCellNeighbor(c3);
	c3.addSpheroidCellNeighbor(c1);
	c3.addSpheroidCellNeighbor(c2);

	/*
	c1.activate();
	c2.activate();
	c3.activate();
	*/

	c1.step(env);
	c2.step(env);
	c3.step(env);

	c1.step(env);
	c2.step(env);
	c3.step(env);

	c1.step(env);
	c2.step(env);
	c3.step(env);



	System.out.println(c1);
	System.out.println();
	System.out.println(c2);
	System.out.println();
	System.out.println(c3);
	System.out.println();


	System.out.println("[Cell.testActivation] new sprout and tip");
	System.out.println();
	
	Cell p = c1.getPredecessor();
	if (p != null) {
	    System.out.println(p);
	    System.out.println();
	    Cell pp = p.getPredecessor();
	    if (pp != null) {
		System.out.println(pp);
		System.out.println();
		if (pp.getPredecessor() != null) {
		    System.out.println("MORE THAN 2 CELLS IN SPROUT!");
		}
	    }
	    else {
		System.out.println("No stalk cell");
	    }
	}
	else {
	    System.out.println("No stalk or tip cells");
	}
    }

    // for test purposes only
    public void testShapeChange() {
	baseSphere = null;
	double[] params = new double[] {3, 0, 0, 0};
	Node n = new Node(new Point3D(3, 0, 0));
	nodeList.add(n);
	radius = 1;
	
	redrawRepresentation();

    }


    // for test purposes only
    public static Cell buildSprout(LinkedList<LinkedList<Node>> nodeListList) {
	Cell first = null;
	Cell prev = null;
	for (Iterator<LinkedList<Node>> i = nodeListList.iterator(); i.hasNext();) {
	    LinkedList<Node> nodeList = i.next();
	    Cell c = new Cell(nodeList, 1);
	    c.setSuccessor(prev);
	    if (prev != null) {
		prev.setPredecessor(c);
	    }
	    if (first == null) {
		first = c;
	    }
	    prev = c;
	}
	return first;
    }

    public static void initialize(Environment e) {
	nextIdNumber = 0;
	divisionCount = 0;
	migrationCount = 0;
	attemptedMigrationCount = 0;
	redirectionCount = 0;
	branchCount = 0;
	limitedXYBranchCount = 0;
	limitedXYBranchStarts = new LinkedList<Cell>();
	baseSphereCellsCreated = 0;
	sproutCellsCreated = 0;
	attemptedMigrationDistance = 0;
	actualMigrationDistance = 0;

	env = e;
	inhibitionRange = e.getInhibitionRange();
	log = e.getLog();
	grid = e.grid;
	useSpecifiedOutputGeometryPrecision = e.outputGeometryPrecisionSpecified();
	outputGeometryPrecisionFactor = Math.pow(10, e.getOutputGeometryPrecision());

	ignoreDiscretizedSprouts = e.ignoreDiscretizedSprouts();
	
	//	minimumInitialRadius = e.getMinimumInitialCellRadiusMicrons();
	//	maximumInitialRadius = e.getMaximumInitialCellRadiusMicrons();
	//	maximumRadiusFactor = e.getMaximumCellRadiusFactor();
	//	minimumRadiusFactor = e.getMinimumCellRadiusFactor();
	//	maximumLength = e.getMaximumCellLengthMicrons();

	initialSproutRadius = e.getInitialSproutRadiusMicrons();
	//	minimumCellRadius = e.getMinimumCellRadiusMicrons();
	//	maximumCellRadius = e.getMaximumCellRadiusMicrons();
	//	maximumCellLength = e.getMaximumCellLengthMicrons();
	//	maximumCellVolume =
	//	    computeVolume(maximumCellRadius, maximumCellLength);
	//	lengthToRadiusRatio = maximumCellLength / maximumCellRadius;
	//	tipLengthToRadiusRatio = 1.5 * lengthToRadiusRatio;

	initialStalkCellLength = e.getInitialStalkCellLengthMicrons();
	initialTipCellLength = e.getInitialTipCellLengthMicrons();

	tipCellPrecedence = e.tipCellsHavePrecedence();

	spheroidCenter = e.getSpheroidCenter();
	spheroidRadius = e.getSpheroidDiameterMicrons() / 2.0;

	minimumLengthToRadiusRatio =
	    e.getMinimumCellLengthToWidthRatio() * 2.0;
	maximumLengthToRadiusRatio =
	    e.getMaximumCellLengthToWidthRatio() * 2.0;
	lengthToRadiusRatioTransitionDistanceCells =
	    e.getLengthToWidthRatioTransitionDistanceCells();
	//	minimumTipLengthToRadiusRatio =
	//	    e.getMinimumTipCellLengthToWidthRatio() * 2.0;
	//	maximumTipLengthToRadiusRatio =
	//	    e.getMaximumTipCellLengthToWidthRatio() * 2.0;

	maximumElongationFactor = e.getMaximumElongationFactor();

	minimumDivisionLength = e.getMinimumDivisionLengthMicrons();
	divisionProbConst1 = e.getDivisionProbabilityConstant1();
	divisionProbConst2 = e.getDivisionProbabilityConstant2();

	numberOfSproutColors = e.getNumberOfSproutColors();

	//	System.out.println("[Cell.intialize] elongationFactor="
	//			   + elongationFactor);
	

	//	System.out.println(minimumDivisionVolume + " " + divisionProbConst1
	//			   + " " + divisionProbConst2);


	//	System.out.println("[Cell.initialize] minimumTipLengthToRadiusRatio="
	//			   + minimumTipLengthToRadiusRatio
	//			   + " maximumTipLengthToRadiusRatio="
	//			   + maximumTipLengthToRadiusRatio);

	//	System.out.println("[Cell.initialize] minimumLengthToRadiusRatio="
	//			   + minimumLengthToRadiusRatio
	//			   + " maximumLengthToRadiusRatio="
	//			   + maximumLengthToRadiusRatio);

	//	System.out.println("[Cell.initialize] tipCellPrecedence=" + tipCellPrecedence);

	/*
	System.out.println("[Cell.initialize] minimumInitialRadius=" + minimumInitialRadius);
	System.out.println("[Cell.initialize] maximumInitialRadius=" + maximumInitialRadius);
	System.out.println("[Cell.initialize] maximumRadiusFactor=" + maximumRadiusFactor);
	System.out.println("[Cell.initialize] minimumRadiusFactor=" + minimumRadiusFactor);
	System.out.println("[Cell.initialize] maximumLength=" + maximumLength);
	System.out.println("[Cell.initialize] initialStalkCellLength=" + initialStalkCellLength);
	System.out.println("[Cell.initialize] initialTipCellLength=" + initialTipCellLength);
	*/

	//	System.out.println("[Cell.initialize] grid=" + grid);

    }


    public double getVolumeCubicMicrons() {
	if (nodeList.size() < 2) {
	    Environment.die("[Cell.getVolume] Cell is not part of a sprout"
			    + this);
	}
	return computedVolume();
    }

    public double getLength() {
	if (nodeList.size() < 2) {
	    Environment.die("[Cell.getLength] Cell is not part of a sprout"
			    + this);
	}
	return computedLength();
    }

    // copies cell by copying nodes and entire successor chain; hence,
    // invoke it on the tip cell to reproduce predecessors.
    public Cell extendedCopy() {
	LinkedList<Node> copiedNodes = new LinkedList<Node>();
	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	    Node n = i.next();
	    copiedNodes.addLast(n.copy());
	}
	Cell cellCopy = new Cell(copiedNodes, radius);
	if (successor != null) {
	    Cell successorCopy = getSuccessor().extendedCopy();
	    cellCopy.setSuccessor(successorCopy);
	    successorCopy.setPredecessor(cellCopy);
	}
	return cellCopy;
    }
	
	


    public Point3D getFrontLocation() {
	return nodeList.getFirst().getLocation().copy();
    }

    public int getIdNumber() {
	return idNumber;
    }


    // Consider going back to enumerated type
    public boolean isTipCell() {
	return (baseSphere == null && predecessor == null && predecessorB == null);
    }

    public boolean isStalkCell() {
	return (predecessor != null && predecessor.isTipCell()
		|| predecessorB !=null && predecessorB.isTipCell());
    }


    public static void testCellPosition() {
	Cell c = new Cell();
	c.predecessor = null;
	c.predecessorB = null;
	c.baseSphere = new spheroid.Sphere();
	if (c.getCellPosition() != CellInterface.CellPosition.CLUSTER) {
	    Environment.die("[Cell.testCellPosiiton] Expected CLUSTER " + c.getCellPosition());
	}
	c.baseSphere = null;
	if (c.getCellPosition() != CellInterface.CellPosition.TIP) {
	    Environment.die("[Cell.testCellPosiiton] Expected TIP " + c.getCellPosition());
	}
	Cell c2 = new Cell();
	c.predecessor = c2;
	if (c.getCellPosition() != CellInterface.CellPosition.STALK) {
	    Environment.die("[Cell.testCellPosiiton] Expected STALK " + c.getCellPosition());
	}
	c.predecessorB = c2;
	if (c.getCellPosition() != CellInterface.CellPosition.STALK) {
	    Environment.die("[Cell.testCellPosiiton] Expected STALK " + c.getCellPosition());
	}
	Cell c3 = new Cell();
	c2.predecessorB = c3;
	if (c.getCellPosition() != CellInterface.CellPosition.REAR) {
	    Environment.die("[Cell.testCellPosiiton] Expected REAR " + c.getCellPosition());
	}
	System.out.println("[Cell.testCellPosition] All tests passed!");
    }


    public CellInterface.CellPosition getCellPosition() {
	CellInterface.CellPosition position = null;
	if (baseSphere != null) {
	    position = CellInterface.CellPosition.CLUSTER;
	}
	else {
	    if (predecessor == null  && predecessorB == null) {
		position = CellInterface.CellPosition.TIP;
	    }
	    else {
		if ((predecessor != null && predecessor.isTipCell())
		    || (predecessorB != null && predecessorB.isTipCell())) {
		    position = CellInterface.CellPosition.STALK;
		}
		else {
		    position = CellInterface.CellPosition.REAR;
		}
	    }
	}
	return position;
    }


    private static void testDistanceToTipCells() {
	Cell c1 = new Cell();
	Cell c2 = new Cell();
	Cell c3 = new Cell();
	Cell c4 = new Cell();
	Cell c5 = new Cell();

	c1.successor = c2;
	c2.predecessor = c1;

	c3.successor = c4;
	c4.predecessor = c3;

	c4.successor = c2;
	c2.predecessorB = c4;

	c2.successor = c5;
	c5.predecessor = c2;

	double dist;
	dist = c1.distanceToTipCells();
	if (dist != 0) {
	    Environment.die("[Cell.testDistanceToTipCells] c1.distanceToTipCells()="
			     + dist);
	}

	dist = c3.distanceToTipCells();
	if (dist != 0) {
	    Environment.die("[Cell.testDistanceToTipCells] c3.distanceToTipCells()="
			     + dist);
	}

	dist = c4.distanceToTipCells();
	if (dist != 1) {
	    Environment.die("[Cell.testDistanceToTipCells] c4.distanceToTipCells()="
			     + dist);
	}

	dist = c2.distanceToTipCells();
	if (dist != 2) {
	    Environment.die("[Cell.testDistanceToTipCells] c2.distanceToTipCells()="
			     + dist);
	}

	dist = c5.distanceToTipCells();
	if (dist != 3) {
	    Environment.die("[Cell.testDistanceToTipCells] c5.distanceToTipCells()="
			     + dist);
	}

	System.out.println("[Cell.testDistanceToTipCells] passed");
    }


    // Nonrecursive version
    public int distanceToTipCells() {
	int maxDistance = 0;
	LinkedList<CellDistanceObject> stack =
	    new LinkedList<CellDistanceObject>();
	stack.add(new CellDistanceObject(this, 0));
	int stackSize = 1;
	while (stackSize > 0) {
	    CellDistanceObject cdo = stack.removeFirst();
	    stackSize--;
	    Cell c = cdo.cell;
	    Cell pred = c.predecessor;
	    Cell predB = c.predecessorB;
	    if (pred == null) {
		if (predB == null) {
		    maxDistance = Math.max(maxDistance, cdo.distance);
		}
		else {
		    stack.addFirst(new CellDistanceObject(predB,
							  cdo.distance + 1));
		    stackSize++;
		}
	    }
	    else {
		if (predB == null) {
		    stack.addFirst(new CellDistanceObject(pred,
							  cdo.distance + 1));
		    stackSize++;
		}
		else {
		    stack.addFirst(new CellDistanceObject(predB,
							  cdo.distance + 1));
		    stackSize++;
		    stack.addFirst(new CellDistanceObject(pred,
							  cdo.distance + 1));
		    stackSize++;
		}
	    }
	}
	if (stack.size() != 0) {
	    Environment.die("[Cell.distanceToTipCell] stack size is not 0: "
			    + stack.size());
	}
	return maxDistance;
    }
    

    public int distanceToTipCellsOLD() {
	int dist = 
	    (predecessor == null) ? 0 : (1 + predecessor.distanceToTipCells());
	int distB = 
	    (predecessorB == null) ? 0 : (1 + predecessorB.distanceToTipCells());
	return Math.max(dist, distB);
    }



    private static void testComputeLengthToRadiusRatio() {
	minimumLengthToRadiusRatio = 1;
	maximumLengthToRadiusRatio = 5;
	lengthToRadiusRatioTransitionDistanceCells = 4;

	Cell c1 = new Cell();
	Cell c2 = new Cell();
	Cell c3 = new Cell();
	Cell c4 = new Cell();
	Cell c5 = new Cell();
	Cell c6 = new Cell();

	c1.successor = c2;
	c2.predecessor = c1;

	c2.successor = c3;
	c3.predecessor = c2;

	c3.successor = c4;
	c4.predecessor = c3;

	c4.successor = c5;
	c5.predecessor = c4;

	c5.successor = c6;
	c6.predecessor = c5;

	double lrr;
	
	lrr = c1.computeLengthToRadiusRatio();
	if (lrr != 5) {
	    Environment.die("[Cell.testComputeLengthToRadiusRatio] c1.computeLengthToRadiusRatio()="
			    + lrr);
	}

	lrr = c2.computeLengthToRadiusRatio();
	if (lrr != 4) {
	    Environment.die("[Cell.testComputeLengthToRadiusRatio] c2.computeLengthToRadiusRatio()="
			    + lrr);
	}

	lrr = c3.computeLengthToRadiusRatio();
	if (lrr != 3) {
	    Environment.die("[Cell.testComputeLengthToRadiusRatio] c3.computeLengthToRadiusRatio()="
			    + lrr);
	}

	lrr = c4.computeLengthToRadiusRatio();
	if (lrr != 2) {
	    Environment.die("[Cell.testComputeLengthToRadiusRatio] c4.computeLengthToRadiusRatio()="
			    + lrr);
	}

	lrr = c5.computeLengthToRadiusRatio();
	if (lrr != 1) {
	    Environment.die("[Cell.testComputeLengthToRadiusRatio] c5.computeLengthToRadiusRatio()="
			    + lrr);
	}

	lrr = c6.computeLengthToRadiusRatio();
	if (lrr != 1) {
	    Environment.die("[Cell.testComputeLengthToRadiusRatio] c6.computeLengthToRadiusRatio()="
			    + lrr);
	}

	System.out.println("[Cell.testComputeLengthToRadiusRatio] passed");

    }

    private double computeLengthToRadiusRatio() {
	int dist = distanceToTipCells();
	double lrRatio;
	if (dist >= lengthToRadiusRatioTransitionDistanceCells) {
	    lrRatio = minimumLengthToRadiusRatio;
	}
	else {
	    double m =
		(maximumLengthToRadiusRatio - minimumLengthToRadiusRatio)
		/ lengthToRadiusRatioTransitionDistanceCells;
	    lrRatio = maximumLengthToRadiusRatio - (m * dist);
	}
	return lrRatio;
    }


    // byron TEST
    public double removableVolumeCubicMicrons() {
	double ltr = computeLengthToRadiusRatio();
	double volume = computedVolume();
	// Cell length remains fixed after volume change, so removable
	// volume must be accounted for by just a radius reduction.
	// Assuming that the constant used for elongation represents
	// maximum practical length to radius ratio, compute the
	// radius that would reach this extreme ratio at the current
	// length.  The difference between the current volume and the
	// volume arising from the reduced radius is the removable
	// volume.
	double length = computedLength();
	// If the length is the maximum elongated length, then what is
	// the corresponding minimum radius?
	double preferredLength = length / (1 + maximumElongationFactor);
	double minimumRadius = preferredLength / ltr;
	double minimumVolume = computeVolume(minimumRadius, length);
	// compute the difference between the current volume and the
	// volume when the radius is at a minimum
	double volumeAboveMinimum = volume - minimumVolume;
	//	if (volumeAboveMinimum < 0) {
	//	    Environment.die("[Cell.removableVolumeCubicMicrons] Cell "
	//	    + idNumber + " has negative removable volume: "
	//			    + volumeAboveMinimum + " volume=" + volume
	//			    + " length=" + length + " preferredLength="
	//			    + preferredLength + " minimumRadius="
	//			    + minimumRadius + " minimumVolume="
	//			    + minimumVolume);
	//	}
	return Math.max(0, volumeAboveMinimum);
    }


    public static void testBranch() {
	//	Node n1r = new Node(new double[] {0, 0, -1});
	//	Node n1f = new Node(new double[] {0, 0, 0});
	Node n1r = new Node(new Point3D(0, 0, -1));
	Node n1f = new Node(new Point3D(0, 0, 0));
	Cell c1 = new Cell(n1f, n1r, 2);

	//	Node n2r = new Node(new double[] {0, 0, 0});
	//	Node n2f = new Node(new double[] {5, 0, 0});
	Node n2r = new Node(new Point3D(0, 0, 0));
	Node n2f = new Node(new Point3D(5, 0, 0));
	Cell c2 = new Cell(n2f, n2r, 2);
	
	c1.setPredecessor(c2);
	c2.setSuccessor(c1);

	Point3D branchTip = new Point3D(0, 0, 1);


	Point3D p1 = n1r.getLocation();
	Point3D p2 = n2r.getLocation();
	
	System.out.println("[Cell.branchTest] " + p1 + " " + p2
			   + " " + branchTip + " " + p1.isCollinear(p2, branchTip));

	System.out.println(c1);
	System.out.println(c2);

	c2.branch(branchTip, 1);

	System.out.println(c1);
	System.out.println(c2);
	System.out.println(c1.predecessorB);
    }
    

    // returns new branched cell or null
    public Cell branch(Point3D branchTip, double branchRadius) {
	if (debugFlag && idNumber == 2327) {
	    System.out.println("[Cell.branch] idNumber=" + idNumber
			       + " branchTip=" + branchTip
			       + " branchRadius=" + branchRadius);
	}

	Cell branchCell = null;
	Node rear = nodeList.get(nodeList.size() - 1).copy();
	Point3D rearPoint = rear.getLocation();
	double branchLength = branchTip.distance(rearPoint);

	double branchVolume = computeVolume(branchRadius, branchLength);

	double volume = computedVolume();
	double removableVolume = removableVolumeCubicMicrons();
	if (removableVolume < branchVolume) {
	    // not necessarily a problem, but let's see when and if it happens
	    Environment.die("[Cell.branch] branch volume " + branchVolume
			    + " is more than excess cell volume "
			    + removableVolumeCubicMicrons());
	}
	if (removableVolume >= branchVolume
	    && branchVolume <= (0.5 * volume)) {
	    double newVolume = volume - branchVolume;
	    resize(newVolume);
	    changedShape = true;

	    // The status of the rear node as an inflection point *for the
	    // new cell* may have changed due to the new cell's different
	    // front node
	    if (successor != null && successor.baseSphere == null) {
		if (!branchTip.isCollinear(rearPoint, successor.nodeList.get(1).getLocation())) {
		    rear.setInflection();
		}
	    }

	    //	    double[] params =
	    //		new double[] {branchTip.x, branchTip.y, branchTip.z, 0};
	    Node front = new Node(branchTip);
	    branched = true;
	    branchCell = new Cell(front, rear, branchRadius);
	    branchCell.onBranch = true;

	    // insert cell into sprout
	    branchCell.setPredecessor(null);
	    branchCell.setSuccessor(successor);
	    if (successor.getPredecessorB() != null) {
		Environment.die("[Cell.branch] Attempting to add 3rd branch to "
				+ successor);
	    }
	    successor.setPredecessorB(branchCell);
	    successor.setBranchAhead();
	    
	    // register cell after its predecessor has been set
	    env.registerCell(branchCell);
	    
	    branchCount++;
	    if (rearPoint.xyDistance(spheroidCenter) > spheroidRadius) {
		limitedXYBranchCount++;
		limitedXYBranchStarts.add(branchCell);
		branchCell.onLimitedXYBranch = true;
		Cell origin = branchCell;
		//		System.out.print("New branch cell:");
		//		while (origin.successor != null) {
		//		    System.out.print(" " + origin.idNumber);
		//		    origin = origin.successor;
		//		}
		//		System.out.println(" " + origin.idNumber + ": base cell");
		//		System.out.println("[Cell.branch] branch point at "
		//				   + rearPoint + " xyDist="
		//				   + rearPoint.xyDistance(spheroidCenter)
		//				   + " spheroidRadius=" + spheroidRadius
		//				   + " successor cell "
		//				   + successor.idNumber
		//				   + " origin cell " + origin.idNumber);
	    }
	    log.println("[Cell.branch] New cell " + branchCell.idNumber
			+ " branched from cell " + idNumber,
			LogStreamInterface.BASIC_LOG_DETAIL);
	}
	if (debugFlag && idNumber == 2327) {
	    System.out.println("[Cell.branch] idNumber=" + idNumber
			       + " End");
	}
	return branchCell;
    }

//    public void branch(Point3D branchTip) {
//	Node rear = nodeList.get(nodeList.size() - 1);
//	Point3D p = rear.getLocation();
//
//	double branchLength = branchTip.distance(p);
//
//	double[] params =
//	    new double[] {branchTip.x, branchTip.y, branchTip.z, 0};
//	Node front = new Node(params);
//
//	//	volume = volume / 2.0;
//	double halfVolume = computedVolume() / 2.0;
//
//	double newRadius = computeRadius(halfVolume, computedLength());
//
//	// note that the new radius may be less than the minimum radius
//	radius = newRadius;
//
//
//
//
//	double branchRadius = computeRadius(halfVolume, branchLength);
//	Cell c = new Cell(front, rear, branchRadius);
//	env.registerCell(c);
//
//	// insert cell into sprout
//	c.setPredecessor(null);
//	c.setSuccessor(successor);
//	if (successor.getPredecessorB() != null) {
//	    Environment.die("[Cell.branch] Attempting to add 3rd branch to "
//			    + successor);
//	}
//	successor.setPredecessorB(c);
//	successor.setBranchAhead();
//	changedShape = true;
//
//	double len = nodeListLength(nodeList);
//	if (Math.abs(len - computedLength()) > .00000001) {
//	    Environment.die("Mismatch: length=" + computedLength() + " node list length=" + len
//			    + "   " + this);
//	}
//
//	branchCount++;
//    }


    private void setBranchAhead() {
	branchAhead = true;
	if (successor != null) {
	    successor.setBranchAhead();
	}
    }

    private void setJunctionAhead() {
	junctionAhead = true;
	if (successor != null) {
	    successor.setJunctionAhead();
	}
    }

    public boolean hasBranchAhead() {
	return branchAhead;
    }

    public boolean hasJunctionAhead() {
	return junctionAhead;
    }




    private void taperTipCell() {
	if (predecessor != null || predecessorB != null) {
	    Environment.die("[Cell.taperTipCell] Cell " + idNumber
			    + " is not a tip cell");
	}
	// Refer to succesor's radius if successor is not part of spheroid
	if (successor == null || successor.baseSphere != null) {
	    Environment.die("[Cell.taperTipCell] Tip cell " + idNumber
			    + " has no successor or has a successor in the spheroid");	    
	}
	double successorRadius = successor.radius;
	// Change radius if it is greater than the successor's radius
	if (radius > successorRadius) {
	    double length = computedLength();
	    double volume = computeVolume(radius, length);
	    double newLength = computeLength(volume, successorRadius);
	    double lengthIncrease = newLength - length;
	    if (lengthIncrease < 0) {
		Environment.die("[Cell.taperTipCell] Cell " + idNumber
				+ " has a length: " + length
				+ " and new length: " + newLength);
	    }
	    Node firstNode = nodeList.removeFirst();
	    Node secondNode = nodeList.getFirst();
	    Point3D firstLocation = firstNode.getLocation();
	    Point3D secondLocation = secondNode.getLocation();
	    Point3D newFirstLocation =
		firstLocation.projectAway(secondLocation, lengthIncrease);

	    boolean actualChange = true;
	    
	    if (firstLocation.equals(newFirstLocation)) {
		actualChange = false;
		log.println("[Cell.taperTipCell] cell " + idNumber
			    + " length increase " + lengthIncrease
			    + " is too small to effect cell position",
			    LogStreamInterface.BASIC_LOG_DETAIL);
	    }
	    double angle = 0;
	    if (actualChange) {
		// make sure cell travels straight ahead
		double tolerance = .00001;
		Point3D straightAhead =
		    firstLocation.plus(firstLocation.minus(secondLocation));
		angle =
		    firstLocation.angleDegrees(straightAhead,
					       newFirstLocation);
		if (Math.abs(angle - 0) > tolerance) {
		    actualChange = false;
		    log.println("[Cell.taperTipCell] cell " + idNumber
				+ " size change is too small to maintain orientation "
				+ angle + " degrees",
				LogStreamInterface.BASIC_LOG_DETAIL);
		}
	    }
	    if (!actualChange) {
		// add back node removed
		nodeList.addFirst(firstNode);
	    }
	    else {
		radius = successorRadius;
		Node newFirstNode = new Node(newFirstLocation);
		nodeList.addFirst(newFirstNode);
		log.println("[Cell.taperTipCell] Cell " + idNumber
			    + " tip cell front extended to "
			    + newFirstLocation + " ("
			    + firstLocation.distance(newFirstLocation)
			    + " microns) resize angle: " + angle
			    + " degrees",
			    LogStreamInterface.BASIC_LOG_DETAIL);
	    }
	    
	    
	}
    }
    

    // migration vector added to front point to get desired new front point
    // returns elongation distance of stalk cell
    public double migrate(Point3D migrationVector) {
	if (predecessor != null || predecessorB != null) {
	    Environment.die("[Cell.migrate] undefined for nontip cells");
	}
	if (successor == null) {
	    Environment.die("[Cell.migrate] cell has no stalk cell");
	}

	if (junctionAhead) {
	    return 0;
	}
	double volume = computedVolume();

	Node frontNode = nodeList.getFirst();
	Point3D frontPoint = frontNode.getLocation();
	Point3D secondPoint = nodeList.get(1).getLocation();
	Point3D newFrontPoint = frontPoint.plus(migrationVector);
	
	Point3D straightAhead = frontPoint.plus(frontPoint.minus(secondPoint));
	double migrationAngleDegrees =
	    frontPoint.angleDegrees(straightAhead, newFrontPoint);
	double goalDistance = frontPoint.distance(newFrontPoint);

	// First, tip cell elongates to cover the migration distance
	double tipElongationDistance = Math.min(goalDistance,
						extendableDistance());
	double remainingGoalDistance = goalDistance - tipElongationDistance;

	// Second, stalk cell elongates to cover remaining distance 
	double stalkElongationDistance =
	    Math.min(remainingGoalDistance, successor.extendableDistance());
	
	double actualDistance =
	    tipElongationDistance + stalkElongationDistance;


	attemptedMigrationDistance += goalDistance;
	actualMigrationDistance += actualDistance;

	if (actualDistance < 0) {
	    Environment.die("[Cell.migrate] negative actual distance: "
			    + actualDistance
			    + " " + goalDistance + " "
			    + successor.extendableDistance());
	}

	attemptedMigrationCount++;
	if (goalDistance > actualDistance) {
	
	    // reset newFrontPoint for shorter migration distance
	    newFrontPoint =
		frontPoint.projectTowards(newFrontPoint, actualDistance);
	  
	    if (newFrontPoint.equals(frontPoint)) {
		// distance is to small to have an effect
		log.println("[Cell.migrate] Cell " + idNumber
			    + " migration distance too small "
			    + actualDistance + " to effect cell front",
			    LogStreamInterface.BASIC_LOG_DETAIL);
		actualDistance = 0;
	    }
	    else {
		double newMigrationAngleDegrees =
		    frontPoint.angleDegrees(straightAhead, newFrontPoint);
		double tolerance = .000001;
		if (Math.abs(migrationAngleDegrees - newMigrationAngleDegrees)
		    > tolerance
		    && actualDistance < tolerance) {
		    log.println("[Cell.migrate] Cell " + idNumber
				+ " migration distance too small "
				+ actualDistance
				+ " to maintain the initial migration angle",
				LogStreamInterface.BASIC_LOG_DETAIL);
		    actualDistance = 0;
		}

	    }
	}
	if (actualDistance > 0) {
	    double angleDegrees =
		frontPoint.angleDegrees(straightAhead, newFrontPoint);;
	    log.println("[Cell.migrate] Cell " + idNumber
			+ " frontPoint=" + frontPoint
			+ " secondPoint=" + secondPoint
			+ " migrationVector=" + migrationVector
			+ " newFrontPoint=" + newFrontPoint
			+ " angle: " + angleDegrees + " degrees",
			LogStreamInterface.BASIC_LOG_DETAIL);

	    Node newFrontNode = new Node(newFrontPoint);
	    boolean collinear =
		newFrontPoint.isCollinear(frontPoint, secondPoint); 
	    
	    
	    migrationCount++;
	    
	    if (collinear) {
		nodeList.removeFirst();
	    }	
	    else {
		redirectionCount++;
		frontNode.setInflection();
	    }
	    
	    
	    nodeList.addFirst(newFrontNode);
	    
	    log.println("[Cell.migrate] Cell " + idNumber
			+ " migration angle: "
			+ angleDegrees
			+ " degrees",
			LogStreamInterface.BASIC_LOG_DETAIL);
	    
	    log.println("[Cell.migrate] Cell " + idNumber
			+ " frontPoint=" + frontPoint + " secondPoint="
			+ secondPoint + " newFrontPoint=" + newFrontPoint,
			LogStreamInterface.BASIC_LOG_DETAIL);
	    
	    //	    LinkedList<Node> rearNodes = removeRear(actualDistance);
	    LinkedList<Node> rearNodes = removeRear(stalkElongationDistance);
	    successor.elongate(rearNodes, stalkElongationDistance);
	    //	    taperTipCell();
	    radius = computeRadius(volume, computedLength());
	    changedShape = true;
	}
	
	double len = nodeListLength(nodeList);
	if (Math.abs(len - computedLength()) > .00000001) {
	    Environment.die("[Cell.migrate] Mismatch: length="
			    + computedLength() + " node list length=" + len
			    + "   " + this);
	}
	
	log.println("[Cell.migrate] Cell " + idNumber + " migrated "
		    + actualDistance + " microns (goal distance: "
		    + goalDistance + ")",
		    LogStreamInterface.BASIC_LOG_DETAIL);
	
	return stalkElongationDistance;
	
    }

    public double migrateOLD(Point3D migrationVector) {
	if (predecessor != null || predecessorB != null) {
	    Environment.die("[Cell.migrate] undefined for nontip cells");
	}
	if (successor == null) {
	    Environment.die("[Cell.migrate] cell has no stalk cell");
	}

	if (junctionAhead) {
	    return 0;
	}

	Node frontNode = nodeList.getFirst();
	Point3D frontPoint = frontNode.getLocation();
	Point3D secondPoint = nodeList.get(1).getLocation();
	Point3D newFrontPoint = frontPoint.plus(migrationVector);
	
	Point3D straightAhead = frontPoint.plus(frontPoint.minus(secondPoint));
	double migrationAngleDegrees =
	    frontPoint.angleDegrees(straightAhead, newFrontPoint);
	double goalDistance = frontPoint.distance(newFrontPoint);

	// first elongate to cover the migration distance
	double tipExtensionDistance = Math.min(extendableDistance(),
					       goalDistance);
	double remainingGoalDistance = goalDistance - tipExtensionDistance;

	double actualDistance = Math.min(goalDistance,
					 successor.extendableDistance());
	attemptedMigrationDistance += goalDistance;
	actualMigrationDistance += actualDistance;

	if (actualDistance < 0) {
	    Environment.die("[Cell.migrate] negative actual distance: "
			    + actualDistance
			    + " " + goalDistance + " "
			    + successor.extendableDistance());
	}

	attemptedMigrationCount++;
	if (goalDistance > actualDistance) {
	    
	    newFrontPoint =
		frontPoint.projectTowards(newFrontPoint, actualDistance);
	  
	    
	    //	    System.out.println("[Cell.migrate] *** "
	    //			       + newFrontPoint.equals(frontPoint)
	    //			       + "  " + frontPoint + "  " + newFrontPoint);

	    if (newFrontPoint.equals(frontPoint)) {
		// distance is to small to have an effect
		log.println("[Cell.migrate] Cell " + idNumber
			    + " migration distance too small "
			    + actualDistance + " to effect cell front",
			    LogStreamInterface.BASIC_LOG_DETAIL);
		actualDistance = 0;
	    }
	    else {
		double newMigrationAngleDegrees =
		    frontPoint.angleDegrees(straightAhead, newFrontPoint);
		double tolerance = .000001;
		if (Math.abs(migrationAngleDegrees - newMigrationAngleDegrees)
		    > tolerance
		    && actualDistance < tolerance) {
		    log.println("[Cell.migrate] Cell " + idNumber
				+ " migration distance too small "
				+ actualDistance
				+ " to maintain the initial migration angle",
				LogStreamInterface.BASIC_LOG_DETAIL);
		    actualDistance = 0;
		}

	    }
	}
	if (actualDistance > 0) {
	    double angleDegrees =
		frontPoint.angleDegrees(straightAhead, newFrontPoint);;
	    log.println("[Cell.migrate] Cell " + idNumber
			+ " frontPoint=" + frontPoint
			+ " secondPoint=" + secondPoint
			+ " migrationVector=" + migrationVector
			+ " newFrontPoint=" + newFrontPoint
			+ " angle: " + angleDegrees + " degrees",
			LogStreamInterface.BASIC_LOG_DETAIL);

	    Node newFrontNode = new Node(newFrontPoint);
	    boolean collinear =
		newFrontPoint.isCollinear(frontPoint, secondPoint); 
	    
	    
	    migrationCount++;
	    
	    if (collinear) {
		nodeList.removeFirst();
	    }	
	    else {
		redirectionCount++;
		frontNode.setInflection();
	    }
	    
	    
	    nodeList.addFirst(newFrontNode);
	    
	    log.println("[Cell.migrate] Cell " + idNumber
			+ " migration angle: "
			+ angleDegrees
			+ " degrees",
			LogStreamInterface.BASIC_LOG_DETAIL);
	    
	    log.println("[Cell.migrate] Cell " + idNumber
			+ " frontPoint=" + frontPoint + " secondPoint="
			+ secondPoint + " newFrontPoint=" + newFrontPoint,
			LogStreamInterface.BASIC_LOG_DETAIL);
	    
	    LinkedList<Node> rearNodes = removeRear(actualDistance);
	    successor.elongate(rearNodes, actualDistance);
	    taperTipCell();
	    changedShape = true;
	}
	
	double len = nodeListLength(nodeList);
	if (Math.abs(len - computedLength()) > .00000001) {
	    Environment.die("[Cell.migrate] Mismatch: length="
			    + computedLength() + " node list length=" + len
			    + "   " + this);
	}
	
	log.println("[Cell.migrate] Cell " + idNumber + " migrated "
		    + actualDistance + " microns (goal distance: "
		    + goalDistance + ")",
		    LogStreamInterface.BASIC_LOG_DETAIL);
	
	return actualDistance;
	
    }


    // byron REWRITE

    public static void testExtendableDistance() {
	Environment e = new Environment(1);

	Cell.minimumLengthToRadiusRatio = 5;
	Cell.maximumLengthToRadiusRatio = 5;
	//	Cell.minimumTipLengthToRadiusRatio = 8;
	//	Cell.maximumTipLengthToRadiusRatio = 8;

	double dist;
	Point3D p1 = new Point3D(10, 0, 0);
	Point3D p2 = new Point3D(0, 0, 0);
	//	Node n1 = new Node(new double[] {p1.x, p1.y, p1.z});
	//	Node n2 = new Node(new double[] {p2.x, p2.y, p2.z});
	Node n1 = new Node(p1);
	Node n2 = new Node(p2);
	LinkedList<Node> nodeList = new LinkedList<Node>();
	nodeList.add(n1);
	nodeList.addLast(n2);
	Cell c = new Cell();
	c.nodeList = nodeList;
	c.radius = 2;
	Cell c2 = new Cell();
	c.predecessor = c2;
	dist = c.extendableDistance();
	if (dist != 2) {
	    Environment.die("[testProliferation] c.extendableDistance()="
			    + dist);
	}

	Point3D p3 = new Point3D(10, 0, 0);
	Point3D p4 = new Point3D(0, 0, 0);
	//	Node n3 = new Node(new double[] {p3.x, p3.y, p3.z});
	//	Node n4 = new Node(new double[] {p4.x, p4.y, p4.z});
	Node n3 = new Node(p3);
	Node n4 = new Node(p4);
	LinkedList<Node> nodeList2 = new LinkedList<Node>();
	nodeList2.add(n3);
	nodeList2.addLast(n4);
	Cell cb = new Cell();
	cb.nodeList = nodeList2;
	cb.radius = 1;
	cb.predecessor = c2;
	dist = cb.extendableDistance();
	if (dist != 0) {
	    Environment.die("[testProliferation] c.extendableDistance()="
			    + dist);
	}

	Point3D p5 = new Point3D(10, 0, 0);
	Point3D p6 = new Point3D(0, 0, 0);
	//	Node n5 = new Node(new double[] {p5.x, p5.y, p5.z});
	//	Node n6 = new Node(new double[] {p6.x, p6.y, p6.z});
	Node n5 = new Node(p5);
	Node n6 = new Node(p6);
	LinkedList<Node> nodeList3 = new LinkedList<Node>();
	nodeList3.add(n5);
	nodeList3.addLast(n6);
	Cell cc = new Cell();
	cc.nodeList = nodeList3;
	cc.radius = 5;
	cc.predecessor = c2;
	double volume = Math.PI * 5 * 5 * 10;
	double preferredRadius = Math.pow(volume / (Math.PI * 5), 1.0/3.0);
	double preferredLength = preferredRadius * 5;
	double stretchedLength =
	    preferredLength * (1 + maximumElongationFactor);
	dist = cc.extendableDistance();
	System.out.println("expected: " + (stretchedLength - 10) + " actual: "
			   + dist);
	if (dist != stretchedLength - 10) {
	    Environment.die("[testProliferation] c.extendableDistance()="
			    + dist);
	}



	//	c.radius = 2;
	//	double vol = c.computedVolume();
	//	double maxLen = computeLength(vol, minimumCellRadius);
	//	dist = c.extendableDistance();
	//	if (dist != maxLen - length) {
	//	    Environment.die("[testProliferation] c.extendableDistance()="
	//			    + dist + " maxLen=" + maxLen
	//			    + " length=" + length);
	//	}
	
    }
	




    // byron TEST
    private double extendableDistance() {
	// cant extend into a branch
	//	if (predecessor != null && predecessorB != null) {
	if (branchAhead || junctionAhead) {
	    return 0;
	}
	//	if (predecessor == null && predecessorB == null) {
	//	    Environment.die("[Cell.extendableDistance] called for a tip cell "
	//			    + this);
	//	}
	if (baseSphere != null) {
	    return 0;
	}

	//	double maximumLength = computedVolume() / (Math.PI * sqr(minimumRadius));

	double volume = computedVolume();
	double lengthToRadiusRatio = computeLengthToRadiusRatio();
	double proportionedRadius =
	    computeProportionedRadius(volume, lengthToRadiusRatio);
	double proportionedLength =
	    proportionedRadius * lengthToRadiusRatio;
	double maximumLength =
	    proportionedLength * (1 + maximumElongationFactor);

	//	System.out.println("***[Cell.extendableDistance]" + this);
	//	System.out.println("***[Cell.extendableDistance]" + maximumLength
	//			   + " " + computedLength());
	
	



	//	log.println("[Cell.extendableDistance] " + maximumLength
	//		    + "  " + computedLength(),
	//		    LogStreamInterface.BASIC_LOG_DETAIL);


	//	System.out.println("*** [Cell.extendableDistance] Cell " + idNumber
	//			   + " maximum length: " + maximumLength
	//			   + " minimum radius: " + minimumRadius
	//			   + " volume: " + computedVolume());


	/*
	System.out.println("[Cell.extendableDistance] length=" + computedLength()
			   + "   maximumlength=" + maximumLength
			   + "  " + computeLength(computedVolume(), radius)
			   + "   " + radius + "  " + minimumRadius);
	System.out.println();
	System.out.println("[Cell.extendableDistance] recomputed length="
			   + computeLength(computedVolume(), radius));
	System.out.println("[Cell.extendableDistance] node list length="
			   + nodeListLength(nodeList));
	System.out.println();

	double testRadius = 1;
	double testLength = 10;

	double compVolume = computeVolume(testRadius, testLength);

	System.out.println("[Cell.extendableDistance] compVolume=" + compVolume);

	double compLength = computeLength(compVolume, testRadius);

	System.out.println("[Cell.extendableDistance] " + testLength + " " + compLength);
	System.out.println();


	System.out.println();
	*/



	/*
	 * If a cell has recently branched, it radius may have
	 * decreased below its minimum
	 */

	/*
	if (maximumLength < computedLength()) {
	    //	    dist = maximumLength - length;
	    System.out.println("*** " + successor.getPredecessorB());
	    Environment.die("[Cell.extendableDistance] unusual length: " + this);
	}
	*/

	// the cell may have branched reducing its radius below the minimum radius
	double dist = Math.max(0, maximumLength - computedLength());
	
	//	System.out.println("*** [Cell.extendableDistance] Cell " + idNumber
	//			   + " extendable distance: " + dist);

	return dist;

    }



    private double computedVolume() {
	return computeVolume(radius, computedLength());
    }

    private double computedLength() {
	return nodeListLength(nodeList);
    }

    private double nodeListLength(LinkedList<Node> nodeList) {
	double dist = 0;
	Point3D prev = null;
	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	    Node n = i.next();
	    Point3D p = n.getLocation();
	    if (prev != null) {
		dist += p.distance(prev);
	    }
	    prev = p;
	}
	return dist;
    }


    // byron TEST
    private void elongate(LinkedList<Node> nodes, double deltaLength) {
	if (deltaLength == 0) {
	    return;
	}
	if (predecessor != null && predecessorB != null) {
	    Environment.die("[Cell.elongate] Undefined for a branch");
	}	
	if (baseSphere != null) {
	    Environment.die("[Cell.elongate] Spheroid cell does not elongate: " + this);
	}
	double volume = computedVolume();
	double length = computedLength();
	double lengthToRadiusRatio = computeLengthToRadiusRatio();
	double proportionedRadius =
	    computeProportionedRadius(volume, lengthToRadiusRatio);
	double proportionedLength =
	    proportionedRadius * lengthToRadiusRatio;
	double maxLength = proportionedLength * (1 + maximumElongationFactor);
	if (deltaLength > maxLength - length) {
	    Environment.die("[Cell.elongate] attempting to increase length "
			    + deltaLength + " for cell: " + toString());
	}
	addNodesToFront(nodes);
	double newLength = computedLength();
	double newRadius = Math.sqrt(volume / (newLength * Math.PI));
	radius = newRadius;
	changedShape = true;	
    }

	
    //    private void OLDelongate(LinkedList<Node> nodes, double deltaLength) {
    //	System.out.println("[Cell.elongate] nodeListLength(nodes)=" + nodeListLength(nodes)
    //			   + " deltaLength=" + deltaLength);
    //	System.out.println("[Cell.elongate] pre-elongation cell " + idNumber
    //			   + " radius: " + radius + " length: " + computedLength()
    //			   + " volume: " + getVolumeCubicMicrons());
    //	double len = nodeListLength(nodes);
    //	if (Math.abs(len - deltaLength) > .0000001) {
    //	    Environment.die("[Cell.elongate] " + len + " " + deltaLength);
    //	}
    //	if (predecessorB != null) {
    //	    Environment.die("[Cell.elongate] Undefined for a branch");
    //	}	
    //	if (baseSphere != null) {
    //	    Environment.die("[Cell.elongate] Spheroid cell does not elongate: " + this);
    //	}
    //	addNodesToFront(nodes);
    //	double newLength = computedLength() + deltaLength;
    //	double newRadius = Math.sqrt(computedVolume() / (newLength * Math.PI));
    //	if (newRadius + DECIMAL_TOLERANCE < minimumCellRadius) {
    //	    Environment.die("[Cell.elongate] attempting to change radius to "
    //			    + newRadius + " for cell: " + toString());
    //	}
    //	radius = newRadius;
    //	//	length = newLength;
    //	changedShape = true;
    //	System.out.println("[Cell.elongate] post-elongation cell " + idNumber
    //			   + " radius: " + radius + " length: " + computedLength()
    //			   + " volume: " + getVolumeCubicMicrons());
    //    }

	
    public static boolean areCollinear(Point3D p1, Point3D p2, Point3D p3) {
	double deltaX12 = p1.x - p2.x;
	double deltaY12 = p1.y - p2.y;
	double deltaZ12 = p1.z - p2.z;
	double dist12 =
	    Math.sqrt(sqr(deltaX12) + sqr(deltaY12) + sqr(deltaZ12));
	double slopeX12 = deltaX12 / dist12;
	double slopeY12 = deltaY12 / dist12;
	double slopeZ12 = deltaZ12 / dist12;

	double deltaX23 = p2.x - p3.x;
	double deltaY23 = p2.y - p3.y;
	double deltaZ23 = p2.z - p3.z;
	double dist23 =
	    Math.sqrt(sqr(deltaX23) + sqr(deltaY23) + sqr(deltaZ23));
	double slopeX23 = deltaX23 / dist23;
	double slopeY23 = deltaY23 / dist23;
	double slopeZ23 = deltaZ23 / dist23;

	boolean collinear = false;
	// points are collinear if corresponding slopes are equivalent,
	// or corresponding slopes are negations of eachother
	if ((slopeX12 == slopeX23 && slopeY12 == slopeY23
	     && slopeZ12 == slopeZ23)
	    || (slopeX12 == -slopeX23 && slopeY12 == -slopeY23
		&& slopeZ12 == -slopeZ23)) {
	    collinear = true;
	}
	return collinear;
    }

    private static double sqr(double n) {
	return (n * n);
    }



    // byron REWRITE
    public static void testProliferate() {
	Environment e = new Environment(1);
	double minimumCellRadius = 1;
	double maximumCellRadius = 5;
	double maximumCellLength = 20;
	double maximumCellVolume =
	    computeVolume(maximumCellRadius, maximumCellLength);
	double lengthToRadiusRatio = maximumCellLength / maximumCellRadius;
	
	Point3D p1 = new Point3D(10, 0, 0);
	Point3D p2 = new Point3D(0, 0, 0);
	//	Node n1 = new Node(new double[] {p1.x, p1.y, p1.z});
	//	Node n2 = new Node(new double[] {p2.x, p2.y, p2.z});
	Node n1 = new Node(p1);
	Node n2 = new Node(p2);
	LinkedList<Node> nodeList = new LinkedList<Node>();
	nodeList.add(n1);
	nodeList.addLast(n2);
	Cell c = new Cell();
	c.nodeList = nodeList;
	//	c.radius = p1.distance(p2) / lengthToRadiusRatio;
	//	c.radius = 1;
	c.radius = 2.5;
	System.out.println(c);
	c.proliferate(20, e);
	System.out.println(c);
    }
    

    // byron REWRITE
    private static void testProliferate2() {
	Environment e = new Environment(1);

	Cell.minimumLengthToRadiusRatio = 5;
	Cell.maximumLengthToRadiusRatio = 5;
	//	Cell.minimumTipLengthToRadiusRatio = 8;
	//	Cell.maximumTipLengthToRadiusRatio = 8;
	divisionProbConst1 = 0.012617;
	divisionProbConst2 = -0.13378;
	

	Cell.minimumDivisionLength = 20;
	double minimumDivisionRadius = 
	    minimumDivisionLength
	    / ((minimumLengthToRadiusRatio 
		+ maximumLengthToRadiusRatio)
	       / 2.0);

	//	double minimumTipDivisionRadius = 
	//	    minimumDivisionLength
	//	    / ((minimumTipLengthToRadiusRatio 
	//		+ maximumTipLengthToRadiusRatio)
	//	       / 2.0);

	Cell.env = new Environment(1);
	Cell.log = new LogStream("temp2", true, 3);
	
	//	Node n1 = new Node(new double[] {5, 0, 0});
	//	Node n2 = new Node(new double[] {0, 0, 0});
	Node n1 = new Node(new Point3D(5, 0, 0));
	Node n2 = new Node(new Point3D(0, 0, 0));
	LinkedList<Node> nodeList = new LinkedList<Node>();
	nodeList.add(n1);
	nodeList.addLast(n2);
	Cell c = new Cell();
	//	Node n1b = new Node(new double[] {7, 0, 0});
	//	Node n2b = new Node(new double[] {5, 0, 0});
	Node n1b = new Node(new Point3D(7, 0, 0));
	Node n2b = new Node(new Point3D(5, 0, 0));
	LinkedList<Node> nodeListB = new LinkedList<Node>();
	nodeList.add(n1b);
	nodeList.addLast(n2b);
	Cell c2 = new Cell();
	c2.nodeList = nodeListB;
	//	c.predecessor = c2;
	c.nodeList = nodeList;
	c.radius = 1; // minimumDivisionRadius;
	double volume = c.computedVolume();
	double volumeDelta = .1 * volume;
	System.out.println("volume=" + volume + " volumeDelta=" + volumeDelta);
	System.out.println(c);
	c.proliferate(volumeDelta, e);
	System.out.println(c);
    }



    public void resize() {
	resize(computedVolume());
    }

    private void resize(double volume) {
	resize(volume, computeLengthToRadiusRatio());
    }
    
    // byron REWRITE
    private static void testResize() {
	Cell.env = new Environment(1);
	Cell.log = new LogStream("temp2", true, 3);

	//	Node n1 = new Node(new double[] {10, 0, 0});
	//	Node n2 = new Node(new double[] {0, 0, 0});
	Node n1 = new Node(new Point3D(10, 0, 0));
	Node n2 = new Node(new Point3D(0, 0, 0));
	LinkedList<Node> nodeList = new LinkedList<Node>();
	nodeList.add(n1);
	nodeList.addLast(n2);
	Cell c = new Cell();
	c.nodeList = nodeList;

	double minLTR = 4;
	double maxLTR = 5;
	c.radius = 5;
	double volume = c.computedVolume();
	System.out.println(c);
	c.resize(volume, minLTR);
	System.out.println(c);
	double volumeAfterResize = c.computedVolume();
	double tolerance = 0.00000000001;
	if (Math.abs(volume - volumeAfterResize) > tolerance) {
	    Environment.die("[Cell.testResize] volume=" + volume
			    + " volumeAfterResize=" + volumeAfterResize);
	}
	double ltr = c.computedLength() / c.radius;
	if (ltr < minLTR || ltr > maxLTR) {
	    Environment.die("[Cell.testResize] ltr=" + ltr
			    + " minLTR=" + minLTR + " maxLTR=" + maxLTR);
	}

	//	Node n3 = new Node(new double[] {10, 0, 0});
	//	Node n4 = new Node(new double[] {0, 0, 0});
	Node n3 = new Node(new Point3D(10, 0, 0));
	Node n4 = new Node(new Point3D(0, 0, 0));
	LinkedList<Node> nodeList2 = new LinkedList<Node>();
	nodeList2.add(n3);
	nodeList2.addLast(n4);
	Cell c2 = new Cell();
	c2.nodeList = nodeList2;

	minLTR = 4;
	maxLTR = 5;
	c2.radius = 1;
	volume = c2.computedVolume();
	System.out.println(c2);
	c2.resize(volume, minLTR);
	System.out.println(c2);
	volumeAfterResize = c2.computedVolume();
	if (Math.abs(volume - volumeAfterResize) > tolerance) {
	    Environment.die("[Cell.testResize] volume=" + volume
			    + " volumeAfterResize=" + volumeAfterResize);
	}
	if (Math.abs(c2.computedLength() - 10) > tolerance) {
	    Environment.die("[Cell.testResize] c2.computedlength()="
			    + c2.computedLength());
	}
	if (Math.abs(c2.radius - 1) > tolerance) {
	    Environment.die("[Cell.testResize] c2.radius="
			    + c2.radius);
	}
    }


    public static void testResize2() {
	double tolerance = .00000000001;
	log = new LogStream("temp", false, 0);
	LinkedList<Node> nodeList = new LinkedList<Node>();
	Node n1 = new Node(new Point3D(1, 0, 0));
	Node n2 = new Node(new Point3D(0, 0, 0));
	nodeList.add(n1);
	nodeList.addLast(n2);
	Cell c = new Cell();
	c.nodeList = nodeList;
	c.radius = 1;
	double volume = computeVolume(c.radius, c.computedLength());
	c.resize(2 * volume, 1);
	double newVolume = c.computedVolume();
	double newLength = c.computedLength();
	double newRadius = c.radius;
	if (newVolume != 2 * volume) {
	    Environment.die("[Cell.testResize2] new volume is not twice old volume: newVolume="
			    + newVolume + " volume=" + volume);
	}
	if (Math.abs((newLength / newRadius) - 1) > tolerance) {
	    Environment.die("[Cell.testResize2] new length-to-radius ratio is not 1: newlength="
			    + newLength + " newRadius=" + newRadius);
	}
	System.out.println("[Cell.testResize2] Passed");
    }


    private int printPredecessors() {
	System.out.print(idNumber + " ");
	if (predecessor == null) {
	    return 1;
	}
	else {
	    return 1 + predecessor.printPredecessors();
	}
    }

    // byron TEST 

    // Changes cell's length and radius to correspond to volume
    // parameter. Enforce a nonincreasing radius requirement: a cell's
    // radius should not be greater than its successor and should not
    // be less than both of its predecessors.
    private void resize(double volume, double ltr) {
	log.println("[Cell.resize] resizing cell " + idNumber,
		    LogStreamInterface.BASIC_LOG_DETAIL);
	double length = computedLength();
	double goalRadius = computeProportionedRadius(volume, ltr);
	if (predecessor != null) {
	    goalRadius = Math.max(goalRadius, predecessor.radius);
	}
	if (predecessorB != null) {
	    goalRadius = Math.max(goalRadius, predecessorB.radius);
	}
	// By putting the successor radius comparison last, precedence
	// is given to it.
	if (successor != null && successor.baseSphere == null) {
	    goalRadius = Math.min(goalRadius, successor.radius);
	}
	double goalLength = computeLength(volume, goalRadius);

	// Cell length does not decrease, and it does not increase if
	// there is a branch or junction ahead.
	if (length >= goalLength || branchAhead || junctionAhead) {
	    radius = computeRadius(volume, length);
	}
	// Otherwise, change both dimensions to the goal dimensions.
	else {
	    radius = goalRadius;
	    double lengthIncrease = goalLength - length;
	    lengthenAndPushForward(lengthIncrease);
	}
    }
    

    private void resizeOLD(double volume, double ltr) {
	if (debugFlag && idNumber == 2327) {
	    System.out.println("[Cell.resize] idNumber=" + idNumber
			       + " volume=" + volume
			       + " ltr=" + ltr);
	    System.out.println("pred count: " + printPredecessors());
	    Throwable th = new Throwable();
	    th.printStackTrace();
	}
	log.println("[Cell.resize] resizing cell " + idNumber,
		    LogStreamInterface.BASIC_LOG_DETAIL);
	double length = computedLength();
	double goalRadius = computeProportionedRadius(volume, ltr);
	if (predecessor != null) {
	    goalRadius = Math.max(goalRadius, predecessor.radius);
	}
	if (predecessorB != null) {
	    goalRadius = Math.max(goalRadius, predecessorB.radius);
	}
	// By putting the successor radius comparison last, precedence
	// is given to it.
	if (successor != null && successor.baseSphere == null) {
	    goalRadius = Math.min(goalRadius, successor.radius);
	}
	double goalLength = computeLength(volume, goalRadius);


	if (debugFlag && idNumber == 2327) {
	    System.out.println("[Cell.resize] idNumber=" + idNumber
			       + " volume=" + volume
			       + " ltr=" + ltr + " length=" + length
			       + " goalLength=" + goalLength
			       + " branchAhead=" + branchAhead
			       + " predecessor=" + predecessor
			       + " predecessorB=" + predecessorB);
	}

	// Cell length does not decrease, and it does not increase if
	// there is a branch ahead.
	if (length >= goalLength || branchAhead) {
	    radius = computeRadius(volume, length);
	}
	// Otherwise, change both dimensions to the goal dimensions.
	else {
	    // push the front of the cell forward
	    radius = goalRadius;
	    double lengthIncrease = goalLength - length;
	    if (predecessor == null && predecessorB == null) {
		// tip cell grows in its last direction. 
		// remove front most node to be replaced by a new
		// frontmost node
		Node firstNode = nodeList.removeFirst();
		Node secondNode = nodeList.getFirst();
		Point3D firstLocation = firstNode.getLocation();
		Point3D secondLocation = secondNode.getLocation();
		Point3D newFirstLocation =
		    firstLocation.projectAway(secondLocation, lengthIncrease);

		boolean actualChange = true;

		if (firstLocation.equals(newFirstLocation)) {
		    actualChange = false;
		    log.println("[Cell.resize] cell " + idNumber
				+ " length increase " + lengthIncrease
				+ " is too small to effect cell position",
				LogStreamInterface.BASIC_LOG_DETAIL);
		}
		double angle = 0;
		if (actualChange) {
		    // make sure cell travels straight ahead
		    double tolerance = .00001;
		    Point3D straightAhead =
			firstLocation.plus(firstLocation.minus(secondLocation));
		    angle =
			firstLocation.angleDegrees(straightAhead,
						   newFirstLocation);
		    if (Math.abs(angle - 0) > tolerance) {
			actualChange = false;
			log.println("[Cell.resize] cell " + idNumber
				    + " size change is too small to maintain orientation "
				    + angle + " degrees",
				    LogStreamInterface.BASIC_LOG_DETAIL);
		    }
		}
		if (!actualChange) {
		    // add back node removed
		    nodeList.addFirst(firstNode);
		}
		else {
		    Node newFirstNode = new Node(newFirstLocation);
		    nodeList.addFirst(newFirstNode);
		    log.println("[Cell.resize] Cell " + idNumber
				+ " tip cell front extended to "
				+ newFirstLocation + " ("
				+ firstLocation.distance(newFirstLocation)
				+ " microns) resize angle: " + angle
				+ " degrees",
				LogStreamInterface.BASIC_LOG_DETAIL);
		}
	    }
	    else {
		if (false) {
		    System.out.println("[Cell.resize] idNumber=" + idNumber
				       + " lengthIncrease=" + lengthIncrease);
		}
		// proliferating cell has cells ahead of it to push
		log.println("[Cell.resize] Cell " + idNumber
			    + " pushing cell " + predecessor.idNumber
			    + " forward " + lengthIncrease + " microns",
			    LogStreamInterface.BASIC_LOG_DETAIL);
		//		LinkedList<Node> newFrontNodes =
		//		    predecessor.pushForward(lengthIncrease);
		pushForwardOLD(lengthIncrease);
		// add nodes to front and remove any intermediate
		// non-apex nodes
		//		addNodesToFront(newFrontNodes);
	    }
	}
	if (debugFlag && idNumber == 2327) {
	    System.out.println("[Cell.resize] idNumber=" + idNumber
			       + " End");
	}

    }



    // Returns a possibly empty list of new cells created by cell division
    public LinkedList<CellInterface> proliferate(double volumeIncrease,
						 EnvironmentInterface e) {
	LinkedList<CellInterface> newCellList;
	if (volumeIncrease < 0) {
	    Environment.die("[Cell.proliferate] negative volume increase: "
			    + volumeIncrease);
	}
	if (nodeList.size() < 2) {
	    Environment.die("[Cell.proliferate] Add code for initial proliferation");
	}

	Cell newCell;
	double actualVolumeIncrease;
	double newVolume;
	double volume = computedVolume();
	if (branchAhead || junctionAhead) {
	    // The cell length may not be changed.  The radius can be
	    // increased to the larger of: 1) the current radius, 2)
	    // the proportioned radius (according to the current
	    // length and length-to-radius ratio), and 3) The radius
	    // of the cell's immediate predecessors.
	    double length = computedLength();
	    double ltrRatio = computeLengthToRadiusRatio();
	    double proportionedRadius = length / ltrRatio;
	    double predecessorRadius =
		predecessor == null? 0 : predecessor.radius;
	    double predecessorBRadius =
		predecessorB == null? 0 : predecessorB.radius;
	    double maximumRadius =
		Math.max(Math.max(radius, proportionedRadius),
			 Math.max(predecessorRadius, predecessorBRadius));
	    double radiusFromVolumeIncrease =
		computeRadius(volume + volumeIncrease, length);
	    radius = Math.min(maximumRadius, radiusFromVolumeIncrease);
	    newVolume = computeVolume(radius, length);
	    actualVolumeIncrease = newVolume - volume;
	    if (actualVolumeIncrease > 0) {
		changedShape = true;
	    }
	    // log this volume increase before resizing
	    log.println("[Cell.proliferate] Cell " + idNumber
			+ " requested volume increase:" + volumeIncrease
			+ " actual volume increase: " + actualVolumeIncrease,
			LogStreamInterface.BASIC_LOG_DETAIL);
	    // Do not resize because the length must not change
	    // Do not allow cell division
	    newCellList = new LinkedList<CellInterface>();
	}
	else {
	    newVolume = volume + volumeIncrease;
	    actualVolumeIncrease = volumeIncrease;
	    // log this volume increase before resizing
	    log.println("[Cell.proliferate] Cell " + idNumber
			+ " requested volume increase:" + volumeIncrease
			+ " actual volume increase: " + actualVolumeIncrease,
			LogStreamInterface.BASIC_LOG_DETAIL);
	    resize(newVolume);
	    changedShape = true;
	    // cell resizing is done before division check!  Resizing
	    // after mitosis is done in the following time step.
	    newCellList = divisionCheck(newVolume, e);
	}
	return newCellList;
    }


    //    // returns new cell (if any) created by cell division or null
    //    public Cell proliferateOLD(double volumeIncrease,
    //			    EnvironmentInterface e) {
    //	if (volumeIncrease < 0) {
    //	    Environment.die("[Cell.proliferate] negative volume increase: "
    //			    + volumeIncrease);
    //	}
    //	log.println("[Cell.proliferate] Cell " + idNumber
    //		    + " increasing volume " + volumeIncrease
    //		    + " cubic microns (radius=" + radius + ")",
    //		    LogStreamInterface.BASIC_LOG_DETAIL);
    //	if (branchAhead) {
    //	    Environment.die("[Cell.proliferate] Undefined for a branch");
    //	}
    //
    //	//	System.out.println("[Cell.proliferate] " + volumeIncrease + " " + this);
    //
    //	if (nodeList.size() < 2) {
    //	    Environment.die("[Cell.proliferate] Add code for initial proliferation");
    //	}
    //	double volume = computedVolume();
    //	double newVolume = volume + volumeIncrease;
    //	resize(newVolume);
    //	changedShape = true;
    //
    //
    //	log.println("[Cell.proliferate] Cell " + idNumber
    //		    + " volume: " + volume + " increased to: "
    //		    + newVolume + "(r=" + radius + ",l=" + computedLength()
    //		    + ",l/r=" + (computedLength() / radius),
    //		    LogStreamInterface.BASIC_LOG_DETAIL);
    //	
    //	if (branchAhead) {
    //	    Environment.die("[Cell.proliferate] "
    //			    + "Cannot push a branch forward from cell "
    //			    + this);
    //	}
    //	
    //	// cell resizing is done before division check!  Resizing
    //	// after mitosis is done in the following time step.
    //	Cell newCell = divisionCheck(newVolume, e);
    //	return newCell;
    //	//	if (newVolume > maximumCellVolume) {
    //	//	    divide(e);
    //	//	}
    //    }



    // byron REWRITE
    private static void testDivisionCheck() {
	Environment e = new Environment(1);
	Cell.env = e;
	Cell.log = new LogStream("temp2", true, 3);
	
	Cell.minimumDivisionLength = 1;

	Cell.minimumLengthToRadiusRatio = 5;
	Cell.maximumLengthToRadiusRatio = 5;

	divisionProbConst1 = .1;
	divisionProbConst2 = 0;

	// the smallest cell length that results in a 1.0 probability
	// of division is 10 since (10 * divisionProbConst1) +
	// divisionprobConst2 = 1.  The length-to-radius ratio is 5,
	// so the cell should also have a radius of 2.  Thus a cell
	// with volume 10 * pi * 2^2 will divide with probability 1.
	// Creating a cell witrh double this volume should result in 4
	// cells (3 new + 1 original)

	Node n1 = new Node(new Point3D(20, 0, 0));
	Node n2 = new Node(new Point3D(0, 0, 0));
	LinkedList<Node> nodeList = new LinkedList<Node>();
	nodeList.add(n1);
	nodeList.addLast(n2);
	Cell c = new Cell();
	c.nodeList = nodeList;
	c.radius = 2;

	LinkedList newCellList = c.divisionCheck(c.computedVolume(), e);
	
	if (newCellList.size() != 3) {
	    Environment.die("[Cell.testDivisionCheck] Expected 3 new cells, but "
			    + newCellList.size() + " new cells were created");
	}

	System.out.println("[Cell.testDivisionCheck] passed");
    }



    // byron TEST
    // Returns possibly empty linked list of newly created cells.
    private LinkedList<CellInterface> divisionCheck(double volume,
						    EnvironmentInterface e) {
	LinkedList<CellInterface> newCellList =
	    new LinkedList<CellInterface>();
       	double ltr = computeLengthToRadiusRatio();
	double proportionedRadius =
	    computeProportionedRadius(volume, ltr);
	double proportionedLength = ltr * proportionedRadius;
	if (proportionedLength >= minimumDivisionLength) {
	    Cell newCell;
	    double prob =
		(proportionedLength * divisionProbConst1) + divisionProbConst2;
	    double rand = env.random.nextDouble();
	    if (rand < prob) {
		newCell = divide(e);
		newCellList.add(newCell);
		LinkedList<Cell> checkCellList = new LinkedList<Cell>();
		checkCellList.add(this);
		checkCellList.add(newCell);
		int cellsToCheck = 2;
		double forcedDivisionLength = 
		    (1 - divisionProbConst2) / divisionProbConst1; 
		while (cellsToCheck > 0) {
		    Cell c = checkCellList.removeFirst();
		    cellsToCheck--;
		    if (c.computedLength() >= forcedDivisionLength) {
			newCell = c.divide(e);
			newCellList.add(newCell);
			checkCellList.add(c);
			checkCellList.add(newCell);
			cellsToCheck += 2;
		    }
		}
	    }
	}
	return newCellList;
    }

    
    private static double distance(Point3D p1, Point3D p2) {
	double deltaX = p1.x - p2.x;
	double deltaY = p1.y - p2.y;
	double deltaZ = p1.z - p2.z;
	double d = Math.sqrt(sqr(deltaX) + sqr(deltaY) + sqr(deltaZ));
	return d;
    }


    // Returns a point p3 that: 1) is on the same line as that defined
    // by p1 and p2; and 2) is dist away from p2 in the p1-to-p2
    // direction.
    private static Point3D project(Point3D p1, Point3D p2, double dist) {
	double deltaX = p2.x - p1.x;
	double deltaY = p2.y - p1.y;
	double deltaZ = p2.z - p1.z;
	double d = Math.sqrt(sqr(deltaX) + sqr(deltaY) + sqr(deltaZ));
	double slopeX = deltaX / d;
	double slopeY = deltaY / d;
	double slopeZ = deltaZ / d;
	// determine coordinates of p3
	double x3 = p2.x + (slopeX * dist);
	double y3 = p2.y + (slopeY * dist);
	double z3 = p2.z + (slopeZ * dist);
	return new Point3D(x3, y3, z3);
    }
    


    private static void testLengthenAndPushForward() {
	Cell c1 = new Cell();
	Node n1a = new Node(new Point3D(2, 0, 0));
	Node n1b = new Node(new Point3D(1, 0, 0));
	LinkedList<Node> nodeList1 = new LinkedList<Node>();
	nodeList1.add(n1a);
	nodeList1.add(n1b);
	c1.nodeList = nodeList1;

	Cell c2 = new Cell();
	Node n2a = new Node(new Point3D(1, 0, 0));
	Node n2b = new Node(new Point3D(0, 0, 0));
	LinkedList<Node> nodeList2 = new LinkedList<Node>();
	nodeList2.add(n2a);
	nodeList2.add(n2b);
	c2.nodeList = nodeList2;

	c1.successor = c2;
	c2.predecessor = c1;

	c2.lengthenAndPushForward(1);

	if (c1.nodeList.size() != 2) {
	    Environment.die("[Cell.testLengthenAndPushForward] c1 has "
			    + c1.nodeList.size() + " nodes");
	}
	if (c2.nodeList.size() != 2) {
	    Environment.die("[Cell.testLengthenAndPushForward] c2 has "
			    + c2.nodeList.size() + " nodes");
	}

	Point3D p1a = new Point3D(3, 0, 0);
	Point3D p1b = new Point3D(2, 0, 0);
	Point3D p2a = new Point3D(2, 0, 0);
	Point3D p2b = new Point3D(0, 0, 0);
	if (! p2b.equals(c2.nodeList.get(1).getLocation())) {
	    Environment.die("[Cell.testLengthenAndPushForward] Expected "
			    + p2b + " (p2b)  found "
			    + c2.nodeList.get(1).getLocation());
	}
	if (! p2a.equals(c2.nodeList.get(0).getLocation())) {
	    Environment.die("[Cell.testLengthenAndPushForward] Expected "
			    + p2a + " (p2a)  found "
			    + c1.nodeList.get(0).getLocation());
	}
	if (! p1b.equals(c1.nodeList.get(1).getLocation())) {
	    Environment.die("[Cell.testLengthenAndPushForward] Expected "
			    + p1b + " (p1b)  found "
			    + c1.nodeList.get(1).getLocation());
	}
	if (! p1a.equals(c1.nodeList.get(0).getLocation())) {
	    Environment.die("[Cell.testLengthenAndPushForward] Expected "
			    + p1a + " (p1a)  found "
			    + c1.nodeList.get(0).getLocation());
	}

	System.out.println("[Cell.testLengthenAndPushForward] passed");
	
    }

    // The cell's length is increased by lengthIncrease and all cells
    // ahead of it are pushed forward by the same distance.  Rewritten
    // to eliminate recursion.
    private void lengthenAndPushForward(double lengthIncrease) {
	if (lengthIncrease < 0) {
	    Environment.die("[Cell.lengthenAndPushForward] negative length increase: " + lengthIncrease);
	}

	Cell c = this;
	// Find tip cell
	while (c.predecessor != null || c.predecessorB != null) {
	    if (c.predecessor != null) {
		if (c.predecessorB != null) {
		    Environment.die("[Cell.lengthenAndPushForward] Undefined for a branch at cell "
				    + idNumber);
		}
		else {
		    c = c.predecessor;
		}
	    }
	    else {
		c = c.predecessorB;
	    }
	}

	boolean lengthIncreaseSignificant = true;

	// Extend front of tip cell
	Cell tip = c;
	Node frontNode = tip.nodeList.getFirst();
	Node nextNode = tip.nodeList.get(1);
	Point3D p1 = frontNode.getLocation();
	Point3D p2 = nextNode.getLocation();
	Point3D p3 = p1.projectAway(p2, lengthIncrease);
	if (p3.equals(p1)) {
	    // Length increase is too small to make a difference
	    lengthIncreaseSignificant = false;
	}
	else {
	    // make sure cell travels straight ahead
	    double tolerance = .00001;
	    Point3D straightAhead = p1.plus(p1.minus(p2));
	    double angle = p1.angleDegrees(straightAhead, p3);
	    if (Math.abs(angle - 0) > tolerance) {
		if (lengthIncrease < tolerance) {
		    // lengthIncrease is so small that p3 is distorted
		    lengthIncreaseSignificant = false;
		}
		else {
		    Environment.die("[Cell.lengthenAndPushForward] tip cell " + tip.idNumber
				    + " was pushed forward at angle " + angle
				    + "  lengthIncrease=" + lengthIncrease);
		}
	    }
	}

	if (lengthIncreaseSignificant) {
	    Node newFrontNode = new Node(p3);
	    // since p1, p2, and p3 are collinear, the presence of p3
	    // makes the front node unnecessary
	    tip.nodeList.removeFirst();
	    tip.nodeList.addFirst(newFrontNode);
	    // Starting with the tip cell, transfer nodes from a cell
	    // to the one following it.  The rear nodes of a cell
	    // become the new front nodes of the cell behind it.
	    c = tip;
	    while (c != this) {
		LinkedList<Node> transferNodes =
		    c.removeRear(lengthIncrease);
		c.successor.addNodesToFront(transferNodes);
		c = c.successor;
	    }
	}
    }


    private LinkedList<Node> pushForwardOLD(double lengthIncrease) {
	if (debugFlag && idNumber == 2327) {
	    //	    debugFlag2 = true;
	    System.out.println("[Cell.pushForward] idNumber=" + idNumber
			       + " lengthIncrease=" + lengthIncrease
			       + " predecessor=" + predecessor);
	}
	if (lengthIncrease < 0) {
	    Environment.die("[Cell.pushForward] negative length increase: " + lengthIncrease);
	}
	if (predecessorB != null) {
	    Environment.die("[Cell.pushForward] Undefined for a branch at cell "
			    + idNumber);
	}

	// determine cells new front node(s)
	if (predecessor == null) {
	    // tip cells must determine new frontmost node
	    Node frontNode = nodeList.getFirst();
	    Node nextNode = nodeList.get(1);
	    Point3D p1 = frontNode.getLocation();
	    Point3D p2 = nextNode.getLocation();
	    Point3D p3 = project(p2, p1, lengthIncrease);
	    // since p1, p2, and p3 are collinear, the presence of p3
	    // makes the front node unnecessary
	    //	    Node newFrontNode = new Node(new double[] {p3.x, p3.y, p3.z, 1});

	    log.println("[Cell.pushForward] cell " + idNumber
			+ " frontPoint=" + p1 + " secondPoint=" + p2
			+ " newFrontPoint=" + p3,
			LogStreamInterface.BASIC_LOG_DETAIL);


	    if (p3.equals(p1)) {
		// Length increase is too small to make a difference
		log.println("[Cell.pushForward] Length increase is too small to change tip cell "
			    + idNumber,
			    LogStreamInterface.BASIC_LOG_DETAIL);
		return new LinkedList<Node>();
	    }

	    Node newFrontNode = new Node(p3);

	    // make sure cell travels straight ahead
	    double tolerance = .00001;
	    Point3D straightAhead = p1.plus(p1.minus(p2));
	    double angle = p1.angleDegrees(straightAhead, p3);
	    if (Math.abs(angle - 0) > tolerance) {
		if (lengthIncrease < tolerance) {
		    // lengthIncrease is so small that p3 is distorted
		    log.println("[Cell.pushForward] Length increase is small enough to distort front of cell "
				+ idNumber,
				LogStreamInterface.BASIC_LOG_DETAIL);
		    return new LinkedList<Node>();
		}
		else {
		    Environment.die("[Cell.pushForward] tip cell " + idNumber
				    + " was pushed forward at angle " + angle
				    + "  lengthIncrease=" + lengthIncrease);
		}
	    }
	    
	    //	    Node newFrontNode = new Node(NodeInterface.ShapeType.SPHERE,      //&&
	    //					 new double[] {p3.x, p3.y, p3.z, 1});
	    nodeList.removeFirst();
	    nodeList.addFirst(newFrontNode);
	    log.println("[Cell.pushForward] Tip cell " + idNumber
			+ " front extended to " + p3
			+ " (" + p3.distance(p1) + " microns) angle: "
			+ angle + " degrees",
			LogStreamInterface.BASIC_LOG_DETAIL);
	    
	}
	else {
	    log.println("[Cell.pushForward] Cell " + idNumber
			+ " pushing cell " + predecessor.idNumber
			+ " forward " + lengthIncrease + " microns",
			LogStreamInterface.BASIC_LOG_DETAIL);

	    // cell is not a tip cell so
	    LinkedList<Node> frontNodes =
		predecessor.pushForwardOLD(lengthIncrease);
	    addNodesToFront(frontNodes);
	}
	changedShape = true;
	if (debugFlag && idNumber == 2327) {
	    //	    debugFlag2 = true;
	    System.out.println("[Cell.pushForward] idNumber=" + idNumber
			       + " End");
	}
	return removeRear(lengthIncrease);
    }





    // nodes argument has frontmost node at rear, as returned by removeRear
    private void addNodesToFront(LinkedList<Node> nodes) {
	if (predecessorB != null) {
	    Environment.die("[Cell.addNodesToFront] Undefined for a branch");
	}

	if (nodes.size() == 0) {
	    return;
	}
	Node frontNode = nodeList.getFirst();
	// Since there will be a new frontmost node, the current
	// frontmost node is discarded unless it is a unique
	// inflection
	if (!frontNode.isInflection()
	    || frontNode.getLocation().equals(nodes.getFirst().getLocation())) {
	    nodeList.removeFirst();
	}
	for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
	    Node n = i.next();
	    // add only those nodes that are inflections or the node
	    // that is the new frontmost node are added
	    if (n.isInflection() || !i.hasNext()) {
		nodeList.addFirst(n);
	    }
	}
	changedShape = true;
    }



    // remove and return rear nodes of cell.  Returned list has the
    // cell's frontmost removed node at the end and rearmost
    // removeded node at the front.
    private LinkedList<Node> removeRear(double removalLength) {
	changedShape = true;
	return removeRear(removalLength, true);
    }


    // remove and return rear nodes of cell.  Returned list has the
    // cell's frontmost removed node at the end and rearmost
    // removed node at the front if addAsLast is true.  Otherwise,
    // the frontmost removed node is at the front of the returned
    // list.
    private LinkedList<Node> removeRear(double removalLength,
					boolean addAsLast) {

	/*
	System.out.println("[Cell.removeRear] " + removalLength
			   + "  " + nodeListLength(nodeList)
			   + "  " + this);
	System.out.println();
	*/

	LinkedList<Node> removedNodes = new LinkedList<Node>();
	if (removalLength < 0) {
	    Environment.die("[Cell.removeRear] removalLength="
			    + removalLength);
	}
	if (removalLength > 0) {
	    Node lastNode = nodeList.removeLast();
	    removedNodes.add(lastNode);
	    double lengthRemaining = removalLength;
	    while (lengthRemaining > 0) {
		Node nextToLastNode = nodeList.getLast();
		double dist = distance(lastNode.getLocation(),
				       nextToLastNode.getLocation());
		//		System.out.println("    [Cell.removeRear] " + lengthRemaining
		//				   + " " + dist + " " + nodeList.size());
		if (lengthRemaining > dist) {
		    // remove nextToLastNode
		    nodeList.removeLast();
		    if (addAsLast) {
			removedNodes.addLast(nextToLastNode);
		    }
		    else {
			removedNodes.addFirst(nextToLastNode);
		    }
		    lastNode = nextToLastNode;
		    lengthRemaining = lengthRemaining - dist;
		}
		else {
		    if (lengthRemaining == dist) {
			// nextToLastNode remains in nodeList, but is also
			// placed in the removed list
			if (addAsLast) {
			    removedNodes.addLast(nextToLastNode);
			}
			else {
			    removedNodes.addFirst(nextToLastNode);
			}
		    }
		    else {
			// create a new node for both lists
			Point3D p = project(lastNode.getLocation(),
					    nextToLastNode.getLocation(),
					    lengthRemaining - dist);
			//			Node terminus = new Node(new double[] {p.x, p.y, p.z, 1});
			Node terminus = new Node(p);
			//			Node terminus = new Node(NodeInterface.ShapeType.SPHERE,   //&&
			//						 new double[] {p.x, p.y, p.z, 1});
			nodeList.addLast(terminus);
			if (addAsLast) {
			    removedNodes.addLast(terminus);
			}
			else {
			    removedNodes.addFirst(terminus);
			}
		    }
		    lengthRemaining = 0;
		}
	    }
	}
	changedShape = true;
	return removedNodes;
    }
	

    // returns new cell
    public Cell divide(EnvironmentInterface e) {
	log.println("[Cell.divide] Starting division of cell " + idNumber,
		    LogStreamInterface.BASIC_LOG_DETAIL);
	//	System.out.println("[Cell.divide] Starting division of cell "
	//			   + idNumber);

	//	System.out.println("[Cell.divide] start");
	Environment env = (Environment) e;
	double newLength = computedLength() / 2.0;
	// remove rear nodes from nodeList
	// nodes are in order from front to rear

	LinkedList<Node> rearNodes = removeRear(newLength, false);

	if (!rearNodes.getFirst().getLocation().equals(nodeList.getLast().getLocation())) {
	    Environment.die("[Cell.divide] Mismatched node lists. First rear="
			    + rearNodes.getFirst() + "   last front="
			    + nodeList.getFirst());
	}

	//	System.out.println("[Cell.divide] finished removeRear");

	//	Cell c = new Cell(rearNodes, radius, minimumRadius, maximumRadius);
	Cell c = new Cell(rearNodes, radius);
	c.onBranch = this.onBranch;
	c.onLimitedXYBranch = this.onLimitedXYBranch;

	//	if (getOrigin().idNumber == 192) {
	//	    System.out.println("[Cell.divide] At time-step " + env.getCurrentStepNumber()
	//			       + " cell " + idNumber + " divides to form cell " + c.getIdNumber());
	//}


	//	System.out.println("[Cell.divide] finished cell creation");


	//	length = newLength;
	//	volume = volume / 2.0;
	c.setPredecessor(this);
	c.setSuccessor(this.successor);
	if (successor != null) {
	    if (successor.getPredecessor() == this) {
		successor.setPredecessor(c);
	    }
	    else {
		if (successor.getPredecessorB() == this) {
		    successor.setPredecessorB(c);
		}
		else {
		    Environment.die("[Cell.divide] Mismatched successor/predecessor: "
				    + successor + "   " + this);
		}
	    }
	}
	setSuccessor(c);
	    
	//	System.out.println("[Cell.divide] registering cell");

	// register after cell's predecessors set
	env.registerCell(c);
	//	System.out.println("[Cell.divide] created cell: " + c);
	changedShape = true;

	divisionCount++;


	log.println("[Cell.divide] created " + c.getCellPosition() + " cell "
		    + c.getIdNumber() + " from cell "
		    + getIdNumber() + " " + getCellPosition(),
		    LogStreamInterface.BASIC_LOG_DETAIL);
	return c;
    }

    public void setSuccessor(Cell successor) {
	if (baseSphere != null) {
	    Environment.die("[Cell.setSuccessor] Spheroid cell cannot have a successor: "
			    + this);
	}
	this.successor = successor;
    }

    public void setPredecessor(Cell predecessor) {
	this.predecessor = predecessor;
    }

    public void setPredecessorB(Cell predecessorB) {
	this.predecessorB = predecessorB;
    }

    public Cell getSuccessor() {
	return successor;
    }


    public Cell getPredecessor() {
	return predecessor;
    }

    public Cell getPredecessorB() {
	return predecessorB;
    }


    public String toString() {
	String nodeString = "";
	boolean first = true;
	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	    Node n = i.next();
	    if (first) {
		first = false;
		nodeString += n.toString();
	    }
	    else {
		nodeString += "," + n.toString();
	    }
	}
	String spheroidCellNeighborString = "[";
	first = true;
	for (Iterator<Cell> i = spheroidCellNeighbors.iterator(); i.hasNext();) {
	    Cell c = i.next();
	    if (first) {
		first = false;
		spheroidCellNeighborString += c.getIdNumber();
	    }
	    else {
		spheroidCellNeighborString += "," + c.getIdNumber();
	    }
	}
	spheroidCellNeighborString += "]";

	String pred = null;
	if (predecessor != null) {
	    pred = "" + predecessor.getIdNumber();
	}
	String predB = null;
	if (predecessorB != null) {
	    predB = "" + predecessorB.getIdNumber();
	}
	String succ = null;
	if (successor != null) {
	    succ = "" + successor.getIdNumber();
	}
	String s =
	    "Cell[idNumber=" + idNumber
	    + ",state=" + state
	    + ",baseSphere=" + baseSphere
	    + ",spheroidCellNeighbors=" + spheroidCellNeighborString
	    //	    + ",role=" + role
	    + ",length=" + computedLength()
	    + ",radius=" + radius
	    + ",localStorage=" + localStorage
	    + ",initialRadius=" + initialRadius
	    //	    + ",minimumRadius=" + minimumRadius
	    //	    + ",maximumRadius=" + maximumRadius
	    + ",volume=" + computedVolume()
	    //	    + ",maximumVolume=" + maximumVolume
	    + ",predecessor=" + pred
	    + ",predecessorB=" + predB
	    + ",successor=" + succ
	    + ",inhibited=" + inhibited
	    + ",branchAhead=" + branchAhead
	    + ",junctionAhead=" + junctionAhead
	    + ",dead=" + dead
	    + ",nodeList=[" + nodeString + "]"
	    +"]";
	return s;
    }


    public void setLocalStorage(Object localStorage) {
	if (this.localStorage != null) {
	    Environment.die("[Cell.setLocalStorage] local storage already set");
	}
	this.localStorage = localStorage;
    }

    public Object getLocalStorage() {
	return localStorage;
    }

    public static void setRuleSet(RuleSetInterface ruleSet) {
	//	if (Cell.ruleSet != null) {
	//	    Environment.die("[Cell.setRuleSet] rule set already set");
	//	}
       	Cell.ruleSet = ruleSet;
    }

    public static void setConcentrationsManager(ConcentrationsInterface concentrationsManager) {
	//	if (Cell.concentrationsManager != null) {
	//	    Environment.die("[Cell.setConcentrationsManager] manager already set");
	//	}
	Cell.concentrationsManager = concentrationsManager;
    }
    


    private void redrawRepresentation() {
	LinkedList<Int3D> newGridRepresentation =
	    createGridRepresentation();
	removeFromGrid(gridRepresentation);
	addToGrid(newGridRepresentation);
	gridRepresentation = newGridRepresentation;
    }

    /*
     * 
     */
    public void step(SimState state) {
	//	if (idNumber == 324) {
	//	    log.println("[Cell.step] cell " + idNumber + " has "
	//			+ nodeList.size() + " nodes",
	//			LogStreamInterface.BASIC_LOG_DETAIL);
	//	}
	if (tipCellPrecedence) {
	    if (nonTipCellPreviouslyProcessed && isTipCell()) {
		Environment.die("[Cell.step] Non-tip cell processed before this tip cell " + this);
	    }
	    nonTipCellPreviouslyProcessed = !isTipCell();
	}
	lastSteppedCell = idNumber;
	if (dead) {
	    Environment.die("[Cell.step] " + this + " is dead");
	}
	Environment env = (Environment) state;
	/*
	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	    Node n = i.next();
	    n.activate(env);
	}
	*/
	ruleSet.actCell(this, localStorage, env);
	if (changedShape && !ignoreDiscretizedSprouts) {
	    redrawRepresentation();
	}
	changedShape = false;
	
	//	System.out.println("[Cell.step] Stepped cell " + this + "  " + state.schedule.getSteps());

	// If necessary schedule tip cells as a special case
	boolean valid;
	if (tipCellPrecedence && isTipCell()) {
	    //	    System.out.println("SCHEDULING " + tipCellPrecedence + " " + isTipCell());
	    valid = env.schedule.scheduleOnce(this, env.TIP_CELL_ORDER);
	}
	else {
	    //	    System.out.println("SCHEDULING " + tipCellPrecedence + " " + isTipCell());
	    valid = env.schedule.scheduleOnce(this, env.CELL_ORDER);
	}
	if (!valid) {
	    Environment.die("[Cell.act]  Unable to schedule cell " + this);
	}

    }


    private void addToGrid(LinkedList<Int3D> rep) {
	for (Iterator<Int3D> i = rep.iterator(); i.hasNext();) {
	    Int3D p = i.next();
	    grid.add(p.x, p.y, p.z, this);
	}
    }

    
    private void removeFromGrid(LinkedList<Int3D> rep) {
	for (Iterator<Int3D> i = rep.iterator(); i.hasNext();) {
	    Int3D p = i.next();
	    grid.remove(p.x, p.y, p.z, this);
	}
    }

    


    private LinkedList<Int3D> createGridRepresentation() {
	//	System.out.println("[Cell.createGridRepresentation] start");

	LinkedList<Int3D> rep = new LinkedList<Int3D>();
	if (baseSphere != null) {
	    baseSphere.createRepresentation(grid, rep);
	}

	if (ignoreDiscretizedSprouts) {
	    return rep;
	}

	//	System.out.println("[Cell.createGridRepresentation] starting node list iteratiuon");

	Point3D prev = null;
	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	    Node n = i.next();

	    //	    System.out.println("[Cell.createGridRepresentation] examing node " + n);

	    Point3D p = n.getLocation();
	    if (prev != null) {

		//		System.out.println("[Cell.createGridRepresentation] starting capsule rep");


		spheroid.Sphere.createCylinderRepresentation(grid, p, prev,
							     radius, rep);

		//		System.out.println("[Cell.createGridRepresentation] finished capsule rep");

	    }
	    prev = p;
	}
	return rep;
    }


    /*
    public void addRepresentation(SimpleGrid grid) {
	if (baseSphere != null) {
	    baseSphere.addRepresentation(grid, this);
	}
	addSproutRepresentation(grid);
    }

    private void addSproutRepresentation(SimpleGrid grid) {
	Point3D prev = null;
	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	    Node n = i.next();
	    Point3D p = n.getLocation();
	    if (prev != null) {
		spheroid.Sphere.addCapsuleRepresentation(grid, this,
							 p, prev, radius);
	    }
	    prev = p;
	}
    }
    */

    //       
    //    public LinkedList<Int3D> createRepresentation(Environment env) {
    //	System.err.println("[Cell.createRepresentation] test this!!!");
    //	LinkedList<Int3D> rep = new LinkedList<Int3D> ();
    //	if (baseSphere != null) {
    ////	    baseSphere.createRepresentation(env.grid, this);
    //	}
    //	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
    //	    Node n = i.next();
    //	    LinkedList<Int3D> voxelLocations = n.createRepresentation(env);
    //	    rep.addAll(voxelLocations);
    //	}
    //	    
    //	return rep;
    //    }

    public void setStopObject(Stoppable stopObject) {
	if (this.stopObject != null) {
	    Environment.die("[Cell.setStopObject] stop object already set");
	}
	this.stopObject = stopObject;
    }

    /*
    private void removeFromGrid(LinkedList<Int3D> voxels, Environment env) {
	for (Iterator<Int3D> i = voxels.iterator(); i.hasNext();) {
	    Int3D v = i.next();
	    env.grid.removeAll(v.x, v.y, v.z, this);
	}
    }
    */

    private void addToGrid(LinkedList<Int3D> voxels, Environment env) {
	for (Iterator<Int3D> i = voxels.iterator(); i.hasNext();) {
	    Int3D v = i.next();
	    env.grid.add(v.x, v.y, v.z, this);
	}
    }


    public void remove(EnvironmentInterface e) {
	Environment env = (Environment) e;
	if (stopObject == null) {
	    Environment.die("[Cell.remove] No stop object present");
	}
	stopObject.stop();
	//	removeFromGrid(createRepresentation(env), env);
	dead = true;
	//	env.log.println("[Cell.remove] Cell " + this + " removed.");
    }

    
    public double getAvgNgPerMl(EnvironmentInterface.ConcentrationType concType) {
    	double sum = 0.0;
    	double count = 0.0;
    	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
    	    Node n = i.next();
    	    Point3D p3d = n.getLocation();
	    double conc = concentrationsManager.getNgPerMl(concType,
							   p3d.x, p3d.y, p3d.z);
	    if (conc == 0) {
		//		System.out.println("***[cell.getAvgNgPerMl] cell " + getIdNumber()
		//				   + " Node location" + p3d);
		//		Point3D gridP = grid.translateToGrid(p3d);
		//		int gridX = (int) Math.round(gridP.x);
		//		int gridY = (int) Math.round(gridP.y);
		//		int gridZ = (int) Math.round(gridP.z);
		//		for (int x = gridX - 1; x <= gridX + 1; x++) {
		//		    for (int y = gridY - 1; y <= gridY + 1; y++) {
		//			for (int z = gridZ - 1; z <= gridZ + 1; z++) {
		//			    grid.printOccupyingCells(x, y, z);
		//			}
		//		    }
		//		}
		log.println("Cell " + getIdNumber() + " has no " + concType + " at " + p3d,
			    LogStreamInterface.HIGH_LOG_DETAIL);
		//		System.out.println(this);
		//		System.out.println(predecessor);
		//		Environment.die("");
	    }
	    sum += conc;
    	    count ++;
    	}
    	//	System.out.println("[Cell.getAvgNgPerMl] sum=" + sum + " count=" + count);
    	double avg = sum / count;
    	return avg;
    }
    

    public static void testGetGradient() {
	//	Node n1 = new Node(new double[] {0, 0, 0});
	//	Node n2 = new Node(new double[] {0, 0, -1});
	Node n1 = new Node(new Point3D(0, 0, 0));
	Node n2 = new Node(new Point3D(0, 0, -1));
	Cell c = new Cell(n1, n2, 1);

	Point3D gradient = c.getGradient(EnvironmentInterface.ConcentrationType.VEGF,
					 Math.PI / 2);
	System.out.println("[Cell.testGradient] gradient=" + gradient);
    }


    public Point3D getGradient(EnvironmentInterface.ConcentrationType ct,
			       double maximumVarianceAngleRadians) {
	Point3D p0 = nodeList.getFirst().getLocation();
	Point3D projection = p0.plus(getFrontOrientation());

	//	System.out.println("[Cell.getGradient] p0=" + p0 + "  projection=" + projection);

	/*
	  old code used p when segments were capsules
	Point3D p1 = nodeList.get(1).getLocation();
	Point3D p = p0.projectAway(p1, radius);
	*/

	//	if (p0.magnitude() < 60) {
	//	    Environment.die("[Cell.getGradient] distance=" + p0.magnitude());
	//	}

	Point3D gradient =
	    concentrationsManager.getGradient(ct, p0, projection, maximumVarianceAngleRadians);
	
	//	if (gradient == null) {
	//	    Environment.die("[Cell.getGradient] Null gradient");
	//	}

	return gradient;
    }

    public static Point3D getProjectedTip(Cell c) {
	Point3D p0 = c.nodeList.getFirst().getLocation();
	Point3D p1 = c.nodeList.get(1).getLocation();
	Point3D p = p0.projectAway(p1, c.radius);
	return p;
    }
	
    public LinkedList<Point3D> getNodeLocations() {
	LinkedList<Point3D> pointList = new LinkedList<Point3D>();
	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	    Node n = i.next();
	    Point3D p = n.getLocation().copy();
	    pointList.add(p);
	}
	return pointList;
    }


    /*
    public Point3D getConcentrationVector(EnvironmentInterface.ConcentrationType ct) {
	Node frontNode = nodeList.getFirst();
	Point3D frontLocation = frontNode.getLocation();
	Point3D concentrationVector =
	    concentrationsManager.getConcentrationVector(ct, frontLocation);
	return concentrationVector;
    }
    */



    //    private void propagateInhibition(int range) {
    //	if (! ignoreInhibition) {
    //	    inhibited = true;
    //	    inhibitNeighbors(range);
    //	}
    //    }
	    
    /* 
     * The ignoreInhibition flag prevents an infinite loop of
     * neighbors inhibiting each other.
     */
    //    private void inhibitNeighbors(int range) {
    //	ignoreInhibition = true;
    //	if (range >= 1) {
    //	    for (Iterator<Cell> i = spheroidCellNeighbors.iterator();
    //		 i.hasNext();) {
    //		Cell c = i.next();
    //		c.propagateInhibition(range - 1);
    //	    }
    //	}
    //	ignoreInhibition = false;
    //    }

    public boolean isInhibited() {
	return inhibited;
    }


    // Data structure used by inhibitNeighborsBFS
    public class CellDistanceObject {
	public Cell cell;
	public int distance;
	
	public CellDistanceObject(Cell cell, int distance) {
	    this.cell = cell;
	    this.distance = distance;
	}
    }


    // Inhibits neighbors found via breadth first search to a distance
    // of the range argument.
    private void inhibitNeighborsBFS(int range) {
	LinkedList<CellDistanceObject> queue =
	    new LinkedList<CellDistanceObject>();
	LinkedList<Integer> processedIds = new LinkedList<Integer>();
	
	// fill the queue with the cell's spheroid neighbors and their
	// distance from this.
	for (Iterator<Cell> i = spheroidCellNeighbors.iterator(); i.hasNext();) {
	    Cell ce = i.next();
	    queue.add(new CellDistanceObject(ce, 1));
	}
	processedIds.add(this.idNumber);

	//	System.out.println("[Cell.inhibitNeighborsBFS] queue.size()=" + queue.size());
	while (queue.size() != 0) {
	    //	    System.out.print("processedIds= ");
	    //	    for (Iterator<Integer> i = processedIds.iterator(); i.hasNext();) {
	    //		int x = i.next();
	    //		System.out.print(x + " ");
	    //	    }
	    //	    System.out.println();

	    CellDistanceObject io = queue.removeFirst();
	    Cell c = io.cell;
	    int d = io.distance;
	    //	    System.out.println("[Cell.inhibitNeighborsBFS] considering cell " + c.idNumber
	    //			       + " at distance " + d);
	    
	    // skip cells that are too far away
	    if (d > range) {
		continue;
	    }
	    // skip cells that have already been processed
	    int result = Collections.binarySearch(processedIds, c.getIdNumber());
	    if (result >= 0) {
		continue;
	    }
	    int insertionIndex = -(result + 1);
	    //	    System.out.println("Adding " + c.getIdNumber() + " at index " + insertionIndex);
	    c.inhibited = true;
	    processedIds.add(insertionIndex, c.getIdNumber());
	    for (Iterator<Cell> i = c.spheroidCellNeighbors.iterator(); i.hasNext();) {
		Cell ce = i.next();
		queue.add(new CellDistanceObject(ce, d + 1));
	    }
	}
    }


    private static int modulo(int n, int modulus) {
	int m = n % modulus;
	if (m < 0) {
	    m += modulus;
	}
	return m;
    }


    public static void testInhibitNeighborsBFS() {
	int dim = 5;
	Cell[][] cellArr = new Cell[dim][dim];
	for (int i = 0; i < dim; i++) {
	    for (int j = 0; j < dim; j++) {
		cellArr[i][j] = new Cell();
	    }
	}
	LinkedList<Cell> neighbors;
	for (int i = 0; i < dim; i++) {
	    for (int j = 0; j < dim; j++) {
		neighbors = new LinkedList<Cell>();
		//		neighbors.add(cellArr[modulo(i + 1, dim)][modulo(j + 1, dim)]);
		neighbors.add(cellArr[modulo(i + 1, dim)][modulo(j, dim)]);
		//		neighbors.add(cellArr[modulo(i + 1, dim)][modulo(j - 1, dim)]);
		neighbors.add(cellArr[modulo(i, dim)][modulo(j + 1, dim)]);
		neighbors.add(cellArr[modulo(i, dim)][modulo(j - 1, dim)]);
		//		neighbors.add(cellArr[modulo(i - 1, dim)][modulo(j + 1, dim)]);
		neighbors.add(cellArr[modulo(i - 1, dim)][modulo(j, dim)]);
		//		neighbors.add(cellArr[modulo(i - 1, dim)][modulo(j - 1, dim)]);
		cellArr[i][j].spheroidCellNeighbors = neighbors;
	    }
	}
	
	//	neighbors = cellArr[0][0].spheroidCellNeighbors;
	//	for (Iterator<Cell> i = neighbors.iterator(); i.hasNext();) {
	//	    Cell c = i.next();
	//	    System.out.println(c.getIdNumber());
	//	}

	int x = dim / 2;
	int y = dim / 2;
	int r = 100;
	System.out.println("cellArr[" + x + "][" + y + "].inhibitNeighborsBFS(" + r
			   + ")  (idNumber=" + cellArr[x][y].getIdNumber() + ")");
	cellArr[x][y].inhibitNeighborsBFS(r);
	for (int i = 0; i < dim; i++) {
	    for (int j = 0; j < dim; j++) {
		System.out.println("cellArr[" + i + "][" + j + "].inhibited="
				   + cellArr[i][j].inhibited);
	    }
	}
	
    }


    public void addSpheroidCellNeighbor(Cell c) {
	spheroidCellNeighbors.add(c);
    }


    public Point3D getFrontOrientation() {
	//	System.out.println("[Cell.getFrontOrientation] cell " + idNumber
	//			   + " nodeList.size()=" + nodeList.size());
	if (baseSphere != null) {
	    Environment.die("[Cell.getFrontOrientation] spheroid cell has no front orientation: "
			    + this);
	}
	Point3D p0 = nodeList.getFirst().getLocation();
	Point3D p1 = nodeList.get(1).getLocation();
	return p0.minus(p1);
    }


    public static void testMigrationRule(Environment e) {
	//	Node n0 = new Node(new double[] {0,0,0});
	//	Node n1 = new Node(new double[] {0,0,-1});
	Node n0 = new Node(new Point3D(0,0,0));
	Node n1 = new Node(new Point3D(0,0,-1));
	Cell c = new Cell(n0, n1, 1);

	c.step(e);

    }
	

    private static double round(double n) {
	return Math.round(n * outputGeometryPrecisionFactor) / outputGeometryPrecisionFactor;
    }

    public static void testRound() {
	double n = 543210.1234567890123456789;
	System.out.println(n);
	if (useSpecifiedOutputGeometryPrecision) {
	    n = round(n);
	}
	System.out.println(n);
    }

    public void printBaseSphereCoordinates(boolean suppressLeadingComma, PrintStream ps) {
	if (baseSphere != null) {
	    double x = baseSphere.xCoord;
	    double y = baseSphere.yCoord;
	    double z = baseSphere.zCoord;
	    double radius = baseSphere.radius;
	    if (!suppressLeadingComma) {
		ps.print(",");
	    }
	    if (useSpecifiedOutputGeometryPrecision) {
		x = round(x);
		y = round(y);
		z = round(z);
	    }
	    ps.print(x + "," + y + "," + z + "," + radius);
	}
    }

    public static void testPrintBaseSphereCoordinates() {
	spheroid.Sphere s = new spheroid.Sphere();
	s.xCoord = 7;
	s.yCoord = 13;
	s.zCoord = 29;
	s.radius = 41;
	Cell c = new Cell(s);
	c.printBaseSphereCoordinates(true, System.out);
    }

    // prints coordinates of cell's endpoints and inflection points
    public boolean printCellCoordinates(boolean firstFlag, PrintStream ps) {
	if (baseSphere != null) {
	    Environment.die("[Cell.printCellCoordinates] invoked on spheroid cell ");
	}
	
	double radiusForPrinting = radius;
	// Iterate backwards from the end of the list which is the rear endpoint.
	ListIterator<Node> i = nodeList.listIterator(nodeList.size());
	Point3D prev = null;
	// make a copy because prev will be mutated
	if (i.hasPrevious()) {
	    prev = i.previous().getLocation().copy();
	    if (useSpecifiedOutputGeometryPrecision) {
		prev = new Point3D(round(prev.x), round(prev.y), round(prev.z));
		radiusForPrinting = round(radius);
	    }
	}
	while (i.hasPrevious()) {
	    Node n = i.previous();
	    Point3D p = n.getLocation();
	    double x = p.x;
	    double y = p.y;
	    double z = p.z;
	    // Determine if the cylinder should be printed, i.e. its
	    // endpoints are not the same.  The endpoints are always
	    // different, but if the precision of coordinate values
	    // is restricted, then endpoints may appear to be the
	    // same.
	    boolean print = true;
	    if (useSpecifiedOutputGeometryPrecision) {
		x = round(x);
		y = round(y);
		z = round(z);
		if (x == prev.x && y == prev.y && z == prev.z) {
		    print = false;
		}
	    }
	    
	    if (print) {
		if (firstFlag) {
		    firstFlag = false;
		}
		else {
		    ps.print(",");
		}
		ps.print(prev.x + "," + prev.y + "," + prev.z
			 + "," + x + "," + y + "," + z
			 + "," + radiusForPrinting);
		prev.x = x;
		prev.y = y;
		prev.z = z;
	    }
	}
	return firstFlag;
    }


    // Used for testing when the idNumber of the cell is printed
    // instead of its coordinates.
    private static void testprintSproutCoordinates2() {
	Cell c0 = new Cell();
	Cell c1 = new Cell();
	Cell c2 = new Cell();
	Cell c3 = new Cell();
	Cell c4 = new Cell();
	Cell c5 = new Cell();
	Cell c6 = new Cell();
	System.out.println("height 1 tree");
	c0.printSproutCoordinates(true, System.out);	System.out.println();

	System.out.println("height 2 trees");
	c0.predecessor = c1;
	c1.successor = c0;
	c0.printSproutCoordinates(true, System.out); System.out.println();

	c0.predecessor = null;
	c0.predecessorB = c2;
	c2.successor = c0;
	c0.printSproutCoordinates(true, System.out); System.out.println();

	c0.predecessor = c1;
	c0.printSproutCoordinates(true, System.out); System.out.println();
	
	System.out.println("height 3 trees");
	c1.predecessor = c3;
	c3.successor = c1;
	c0.printSproutCoordinates(true, System.out); System.out.println();
	c1.predecessor = null;
	c1.predecessorB = c4;
	c4.successor = c1;
	c0.printSproutCoordinates(true, System.out); System.out.println();
	c1.predecessor = c3;
	c0.printSproutCoordinates(true, System.out); System.out.println();
	c0.predecessorB = null;
	c0.printSproutCoordinates(true, System.out); System.out.println();
	c0.predecessorB = c2;
	c2.predecessor = c5;
	c5.successor = c2;
	c0.printSproutCoordinates(true, System.out); System.out.println();
	c2.predecessorB = c6;
	c6.successor = c2;
	c0.printSproutCoordinates(true, System.out); System.out.println();
	c0.predecessor = null;
	c0.printSproutCoordinates(true, System.out); System.out.println();
	c2.predecessor = null;
	c0.printSproutCoordinates(true, System.out); System.out.println();
    }


    // Prints sprout coordinates from this cell forward by following
    // predecessor and predecessorB links
    public boolean printSproutCoordinates(boolean firstFlag, PrintStream ps) {
	boolean returning = false;
	Cell c = this;
	// loop ends when the traversal has returned to the starting cell
	while (!(returning && c == this)) {
	    // print the cell only when it is first encountered
	    if (!returning) {
		firstFlag = c.printCellCoordinates(firstFlag, ps);
	    }
	    // heading towards a tip cell
	    if (!returning) {
		// If it exists proceed to the predecessor
		if (c.predecessor != null) {
		    c = c.predecessor;
		}
		else {
		    // When the predecessor does not exists proceed to
		    // predecessorB (it it exists)
		    if (c.predecessorB != null) {
			c = c.predecessorB;
		    }
		    // If no predecessor exists, go back
		    else {
			System.out.println("[Cell.printSproutCoordinates] tip cell " + idNumber);
			returning = true;
		    }
		}
	    }
	    else {
		// Check for impossible condition
		if (c.successor == null) {
		    Environment.die("[Cell.printSproutCoordinates] Cell has non successor to return to: "
				    + c.getIdNumber());
		}
		else {
		    if (c == c.successor.predecessor) {
			// When returning from the predecessor, head
			// out on the predecessorB (it it exists)
			if (c.successor.predecessorB != null) {
			    c = c.successor.predecessorB;
			    returning = false;
			}
			// If no predecssorB exists, then return to the successor
			else {
			    c = c.successor;
			}
		    }
		    else { 
			// When returning from the predecessorB, since
			// the predecessor has already been visited,
			// return to the successor
			if (c == c.successor.predecessorB) {
			    c = c.successor;
			}
			else {
			    // Should be impossible to get here.
			    Environment.die("[Cell.printSproutCoordinates] Cell is not a predecessor of its successor: "
					    + c.getIdNumber() + "  " + c.successor.getIdNumber());
			}
		    }
		}
	    }
	}
	return firstFlag;
    }


    // returns new value of firstFlag indicating that the next item to
    // be printed is the first
    public boolean printSproutCoordinatesOLDOLD(boolean firstFlag,
						PrintStream ps) {
	// this procedure starts from the base sphere, so print
	// nodeList and then recur on predecessors
	if (baseSphere != null) {
	    Environment.die("[Cell.printSproutCoordinates] invoked on spheroid cell ");
	}
	
	// assign the cell a color integer
	setColorInts();

	//	System.out.println("[Cell.printSproutCoordinatesOLDOLD] " + idNumber
	//			   + " radius=" + radius + " pred="
	//			   + (predecessor == null? "null" : predecessor.idNumber)
	//			   + "  predB="
	//			   + (predecessorB == null? "null" : predecessorB.idNumber));

	double radiusForPrinting = radius;
	String branchIndicator = "";
	if (INDICATE_BRANCHES) {
	    branchIndicator = onBranch? ",1" : ",0";
	}
	ListIterator<Node> i = nodeList.listIterator(nodeList.size());
	Point3D prev = null;
	// make a copy because prev will be mutated
	if (i.hasPrevious()) {
	    prev = i.previous().getLocation().copy();
	    if (useSpecifiedOutputGeometryPrecision) {
		prev = new Point3D(round(prev.x), round(prev.y), round(prev.z));
		radiusForPrinting = round(radius);
	    }
	}
	while (i.hasPrevious()) {
	    Node n = i.previous();
	    Point3D p = n.getLocation();
	    double x = p.x;
	    double y = p.y;
	    double z = p.z;
	    // Determine if the cylinder should be printed, i.e. its
	    // endpoints are not the same.  The endpoints are always
	    // different, but if the precision of coordinate values
	    // is restricted, then endpoints may appear to be the
	    // same.
	    boolean print = true;
	    if (useSpecifiedOutputGeometryPrecision) {
		x = round(x);
		y = round(y);
		z = round(z);
		if (x == prev.x && y == prev.y && z == prev.z) {
		    print = false;
		}
	    }
	    
	    if (PRINT_ONLY_LIMITED_XY_BRANCHES) {
		if (!onLimitedXYBranch) {
		    print = false;
		}
	    }

	    if (print) {
		if (firstFlag) {
		    firstFlag = false;
		}
		else {
		    ps.print(",");
		}
		ps.print(idNumber + "," + prev.x + "," + prev.y + "," + prev.z
			 + "," + x + "," + y + "," + z
			 + "," + radiusForPrinting);
		if (PRINT_SPROUT_COLOR) {
		    ps.print("," + cellColorInt + "," + stateColorInt);
		}
		prev.x = x;
		prev.y = y;
		prev.z = z;
	    }
	}
	if (predecessor != null) {
	    firstFlag = 
		predecessor.printSproutCoordinatesOLDOLD(firstFlag, ps);
	    if (predecessorB != null) {
		firstFlag =
		    predecessorB.printSproutCoordinatesOLDOLD(firstFlag, ps);
	    }
	}
	else {
	    //	    System.out.println();
	    if (predecessorB != null) {
		Environment.die("[Cell.printSproutCoordinates] predecessorB present, missing predecessor:    "
				+ this);
	    }
	    else {
		//		System.out.println("[Cell.printSproutCoordinatesOLDOLD] tip cell "
		//				   + idNumber);
	    }
	}
	return firstFlag;
    }
    

    // returns new value of firstFlag indicating that the next item to
    // be printed is the first
    public boolean printSproutCoordinatesOLD(boolean firstFlag, PrintStream ps) {
	// this procedure starts from the base sphere, so print
	// nodeList and then recur on predecessors
	if (baseSphere != null) {
	    Environment.die("[Cell.printSproutCoordinates] invoked on speheroid cell ");
	}
	
	boolean first = true;
	Point3D prev = null;
	for (ListIterator<Node> i = nodeList.listIterator(nodeList.size()); i.hasPrevious();) {
	    Node n = i.previous();
	    Point3D p = n.getLocation();
	    if (prev != null) {
		if (firstFlag) {
		    firstFlag = false;
		}
		else {
		    ps.print(",");
		}
		ps.print(prev.x + "," + prev.y + "," + prev.z
				 + "," + p.x + "," + p.y + "," + p.z
				 + "," + radius);
	    }
	    prev = p;
	}
	
	if (predecessor != null) {
	    firstFlag = predecessor.printSproutCoordinates(firstFlag, ps);
	    if (predecessorB != null) {
		firstFlag = predecessorB.printSproutCoordinates(firstFlag, ps);
	    }
	}
	else {
	    //	    System.out.println();
	    if (predecessorB != null) {
		Environment.die("[Cell.printSproutCoordinates] predecessorB present, missing predecessor:    "
				+ this);
	    }
	}
	return firstFlag;
    }
    



    public static void testPrintSproutCoordinates2() {
	System.out.println("[Cell.testprintSproutCooordinates2]");
	Point3D p1 = new Point3D(0, 0, 0);
	Point3D p2 = new Point3D(0.0001, 0, 0);
	Point3D p3 = new Point3D(0.001, 0, 0);
	Point3D p4 = new Point3D(0.1, 0, 0);
	Node n1 = new Node(p1);
	Node n2 = new Node(p2);
	Node n3 = new Node(p3);
	Node n4 = new Node(p4);
	LinkedList<Node> nodeList = new LinkedList<Node>();
	nodeList.add(n1);
	nodeList.add(n3);
	nodeList.add(n2);
	nodeList.add(n4);

	Cell c = new Cell(nodeList, 1);
	c.printSproutCoordinates(true, System.out);
    }

    public static void testPrintSproutCoordinates() {
	Point3D p1 = new Point3D(0, 0, 0); //rear
	Point3D p2 = new Point3D(1, 0, 0);
	Point3D p3 = new Point3D(1, 1, 0);
	Point3D p4 = new Point3D(0, 0, 1); // front

	Point3D p5 = new Point3D(0, 0, 1);
	Point3D p6 = new Point3D(0, 1, 2);
	Point3D p7 = new Point3D(1, 2, 2);
	Point3D p8 = new Point3D(1, 1, 3);

	Point3D p9 = new Point3D(0, 0, 1);
	Point3D p0 = new Point3D(2, 1, 1);

	Node n1 = new Node(p1);
	Node n2 = new Node(p2);
	Node n3 = new Node(p3);
	Node n4 = new Node(p4);

	Node n5 = new Node(p5);
	Node n6 = new Node(p6);
	Node n7 = new Node(p7);
	Node n8 = new Node(p8);

	Node n9 = new Node(p9);
	Node n0 = new Node(p0);


	LinkedList<Node> nodeList1 = new LinkedList<Node>();
	LinkedList<Node> nodeList2 = new LinkedList<Node>();
	LinkedList<Node> nodeList3 = new LinkedList<Node>();
	
	nodeList1.addFirst(n1);
	nodeList1.addFirst(n2);
	nodeList1.addFirst(n3);
	nodeList1.addFirst(n4);

	nodeList2.addFirst(n5);
	nodeList2.addFirst(n6);
	nodeList2.addFirst(n7);
	nodeList2.addFirst(n8);


	nodeList3.addFirst(n9);
	nodeList3.addFirst(n0);


	Cell c1 = new Cell(nodeList1, 1);
	Cell c2 = new Cell(nodeList2, 1);
	Cell c3 = new Cell(nodeList3, 1);

	c1.setPredecessor(c2);
	c1.setPredecessorB(c3);
	    
	c2.setSuccessor(c1);
	c3.setSuccessor(c1);

	System.out.println(c1);
	System.out.println();
	System.out.println(c2);
	System.out.println();
	System.out.println(c3);
	System.out.println();
	System.out.println();
	


	c1.printSproutCoordinates(true, System.out);
    }

    public Cell getOrigin() {
	Cell origin;
	if (successor == null) {
	    origin = this;
	}
	else {
	    origin = successor.getOrigin();
	}
	return origin;
    }


    // Nonrecursive version
    // Checks that each cell's first node location is the same as the
    // last node location of its predecessors.
    public boolean continuityCheck() {
	LinkedList<Cell> cellStack = new LinkedList<Cell>();
	int cellStackSize = 0;
	cellStack.add(this);
	cellStackSize++;
	while (cellStackSize > 0) {
	    Cell c = cellStack.removeFirst();
	    Point3D p = c.nodeList.getFirst().getLocation();
	    Point3D tail;
	    cellStackSize--;
	    if (c.predecessorB != null) {
		tail = c.predecessorB.nodeList.getLast().getLocation();
		if (!p.equals(tail)) {
		    Environment.die("[Cell.continuityCheck] Point: " + p
				    + " of cell " + c
				    + " is not the last location of: "
				    + c.predecessorB);
		}
		cellStack.addFirst(c.predecessorB);
		cellStackSize++;
	    }
	    if (c.predecessor != null) {
		tail = c.predecessor.nodeList.getLast().getLocation();
		if (!p.equals(tail)) {
		    Environment.die("[Cell.continuityCheck] Point: " + p
				    + " of cell " + c
				    + " is not the last location of: "
				    + c.predecessor);
		}
		cellStack.addFirst(c.predecessor);
		cellStackSize++;
	    }
	}
	if (cellStack.size() != 0) {
	    Environment.die("[Cell.continuityCheck] Stack size is: "
			    + cellStack.size());
	}
	return true;
    }
		    
    public boolean continuityCheckOLD(Point3D p, Cell succ) {
	Point3D tailLocation = nodeList.getLast().getLocation();
	if (!tailLocation.equals(p)) {
	    Environment.die("[Cell.continuityCheck] Point: " + p
			    + " of cell " + succ
			    + " is not the last location of: " + this);
	}
	Point3D tipLocation = nodeList.getFirst().getLocation();
	if (predecessor != null) {
	    predecessor.continuityCheckOLD(tipLocation, this);
	}
	if (predecessorB != null) {
	    predecessorB.continuityCheckOLD(tipLocation, this);
	}
	return true;
    }

    public boolean continuityCheckOLD() {
	if (baseSphere == null) {
	    Environment.die("[Cell.continuityCheck] Do not start contiuity check at a nonspheroid cell: "
			    + this);
	}
	if (nodeList.size() != 1) {
	    Environment.die("[Cell.continuityCheck] Spheroid cell: " + this
			    + " has " + nodeList.size() + " nodes");
	}
	if (successor != null) {
	    Environment.die("[Cell.continuityCheck] Spheroid cell: " + this
			    + " has successor: " + successor);
	}

	/*
	if (predecessorB != null)
	    Environment.die("[Cell.continuityCheck] Spheroid cell: " + this
			    + " has B predecessor: " + predecessorB);
	*/

	Point3D p = nodeList.getFirst().getLocation();
	if (predecessor != null) {
	    predecessor.continuityCheckOLD(p, this);
	}
	if (predecessorB != null) {
	    predecessorB.continuityCheckOLD(p, this);
	}
	return true;
    }
    

    public static int getCreatedCellCount() {
	return nextIdNumber;
    }

    // Nonrecursive version
    public void markPredecessors() {
	LinkedList<Cell> stack = new LinkedList<Cell>();
	int stackSize = 0;
	stack.add(this);
	stackSize++;
	while (stackSize > 0) {
	    Cell c = stack.removeFirst();
	    stackSize--;
	    c.predecessorMark = true;
	    if (c.predecessorB != null) {
		stack.addFirst(c.predecessorB);
		stackSize++;
	    }
	    if (c.predecessor != null) {
		stack.addFirst(c.predecessor);
		stackSize++;
	    }
	}
	if (stack.size() != 0) {
	    Environment.die("[Cell.markPredecessors] stack size is not 0: "
			    + stack.size());
	}
    }


    public void markPredecessorsOLD() {
	predecessorMark = true;
	if (predecessor != null) {
	    predecessor.markPredecessors();
	}
	if (predecessorB != null) {
	    predecessorB.markPredecessors();
	}
    }

    public boolean isMarkedAsPredecessor() {
	return predecessorMark;
    }

    public void printSproutCoordinatesOld() {
	Cell pred = null;
	/*
	if (baseSphere != null) {
	    Point3D q = nodeList.getFirst().getLocation();
	    System.out.println(q);
	}
	*/
	for (Cell c = predecessor; c != null; c = pred) {
	    pred = c.getPredecessor();
	    //	    System.out.println(c.getIdNumber());
	    boolean isTipCell = (pred == null);
	    for (ListIterator<Node> i = c.nodeList.listIterator(c.nodeList.size());
		 i.hasPrevious();) {
		Node n = i.previous();
		Point3D p = n.getLocation();
		boolean firstNode = !i.hasPrevious();
		if (!firstNode || isTipCell) {
		    System.out.println(p.x + "\t" + p.y + "\t" + p.z + "\t" + c.radius);
		}
		firstNode = false;
	    }
	}
    }
    


    public static void printStats() {
	System.out.println("[Cell.printStats] divisionCount=" + divisionCount);
	System.out.println("[Cell.printStats] attemptedMigrationCount="
			   + attemptedMigrationCount);
	System.out.println("[Cell.printStats] migrationCount=" + migrationCount);
	System.out.println("[Cell.printStats] redirectionCount=" + redirectionCount);
	System.out.println("[Cell.printStats] sproutCellsCreated="
			   + sproutCellsCreated);
	System.out.println("[Cell.printStats] branchCount=" + branchCount);
	System.out.println("[Cell.printStats] limitedXYBranchCount="
			   + limitedXYBranchCount);
    }



    public static void testMigrate() {
	Point3D p;
	p = new Point3D(0,0,0);
	Node n0 = new Node(p);
	p = new Point3D(-1,0,0);
	Node n1 = new Node(p);
	p = new Point3D(-2,0,0);
	Node n2 = new Node(p);
	Cell c1 = new Cell(n0, n1, 1);
	Cell c2 = new Cell(n1, n2, 1);

	c1.setSuccessor(c2);
	c2.setPredecessor(c1);

	System.out.println("[Cell.testMigration]");
	System.out.println(c1);
	System.out.println();
	System.out.println(c2);
	System.out.println();
	System.out.println();

	p = new Point3D(1, 1, 0);

	c1.migrate(p);

	System.out.println(c1);
	System.out.println();
	System.out.println(c2);
	System.out.println();


    }


    public static long getBranchCount() {
	return branchCount;
    }

    public static long getLimitedXYBranchCount() {
	return limitedXYBranchCount;
    }

    // Nonrecursive version
    public double getSproutLength() {
	LinkedList<Cell> cellStack = new LinkedList<Cell>();
	int cellStackSize = 0;
	double sproutLength = 0;
	cellStack.add(this);
	cellStackSize++;
	while (cellStackSize > 0) {
	    Cell c = cellStack.removeFirst();
	    cellStackSize--;
	    sproutLength += c.computedLength();
	    if (c.predecessorB != null) {
		cellStack.addFirst(c.predecessorB);
		cellStackSize++;
	    }
	    if (c.predecessor != null) {
		cellStack.addFirst(c.predecessor);
		cellStackSize++;
	    }
	}
	if (cellStack.size() != 0) {
	    Environment.die("[Cell.getSproutlength] cellStack.size()="
			    + cellStack.size());
	}
	return sproutLength;
    }

    public double getSproutLengthOLD() {
	double cellLength = computedLength();
	double predecessorLength = 0;
	double predecessorBLength = 0;
	if (predecessor != null) {
	    predecessorLength = predecessor.getSproutLength();
	}
	if (predecessorB != null) {
	    predecessorBLength = predecessorB.getSproutLength();
	}
	return cellLength + predecessorLength + predecessorBLength;
    }


    // Nonrecursive version
    public double getSproutVolume() {
	LinkedList<Cell> cellStack = new LinkedList<Cell>();
	int cellStackSize = 0;
	double sproutVolume = 0;
	cellStack.add(this);
	cellStackSize++;
	while (cellStackSize > 0) {
	    Cell c = cellStack.removeFirst();
	    cellStackSize--;
	    sproutVolume += c.computedVolume();
	    if (c.predecessorB != null) {
		cellStack.addFirst(c.predecessorB);
		cellStackSize++;
	    }
	    if (c.predecessor != null) {
		cellStack.addFirst(c.predecessor);
		cellStackSize++;
	    }
	}
	if (cellStack.size() != 0) {
	    Environment.die("[Cell.getSproutVolume] cellStack.size()="
			    + cellStack.size());
	}
	return sproutVolume;
    }

    public double getSproutVolumeOLD() {
	double sproutVolume = 0;
	double predecessorVolume = 0;
	double predecessorBVolume = 0;
	if (baseSphere == null) {
	    sproutVolume = computedVolume();
	}
	if (predecessor != null) {
	    predecessorVolume = predecessor.getSproutVolume();
	}
	if (predecessorB != null) {
	    predecessorBVolume = predecessorB.getSproutVolume();
	}
	return sproutVolume + predecessorVolume + predecessorBVolume;
    }

    public static void testCollectSproutData() {
	System.out.println("[Cell.testCollectSproutData] Begin test 1");
	spheroidCenter = new Point3D(0, 0, 0);
	spheroidRadius = 5;
	nextIdNumber = 1;
	Cell c1 = new Cell();
	c1.baseSphere = new spheroid.Sphere();
	Node n1 = new Node(new Point3D(4, 0, 0));
	LinkedList<Node> nl1 = new LinkedList<Node>();
	nl1.add(n1);
	c1.nodeList = nl1;

	Cell c2 = new Cell();
	c2.radius = 1;
	Node n2 = new Node(new Point3D(7, 0, 0));
	LinkedList<Node> nl2 = new LinkedList<Node>();
	nl2.add(n2);
	nl2.addLast(n1);
	c2.nodeList = nl2;

	c2.successor = c1;
	c1.predecessor = c2;

	SproutData sd = new SproutData();
	c1.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 3
	    || sd.sproutVolumeCubicMicrons != computeVolume(c2.radius, 3) 
	    || sd.limitedXYSproutLengthMicrons != 2
	    || sd.limitedXYSproutAreaSquareMicrons != c2.radius * 2
	    || sd.limitedXYSproutCount != 1) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 2");

	Cell c3 = new Cell();
	c3.radius = 1;
	Node n3 = new Node(new Point3D(7, 3, 0));
	LinkedList<Node> nl3 = new LinkedList<Node>();
	nl3.add(n2);
	nl3.addLast(n3);
	c3.nodeList = nl3;
	c3.successor = c2;
	c2.predecessor = c3;
	
	sd = new SproutData();
	c1.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 6
	    || sd.sproutVolumeCubicMicrons != computeVolume(1, 6) 
	    || sd.limitedXYSproutLengthMicrons != 5
	    || sd.limitedXYSproutAreaSquareMicrons != c2.radius * 5
	    || sd.limitedXYSproutCount != 1) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}

	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 3");
	
	// test single cylinder completely inside spheroid shadow
	Cell c4 = new Cell();
	c4.radius = 1;
	Node n4a = new Node(new Point3D(2, 2, 1000));
	Node n4b = new Node(new Point3D(3, 2, 1000));
	c4.nodeList = new LinkedList<Node>();
	c4.nodeList.add(n4a);
	c4.nodeList.addLast(n4b);
	
	sd = new SproutData();
	c4.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 1
	    || sd.sproutVolumeCubicMicrons != computeVolume(1, 1) 
	    || sd.limitedXYSproutLengthMicrons != 0
	    || sd.limitedXYSproutAreaSquareMicrons != c2.radius * 0
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();

	System.out.println("[Cell.testCollectSproutData] Begin test 4");
	// test single cylinder completely outside spheroid shadow
	Cell c5 = new Cell();
	c5.radius = 2;
	Node n5a = new Node(new Point3D(13, 13, 1000));
	Node n5b = new Node(new Point3D(16, 17, 1000));
	c5.nodeList = new LinkedList<Node>();
	c5.nodeList.add(n5a);
	c5.nodeList.addLast(n5b);
	
	sd = new SproutData();
	c5.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 5
	    || sd.sproutVolumeCubicMicrons != computeVolume(2, 5) 
	    || sd.limitedXYSproutLengthMicrons != 5
	    || sd.limitedXYSproutAreaSquareMicrons != c5.radius * 5
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 5");
	// test single cylinder with rear point on circle
	Cell c6 = new Cell();
	c6.radius = 1;
	Node n6a = new Node(new Point3D(5, 6, 1000));
	Node n6b = new Node(new Point3D(5, 0, 1000));
	c6.nodeList = new LinkedList<Node>();
	c6.nodeList.add(n6a);
	c6.nodeList.addLast(n6b);
	
	sd = new SproutData();
	c6.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 6
	    || sd.sproutVolumeCubicMicrons != computeVolume(1, 6) 
	    || sd.limitedXYSproutLengthMicrons != 6
	    || sd.limitedXYSproutAreaSquareMicrons != c6.radius * 6
	    || sd.limitedXYSproutCount != 1) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();



	System.out.println("[Cell.testCollectSproutData] Begin test 6");
	// test single cylinder with forward point on circle
	Cell c7 = new Cell();
	c7.radius = 1;
	Node n7a = new Node(new Point3D(5, 0, 1000));
	Node n7b = new Node(new Point3D(5, 6, 1000));
	c7.nodeList = new LinkedList<Node>();
	c7.nodeList.add(n7a);
	c7.nodeList.addLast(n7b);
	
	sd = new SproutData();
	c7.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 6
	    || sd.sproutVolumeCubicMicrons != computeVolume(1, 6) 
	    || sd.limitedXYSproutLengthMicrons != 6
	    || sd.limitedXYSproutAreaSquareMicrons != c7.radius * 6
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 7");
	// test single cylinder tangent to circle
	Cell c8 = new Cell();
	c8.radius = 1;
	Node n8a = new Node(new Point3D(5, -4, 1000));
	Node n8b = new Node(new Point3D(5, 6, 1000));
	c8.nodeList = new LinkedList<Node>();
	c8.nodeList.add(n8a);
	c8.nodeList.addLast(n8b);
	
	sd = new SproutData();
	c8.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 10
	    || sd.sproutVolumeCubicMicrons != computeVolume(1, 10) 
	    || sd.limitedXYSproutLengthMicrons != 10
	    || sd.limitedXYSproutAreaSquareMicrons != c8.radius * 10
	    || sd.limitedXYSproutCount != 1) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}

	//	System.out.println(sd);
	//	System.out.println();

	//	if (true) {return;}

	System.out.println("[Cell.testCollectSproutData] Begin test 8");
	// test single cylinder crossing circle
	Cell c9 = new Cell();
	c9.radius = 3;
	Node n9a = new Node(new Point3D(0, 4, 1000));
	Node n9b = new Node(new Point3D(0, 6, 1000));
	c9.nodeList = new LinkedList<Node>();
	c9.nodeList.add(n9a);
	c9.nodeList.addLast(n9b);
	
	sd = new SproutData();
	c9.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 2
	    || sd.sproutVolumeCubicMicrons != computeVolume(c9.radius, 2) 
	    || sd.limitedXYSproutLengthMicrons != 1
	    || sd.limitedXYSproutAreaSquareMicrons != c9.radius * 1
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();

	System.out.println("[Cell.testCollectSproutData] Begin test 9");
	// test two cylinders completely within spheroid shadow
	Cell c10 = new Cell();
	c10.radius = 3;
	Node n10a = new Node(new Point3D(0, 1, 1000));
	Node n10b = new Node(new Point3D(1, 3, 1000));
	Node n10c = new Node(new Point3D(2, 0, 1000));
	c10.nodeList = new LinkedList<Node>();
	c10.nodeList.add(n10a);
	c10.nodeList.addLast(n10b);
	c10.nodeList.addLast(n10c);
	
	sd = new SproutData();
	c10.collectSproutData(sd);
	if (sd.sproutLengthMicrons != Math.sqrt(5) + Math.sqrt(10)
	    || sd.sproutVolumeCubicMicrons != computeVolume(c10.radius,
							    Math.sqrt(5)
							    + Math.sqrt(10)) 
	    || sd.limitedXYSproutLengthMicrons != 0
	    || sd.limitedXYSproutAreaSquareMicrons != 0
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 10");
	// test two cylinders completely outside spheroid shadow
	Cell c11 = new Cell();
	c11.radius = 2;
	Node n11a = new Node(new Point3D(8, 8, 1000));
	Node n11b = new Node(new Point3D(10, 8, 1000));
	Node n11c = new Node(new Point3D(14, 11, 1000));
	c11.nodeList = new LinkedList<Node>();
	c11.nodeList.add(n11a);
	c11.nodeList.addLast(n11b);
	c11.nodeList.addLast(n11c);
	
	sd = new SproutData();
	c11.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 7
	    || sd.sproutVolumeCubicMicrons != computeVolume(c11.radius, 7)
	    || sd.limitedXYSproutLengthMicrons != 7
	    || sd.limitedXYSproutAreaSquareMicrons != c11.radius * 7
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 11");
	// test two cylinders with common point on circle; rear
	// cylinder out and front cylinder in
	Cell c12 = new Cell();
	c12.radius = 1;
	Node n12a = new Node(new Point3D(3, 1, 1000));
	Node n12b = new Node(new Point3D(0, 5, 1000));
	Node n12c = new Node(new Point3D(0, 7, 1000));
	c12.nodeList = new LinkedList<Node>();
	c12.nodeList.add(n12a);
	c12.nodeList.addLast(n12b);
	c12.nodeList.addLast(n12c);
	
	sd = new SproutData();
	c12.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 7
	    || sd.sproutVolumeCubicMicrons != computeVolume(c12.radius, 7)
	    || sd.limitedXYSproutLengthMicrons != 2
	    || sd.limitedXYSproutAreaSquareMicrons != c12.radius * 2
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 12");
	// test two cylinders with common point on circle; rear
	// cylinder in and front cylinder out
	Cell c13 = new Cell();
	c13.radius = 5;
	Node n13a = new Node(new Point3D(0, 7, 1000));
	Node n13b = new Node(new Point3D(0, 5, 1000));
	Node n13c = new Node(new Point3D(3, 1, 1000));
	c13.nodeList = new LinkedList<Node>();
	c13.nodeList.add(n13a);
	c13.nodeList.addLast(n13b);
	c13.nodeList.addLast(n13c);
	
	sd = new SproutData();
	c13.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 7
	    || sd.sproutVolumeCubicMicrons != computeVolume(c13.radius, 7)
	    || sd.limitedXYSproutLengthMicrons != 2
	    || sd.limitedXYSproutAreaSquareMicrons != c13.radius * 2
	    || sd.limitedXYSproutCount != 1) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();

	System.out.println("[Cell.testCollectSproutData] Begin test 13");
	// test two cylinders with common point on circle; both
	// cylinders in
	Cell c14 = new Cell();
	c14.radius = 1;
	Node n14a = new Node(new Point3D(0, 4, 1000));
	Node n14b = new Node(new Point3D(0, 5, 1000));
	Node n14c = new Node(new Point3D(3, 1, 1000));
	c14.nodeList = new LinkedList<Node>();
	c14.nodeList.add(n14a);
	c14.nodeList.addLast(n14b);
	c14.nodeList.addLast(n14c);
	
	sd = new SproutData();
	c14.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 6
	    || sd.sproutVolumeCubicMicrons != computeVolume(c14.radius, 6)
	    || sd.limitedXYSproutLengthMicrons != 0
	    || sd.limitedXYSproutAreaSquareMicrons != 0
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 14");
	// test two cylinders with common point on circle; both
	// cylinders outside
	Cell c15 = new Cell();
	c15.radius = 2;
	Node n15a = new Node(new Point3D(0, 7, 1000));
	Node n15b = new Node(new Point3D(0, 5, 1000));
	Node n15c = new Node(new Point3D(4, 5, 1000));
	c15.nodeList = new LinkedList<Node>();
	c15.nodeList.add(n15a);
	c15.nodeList.addLast(n15b);
	c15.nodeList.addLast(n15c);
	
	sd = new SproutData();
	c15.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 6
	    || sd.sproutVolumeCubicMicrons != computeVolume(c15.radius, 6)
	    || sd.limitedXYSproutLengthMicrons != 6
	    || sd.limitedXYSproutAreaSquareMicrons != c15.radius * 6
	    || sd.limitedXYSproutCount != 1) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 15");
	// test two one-cylinders cells both inside shadow
	Cell c16 = new Cell();
	Cell c17 = new Cell();
	c16.radius = 1;
	c17.radius = 2;
	Node n16a = new Node(new Point3D(0, 4, 1000));
	Node n16b = new Node(new Point3D(0, 0, 1000));
	Node n17b = new Node(new Point3D(2, 0, 1000));
	c16.nodeList = new LinkedList<Node>();
	c16.nodeList.add(n16a);
	c16.nodeList.addLast(n16b);
	c17.nodeList = new LinkedList<Node>();
	c17.nodeList.addLast(n16b);
	c17.nodeList.addLast(n17b);
	c16.successor = c17;
	c17.predecessor = c16;

	sd = new SproutData();
	c17.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 6
	    || sd.sproutVolumeCubicMicrons != computeVolume(c16.radius, 4) + computeVolume(c17.radius, 2)
	    || sd.limitedXYSproutLengthMicrons != 0
	    || sd.limitedXYSproutAreaSquareMicrons != 0
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 16");
	// test two one-cylinders cells both outside shadow
	Cell c18 = new Cell();
	Cell c19 = new Cell();
	c18.radius = 1;
	c19.radius = 2;
	Node n18a = new Node(new Point3D(7, 4, 1000));
	Node n18b = new Node(new Point3D(7, 7, 1000));
	Node n19b = new Node(new Point3D(9, 7, 1000));
	c18.nodeList = new LinkedList<Node>();
	c18.nodeList.add(n18a);
	c18.nodeList.addLast(n18b);
	c19.nodeList = new LinkedList<Node>();
	c19.nodeList.addLast(n18b);
	c19.nodeList.addLast(n19b);
	c18.successor = c19;
	c19.predecessor = c18;

	sd = new SproutData();
	c19.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 5
	    || sd.sproutVolumeCubicMicrons != computeVolume(c18.radius, 3) + computeVolume(c19.radius, 2)
	    || sd.limitedXYSproutLengthMicrons != 5
	    || sd.limitedXYSproutAreaSquareMicrons != (c18.radius * 3) + (c19.radius * 2)
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();


	System.out.println("[Cell.testCollectSproutData] Begin test 17");
	// test double sprouts
	Cell c20 = new Cell();
	Cell c21 = new Cell();
	c20.radius = 10;
	c21.radius = 11;
	Node n20a = new Node(new Point3D(0, -3, 1000));
	Node n20b = new Node(new Point3D(0, -7, 1000));
	Node n20c = new Node(new Point3D(7, -7, 1000));
	Node n21a = new Node(new Point3D(7, 0, 1000));
	Node n21b = new Node(new Point3D(0, 0, 1000));
	Node n21c = new Node(new Point3D(0, 9, 1000));
	c20.nodeList = new LinkedList<Node>();
	c20.nodeList.add(n20a);
	c20.nodeList.addFirst(n20b);
	c20.nodeList.addFirst(n20c);
	c21.nodeList = new LinkedList<Node>();
	c21.nodeList.addFirst(n20c);
	c21.nodeList.addFirst(n21a);
	c21.nodeList.addFirst(n21b);
	c21.nodeList.addFirst(n21c);
	c21.successor = c20;
	c20.predecessor = c21;

	sd = new SproutData();
	c20.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 34
	    || sd.sproutVolumeCubicMicrons != computeVolume(c20.radius, 11) + computeVolume(c21.radius, 23)
	    || sd.limitedXYSproutLengthMicrons != 22
	    || sd.limitedXYSproutAreaSquareMicrons != (c20.radius * 9) + (c21.radius * 13)
	    || sd.limitedXYSproutCount != 2) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
	//	System.out.println(sd);
	//	System.out.println();

	System.out.println("[Cell.testCollectSproutData] Begin test 18");
	// test different z-coordinate points
	Cell c22 = new Cell();  // front
	Cell c23 = new Cell();  // rear
	c22.radius = 1;
	c23.radius = 2;
	Node n22a = new Node(new Point3D(10, 14, 1000));  // front 22
	Node n22b = new Node(new Point3D(10, 10, 1000));  // rear 22 front 23
	Node n23b = new Node(new Point3D(10, 10, 900));   // rear 23
	c22.nodeList = new LinkedList<Node>();
	c22.nodeList.add(n22a);
	c22.nodeList.addLast(n22b);
	c23.nodeList = new LinkedList<Node>();
	c23.nodeList.addLast(n22b);
	c23.nodeList.addLast(n23b);
	c22.successor = c23;
	c23.predecessor = c22;

	sd = new SproutData();
	c23.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 104
	    || sd.sproutVolumeCubicMicrons != computeVolume(c22.radius, 4) + computeVolume(c23.radius, 100)
	    || sd.limitedXYSproutLengthMicrons != 4
	    || sd.limitedXYSproutAreaSquareMicrons != 4 * c22.radius
	    || sd.limitedXYSproutCount != 0) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}


	System.out.println("[Cell.testCollectSproutData] begin test 19");
	// test branch length collection
	spheroidRadius = 1;
	Cell c24 = new Cell();
	Node n24a = new Node(new Point3D(0, 1, 0));
	Node n24b = new Node(new Point3D(0, 2, 0));
	c24.nodeList = new LinkedList<Node>();
	c24.nodeList.add(n24a);
	c24.nodeList.addFirst(n24b);
	Cell c25 = new Cell();
	Node n25a = new Node(new Point3D(0, 2, 0));
	Node n25b = new Node(new Point3D(0, 4, 0));
	c25.nodeList = new LinkedList<Node>();
	c25.nodeList.add(n25a);
	c25.nodeList.addFirst(n25b);
	Cell c26 = new Cell();
	Node n26a = new Node(new Point3D(0, 4, 0));
	Node n26b = new Node(new Point3D(0, 8, 0));
	c26.nodeList = new LinkedList<Node>();
	c26.nodeList.add(n26a);
	c26.nodeList.addFirst(n26b);
	
	Cell c27 = new Cell();
	Node n27a = new Node(new Point3D(0, 4, 0));
	Node n27b = new Node(new Point3D(-8, 4, 0));
	c27.nodeList = new LinkedList<Node>();
	c27.nodeList.add(n27a);
	c27.nodeList.addFirst(n27b);

	Cell c28 = new Cell();
	Node n28a = new Node(new Point3D(0, 2, 0));
	Node n28b = new Node(new Point3D(16, 2, 0));
	c28.nodeList = new LinkedList<Node>();
	c28.nodeList.add(n28a);
	c28.nodeList.addFirst(n28b);
	Cell c29 = new Cell();
	Node n29a = new Node(new Point3D(16, 2, 0));
	Node n29b = new Node(new Point3D(48, 2, 0));
	c29.nodeList = new LinkedList<Node>();
	c29.nodeList.add(n29a);
	c29.nodeList.addFirst(n29b);

	Cell c30 = new Cell();
	Node n30a = new Node(new Point3D(16, 2, 0));
	Node n30b = new Node(new Point3D(16, 66, 0));
	c30.nodeList = new LinkedList<Node>();
	c30.nodeList.add(n30a);
	c30.nodeList.addFirst(n30b);

	c24.predecessor = c25;
	c25.successor = c24;
	c25.predecessor = c26;
	c26.successor = c25;
	
	c25.predecessorB = c27;
	c27.successor = c25;

	c24.predecessorB = c28;
	c28.successor = c24;
	c28.predecessor = c29;
	c29.successor = c28;

	c28.predecessorB = c30;
	c30.successor = c28;

	c24.radius = 1;
	c25.radius = 1;
	c26.radius = 1;
	c27.radius = 1;
	c28.radius = 1;
	c29.radius = 1;
	c30.radius = 1;

	sd = new SproutData();
	c24.collectSproutData(sd);
	if (sd.sproutLengthMicrons != 127
	    || sd.sproutVolumeCubicMicrons != computeVolume(1, 127)
	    || sd.limitedXYSproutLengthMicrons != 127
	    || sd.limitedXYSproutAreaSquareMicrons != 127
	    || sd.limitedXYSproutCount != 1) {
	    System.out.println("[Cell.testCollectSproutData] Unexpected result: "
			       + sd);
	}
    }

    //    public static void testCollectSproutData2() {
    //	spheroidCenter = new Point3D(0, 0, 0);
    //	spheroidRadius = 5;
    //	Cell c1 = new Cell();
    //	c1.baseSphere = new spheroid.Sphere();
    //byron
    //    }


    public void collectSproutDataOLD(SproutData accumulator) {
	collectSproutDataOLD(accumulator, false);
    }

    // Nonrecursive version
    public void collectSproutDataOLDOLDOLD(SproutData accumulator) {
	LinkedList<MyDouble> branchLengths = new LinkedList<MyDouble>();
	LinkedList<Cell> cellStack = new LinkedList<Cell>();
	int cellStackSize = 0;
	cellStack.add(this);
	cellStackSize++;
	boolean first = true;
	boolean trace = false;
	while (cellStackSize > 0) {
	    Cell c = cellStack.removeFirst();
	    cellStackSize--;
	    //	    System.out.println("[Cell.collectSproutData] popping cell "
	    //			       + c.idNumber);
	    MyDouble currentBranchLength = new MyDouble(0);
	    // Don't add the first one because it is the start of the
	    // entire sprout and not the start of a branch.
	    if (first) {
		first = false;
	    }
	    else {
		branchLengths.addLast(currentBranchLength);
	    }
	    // traverse all cells from c to the tip while placing
	    // cells that are the starts of limited XY branches on the stack.
	    while (c != null) {
		//		System.out.println("     [Cell.collectSproutData] collecting data for cell "
		//				   + c.idNumber);
		if (trace) {
		    String succ =
			c.successor != null? "" + c.successor.idNumber: "null";
		    String pred =
			c.predecessor != null? "" + c.predecessor.idNumber: "null";
		    String predB =
			c.predecessorB != null? "" + c.predecessorB.idNumber: "null";
		    System.out.println("***** [Cell.collectSproutData] cell "
				       + c.idNumber  + " succ=" + succ
				       + " pred=" + pred + " predB=" + predB);
		}

		c.collectCellData(accumulator, currentBranchLength);
		if (c.predecessorB != null) {
		    // the branch point is the tip of the cell
		    Point3D branchLocation =
			c.nodeList.getFirst().getLocation();
		    double xyDistToCenter =
			branchLocation.xyDistance(spheroidCenter);
		    System.out.println("[Cell.collectSproutData] predB="
				       + c.predecessorB.idNumber + " loc="
				       + branchLocation + " center=" 
				       + spheroidCenter
				       + " xyDist=" + xyDistToCenter
				       + " spheroidRadius=" + spheroidRadius);
		    if (xyDistToCenter > spheroidRadius) {
			//			System.out.println("     [Cell.collectSproutData] pushing cell "
			//					   + c.predecessorB.idNumber);
			System.out.println("[Cell.collectSproutData] branch point found at " + branchLocation);
			cellStack.addFirst(c.predecessorB);
			cellStackSize++;
		    }
		}
		c = c.predecessor;
	    }
	}
	for (Iterator<MyDouble> i = branchLengths.iterator(); i.hasNext();) {
	    MyDouble md = i.next();
	    //	    accumulator.branchLengthList.add(md.value);
	}
	if (cellStack.size() != 0) {
	    Environment.die("[Cell.collectSproutData] cellStack.size()="
			    + cellStack.size());
	}
    }
	
    // Nonrecursive version
    public void collectSproutData(SproutData accumulator) {
	LinkedList<Cell> cellStack = new LinkedList<Cell>();
	int cellStackSize = 0;
	cellStack.add(this);
	cellStackSize++;
	while (cellStackSize > 0) {
	    Cell c = cellStack.removeFirst();
	    cellStackSize--;
	    c.collectCellData(accumulator);
	    if (c.predecessorB != null) {
		cellStack.addFirst(c.predecessorB);
		cellStackSize++;
	    }
	    if (c.predecessor != null) {
		cellStack.addFirst(c.predecessor);
		cellStackSize++;
	    }
	}
	if (cellStack.size() != 0) {
	    Environment.die("[Cell.collectSproutData] cellStack.size()="
			    + cellStack.size());
	}
    }
	

    public static void testComputeLimitedXYBranchLengths() {
	int branchCount;
	int expectedBranchCount;
	LinkedList<Double> branchLengths;

	Cell c0 = new Cell();
	Cell c1 = new Cell();
	
	Node n0a = new Node(new Point3D(-10, 0, 0));
	Node n0b = new Node(new Point3D(-10, 10, 1));

	Node n1a = new Node(new Point3D(-10, 10, 1));
	Node n1b = new Node(new Point3D(0, 10, 2));

	c0.nodeList = new LinkedList<Node>();
	c0.nodeList.add(n0a);
	c0.nodeList.addFirst(n0b);

	c1.nodeList = new LinkedList<Node>();
	c1.nodeList.add(n1a);
	c1.nodeList.addFirst(n1b);

	c0.predecessor = c1;
	c1.successor = c0;


	System.out.println("[Cell.testComputeLimitedXYBranchLengths] Begin test 1");
	limitedXYBranchStarts = new LinkedList<Cell>();
	limitedXYBranchStarts.add(c0);
	spheroidCenter = new Point3D(0, 0, 0);
	spheroidRadius = 1;
	branchLengths = computeLimitedXYBranchLengths();
	expectedBranchCount = 1;
	branchCount = branchLengths.size();
	if (branchCount != expectedBranchCount) {
	    Environment.die("[Cell.testComputeLimitedXYBranchLengths] expected "
			    + expectedBranchCount + " branches, but computed "
			    + branchCount + " branches ");
	}
	if (branchLengths.get(0) != 20) {
	    Environment.die("[Cell.testComputeLimitedXYBranchLengths] expected  first branch length of 29 but computed "
			    + branchLengths.get(0));
	}
	



	System.out.println("[Cell.testComputeLimitedXYBranchLengths] Begin test 2");
	Cell c2 = new Cell();
	Cell c3 = new Cell();

	Node n2a = new Node(new Point3D(0, 10, 2));
	Node n2b = new Node(new Point3D(0, 0, 3));

	Node n3a = new Node(new Point3D(0, 0, 3));
	Node n3b = new Node(new Point3D(10, 0, 4));

	c2.nodeList = new LinkedList<Node>();
	c2.nodeList.add(n2a);
	c2.nodeList.addFirst(n2b);

	c3.nodeList = new LinkedList<Node>();
	c3.nodeList.add(n3a);
	c3.nodeList.addFirst(n3b);

	c1.predecessor = c2;
	c2.successor = c1;

	c2.predecessor = c3;
	c3.successor = c2;

	limitedXYBranchStarts = new LinkedList<Cell>();
	limitedXYBranchStarts.add(c0);
	spheroidCenter = new Point3D(0, 0, 0);
	spheroidRadius = 1;
	branchLengths = computeLimitedXYBranchLengths();
	expectedBranchCount = 1;
	branchCount = branchLengths.size();
	if (branchCount != expectedBranchCount) {
	    Environment.die("[Cell.testComputeLimitedXYBranchLengths] expected "
			    + expectedBranchCount + " branches, but computed "
			    + branchCount + " branches ");
	}
	if (branchLengths.get(0) != 29) {
	    Environment.die("[Cell.testComputeLimitedXYBranchLengths] expected  first branch length of 29 but computed "
			    + branchLengths.get(0));
	}
	
	System.out.println("[Cell.testComputeLimitedXYBranchLengths] Begin test 3");
	Cell c4 = new Cell();
	Node n4a = new Node(new Point3D(0, 10, 2));
	Node n4b = new Node(new Point3D(10, 10, 7));
	c4.nodeList = new LinkedList<Node>();
	c4.nodeList.add(n4a);
	c4.nodeList.addFirst(n4b);
	c1.predecessorB = c4;
	c4.successor = c1;

	branchLengths = computeLimitedXYBranchLengths();
	expectedBranchCount = 1;
	branchCount = branchLengths.size();
	if (branchCount != expectedBranchCount) {
	    Environment.die("[Cell.testComputeLimitedXYBranchLengths] expected "
			    + expectedBranchCount + " branches, but computed "
			    + branchCount + " branches ");
	}
	if (branchLengths.get(0) != 29) {
	    Environment.die("[Cell.testComputeLimitedXYBranchLengths] expected  first branch length of 29 but computed "
			    + branchLengths.get(0));
	}


	System.out.println("[Cell.testComputeLimitedXYBranchLengths] Begin test 4");
	Cell c5 = new Cell();
	Node n5a = new Node(new Point3D(21, 21, 9));
	Node n5b = new Node(new Point3D(24, 25, 13));
	c5.nodeList = new LinkedList<Node>();
	c5.nodeList.add(n5a);
	c5.nodeList.addFirst(n5b);

	limitedXYBranchStarts.addLast(c5);

	branchLengths = computeLimitedXYBranchLengths();
	expectedBranchCount = 2;
	branchCount = branchLengths.size();
	if (branchCount != expectedBranchCount) {
	    Environment.die("[Cell.testComputeLimitedXYBranchLengths] expected "
			    + expectedBranchCount + " branches, but computed "
			    + branchCount + " branches ");
	}
	if (branchLengths.get(0) != 29) {
	    Environment.die("[Cell.testComputeLimitedXYBranchLengths] expected  first branch length of 29 but computed "
			    + branchLengths.get(0));
	}
	if (branchLengths.get(1) != 5) {
	    Environment.die("[Cell.testComputeLimitedXYBranchLengths] expected  second branch length of 5 but computed "
			    + branchLengths.get(1));
	}


    }
	


    public static LinkedList<Double> computeLimitedXYBranchLengths() {
	LinkedList<Double> branchLengthList = new LinkedList<Double>();
	for (Iterator<Cell> i = limitedXYBranchStarts.iterator();
	     i.hasNext();) {
	    Cell c = i.next();
	    double branchLength = 0;
	    while (c != null) {
		Iterator<Node> j = c.nodeList.descendingIterator();
		Node n = j.next();
		Point3D point = n.getLocation();
		Point3D intersection = null;
		while (j.hasNext() && intersection == null) {
		    n = j.next();
		    Point3D endPoint = n.getLocation();
		    // nextXYIntersection returns null when the XY
		    // projection of the line segment defined by point
		    // and endpoint does not intersect the XY
		    // projection of the sphere defined by
		    // spheroidCenter and spheroidRadius
		    intersection =
			Point3D.nextXYIntersection(point, endPoint,
						   spheroidCenter,
						   spheroidRadius);
		    Point3D point2 =
			intersection == null? endPoint : intersection;
		    double xyDist = point.xyDistance(point2);		    
		    branchLength += xyDist;
		    point = endPoint;
		}
		// If the cell did not cross the spheroid boundary,
		// then proceed with the next cell; otherwise stop
		// processing the current branch.
		if (intersection == null) {
		    c = c.predecessor;
		}
		else {
		    c = null;
		}
	    }
	    branchLengthList.add(branchLength);
	}
	return branchLengthList;
    }


    // Collect sprout data for one cell
    public void collectCellData(SproutData accumulator) {
	if (baseSphere != null && nodeList.size() != 1) {
	    Environment.die("[Cell.collectCellData] Cell with base sphere has nodeList of size "
			    + nodeList.size());
	}

	Iterator<Node> i = nodeList.descendingIterator();
	Point3D point = i.next().getLocation();
	boolean pointExposed = 
	    point.xyDistance(spheroidCenter) > spheroidRadius;
	double cellLength = 0;
	double exposedCellLength = 0;
	double virtualSproutCount = 0;
	while (i.hasNext()) {
	    Node n = i.next();
	    Point3D endPoint = n.getLocation();
	    double distance = endPoint.distance(point);
	    cellLength += distance;
	    Point3D intersection =
		Point3D.nextXYIntersection(point, endPoint,
					   spheroidCenter, spheroidRadius);
	    // Loop through 0, 1, or 2 intersection points.
	    // Intersection points are not considered to be exposed.
	    while (intersection != null) {
		if (pointExposed) {
		    exposedCellLength += point.xyDistance(intersection);
		}
		point = intersection;
		pointExposed = false;
		intersection =
		    Point3D.nextXYIntersection(point, endPoint,
					       spheroidCenter, spheroidRadius);
	    }

	    // The point variable was updated to the last non-null
	    // intersection point.  The interval from point to
	    // endPoint must be processed.
	    boolean endPointExposed =
		endPoint.xyDistance(spheroidCenter) > spheroidRadius;
	    if (endPointExposed) {
		exposedCellLength += point.xyDistance(endPoint);
		if (!pointExposed) {
		    // The interval from point to endPoint appears to
		    // be leaving the spheroid as a new sprout
		    virtualSproutCount ++;
		}
	    }
	    point = endPoint;
	    pointExposed = endPointExposed;
	}
	accumulator.sproutLengthMicrons += cellLength;
	accumulator.sproutVolumeCubicMicrons
	    += computeVolume(radius, cellLength);
	accumulator.limitedXYSproutLengthMicrons += exposedCellLength;
	// The branch length computation needs to be changed to
	// account for the unlikely case where a branch is occluded by
	// the spheorid and then reappears as a new sprout
	//	if (onLimitedXYBranch) {
	//	    //	if (onBranch) {
	//	    accumulator.limitedXYBranchLengthMicrons += exposedCellLength;
	//	}
	accumulator.limitedXYSproutAreaSquareMicrons
	    += exposedCellLength * radius;
	accumulator.limitedXYSproutCount += virtualSproutCount;
    }

    public class MyDouble {
	public double value;
	public MyDouble(double value) { this.value = value;}
    }



    // Collect sprout data for one cell.  The currentSegmentLength
    // argument is used to record the length of the current limited XY
    // branch.  If the cell is not on a limited XY branch, then this
    // argument is not used by the caller.
    public void collectCellData(SproutData accumulator,
				MyDouble currentSegmentLength) {
	if (baseSphere != null && nodeList.size() != 1) {
	    Environment.die("[Cell.collectCellData] Cell with base sphere has nodeList of size "
			    + nodeList.size());
	}

	Iterator<Node> i = nodeList.descendingIterator();
	Point3D point = i.next().getLocation();
	boolean pointExposed = 
	    point.xyDistance(spheroidCenter) > spheroidRadius;
	double cellLength = 0;
	double exposedCellLength = 0;
	double virtualSproutCount = 0;
	while (i.hasNext()) {
	    Node n = i.next();
	    Point3D endPoint = n.getLocation();
	    double distance = endPoint.distance(point);
	    cellLength += distance;
	    Point3D intersection =
		Point3D.nextXYIntersection(point, endPoint,
					   spheroidCenter, spheroidRadius);
	    // Loop through the 0, 1, or 2 intersection points of the
	    // cylinder axis and the perimiter of the spheroid of
	    // cells when both are projected obto the X-Y plane.  An
	    // intersection point can exist due to the cylinder axis
	    // crossing the perimiter or the cylinder axis being
	    // tangent to the perimiter.  Intersection points are not
	    // considered to be exposed.
	    double xyDist;
	    while (intersection != null) {
		if (pointExposed) {
		    xyDist = point.xyDistance(intersection);
		    //		    exposedCellLength += point.xyDistance(intersection);
		    exposedCellLength += xyDist;
		    currentSegmentLength.value += xyDist;
		    // This current segment has ended at the spheroid
		    // perimiter.  Creating a new currentSegmentLength
		    // MyDouble stops the current one from further
		    // increase
		    currentSegmentLength = new MyDouble(0);
		}
		// If there is a second intersection point, then the
		// interval between the first and second intersection
		// points is within the spheroid perimiter and hence
		// not exposed.  The interval from the last
		// intersection to the end point is handled outside
		// the loop.
		point = intersection;
		pointExposed = false;
		intersection =
		    Point3D.nextXYIntersection(point, endPoint,
					       spheroidCenter, spheroidRadius);
	    }

	    // The point variable was updated to the last non-null
	    // intersection point.  The interval from point to
	    // endPoint must be processed.
	    boolean endPointExposed =
		endPoint.xyDistance(spheroidCenter) > spheroidRadius;
	    if (endPointExposed) {
		xyDist = point.xyDistance(endPoint);
		exposedCellLength += xyDist;
		currentSegmentLength.value += xyDist;
		//		exposedCellLength += point.xyDistance(endPoint);
		if (!pointExposed) {
		    // The interval from point to endPoint appears to
		    // be leaving the spheroid as a new sprout
		    virtualSproutCount ++;
		}
	    }
	    point = endPoint;
	    pointExposed = endPointExposed;
	}
	accumulator.sproutLengthMicrons += cellLength;
	accumulator.sproutVolumeCubicMicrons
	    += computeVolume(radius, cellLength);
	accumulator.limitedXYSproutLengthMicrons += exposedCellLength;
	// The branch length computation needs to be changed to
	// account for the unlikely case where a branch is occluded by
	// the spheorid and then reappears as a new sprout
	//	if (onLimitedXYBranch) {
	//	    //	if (onBranch) {
	//	    accumulator.limitedXYBranchLengthMicrons += exposedCellLength;
	//	}
	accumulator.limitedXYSproutAreaSquareMicrons
	    += exposedCellLength * radius;
	accumulator.limitedXYSproutCount += virtualSproutCount;
    }








    public void collectSproutDataOLD(SproutData accumulator, boolean onBranch) {
	if (predecessor != null) {
	    predecessor.collectSproutDataOLD(accumulator, onBranch);
	}
	if (predecessorB != null) {
	    predecessorB.collectSproutDataOLD(accumulator, true);
	}
	if (baseSphere != null && nodeList.size() != 1) {
	    Environment.die("[Cell.collectSproutData] Cell with base sphere has nodeList of size "
			    + nodeList.size());
	}
	Iterator<Node> i = nodeList.descendingIterator();
	Point3D point = i.next().getLocation();
	boolean pointExposed = 
	    point.xyDistance(spheroidCenter) > spheroidRadius;
	double cellLength = 0;
	double exposedCellLength = 0;
	double virtualSproutCount = 0;
	while (i.hasNext()) {
	    Node n = i.next();
	    Point3D endPoint = n.getLocation();
	    double distance = endPoint.distance(point);
	    cellLength += distance;
	    Point3D intersection =
		Point3D.nextXYIntersection(point, endPoint,
					   spheroidCenter, spheroidRadius);
	    // Loop through 0, 1, or 2 intersection points.
	    // Intersection points are not considered to be exposed.
	    while (intersection != null) {
		if (pointExposed) {
		    exposedCellLength += point.xyDistance(intersection);
		}
		point = intersection;
		pointExposed = false;
		intersection =
		    Point3D.nextXYIntersection(point, endPoint,
					       spheroidCenter, spheroidRadius);
	    }

	    // The point variable was updated to the last non-null
	    // intersection point.  The interval from point to
	    // endPoint must be processed.
	    boolean endPointExposed =
		endPoint.xyDistance(spheroidCenter) > spheroidRadius;
	    if (endPointExposed) {
		exposedCellLength += point.xyDistance(endPoint);
		if (!pointExposed) {
		    // The interval from point to endPoint appears to
		    // be leaving the spheroid as a new sprout
		    virtualSproutCount ++;
		}
	    }
	    point = endPoint;
	    pointExposed = endPointExposed;
	}
	accumulator.sproutLengthMicrons += cellLength;
	accumulator.sproutVolumeCubicMicrons
	    += computeVolume(radius, cellLength);
	accumulator.limitedXYSproutLengthMicrons += exposedCellLength;
	// The branch length computation needs to be changed to
	// account for the unlikely case where a branch is occluded by
	// the spheorid and then reappears as a new sprout
	//	if (onBranch) {
	//	    accumulator.limitedXYBranchLengthMicrons += exposedCellLength;
	//	}
	accumulator.limitedXYSproutAreaSquareMicrons
	    += exposedCellLength * radius;
	accumulator.limitedXYSproutCount += virtualSproutCount;

    }


    public static void testGetSproutLengthVolume() {
	Point3D p;
	p = new Point3D(0,0,0);
	Node n0 = new Node(p);
	p = new Point3D(-1,0,0);
	Node n1 = new Node(p);
	p = new Point3D(0,1,0);
	Node n2 = new Node(p);
	p = new Point3D(2,0,0);
	Node n3 = new Node(p);
	
	LinkedList<Node> nl1 = new LinkedList<Node>();
	nl1.add(n0);
	nl1.addLast(n1);
	LinkedList<Node> nl2 = new LinkedList<Node>();
	nl2.add(n2);
	nl2.addLast(n0);
	LinkedList<Node> nl3 = new LinkedList<Node>();
	nl3.add(n3);
	nl3.addLast(n0);

	//       	Cell c1 = new Cell(n0, n1, 1);
	//	Cell c2 = new Cell(n2, n0, 2);
	//	Cell c3 = new Cell(n3, n0, 3);

       	Cell c1 = new Cell();
	c1.nodeList = nl1;
	c1.radius = 1;
	Cell c2 = new Cell();
	c2.nodeList = nl2;
	c2.radius = 2;
	Cell c3 = new Cell();
	c3.nodeList = nl3;
	c3.radius = 3;

	LinkedList<Node> nl = new LinkedList<Node> ();
	p = new Point3D(0,0,0);
	Node n = new Node(p);
	nl.add(n);

	Cell baseSphereCell = new Cell();
	baseSphereCell.baseSphere = new spheroid.Sphere();
	baseSphereCell.nodeList = nl;
	baseSphereCell.radius = 0;

	baseSphereCell.setPredecessor(c1);

	c1.setSuccessor(baseSphereCell);
	c1.setPredecessor(c2);
	c1.setPredecessorB(c3);

	c2.setSuccessor(c1);
	c3.setSuccessor(c1);


	double c1Length = c1.computedLength();
	double c2Length = c2.computedLength();
	double c3Length = c3.computedLength();

	double baseSproutLength = baseSphereCell.getSproutLength();
	double expectedBaseSproutLength = c1Length + c2Length + c3Length;
	if (baseSproutLength != expectedBaseSproutLength) {
	    Environment.die("[Cell.testGetLengthVolume] Expected sprout length starting from base: "
			    + expectedBaseSproutLength
			    + " baseSphereCell.getSproutLength()="
			    + baseSphereCell.getSproutLength());
	}
	
	double c1SproutLength = c1.getSproutLength();
	double expectedC1SproutLength = c1Length + c2Length + c3Length;
	if (c1SproutLength != expectedC1SproutLength) {
	    Environment.die("[Cell.testGetLengthVolume] Expected sprout length starting from c1: "
			    + expectedC1SproutLength
			    + " c1.getSproutLength()=" + c1.getSproutLength());
	}

	double c2SproutLength = c2.getSproutLength();
	double expectedC2SproutLength = c2Length;
	if (c2SproutLength != expectedC2SproutLength) {
	    Environment.die("[Cell.testGetLengthVolume] Expected sprout length starting from c2: "
			    + expectedC2SproutLength
			    + " c2.getSproutLength()=" + c2.getSproutLength());
	}

	double c3SproutLength = c3.getSproutLength();
	double expectedC3SproutLength = c3Length;
	if (c3SproutLength != expectedC3SproutLength) {
	    Environment.die("[Cell.testGetLengthVolume] Expected sprout length starting from c3: "
			    + expectedC3SproutLength
			    + " c3.getSproutLength()=" + c3.getSproutLength());
	}


	double c1Volume = c1.computedVolume();
	double c2Volume = c2.computedVolume();
	double c3Volume = c3.computedVolume();

	double baseSproutVolume = baseSphereCell.getSproutVolume();
	double expectedBaseSproutVolume = c1Volume + c2Volume + c3Volume;
	if (baseSproutVolume != expectedBaseSproutVolume) {
	    Environment.die("[Cell.testGetLengthVolume] Expected sprout volume starting from base: "
			    + expectedBaseSproutVolume
			    + " baseSphereCell.getSproutVolume()="
			    + baseSphereCell.getSproutVolume());
	}

	double c1SproutVolume = c1.getSproutVolume();
	double expectedC1SproutVolume = c1Volume + c2Volume + c3Volume;
	if (c1SproutVolume != expectedC1SproutVolume) {
	    Environment.die("[Cell.testGetLengthVolume] Expected sprout volume starting from c1: "
			    + expectedC1SproutVolume
			    + " c1.getSproutVolume()=" + c1.getSproutVolume());
	}

	double c2SproutVolume = c2.getSproutVolume();
	double expectedC2SproutVolume = c2Volume;
	if (c2SproutVolume != expectedC2SproutVolume) {
	    Environment.die("[Cell.testGetLengthVolume] Expected sprout volume starting from c2: "
			    + expectedC2SproutVolume
			    + " c2.getSproutVolume()=" + c2.getSproutVolume());
	}

	double c3SproutVolume = c3.getSproutVolume();
	double expectedC3SproutVolume = c3Volume;
	if (c3SproutVolume != expectedC3SproutVolume) {
	    Environment.die("[Cell.testGetLengthVolume] Expected sprout volume starting from c3: "
			    + expectedC3SproutVolume
			    + " c3.getSproutVolume()=" + c3.getSproutVolume());
	}




	System.out.println("[Cell.testGetSproutLengthVolume] Passed");

    }


    public int getSproutLengthCountOLD() {
	int lengthCount = 0;
	int lengthCountB = 0;
	if (predecessor != null) {
	    lengthCount = predecessor.getSproutLengthCountOLD();
	}
	if (predecessorB != null) {
	    lengthCountB = predecessorB.getSproutLengthCountOLD();
	}
	return 1 + Math.max(lengthCount, lengthCountB);
    }


    // used for testing purposes only
    //    public Cell getOrigin() {
    //	Cell c = this; 
    //	while (c.successor != null) {
    //	    c = c.successor;
    //	}
    //	return c;
    //    }
	

    private static void testAddCellGeometry() {
	spheroid.Sphere s = new spheroid.Sphere();
	s.xCoord = 101;
	s.yCoord = 102;
	s.zCoord = 103;
	s.radius = 104;
	Point3D p1 = new Point3D(11, 12, 13);
	Point3D p2 = new Point3D(21, 22, 23);
	Point3D p3 = new Point3D(31, 32, 33);
	Point3D p4 = new Point3D(41, 42, 43);
	Node n1 = new Node(p1);
	Node n2 = new Node(p2);
	Node n3 = new Node(p3);
	Node n4 = new Node(p4);
	LinkedList<Node> nodeList = new LinkedList<Node>();
	nodeList.add(n1);
	nodeList.add(n2);
	nodeList.add(n3);
	nodeList.add(n4);
	Cell c1 = new Cell();
	c1.baseSphere = s;
	Cell c2 = new Cell();
	c2.nodeList = nodeList;
	c2.radius = 77;
	LinkedList<CellGeometry> cgList = new LinkedList<CellGeometry>();
	c1.addCellGeometry(cgList);
	c2.addCellGeometry(cgList);
	for (Iterator<CellGeometry> i = cgList.iterator(); i.hasNext();) {
	    CellGeometry cg = i.next();
	    System.out.println(cg);
	}
	
    }


    public void addCellGeometry(LinkedList<CellGeometry> cgList) {
	LinkedList shapeList = new LinkedList<Shape>();
	if (baseSphere != null) {
	    Point3D center =
		new Point3D(baseSphere.xCoord,
			    baseSphere.yCoord,
			    baseSphere.zCoord);
	    double radius = baseSphere.radius;
	    Shape s = new Shape(center, radius);
	    shapeList.add(s);
	}
	else {
	    Iterator<Node> i = nodeList.iterator();
	    Node n1 = i.next();
	    Point3D p1 = n1.getLocation();
	    boolean cylinderCreated = false;
	    while (i.hasNext()) {
		Node n2 = i.next();
		Point3D p2 = n2.getLocation();
		Shape s = new Shape(p1, p2, radius);
		shapeList.add(s);
		p1 = p2;
		cylinderCreated = true;
	    }
	    if (!cylinderCreated) {
		Environment.die("[Cell.addCellGeometry] Sprout cell "
				+ idNumber + " has no cylinders");
	    }
	}
	CellGeometry cg = new CellGeometry(this, shapeList);
	cgList.add(cg);
    }
	

    public static double getInitialSproutLengthMicrons() {
	return initialTipCellLength + initialStalkCellLength;
    }


    public static long getAttemptedMigrationCount() {
	return attemptedMigrationCount;
    }

    public static double getAttemptedMigrationDistance() {
	return attemptedMigrationDistance;
    }

    public static double getActualMigrationDistance() {
	return actualMigrationDistance;
    }



    // Create a list of parameters

    
    public enum ParameterID {RADIUS, DEAD};
    private static Class PARAMETER_ID_CLASS = null;
    static {
    	try {
	    PARAMETER_ID_CLASS =
		Class.forName("scaffold.Cell$ParameterID");
	}
	catch (Exception e) {
	    Environment.die("[Cell] Unable to get class named scaffold.Cell$parameterID");
	}
    }

    private static void testGetSetParameters() {
	Cell c = new Cell();
	LinkedList<Parameter> params = c.getParameters();
	Parameter radiusParam = null;
	for (Iterator<Parameter> i = params.iterator(); i.hasNext();) {
	    Parameter p = i.next();
	    if (radiusParam == null) {
		radiusParam = p;
	    }
	    System.out.println(p);
	}

	radiusParam.getValues()[0] = "-1";
	radiusParam.setPresent(true);
	String status = c.setParameters(params);
	System.out.println(status);

	
    }

    public LinkedList<Parameter> getParameters() {
	LinkedList<Parameter> paramList = new LinkedList<Parameter>();
	Parameter p;
	p = new Parameter("Radius",
			  new String[] {"" + radius},
			  ParameterID.RADIUS);
	paramList.add(p);
	p = new Parameter("Dead", new String[0], ParameterID.DEAD);
	paramList.add(p);
	
	return paramList;
    }
    



    public String setParameters(LinkedList<Parameter> paramList) {
	String status = null;
	for (Iterator<Parameter> i = paramList.iterator();
	     i.hasNext() && status == null;) {
	    Parameter p = i.next();
	    if (p.getIdObject().getClass() != PARAMETER_ID_CLASS) {
		Environment.die("[Cell.setParameters] Unknown parameter object: "
				+ p);
	    }
	    ParameterID pid = (ParameterID) p.getIdObject();
	    switch (pid) {
	    case RADIUS:
		double newRadius = 0;
		try {
		    newRadius = Double.parseDouble(p.getString(0));
		}
		catch (Exception e) {
		    status = "Radius parameter is not a number";
		    continue;
		}
		if (newRadius <= 0) {
		    status = "Radius parameter is not a positive number";
		    continue;
		}
		radius = newRadius;
		changedShape = true;
		break;
	    case DEAD:
		dead = p.present();
		break;
	    default:
		Environment.die("[Cell.setParameters] Unexpected parameter: "
				+ p);
	    }
	}
	return status;
    }



    public boolean hasBranchStalkCell() {
	if (!onBranch) {
	    Environment.die("[Cell.hasBranchStalkCell] Cell " + idNumber
			    + " is not on a branch");
	}
	if (predecessor != null || predecessorB != null) {
	    Environment.die("[Cell.hasBranchStalkCell] Cell " + idNumber
			    + " is not a tip cell");
	}
	if (successor == null) {
	    Environment.die("[Cell.hasBranchStalkCell] Cell " + idNumber
			    + " does not have a successor cell");
	}
	boolean hasStalkCell =
	    (successor.predecessor == null || successor.predecessorB == null);
	return hasStalkCell;
    }


    boolean changedGeometry() {
	return changedShape;
    }

    public static void main(String[] args) {
	testComputeLimitedXYBranchLengths();
	testCollectSproutData();

	if (true) {return;}

	testGetSetParameters();
	if (true) {return;}

	testDistanceToTipCells();
	if (true) {return;}

	testGetSproutLengthVolume();
	if (true) {return;}

	testDivisionCheck();
	if (true) {return;}

	testLengthenAndPushForward();
	if (true) {return;}

	testResize2();
	if (true) {return;}

	testComputeLengthToRadiusRatio();
	if (true) {return;}

	testAddCellGeometry();
	if (true) {return;}

	testExtendableDistance();
	if (true) {return;}

	testProliferate2();
	if (true) {return;}

	testResize();
	if (true) {return;}

	testProliferate();
	if (true) {return;}

	testComputeProportionedRadius();
	if (true) {return;}

	

	testInhibitNeighborsBFS();
	if (true) {return;}

	testprintSproutCoordinates2();
	if (true) {return;}

	testCellPosition();
	//	testBranch();
	if (true) {return;}

	Node n1 = new Node(new Point3D(0, 0, 0));
	//	Node n1 = new Node(NodeInterface.ShapeType.SPHERE,       //&&
	//			   new double[] {0, 0, 0, 0});
	Node n2 = new Node(new Point3D(1, 1, 0));
	//	Node n2 = new Node(NodeInterface.ShapeType.SPHERE,       //&&
	//			   new double[] {1, 1, 0, 0});
	Node n3 = new Node(new Point3D(2, 2, 0));
	//	Node n3 = new Node(NodeInterface.ShapeType.SPHERE,       //&&
	//			   new double[] {2, 2, 0, 0});
	Node n4 = new Node(new Point3D(3, 3, 0));
	//	Node n4 = new Node(NodeInterface.ShapeType.SPHERE,       //&&
	//			   new double[] {3, 3, 0, 0});
	n2.setInflection();
	LinkedList<Node> n12 = new LinkedList<Node>();
	LinkedList<Node> n23 = new LinkedList<Node>();
	LinkedList<Node> n34 = new LinkedList<Node>();
	n12.add(n1);
	n12.add(n2);
	n23.add(n2);
	n23.add(n3);
	n34.add(n3);
	n34.add(n4);
	//	n12.add(n4);
	Cell c12 = new Cell(n12, 1.5);
	Cell c23 = new Cell(n23, 1.5);
	Cell c34 = new Cell(n34, 1.5);
	c12.setSuccessor(c23);
	c23.setPredecessor(c12);
	c23.setSuccessor(c34);
	c34.setPredecessor(c23);
	System.out.println(c12);
       	System.out.println(c23);
	System.out.println(c34);
	System.out.println();
	
	Cell cp12 = c12.extendedCopy();
	Cell cp23 = cp12.getSuccessor();
	Cell cp34 = cp23.getSuccessor();
	System.out.println(cp12);
       	System.out.println(cp23);
	System.out.println(cp34);
	System.out.println();


	if (true) {return;}

	Point3D p;
	p = new Point3D(0, -.5, 0);
	c12.migrate(p);
	System.out.println(c12);
       	System.out.println(c23);
	//       	System.out.println(c34);
	System.out.println();
	
	p = new Point3D(0, -1, 0);
	c12.migrate(p);
	System.out.println(c12);
       	System.out.println(c23);
	//       	System.out.println(c34);
	System.out.println();
	

	p = new Point3D(0, -1.5, 0);
	c12.migrate(p);
	System.out.println(c12);
       	System.out.println(c23);
	//       	System.out.println(c34);
	System.out.println();
	
	p = new Point3D(0, -2, 0);
	c12.migrate(p);
	System.out.println(c12);
       	System.out.println(c23);
	//       	System.out.println(c34);
	System.out.println();
	

	p = new Point3D(0, -2.5, 0);
	c12.migrate(p);
	System.out.println(c12);
       	System.out.println(c23);
	//       	System.out.println(c34);
	System.out.println();
	

	if (true) {return;}



	System.out.println();
	//	LinkedList<Node> nl2 = c12.removeRear(3, true);
	System.out.println(c12);
	System.out.println(c23);
	System.out.println();

	if (true) {return;}

	Point3D p1 = new Point3D(0, 0, 0);
	Point3D p2 = new Point3D(1, 1, 1);
	Point3D p12 = project(p1, p2, Math.sqrt(3));
	Point3D p21 = project(p2, p1, Math.sqrt(3));
	System.out.println("p12=(" + p12.x + "," + p12.y + "," + p12.z + ")");
	System.out.println("p21=(" + p21.x + "," + p21.y + "," + p21.z + ")");

	if (true) {return;}

	// create a cell and removeRear
	/*
	Node n1 = new Node(NodeInterface.ShapeType.SPHERE,
			   new double[] {0, 0, 0, 0});
	Node n2 = new Node(NodeInterface.ShapeType.SPHERE,
			   new double[] {1, 1, 1, 0});
	Node n3 = new Node(NodeInterface.ShapeType.SPHERE,
			   new double[] {2, 2, 2, 0});
	Node n4 = new Node(NodeInterface.ShapeType.SPHERE,
			   new double[] {3, 3, 3, 0});
			   
	LinkedList<Node> nodeList = new LinkedList<Node>();
	nodeList.add(n1);
	nodeList.add(n2);
	nodeList.add(n3);
	nodeList.add(n4);
	Cell c1 = new Cell(nodeList);
	System.out.println(c1);
	System.out.println();
	nodeList = c1.removeRear(Math.sqrt(3));
	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	    Node n = i.next();
	    System.out.println(n);
	}
	System.out.println();
	for (Iterator<Node> i = c1.getNodeList().iterator(); i.hasNext();) {
	    Node n = i.next();
	    System.out.println(n);
	}
	*/


	if (true) {return;}

	p = new Point3D(1, 1, 1);
	Node n = new Node(p);
	//	Node n = new Node(shared.NodeInterface.ShapeType.SPHERE, params);    //&&
	LinkedList<Node> nl = new LinkedList<Node>();
	nl.add(n);
	Cell c = new Cell(nl, 1);
	//	c.setGrowthRole(CellInterface.GrowthRole.STALK);
	//	System.out.println(c.getGrowthRole());
	System.out.println(c);
    }

    LinkedList<Node> getNodeList() {
	return nodeList;
    }

}
