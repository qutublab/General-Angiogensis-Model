
/*
 * 10-13-2010 
 * Branching probabilities simplified and take into
 * consideration BDNF level.
 *
 * 2-25-2011
 * Location of branch tip now determined by using spherical
 * ccordinates.
 *
 * 6-8-2010
 * Branched cells are marked as such and the last step for which they
 * will undergo special proliferation is recorded.
 */


/*
 * If the x-second branch probability is B, what is the branch probability
 * for y seconds?
 *
 * According to a x-second branch probability, after s seconds, there
 * is a nonbranching probability of (1-B)^(s/x) (and hence a branch
 * probability of 1-(1-B)^(s/x))
 *
 * According to a y-second branching probability of B', after s
 * seconds, there is a nonbranching probability of (1-B')^(s/y).
 *
 * Thus:
 *             (1-B)^(s/x) = (1-B')^(s/y)
 *     ((1-B)^(s/x))^(y/s) = ((1-B')^(s/y))^(y/s)
 *             (1-B)^(y/x) = 1-B'
 *                      B' = 1 - (1-B)^(y/x)
 */



package angiogenesis;

import shared.*;

import java.util.*;


public class BranchingRule extends Rule {

    private static final int SECONDS_PER_HOUR = 60 * 60;

    private static double maximumBranchAngleRadians = Math.PI / 2.0;  
    private static double minimumBranchAngleRadians = Math.PI / 20.0; 
    private static double branchAngleRangeRadians =
	maximumBranchAngleRadians - minimumBranchAngleRadians;

    private static double initialBranchLengthMicrons = 3;
    private static double initialBranchRadiusMicrons;
    private static double initialBranchVolume;
    private static double branchVolumeRatioThreshold;

    private static final double BRANCH_DATA_WINDOW_IN_SECONDS = 2.0 * 60.0 * 60.0; // 2 hours


    private static RandomInterface random;
    private static boolean dll4IsPresent;

    private static double timeRatio;

    private static boolean rearCellBranchingDisabled;
    private static boolean allCellBranchingDisabled;

    private static int specialBranchTimeSteps;

    public BranchingRule(Parameters p, EnvironmentInterface e) {
	ruleName = "Branching";
	ruleIdentifier = "B";
	versionString = "0.2";

	random = e.getRandom();
	dll4IsPresent = e.dll4IsPresent();
	double timeStepLengthInSeconds = e.getTimeStepLengthInSeconds();
	timeRatio = timeStepLengthInSeconds / BRANCH_DATA_WINDOW_IN_SECONDS;

	double minimumBranchAngleDegrees = p.getMinimumBranchAngleDegrees();
	double maximumBranchAngleDegrees = p.getMaximumBranchAngleDegrees();
	if (minimumBranchAngleDegrees > maximumBranchAngleDegrees) {
	    SimpleRuleSet.die("Minimum branch angle parameter " + minimumBranchAngleDegrees
			      + " is not less than or equal to maximum branch angle parameter "
			      + maximumBranchAngleDegrees);
	}
	double degreesToRadiansFactor = Math.PI / 180.0;
	minimumBranchAngleRadians = minimumBranchAngleDegrees * degreesToRadiansFactor;
	maximumBranchAngleRadians = maximumBranchAngleDegrees * degreesToRadiansFactor;
	branchAngleRangeRadians = maximumBranchAngleRadians - minimumBranchAngleRadians;

	initialBranchLengthMicrons = p.getInitialBranchLengthMicrons();
	initialBranchRadiusMicrons = p.getInitialBranchRadiusMicrons();
	initialBranchVolume =
	    Math.PI * initialBranchRadiusMicrons * initialBranchRadiusMicrons
	    * initialBranchLengthMicrons;
	branchVolumeRatioThreshold = p.getCellBranchVolumeRatioThreshold();

	rearCellBranchingDisabled = p.disableRearCellBranching();
	allCellBranchingDisabled = p.disableAllCellBranching();

	double specialBranchTimeSeconds =
	    p.getSpecialBranchTimeHours() * SECONDS_PER_HOUR;
	specialBranchTimeSteps =
	    (int) Math.round(specialBranchTimeSeconds
			     / timeStepLengthInSeconds);

    }


    private static double branchingProbability(CellInterface c) {
	double prob = 0;
	double vegfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.VEGF);
	double bdnfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.BDNF);

	if (vegfNgPerMl < 0 || bdnfNgPerMl < 0) {
	    SimpleRuleSet.die("[BranchingRule.branchingProbability] Negative concentration [vegf]="
			      + vegfNgPerMl + "  [bdnf]=" + bdnfNgPerMl);
	}

	if (vegfNgPerMl == 0 && bdnfNgPerMl == 0) {
	    prob = .05;
	}
	else {
	    prob = .1;
	}
	

	/*
	 * The probability has been computed for a time window of size
	 * BRANCH_DATA_WINDOW_IN_SECONDS.  Adjust the probability for
	 * the time step of the simulation.
	 */
	double adjustedProb = 1 - (Math.pow(1 - prob, timeRatio));
	return adjustedProb;
    }
	    

    // New computation for branch tip
    private static Point3D computeBranchTip(CellInterface c) {
	double branchLength = initialBranchLengthMicrons;

	// The branch is formed at the rear of the cell and shares the
	// cell's rearmost point.  The branch angle is measured
	// between the the cell's rearmost segment and the new cell.

	// Compute the spherical coordinates of the branch tip
	// assuming a system where the rearmost point of the cell is
	// at the origin, and its segment is aligned with the zenith
	// direction.  The branch angle is the angle of inclination
	// and must be within the range specified by the model
	// parameters.  The azimuth angle is determined completely at
	// random.  The point's radius is given by the branchLength
	// argument.

	double branchInclinationRadians =
	    minimumBranchAngleRadians + (random.nextDouble() * branchAngleRangeRadians);
	double branchAzimuthRadians = random.nextDouble() * 2 * Math.PI;

	// These coordinates are: a) spherical and b) relative to a
	// system where the rearmost segment of the cell is coincident
	// with the zenith axis.  We must: a) convert the coordinates
	// to cartesian space; b) rotate the point about the origin in
	// exactly the same way that the posited z-axis must be
	// rotated to have the same orientation as the rearmost cell
	// segment orginally had; and c) translate the points in the
	// same manner that the rearmost segment must be translated to
	// place it in its original location.

	// Convert the spherical coordinates to a cartesian system
	// where the positive z-axis is coincident with the zenith
	// direction and the positive x-axis is coincident with the
	// azimuth axis.

	double branchTipZ = branchLength * Math.cos(branchInclinationRadians);
	// To compute x and y coordinates, need length of branch
	// projected onto xy plane
	double xyBranchLength = branchLength * Math.sin(branchInclinationRadians);
	// Azimuth angle is in direction from positive x-axis towards
	// positive y-axis
	double branchTipX = xyBranchLength * Math.cos(branchAzimuthRadians);
	double branchTipY = xyBranchLength * Math.sin(branchAzimuthRadians);

	Point3D branchTip = new Point3D(branchTipX, branchTipY, branchTipZ);

	// Recall that rearmost segment of the cell is aligned with
	// the z-axis.  Rotate the coordinate system including the
	// coordinates of the branch tip so that the z-axis points in
	// the same direction as the rearmost segment of the cell.
	// Then translate the coordinates system which will make the
	// nonnegative portion of the original z-axis coincident with
	// the rearmost cell segment.  The resulting transformation
	// yields the cartesian coordinates of the branch tip in the
	// system used by the rest of the model.

	// Get data for rearmost segment of cell
	LinkedList<Point3D> nodes = c.getNodeLocations();
	int numberOfNodes = nodes.size();
	Point3D a = nodes.removeLast();
	Point3D b = nodes.removeLast();

      	// Move segment so it starts from the origin
	Point3D p = b.minus(a);

	// Compute spherical coordinates of p.  Rotations in the
	// inclination and azimuth angles will then be applied to the
	// branch tip in order to compute its coordinates in the
	// system used by the rest of the model.

	double pRadius = p.magnitude();
	// radius projected onto xy plane
	double pXYRadius = Math.sqrt((p.x * p.x) + (p.y * p.y));
	double pInclinationRadians = Math.asin(pXYRadius / pRadius);
	// Since pXYRadius and pRadius are always positive, we need to
	// detect when the inclination angle is greater than 90
	// degrees (pi/2).  This happens when p's zcoordinate is
	// negative.
	if (p.z < 0) {
	    pInclinationRadians = Math.PI - pInclinationRadians;
	}
	double pAzimuthRadians = Math.acos(p.x / pXYRadius);
	// The arcosine ranges from 0 to 180 degrees (0 to pi).
	// However the azimuth can range from 0 to 360 degrees.
	// Detect angles greater than 180 degrees by examining the
	// point's y coordinate.  Negative angles are used instead of
	// angles between 180 and 360 degrees.
	if (p.y < 0) {
	    pAzimuthRadians = - pAzimuthRadians;
	}
	
	// If the inclination angle is either 0 or 180 degrees, then
	// the azimuth is undefined, so force it to 0.
	if (pInclinationRadians == 0 || pInclinationRadians == Math.PI) {
	    pAzimuthRadians = 0;
	}


	// Rotate and translate the branch tip to correspond to the
	// original coordinate system.

	// First rotate it
	Point3D rotatedBranchTip = branchTip.rotateY(pInclinationRadians).rotateZ(pAzimuthRadians);


	// Before translating, check that the rotations have been done
	// correctly i.e. the branch length and angle are as desired.
	double tolerance = .0000001;
	if (Math.abs(branchLength - rotatedBranchTip.magnitude()) > tolerance) {
	    SimpleRuleSet.die("[BranchingRule.computeBranchTip] length test failed: "
			      + "goal length=" + branchLength + "  actual length="
			      + rotatedBranchTip.magnitude());
	}
	double angle = Math.acos(rotatedBranchTip.dot(p) / (branchLength * p.magnitude()));	
	if (Math.abs(angle - branchInclinationRadians) > tolerance) {
	    SimpleRuleSet.die("[BranchingRule.computeBranchTip] angle test failed: "
			      + "desired angle=" + branchInclinationRadians + "  actual angle="
			      + angle);
	}
	

	// translate the branch tip to its proper place
	Point3D trueBranchTip = rotatedBranchTip.plus(a);

	return trueBranchTip;

    }


    private static double sqr(double n) {
	return n * n;
    }

	
    public RuleResult act(CellInterface c,
			  Storage s,
			  EnvironmentInterface e) {
	log.println("[BranchingRule.act] Begin cell "
		    + c.getIdNumber() + " branching activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);
	if (allCellBranchingDisabled
	    || (rearCellBranchingDisabled
		&& c.getCellPosition() == CellInterface.CellPosition.REAR)) {
	    log.println("[BranchingRule.act] End cell "
			+ c.getIdNumber() + " branching activity",
			LogStreamInterface.BASIC_LOG_DETAIL);
	    return null;
	}

	// don't branch if the cell is inhibited or its succesor
	// cannot support the branch
	if (c.isInhibited() || (!c.canBranch())) {
	    log.println("[BranchingRule.act] End cell "
			+ c.getIdNumber() + " branching activity",
			LogStreamInterface.BASIC_LOG_DETAIL);	    
	    return null;
	}
	CellInterface branchCell = null;
	double prob = branchingProbability(c);
	double rand = random.nextDouble();
	if (rand < prob) {
	    if (initialBranchVolume <= c.removableVolumeCubicMicrons()) {
		Point3D branchTip = computeBranchTip(c);
		branchCell = c.branch(branchTip, initialBranchRadiusMicrons);
		if (branchCell != null) {
		    Storage branchCellStorage =
			(Storage) branchCell.getLocalStorage();
		    s.copyTo(branchCellStorage);
		    // The original cell may have itself divided in
		    // the same time step before branching, so its
		    // divided flag may be set to true.  Force the
		    // flag to false;
		    branchCellStorage.divided = false;
		    branchCellStorage.isBranch = true;
		    branchCellStorage.lastSpecialBranchStep =
			e.stepsCompleted() + specialBranchTimeSteps;
		}
	    }
	}
	log.println("[BranchingRule.act] End cell "
		    + c.getIdNumber() + " branching activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);
	return new RuleResult(branchCell);
    }


    public static void main(String[] args) {

	//	BranchingRule.random = new TestRandom(0);
	//	computeBranchTip(null, 1);
    }


}