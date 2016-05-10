

package scaffold;

import sim.util.Int3D;
import sim.util.Double3D;

import java.util.*;

import shared.*;


public class Sphere implements Shape{

    private static final double SQRT_3 = Math.sqrt(3);
    
    double centerX;
    double centerY;
    double centerZ;
    double radius;

    public Sphere(double centerX,
		  double centerY, 
		  double centerZ, 
		  double radius) {
	this.centerX = centerX;
	this.centerY = centerY;
	this.centerZ = centerZ;
	this.radius = radius;
    }


    public Sphere(double[] params) {
	if (params.length != 4) {
	    Environment.die("[Sphere] array argument has " + params.length
			    + " elements.");
	}
	centerX = params[0];
	centerY = params[1];
	centerZ = params[2];
	radius = params[3];
    }

    public NodeInterface.ShapeType getShapeType() {
	return NodeInterface.ShapeType.SPHERE;
    }

    public double[] getParams() {
	return new double[] {centerX, centerY, centerZ, radius};
    }


    public String toString() {
	String s =
	    "Sphere[x=" + centerX
	    + ",y=" + centerY
	    + ",z=" + centerZ
	    + ",r=" + radius
	    + "]";
	return s;
    }

    public Point3D getLocation() {
	return new Point3D(centerX, centerY, centerZ);
    }

    private double distance(double x1, double y1, double z1,
			    double x2, double y2, double z2) {
	return Math.sqrt(Math.pow(x1 - x2, 2)
			 + Math.pow(y1 - y2, 2)
			 + Math.pow(z1 - z2, 2));
    }


    public LinkedList<Int3D> createRepresentation(Environment env) {
	return createRepresentation(centerX, centerY, centerZ, radius, env);
    }

    private LinkedList<Int3D> createRepresentation(double x,
						   double y,
						   double z,
						   double r,
						   Environment env) {
	//	Environment env = (Environment) e;
	/*
	 * Convert sphere arguments to voxel system
	 */
	double voxelUnitLength = env.getVoxelLengthMicrons(); //env.getVoxelGridScale();
	double voxelXOrigin = env.getVoxelXOrigin();
	double voxelYOrigin = env.getVoxelYOrigin();
	double voxelZOrigin = env.getVoxelZOrigin();
	double xv = (x - voxelXOrigin) / voxelUnitLength;
	double yv = (y - voxelYOrigin) / voxelUnitLength;
	double zv = (z - voxelZOrigin) / voxelUnitLength;
	double rv = r / voxelUnitLength;

	int minX = (int) Math.floor(xv - rv);
	int maxX = (int) Math.ceil(xv + rv);
	int minY = (int) Math.floor(yv - rv);
	int maxY = (int) Math.ceil(yv + rv);
	int minZ = (int) Math.floor(zv - rv);
	int maxZ = (int) Math.ceil(zv + rv);


	LinkedList<Int3D> points = new LinkedList<Int3D>();
	

	/*
	 * Note that the centers of two neighboring voxels that have
	 * only a side (surface) in common are a distance of l apart
	 * where l is the voxel edge length. If two voxels share only
	 * an edge, than their centers are a distance of sqrt(l^2 +
	 * l^2) = sqrt(2) * l apart.  If two voxels share only a corner
	 * point, then their centers are a distance of sqrt(l^2 + l^2
	 * +l^2) = sqrt(3) * l apart.
	 *
	 * Instead of representing a sphere by all voxels contained in
	 * the sphere, use only those voxels forming a hollow shell.
	 * The shell is defined as those voxels whose centers are at a
	 * distance from the sphere center of at most rv and and more
	 * than rv-(SQRT_3 * l) from the sphere center.
	 */
	double minRadius = Math.max(0, rv - (voxelUnitLength * SQRT_3));
	//	System.out.println("[Sphere.createRepresentation] " + rv 
	//			   + "  " + minRadius);

	for (int i = minX; i <= maxX; i++) {
	    for (int j = minY; j <= maxY; j++) {
		for (int k = minZ; k <= maxZ; k++) {
		    double dist = distance(xv, yv, zv, i, j, k);
		    if (dist <= rv && rv > minRadius) {
			points.add(new Int3D(i, j, k));
			//			System.out.println("[Sphere] added " + i
			//					   + " " + j + " " + k);
		    }
		}
	    }
	}

	return points;

    }

    public void translate(double deltaX, double deltaY, double deltaZ) {
	centerX += deltaX;
	centerY += deltaY;
	centerZ += deltaZ;
    }

    public void resize(double factor) {
	radius *= factor;
    }

    public static void main(String[] args) {
	String fileName = args[0];
	Environment e = new Environment(new Parameters(args));
	double[] params = new double[] {0, 0, 0, 1};
	Node n =
	    new Node(shared.NodeInterface.ShapeType.SPHERE, params);
	LinkedList<Node> nodeList = new LinkedList<Node>();
	nodeList.add(n);
	Cell c = new Cell(nodeList, 1);
	Sphere s = new Sphere(0, 0, 0, 1);
	LinkedList<Int3D> points = s.createRepresentation(e);

	for (Iterator<Int3D> i = points.iterator(); i.hasNext();) {
	    Int3D pt = i.next();
	    System.out.println(pt.x + " " + pt.y + " " + pt.z);
	}

	/*
	int minX = g.getMinX();
	int maxX = g.getMaxX();
	int minY = g.getMinY();
	int maxY = g.getMaxY();
	int minZ = g.getMinZ();
	int maxZ = g.getMaxZ();
	for (int x = minX; x <= maxX; x++) {
	    for (int y = minY; y <= maxY; y++) {
		for (int z = minZ; z <= maxZ; z++) {
		    if (g.get(x, y, z) == c) {
			System.out.println(x + " " + y + " " + z);
		    }
		}
	    }
	}
	*/

    }


}
