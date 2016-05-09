
package angiogenesis;

import java.util.*;

import shared.*;

public class RuleResult {

    public double stalkElongationDistance = 0;
    //    public CellInterface newCell = null;
    public LinkedList<CellInterface> newCellList = null;

    public RuleResult(double stalkElongationDistance) {
	this.stalkElongationDistance = stalkElongationDistance;
    }

    public RuleResult(CellInterface newCell) {
	newCellList = new LinkedList<CellInterface>();
	if (newCell != null) {
	    newCellList.add(newCell);
	}
    }

    public RuleResult(LinkedList<CellInterface> newCellList) {
	this.newCellList = newCellList;
    }

}