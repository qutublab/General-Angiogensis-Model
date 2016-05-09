
/*
 */


package angiogenesis;

import shared.*;
import java.util.*;

public class MigrationRule2B extends MigrationRule {
    
    
    private static double migrationConstant1;
    private static double migrationConstant2;
    private static double minimumVegfConcentration;
    
    public MigrationRule2B(Parameters2 p, EnvironmentInterface e) {
	ruleName = "Migration";
	ruleIdentifier = "M2b";
	versionString = "0.2";
	random = e.getRandom();
	double timeStepLengthInSeconds = e.getTimeStepLengthInSeconds();
	
	migrationConstant1 = p.getMigrationConstant1();
	migrationConstant2 = p.getMigrationConstant2();
	minimumVegfConcentration = Math.exp((100.0 - migrationConstant2) / migrationConstant1);

	//	System.out.println("[MigrationRule2B] migrationConstant1=" + migrationConstant1
	//			   + "  migrationConstant2=" + migrationConstant2);
	

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


    }


    public MigrationRule2B(Parameters2 p, EnvironmentInterface e, double migrationMagnitudeFactor) {
	this(p, e);
	MigrationRule2B.migrationMagnitudeFactor = migrationMagnitudeFactor;
	e.getOutputBuffer().println("Migration magnitude scaled by " + migrationMagnitudeFactor);
    }
    

    public static class TestCell implements CellInterface {
	//	MigrationRule2.baselineMigration = 1;
	public double vegfNgMl = 0;
	public double bdnfNgMl = 0;
	public int getIdNumber() {return 0;}
	public EnvironmentInterface.CellState getCellState() {return null;}
	public boolean isTipCell() {return true;}
	public boolean isStalkCell() {return !isTipCell();}
	public boolean isInhibited() {return false;}
	public int activate() {return 0;}
	public double migrate(Point3D p) {return 0.0;}
	public LinkedList<CellInterface> proliferate(double v, EnvironmentInterface e) {return null;}
	public void remove(EnvironmentInterface e) {return;}
	public double getAvgNgPerMl(EnvironmentInterface.ConcentrationType ct) {
	    double a = 0;
	    switch (ct) {
	    case VEGF: a = vegfNgMl; break;
	    case BDNF: a = bdnfNgMl; break;
	    }
	    return a;
	}
	public Point3D getGradient(EnvironmentInterface.ConcentrationType ct,
				   double angleRadians) {
	    return null;
	}
	public double getVolumeCubicMicrons() {return 0.0;}
	public double getLength() {return 0.0;}
	public LinkedList<Point3D> getNodeLocations() {return null;}
	public boolean hasBranchAhead() {return false;}
	public boolean canBranch() {return !hasBranchAhead();}
	public CellInterface branch(Point3D p, double d) {return null;}
	public Point3D getFrontOrientation() {return null;}
	public double removableVolumeCubicMicrons() {return 0.0;}
	public CellPosition getCellPosition() {return null;}
	public Object getLocalStorage() {return null;}
	public CellInterface getSuccessor() {return null;}
	public CellInterface getPredecessor() {return null;}
	public Point3D getFrontLocation() {return null;}
	public void resize() {return;}
	public LinkedList<Parameter> getParameters() {return null;}
	public String setParameters(LinkedList<Parameter> p) {return null;}
	public boolean hasBranchStalkCell() {return false;}
    }

    public static class FakeLogStream implements LogStreamInterface {
	public void print(String s, int i) {}
	public void println(String s, int i) {}
    }

    private static void testComputeMigrationMagnitude() {
	minimumVegfConcentration = 10;
	baselineMigration = 1;
	migrationMagnitudeFactor = 1;
	maximumMigrationMagnitude = 11;
	log = new FakeLogStream();
	boolean testFailed = false;
	TestCell c = new TestCell();
	c.bdnfNgMl = 10;
	double m;
	double expectedMagnitude;
	
	m = computeMigrationMagnitude(c);
	expectedMagnitude = 1 * baselineMigration;
	if (m != expectedMagnitude) {
	    testFailed = true;
	    System.out.println("[MigrationRuleB.testComputeMigrationMagnitude] For BDNF "
		+ c.bdnfNgMl + " ng/ml, expected " + expectedMagnitude
		+ ", but computeMigrationMagnitude returned " + m);
	}
	
	c.bdnfNgMl = 50;
	m = computeMigrationMagnitude(c);
	expectedMagnitude = 1.563492 * baselineMigration;
	if (m != expectedMagnitude) {
	    testFailed = true;
	    System.out.println ("[MigrationRuleB.testComputeMigrationMagnitude] For BDNF "
		+ c.bdnfNgMl + " ng/ml, expected " + expectedMagnitude
		+ ", but computeMigrationMagnitude returned " + m);
	}
	if (!testFailed) {
	    System.out.println("[MigrationRuleB.testComputeMigrationMagnitude] All tests passed!");
	}
    }



    private static double computeMigrationMagnitude(CellInterface c) {
	double vegfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.VEGF);
	double bdnfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.BDNF);
	
	double y;
	if (vegfNgPerMl < minimumVegfConcentration) {
	    y = 1.0;
	}
	else {
	    y = ((migrationConstant1 * Math.log(vegfNgPerMl)) + migrationConstant2) / 100.0;
	}


	double bdnfFactor;
	// Subtract 1 from calculation to get amount of increase above baseline
	if (bdnfNgPerMl < 25) {
	    bdnfFactor = 1 - 1;
	}
	else {
	    bdnfFactor = 1.563492 - 1;
	}

	double nominalMigrationMagnitude =
	    (y + bdnfFactor) * baselineMigration;


	double migrationMagnitude = Math.min(nominalMigrationMagnitude,
					     maximumMigrationMagnitude);

	return migrationMagnitudeFactor * migrationMagnitude;
    }



    // Although this code is textually the same as the parent class,
    // it invokes the class's computeMigrationMagnitude method instead
    // of the parent class's computeMigrationMagnitude method.
    public RuleResult act(CellInterface c,
			  Storage s,
			  EnvironmentInterface e) {
	log.println("[MigrationRule2B.act] Begin cell "
		    + c.getIdNumber() + " migration activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);

	if (!c.isTipCell()) {
	    log.println("[MigrationRule2B.act] End cell "
			+ c.getIdNumber() + " migration activity",
			LogStreamInterface.BASIC_LOG_DETAIL);
	    return null;
	}
	if (migrationDisabled) {
	    log.println("[MigrationRule2B.act] End cell "
			+ c.getIdNumber() + " migration activity",
			LogStreamInterface.BASIC_LOG_DETAIL);
	    return null;
	}

	double migrationMagnitude = computeMigrationMagnitude(c);
	Point3D migrationDirection = computeUnitMigrationDirection(c);
	
	double actualMigrationDistance = 0;
	if (migrationDirection != null) {
	    Point3D migrationVector = migrationDirection.mult(migrationMagnitude);

	    actualMigrationDistance = c.migrate(migrationVector);
	    log.println("Migration2 rule: cell " + c.getIdNumber() + " migrated to "
			+ c.getFrontLocation() + " distance: " + actualMigrationDistance,
			LogStreamInterface.BASIC_LOG_DETAIL);
	    
	}
	log.println("[MigrationRule2B.act] End cell "
		    + c.getIdNumber() + " migration activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);

	return new RuleResult(actualMigrationDistance);
    }

    public static void main(String[] args) {
	testComputeMigrationMagnitude();
    }



}