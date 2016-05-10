


package scaffold;

import sim.util.Int3D;
import sim.util.Double3D;

import java.util.*;

import shared.*;

public class Capsule implements Shape {

    double x1;
    double y1;
    double z1;
    double x2;
    double y2;
    double z2;
    double radius;

    public Capsule(double x1,
		   double y1, 
		   double z1, 
		   double x2,
		   double y2, 
		   double z2, 
		   double radius) {
	this.x1 = x1;
	this.y1 = y1;
	this.z1 = z1;
	this.x2 = x2;
	this.y2 = y2;
	this.z2 = z2;
	this.radius = radius;
    }

    public Capsule(double[] params) {
	if (params.length != 7) {
	    Environment.die("[Capsule] array argument has " + params.length
			    + " elements.");
	}
	x1 = params[0];
	y1 = params[1];
	z1 = params[2];
	x2 = params[3];
	y2 = params[4];
	z2 = params[5];
	radius = params[6];
    }


    public NodeInterface.ShapeType getShapeType() {
	return NodeInterface.ShapeType.CAPSULE;
    }

    public double[] getParams() {
	return null;
    }



    public Point3D getLocation() {
	return new Point3D(x1, y1, z1);
    }


    public String toString() {
	String s =
	    "Capsule[x1=" + x1
	    + ",y1=" + y1
	    + ",z1=" + z1
	    + ",x2=" + x2
	    + ",y2=" + y2
	    + ",z2=" + z2
	    + ",r=" + radius
	    + "]";
	return s;
    }


    public LinkedList<Int3D> createRepresentation(Environment env) {
	System.err.println("[Capsule.createRepresentation] not implemented!");
	return new LinkedList<Int3D>();
    }

    public void translate(double deltaX, double deltaY, double deltaZ) {
	x1 += deltaX;
	y1 += deltaY;
	z1 += deltaZ;
	x2 += deltaX;
	y2 += deltaY;
	z2 += deltaZ;
    }


}
