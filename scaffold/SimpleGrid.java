
package scaffold;

import shared.*;

// for testing purposes only
import spheroid.*;

import java.util.*;

public class SimpleGrid implements GridInterface {

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;

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



    // 0-ary constructior required for subclasses

    public SimpleGrid() {
    }

    public SimpleGrid(Point3D origin, double scale,
		      int minX, int maxX,
		      int minY, int maxY,
		      int minZ, int maxZ) {
	System.out.println("[SimpleGrid] Constructing grid [" + minX + ".." + maxX + "]["
			   + minY + ".." + maxY + "][" + minZ + ".." + maxZ + "]");
	this.origin = origin.copy();
	this.scale = scale;
	if (minX > maxX) {
	    Environment.die("[SimpleGrid] Minimum X value: " + minX
			    + " is greater than maximum X value: "
			    + maxX);
	}
	if (minY > maxY) {
	    Environment.die("[SimpleGrid] Minimum Y value: " + minY
			    + " is greater than maximum Y value: "
			    + maxY);
	}
	if (minZ > maxZ) {
	    Environment.die("[SimpleGrid] Minimum Z value: " + minZ
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
	for (int i = 0; i < xSize; i++) {
	    for (int j = 0; j < ySize; j++) {
		for (int k = 0; k < zSize; k++) {
		    grid[i][j][k] = new Content();
		}
	    }
	}

	int numberOfLocations = xSize * ySize * zSize;
	System.out.println("[SimpleGrid] Grid completed containing "
			   + numberOfLocations + " locations.");
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
	SimpleGrid sg = new SimpleGrid(origin, scale, -10, 10, -10, 10, -10, 10);
	Point3D p1 = new Point3D(1, 2, 3);
	Point3D p2 = sg.translateToGrid(p1);
	Point3D p3 = sg.translateFromGrid(p2);
	System.out.println("[SimpleGrid.testTranslation] " + p1 + "  " + p2 + "  " + p3);
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



    private void checkIndices(int x, int y, int z) {
	if (x < minX || x > maxX || y < minY || y > maxY
	    || z < minZ || z > maxZ) {
	    Error e = new Error();
	    System.err.println("SimpleGrid indices out of bounds: [" + x + "][" + y
			       + "][" + z + "]");
	    e.printStackTrace();
	    Environment.die("");
	}
    }

    // adds if NOT already present
    public void add(int x, int y, int z, Cell c) {
	checkIndices(x, y, z);
	int xIndex = trueX(x);
	int yIndex = trueY(y);
	int zIndex = trueZ(z);
	/*
	Cell content = grid[xIndex][yIndex][zIndex];
	if (content != null && content != c) {
	    Environment.die("[SimpleGrid.add] Location (" + x + "," + y + "," + z
			    + ") already occupied by " + content);
	}
	*/
	LinkedList<Cell> cellList = grid[xIndex][yIndex][zIndex].contents;
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
	checkIndices(x, y, z);
	int xIndex = trueX(x);
	int yIndex = trueY(y);
	int zIndex = trueZ(z);
	/*
	Cell content = grid[xIndex][yIndex][zIndex];
	if (content != null && content != c) {
	    Environment.die("[SimpleGrid.add] Location (" + x + "," + y + "," + z
			    + ") occupied by an unexpected cell " + content);
	}
	grid[xIndex][yIndex][zIndex] = null;
	*/

	LinkedList<Cell> cellList = grid[xIndex][yIndex][zIndex].contents;
	for (Iterator<Cell> i = cellList.iterator(); i.hasNext();) {
	    Cell c2 = i.next();
	    if (c == c2) {
		i.remove();
	    }
	}
    }


    public boolean isOccupied(int x, int y, int z) {
	return (grid[trueX(x)][trueY(y)][trueZ(z)].contents.size() > 0);
    }


    // testing spheroid.Sphere.addRepresentation
    private static void testAddRepresentation() {
	//	Point3D origin = new Point3D(0, 0, 0);
	Point3D origin = new Point3D(0, 0, 0);
	double scale = 1;
	SimpleGrid sg = new SimpleGrid(origin, scale, -10, 10, -10, 10, -10, 10);
	spheroid.Sphere s = new spheroid.Sphere();
	s.xCoord = 0;
	s.yCoord = 0;
	s.zCoord = 0;
	s.radius = 1;
	
	Cell c = new Cell();

	System.out.println("[SimpleGrid.testAddRepresentation] starting Sphere.addRepresentation");
	s.addRepresentation(sg, c);
	System.out.println("[SimpleGrid.testAddRepresentation] starting printOccupiedLocations");
	sg.printOccupiedLocations();
    }


    // testing spheroid.Sphere.addCapsuleRepresentation
    private static void testAddCapsuleRepresentation() {
	//	Point3D origin = new Point3D(0, 0, 0);
	Point3D origin = new Point3D(0, 0, 0);
	double scale = 1;
	SimpleGrid sg = new SimpleGrid(origin, scale, -10, 10, -10, 10, -10, 10);

	Point3D p0 = new Point3D(0, 0, 0);
	Point3D p1 = new Point3D(2, 2, 2);
	double radius = 1;

	Cell c = new Cell();

	System.out.println("[SimpleGrid.testAddCapsuleRepresentation] starting Sphere.addCapsuleRepresentation");
	spheroid.Sphere.addCapsuleRepresentation(sg, c, p0, p1, radius);
	System.out.println("[SimpleGrid.testAddCapsuleRepresentation] starting printOccupiedLocations");
	sg.printOccupiedLocations();
    }
    

    public void clear() {
	int xLength = (maxX - minX) + 1;
	int yLength = (maxY - minY) + 1;
	int zLength = (maxZ - minZ) + 1;
	
	for (int x = 0; x < xLength; x++) {
	    for (int y = 0; y < yLength; y++) {
		for (int z = 0; z < zLength; z++) {
		    grid[x][y][z].contents.clear();
		}
	    }
	}
    }


    public void addOccupyingCellOrigins(int x, int y, int z, LinkedList<Integer> list) {
	int gx = trueX(x);
	int gy = trueY(y);
	int gz = trueZ(z);
	LinkedList<Cell> cellList = grid[gx][gy][gz].contents;
	for (Iterator<Cell> i = cellList.iterator(); i.hasNext();) {
	    Cell c = i.next();
	    Cell origin = c.getOrigin();
	    int idNumber = origin.getIdNumber();


	    int searchResult = Collections.binarySearch(list, idNumber);
	    System.out.println("[SimpleGrid.addOccupyingOrigins] " + idNumber
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
	LinkedList<Cell> cellList = grid[gx][gy][gz].contents;
	System.out.print("[" + x + "][" + y + "][" + z + "]  ");
	for (Iterator<Cell> i = cellList.iterator(); i.hasNext();) {
	    Cell c = i.next();
	    System.out.print(c.getIdNumber() + " ");
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
		    if (grid[actualX][actualY][actualZ].contents.size() > 0) {
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
		    for (Iterator<Cell> i = grid[actualX][actualY][actualZ].contents.iterator();
			 i.hasNext();) {
			Cell c = i.next();
			if (idNumber == c.getIdNumber()) {
			    System.out.println("[" + x + "][" + y+ "][" + z +"]: " + idNumber
					       + " found");
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
