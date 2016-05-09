package shared;

// import sim.util.Double3D;

import java.util.*;

/*
 * Interface for methods of the Cell class that can be invoked by
 * angiogenic rule sets.
 */

public interface CellInterface {


    public enum CellPosition {TIP, STALK, REAR, CLUSTER};

    /*
    public void translate(double deltaX, double deltaY, double deltaZ,
			  EnvironmentInterface e);
    */

    //    public GrowthRole getGrowthRole();

    public int getIdNumber();

    public EnvironmentInterface.CellState getCellState();


    public boolean isTipCell();
    public boolean isStalkCell();

    public boolean isInhibited();

    //    public Point3D getFrontNodeLocation();

    //    public void move(NodeInterface.ShapeType shape, double[] params);

    //    public void move();

    //    public void divide(EnvironmentInterface e);


    public int activate();
    public double migrate(Point3D newFrontPoint);
    
    // returns new cell if the cell divided after the volume increase or null
    public LinkedList<CellInterface> proliferate(double volumeIncrease, EnvironmentInterface e);

    public void remove(EnvironmentInterface e);

    //    public LinkedList<Double3D> getNodeLocations();

    public double getAvgNgPerMl(EnvironmentInterface.ConcentrationType ct);

    public Point3D getGradient(EnvironmentInterface.ConcentrationType ct,
			       double maximumVarianceAngleRadians);

    public double getVolumeCubicMicrons();

    public double getLength();

    public LinkedList<Point3D> getNodeLocations();

    public boolean hasBranchAhead();
    
    public boolean canBranch();

    public CellInterface branch(Point3D p, double initialBranchRadiusMicrons);

    public Point3D getFrontOrientation();


    public double removableVolumeCubicMicrons();

    public CellPosition getCellPosition();

    public Object getLocalStorage();

    public CellInterface getSuccessor();
    public CellInterface getPredecessor();

    public Point3D getFrontLocation();

    public void resize();

    public LinkedList<Parameter> getParameters();
    public String setParameters(LinkedList<Parameter> paramList);

    // Returns true if tip cell has its own stalk cell
    // Signals an error when not a tip cell or not on a branch
    public boolean hasBranchStalkCell();



    // returns concentration at cell's frontmost node
    //    public double getFrontNgPerMl(ConcentrationsInterface.ConcentrationType ct);

    // Returns the coordinates of the terminal point of a free vector
    // (with initial point the front node of the tip cell) pointing in
    // the direction of greatest concentration.
    /*
      public Point3D getConcentrationVector(EnvironmentInterface.ConcentrationType ct);
    */

    /*
    public void create(LinkedList<NodeData> nodeDatalist,
		       EnvironmentInterface e);
    */
}
