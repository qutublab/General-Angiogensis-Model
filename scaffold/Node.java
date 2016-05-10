
/*
 * 5-29-2011 Node now uses Point3D objects
 */


package scaffold;

import shared.*;

import java.util.*;


public class Node {

    private Point3D location;
    private boolean inflection = false;


    public Node copy() {
	Node n = new Node(location);
	n.inflection = inflection;
	return n;
    }

    public void setInflection() {
	inflection = true;
    }

    public boolean isInflection() {
	return inflection;
    }

    public Node(Point3D location) {
	this.location = location.copy();
    }

    public Point3D getLocation() {
	return location.copy();
    }

    public String toString() {
	String s =
	    "Node[location=" + location
	    + ",inflection=" + inflection
	    + "]";
	return s;
    }
    

    public static void main(String[] args) {
	Node n = new Node(new Point3D(1, 2, 3));
	Node n2 = new Node(new Point3D(4, 5, 6));
	Node nc = n.copy();
	nc.setInflection();
	System.out.println(n);
	System.out.println(n2);
	System.out.println(nc);
	System.out.println(n.isInflection());
	System.out.println(n2.isInflection());
	System.out.println(nc.isInflection());
	System.out.println(n.getLocation());
	System.out.println(n2.getLocation());
	System.out.println(nc.getLocation());
    }

}
    
