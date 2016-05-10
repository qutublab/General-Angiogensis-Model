
package scaffold;

import shared.*;

// for testing purposes only
import spheroid.*;

import java.util.*;
import java.math.*;

public class LazySimpleGrid extends SimpleGrid implements GridInterface {

    private static OutputBuffer buffer;

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;

    private boolean ignoreDiscretizedSprouts = false;


    public class Content {
	public LinkedList<Cell> contents = new LinkedList<Cell>();
    }


    private Content[][][] grid;

    

    /*
     * Square brackets [] denote array indices.  Parentheses () denote
     * cartesian coordinates.  Since the grid is in cartesian space,
     * array indices can represent cartesian points.  In addition,
     * noninteger array indices can also represent points - [.5,0,0]
     * represents a point between [0,0,0] and [1,0,0].
     *
     * Another way of thinking of this is that there are two
     * coordinate systems.
     */

    private Point3D origin; // location of center of voxel [0,0,0] as a cartesian point
    private double scale;   // distance between adjacent voxel centers
    /*
     * The center of voxel [x,y,z] is at:
     *
     *   (x*scale + origin.x, y*scale + origin, z*scale + origin.z)
     *
     *   x*scale = distance along x-axis from center of [0,0,0] to [x,y,z] in cartesian space
     *   origin.x = distance along x-axis from center of [0,0,0] to (0,0,0)
     */

    /*
    public static void initialize(Environment e) {
	buffer = e.getOutputBuffer();

	//	disableGrid = e.disableGrid();
    }
    */

    public LazySimpleGrid(Environment env) {
	buffer = env.getOutputBuffer();
	ignoreDiscretizedSprouts = env.ignoreDiscretizedSprouts();

	double xOrigin = env.getVoxelXOrigin();
	double yOrigin = env.getVoxelYOrigin();
	double zOrigin = env.getVoxelZOrigin();
	origin = new Point3D(xOrigin, yOrigin, zOrigin);

	scale = env.getVoxelLengthMicrons();

	if (ignoreDiscretizedSprouts) {
	    /*
	     * Sprouts are ignored, but the spheroid is still stored
	     * in the grid, so create a grid just large enough for the
	     * spheroid.
	     */
	    // Express spheroid radius in grid unit lengths
	    double spheroidRadius = (env.getSpheroidDiameterMicrons() / 2.0) / scale;

	    // determine minimum and maximum coordinates for each grid
	    // dimension
	    minX = (int) Math.floor(xOrigin - spheroidRadius);
	    maxX = (int) Math.ceil(xOrigin + spheroidRadius);
	    minY = (int) Math.floor(yOrigin - spheroidRadius);
	    maxY = (int) Math.ceil(yOrigin + spheroidRadius);
	    minZ = (int) Math.floor(zOrigin - spheroidRadius);
	    maxZ = (int) Math.ceil(zOrigin + spheroidRadius);
	    
	    /*
	    this(new Point3D(env.getVoxelXOrigin(), env.getVoxelYOrigin, env.getVoxelZOrigin),
		 env.getVoxelGridScale(),
		 minX, maxX, minY, maxY, minZ, maxZ);
	    */
	}
	else {
	    minX = env.getVoxelGridMinX();
	    maxX = env.getVoxelGridMaxX();
	    minY = env.getVoxelGridMinY();
	    maxY = env.getVoxelGridMaxY();
	    minZ = env.getVoxelGridMinZ();
	    maxZ = env.getVoxelGridMaxZ();

	    /*
	    this(new Point3D(env.getVoxelXOrigin(), env.getVoxelYOrigin, env.getVoxelZOrigin),
		 env.getVoxelGridScale(),
		 env.getVoxelGridMinX(), env.getVoxelGridMaxX(),
		 env.getVoxelGridMinY(), env.getVoxelGridMaxY(),
		 env.getVoxelGridMinZ(), env.getVoxelGridMaxZ());
	    */
	}
	buildGrid();
    }

    public LazySimpleGrid(Point3D origin, double scale,
			  int minX, int maxX,
			  int minY, int maxY,
			  int minZ, int maxZ) {
	buffer.println("[LazySimpleGrid] Constructing grid ["
		       + minX + ".." + maxX + "]["
		       + minY + ".." + maxY + "][" + minZ + ".." + maxZ + "]");
	this.origin = origin.copy();
	this.scale = scale;
	if (minX > maxX) {
	    Environment.die("[LazySimpleGrid] Minimum X value: " + minX
			    + " is greater than maximum X value: "
			    + maxX);
	}
	if (minY > maxY) {
	    Environment.die("[LazySimpleGrid] Minimum Y value: " + minY
			    + " is greater than maximum Y value: "
			    + maxY);
	}
	if (minZ > maxZ) {
	    Environment.die("[LazySimpleGrid] Minimum Z value: " + minZ
			    + " is greater than maximum Z value: "
			    + maxZ);
	}
	this.minX = minX;
	this.maxX = maxX;
	this.minY = minY;
	this.maxY = maxY;
	this.minZ = minZ;
	this.maxZ = maxZ;
	int xSize = (maxX - minX) + 1;
	int ySize = (maxY - minY) + 1;
	int zSize = (maxZ - minZ) + 1;
	grid = new Content[xSize][ySize][zSize];
	
	
	// allocate content objects in a lazy, on-demand way
	/*
	  for (int i = 0; i < xSize; i++) {
	  for (int j = 0; j < ySize; j++) {
	  for (int k = 0; k < zSize; k++) {
	  grid[i][j][k] = new Content();
	  }
	  }
	  }
	*/
	
	//	long numberOfLocations = xSize * ySize * zSize;
	BigInteger bigX = new BigInteger("" + xSize);
	BigInteger bigY = new BigInteger("" + ySize);
	BigInteger bigZ = new BigInteger("" + zSize);
	BigInteger numberOfLocations = bigX.multiply(bigY).multiply(bigZ);
	if (!ignoreDiscretizedSprouts) {
	    System.out.println("[LazySimpleGrid] Grid completed containing "
			       + numberOfLocations + " locations.");
	}
    }



    private void buildGrid() {
	buffer.println("[LazySimpleGrid] Constructing grid ["
		       + minX + ".." + maxX + "]["
		       + minY + ".." + maxY + "][" + minZ + ".." + maxZ + "]");
	if (minX > maxX) {
	    Environment.die("[LazySimpleGrid] Minimum X value: " + minX
			    + " is greater than maximum X value: "
			    + maxX);
	}
	if (minY > maxY) {
	    Environment.die("[LazySimpleGrid] Minimum Y value: " + minY
			    + " is greater than maximum Y value: "
			    + maxY);
	}
	if (minZ > maxZ) {
	    Environment.die("[LazySimpleGrid] Minimum Z value: " + minZ
			    + " is greater than maximum Z value: "
			    + maxZ);
	}
	int xSize = (maxX - minX) + 1;
	int ySize = (maxY - minY) + 1;
	int zSize = (maxZ - minZ) + 1;
	grid = new Content[xSize][ySize][zSize];


	//	long numberOfLocations = xSize * ySize * zSize;
	BigInteger bigX = new BigInteger("" + xSize);
	BigInteger bigY = new BigInteger("" + ySize);
	BigInteger bigZ = new BigInteger("" + zSize);
	BigInteger numberOfLocations = bigX.multiply(bigY).multiply(bigZ);
	if (!ignoreDiscretizedSprouts) {
	    System.out.println("[LazySimpleGrid] Grid completed containing "
			       + numberOfLocations + " locations.");
	}
    }


    

    /* 
     * translate from [] to ()
     */
    public Point3D translateFromGrid(Point3D p) {
	double x = (p.x * scale) + origin.x;
	double y = (p.y * scale) + origin.y;
	double z = (p.z * scale) + origin.z;
	return new Point3D(x, y, z);
    }


    /*
     * translate from () to []
     */
    public Point3D translateToGrid(Point3D p) {
	double x = (p.x - origin.x) / scale; 
	double y = (p.y - origin.y) / scale; 
	double z = (p.z - origin.z) / scale; 
	return new Point3D(x, y, z);
    }

    public double translateToGrid(double len) {
	return len / scale;
    }

    private static void testTranslation() {
	Point3D origin = new Point3D(1, 1, 1);
	double scale = 2;
	LazySimpleGrid sg = new LazySimpleGrid(origin, scale, -10, 10, -10, 10, -10, 10);
	Point3D p1 = new Point3D(1, 2, 3);
	Point3D p2 = sg.translateToGrid(p1);
	Point3D p3 = sg.translateFromGrid(p2);
	System.out.println("[LazySimpleGrid.testTranslation] " + p1 + "  " + p2 + "  " + p3);
    }



    public int getMinX() {
	return minX;
    }

    public int getMaxX() {
	return maxX;
    }

    public int getMinY() {
	return minY;
    }

    public int getMaxY() {
	return maxY;
    }

    public int getMinZ() {
	return minZ;
    }

    public int getMaxZ() {
	return maxZ;
    }


    public int trueX(int x) {
	return x - minX;
    }

    public int trueY(int y) {
	return y - minY;
    }

    public int trueZ(int z) {
	return z - minZ;
    }



    private boolean checkIndices(int x, int y, int z, boolean repressErrorSignal) {
	boolean validIndices = true;
	if (x < minX || x > maxX || y < minY || y > maxY
	    || z < minZ || z > maxZ) {
	    validIndices = false;
	    if (! repressErrorSignal) {
		Error e = new Error();
		System.err.println("LazySimpleGrid indices out of bounds: [" + x + "][" + y
				   + "][" + z + "]");
		e.printStackTrace();
		Environment.die("");
	    }
	}
	return validIndices;
    }

    // adds if NOT already present
    public void add(int x, int y, int z, Cell c) {
	boolean indicesInBounds = checkIndices(x, y, z, ignoreDiscretizedSprouts);
	if (!indicesInBounds) {
	    return;
	}
	int xIndex = trueX(x);
	int yIndex = trueY(y);
	int zIndex = trueZ(z);
	/*
	Cell content = grid[xIndex][yIndex][zIndex];
	if (content != null && content != c) {
	    Environment.die("[LazySimpleGrid.add] Location (" + x + "," + y + "," + z
			    + ") already occupied by " + content);
	}
	*/
	Content cont = grid[xIndex][yIndex][zIndex];
	if (cont == null) {
	    cont = new Content();
	    grid[xIndex][yIndex][zIndex] = cont;
	}

	LinkedList<Cell> cellList = cont.contents;
	boolean found = false;
	for (Iterator<Cell> i = cellList.iterator(); i.hasNext();) {
	    Cell c2 = i.next();
	    if (c == c2) {
		found = true;
		break;
	    }
	}
	if (!found) {
	    cellList.add(c);
	}
    }


    // removes ALL instances of c
    public void remove(int x, int y, int z, Cell c) {
	boolean indicesInBounds = checkIndices(x, y, z, ignoreDiscretizedSprouts);
	if (!indicesInBounds) {
	    return;
	}
	int xIndex = trueX(x);
	int yIndex = trueY(y);
	int zIndex = trueZ(z);
	/*
	Cell content = grid[xIndex][yIndex][zIndex];
	if (content != null && content != c) {
	    Environment.die("[LazySimpleGrid.add] Location (" + x + "," + y + "," + z
			    + ") occupied by an unexpected cell " + content);
	}
	grid[xIndex][yIndex][zIndex] = null;
	*/

	Content cont = grid[xIndex][yIndex][zIndex];
	// if the location has not been allocated, then there is
	// nothing to remove!
	if (cont == null) {
	    return;
	}

	LinkedList<Cell> cellList = cont.contents;
	for (Iterator<Cell> i = cellList.iterator(); i.hasNext();) {
	    Cell c2 = i.next();
	    if (c == c2) {
		i.remove();
	    }
	}
	if (cellList.size() == 0) {
	    grid[xIndex][yIndex][zIndex] = null;
	} 
    }


    //    edit so that false is returned when indices are out of bounds and ignoreDiscretizedSprouts is true
    public boolean isOccupied(int x, int y, int z) {
	boolean occupied;
	Content c = null;
	if (checkIndices(x, y, z, ignoreDiscretizedSprouts)) {
	    c = grid[trueX(x)][trueY(y)][trueZ(z)];
	}
	if (c == null) {
	    occupied = false;
	}
	else {
	    occupied = (c.contents.size() > 0);
	}
	return occupied;
    }


    // testing spheroid.Sphere.addRepresentation
    private static void testAddRepresentation() {
	//	Point3D origin = new Point3D(0, 0, 0);
	Point3D origin = new Point3D(0, 0, 0);
	double scale = 1;
	LazySimpleGrid sg = new LazySimpleGrid(origin, scale, -10, 10, -10, 10, -10, 10);
	spheroid.Sphere s = new spheroid.Sphere();
	s.xCoord = 0;
	s.yCoord = 0;
	s.zCoord = 0;
	s.radius = 1;
	
	Cell c = new Cell();

	System.out.println("[LazySimpleGrid.testAddRepresentation] starting Sphere.addRepresentation");
	s.addRepresentation(sg, c);
	System.out.println("[LazySimpleGrid.testAddRepresentation] starting printOccupiedLocations");
	sg.printOccupiedLocations();
    }


    // testing spheroid.Sphere.addCapsuleRepresentation
    private static void testAddCapsuleRepresentation() {
	//	Point3D origin = new Point3D(0, 0, 0);
	Point3D origin = new Point3D(0, 0, 0);
	double scale = 1;
	LazySimpleGrid sg = new LazySimpleGrid(origin, scale, -10, 10, -10, 10, -10, 10);

	Point3D p0 = new Point3D(0, 0, 0);
	Point3D p1 = new Point3D(2, 2, 2);
	double radius = 1;

	Cell c = new Cell();

	System.out.println("[LazySimpleGrid.testAddCapsuleRepresentation] starting Sphere.addCapsuleRepresentation");
	spheroid.Sphere.addCapsuleRepresentation(sg, c, p0, p1, radius);
	System.out.println("[LazySimpleGrid.testAddCapsuleRepresentation] starting printOccupiedLocations");
	sg.printOccupiedLocations();
    }
    

    public void clear() {
	int xLength = (maxX - minX) + 1;
	int yLength = (maxY - minY) + 1;
	int zLength = (maxZ - minZ) + 1;
	
	for (int x = 0; x < xLength; x++) {
	    for (int y = 0; y < yLength; y++) {
		for (int z = 0; z < zLength; z++) {
		    grid[x][y][z] = null;
		}
	    }
	}
    }



    public void addOccupyingCellOrigins(int x, int y, int z, LinkedList<Integer> list) {

	if (!checkIndices(x, y, z, ignoreDiscretizedSprouts)) {
	    return;
	}

	int gx = trueX(x);
	int gy = trueY(y);
	int gz = trueZ(z);
	
	Content c = grid[gx][gy][gz];
	if (c == null) {
	    c = new Content();
	    grid[gx][gy][gz] = c;
	}

	LinkedList<Cell> cellList = c.contents;
	for (Iterator<Cell> i = cellList.iterator(); i.hasNext();) {
	    Cell ce = i.next();
	    Cell origin = ce.getOrigin();
	    int idNumber = origin.getIdNumber();


	    int searchResult = Collections.binarySearch(list, idNumber);
	    System.out.println("[LazySimpleGrid.addOccupyingOrigins] " + idNumber
			       + "  " + searchResult);
	    if (searchResult < 0) {
		int insertionPoint = -(searchResult + 1);
		list.add(insertionPoint, idNumber);
	    }

	}
    }
	    

    public void printOccupyingCells(int x, int y, int z) {
	int gx = trueX(x);
	int gy = trueY(y);
	int gz = trueZ(z);
	Content c = grid[gx][gy][gz];
	if (c != null) {
	    LinkedList<Cell> cellList = grid[gx][gy][gz].contents;
	    System.out.print("[" + x + "][" + y + "][" + z + "]  ");
	    for (Iterator<Cell> i = cellList.iterator(); i.hasNext();) {
		Cell ce = i.next();
		System.out.print(ce.getIdNumber() + " ");
	    }
	}
	System.out.println();
    }
	    

    public void printOccupiedLocations() {
	for (int z = minZ; z <= maxZ; z ++) {
	    for (int y = minY; y <= maxY; y ++) {
		for (int x = minX; x <= maxX; x ++) {
		    int actualX = trueX(x);
		    int actualY = trueY(y);
		    int actualZ = trueZ(z);
		    Content c = grid[actualX][actualY][actualZ];
		    if (c != null && c.contents.size() > 0) {
			System.out.println("[" + x + "][" + y+ "][" + z +"]");
		    }
		}
	    }
	}
    }

    public void find(int idNumber) {
	for (int z = minZ; z <= maxZ; z ++) {
	    for (int y = minY; y <= maxY; y ++) {
		for (int x = minX; x <= maxX; x ++) {
		    int actualX = trueX(x);
		    int actualY = trueY(y);
		    int actualZ = trueZ(z);
		    Content c = grid[actualX][actualY][actualZ];
		    if (c != null) {
			for (Iterator<Cell> i = c.contents.iterator();
			     i.hasNext();) {
			    Cell ce = i.next();
			    if (idNumber == ce.getIdNumber()) {
				System.out.println("[" + x + "][" + y+ "]["
						   + z +"]: " + idNumber
						   + " found");
			    }
			}
		    }
		}
	    }
	}
    }

    public static void main(String[] args) {
	testAddRepresentation();
	System.out.println();
	testAddCapsuleRepresentation();
    }

}
