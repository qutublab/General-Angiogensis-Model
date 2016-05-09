
package shared;

public class Shape {

    public enum ShapeType {SPHERE, CYLINDER};

    public ShapeType shape = null;
    public Point3D primaryLocation;
    public Point3D secondaryLocation;
    public double dimension;

    public Shape(Point3D primaryLocation, double dimension) {
	shape = ShapeType.SPHERE;
	this.primaryLocation = primaryLocation;
	this.dimension = dimension;
    }

    public Shape(Point3D primaryLocation, Point3D secondaryLocation,
		 double dimension) {
	shape = ShapeType.CYLINDER;
	this.primaryLocation = primaryLocation;
	this.secondaryLocation = secondaryLocation;
	this.dimension = dimension;
    }

    public String toString() {
	String retStr;
	retStr =
	    "Shape[shape=" + shape
	    + ",primaryLocation=" + primaryLocation
	    + ",secondaryLocation=" + secondaryLocation
	    + ",dimension=" + dimension
	    + "]";
	return retStr;
    }


}