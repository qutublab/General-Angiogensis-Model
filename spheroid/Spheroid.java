
package spheroid;

/*
 * It may be necessary to use a point other than the center of the
 * inner spheres.  Two candidates are: 1) the projection of a line
 * passing through the spheroid center and an inner sphere center onto
 * the inner sphere; and 2) the projection of a line passing through
 * the spheroid center and an inner sphere center onto the spheroid.
 *
 * In the first case, the intersection of the projected line and the
 * sphere will yield two points; the one further away from either
 * center would be used.  Similarly for the second case, the
 * intersection yields two points, and the further point would be
 * used.
 *
 * At 3-D line is represented by the parameterized equations:
 *
 *  x = at + x_0
 *  y = bt + y_0
 *  x = ct + z_0
 *
 * where: 1) (x_0, y_0, z_0) is a point on the line, 2) a, b, and c
 * are constants, and 3) t is a free parameter.  Given two points on a
 * line, the equations become:
 *
 *  x = (x_1-x_0)t + x_0
 *  y = (y_1-y_0)t + y_0
 *  x = (z_1-z_0)t + z_0
 *
 * Thus, when t=0, (x,y,z) = (x_0,y_0,z_0) and when t=1, (x,y,z) =
 * (x_1,y_1,z_1)
 *
 * Given a line represented as above, the intersection with the sphere
 * (x-x_1)^2 + (y-y_1)^2 + (z-z_1)^2 = r^2 (centered at x_1,y_1,z_1)
 * is expressed by substituting the parameterized line equations into
 * the sphere equation:
 *
 *  ((x_1-x_0)t + x_0 - x_1)^2
 *    + ((y_1-y_0)t + y_0 - y_1)^2
 *    + ((z_1-z_0)t + z_0 - z_1)^2
 *  = r^2
 *
 * which yields a quadratic equation in variable t.  Solving yields
 * two values of t -- each value representing a point of intersection.
 */


import shared.*;
import java.util.*;

import java.io.*;

/*
 * Assume a spheroid centered at the origin of a cartesian grid.
 * Spheres are packed so that: 1) there is a sphere whose center is
 * the origin; and 2) each sphere is entirely within the spheroid.
 */


public class Spheroid {

    private static final double SIN_60_DEGREES = Math.sqrt(.75);
    private static final double SQRT_6 = Math.sqrt(6);

    // A sphere can have at most 12 neighbor spheres touching it
    private static final int MAXIMUM_NUMBER_OF_NEIGHBORS = 12;



    private static double spheroidDiameter = 120;
    private static double sphereDiameter =13;

    //    private static double SPHEROID_DIAMETER = 120;
    //    static{
    //	System.out.println("[Spheroid] spheroid diameter set to " + SPHEROID_DIAMETER);
    //    }
    //    private static double SPHERE_DIAMETER = 13;


    private static double spheroidRadius = spheroidDiameter / 2;
    private static double sphereRadius = sphereDiameter / 2;


    //    private static final double SPHERE_DIAM_DIV_2 = sphereDiameter / 2.0;

    private static double sin60Diam = SIN_60_DEGREES * sphereDiameter;
    private static double sin60DiamDiv3 = sin60Diam / 3.0;
    private static double sqrt6DiamDiv3 = (SQRT_6 * sphereDiameter) / 3.0;



    private static boolean specialCompare = false;



    public static class SphereComparator implements Comparator {
	public int compare(Object o1, Object o2) {
	    Sphere s1 = (Sphere) o1;
	    Sphere s2 = (Sphere) o2;
	    if (specialCompare) {
		return s1.neighborCount - s2.neighborCount;
	    }
	    int comp;
	    double diff = s1.zCoordRounded - s2.zCoordRounded;
	    if (diff == 0) {
		diff = s1.yCoordRounded - s2.yCoordRounded;
		if (diff == 0) {
		    diff = s1.xCoordRounded - s2.xCoordRounded;
		}
	    }
	    if (diff < 0) {
		comp = -1;
	    }
	    else {
		if (diff == 0) {
		    comp = 0;
		}
		else {
		    comp =1;
		}
	    }
	    return comp;
	}

	public boolean equals(Object o) {
	    return o == this;
	}
    }
    


    public static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }

    private static void packRow(LinkedList<Sphere> sphereList,
				double radius,
				double initX, double limitX,
				double y, double z) {
	double diameter = 2 * radius;
	for (double x = initX; x <= limitX; x += diameter) {
	    Sphere s = new Sphere();
	    s.xCoord = x;
	    s.yCoord = y;
	    s.zCoord = z;
	    s.radius = radius;
	    s.xCoordRounded = round(x);
	    s.yCoordRounded = round(y);
	    s.zCoordRounded = round(z);
	    sphereList.add(s);
	}
    }

    /*
     * Given a circle x^2 + y^2 = rbig^2 and value b, find the
     * x-coordinate of the center of a smaller circle with radius
     * rsmall < rbig whose center has y-coordinate b, is tangent to
     * the first circle, and lies within the first circle.
     *
     * The smaller circle can be described as:
     *
     * (x - a)^2 + (y - b)^2 = rsmall^2
     *
     * The desired x-coordinate is the constant a.  It is found by
     * solving the pair of equations for x yielding a quadratic
     * equation with unknown a.  Since the circles are tangent, the
     * quadratic formula should yield a single value which it does
     * when its determinant is 0.  This gives a new formula for
     * unknown a.  In this formula, a is raised to the fourth power
     * meaning that there can be up to 4 circles with a center
     * y-coordinate of b that are tangent to the larger circle.  Two
     * of these smaller circles will be _outside_ of the larger
     * circle. Since the larger circle is centered at the origin,
     * there can be up to two smaller circles inside of it with
     * centers at (a,b) and (-a,b).  When a=0, there is only one
     * smaller circle.
     */

    private static double rightmostInnerCircle(double rbig, double rsmall, double b) {
        double rbig2 = rbig * rbig;
        double rsmall2 = rsmall * rsmall;
        double b2 = b * b;
	
        double c = rbig2 + b2 - rsmall2;
        double c2 = c * c;
	
	/*
	 * Variables qa, qb, and qc are a, b, anc c as given in the
	 * quadratic formula.  Their values come from solving the
	 * above circle equations and forcing the intersection to be a
	 * tangent.
	 */

        double qa = 1;
        double qb = (2 * c) - (4 * rbig2);
        double qb2 = qb * qb;
        double qc = c2 - (4 * rbig2 * b2);
        double determinant = qb2 - (4 * qa * qc);

	double baseValue = -qb / (2 * qa);
	double plusMinusValue = Math.sqrt(determinant) / (2 * qa);

	// Note that the above yields a solution for a^2 (not a) !!

	/*
	 * The two values from the solution of the quadratic formula
	 * are baseValue + plusMinusValue and baseValue -
	 * plusMinusValue.  The second should yield the desired value
	 * for a, but just in case, make sure that one actually
	 * corresponds to a center within the larger circle.
	 */
	
	double a = -1;
	double[] candidates = {baseValue + plusMinusValue, baseValue - plusMinusValue};
	for (int i = 0; i < candidates.length; i ++) {
	    double possibleA = Math.sqrt(candidates[i]);
	    double distance = Math.sqrt(possibleA * possibleA + b2);
	    if (distance <= rbig) {
		a = possibleA;
		break;
	    }
	}
	return a;
    }
    


    private static void packTier(LinkedList<Sphere> sphereList,
				 double spheroidRadius,
				 double radius,
				 double initX, double initY,
				 double z) {
	double r2 = spheroidRadius * spheroidRadius;
	double z2 = z * z;
	double tierRadius = Math.sqrt(r2 - z2);

	double diameter = 2 * radius;
	double deltaY = SIN_60_DEGREES * (diameter);

	/*
	 * Set initY to the least possible y-coordinate reachable from
	 * the current value of initY.  Adjust initX accordingly since
	 * each row is offset by one radius from the row above and
	 * below it.
	 */
	int ySteps = (int) Math.floor((initY + (tierRadius - radius)) / deltaY);
	initY = initY - (ySteps * deltaY);
	initX = initX + ((ySteps % 2) * radius);
	

	/*
	 * Pack the rows that have y-coordinates greater than or equal
	 * to initY.
	 */
	boolean done = false;
	for (double y = initY; y + radius <= tierRadius && !done; y += deltaY) {
	    //	    System.out.println("z=" + z + " y=" + y + " radius=" + radius
	    //			       + " tierRadius=" + tierRadius);
	    /*
	     * Find the x-coordinate of the rightmost and leftmost
	     * spheres on row y by finding the centers of rightmost
	     * and leftmost sphere circumferences that are tangent to
	     * the tier circle
	     */
	    double rightXLimit = rightmostInnerCircle(tierRadius, radius, y);
	    double leftXLimit = -rightXLimit;
	    
	    /* 
	     * set initX to the leftmost possible value
	     */
	    int xSteps = (int) Math.floor((initX - leftXLimit) / diameter);
	    initX = initX - (xSteps * diameter);
	    if (initX > rightXLimit) {
		done = true;
	    }
	    packRow(sphereList, radius, initX, rightXLimit, y, z);
	    // set initX for next row of tier
	    initX += radius;
	}

	/* 
	 * The center of the spheres placed in the tier will be in a
	 * plane parallel to the x-y plane at height z.  Thus the
	 * spheres must lie in a circle determined by the cross
	 * section of the spheroid at z.  The center of the spheroid
	 * is (0,0,0), so the circle is defined by the equation:
	 *
	 *             x^2 + y^2 = r^2 - z^2
	 *
	 * where r is the spheroid radius and z is the method
	 * parameter z.
	 */


    }
	

    public static LinkedList<Sphere> packSphere(double bigRadius,
						double smallRadius) {
	LinkedList<Sphere> sphereList = new LinkedList<Sphere>();
	double diameter = 2 * smallRadius;
	double deltaY = (SIN_60_DEGREES * diameter) / 3;
	double deltaZ = (Math.sqrt(6) / 3) * diameter;

	/*
	 * Set coordinates so that a small sphere is placed at the
	 * center of the large sphere.
	 */
	double initX = 0;
	double initY = 0;
	double initZ = 0;
	
	/* 
	 * Start at the z=0 plane and continue upwards.
	 */
	double x = initX;
	double y = initY;
	for (double z = initZ; z <= bigRadius; z += deltaZ) {
	    packTier(sphereList, bigRadius, smallRadius, x, y, z);
	    x += smallRadius;
	    /*
	     * Use ABC packing pattern, so each y always changes by
	     * 1/3 of the height of the icosoles triangle defined by
	     * the center of three planar small spheres.
	     */
	    y += deltaY;
	}

	/*
	 * Start below the z=0 plane and work downwards
	 */

	x = initX + smallRadius;
	y = initY - deltaY;
	for (double z = initZ - deltaZ; z > -bigRadius; z -= deltaZ) {
	    packTier(sphereList, bigRadius, smallRadius, x, y, z);
	    x += smallRadius;
	    y -= deltaY;
	}	

	return sphereList;
 }
				 
    private static double sphereVolume(double radius) {
	return (4.0 / 3.0) * Math.PI * Math.pow(radius, 3);
    }

    private static double round(double n) {
	long factor = Long.parseLong("10000000000");
	double rnd = Math.round(n * factor);
	double n2 =  rnd / factor;
	return n2;
    }


    //    private static void removeHidden(LinkedList<Sphere> sphereList)



    /*
     * Counting Neighbors
     *
     * Each sphere has 12 potential neighboring spheres: 6 surrounding
     * it on its tier, 3 on the tier above it and 3 on the tier below
     * it.
     *
     * Since the spheres are packed into a larger sphere, a sphere is
     * considered to be on the perimiter of the larger sphere if it
     * has less than 12 neighbors.
     *
     * The 6 potential neighbors on the same tier of a sphere centered
     * at (x,y,z) are: the two flanking positions (x+d,y,z) and
     * (x-d,y,z); the two forward positions (x+d/2,y+sin60*d,z) and
     * (x-d/2,y+sin60*d,z); and the two rear positions
     * (x+d/2,y-sin60*d,z) and (x-d/2,y-sin60*d,z).
     *
     * The 3 potential neighbors on the tier above and the 3 potential
     * neighbors on the tier above must use the B and C patterns
     * (assuming the tier of the sphere in question is in pattern A).
     * The sets of x-y coordinates of these spheres are:
     *
     *  (x,y-(sin60*d/3)+sin60*d) = (x,y+sin60*2d/3)
     *  (x-d/2,y-sin60*d/3)
     *  (x+d/2,y-sin60*d/3)
     *
     *  (x,y-sin60*2d/3)
     *  (x-d/2,y+sin60*d/3)
     *  (x+d/2,y+sin60*d/3)
     *
     *  Each set of coordinates represents either the three potenitial
     *  neighbors above or below the sphere in question.  The
     *  z-coordinate of the tiers immediately above and below are:
     *  z+sqrt(6)*d/3 and z-sqrt(6)*d/3.
     *
     *
     */


    /*
     * Returns the locations of the centers of potential immediately
     * neighboring spheres on the same tier (z-coordinate) as Sphere
     * argument s.
     */
    private static Point3D[] sameTierNeighborLocations(Sphere s) {
	Point3D[] loc = new Point3D[6];
	Point3D p;
	int index = 0;

	p = new Point3D(round(s.xCoord + sphereDiameter), s.yCoordRounded, s.zCoordRounded);
	loc[index++] = p;

	p = new Point3D(round(s.xCoord - sphereDiameter), s.yCoordRounded, s.zCoordRounded);
	loc[index++] = p;

	p = new Point3D(round(s.xCoord + (sphereRadius)),
			round(s.yCoord + sin60Diam),
			s.zCoordRounded);
	loc[index++] = p;

	p = new Point3D(round(s.xCoord - (sphereRadius)),
			round(s.yCoord + sin60Diam),
			s.zCoordRounded);
	loc[index++] = p;

	p = new Point3D(round(s.xCoord + (sphereRadius)),
			round(s.yCoord - sin60Diam),
			s.zCoordRounded);
	loc[index++] = p;

	p = new Point3D(round(s.xCoord - (sphereRadius)),
			round(s.yCoord - sin60Diam),
			s.zCoordRounded);
	loc[index++] = p;

	return loc;
    }

    /*
     * Returns the locations of the centers of potential immediately
     * neighboring cells in the B tier either above or below (as
     * specified by argument zCoord) the Sphere argument s.
     */
    private static Point3D[] adjacentBTierNeighborLocations(Sphere s, double zCoord) {
	Point3D[] loc = new Point3D[3];
	Point3D p;
	int index = 0;

	p = new Point3D(s.xCoordRounded,
			round(s.yCoord + (2.0 * sin60DiamDiv3)),
			zCoord);
	loc[index++] = p;

	p = new Point3D(round(s.xCoord - sphereRadius),
			round(s.yCoord - sin60DiamDiv3),
			zCoord);
	loc[index++] = p;

	p = new Point3D(round(s.xCoord + sphereRadius),
			round(s.yCoord - sin60DiamDiv3),
			zCoord);
	loc[index++] = p;
	
	return loc;
    }

    /*
     * Returns the locations of the centers of potential immediately
     * neighboring cells in the C tier either above or below (as
     * specified by argument zCoord) the Sphere argument s.
     */
    private static Point3D[] adjacentCTierNeighborLocations(Sphere s, double zCoord) {
	Point3D[] loc = new Point3D[3];
	Point3D p;
	int index = 0;

	p = new Point3D(s.xCoordRounded,
			round(s.yCoord - (2.0 * sin60DiamDiv3)),
			zCoord);
	loc[index++] = p;

	p = new Point3D(round(s.xCoord - sphereRadius),
			round(s.yCoord + sin60DiamDiv3),
			zCoord);
	loc[index++] = p;

	p = new Point3D(round(s.xCoord + sphereRadius),
			round(s.yCoord + sin60DiamDiv3),
			zCoord);
	loc[index++] = p;

	return loc;
    }


    /*
     * Returns the z-coordinates of the tiers immediately above and
     * below the tier occupied by Sphere argument s.
     */
    private static double[] adjacentTierZCoords(Sphere s) {
	double[] tierZCoords = new double[] {round(s.zCoord + sqrt6DiamDiv3),
					     round(s.zCoord - sqrt6DiamDiv3)};
	return tierZCoords;
    }

    private static void countNeighbors(LinkedList<Sphere> sphereList) {
	Collections.sort(sphereList);
	Point3D[] loc;
	Sphere target = new Sphere();
	SphereComparator comp = new SphereComparator();
	for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
	    Sphere s = i.next();
	    int neighborCount = 0;
	    loc = sameTierNeighborLocations(s);
	    for (int j = 0; j < loc.length; j ++) {
		Point3D p = loc[j];
		target.xCoordRounded = p.x;
		target.yCoordRounded = p.y;
		target.zCoordRounded = p.z;
		neighborCount +=
		    Collections.binarySearch(sphereList, target, comp)>=0 ?1:0;
	    }
	    
	    int oldBCount = 0;
	    int oldCCount = 0;
	    for (double z : adjacentTierZCoords(s)) {

		/*
		 * Assume tier is B type
		 */
		int bCount = 0;
		loc = adjacentBTierNeighborLocations(s, z);
		for (int j = 0; j < loc.length; j ++) {
		    Point3D p = loc[j];
		    target.xCoordRounded = p.x;
		    target.yCoordRounded = p.y;
		    target.zCoordRounded = p.z;
		    bCount += Collections.binarySearch(sphereList, target, comp)>=0 ?1:0;
		}

		/*
		 * Assume tier is C type
		 */
		int cCount = 0;
		loc = adjacentCTierNeighborLocations(s, z);
		for (int j = 0; j < loc.length; j ++) {
		    Point3D p = loc[j];
		    target.xCoordRounded = p.x;
		    target.yCoordRounded = p.y;
		    target.zCoordRounded = p.z;
		    cCount += Collections.binarySearch(sphereList, target, comp)>=0 ?1:0;
		}

		if (bCount > 0 && cCount > 0) {
                    die("[Spheroid.countNeighbors] Positiove B and C counts in the same tier at x="
                        + s.xCoord + " y=" + s.yCoord + " z=" + s.zCoord);
                }
		
		/*
                 * At this point at least one of bCount and cCount is
                 * zero, so instead of checking for the non-zero
                 * count, just add them together.
                 */
                neighborCount += bCount + cCount;

                /*
                 * Make sure that tiers disagree on non-zero counts (due to ABC stacking)
                 */
                if ((oldBCount > 0 && bCount > 0)
                    || (oldCCount > 0 && cCount > 0)) {
                    die("[Spheroid.countNeighbors] Non-ABC stacking detected at x="
                        + s.xCoord + " y=" + s.yCoord + " z=" + s.zCoord
                        + " " + oldBCount + " " + bCount
                        + " " + oldCCount + " " + cCount);
                }
                oldBCount = bCount;
                oldCCount = cCount;
	    }
	    s.neighborCount = neighborCount;
	}
    }


    /*
    private static int neighborCheck(Sphere s, Sphere n,
				     LinkedList<Sphere> sphereList, SphereComparator comp) {
	int returnValue;
	int searchResult = Collections.binarySearch(sphereList, n, comp);
	if (searchResult >= 0) {
	    returnValue = 1;
	    Sphere neighbor = sphereList.get(searchResult);
	    s.recordNeighbor(neighbor.idNumber);
	    if (s.idNumber == neighbor.idNumber) {
		die("[Spheroid.neighborCheck] Sphere is its own neighbor: " + s.idNumber);
	    }
	}
	else {
	    returnValue = 0;
	}
	return returnValue;
    }
    */    


    /*
     * For each sphere in sphere list, computes the projection of a
     * line from (x,y,z) through the sphere center onto the sphere
     * itself.  The line intersects the sphere at two points.  Only
     * the further point is kept.  The projected points are
     * represented as Sphere objects in order to avoid the creation of
     * a new class.
     *
     *
     *  ((x_1-x_0)t + x_0 - x_1)^2
     *    + ((y_1-y_0)t + y_0 - y_1)^2
     *    + ((z_1-z_0)t + z_0 - z_1)^2
     *  = r^2
     *
     * let dx = x_1-x_0
     *     dy = y_1-y_0
     *     dz = z_1-z_0
     *
     * ((dx)t - dx)^2 + ((dy)t - dy)^2 + ((dz)t - dz)^2 = r^2
     *
     * dx^2(t - 1)^2 + dy^2(t - 1)^2 + dz^2(t - 1)^2 = r^2
     *
     * (t - 1)^2(dx^2 + dy^2 + dz^2) = r^2
     *
     * (t - 1)^2 = r^2/(dx^2 + dy^2 + dz^2)
     *
     * (t - 1) = +- r/sqrt(dx^2 + dy^2 + dz^2)
     *
     * t = 1 +- r/sqrt(dx^2 + dy^2 + dz^2)
     *
     */
    private static void computeProjections(LinkedList<Sphere> sphereList,
					   double x0,
					   double y0,
					   double z0) {

	for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
	    Sphere s = i.next();
	    double x1 = s.xCoord;
	    double y1 = s.yCoord;
	    double z1 = s.zCoord;
	    double r = s.radius;

	    double dx = x1 - x0;
	    double dy = y1 - y0;
	    double dz = z1 - z0;

	    double n = (r / Math.sqrt(sqr(dx) + sqr(dy) + sqr(dz)));
	    double t1 = 1.0 + n;
	    double t2 = 1.0 - n;
	    
	    double u1 = (dx * t1) + x0;
	    double v1 = (dy * t1) + y0;
	    double w1 = (dz * t1) + z0;

	    double u2 = (dx * t2) + x0;
	    double v2 = (dy * t2) + y0;
	    double w2 = (dz * t2) + z0;

	    /*
	    System.out.println("[Spheroid.computeProjections] projections: ("
			       + u1 + "," + v1 + "," + w1 + ")     ("
			       + u2 + "," + v2 + "," + w2 + ")");
	    */

	    double dist1 =
		(Math.sqrt(sqr(u1 - x0) + sqr(v1 - y0) + sqr(w1 - z0)));
	    double dist2 =
		(Math.sqrt(sqr(u2 - x0) + sqr(v2 - y0) + sqr(w2 - z0)));

	    Point3D p;
	    if (dist1 > dist2) {
		p = new Point3D(u1, v1, w1);
	    }
	    else {
		if (dist1 == dist2) {
		    die("[Spheroid,computeProjections] Single intersection!");
		}
		p = new Point3D(u2, v2, w2);
	    }
	    s.projection = p;
	}
    }


    private static double sqr(double n) {
	return n * n;
    }


    private static void projectionTest() {
	LinkedList<Sphere> sList = new LinkedList<Sphere>();
	Sphere s = new Sphere();
	s.xCoord = 2;
	s.yCoord = 2;
	s.zCoord = 2;
	s.radius = 1;
	sList.add(s);
	computeProjections(sList, 0, 0, 0);
	for (Iterator<Sphere> i = sList.iterator(); i.hasNext();) {
	    s = i.next();
	    System.out.println("x=" + s.xCoord + " y=" + s.yCoord + " z=" + s.zCoord
			       + " r=" + s.radius + "       projection=" + s.projection);
	}
    }


    /*
    private static void countNeighborTest() {
	LinkedList<Sphere> sphereList = new LinkedList<Sphere> ();
	LinkedList<Sphere> sphereList2 = new LinkedList<Sphere> ();

        sphereList = packSphere(spheroidRadius, sphereRadius);
        sphereList2 = packSphere(spheroidRadius, sphereRadius);

	countNeighbors(sphereList);
	countNeighbors2(sphereList2);

	Iterator<Sphere> k = sphereList.iterator();
	Iterator<Sphere> k2 = sphereList2.iterator();
	int count  = 0;
	while (k.hasNext() && k2.hasNext()) {
	    count ++;
	    Sphere s = k.next();
	    Sphere s2 = k2.next();
	    if (s.xCoord != s2.xCoord || s.yCoord != s2.yCoord || s.zCoord != s2.zCoord || 
		s.neighborCount != s2.neighborCount) {
		die("[Spheroid.main] FAIL");
	    }
	}
	if (k.hasNext() || k2.hasNext()) {
	    die("[Spheroid.main] unequal length sphere lists");
	}

	System.out.println("[Spheroid.main] done " + count);

    }
    */

    private static void removeHiddenTest() {
	LinkedList<Sphere> sphereList =
	    packSphere(spheroidRadius, sphereRadius);
	countNeighbors(sphereList);
	System.out.println("[Spheroid.removeHiddenTest] Spheres created: "
			   + sphereList.size());
	removeHidden(sphereList);
	System.out.println("[Spheroid.removeHiddenTest] Spheres after removal: "
			   + sphereList.size());
    }

    private static void removeHidden(LinkedList<Sphere> sphereList) {
	for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
	    Sphere s = i.next();
	    if (s.neighborCount == MAXIMUM_NUMBER_OF_NEIGHBORS) {
		i.remove();
	    }
	}
    }


    private static void assignIdNumbers(LinkedList<Sphere> sphereList) {
	int index = 0;
	for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
	    Sphere s = i.next();
	    s.setIdNumber(index++);
	}
    }


    private static void assignNeighbors(LinkedList<Sphere> sphereList) {
	Point3D[] loc;
	Sphere target = new Sphere();
	SphereComparator comp = new SphereComparator();
	for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
	     Sphere s = i.next();
	     /*
	     System.out.println("[Spheroid.assignNeighbors] processing "
				+ s.idNumber + " (" + s.xCoord + ","
				+ s.yCoord + "," + s.zCoord + ")");
	     */
	     loc = sameTierNeighborLocations(s);
	     for (Point3D p : loc) {
		 /*
		 System.out.println("[Spheroid.assignNeighbors] Checking for neighbopr at "
				    + p);
		 */
		 target.xCoordRounded = p.x;
		 target.yCoordRounded = p.y;
		 target.zCoordRounded = p.z;
		 int result = 
		     Collections.binarySearch(sphereList, target, comp);
		 if (result >= 0) {
		     //		     System.out.println("***** FOUND " + result);
		     /*
		      * Since id numbers were asigned in order after
		      * internal spheres were removed, the search
		      * result should be the same as the sphere's id
		      * number
		      */
		     s.recordNeighbor(result);
		 }
	     }
	     for (double z : adjacentTierZCoords(s)) {
		 loc = adjacentBTierNeighborLocations(s, z);
		 for (Point3D p : loc) {
		     target.xCoordRounded = p.x;
		     target.yCoordRounded = p.y;
		     target.zCoordRounded = p.z;
		     int result = 
			 Collections.binarySearch(sphereList, target, comp);
		     if (result >= 0) {
			 s.recordNeighbor(result);
		     }		     
		 }
		 loc = adjacentCTierNeighborLocations(s, z);
		 for (Point3D p : loc) {
		     target.xCoordRounded = p.x;
		     target.yCoordRounded = p.y;
		     target.zCoordRounded = p.z;
		     int result = 
			 Collections.binarySearch(sphereList, target, comp);
		     if (result >= 0) {
			 s.recordNeighbor(result);
		     }
		 }
	     }
	}
    }
    

    private static void mutualNeighborCheck(LinkedList<Sphere> sphereList) {
	//	System.out.println("[Spheroid.mutualNeighborCheck] started "
	//			   + sphereList.size());
	int size = sphereList.size();
	Sphere[] sphereArray = new Sphere[size];
	int index = 0;
	for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
	    Sphere s = i.next();
	    sphereArray[index++] = s;
	}

	for (int i = 0; i < size; i ++) {
	    Sphere s = sphereArray[i];
	    for (Iterator<Integer> j = s.neighbors.iterator(); j.hasNext();) {
		int neighborIndex = j.next();
		Sphere n = sphereArray[neighborIndex];
		if (! n.isNeighbor(s.idNumber)) {
		    die("[Spheroid.mutualNeighborCheck] sphere " + s.idNumber
			+ " lists sphere " + neighborIndex
			+ " as a neighbor, but it is not mutual");
		}
	    }
	}
	//	System.out.println("[Spheroid.mutualNeighborCheck] PASSED");
    }

    public static LinkedList<Sphere> processSpheres(OutputBufferInterface buffer) {
	LinkedList<Sphere> sphereList = 
	    packSphere(spheroidRadius, sphereRadius);
	buffer.println("// [Spheroid.processSpheres] Spheroid cluster contains "
		       + sphereList.size() + " cells");
	countNeighbors(sphereList);
	removeHidden(sphereList);
	/*
	int size = sphereList.size();
	Sphere[] sphereArray = new Sphere[size];
	int index = 0;
	for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
	    Sphere s = i.next();
	    s.setIdNumber(index);
	    sphereArray[index++] = s;
	}
	*/
	assignIdNumbers(sphereList);
	assignNeighbors(sphereList);
	mutualNeighborCheck(sphereList);
	computeProjections(sphereList, 0, 0, 0);
	return sphereList;
    }


    public static Sphere[] getSphereData(double spheroidDiameter, double sphereDiameter,
					 OutputBufferInterface buffer) {
	//	System.out.println("[Spheroid.getSphereData] " + spheroidDiameter + " "
	//			   + sphereDiameter);

	/*
	 * Why the cluster diameter must be at least 3 times the cell diameter:
	 *
	 * If the cluster diameter is less than 3 times the sphere
	 * diameter, then the cluster will consiste of a single sphere
	 * cell placed at the center of the cluster.  A cluster with a
	 * single cell does not have a unique location on which to
	 * start a sprout, because the current method of finding a
	 * sprout location is to use a line passing through the
	 * spheroid cluster center and the cell center onto the cell.
	 * This line intersects the cell at two points; the outermost
	 * point is where the cell's sprout is located.  The cluster's
	 * center and the center of the innermost cell are the same,
	 * so ther is no unique line that can be used to locate the
	 * starting point of a sprout.
	 */
	if (spheroidDiameter < (3 * sphereDiameter)) {
	    die("Spheroid cluster diameter parameter " + spheroidDiameter
		+ " is less than three times the cluster cell diameter: " + sphereDiameter);
	}


	Spheroid.spheroidDiameter = spheroidDiameter;
	Spheroid.sphereDiameter = sphereDiameter;

	spheroidRadius = spheroidDiameter / 2;
	sphereRadius = sphereDiameter / 2;


	sin60Diam = SIN_60_DEGREES * sphereDiameter;
	sin60DiamDiv3 = sin60Diam / 3.0;
	sqrt6DiamDiv3 = (SQRT_6 * sphereDiameter) / 3.0;


	return getSphereData(buffer);
    }

    public static Sphere[] getSphereData(OutputBufferInterface buffer) {
	LinkedList<Sphere> sphereList = processSpheres(buffer);
	int size = sphereList.size();
	Sphere[] sphereArray = new Sphere[size];
	int index = 0;
	double minX = 0;
	double maxX = 0;
	double minY = 0;
	double maxY = 0;
	double minZ = 0;
	double maxZ = 0;
	for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
	    Sphere s = i.next();
	    sphereArray[index++] = s;
	    //	    System.out.println(s);
	    minX = Math.min(minX, s.xCoord - s.radius);
	    maxX = Math.max(maxX, s.xCoord + s.radius);
	    minY = Math.min(minY, s.yCoord - s.radius);
	    maxY = Math.max(maxY, s.yCoord + s.radius);
	    minZ = Math.min(minZ, s.zCoord - s.radius);
	    maxZ = Math.max(maxZ, s.zCoord + s.radius);
	}
	/*
	System.out.println("[Spheroid.getSphereData] minX=" + minX + " maxX=" + maxX
			   + " minY=" + minY + " maxY=" + maxY
			   + " minZ=" + minZ + " maxZ=" + maxZ);
	*/
	return sphereArray;
    }


    public static class TestBuffer implements OutputBufferInterface {
	public boolean println(String s) {
	    System.out.println(s);
	    return true;
	}
    }

    public static void main(String args[]) throws Exception {
	double r0 = Double.parseDouble(args[0]);
	double r1 = Double.parseDouble(args[1]);
	String filename = args[2];
	double largeRadius = Math.max(r0, r1);
	double smallRadius = Math.min(r0, r1);
	LinkedList<Sphere> spList = packSphere(largeRadius, smallRadius);
	PrintStream ps = new PrintStream(filename);
	String separator = "\t";
	for (Iterator<Sphere> i = spList.iterator(); i.hasNext();) {
	    Sphere sp = i.next();
	    ps.println(sp.xCoord + separator + sp.yCoord + separator
		       + sp.zCoord);
	}
	ps.close();
	System.out.println("Wrote file " + filename);

	if (true) {return;}


	processSpheres(new TestBuffer());
	if (true) {return;}

	LinkedList<Sphere> sphereList;
	System.out.println("Packing spheres of diameter " + sphereDiameter 
			   + " microns into a larger sphere of diameter "
			   + spheroidDiameter + " microns");

	//	System.out.println(round(3.88));
	//	if (true) {System.exit(0);}

	//	String adjustArg = args[0];
	//	double adjust = Double.parseDouble(adjustArg);

	//	packTier(sphereList, 5, 1, 0, 0, 0);
	sphereList = packSphere(spheroidRadius, sphereRadius);
	double bigSphereVolume = sphereVolume(spheroidRadius);
	double smallSphereVolume = sphereVolume(sphereRadius);
	int packedSpheres = sphereList.size();
	double fillPct = 
	    ((packedSpheres * smallSphereVolume) / bigSphereVolume) * 100;
	System.out.println(packedSpheres + " small spheres.");
	System.out.println(fillPct + "% filled");
	boolean first = true;
	for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
	    Sphere s = i.next();
	    /*
	    System.out.println(s.xCoord
			       + "  " + s.yCoord
			       + "  " + s.zCoord
			       + "  " + s.radius);
	    */
	    /*
	    if (first) {
		first = false;
	    }
	    else {
		System.out.print(",");
	    }
	    */
	    System.out.println(round(s.xCoord)
			       + "," + round(s.yCoord)
			       + "," + round(s.zCoord)
			       + "," + s.radius);


	    //	    System.out.println(s.xCoord);
	    //	    System.out.println(s.yCoord);
	}
	
	System.out.println();
	System.out.println();
	//	removeHidden(sphereList);
	int internalCount = 0;
	int externalCount = 0;
        for (Iterator<Sphere> i = sphereList.iterator(); i.hasNext();) {
            Sphere s = i.next();
	    if (s.neighborCount == 12) {
		internalCount ++;
	    }
	    else {
		externalCount ++;
	    }
	    /*
            System.out.println("(" + round(s.xCoord)
                               + "," + round(s.yCoord)
                               + "," + round(s.zCoord)
                               + ")     " + s.neighbors);
	    */
	}
	System.out.println("internal count = " + internalCount);
	System.out.println("external count = " + externalCount);
	
    }
    
}
