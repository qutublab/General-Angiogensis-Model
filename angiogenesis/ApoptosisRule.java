

/*
 * Tyler's research of the experimental literature shows that for the
 * concentration levels that we are dealing with, VEGF, ANG1, and ANG2
 * each prolong the life of cells, that is, their presence reduces the
 * rate of apoptosis.
 *
 * Each study for VEGF, ANG1, and ANG2, gives apoptosis rates for
 * no-serum cultures under a variety of concentration levels including
 * 0%.  Since lab cultures will contain serum, the lack of serum in
 * the studies must be addressed in creating an apoptosis rule.
 *
 * The following assumptions are made.
 *
 * Assumption 1: The effects of VEGF, ANG1, and ANG2 on apoptosis are
 * independent of each other.
 *
 * Assumption 2: The ratio of the effect of non-zero concentration
 * levels to the effect of a zero concentration level is the same with
 * serum as without serum.
 *
 * The apoptosis rule works by first deciding if a cell should die due
 * to the normal apoptosis rate.  If it is the case that a cell should
 * die, then it is determined if the local VEGF, ANG1, or ANG2
 * concentration levels will prevent the death.  By assumption 1,
 * these checks are done independently such that if any of them saves
 * the cell, then the cell survives.  Each check follows the same
 * general strategy described as follows.  Suppose the cell is in an
 * environment of with a concentration level of n.  The apoptosis rate
 * a_0 for no-serum cultures at 0% concentration level is compared to
 * the apoptosis rate a_n for no-serum cultures at a concentration
 * level of n.  Note that a_0 > a_n and at a concentration level of n,
 * a_0-a_n cells survive that would have otherwise died at a
 * concentartion level of 0.  Hence for no-serum, (a_0-a_n)/a_0 is the
 * ratio of the number of surviving cells at concentartion level n
 * that would otherwise have died to the number of cells that die at
 * concentartion level 0. By assumption 2, (a_0-a_n)/a_0 is also the
 * percentage of cells that would normally die that survived due to
 * the concentration level of n in the case of serum cultures.  Since
 * a_0 > a_n, this value is between 0 and 1 and can be interepretted
 * as a probability of the survival of the cell that would die at the
 * 0% level.
 */




/*
 * If the x-second death rate is D, what is the equivalent death rate
 * for y seconds?
 *
 * According to x-second death rate, after s seconds, there is a
 * survival rate of (1-D)^(s/x) (and hence a death rate of 1-(1-D)^(s/x)
 *
 * according to a y-second death rate D', after s seconds, there is a
 * survival rate of (1-D')^(s/y).
 *
 * Thus:
 *             (1-D)^(s/x) = (1-D')^(s/y)
 *     ((1-D)^(s/x))^(y/s) = ((1-D')^(s/y))^(y/s)
 *             (1-D)^(y/x) = 1-D'
 *                      D' = 1 - (1-D)^(y/x)
 */


package angiogenesis;

import shared.*;
//import interfaces.*;

//import java.util.*;

public class ApoptosisRule {

    // local constants to be used as shorthand
    private static final EnvironmentInterface.ConcentrationType VEGF_TYPE
	= EnvironmentInterface.ConcentrationType.VEGF;
    private static final EnvironmentInterface.ConcentrationType ANG1_TYPE
	= EnvironmentInterface.ConcentrationType.ANG1;
    private static final EnvironmentInterface.ConcentrationType ANG2_TYPE
	= EnvironmentInterface.ConcentrationType.ANG2;

    // Period of time over which the rates below were found
    private static final double STANDARD_TIME_IN_SECONDS = 24 * 60 * 60;


    // The following values are referrred to as _rates_.  They are
    // then adjusted to arrive at _probabilities_ which are used in the
    // rest of this module.

    // Apoptosis rate in serum derived experimentally
    private static final double BASE_APOPTOSIS_RATE = .055;
    // Apoptosis rate w/o serum or other concentrations derived experimentally
    // Shouldn't these rates be the same?
    private static final double ZERO_VEGF_APOPTOSIS_RATE = .015;
    private static final double ZERO_ANG1_APOPTOSIS_RATE = .016;
    private static final double ZERO_ANG2_APOPTOSIS_RATE = .017;


    // _Probabilities_
    private static final double[] ZERO_LEVEL_APOPTOSIS_PROBABILITY
	= new double[EnvironmentInterface.ConcentrationType.values().length];

    //    private static final double BASE_APOPTOSIS_PROBABILITY;
    private static double baseApoptosisProbability;

    private static double timeStepLengthInSeconds;
    //    private static double baseApoptosisProbability;
    //    private static double zeroVegfApoptosisProbability;
    //    private static double zeroAng1ApoptosisProbability;
    //    private static double zeroAng2ApoptosisProbability;


    private static RandomInterface random;

    private static double log(double n, double base) {
	double lg = Math.log(n);
	double baseLg = Math.log(base);
	return lg / baseLg;
    }

    private static double log10(double n) {
	return log(n, 10);
    }

    public static void initialize(EnvironmentInterface e) {
	timeStepLengthInSeconds = e.getTimeStepLengthInSeconds();
	
	// now that timeStepLengthInSeconds has been set, the adjust
	// method can be used.
	baseApoptosisProbability =
	    adjust(BASE_APOPTOSIS_RATE, STANDARD_TIME_IN_SECONDS);
	
	ZERO_LEVEL_APOPTOSIS_PROBABILITY[VEGF_TYPE.ordinal()]
	    = adjust(ZERO_VEGF_APOPTOSIS_RATE, STANDARD_TIME_IN_SECONDS);
	ZERO_LEVEL_APOPTOSIS_PROBABILITY[ANG1_TYPE.ordinal()]
	    = adjust(ZERO_ANG1_APOPTOSIS_RATE, STANDARD_TIME_IN_SECONDS);
	ZERO_LEVEL_APOPTOSIS_PROBABILITY[ANG2_TYPE.ordinal()]
	    = adjust(ZERO_ANG2_APOPTOSIS_RATE, STANDARD_TIME_IN_SECONDS);
	
	
	
	random = e.getRandom();
	
	if (baseApoptosisProbability < 0 || baseApoptosisProbability > 1) {
	    SimpleRuleSet.die("[ApoptosisRule.initialze] Base apoptosis probability is "
			      + baseApoptosisProbability);
	}
	
	
	for (EnvironmentInterface.ConcentrationType concType :
		 EnvironmentInterface.ConcentrationType.values()) {
	    double prob = ZERO_LEVEL_APOPTOSIS_PROBABILITY[concType.ordinal()];
	    if (prob < 0 || prob > 1) {
		SimpleRuleSet.die("[ApoptosisRule.initialze] 0% " + concType
				  + " apoptosis probability is " + prob);
	    }
	}
    }


    // Recalculates apoptosis rate for standard time to apoptosis rate
    // for the current time-step length
    private static double adjust(double prob, double standardTimeInSeconds) {
	return 1 - Math.pow(1 - prob,
			    timeStepLengthInSeconds / standardTimeInSeconds);
    }

    

    /*
     * Returns the probability of a cell dying due to its local VEGF
     * concentration.  Note that the log of the concentration level is
     * used.  This means that if the concentration level is 0, a
     * different calculation must be used.  Hence the use of
     * ZERO_LEVEL_APOPTOSIS_PROBABILITY.
     */
    private static double vegfApoptosisProb(double vegfNgPerMl) {
	double returnProb =
	    ZERO_LEVEL_APOPTOSIS_PROBABILITY[VEGF_TYPE.ordinal()];
	if (vegfNgPerMl != 0) {
	    double logOfVegf = log10(vegfNgPerMl);
	    double prob =
		(0.047 * Math.pow(logOfVegf, 2))
		+ (0.1664 * logOfVegf) + 0.257;
	    if (prob < 0 || prob > 1) {
		SimpleRuleSet.die("[ApoptosisRule.vegfApoptosisProb] "
				  + prob + "  " + " unadjusted death probability!");
	    }
	    returnProb = adjust(prob, STANDARD_TIME_IN_SECONDS);
	    if (returnProb < 0 || returnProb > 1) {
		SimpleRuleSet.die("[ApoptosisRule.vegfApoptosisProb] " + returnProb
				  + "  " + " adjusted death probability!");
	    }
	}
	return returnProb;
    }
    

    private static double ang1ApoptosisProb(double ang1NgPerMl) {
	//	System.out.println(ang1NgPerMl);
	double returnProb = 
	    ZERO_LEVEL_APOPTOSIS_PROBABILITY[ANG1_TYPE.ordinal()];
	if (ang1NgPerMl != 0) {
	    double logOfAng1 = log10(ang1NgPerMl);
	    //	    System.out.println(logOfAng1);
	    double prob = (-.11426 * logOfAng1) + .46152;
	    if (prob < 0 || prob > 1) {
		SimpleRuleSet.die("[ApoptosisRule.ang1ApoptosisProb] "
				  + prob + "  " + " unadjusted death probability!");
	    }
	    returnProb = adjust(prob, STANDARD_TIME_IN_SECONDS);
	    if (returnProb < 0 || returnProb > 1) {
		SimpleRuleSet.die("[ApoptosisRule.ang1ApoptosisProb] " + returnProb
				  + "  " + " adjusted death probability!");
	    }
	}
	return returnProb;
    }
	
    private static double ang2ApoptosisProb(double ang2NgPerMl) {
	double returnProb = 
	    ZERO_LEVEL_APOPTOSIS_PROBABILITY[ANG2_TYPE.ordinal()];
	if (ang2NgPerMl != 0) {
	    double logOfAng2 = log10(ang2NgPerMl);
	    double prob = (-.085727 * logOfAng2) + .45662;
	    if (prob < 0 || prob > 1) {
		SimpleRuleSet.die("[ApoptosisRule.ang2ApoptosisProb] "
				  + prob + "  " + " unadjusted death probability!");
	    }
	    returnProb = adjust(prob, STANDARD_TIME_IN_SECONDS);
	    if (returnProb < 0 || returnProb > 1) {
		SimpleRuleSet.die("[ApoptosisRule.ang2ApoptosisProb] " + returnProb
				  + "  " + " adjusted death probability!");
	    }
	}
	return returnProb;
    }
    

    /*
     * Returns the probability of a dying cell being saved due to a
     * local concentration.
     */
    private static double saveProbability(EnvironmentInterface.ConcentrationType concType,
					  double ngPerMl) {
	double apoptosisProbability = -1;
	switch (concType) {
	case VEGF:
	    apoptosisProbability = vegfApoptosisProb(ngPerMl);
	    break;
	case ANG1:
	    apoptosisProbability = ang1ApoptosisProb(ngPerMl);
	    break;
	case ANG2:
	    apoptosisProbability = ang2ApoptosisProb(ngPerMl);
	    break;
	default:
	    SimpleRuleSet.die("[ApoptosisRule.saveprobability] Unsupported factor: "
			      + concType);
	}
	double baseApoptosisProbability =
	    ZERO_LEVEL_APOPTOSIS_PROBABILITY[concType.ordinal()];
	double saveProbability =
	    (baseApoptosisProbability - apoptosisProbability)
	    / baseApoptosisProbability;
	return saveProbability;
    }

    /*
    private static double vegfSaveProbability(double vegfNgPerMl) {
	double deathProbability = vegfApoptosisProb(vegfNgPerMl);
	double saveProbability =
	    (zeroVegfApoptosisProbability - deathProbability)
	    / zeroVegfApoptosisProbability;
	return saveProbability;
    }

    private static double ang1SaveProbability(double ang1NgPerMl) {
	double deathProbability = ang1ApoptosisProb(ang1NgPerMl);
	double saveProbability =
	    (zeroAng1ApoptosisProbability - deathProbability)
	    / zeroAng1ApoptosisProbability;
	return saveProbability;
    }

    private static double ang2SaveProbability(double ang2NgPerMl) {
	double deathProbability = ang2ApoptosisProb(ang2NgPerMl);
	double saveProbability =
	    (zeroAng2ApoptosisProbability - deathProbability)
	    / zeroAng2ApoptosisProbability;
	return saveProbability;
    }
    */


    /*
     * Returns true if a dying cell is saved due to its local
     * substance level.
     */
    private static boolean savedBy(EnvironmentInterface.ConcentrationType concType,
				   CellInterface cell,
				   EnvironmentInterface e) {
	double ngPerMl = cell.getAvgNgPerMl(concType);
	double saveProbability = saveProbability(concType, ngPerMl);
	double rand = random.nextDouble();
	boolean saved = false;
	if (rand < saveProbability) {
	    saved = true;
	}
	return saved;
    }

    static void act(CellInterface cell,
		    Storage s,
		    EnvironmentInterface e) {
	System.out.println("[ApoptosisRule.act] invoked");
	// compute normal death
	// 0 <= r < 1
	double r = random.nextDouble();
	//	e.getLog().println("r=" + r + "   " + baseApoptosisProbability);
	if (r < baseApoptosisProbability) {
	    //	    e.getLog().println("Checking for a save");
	    // cell will die unless saved by VEGF, ANG1, or ANG2
	    if (!savedBy(VEGF_TYPE, cell, e) 
		&& !savedBy(ANG1_TYPE, cell, e)
		&& !savedBy(ANG2_TYPE, cell, e)) {
		cell.remove(e);
	    }
	}
	
    }
    
    public static void main(String[] args) {
	System.out.println(log10(1));
	System.out.println(log10(100));
	System.out.println(log10(1000000));


    }
    
}
