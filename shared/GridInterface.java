
package shared;

import java.util.*;

public interface GridInterface {
    
    public int getMaxX();
    public int getMinX();
    public int getMaxY();
    public int getMinY();
    public int getMaxZ();
    public int getMinZ();

    public boolean isOccupied(int x, int y, int z);

    public Point3D translateToGrid(Point3D p);
    public Point3D translateFromGrid(Point3D p);

    public void printOccupyingCells(int x, int y, int z);

    //    public void addOccupyingCellOrigins(int x, int y, int z, LinkedList<Integer> list);

}