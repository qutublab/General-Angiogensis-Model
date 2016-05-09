
package sharedMason;

import shared.*;

import sim.engine.*;
import sim.util.*;


public interface ConcentrationsInterface extends Steppable {


    public void step(SimState state);

    public void setStopObject(Stoppable stopObject);

    public void initialize(EnvironmentInterface e);

    public double getNgPerMl(EnvironmentInterface.ConcentrationType ct,
			     double x, double y, double z);

    public Point3D getGradient(EnvironmentInterface.ConcentrationType ct, Point3D p0, Point3D p1,
			       double maximumVarianceAngleRadians);

    public void testGetGradient();

    public double getCollagenConcentration();
    
    public double getNominalConcentration(EnvironmentInterface.ConcentrationType ct);


    public StateDiagramModelResult.InitialConditions getInitialConditionsDescriptor();
    public String getInitialConditionsString();

    // Returns the coordinates of the terminal point of a free vector
    // (with the argument as the initial point) pointing in the
    // direction of greatest concentration.  MAYBE: The length of the
    // free vector is a maximum of 1 and corresponds to the fraction
    // of the greatest possible concentration.
    //    public Point3D getConcentrationVector(EnvironmentInterface.ConcentrationType ct, Point3D location);
}
