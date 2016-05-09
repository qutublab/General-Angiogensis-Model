
/*
 *
 * 2/15/2011 - added rotateX, rotateY, and rotateZ methods
 * 3/1/2011 - added angleRadians method
 * 5/25/2011 - added angleDegrees method
 */



package shared;

import java.util.*;

public class Point3D {

    private static final int[] DELTA_ZERO = new int[] {0};
    private static final int[] DELTA_POS_ONE = new int[] {0, 1};
    private static final int[] DELTA_NEG_ONE = new int[] {0, -1};

    private static double defaultScale = 1;

    public double x;
    public double y;
    public double z;



    public Point3D(double x, double y, double z) {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    
    private static void die(String s) {
        System.err.println(s);
        Throwable th = new Throwable();
        th.printStackTrace();
        System.exit(1);

    }


    public boolean isAligned() {
	boolean aligned = 
	    x == Math.floor(x) && y == Math.floor(y) && z == Math.floor(z);
	return aligned;
    }

    public static void setDefaultScale(double defaultScale) {
	Point3D.defaultScale = defaultScale;
    }



    public boolean equals(Point3D p) {
	return (x == p.x && y == p.y && z == p.z);
    }

    public String toString() {
	String s;
	s = "Point3D[x=" + x
	    + ",y=" + y
	    + ",z=" + z
	    +"]";
	return s;
    }

    public Point3D copy() {
	return new Point3D(x, y, z);
    }

    private static double sqr(double n) {
	return (n * n);
    }


    public static double distance(double x1, double y1, double z1,
				  double x2, double y2, double z2) {
	double deltaX = x1 - x2;
	double deltaY = y1 - y2;
	double deltaZ = z1 - z2;
	double d = Math.sqrt(sqr(deltaX) + sqr(deltaY) + sqr(deltaZ));
	return d;
    }

    public double distance(Point3D p) {
	return distance(x, y, z, p.x, p.y, p.z);
    }

    public double xyDistance(Point3D p) {
	return distance(x, y, 0, p.x, p.y, 0);
    }


    private static void testProjectTowards() {
	Point3D frontPoint = 
	    new Point3D(-185.24149212796104,
			142.59896712688976,
			-50.41634832280967);
	Point3D newFrontPoint =
	    new Point3D(-212.7838258961798,
			163.80106554714666,
			-57.91242210698337);
	double actualDistance = 1.7763568394002505E-14;
	Point3D p = frontPoint.projectTowards(newFrontPoint, actualDistance);
	System.out.println("[Point3D.testProjectTowards] frontPoint="
			   + frontPoint + " p=" + p);
    }

    /*
     * Returns a point dist away from this towards (in the direction
     * of) p
     */
    public Point3D projectTowards(Point3D p, double dist) {
	double deltaX = p.x - x;
	double deltaY = p.y - y;
	double deltaZ = p.z - z;
	double d = Math.sqrt(sqr(deltaX) + sqr(deltaY) + sqr(deltaZ));
	double slopeX = deltaX / d;
	double slopeY = deltaY / d;
	double slopeZ = deltaZ / d;
	// determine coordinates of projection
	double x2 = x + (slopeX * dist);
	double y2 = y + (slopeY * dist);
	double z2 = z + (slopeZ * dist);
	return new Point3D(x2, y2, z2);
    }


    public Point3D projectTowards2(Point3D p, double dist) {
	Point3D v = p.minus(this);
	Point3D unitV = v.normalize();
	return unitV.mult(dist).plus(this);
    }

    public Point3D projectAway2(Point3D p, double dist) {
	Point3D v = this.minus(p);
	Point3D unitV = v.normalize();
	return unitV.mult(dist).plus(this);
    }

    /*
     * Returns a point dist away from this away from (in the opposite
     * direction of) p
     */
    public Point3D projectAway(Point3D p, double dist) {
	return projectTowards(p, -dist);
    }


    public Point3D snapToGrid() {
	return snapToGrid(defaultScale);
    }

    public Point3D snapToGrid(double scale) {
	double newX = align(x, scale);
	double newY = align(y, scale);
	double newZ = align(z, scale);
	return new Point3D(newX, newY, newZ);
    }

    public double align(double n) {
	return align(n, defaultScale);
    }

    public double align(double n, double scale) {
	return Math.round(n / scale) * scale;
    }


    int[] deltaArray(double n) {
	if (n == 0) {
	    return DELTA_ZERO;
	}
	else {
	    if (n > 0) {
		return DELTA_POS_ONE;
	    }
	    else {
		return DELTA_NEG_ONE;
	    }
	}
    }


    public boolean isCollinear(Point3D p1, Point3D p2) {
	/*
	 * The line through p1 and p2 can be described by the
	 * following parmeterized equation:
	 *
	 * p = p1 + (p2 - p1)t
	 *
	 * Thus if p0 is collinear to p1 and p2, for some t:
	 *
	 * p0 = p1 + (p2 -p1)t     and hence:
	 *
	 * x0 = x1 + (x2 - x1)t
	 * y0 = y1 + (y2 - y1)t
	 * z0 = z1 + (z2 - z1)t
	 *
	 * solving for t yields
	 *
	 * t = (x0 - x1)/(x2 - x1)
	 * t = (y0 - y1)/(y2 - y1)
	 * t = (z0 - z1)/(z2 - z1)
	 *
	 * Each of these t's must be equivalent.  Two equations
	 * expressing this can be given where division is eliminated:
	 *
	 * (x0 - x1)(y2 - y1) = (y0 - y1)(x2 - x1)
	 * (x0 - x1)(z2 - z1) = (z0 - z1)(x2 - x1)
	 *
	 * Note that if, for example, x0 = x1, then (x0 - x1) is zero and can
	 * hide the fact that 3 points are not collinear:
	 *
	 * (10, 7, 7), (10, 8, 6), and (10, 7, 6) are not collinear
	 *
	 * To get around this problem include all pairwise comparisons of t:
	 *
	 * (y0 - y1)(z2 - z1) = (z0 - z1)(y2 - y1)
	 */

	boolean collinear =
	    (x - p1.x) * (p2.y - p1.y) == (y - p1.y) * (p2.x - p1.x)
	    && (x - p1.x) * (p2.z - p1.z) == (z - p1.z) * (p2.x - p1.x)
	    && (y - p1.y) * (p2.z - p1.z) == (z - p1.z) * (p2.y - p1.y);

	return collinear;
    }

    public Point3D plus(Point3D p) {
	return new Point3D(x + p.x, y + p.y, z + p.z);
    }

    public Point3D minus(Point3D p) {
	return new Point3D(x - p.x, y - p.y, z - p.z);
    }

    public Point3D mult(double n) {
	return new Point3D(n * x, n * y, n * z);
    }


    /*
     * Interpreting points as vectors, returns the magnitude of the point.
     */
    public double magnitude() {
	return distance(x, y, z, 0, 0, 0);
    }


    /*
     * Interpretting points as vectors, returns the normalized vector.
     */
    public Point3D normalize() {
	double mag = magnitude();
	Point3D norm = new Point3D(x / mag, y / mag, z / mag);
	return norm;
    }

    /*
     * Interpreting points as vectors, returns the dot product of the
     * point and the argument p.
     */
    public double dot(Point3D p) {
	double dotProduct = (x * p.x) + (y * p.y) + (z * p.z);
	return dotProduct;
    }

    /*
     * Interpreting points as vectors, returns the cross product of
     * the point and the argument p.  If the point is q and the
     * argument is p, then qXp is returned.
     */
    public Point3D cross(Point3D p) {
	double crX = (y * p.z) - (z * p.y);
	double crY = (x * p.z) - (z * p.x);
	double crZ = (x * p.y) - (y * p.x);
	return new Point3D(crX, crY, crZ);
    }

    public double distanceToSegment(Point3D p0, Point3D p1) {
	/*
	 * Three mutually exclusive cases: 1) A perpindicular line can
	 * be drawn from the point to the line segment; 2) The angle
	 * p-p0-p1 is greater than 90 degrees where p is the current
	 * point; 3) The angle p-p1-p0 is greater than 90 degrees.
	 *
	 * In the first case the formula for the distance between a
	 * point and a line can be used.  In the second and third
	 * cases, the distance between p and the segment is
	 * respectively the distance from p to p0 or from p to p1.
	 */
	
	/*
	 * Angle p-p0-p1 > 90 degrees when cos(p-p0-p1) < 0 when the
	 * dot product of the vector from p0 to p and from p0 to p1 is
	 * negative.
	 */
	
	// degenerate case: p0=p1
	if (p0.equals(p1)) {
	    return distance(p0);
	}

	double distance;
	if (this.minus(p0).dot(p1.minus(p0)) < 0) {
	    distance = this.distance(p0);
	}
	else {
	    if (this.minus(p1).dot(p0.minus(p1)) < 0) {
		distance = this.distance(p1);
	    }
	    else {
		distance = this.distanceToLine(p0, p1);
	    }
	}
	return distance;
    }


    /* 
     * Returns true if the point can contained in a cylinder with
     * axial end points given by the point arguments.
     *
     * The computation uses ideas from the distanceToCylinder method
     * above.
     */
    public boolean canBeCapturedByCylinder(Point3D p0, Point3D p1) {
	boolean canBeCaptured;
	if (p0.equals(p1)) {
	    die("[Point3D.canBeCapturedByCylinder] Arguments must be different: " + p0);
	}
	if (this.minus(p0).dot(p1.minus(p0)) < 0 || this.minus(p1).dot(p0.minus(p1)) < 0) {
	    canBeCaptured = false;
	}
	else {
	    canBeCaptured = true;
	}
	return canBeCaptured;
    }


    public boolean withinCylinder(Point3D p0, Point3D p1, double radius) {
	boolean within;
	if (canBeCapturedByCylinder(p0, p1)) {
	    within = (distanceToLine(p0, p1) <= radius);
	}
	else {
	    within = false;
	}
	return within;
    }


    private static void testDistanceToSegment() {
	Point3D p0 = new Point3D(0, 0, 0);
	Point3D p1 = new Point3D(5, 0, 0);

	Point3D p2 = new Point3D(-1, 1, 0);
	Point3D p3 = new Point3D(2 , 1, 0);
	Point3D p4 = new Point3D(6, 1, 0);
	
	System.out.println(p2.distanceToSegment(p0, p1));
	System.out.println(p3.distanceToSegment(p0, p1));
	System.out.println(p4.distanceToSegment(p0, p1));
    }


    /*
     * Returns the shortest distance between the point and the line
     * defined b y the arguments p1 and p2.
     *
     * As described at: 
     * mathworld.wolfram.com/Point-LineDistance3-Dimensional.html
     */
    public double distanceToLine(Point3D p1, Point3D p2) {
	double dist =
	    this.minus(p1).cross(this.minus(p2)).magnitude()
	    / p2.minus(p1).magnitude();
	return dist;
    }
	    



    // It seems that this approach traverses diagonals until it can
    // travel parallel to an axis to the target
    public LinkedList<Point3D> gridPath(Point3D target) {
	double currentX = align(x);
	double currentY = align(y);
	double currentZ = align(z);

	double targetX = align(target.x);
	double targetY = align(target.y);
	double targetZ = align(target.z);

	int[] deltaX = deltaArray(targetX - currentX);
	int[] deltaY = deltaArray(targetY - currentY);
	int[] deltaZ = deltaArray(targetZ - currentZ);


	LinkedList<Point3D> path = new LinkedList<Point3D>();
	while (currentX != targetX
	       || currentY != targetY
	       || currentZ != targetZ) {
	    // find next point in path
	    double minDistance = Double.POSITIVE_INFINITY;
	    double nextX = 0;
	    double nextY = 0;
	    double nextZ = 0;
	    for (int i = 0; i < deltaX.length; i ++) {
		for (int j = 0; j < deltaY.length; j ++) {
		    for (int k = 0; k < deltaZ.length; k ++) {
			double candX = currentX + deltaX[i];
			double candY = currentY + deltaY[j];
			double candZ = currentZ + deltaZ[k];
			double dist = distance(targetX, targetY, targetZ,
					       candX, candY, candZ);
			if (dist < minDistance) {
			    minDistance = dist;
			    nextX = candX;
			    nextY = candY;
			    nextZ = candZ;
			}
		    }
		}
	    }
	    path.add(new Point3D(nextX, nextY, nextZ));
	    currentX = nextX;
	    currentY = nextY;
	    currentZ = nextZ;
	}
	return path;
    }

    // Returns the result of rotating the point around the z-axis in
    // the direction from the positive x-axis to the positive y-axis.
    public Point3D rotateZ(double angleRadians) {
	double sin = Math.sin(angleRadians);
	double cos = Math.cos(angleRadians);

	double newX = (x * cos) - (y * sin);
	double newY = (y * cos) + (x * sin);

	return new Point3D(newX, newY, z);
    }

    private static void testRotateZ() {
	Point3D p;
	Point3D q;
	Point3D expected;
	double angleRadians;
	double tolerance = .0000000001;

	p = new Point3D(1, 1, 100);
	angleRadians = Math.PI / 2.0;
	q = p.rotateZ(angleRadians);
	expected = new Point3D(-1, 1, 100);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

	p = new Point3D(1, 0, 100);
	angleRadians = Math.PI / 2.0;
	q = p.rotateZ(angleRadians);
	expected = new Point3D(0, 1, 100);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

	p = new Point3D(1, 0, 100);
	angleRadians = Math.PI;
	q = p.rotateZ(angleRadians);
	expected = new Point3D(-1, 0, 100);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

	p = new Point3D(1, 0, 100);
	angleRadians = Math.PI / 2.0;
	q = p.rotateZ(angleRadians);
	expected = new Point3D(0, 1, 100);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

	p = new Point3D(1, 0, 100);
	angleRadians = Math.PI / 4.0;
	q = p.rotateZ(angleRadians);
	expected = new Point3D(Math.sqrt(.5), Math.sqrt(.5), 100);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

    }

    // Returns the result of rotating the point around the y-axis in
    // the direction from the positive z-axis to the positive x-axis.
    public Point3D rotateY(double angleRadians) {
	double sin = Math.sin(angleRadians);
	double cos = Math.cos(angleRadians);

	double newZ = (z * cos) - (x * sin);
	double newX = (x * cos) + (z * sin);

	return new Point3D(newX, y, newZ);
    }

    
    // Returns the result of rotating the point around the x-axis in
    // the direction from the positive z-axis to the positive y-axis.
    public Point3D rotateX(double angleRadians) {
	double sin = Math.sin(angleRadians);
	double cos = Math.cos(angleRadians);

	double newZ = (z * cos) - (y * sin);
	double newY = (y * cos) + (z * sin);

	return new Point3D(x, newY, newZ);
    }


    private static void testRotateY() {
	Point3D p;
	Point3D q;
	Point3D expected;
	double angleRadians;
	double tolerance = .0000000001;

	p = new Point3D(1, 100, 0);
	angleRadians = Math.PI / 2.0;
	q = p.rotateY(angleRadians);
	expected = new Point3D(0, 100, -1);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

	p = new Point3D(-1, 100, 1);
	angleRadians = Math.PI / 2.0;
	q = p.rotateY(angleRadians);
	expected = new Point3D(1, 100, 1);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

    }

    
    private static void testRotateX() {
	Point3D p;
	Point3D q;
	Point3D expected;
	double angleRadians;
	double tolerance = .0000000001;

	p = new Point3D(100, -1, -1);
	angleRadians = Math.PI / 2.0;
	q = p.rotateX(angleRadians);
	expected = new Point3D(100, -1, 1);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

	p = new Point3D(100, 0, 1);
	angleRadians = Math.PI / 2.0;
	q = p.rotateX(angleRadians);
	expected = new Point3D(100, 1, 0);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

	p = new Point3D(100, 0, 1);
	angleRadians = Math.PI;
	q = p.rotateX(angleRadians);
	expected = new Point3D(100, 0, -1);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

	p = new Point3D(100, 0, 1);
	angleRadians = Math.PI / 2.0;
	q = p.rotateX(angleRadians);
	expected = new Point3D(100, 1, 0);
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

	p = new Point3D(100, 0, 1);
	angleRadians = Math.PI / 4.0;
	q = p.rotateX(angleRadians);
	expected = new Point3D(100, Math.sqrt(.5), Math.sqrt(.5));
	
	if (Math.abs(q.x - expected.x) > tolerance
	    || Math.abs(q.y - expected.y) > tolerance
	    || Math.abs(q.z - expected.z) > tolerance) {
	    System.out.println("FAIL: " + p + " rotated by " + angleRadians +
			       " yielded " + q + "    expected " + expected);
	}
	else {
	    System.out.println("PASSED points within " + tolerance);
	}

    }

    public double angleDegrees(Point3D v) {
	return angleRadians(v) * 180.0 / Math.PI;
    }

    // Returns the angle formed by the point, the origin as vertex,
    // and the argument.
    public double angleRadians(Point3D v) {
	double cosine = this.dot(v) / (this.magnitude() * v.magnitude());
	// due to mathematical imprecision, the computed cosine may be
	// a bit less than -1 or greater than 1 which are not in the
	// range of the cosine function.  Force the values back to -1
	// or as necessary.
	cosine = Math.max(-1, Math.min(cosine, 1));
	double angle = Math.acos(cosine);
	if (Double.isNaN(angle)) {
	    die("[Point3D.angleRadians] Unable to compute angle between "
		+ this + " and " + v);
	}
	return angle;
    }
	

    private static void testAngleDegrees2() {
	Point3D p = new Point3D(1, 1, 1);
	Point3D q = new Point3D(1, 2, 1);
	Point3D r = new Point3D(2, 1, 1);

	double tolerance = 0.0000000000001;
	double degrees = p.angleDegrees(q, r);
	if (Math.abs(degrees - 90) > tolerance) {
	    die("[Point3D.testAngleDegrees2] degrees=" + degrees
		+ " expected 90");
	}
	degrees = p.angleDegrees(r, q);
	if (Math.abs(degrees - 90) > tolerance) {
	    die("[Point3D.testAngleDegrees2] degrees=" + degrees
		+ " expected 90");
	}

	r = new Point3D(2, 2, 1);
	degrees = p.angleDegrees(r, q);
	if (Math.abs(degrees - 45) > tolerance) {
	    die("[Point3D.testAngleDegrees2] degrees=" + degrees
		+ " expected 45");
	}
	degrees = p.angleDegrees(q, r);
	if (Math.abs(degrees - 45) > tolerance) {
	    die("[Point3D.testAngleDegrees2] degrees=" + degrees
		+ " expected 45");
	}

	System.out.println("[Point3D.testAngleDegrees2] passed");
    }

    public double angleDegrees(Point3D q, Point3D r) {
	return angleRadians(q, r) * 180.0 / Math.PI;
    } 

    // Returns the angle formed by q and r when the point is
    // used as a vertex, i.e. if the point is p, then the angle
    // between pq and pr is returned.
    public double angleRadians(Point3D q, Point3D r) {
	Point3D pq = q.minus(this);
	Point3D pr = r.minus(this);
	return pq.angleRadians(pr);
    }

    private static void testIsCollinear() {
	Point3D p1 = new Point3D(5.126748621482023, 63.89801426911933, -30.824498838802775);
	Point3D p2 = new Point3D(8.258383281513414, 61.983737538487354, -26.97176685338559);
	Point3D p3 = new Point3D(-0.8743661532943046, 67.56632031957378, -38.207443962008384);
	System.out.println("*** " + p1.isCollinear(p2, p3));
    }


    private static void testXYDistance() {
	Point3D p = new Point3D(0, 0, 9);
	Point3D q = new Point3D(3, 4, 0);
	System.out.println("[Point3D.testXYDistance] xyDistance between "
			   + p + " and " + q
			   + " is " + p.xyDistance(q));
    }


    private static void testNextXYIntersection() {
	System.out.println("[Point3D.testNextXYIntersection] Begin testing");
	Point3D p;
	Point3D q;
	Point3D center;
	double radius;
	Point3D intersection;
	Point3D expected;
	p = new Point3D(0, 1, 5);
	q = new Point3D(10, 1, 4);
	center = new Point3D(3, 1, 7);
	radius = 2;
	expected = new Point3D(1, 1, 0);
	intersection = nextXYIntersection(p, q, center, radius);
	if (!intersection.equals(expected)) {
	    die("p=" + p + " q=" + q + " center=" + center + " radius="
		+ radius + " nextXYIntersection(p, q, center, radius)="
		+ intersection + " expected: " + expected);
	}

	p = new Point3D(10, 1, 4);
	q = new Point3D(0, 1, 5);
	center = new Point3D(3, 1, 7);
	radius = 2;
	expected = new Point3D(5, 1, 0);
	intersection = nextXYIntersection(p, q, center, radius);
	if (!intersection.equals(expected)) {
	    die("p=" + p + " q=" + q + " center=" + center + " radius="
		+ radius + " nextXYIntersection(p, q, center, radius)="
		+ intersection + " expected: " + expected);
	}

	center = new Point3D(-2, -3, -1);
	radius = 5;
	p = new Point3D(-2, -1, 7);
	q = new Point3D(-2, 13, 100);
	expected = new Point3D(-2, 2, 0);
	intersection = nextXYIntersection(p, q, center, radius);
	if (!intersection.equals(expected)) {
	    die("p=" + p + " q=" + q + " center=" + center + " radius="
		+ radius + " nextXYIntersection(p, q, center, radius)="
		+ intersection + " expected: " + expected);
	}

	center = new Point3D(-2, -3, -1);
	radius = 5;
	p = new Point3D(-2, 13, 100);
	q = new Point3D(-2, -1, 7);
	expected = new Point3D(-2, 2, 0);
	intersection = nextXYIntersection(p, q, center, radius);
	if (!intersection.equals(expected)) {
	    die("p=" + p + " q=" + q + " center=" + center + " radius="
		+ radius + " nextXYIntersection(p, q, center, radius)="
		+ intersection + " expected: " + expected);
	}

	center = new Point3D(0, 0, -1);
	radius = 5;
	p = new Point3D(0, 5, 99);
	q = new Point3D(0, 10, 3);
	expected = null;
	intersection = nextXYIntersection(p, q, center, radius);
	if (intersection != expected) {
	    die("p=" + p + " q=" + q + " center=" + center + " radius="
		+ radius + " nextXYIntersection(p, q, center, radius)="
		+ intersection + " expected: " + expected);
	}

	center = new Point3D(0, 0, -1);
	radius = 5;
	p = new Point3D(-5, 0, 99);
	q = new Point3D(10, 0, 3);
	expected = new Point3D(5, 0, 0);
	intersection = nextXYIntersection(p, q, center, radius);
	if (!expected.equals(intersection)) {
	    die("p=" + p + " q=" + q + " center=" + center + " radius="
		+ radius + " nextXYIntersection(p, q, center, radius)="
		+ intersection + " expected: " + expected);
	}



	center = new Point3D(0, 0, -1);
	radius = 5;
	p = new Point3D(-5, 0, 99);
	q = new Point3D(0, 5, 3);
	expected = q;
	intersection = nextXYIntersection(p, q, center, radius);
	if (!expected.equals(q)) {
	    die("p=" + p + " q=" + q + " center=" + center + " radius="
		+ radius + " nextXYIntersection(p, q, center, radius)="
		+ intersection + " expected: " + expected);
	}

	center = new Point3D(0, 0, 0);
	radius = 60;
	p = new Point3D(52.22601099844082, -29.53715922682373, 0);
	q = new Point3D(58.835804318638004, -26.262963917247703, 57.12289329435853);
	
	double m = (q.y - p.y) / (q.x - p.x);
	// p.y = (m * p.x) + b
	double b = p.y - (m * p.x);

	if (p.y != (m * p.x) + b) {
	    die("[point3D.testNextXYIntersection] "
		+ "p not consistent with equation: p=" + p
		+ " m=" + m + " b=" + b + " mp.x+b=" + ((m * p.x) + b));
	}
	if (q.y != (m * q.x) + b) {
	    die("[point3D.testNextXYIntersection] "
		+ "q not consistent with equation: q=" + q
		+ " m=" + m + " b=" + b + " mq.x+b=" + ((m * q.x) + b));
	}

	// y = mx + b   r^2 =x^2 + y^2
	// r^2 = x^2 + (mx + b)^2
	// r^2 = x^2 + m^2x^2 + 2mbx + b^2
	// (m^2 + 1)x^2 + 2mbx + b^2 - r^2 = 0
	// x = (-2mb +- sqrt(4m^2b^2 - 4(m^2 + 1)(b^2 - r^2))) / 2(m^2 + 1)

	double quadA = (m * m) + 1;
	double quadB = 2 * m * b;
	double quadC = (b * b) - (radius * radius);
	double discriminant = (quadB * quadB) - (4 * quadA * quadB);
	double x1 = (-quadB + Math.sqrt(discriminant)) / (2 * quadA);
	double x2 = (-quadB - Math.sqrt(discriminant)) / (2 * quadA);
	
	// since p.x < q.x
	if (((x1 > q.x || x1 < p.x)
	     && (x1 > q.x || x1 < p.x))
	    ||
	    ((x1 <= q.x || x1 >= p.x)
	     && (x1 <= q.x || x1 >= p.x))) {
	    die("[Point3D.testNextXYIntersection] Unable to find intersection"
		+ " x1=" + x1 + " x2=" + x2);
	}
	double x = (x1 >= p.x && x1 <= q.x)? x1 : x2;
	double y = (m * x) + b;

	expected = new Point3D(x, y, 0);
	intersection = nextXYIntersection(p, q, center, radius);
	if (!expected.equals(q)) {
	    die("p=" + p + " q=" + q + " center=" + center + " radius="
		+ radius + " nextXYIntersection(p, q, center, radius)="
		+ intersection + " expected: " + expected);
	}



	System.out.println("[Point3D.testNextXYIntersection] End testing");
    }
    
    // Returns the first intersection point of line segment pq (after
    // p and moving from p to q) and a circle when both are projected
    // onto the xy-plane (z=0).  If no such intersection exists, then
    // null is returned.
    //
    // It is important that p is not returned.  This allows the
    // iteration of locating intersection points.  Any resulting
    // intersection point: a) is closer to q than p is, or b) is p if
    // p=q and p is on the circle.
    public static Point3D nextXYIntersection(Point3D p,
					     Point3D q,
					     Point3D center,
					     double radius) {
	// Equation for circle: 
	// (x - center.x)^2 + (y - center.y)^2 = radius^2
	//
	// Parameterized line formula:
	// x = p.x + (dx)t
	// y = p.y + (dy)t
	//
	// where:
	// dx = q.x - p.x
	// dx = q.y - p.y
	//
	// Note that when t = 0, x = p.x and y = p.y,
	// and when t = 1, x = q.x, and y = q.y.  Thus
	// a point is on the line segment exactly when it
	// can be parameterized by a t such that 0 <= t
	// and t <= 1.
	//
	// Substituting the line equation into the circle
	// equation yields:
	//
	// (p.x + (dx)t - center.x)^2 + (p.y + (dy)t - center.y)^2 = radius^2
	//
	// p.x^2 + (dx^2)t^2 + center.x^2 + 2p.x(dx)t + -2p.x(center.x)
	// + -2t(dx)center.x
	// + p.y^2 + (dy^2)t^2 + center.y^2 + 2p.y(dy)t + -2p.y(center.y)
	// + -2t(dy)center.y
	// - radius^2
	// = 0
	//
	// (dx^2 + dy^2)t^2
	// + 2(p.x(dx) + -(dx)center.x + p.y(dy) + -(dy)center.y)t
	// + p.x^2 + center.x^2 + -2p.x(center.x)
	// + p.y^2 + center.y^2 + -2p.y(center.y) 
	// - radius^2
	// = 0
	// 
	// Let:
	// a = (dx^2 + dy^2)
	// b = 2(p.x(dx) + -(dx)center.x + p.y(dy) + -(dy)center.y)
	// c = p.x^2 + center.x^2 + -2p.x(center.x)
	//     + p.y^2 + center.y^2 + -2p.y(center.y) 
	//     - radius^2
	//
	
	double dx = q.x - p.x;
	double dy = q.y - p.y;
	
	double a = sqr(dx) + sqr(dy);
	double b =
	    2 * ((p.x * dx) + -(dx * center.x)
		 + (p.y * dy) + -(dy * center.y));
	double c =
	    sqr(p.x) + sqr(center.x) + (-2 * p.x * center.x) + sqr(p.y)
	    + sqr(center.y) + (-2 * p.y * center.y) - sqr(radius);
	
	double discriminant = sqr(b) - (4 * a * c);
	if (discriminant < 0) {
	    // there is no real solution and hence no intersection
	    return null;
	}
	double rootDiscriminant = Math.sqrt(discriminant);
	double t1 = (-b + rootDiscriminant) / (2 * a);
	double t2 = (-b - rootDiscriminant) / (2 * a);

	// t values between 0 an 1 indicate points on the line
	// segment.  Want t values > 0 because p should not be
	// returned (unless p=q).
	boolean t1InBounds = (t1 > 0) && (t1 <= 1);
	boolean t2InBounds = (t2 > 0) && (t2 <= 1);
	if ((!t1InBounds) && (!t2InBounds)) {
	    // The intersection points are not between p and q
	    return null;
	}

	double t;
	double nextBest = 0;
	boolean nextBestExists = false;
	// Use the smallest of t1 and t2 that is between 0 and 1
	if (t1InBounds) {
	    if (t2InBounds) {
		t = Math.min(t1, t2);
		nextBestExists = true;
		nextBest = Math.max(t1, t2);
	    }
	    else {
		t = t1;
	    }
	}
	else {
	    t = t2;
	}
	
	Point3D intersection =
	    new Point3D(p.x + (dx * t), p.y + (dy * t), 0);
	Point3D nextBestIntersection = null;
	if (nextBestExists) {
	    nextBestIntersection =
		new Point3D(p.x + (dx * nextBest), p.y + (dy * nextBest), 0);
	}
	// If the intersection is too close to p, then when this
	// method is iterated in order to find the next intesection,
	// it is possible that the same intersection point will be
	// found, and hence a process that iterates this method may
	// not halt.  To prevent this, force the intersection to be
	// significantly different than p.
	double acceptableDifference = 0.00000001;
	if (Math.abs(intersection.x - p.x) < acceptableDifference
	    && Math.abs(intersection.y - p.y) < acceptableDifference) {
	    intersection = null;
	    if (nextBestExists) {
		if (Math.abs(nextBestIntersection.x - p.x)
		    >= acceptableDifference
		    || Math.abs(nextBestIntersection.y - p.y)
		    >= acceptableDifference) {
		    intersection = nextBestIntersection;
		}
	    }
	}
		

	return intersection;
    }
    
    
    public static void main(String[] args) {

	testProjectTowards();
	if (true) {return;}

	testAngleDegrees2();
	if (true) {return;}

	testNextXYIntersection();
	if (true) {return;}

	testXYDistance();
	if (true) {return;}

	testIsCollinear();
	if (true) {return;}

	System.out.println("Begin testRotateY");
	testRotateY();

	System.out.println("Begin testRotateZ");
	testRotateZ();

	System.out.println("Begin testRotateX");
	testRotateX();
	if (true) {return;}

	testDistanceToSegment();
	if (true) {return;}

	Point3D p1;
	Point3D p2;
	Point3D p3;

	p1 = new Point3D(1, 2, 3);
	System.out.println(p1.isAligned() + "  " + p1);
	p1 = new Point3D(1.1, 2, 3);
	System.out.println(p1.isAligned() + "  " + p1);
	p1 = new Point3D(1, 2.1, 3);
	System.out.println(p1.isAligned() + "  " + p1);
	p1 = new Point3D(1, 2, 3.1);
	System.out.println(p1.isAligned() + "  " + p1);
	p1 = new Point3D(1.1, 2.1, 3);
	System.out.println(p1.isAligned() + "  " + p1);
	p1 = new Point3D(1.1, 2, 3.1);
	System.out.println(p1.isAligned() + "  " + p1);
	p1 = new Point3D(1, 2.1, 3.1);
	System.out.println(p1.isAligned() + "  " + p1);
	p1 = new Point3D(1.1, 2.1, 3.1);
	System.out.println(p1.isAligned() + "  " + p1);
	
	if (true) {return;}

	p1 = new Point3D(0, 0, 0);
	p2 = new Point3D(1, 0, 0);
	p3 = new Point3D(2, -1, 0);

	System.out.println(p3.distanceToSegment(p2, p1));

	if (true){return;}

	p1 = new Point3D(10, 7, 7);
	p2 = new Point3D(10, 8, 6);
	p3 = new Point3D(10, 7, 6);
	System.out.println("[Point3D.main] " + p1 + " " + p2 + " " + p3);
	System.out.println(p1.isCollinear(p2, p3));

	System.out.println(p1.isCollinear(p2, p3));
	System.out.println(p1.isCollinear(p3, p2));

	System.out.println(p2.isCollinear(p1, p3));
	System.out.println(p2.isCollinear(p3, p1));


	System.out.println(p3.isCollinear(p1, p2));
	System.out.println(p3.isCollinear(p2, p1));

	if(true){return;}

	Point3D ap = new Point3D(0, 0, 0);
	Point3D aq = new Point3D(3, 5, 0);

	LinkedList<Point3D> path = ap.gridPath(aq);
	for (Iterator<Point3D> i = path.iterator(); i.hasNext();) {
	    Point3D a = i.next();
	    System.out.println(a);
	}
	System.out.println();


	Point3D p = new Point3D(1.3, 2.7, 3.9);
	System.out.println(p.snapToGrid());
	System.out.println(p.snapToGrid(.5));
	System.out.println(p.snapToGrid(.25));
	System.out.println(p.snapToGrid(2));
	
	System.out.println();

	p = new Point3D(5, 5, 5);
	Point3D q = new Point3D(3, 3, 3);
	System.out.println(p.projectTowards(q, Math.sqrt(3)));
	System.out.println(p.projectAway(q, Math.sqrt(3)));

    }

    

}
