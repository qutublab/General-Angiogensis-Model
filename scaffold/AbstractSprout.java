
package scaffold;

import shared.Point3D;

import java.util.*;

public class AbstractSprout {

    private static double[] shellRadius;
    private static Point3D shellCenter;

    private static int nextIdNumber = 0;

    int idNumber = -1;
    Point3D location = null;
    AbstractSprout successor = null;
    AbstractSprout predecessor = null;
    AbstractSprout predecessorB = null;
    boolean cellJunction = false;
    boolean reentrant = false;

    boolean flag = false;

    // -1 indicates location is not within a shell
    int shellIndex = -1;
    boolean onShell = false;

    public AbstractSprout(Point3D location, AbstractSprout predecessor,
			  AbstractSprout predecessorB, boolean cellJunction) {
	idNumber = nextIdNumber++;
	this.location = location;
	this.predecessor = predecessor;
	this.predecessorB = predecessorB;
	this.cellJunction = cellJunction;
	computeShellIndex();
    }


    public static void initialize(double[] shellRadius, Point3D shellCenter) {
	AbstractSprout.shellRadius = shellRadius;
	AbstractSprout.shellCenter = shellCenter;
    }

    private void computeShellIndex() {
	double dist = shellCenter.distance(location);
	//	System.out.println("[AbstractSprout.computeShellIndex] " + dist);
	shellIndex = -1;  // reset to -1 to handle cases where shellIndex is recomputed
	onShell = false; // reset to false for recomputations
	for (int i = 0; i < shellRadius.length; i ++) {
	    if (dist <= shellRadius[i]) {
		shellIndex = i;
		onShell = (dist == shellRadius[i]);
		/*
		System.out.println("[AbstractSprout.computeShellIndex] " + i + " "+ dist + " <= "
				   + shellRadius[i] + " " + onShell);
		*/
		break;
	    }
	}
    }


    public static void setShellRadius(double[] shellRadius) {
	AbstractSprout.shellRadius = shellRadius;
    }

    public static void setShellCenter(Point3D shellCenter) {
	AbstractSprout.shellCenter = shellCenter;
    }


    public int getIdNumber() {
	return idNumber;
    }

    public void setSuccessor(AbstractSprout successor) {
	this.successor = successor;
    }

    public Point3D getLocation() {
	return location;
    }

    public int getShellIndex() {
	return shellIndex;
    }

    public boolean isOnShell() {
	return onShell;
    }

    /*
     * Returns abstract representation of sprout starting from cell argument c
     * less the rearmost node.
     *
     * Note: cells are processed in succesor to predecessor order
     * (cells closest to spheroid before cells closest to tip cell,
     * but nodes are processed in the reverse order (nodes closest to
     * tip cell before nodes closest to spheroid)
     */
    private static AbstractSprout makeAbstractSprout(Cell c) {
	if (c == null) {
	    return null;
	}
	AbstractSprout as = null;
	AbstractSprout pred = makeAbstractSprout(c.getPredecessor());
	AbstractSprout predB = makeAbstractSprout(c.getPredecessorB());
	
	boolean firstNode = true;
	LinkedList<Node> nodeList = c.getNodeList();
	for (Iterator<Node> i = nodeList.iterator(); i.hasNext();) {
	    Node n = i.next();
	    //	    System.out.println("[makeAbstractSprout] considering " + n);

	    // skip last node which was handled when c's predecessor
	    // (if it exists) was processed because cells share
	    // endpoint nodes locations.
	    if (i.hasNext() || pred == null) {
		// frontmost (closest to sprout tip) nodes come first
		as = new AbstractSprout(n.getLocation(), pred, predB, firstNode);
		if (pred != null) {
		    pred.setSuccessor(as);
		}	    
		if (predB != null) {
		    pred.setSuccessor(as);
		}
		firstNode = false;
		pred = as;
		predB = null;
		//		System.out.println("[makeAbstractSprout] added " + as);
	    }
	}
	return as;
    }


    private static LinkedList<AbstractSprout> makeAbstract(LinkedList<Cell> sproutOriginCells) {
	LinkedList<AbstractSprout> abstractSproutList = new LinkedList<AbstractSprout>();
	for (Iterator<Cell> i = sproutOriginCells.iterator(); i.hasNext();) {
	    Cell c = i.next();
	    AbstractSprout as = makeAbstractSprout(c);
	    abstractSproutList.add(as);
	}
	return abstractSproutList;
    }
    


    private double sqr(double n) {
	return n * n;
    }


    /*
     * Returns the point along the line segment from p0 to p1 that
     * interects a sphere with the given center and radius.
     *
     * Note that if the line (not just the segment) defined by the
     * points is not tangent to the sphere, then if there is an
     * intersction, then there are two intersecting points.
     *
     * Assumptions: 1) the line segment does intersect the sphere (as
     * a nontangent). 2) Exactly one of p0 and p1 is within the sphere
     * and the other is outside of it. (Hence the line intersects and
     * is not tangent)
     */

    /*
     * The line from p0 = (x0,y0,z0) to p1 = (x1,y1,z1) is described in parametric form as:
     *
     * x = at + x0
     * y = bt + y0
     * z = ct + z0
     *
     * where:
     * 
     * a = x1 - x0
     * b = y1 - y0
     * c = z1 - z0
     *
     * Note that increasing non-negative values of t create points
     * starting from p0 and moving in the direction of p1.
     *
     * A sphere with center (xc,yc,zc) and with radius r is given as:
     *
     * (x - xc)^2 + (y - yc)^2 + (z - zc)^2 = r^2
     *
     * Thus, by substitution, the intersection of the above line and
     * sphere is described as:
     *
     * (at + x0 - xc)^2 + (bt + y0 - yc)^2 + (ct + z0 - zc)^2 = r^2
     *
     * If dx = x0 - xc, dy = y0 - yc, dz = z0 - zc, then:
     *
     * (at + dx)^2 + (bt + dy)^2 + (ct + dz)^2 = r^2
     *
     * and hence
     *
     * (a^2 + b^2 + c^2)t^2 + 2(adx + bdy + cdz)t + (dx^2 + dy^2 + dz^2 - r^2) = 0
     *
     * A solution for this binomial yields up to 2 values of t.  If p0
     * is closer to the sphere center than p1, then one value of t is
     * negative and represents the intersection point as one travels
     * along the line from p0 away from p1.  If p0 is farther away
     * from the center than p1, then both values of t are positive and
     * the lesser value represents a point on the finite segment
     * defined by p0 and p1.
     */

    private Point3D computeBorderPoint(Point3D p0, Point3D p1, Point3D center, double radius) {
	// compute constants for the parameterized line formula
	double a = p1.x - p0.x;
	double b = p1.y - p0.y;
	double c = p1.z - p0.z;

	double dx = p0.x - center.x;
	double dy = p0.y - center.y;
	double dz = p0.z - center.z;
	
	// parameters for the quadratic formula
	double quadA = sqr(a) + sqr(b) + sqr(c);
	double quadB = 2 * ((a * dx) + (b * dy) + (c * dz));
	double quadC = sqr(dx) + sqr(dy) + sqr(dz) - sqr(radius);
	
	double discriminant = Math.sqrt(sqr(quadB) - (4 * quadA * quadC));
	double t1 = (-quadB + discriminant) / (2 * a);
	double t2 = (-quadB - discriminant) / (2 * a);

	if (t1 < 0 && t2 < 0) {
	    Environment.die("[AbstractSprout.computeBorderPoint] Two negative values for t!");
	}

	double t;
	// Note t2 < t1
	if (t1 < 0) {
	    t = t2;
	}
	else {
	    if (t2 < 0) {
		t = t1;
	    }
	    else {
		t = Math.min(t1, t2);
	    }
	}

	
	double x = (a * t) + p0.x;
	double y = (b * t) + p0.y;
	double z = (c * t) + p0.z;

	return new Point3D(x, y, z);
    }


    /* 
     * Add AbstractSprout objects to indicate shell borders
     */
    private void addBorders() {
	AbstractSprout[] predArray = new AbstractSprout[] {predecessor, predecessorB};
	int predecessorFlag = 0;
	for (AbstractSprout pred : predArray) {
	    switch (Math.abs(pred.getShellIndex() - getShellIndex())) {
	    case 0:
		pred.addBorders();
		break;
	    case 1:
		if (!isOnShell()) {
		    // create an AbstractSprout object located on the shell
		    Point3D borderPoint = computeBorderPoint(location,
							     pred.getLocation(),
							     shellCenter,
							     shellRadius[getShellIndex()]);
		    AbstractSprout border = new AbstractSprout(borderPoint, pred, null, false);
		    if (predecessorFlag == 0) {
			predecessor = border;
		    }
		    else {
			predecessorB = border;
		    }
		    border.setSuccessor(this);
		    // check for shell recognition
		    if (! border.isOnShell()) {
			Environment.die("[AbstractSprout.addBorders] not on shell!");
		    }
		}
		pred.addBorders();
		break;
	    case 2:
		// decide which shell the next border is on
		int index;
		if (isOnShell()) {
		    index = getShellIndex() + 1;
		}
		else {
		    index = getShellIndex();
		}
		Point3D borderPoint = computeBorderPoint(location,
							 pred.getLocation(),
							 shellCenter,
							 shellRadius[index]);
		AbstractSprout border = new AbstractSprout(borderPoint, pred, null, false);
		if (predecessorFlag == 0) {
		    predecessor = border;
		}
		else {
		    predecessorB = border;
		}
		border.setSuccessor(this);
		// check for shell recognition (i.e. floating point math differences)
		if (! border.isOnShell()) {
		    Environment.die("[AbstractSprout.addBorders] not on shell!");
		}
		
		// Continue adding borders from the shell since pred
		// is more than one shell away
		border.addBorders();
		break;		
	    }
	    predecessorFlag ++;
	}
    }

		
    
    public String toString() {
	int succId = (successor == null)? -1:successor.getIdNumber();
	int predId = (predecessor == null)? -1:predecessor.getIdNumber();
	int predBId = (predecessorB == null)? -1:predecessorB.getIdNumber();

	String returnString =
	    "AbstractSprout[idNumber=" + idNumber
	    + ",location=" + location
	    + ",cellJunction=" + cellJunction
	    + ",shellIndex=" + shellIndex
	    + ",onShell=" + onShell
	    + ",successor=" + succId
	    + ",predecessor=" + predId
	    + ",predecessorB=" + predBId
	    + "]";
	return returnString;
    }
	    

    public void display() {
	for (AbstractSprout as = this; as != null; as = as.predecessor) {
	    System.out.println(this);
	}
    }
	

    private static void testAbstractSprout() {
	double[] shellRadius = new double[] {10, 20, 30};
	Point3D shellCenter = new Point3D(0, 0, 0);
	AbstractSprout.initialize(shellRadius, shellCenter);
	Point3D p = new Point3D(10*Math.sqrt(2), 10*Math.sqrt(2), 0);
	AbstractSprout as = new AbstractSprout(p, null, null, false);
	System.out.println(as);
    }

    private static void testMakeAbstractSprout() {
	LinkedList<LinkedList<Node>> nodeListList = new LinkedList<LinkedList<Node>>();
	LinkedList<Node> nodeList;
	Point3D p;
	
	nodeList = new LinkedList<Node>();
	p = new Point3D(28, 0, 0);
	nodeList.addLast(new Node(p));
	nodeList = new LinkedList<Node>();
	p = new Point3D(21, 0, 0);
	nodeList.addLast(new Node(p));
	p = new Point3D(14, 0, 0);
	nodeList.addLast(new Node(p));
	p = new Point3D(7, 0, 0);
	nodeList.addLast(new Node(p));

	nodeListList.add(nodeList);

	Cell c = Cell.buildSprout(nodeListList);

	double[] shellRadius = new double[] {10, 20, 30};
	Point3D shellCenter = new Point3D(0, 0, 0);
	AbstractSprout.initialize(shellRadius, shellCenter);
	AbstractSprout as = makeAbstractSprout(c); 
	as.display();
    }

    public static void main(String[] args) {
	testMakeAbstractSprout();

    }


}