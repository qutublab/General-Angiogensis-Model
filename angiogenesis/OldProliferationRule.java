
/*
 * 3-28-2011
 * Proliferation depends on cell volume.
 *
 *
 */


/*
 * If the x-second proliferation rate due to VEGF is P, what is the
 * equivalent rate over a span of y seconds?
 *
 * According to the x-second rate, after s seconds, there is
 * proliferation of P^(s/x) per cell.  According to a y-second
 * proliferation rate of P', after s seconds there is a proliferation
 * of (P')^(s/y) per cell.
 *
 * Thus:
 *  (P')^(s/y) = P^(s/x)
 *  P' = (P^(s/x))^(y/s)
 *  P' = P^(y/x)
 */

package angiogenesis;



import shared.*;

public class ProliferationRule extends Rule {

    private static final int SECONDS_PER_HOUR = 60 * 60;

    private static double timeStepLengthInSeconds;

    private static double baselineGrowthRatePerTimeStep;

    private static double vegfCoefficient1;
    private static double bdnfThresholdNgMl;
    private static double bdnfCoefficient2;
    private static double bdnfCoefficient3;
    private static double bdnfConstant4;

    
    private static boolean rearCellProliferationDisabled;
    private static boolean allCellProliferationDisabled;

    private static double specialBranchProliferationFactor;

    public ProliferationRule(Parameters p, EnvironmentInterface e) {
	ruleName = "Proliferation";
	ruleIdentifier = "P";
	versionString = "0.1";

	timeStepLengthInSeconds = e.getTimeStepLengthInSeconds();
	double hoursPerTimeStep = timeStepLengthInSeconds / SECONDS_PER_HOUR;


	/*
	 * Since the growth rate, b, is per hour, it needs to be
	 * adjusted for the time-step length of the
	 * simulation. Baseline growth rate, b, is per hour.  The
	 * adjusted rate, r, for a time step of length h hours is
	 * determined by the following exponential equation (to
	 * express compounded growth) which states that the two rates
	 * give the same results after z hours:
	 *
	 *                       r^(z/h) = b^z
	 *                             r = b^h
	 */
	baselineGrowthRatePerTimeStep =
	    Math.pow(p.getProliferationBaselineGrowthRatePerHour(),
		     hoursPerTimeStep);
	if (p.adjustProliferationGrowthRate()) {
	    baselineGrowthRatePerTimeStep *=
		(1 / p.getProliferationActiveFraction()); 
	}

	vegfCoefficient1 = p.getProliferationVegfCoefficient1();
	bdnfThresholdNgMl = p.getProliferationBdnfThresholdNgMl();
	bdnfCoefficient2 = p.getProliferationBdnfCoefficient2();
	bdnfCoefficient3 = p.getProliferationBdnfCoefficient3();
	bdnfConstant4 = p.getProliferationBdnfConstant4();

	rearCellProliferationDisabled = p.disableRearCellProliferation();
	allCellProliferationDisabled = p.disableAllCellProliferation();

	specialBranchProliferationFactor =
	    p.getSpecialBranchProliferationFactor();

    }

    public void adjustBaselineGrowthRatePerTimeStep(double adjustmentFactor) {
	baselineGrowthRatePerTimeStep *= adjustmentFactor;
    }

    public RuleResult act(CellInterface c,
			    Storage s,
			    EnvironmentInterface e) {
	
	if (allCellProliferationDisabled) {
	    return null;
	}
	if (rearCellProliferationDisabled
	    && c.getCellPosition() == CellInterface.CellPosition.REAR) {
	    return null;
	}

	// if there is a branch ahead of the cell do not proliferate
	// because we don't know how to move a branch forward.
	if (c.hasBranchAhead()) {
	    return null;
	}
	double vegfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.VEGF);
	double bdnfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.BDNF);

	double growthIncreaseDueToVegf = vegfCoefficient1 * vegfNgPerMl;

	//	System.out.println("*** [ProliferationRule.act] vegfCoefficient1="
	//			   + vegfCoefficient1 + "  vegfNgPerMl="
	//			   + vegfNgPerMl + "  growthIncreaseDueToVegf="
	//			   + growthIncreaseDueToVegf);

	double growthIncreaseDueToBdnf;
	double bdnfMultiplier;


	//	if (bdnfNgPerMl <= bdnfThresholdNgMl) {
	//	    growthIncreaseDueToBdnf = bdnfCoefficient2 * bdnfNgPerMl;
	//	}
	//	else {
	//	    growthIncreaseDueToBdnf =
	//		(bdnfCoefficient3 * bdnfNgPerMl) + bdnfConstant4;
	//	}

	growthIncreaseDueToBdnf = 0;	
	if (bdnfNgPerMl > 0) {
	    growthIncreaseDueToBdnf = 1;
	}

	
	double increase = 
	    baselineGrowthRatePerTimeStep * c.getVolumeCubicMicrons()
	    * (1 + growthIncreaseDueToVegf + growthIncreaseDueToBdnf); 

	if (s.isBranch && e.stepsCompleted() <= s.lastSpecialBranchStep) {
	    increase = increase * specialBranchProliferationFactor;
	    log.println("Proliferation rule: cell " + c.getIdNumber()
			+ " volume "
			+ c.getVolumeCubicMicrons() 
			+ " SPECIAL BRANCH volume increase " + increase
			+ " (" + (increase / c.getVolumeCubicMicrons()) + ")"
			+ " (special factor: "
			+ specialBranchProliferationFactor + ")",
			LogStreamInterface.BASIC_LOG_DETAIL);
	}
	else {
	    log.println("*Proliferation rule: cell " + c.getIdNumber()
			+ " volume "
			+ c.getVolumeCubicMicrons() 
			+ " volume increase " + increase
			+ " (" + (increase / c.getVolumeCubicMicrons()) + ")",
			LogStreamInterface.BASIC_LOG_DETAIL);
	}
	//	log.println("*Proliferation rule: cell " + c.getIdNumber()
	//		    + " baselineGrowthRatePerTimeStep="
	//		    + baselineGrowthRatePerTimeStep
	//		    + " c.getVolumeCubicMicrons()=" + c.getVolumeCubicMicrons()
	//		    + " growthIncreaseDueToVegf=" + growthIncreaseDueToVegf
	//		    + " growthIncreaseDueToBdnf=" + growthIncreaseDueToBdnf,
	//		    LogStreamInterface.BASIC_LOG_DETAIL);
	CellInterface newCell = c.proliferate(increase, e);
	if (newCell != null) {
	    s.divided = true;
	    Storage newCellStore = (Storage) newCell.getLocalStorage();
	    //	    newCellStore.divided = true;
	    s.copyTo(newCellStore);
	}
	return new RuleResult(newCell);
    }
}