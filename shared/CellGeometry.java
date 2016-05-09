
package shared;

import java.util.*; 

public class CellGeometry {
    public CellInterface cell;
    public LinkedList<Shape> cellShapes;
    public CellGeometry(CellInterface cell, LinkedList<Shape> cellShapes) {
	this.cell = cell;
	this.cellShapes = cellShapes;
    }

    public String toString() {
	String shapes = null;
	boolean first = true;
	for (Iterator<Shape> i = cellShapes.iterator(); i.hasNext();) {
	    Shape s = i.next();
	    if (first) {
		shapes = "[" + s.toString();
		first = false;
	    }
	    else {
		shapes += "," + s.toString();
	    }
	}
	shapes += "]";
	String retStr;
	retStr =
	    "CellGeometry[cell=" + cell.getIdNumber()
	    + ",cellShapes=" + shapes
	    + "]";
	return retStr;
    }

}