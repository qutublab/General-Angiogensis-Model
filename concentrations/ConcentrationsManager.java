
/*
 * 10-14-2010
 * Version 0.2
 * Concentrations are read from a file instead of being hard coded.
 *
 */


/*
 Three ways to compute a "gradient" using a cell's 26 discrete neighbors

 1) Create a vector in the direction of each neighbor weighted by the
    neighbor's concentration.  The sum of these weighted vectors is
    returned.

 2) Find the set of unit vectors pointing to the neighbors with the
    maximum concentration.  Return the average (or sum) of those
    vectors.

 3) Find the set of unit vectors pointing to the neighbors with the
    maximum concentration.  return the average (or sum) of those
    vectors.

*/


package concentrations;

import sharedMason.*;
import shared.*;

import sim.engine.*;


import java.util.*;

public class ConcentrationsManager
    implements ConcentrationsInterface, Steppable {

    private static enum LabelType {VEGF_NG_PER_ML, BDNF_NG_PER_ML};

    private static final String VERSION_STRING = "0.2";

    private static final double VEGF_CONCENTRATION = -1;
    private static final double BDNF_CONCENTRATION = -1;
    private static final double ANG2_CONCENTRATION = 0;
    private static final double ANG1_CONCENTRATION = 0;

    private static EnvironmentInterface e;
    private static RandomInterface random;
    private static GridInterface grid;


    private static OutputBufferInterface buffer;

    private static double vegfNgPerMl = VEGF_CONCENTRATION;
    private static double bdnfNgPerMl = BDNF_CONCENTRATION;

    private static double spheroidRadius;


    private StateDiagramModelResult.InitialConditions initialConditionsDescriptor = null;
    private String initialConditionsStr = null;

    private static boolean ignoreDiscretizedSprouts = false;

    public void step(SimState state) {
    }

    public void setStopObject(Stoppable stopObject) {
    }

    public void initialize(EnvironmentInterface e) {

	this.e = e;
	random = e.getRandom();
	grid = e.getGrid();	

	ignoreDiscretizedSprouts = e.ignoreDiscretizedSprouts();

	String parametersFileName = e.getConcentrationsParametersFileName();

	initialConditionsDescriptor = e.getForcedInitialConditions();

	if (initialConditionsDescriptor == null) {
	    Parameters p = new Parameters(parametersFileName);
	    vegfNgPerMl = p.getVegfNgPerMl();
	    bdnfNgPerMl = p.getBdnfNgPerMl();
	    if (vegfNgPerMl == 0 && bdnfNgPerMl == 0) {
		initialConditionsDescriptor = StateDiagramModelResult.InitialConditions.V0B0;
	    }
	    if (vegfNgPerMl == 50 && bdnfNgPerMl == 0) {
		initialConditionsDescriptor = StateDiagramModelResult.InitialConditions.V50B0;
	    }
	    if (vegfNgPerMl == 25 && bdnfNgPerMl == 50) {
		initialConditionsDescriptor = StateDiagramModelResult.InitialConditions.V25B50;
	    }
	    if (vegfNgPerMl == 0 && bdnfNgPerMl == 50) {
		initialConditionsDescriptor = StateDiagramModelResult.InitialConditions.V0B50;
	    }
	    if (vegfNgPerMl == 0 && bdnfNgPerMl == 100) {
		initialConditionsDescriptor = StateDiagramModelResult.InitialConditions.V0B100;
	    }
	    if (vegfNgPerMl == 25 && bdnfNgPerMl == 25) {
		initialConditionsDescriptor = StateDiagramModelResult.InitialConditions.V25B25;
	    }
	}
	else {
	    switch (initialConditionsDescriptor) {
	    case V0B0:
		vegfNgPerMl = 0;
		bdnfNgPerMl = 0;
		break;
	    case V0B50:
		vegfNgPerMl = 0;
		bdnfNgPerMl = 50;
		break;
	    case V0B100:
		vegfNgPerMl = 0;
		bdnfNgPerMl = 100;
		break;
	    case V25B25:
		vegfNgPerMl = 25;
		bdnfNgPerMl = 25;
		break;
	    case V25B50:
		vegfNgPerMl = 25;
		bdnfNgPerMl = 50;
		break;
	    case V50B0:
		vegfNgPerMl = 50;
		bdnfNgPerMl = 0;
		break;
	    default:
		die("[ConcentrationsManager.initialze] Unexpected intial conditions descriptor: "
		    + initialConditionsDescriptor);
	    }
	}

	initialConditionsStr = "VEGF: " + vegfNgPerMl + "   BDNF: " + bdnfNgPerMl;

	buffer = e.getOutputBuffer();
	buffer.println("Concentrations manager version " + VERSION_STRING);
	buffer.println("VEGF concentration = " + vegfNgPerMl);
	buffer.println("BDNF concentration = " + bdnfNgPerMl);
	e.getLog().println("ConcentrationsManager: VEGF " + vegfNgPerMl + "  BDNF " + bdnfNgPerMl,
			   LogStreamInterface.MINIMUM_LOG_DETAIL);
	spheroidRadius = e.getSpheroidDiameterMicrons() / 2.0;

    }

    public StateDiagramModelResult.InitialConditions getInitialConditionsDescriptor() {
	return initialConditionsDescriptor;
    }

    public String getInitialConditionsString() {
	return initialConditionsStr;
    }


    public double getNominalConcentration(EnvironmentInterface.ConcentrationType ct) {
	double retVal = 0;
	switch (ct) {
	case VEGF:
	    retVal = vegfNgPerMl;
	    break;
	case BDNF:
	    retVal = bdnfNgPerMl;
	    break;
	case ANG1:
	    retVal = ANG1_CONCENTRATION;
	    break;
	case ANG2:
	    retVal = ANG2_CONCENTRATION;
	    break;
	default:
	    die("[ConcentrationsManager.getNominalConcentration] unknown concentration type: "
		+ ct);
	}
	return retVal;
    }

    public double getNgPerMl(EnvironmentInterface.ConcentrationType ct,
			     double x, double y, double z) {

	Point3D gridP = grid.translateToGrid(new Point3D(x, y, z));
	int gridX = (int) Math.round(gridP.x);
	int gridY = (int) Math.round(gridP.y);
	int gridZ = (int) Math.round(gridP.z);

	boolean openSpot = false;
	// Check if there is an unoccupied location
	
	// First check if the indices of the given location's neighbors
	// are valid
	if (!ignoreDiscretizedSprouts) {
	    if (gridX <= grid.getMinX() || gridX >= grid.getMaxX()
		|| gridY <= grid.getMinY() || gridY >= grid.getMaxY()
		|| gridZ <= grid.getMinZ() || gridZ >= grid.getMaxZ()) {
		die("[Concentrationsmanager.getNgPerMl] Neighbors of point (" + x + "," + y
		    + "," + z + ") are not within the grid boundaries");
	    }
	}
	for (int xIndex = gridX - 1; xIndex <= gridX + 1 && !openSpot; xIndex ++) {
	    for (int yIndex = gridY - 1; yIndex <= gridY + 1 && !openSpot; yIndex ++) {
		for (int zIndex = gridZ - 1; zIndex <= gridZ + 1 && !openSpot; zIndex ++) {
		    openSpot = !grid.isOccupied(xIndex, yIndex, zIndex);
		}
	    }
	}


	//	// remove!!
	//	boolean openSpot = true;

	double retVal = 0;
	if (openSpot) {
	    switch (ct) {
	    case VEGF:
		retVal = vegfNgPerMl;
		break;
	    case BDNF:
		retVal = bdnfNgPerMl;
		break;
	    case ANG1:
		retVal = ANG1_CONCENTRATION;
		break;
	    case ANG2:
		retVal = ANG2_CONCENTRATION;
		break;
	    default:
		die("[ConcentrationsManager.getNgPerMl] unknown concentration type: " + ct);
	    }
	}
	return retVal;
    }

    public double getCollagenConcentration() {
	return 0;
    }



    public void testGetGradient() {
	Point3D p = new Point3D(0, 0, 0);
	Point3D forward = new Point3D(1, 1, 1);
	double varianceAngleRadians = 55.001 * Math.PI / 180.0;
	
	System.out.println("[ConcentrationsManager.testGetGradient] p=" + p + "   forward="
			   + forward + "   varianceAngleRadians=" + varianceAngleRadians);
	Point3D g = getGradient(EnvironmentInterface.ConcentrationType.VEGF, p, forward, varianceAngleRadians);
	//check result
    }



    /*
     * Returns a vector relative to p pointing in the direction of
     * highest concentration.  Thus if v is returned, the front of the
     * tip cell should migrate from the point p towards the point p+v
     *
     * Note due to imprecise mathematical computations, the
     * maximumVarianceAngleRadians should slightly exceed the
     * theoretical value.  So for example instead of using pi/4 (45
     * degrees), use the radian equivalent of 45.1 degrees.
     */
    public Point3D getGradient(EnvironmentInterface.ConcentrationType ct, Point3D p,
			       Point3D forwardPoint, double maximumVarianceAngleRadians) {

	//	System.out.println("[ConcentrationsManager.getGradient] " + p);
	double distance = p.magnitude();
	//	if (distance <= spheroidRadius + 1) {
	    //	    System.out.println("[ConcentrationsManager.getGradient] distance to origin: "
	//			       + p.magnitude() + "(" + spheroidRadius + ")");
	//	}


	Point3D coneAxisVector = forwardPoint.minus(p);
	double coneAxisVectorMagnitude = coneAxisVector.magnitude();

	//	System.out.println("[ConcentrationsManager.getGradient] computing gradient at: " + p);

	// search p's 26 immediate grid neighbors

	// convert p's coordinates to grid coordinates
	Point3D gridP = grid.translateToGrid(p);
	int gridX = (int) Math.round(gridP.x);
	int gridY = (int) Math.round(gridP.y);
	int gridZ = (int) Math.round(gridP.z);

	//	System.out.println("[ConcentrationsManager.getGradient] grid location: " + gridP);
	

	int gridMinX = grid.getMinX();
	int gridMaxX = grid.getMaxX();
	int gridMinY = grid.getMinY();
	int gridMaxY = grid.getMaxY();
	int gridMinZ = grid.getMinZ();
	int gridMaxZ = grid.getMaxZ();

	
	// check if the indices of point's neighbors are valid
	if (!ignoreDiscretizedSprouts) {
	    if (gridX <= gridMinX || gridX >= gridMaxX
		|| gridY <= gridMinY || gridY >= gridMaxY
		|| gridZ <= gridMinZ || gridZ >= gridMaxZ) {
		die("[Concentrationsmanager.getGradient] Point's neighbors are not within the grid boundaries: "
		    + p + "(" + gridP + ")");
	    }
	}

	double max = 0;
	int maxCount = 0;
	// compute weighted total and maxTotal in case they are needed at a later time
	// maxTotal is sum of unit vectors pointing to neighbors at maximum concentration
	Point3D weightedTotal = new Point3D(0, 0, 0);
	Point3D maxTotal = new Point3D(0, 0, 0);
	// keep the locations with maximum concentrations
	LinkedList<Point3D> maxLocations = new LinkedList<Point3D>();
	// iterate over grid locations


	for (int x = gridX - 1; x <= gridX + 1; x++) {
	    for (int y = gridY - 1; y <= gridY + 1; y++) {
		for (int z = gridZ - 1; z <= gridZ + 1; z++) {
		    
		    //		    System.out.println("[ConcentrationsManager.getGradient] (" + x + "," + y
		    //				       + "," + z + ") occupied=" + grid.isOccupied(x,y,z));
		    

		    // note that when ignoreDiscretizedSprouts is
		    // true, the grid module accepts indices that are
		    // out of bounds and returns false

		    if (!grid.isOccupied(x, y, z)) {
			// convert x, y, z to continuous system coordinates
			Point3D contP = grid.translateFromGrid(new Point3D(x, y, z));
			// determine if contP is in the cone
			// 
			Point3D pointVector = contP.minus(p);
			double pointVectorMagnitude = pointVector.magnitude();
			double dotProduct = coneAxisVector.dot(pointVector);
			double cosine =
			    dotProduct / (pointVectorMagnitude * coneAxisVectorMagnitude); 
			// Due to imprecision of arithmetic, the
			// cosine computation may result in a values
			// just a bit beyond 1 (0 degrees) and -1 (180
			// degrees) resulting in undefined values from
			// Math.acos.
		       	cosine = Math.max(-1, Math.min(cosine, 1));

			//			System.out.println("[ConcentrationsManager.getGradient] dotProduct="
			//					   + dotProduct + "  pointVectorMagnitude="
			//					   + pointVectorMagnitude + "  coneAxisvectorMagntiude="
			//					   + coneAxisVectorMagnitude);
			//			System.out.println(cosine + "   " + round(cosine));

			double angleRadians = Math.acos(cosine);
			if (angleRadians < 0) {
			    angleRadians += 2 * Math.PI;
			}
			//			System.out.println("[ConcentrationsManager.getGradient] (" + x + "," + y
			//					   + "," + z + ") angle is "
			//					   + (angleRadians * 180.0 / Math.PI));
			
			// angleRadians is not a number (NaN) when
			// pointVectorMagnitude is 0, i.e. the
			// location of the tip of the cell is
			// examined.  For now, ignore this case
			// because the direction is undefined.
			// Perhaps in the future, the migration
			// magnitude can be proportional to the
			// diffence between the highest concentration
			// level and the concentration level at the
			// front of the cell.  Thus when the front of
			// the cell reaches the maximum concentration
			// level, the difference is 0 and there is no
			// migration.
			if ((!Double.isNaN(angleRadians))
			     && angleRadians <= maximumVarianceAngleRadians) {
			    //			    System.out.println("[ConcentrationsManager.getGradient] Considering "
			    //					       + contP);
			    double ngPerMl = getNgPerMl(ct, contP.x, contP.y, contP.z);
			    // convert contV to a vector relative to argument p
			    Point3D contV = contP.minus(p);
			    if (ngPerMl > max) {
				max = ngPerMl;
				maxLocations.clear();
				maxLocations.add(contV);
				maxTotal = contP.normalize();
				maxCount = 1;
			    }
			    else {
				if (ngPerMl == max) {
				    maxLocations.add(contV);
				    maxTotal = maxTotal.plus(contP.normalize());
				    maxCount++;
				}
			    }
			    weightedTotal = weightedTotal.plus(contP.normalize().mult(ngPerMl));
			}
		    }
		}
	    }
	}

	/*
	if (maxCount == 0) {
	    System.out.println("[ConcentrationsManager.getGradient] No open neighbors at "
			       + p);
	    LinkedList<Integer> occupyingCellList = new LinkedList<Integer>();
	    for (int x = gridX - 1; x <= gridX + 1; x++) {
		for (int y = gridY - 1; y <= gridY + 1; y++) {
		    for (int z = gridZ - 1; z <= gridZ + 1; z++) {
			grid.addOccupyingCellOrigins(x, y, z, occupyingCellList);
			grid.printOccupyingCells(x, y, z);
		    }
		}
	    }

	    e.emergency(occupyingCellList);
	}
	*/

	if (maxCount == 0) {
	    //	    System.out.println("[ConcentrationsManager.getGradient] maxCount is 0");
	    return null;
	}

	// Pick a vector at random
	int randomIndex = random.nextInt(maxCount);
	Point3D gradient = maxLocations.get(randomIndex);
	//	System.out.println("[ConcentrationsManager.getGradient] returning: " + gradient);
	return gradient;
    }

    public static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }

}
