
package shared;

import java.util.*;

//import ec.util.MersenneTwisterFast;

/*
 * Interface for methods of the Environment class that can be invoked
 * by angiogenic rule sets.  All run-time information needed by
 * angiogenic rule sets will be accesed via the Environment class.
 * This includes such things as command line parameters and values
 * from data structures that change during the course of a simulation.
 */

public interface EnvironmentInterface {

    public enum ConcentrationType {VEGF, BDNF, ANG1, ANG2};

    public enum SimulationMode {NORMAL, GENETIC_ALGORITHM};

    // spheroid cells are idle, then become quiescent
    public enum CellState {IDLE, QUIESCENT, ACTIVE};

    public void emergency(LinkedList<Integer> lst);

    public String getVersionString();

    public LogStreamInterface getLog();

    public double getTimeStepLengthInSeconds();

    //    public ConcentrationsManagerInterface getConcentrationsManager();

    public RandomInterface getRandom();


    public boolean dll4IsPresent();

    public double getCollagenConcentration();

    public GridInterface getGrid();

    
    public double getMigrationMagnitudeFactor();


    public OutputBufferInterface getOutputBuffer();

    public String getAngiogenesisParametersFileName();

    public String getConcentrationsParametersFileName();

    public String getGuiParametersFileName();

    public boolean ignoreDiscretizedSprouts();

    public StateDiagramModel getStateDiagramModel();

    public SimulationMode getSimulationMode();

    public StateDiagramModelResult.InitialConditions getForcedInitialConditions();
    public StateDiagramModelResult.InitialConditions getInitialConditionsDescriptor();

    public String getInitialConditionsString();

    public double getTotalSimulationHours();
    public String getRuleSetName();

    public double getSpheroidDiameterMicrons();
    public Point3D getSpheroidCenter();
    public double getEstimatedMaximumExtentMicrons();

    // returns number of completed time steps
    public long stepsCompleted();


    public int getSimulationLengthInTimeSteps();


    // Returns a list of cell geometry.  If the onlyChanged argument
    // is true, then only those cells that have been recently changed
    // by the user are returned.
    public LinkedList<CellGeometry> getCellGeometry(boolean onlyChanged);

    /*
    double getVoxelGridScale();
    double getVoxelXOrigin();
    double getVoxelYOrigin();
    double getVoxelZOrigin();
    void createOneCell(double[] cellData);
    */

}
