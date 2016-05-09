
package spheroid;

import shared.Point3D;

import scaffold.*;

import sim.util.Int3D;

import java.util.*;

public class Sphere implements Comparable {
    //	public static int nextIdNumber = 0;
    public int idNumber;
    public double xCoord;
    public double yCoord;
    public double zCoord;
    public double radius;
    public double xCoordRounded;
    public double yCoordRounded;
    public double zCoordRounded;
    public int neighborCount;
    public LinkedList<Integer> neighbors;
    public Point3D projection;

    public static boolean specialCompare = false;


    
    public Sphere() {
	neighbors = new LinkedList<Integer>();
	/*
	  idNumber = nextIdNumber;
	  nextIdNumber ++;
	  neighbors = new int[12];
	  for (int i = 0; i < neighbors.length; i ++) {
	  neighbors[i] = -1;
	  }
	*/
    }
    
    public void setIdNumber(int idNumber) {
	this.idNumber = idNumber;
    }
    
    public void recordNeighbor(int idNumber) {
	neighbors.add(idNumber);
	/*
	  for (int i = 0; i < neighbors.length; i ++) {
	  if (neighbors[i] == idNumber) {
	  break;
	  }
	  else {
	  if (neighbors[i] == -1) {
	  neighbors[i] = idNumber;
	  break;
	  }
	  }
	  }
	*/
    }


    /*
     * Returns n2, the closest integer to n such that n is between ref and n2. 
     */
    private static int roundAway(double n, double ref) {
	int n2 = 0;
	if (n < ref) {
	    n2 = (int) Math.floor(n);
	}
	else {
	    if (n > ref) {
		n2 = (int) Math.ceil(n);
	    }
	    else {
		Spheroid.die("[Sphere.roundAway] Undefined when n=ref=" + n);
	    }
	}
	return n2;
    }

    private static void testRoundAway() {
	double n = 3.5;
	double ref = 3.8;
	double n2 = roundAway(n, ref);
	System.out.println("[Sphere.testRoundAway] n=" + n + " ref=" + ref + " roundAway="
			   + n2);
    }

    private static double sqr(double n) {
	return n * n;
    }


    /* 
     * Adds a list of integer (voxel) indices in grid system to rep argument
     */
    public void createRepresentation(SimpleGrid grid, LinkedList<Int3D> rep) {

	Point3D minPoint =
	    new Point3D(xCoord - radius, yCoord - radius, zCoord - radius);
	Point3D maxPoint =
	    new Point3D(xCoord + radius, yCoord + radius, zCoord + radius);

	Point3D gridMin = grid.translateToGrid(minPoint);
	Point3D gridMax = grid.translateToGrid(maxPoint);

	//	System.out.println("[Sphere.addrepresentation] 1");

	Point3D centerPoint = new Point3D(xCoord, yCoord, zCoord);
	Point3D gridCenter = grid.translateToGrid(centerPoint);
	double centerX = gridCenter.x;
	double centerY = gridCenter.y;
	double centerZ = gridCenter.z;

	double gridRadius = grid.translateToGrid(radius);

	//	System.out.println("[Sphere.addrepresentation] 2");
	
	// 1. translate min and maxes to voxel coordinates
	// 1b. translate center and radius
	// 2. for each voxel coordinate within translated mins and maxes
	// 3. determine if voxel point is within (translated sphere)
	
	int minX = roundAway(gridMin.x, centerPoint.x);
	int maxX = roundAway(gridMax.x, centerPoint.x);
	int minY = roundAway(gridMin.y, centerPoint.y);
	int maxY = roundAway(gridMax.y, centerPoint.y);
	int minZ = roundAway(gridMin.z, centerPoint.z);
	int maxZ = roundAway(gridMax.z, centerPoint.z);


	//	System.out.println("[Sphere.addrepresentation] 3");

	for (int x = minX; x <= maxX; x++) {
	    for (int y = minY; y <= maxY; y++) {
		for (int z = minZ; z <= maxZ; z++) {
		    double dist = 
			Math.sqrt(sqr(x - centerX) + sqr(y - centerY) + sqr(z - centerZ));
		    if (dist <= gridRadius) {
			rep.add(new Int3D(x, y, z));
		    }
		}
	    }
	}
    }

    public void addRepresentation(SimpleGrid grid, Cell c) {
	Spheroid.die("[spheroid.Sphere.addRepresentation] addRepresentation invoked");
	Point3D minPoint = new Point3D(xCoord - radius, yCoord - radius, zCoord - radius);
	Point3D maxPoint = new Point3D(xCoord + radius, yCoord + radius, zCoord + radius);

	Point3D gridMin = grid.translateToGrid(minPoint);
	Point3D gridMax = grid.translateToGrid(maxPoint);

	//	System.out.println("[Sphere.addrepresentation] 1");

	Point3D centerPoint = new Point3D(xCoord, yCoord, zCoord);
	Point3D gridCenter = grid.translateToGrid(centerPoint);
	double centerX = gridCenter.x;
	double centerY = gridCenter.y;
	double centerZ = gridCenter.z;

	double gridRadius = grid.translateToGrid(radius);

	//	System.out.println("[Sphere.addrepresentation] 2");
	
	// 1. translate min and maxes to voxel coordinates
	// 1b. translate center and radius
	// 2. for each voxel coordinate within translated mins and maxes
	// 3. determine if voxel point is within (translated sphere)
	
	int minX = roundAway(gridMin.x, centerPoint.x);
	int maxX = roundAway(gridMax.x, centerPoint.x);
	int minY = roundAway(gridMin.y, centerPoint.y);
	int maxY = roundAway(gridMax.y, centerPoint.y);
	int minZ = roundAway(gridMin.z, centerPoint.z);
	int maxZ = roundAway(gridMax.z, centerPoint.z);


	//	System.out.println("[Sphere.addrepresentation] 3");

	for (int x = minX; x <= maxX; x++) {
	    for (int y = minY; y <= maxY; y++) {
		for (int z = minZ; z <= maxZ; z++) {
		    double dist = 
			Math.sqrt(sqr(x - centerX) + sqr(y - centerY) + sqr(z - centerZ));
		    if (dist <= gridRadius) {
			grid.add(x, y, z, c);
		    }
		}
	    }
	}

	//	roundAwayFromZero

	
    }
    

    /* 
     * Adds a list of integer (voxel) indices in grid system to rep argument
     */

    public static void createCapsuleRepresentation(SimpleGrid grid,
						   Point3D p0, Point3D p1,
						   double radius,
						   LinkedList<Int3D> rep) {
	/*
	System.out.println("[Sphere.createCapsuleRepresentation] start " + p0
			   + " " + p1);
	*/

	double x0 = p0.x;
	double y0 = p0.y;
	double z0 = p0.z;
	double x1 = p1.x;
	double y1 = p1.y;
	double z1 = p1.z;
	
	Point3D minPoint = new Point3D(Math.min(x0, x1) - radius,
				       Math.min(y0, y1) - radius,
				       Math.min(z0, z1) - radius);
	
	Point3D maxPoint = new Point3D(Math.max(x0, x1) + radius,
				       Math.max(y0, y1) + radius,
				       Math.max(z0, z1) + radius);
	
	Point3D gridMin = grid.translateToGrid(minPoint);
	Point3D gridMax = grid.translateToGrid(maxPoint);
	Point3D gridP0 = grid.translateToGrid(p0);
	Point3D gridP1 = grid.translateToGrid(p1);


	int minX = (int) Math.floor(gridMin.x);
	int maxX = (int) Math.floor(gridMax.x);
	int minY = (int) Math.floor(gridMin.y);
	int maxY = (int) Math.floor(gridMax.y);
	int minZ = (int) Math.floor(gridMin.z);
	int maxZ = (int) Math.floor(gridMax.z);


	Point3D candidate = new Point3D(0, 0, 0);

	/*
	System.out.println("[Sphere.createCapsuleRepresentation] starting loop "
			   + minX + " " + maxX + " "
			   + minY + " " + maxY + " "
			   + minZ + " " + maxZ + " ");
	*/

	for (int x = minX; x <= maxX; x ++) {
	    for (int y = minY; y <= maxY; y ++) {
		for (int z = minZ; z <= maxZ; z ++) {
		    /*
		    System.out.println("[Sphere.createCapsuleRepresentation] loop: "
				       + x + " (" + maxX + ") " + y + " (" + maxY + ") "
				       + z + " (" + maxZ + ")");
		    */
		    candidate.x = x;
		    candidate.y = y;
		    candidate.z = z;
		    double dist = candidate.distanceToSegment(gridP0, gridP1);
		    if (dist <= radius) {
			rep.add(new Int3D(x, y, z));
		    }
		}
	    }
	}
    }


    /* 
     * Adds a list of integer (voxel) indices in grid system to rep argument
     */

    public static void createCylinderRepresentation(SimpleGrid grid,
						    Point3D p0, Point3D p1,
						    double radius,
						    LinkedList<Int3D> rep) {
	/*
	System.out.println("[Sphere.createCapsuleRepresentation] start " + p0
			   + " " + p1);
	*/

	double x0 = p0.x;
	double y0 = p0.y;
	double z0 = p0.z;
	double x1 = p1.x;
	double y1 = p1.y;
	double z1 = p1.z;
	
	Point3D minPoint = new Point3D(Math.min(x0, x1) - radius,
				       Math.min(y0, y1) - radius,
				       Math.min(z0, z1) - radius);
	
	Point3D maxPoint = new Point3D(Math.max(x0, x1) + radius,
				       Math.max(y0, y1) + radius,
				       Math.max(z0, z1) + radius);
	
	Point3D gridMin = grid.translateToGrid(minPoint);
	Point3D gridMax = grid.translateToGrid(maxPoint);
	Point3D gridP0 = grid.translateToGrid(p0);
	Point3D gridP1 = grid.translateToGrid(p1);


	int minX = (int) Math.floor(gridMin.x);
	int maxX = (int) Math.floor(gridMax.x);
	int minY = (int) Math.floor(gridMin.y);
	int maxY = (int) Math.floor(gridMax.y);
	int minZ = (int) Math.floor(gridMin.z);
	int maxZ = (int) Math.floor(gridMax.z);


	Point3D candidate = new Point3D(0, 0, 0);

	/*
	System.out.println("[Sphere.createCapsuleRepresentation] starting loop "
			   + minX + " " + maxX + " "
			   + minY + " " + maxY + " "
			   + minZ + " " + maxZ + " ");
	*/

	for (int x = minX; x <= maxX; x ++) {
	    for (int y = minY; y <= maxY; y ++) {
		for (int z = minZ; z <= maxZ; z ++) {
		    /*
		    System.out.println("[Sphere.createCapsuleRepresentation] loop: "
				       + x + " (" + maxX + ") " + y + " (" + maxY + ") "
				       + z + " (" + maxZ + ")");
		    */
		    candidate.x = x;
		    candidate.y = y;
		    candidate.z = z;
		    if (candidate.withinCylinder(gridP0, gridP1, radius)) {
			rep.add(new Int3D(x, y, z));
		    }
		}
	    }
	}
    }



    public static void addCapsuleRepresentation(SimpleGrid grid, Cell c,
						Point3D p0, Point3D p1,
						double radius) {
	Spheroid.die("[spheroid.Sphere.addCapsuleRepresentation] addCapsuleRepresentation invoked");

	double x0 = p0.x;
	double y0 = p0.y;
	double z0 = p0.z;
	double x1 = p1.x;
	double y1 = p1.y;
	double z1 = p1.z;
	
	Point3D minPoint = new Point3D(Math.min(x0, x1) - radius,
				       Math.min(y0, y1) - radius,
				       Math.min(z0, z1) - radius);
	
	Point3D maxPoint = new Point3D(Math.max(x0, x1) + radius,
				       Math.max(y0, y1) + radius,
				       Math.max(z0, z1) + radius);
	
	Point3D gridMin = grid.translateToGrid(minPoint);
	Point3D gridMax = grid.translateToGrid(maxPoint);
	Point3D gridP0 = grid.translateToGrid(p0);
	Point3D gridP1 = grid.translateToGrid(p1);


	int minX = (int) Math.floor(gridMin.x);
	int maxX = (int) Math.floor(gridMax.x);
	int minY = (int) Math.floor(gridMin.y);
	int maxY = (int) Math.floor(gridMax.y);
	int minZ = (int) Math.floor(gridMin.z);
	int maxZ = (int) Math.floor(gridMax.z);

	/*
	System.out.println("[Sphere.addCapsuleRepresentation] " + minX + " "
			   + maxX + " " + minY + " " + maxY + " " + minZ + " "
			   + maxZ);
	*/
	
	//	Point3D q0 = new Point3D(0, 0, 0);
	//	Point3D q1 = new Point3D(1, 0, 0);
	//	System.out.println("pretest: " + q1.distanceToSegment(q0, q0));

			   

	Point3D candidate = new Point3D(0, 0, 0);
	for (int x = minX; x <= maxX; x ++) {
	    for (int y = minY; y <= maxY; y ++) {
		for (int z = minZ; z <= maxZ; z ++) {
		    candidate.x = x;
		    candidate.y = y;
		    candidate.z = z;
		    double dist = candidate.distanceToSegment(gridP0, gridP1);
		    if (dist <= radius) {
			grid.add(x, y, z, c);
		    }
		}
	    }
	}
    }






    public boolean isNeighbor(int idNum) {
	boolean found = false;
	for (Iterator<Integer> i = neighbors.iterator(); i.hasNext();) {
	    int neighborIdNum = i.next();
	    if (idNum == neighborIdNum) {
		found = true;
		break;
	    }
	}
	return found;
    }
    
    public int compareTo(Object o) {
	Sphere s = (Sphere) o;
	if (specialCompare) {
	    return neighborCount - s.neighborCount;
	}
	int comp;
	double diff = zCoordRounded - s.zCoordRounded;
	if (diff == 0) {
	    diff = yCoordRounded - s.yCoordRounded;
	    if (diff == 0) {
		diff = xCoordRounded - s.xCoordRounded;
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
    
    public String toString() {
	String returnString = "";
	String neighborsString = "";
	for (Iterator<Integer> i = neighbors.iterator(); i.hasNext();) {
	    int id = i.next();
	    if (!neighborsString.equals("")) {
		neighborsString += ",";
	    }
	    neighborsString += id;
	}
	/*
	  for (int i = 0; i < neighbors.length; i ++) {
	  if (i > 0) {
	  neighborsString += ",";
	  }
	  neighborsString += neighbors[i];
	  }
	*/
	returnString =
	    "Sphere[idNumber=" + idNumber
	    + ",x=" + xCoord
	    + ",y=" + yCoord
	    + ",z=" + zCoord
	    + ",radius=" + radius
	    + ",projection=" + projection
	    + ",neighborCount=" + neighborCount
	    + ",neighbors=" + neighborsString
	    +"]";
	return returnString;
    }


    public static void main (String[] args) {
	testRoundAway();
    }
    
}

