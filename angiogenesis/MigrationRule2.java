
/*
 */


package angiogenesis;

import shared.*;

public class MigrationRule2 extends MigrationRule {
    
    
    private static double migrationConstant1;
    private static double migrationConstant2;
    private static double minimumVegfConcentration;
    
    public MigrationRule2(Parameters2 p, EnvironmentInterface e) {
	ruleName = "Migration";
	ruleIdentifier = "M2";
	versionString = "0.2";
	random = e.getRandom();
	double timeStepLengthInSeconds = e.getTimeStepLengthInSeconds();
	
	migrationConstant1 = p.getMigrationConstant1();
	migrationConstant2 = p.getMigrationConstant2();
	minimumVegfConcentration = Math.exp((100.0 - migrationConstant2) / migrationConstant1);

	//	System.out.println("[MigrationRule2] migrationConstant1=" + migrationConstant1
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


    public MigrationRule2(Parameters2 p, EnvironmentInterface e, double migrationMagnitudeFactor) {
	this(p, e);
	MigrationRule2.migrationMagnitudeFactor = migrationMagnitudeFactor;
	e.getOutputBuffer().println("Migration magnitude scaled by " + migrationMagnitudeFactor);
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
	

	double nominalMigrationMagnitude = y * baselineMigration;

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
	log.println("[MigrationRule2.act] Begin cell "
		    + c.getIdNumber() + " migration activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);

	if (!c.isTipCell()) {
	    log.println("[MigrationRule2.act] End cell "
			+ c.getIdNumber() + " migration activity",
			LogStreamInterface.BASIC_LOG_DETAIL);
	    return null;
	}
	if (migrationDisabled) {
	    log.println("[MigrationRule2.act] End cell "
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
	log.println("[MigrationRule2.act] End cell "
		    + c.getIdNumber() + " migration activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);

	return new RuleResult(actualMigrationDistance);
    }

}