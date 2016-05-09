
/*
 * 10-13-2010
 * - Changed migration magnitude to: baseline + .01[bdnf] + .04[vegf]
 * - Changed persistence vector weight from .3 to .5
 *
 * 2-27-2011
 * - Constants read from parameter file
 *
 * 3-2-2011
 * - generateRandomUnitVector changed to create coordinates in -.5 to
 * +.5 range instead of 0 to 1 range
 * - Migration direction computed to lie within a user specified cone
 *
 * 5-13-2011
 * - Changed migration magnitude computation
 *
 * 6-15-2011 
 * - Added integrity check for migration direction.  The maximum
 * angular difference between the a cell's persistence direction and
 * any migration direction is determined by computing the vector sum
 * of the weighted persistence vector, the weighted concentration
 * vector (whose maximum angle is expressed as a model parameter), and
 * the weighted random vector.  Note that the maximum angular
 * difference is when the concentration vector is at its extreme, and
 * the random vector is at a right angle to the sum of the persistence
 * and concentration vectors.
 */


/*
  Migration direction is within a cone at the front of the sprout
  specified by the user.  The apex of the cone is the front point of
  the sprout, and its axis is colinear with the sprouts two frontmost
  points.  The user specifies an angle, theta, (relative to the axis)
  at which the cone resides.  Thus any line passing through the apex
  and any other point on the cone is at an angle of theta with the
  axis.

  See: http://www.geometrictools.com/Documentation/IntersectionLineCone.pdf

  Let A be the apex and B be another point on the axis within the
  cone.  A point P on the surface of the cone must satisfy the
  property given above which can be expressed via a vector dot
  product:

  AB dot AP = |AB||AP| cos(theta)

  where AB is the vector from A to B and AP is the vector from A to P.

  The migration direction must be within the cone specified by the
  theta parameter.  Note the that the migration direction is
  determined by the weighted sum of three vectors: gradient,
  persistence, and random.  The gradient vector is requested from the
  cell and is guaranteed to be within the cone.  Because the
  persistence vector is colinear with the cone axis, the sum of it and
  the gradient is also within the cone.  Thus, the random vector is
  the only component that can cause the direction to be outside of the
  cone.

  When the random vector causes the direction to be outside the cone,
  the migration direction will be altered to be on the surface of the
  cone as follows.  Let Q be the point that is the result of applying
  the gradient and persistence vectors to the apex A.  Let R be the
  point that is the result of applying the random vector to Q.  Since
  Q is inside the cone and R is outside the cone, there must be a
  point on the QR line segment that is on the cone.  This point on the
  cone will be the end point (starting from A) of the migration
  direction.

  Let the points A, B, Q, and R above be:

  A = (xa, ya, za)
  B = (xb, yb, zb)
  Q = (xq, yq, zq)
  R = (xr, yr, zr)

  A point on the line from Q to R can be described by equations
  parameterized b t:

  x = xq + t(xr-xq)
  y = yq + t(yr-yq)
  z = zq + t(zr-zq)

  Let a point on this line be denoted as P(t).  Note that P(0)=Q and
  P(1)=R.  Thus, P(t) is on the cone for some value of t between 0 and
  1.

  Using the dot product equation above we get an equation with free
  variable t:

  AB dot AP(t) = |AB||AP(t)| cos(theta)

  (xb-xa,yb-ya,zb-za) dot ((xq+t(xr-xq))-xa,(yq+t(yr-yq))-ya,(zq+t(zr-zq))-za)
  =
  sqrt((xb-xa)^2 + (yb-ya)^2 + (zb-za)^2) 
  sqrt(((xq+t(xr-xq))-xa)^2 + ((yq+t(yr-yq))-ya)^2 + ((zq+t(zr-zq))-za)^2)
  cos(theta)

  Solve for t:

  (xb-xa)((xq+t(xr-xq))-xa) + (yb-ya)((yq+t(yr-yq))-ya) + (zb-za)((zq+t(zr-zq))-za)
  =
  sqrt((xb-xa)^2 + (yb-ya)^2 + (zb-za)^2) 
  sqrt(((xq+t(xr-xq))-xa)^2 + ((yq+t(yr-yq))-ya)^2 + ((zq+t(zr-zq))-za)^2)
  cos(theta)

  Let xba = xb - xa    yba = yb - ya    zba = zb - za
      xrq = xr - xq    yrq = yr - yq    zrq = zr - zq

  xba((xq+t(xrq))-xa) + yba((yq+t(yrq))-ya) + zba((zq+t(zrq))-za)
  =
  sqrt((xba)^2 + (yba)^2 + (zba)^2) 
  sqrt(((xq+t(xrq))-xa)^2 + ((yq+t(yrq))-ya)^2 + ((zq+t(zrq))-za)^2)
  cos(theta)

  Let xqa = xq - xa    yqa = yq - ya    zqa = zq - za

  xba(xqa+t(xrq)) + yba(yqa+t(yrq)) + zba(zqa+t(zrq))
  =
  sqrt((xba)^2 + (yba)^2 + (zba)^2) 
  sqrt((xqa+t(xrq))^2 + (yqa+t(yrq))^2 + (zqa+t(zrq))^2)
  cos(theta)

  xba(xqa) + t(xba)(xrq) + yba(yqa)+ t(yba)(yrq)) + zba(zqa) + t(zba)(zrq)
  =
  sqrt((xba)^2 + (yba)^2 + (zba)^2) 
  sqrt((xqa+t(xrq))^2 + (yqa+t(yrq))^2 + (zqa+t(zrq))^2)
  cos(theta)

  xba(xqa) + yba(yqa) + zba(zqa) 
  + t((xba)(xrq) + (yba)(yrq)) + (zba)(zrq))
  =
  sqrt((xba)^2 + (yba)^2 + (zba)^2) 
  sqrt((xqa+t(xrq))^2 + (yqa+t(yrq))^2 + (zqa+t(zrq))^2)
  cos(theta)

  Let c1 = xba(xqa) + yba(yqa) + zba(zqa) 
      c2 = ((xba)(xrq) + (yba)(yrq)) + (zba)(zrq))
      c3 = (xba)^2 + (yba)^2 + (zba)^2

  c1 + (c2)t
  =
  sqrt(c3) 
  sqrt((xqa+t(xrq))^2 + (yqa+t(yrq))^2 + (zqa+t(zrq))^2)
  cos(theta)

  (c1 + (c2)t)^2
  =
  c3
  ((xqa+t(xrq))^2 + (yqa+t(yrq))^2 + (zqa+t(zrq))^2)
  cos^2(theta)

  (c2)^2(t^2) + 2(c1)(c2)t + (c1)^2
  =
  c3
  ((xrq)^2(t^2) + 2(xqa)(xrq)t + (xqa)^2
   + (yrq)^2(t^2) + 2(yqa)(yrq)t + (yqa)^2
   + (zrq)^2(t^2) + 2(zqa)(zrq)t + (zqa)+2)
  cos^2(theta)

  Let c4 = (xrq)^2 + (yrq)^2 + (zrq)^2
      c5 = 2((xqa)(xrq) + (yqa)(yrq) + (zqa)(zrq))
      c6 = (xqa)^2 + (yqa)^2 + (zqa)^2

  (c2)^2(t^2) + 2(c1)(c2)t + (c1)^2
  =
  c3
  ((c4)(t^2) + (c5)t + c6)
  cos^2(theta)

  Let c7 = (c3)(c4)cos^2(theta)
      c8 = (c3)(c5)cos^2(theta)
      c9 = (c3)(c6)cos^2(theta)

  (c2)^2(t^2) + 2(c1)(c2)t + (c1)^2
  =
  (c7)(t^2) + (c8)t + c9

  ((c2)^2 - c7)(t^2) + (2(c1)(c2) - c8)t + (c1)^2 - c9
  =
  0

  This can be solved by the quadratic formula.  Note that the
  quadratic equation uses cos^2.  This means that solutions for t can
  yield both the desired angle and its supplementary angle.  Hence,
  the desired solution for t is the smallest nonnegative value.

*/


package angiogenesis;

import shared.*;

public class MigrationRule extends Rule {


    protected static final double SECONDS_PER_HOUR = 60.0 * 60.0;


    private static double migrationMagnitudeVegfCoefficient;

    protected static double baselineMicronsPerHour = 6.2;

    protected double concentrationVectorWeight = 1;
    protected double persistenceVectorWeight = .5; //.3;
    protected double randomVectorWeight = .05;
    
    // limit on how much the migration direction can vary from the
    // direction that the front of the cell is currently facing
    protected double maximumVarianceAngleDegrees;
    protected double maximumVarianceAngleRadians;
    protected double cosineMaximumVarianceAngle;
    
    protected double maximumDeflectionRadians;

    
    protected double maximumChemotacticMicronsPerHour = 11;
    protected double maximumMigrationMicronsPerHour = 11;
    protected double collagenMigrationFactor = 4;
    
    
    protected static RandomInterface random;
    
    protected static double baselineMigration;
    //    private static double maximumChemotacticMigration;
    protected static double maximumMigrationMagnitude;    
    protected static double haptotacticMigration;
    
    protected static double migrationMagnitudeFactor = 1;

    protected static boolean migrationDisabled;

    
    public MigrationRule() {
    }
    

    private static void testComputeMaximumDeflectionRadians() {
	double persistenceWeight = 1;
	double concentrationWeight = 0;
	double randomWeight = 1;
	double maximumVarianceRadians = 90 * (Math.PI / 180.0);

	double maxDeflection =
	    computeMaximumDeflectionRadians(persistenceWeight,
					    concentrationWeight,
					    randomWeight,
					    maximumVarianceRadians);
	double maxDeflectionDegrees = maxDeflection * (180.0 / Math.PI);
	double tolerance = 0.000000001;
	double tan = 2;
	double expectedAngle = 45 * (Math.PI / 180.0);
	if (Math.abs(maxDeflection - expectedAngle) > tolerance) {
	    SimpleRuleSet.die("[MigrationRule.testComputeMaximumDeflectionRadians] "
			      + maxDeflection + " expected=" + expectedAngle);
	}
	System.out.println("[MigrationRule.testComputeMaximumDeflectionRadians] passed");


    }


    protected static double computeMaximumDeflectionRadians(double persistenceVectorWeight,
							    double concentrationVectorWeight,
							    double randomVectorWeight,
							    double maximumVarianceRadians) {
	// Assume that the maximum deflection is in the XY plane and
	// the persistence vector is coincident with the y-axis.

	Point3D persistence = new Point3D(0, persistenceVectorWeight, 0);

	//  Compute the maximum angle of the sum of the persistence
	//  and concentration vector with respect to the persistence
	//  vector.  The concentration vector can be as many as
	//  maximumVarianceradians radians from the persistence
	//  vector.
	//
	//  First determine the x and y coordinates of the
	//  concentration vector at the maximum angle.
	//
        //  sqrt(conc.x^2 + conc.y^2) = concentrationVectorWeight
	//  conc.x^2 + conc.y^2 = concentrationVectorWeight^2
	//  conc.x = sqrt(concentrationVectorWeight^2 - conc.y^2)
	//
	//  tan(maxVarianceRadians) = conc.x / conc.y
	//  tan(maxVarianceRadians)
	//     = sqrt(concentrationVectorWeight^2 - conc.y^2) / conc.y
	//  tan(maxVarianceRadians)conc.y 
	//    = sqrt(concentrationVectorWeight^2 - conc.y^2)
	//  tan^2(maxVarianceRadians)conc.y^2 
	//    = concentrationVectorWeight^2 - conc.y^2
	//  tan^2(maxVarianceRadians)conc.y^2 + conc.y^2
	//    = concentrationVectorWeight^2
	//  conc.y^2(1 + tan^2(maxVarianceRadians))
	//    = concentrationVectorWeight^2
	//  conc.y^2
	//    = concentrationVectorWeight^2 / (1 + tan^2(maxVarianceRadians))
	//  conc.y
	//    = sqrt(concentrationVectorWeight^2
	//	     / (1 + tan^2(maxVarianceRadians)))

	double concentrationVectorWeightSquared = 
	    Math.pow(concentrationVectorWeight, 2);

	double concYSquared =
	    concentrationVectorWeightSquared
	    / (1 + Math.pow(Math.tan(maximumVarianceRadians), 2));
	double concY = Math.sqrt(concYSquared);

	double concX =
	    Math.sqrt(concentrationVectorWeightSquared - concYSquared);

	Point3D concentration = new Point3D(concX, concY, 0);

	Point3D sumPersistenceConcentration =
	    persistence.plus(concentration);
	

	// Second, compute the angle between
	// sumPersistenceConcentration and persistence.
	double persistenceConcentrationDeflectionRadians =
	    Math.abs(persistence.angleRadians(sumPersistenceConcentration));

	// Now account for additional deflection from the random
	// vector.  The maxiumum deflection from the random vector
	// occurs when it is normal to the sum of the persistence and
	// concentration vectors and pointing away from the
	// persistence vector.

	double randomDeflectionRadians =
	    Math.abs(Math.atan(randomVectorWeight
			       / sumPersistenceConcentration.magnitude()));
	double maximumDeflectionRadians =
	    persistenceConcentrationDeflectionRadians
	    + randomDeflectionRadians;
	
	return Math.min(maximumDeflectionRadians, maximumVarianceRadians);
    }

    public MigrationRule(Parameters p, EnvironmentInterface e) {
	ruleName = "Migration";
	ruleIdentifier = "M";
	versionString = "0.2";
	random = e.getRandom();
	double timeStepLengthInSeconds = e.getTimeStepLengthInSeconds();
	
	migrationMagnitudeVegfCoefficient = p.getMigrationMagnitudeVegfCoefficient();
	baselineMicronsPerHour = p.getBaselineMigrationMicronsPerHour();
	concentrationVectorWeight = p.getMigrationConcentrationVectorWeight();
	persistenceVectorWeight = p.getMigrationPersistenceVectorWeight();
	randomVectorWeight = p.getMigrationRandomVectorWeight();
	
	
	maximumChemotacticMicronsPerHour = p.getMaximumChemotacticMicronsPerHour();
	maximumMigrationMicronsPerHour = p.getMaximumMigrationMicronsPerHour();
	collagenMigrationFactor = p.getCollagenMigrationFactor();
	
	
	
	baselineMigration = 
	    (timeStepLengthInSeconds / SECONDS_PER_HOUR) * baselineMicronsPerHour;
	
	maximumMigrationMagnitude = 
	    (timeStepLengthInSeconds / SECONDS_PER_HOUR) * maximumMigrationMicronsPerHour;
	
	haptotacticMigration = collagenMigrationFactor * e.getCollagenConcentration();
	
	maximumVarianceAngleDegrees = p.getMigrationVarianceAngleDegrees();
	maximumVarianceAngleRadians = maximumVarianceAngleDegrees * (Math.PI / 180.0);
	cosineMaximumVarianceAngle = Math.cos(maximumVarianceAngleRadians);

	migrationDisabled = p.disableMigration();

	maximumDeflectionRadians =
	    computeMaximumDeflectionRadians(persistenceVectorWeight,
					    concentrationVectorWeight,
					    randomVectorWeight,
					    maximumVarianceAngleRadians);


	bufferParameters(e.getOutputBuffer());
    }
    

    public MigrationRule(Parameters p, EnvironmentInterface e, double migrationMagnitudeFactor) {
	this(p, e);
	MigrationRule.migrationMagnitudeFactor = migrationMagnitudeFactor;
	e.getOutputBuffer().println("Migration magnitude scaled by " + migrationMagnitudeFactor);
    }
    
    private void bufferParameters(OutputBufferInterface buffer) {
	buffer.println(ruleName + " " + versionString);
	buffer.println("migrationMagnitudeVegfCoefficient=" + migrationMagnitudeVegfCoefficient);
	buffer.println("baselineMicronsPerHour=" + baselineMicronsPerHour);
	buffer.println("concentrationVectorWeight=" + concentrationVectorWeight);
	buffer.println("persistenceVectorWeight=" + persistenceVectorWeight);
	buffer.println("randomVectorWeight=" + randomVectorWeight);
	buffer.println("maximumChemotacticMicronsPerHour=" + maximumChemotacticMicronsPerHour);
	buffer.println("maximumMigrationMicronsPerHour=" + maximumMigrationMicronsPerHour);
	buffer.println("collagenMigrationFactor=" + collagenMigrationFactor);

	buffer.println("maximumVarianceAngleDegrees=" + maximumVarianceAngleDegrees);
	buffer.println("maximumVarianceAngleRadians=" + maximumVarianceAngleRadians);
	buffer.println("cosineMaximumVarianceAngle=" + cosineMaximumVarianceAngle);

    }


    private static double computeMigrationMagnitude(CellInterface c) {
	double vegfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.VEGF);
	double bdnfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.BDNF);
	
	
	double nominalMigrationMagnitude =
	    baselineMigration
	    * (1 + (migrationMagnitudeVegfCoefficient * vegfNgPerMl));
	

	log.println("Migration rule: cell " + c.getIdNumber() 
		    + " nominal migration distance is "
		    + nominalMigrationMagnitude + "    "
		    + migrationMagnitudeVegfCoefficient + "  " + vegfNgPerMl,
		    LogStreamInterface.BASIC_LOG_DETAIL);
	


	double migrationMagnitude = Math.min(nominalMigrationMagnitude,
					     maximumMigrationMagnitude);
	
	return migrationMagnitudeFactor * migrationMagnitude;
    }
    
    

    /*
     * Returns a vector relative to the point representing the front
     * of the tip cell to which the cell should migrate.  If the front
     * of the tip cell is located at p and the returned vector is v,
     * then the cell should migrate in the direction of p to p+v.
     */
    protected Point3D computeUnitMigrationDirection(CellInterface c) {
	
	Point3D gradient = c.getGradient(EnvironmentInterface.ConcentrationType.VEGF,
					 maximumVarianceAngleRadians);
	
	if (gradient == null) {
	//	    SimpleRuleSet.die("[computeUnitMigrationDirection] null gradient");
	    return null;
	}
	
	Point3D concentrationVector = gradient.normalize();
	Point3D persistenceVector = c.getFrontOrientation().normalize();
	Point3D randomVector = generateRandomUnitVector();
	
	Point3D weightedConcentrationVector = concentrationVector.mult(concentrationVectorWeight);
	Point3D weightedPersistenceVector = persistenceVector.mult(persistenceVectorWeight);
	Point3D weightedRandomVector = randomVector.mult(randomVectorWeight);
	
	
	Point3D netVector =
	    weightedConcentrationVector.plus(weightedPersistenceVector).plus(weightedRandomVector);
	
	Point3D referencePoint = c.getFrontOrientation();
	if (netVector.angleRadians(referencePoint) > maximumVarianceAngleRadians) {

	    Point3D b = c.getFrontOrientation();
	    Point3D q = weightedConcentrationVector.plus(weightedPersistenceVector);
	    Point3D r = weightedRandomVector;
	    
	    double xba = b.x;
	    double yba = b.y;
	    double zba = b.z;
	    
	    double xqa = q.x;
	    double yqa = q.y;
	    double zqa = q.z;
	    
	    double xrq = r.x - q.x;
	    double yrq = r.y - q.y;
	    double zrq = r.z - q.z;
	    
	    
	    double c1 = (xba * xqa) + (yba * yqa) + (zba * zqa);
	    double c2 = (xba * xrq) + (yba * yrq) + (zba * zrq);
	    double c3 = (xba * xba) + (yba * yba) + (zba * zba);
	    double c4 = (xrq * xrq) + (yrq * yrq) + (zrq * zrq);
	    double c5 = 2 * ((xqa * xrq) + (yqa * yrq) + (zqa * zrq));
	    double c6 = (xqa * xqa) + (yqa * yqa) + (zqa * zqa);
	    double cos = Math.cos(maximumVarianceAngleRadians);
	    double cosSqr = cos * cos;
	    double c7 = c3 * c4 * cosSqr;
	    double c8 = c3 * c5 * cosSqr;
	    double c9 = c3 * c6 * cosSqr;
	    
	    double quadA = (c2 * c2) - c7;
	    double quadB = (2 * c1 * c2) - c8;
	    double quadC = (c1 * c1) - c9;
	    
	    double discriminant = (quadB * quadB) - (4 * quadA * quadC);
	    
	    
	    if (discriminant < -0.001) {
		SimpleRuleSet.die("[MigrationRule.computeUnitMigrationDirection] Negative discriminant: "
				  + discriminant);
	    }
	    // Adjust discriminant for allowed small negative values
	    discriminant = Math.max(discriminant, 0);
	    
	    
	    // Use the smallest positive solution
	    double discriminantRoot = Math.sqrt(discriminant);
	    double t1 = (-quadB + discriminantRoot) / (2 * quadA);
	    double t2 = (-quadB - discriminantRoot) / (2 * quadA);
	    double t;

	    if (t1 < 0) {
		t = t2;
	    }
	    else {
		if (t2 < 0) {
		    t = t1;
		}
		else {
		    t = Math.min(t1, t2);
		}
	    }
	    
	    // Due to mathematical imprecision, allow t values down to -0.01
	    if (t < -0.01 || t >= 1) {
		SimpleRuleSet.die("[MigrationRule.computeUnitMigrationDistance] Invalid t value: "
				  + t);
	    }
	    // Correct allowed negative t values
	    t = Math.max(t, 0);
	    netVector = q.plus(r.minus(q).mult(t));
	    // Verify that the adjustment puts the new point on the
	    // cone.  Due to mathematical imprecision check that the
	    // actual angle and the cone angle are within .0001 of
	    // each other.
	    double angleRadians = netVector.angleRadians(referencePoint);
	    if (Math.abs(angleRadians - maximumVarianceAngleRadians)
		> .0001) {
		SimpleRuleSet.die("[MigrationRule.computeUnitMigrationDistance] Angle incorrect: "
				  + (angleRadians * 180.0 / Math.PI)
				  + " degrees (" + angleRadians + " radians); goal angle: "
				  + (maximumVarianceAngleRadians * 180.0 / Math.PI)
				  + " degrees (" + maximumVarianceAngleRadians + " radians)");
	    }
	    
	}
	log.println("Migration rule: cell " + c.getIdNumber() 
		    + " migration angle is "
		    + netVector.angleDegrees(referencePoint),
		    LogStreamInterface.BASIC_LOG_DETAIL);
	
	double migrationAngleRadians = netVector.angleRadians(referencePoint);
	double tolerance = 0.00001;
	//	if (Math.abs(migrationAngleDegrees) > tolerance) {
	//	    SimpleRuleSet.die("[MigrationRule.computeMigrationMagnitude] cell "
	//			      + c.getIdNumber() + " migration angle is "
	//			      + migrationAngleDegrees);
	//	}


	
	if (maximumDeflectionRadians - migrationAngleRadians < 0) {
	    SimpleRuleSet.die("[RuleSet.MigrationRule] cell " + c.getIdNumber()
			      + " migration angle "
			      + (migrationAngleRadians * (180.0 / Math.PI))
			      + " degrees is greater than maximum deflection angle "
			      + (maximumDeflectionRadians * (180.0 / Math.PI)));
	}
	    

	return netVector.normalize();
	
    }

    private static Point3D generateRandomUnitVector() {
	double x = -.5 + random.nextDouble();
	double y = -.5 + random.nextDouble();
	double z = -.5 + random.nextDouble();
	Point3D p = new Point3D(x, y, z);
	return p.normalize();
    }
    
    public RuleResult act(CellInterface c,
			  Storage s,
			  EnvironmentInterface e) {
	log.println("[MigrationRule.act] Begin cell "
		    + c.getIdNumber() + " migration activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);

	if (!c.isTipCell()) {
	    log.println("[MigrationRule.act] End cell "
			+ c.getIdNumber() + " migration activity",
			LogStreamInterface.BASIC_LOG_DETAIL);	
	    return null;
	}
	if (migrationDisabled) {
	    log.println("[MigrationRule.act] End cell "
			+ c.getIdNumber() + " migration activity",
			LogStreamInterface.BASIC_LOG_DETAIL);	
	    return null;
	}

	double migrationMagnitude = computeMigrationMagnitude(c);
	Point3D migrationDirection = computeUnitMigrationDirection(c);

	log.println("Migration rule: cell " + c.getIdNumber() 
		    + " migration distance is "
		    + migrationMagnitude,
		    LogStreamInterface.BASIC_LOG_DETAIL);
	


	double stalkElongationDistance = 0;

	if (migrationDirection != null) {
	    Point3D migrationVector = migrationDirection.mult(migrationMagnitude);

	    stalkElongationDistance = c.migrate(migrationVector);
	    log.println("Migration rule: cell " + c.getIdNumber() + " migrated to "
			+ c.getFrontLocation() + " stalk elongation distance: "
			+ stalkElongationDistance,
			LogStreamInterface.BASIC_LOG_DETAIL);
	    
	}
	log.println("[MigrationRule.act] End cell "
		    + c.getIdNumber() + " migration activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);

	return new RuleResult(stalkElongationDistance);
    }



    public static void main(String[] args) {
	testComputeMaximumDeflectionRadians();
	
    }
    
    
}