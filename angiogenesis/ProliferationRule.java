
/*
 * 3-28-2011
 * Proliferation depends on cell volume.
 *
 * 6-8-2011
 * Newly branched cells proliferate at a higher rate
 *
 * 7-3-2011
 * Cell proliferate method returns a linked list of newly created cells.
 *
 * 7-15-2011
 * Fixed proliferation rate computation for different length time steps.
 */


/*
 * If the x-second proliferation rate due to VEGF is P, what is the
 * equivalent rate over a span of y seconds?
 *
 * According to the x-second rate, after s seconds, from an intial
 * volume of V, there is a new volume of V(1+P)^(s/x).  According
 * to a y-second proliferation rate of P', after s seconds there is a
 * proliferation of V(1+P')^(s/y).
 *
 * Thus:
 *  (1+P')^(s/y) = (1+P)^(s/x)
 *  1+P' = (1+P)^(s/x))^(y/s)
 *  1+P' = (1+P)^(y/x)
 *  P' = (1+P)^(y/x) - 1
 */

package angiogenesis;

import java.util.*;

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
	 *                   (1+r)^(z/h) = (1+b)^(z/1)
	 *                           1+r = (1+b)^h
	 *                             r = (1+b)^h - 1
	 */
	baselineGrowthRatePerTimeStep =
	    Math.pow(1 + p.getProliferationBaselineGrowthRatePerHour(),
		     hoursPerTimeStep)
	    - 1;
	//	System.out.println("[ProliferationRule] growth rate per hour: "
	//			   + p.getProliferationBaselineGrowthRatePerHour()
	//			   + " growth per rate step: "
	//			   + baselineGrowthRatePerTimeStep
	//			   + " hours per time step: " + hoursPerTimeStep);
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
	log.println("[ProliferationRule.act] Begin cell "
		    + c.getIdNumber() + " proliferation activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);
	
	if (StateMachineRuleSet2.debugFlag) {
	    System.out.println("[ProliferationRule.act] Begin");
	}

	if (allCellProliferationDisabled) {
	log.println("[ProliferationRule.act] End cell "
		    + c.getIdNumber() + " proliferation activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);
	    return null;
	}
	if (rearCellProliferationDisabled
	    && c.getCellPosition() == CellInterface.CellPosition.REAR) {
	    log.println("[ProliferationRule.act] End cell "
			+ c.getIdNumber() + " proliferation activity",
			LogStreamInterface.BASIC_LOG_DETAIL);
	    return null;
	}

	// if there is a branch ahead of the cell do not proliferate
	// because we don't know how to move a branch forward.
	//	if (c.hasBranchAhead()) {
	//	    return null;
	//	}
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
	    log.println("Proliferation rule: new branch cell "
			+ c.getIdNumber()
			+ " has proliferation increased by a factor of "
			+ specialBranchProliferationFactor,
			LogStreamInterface.BASIC_LOG_DETAIL);
	}

	log.println("Proliferation rule: cell " + c.getIdNumber() + " volume "
		    + c.getVolumeCubicMicrons() 
		    + " volume increase " + increase
		    + " (" + (increase / c.getVolumeCubicMicrons()) + ")",
		    LogStreamInterface.BASIC_LOG_DETAIL);

	//	log.println("*Proliferation rule: cell " + c.getIdNumber()
	//		    + " baselineGrowthRatePerTimeStep="
	//		    + baselineGrowthRatePerTimeStep
	//		    + " c.getVolumeCubicMicrons()=" + c.getVolumeCubicMicrons()
	//		    + " growthIncreaseDueToVegf=" + growthIncreaseDueToVegf
	//		    + " growthIncreaseDueToBdnf=" + growthIncreaseDueToBdnf,
	//		    LogStreamInterface.BASIC_LOG_DETAIL);
	if (StateMachineRuleSet2.debugFlag) {
	    System.out.println("[ProliferationRule.act] Cell "
			       + c.getIdNumber() + " increase=" + increase);
	}	
	LinkedList<CellInterface> newCellList = c.proliferate(increase, e);
	for (Iterator<CellInterface> i = newCellList.iterator(); i.hasNext();) {
	    CellInterface newCell = i.next();
	    // Eventhough s.divided is redundantly set to true in this
	    // loop, being in the body of the loop guarantees that
	    // s.divided is set to true only if one or more new cells
	    // were created by the cell proliferate method.
	    s.divided = true;
	    Storage newCellStore = (Storage) newCell.getLocalStorage();
	    s.copyTo(newCellStore);
	}
	//	if (newCell != null) {
	//	    s.divided = true;
	//	    Storage newCellStore = (Storage) newCell.getLocalStorage();
	//	    //	    newCellStore.divided = true;
	//	    s.copyTo(newCellStore);
	//	}
	if (StateMachineRuleSet2.debugFlag) {
	    System.out.println("[ProliferationRule.act] End");
	}
	log.println("[ProliferationRule.act] End cell "
		    + c.getIdNumber() + " proliferation activity",
		    LogStreamInterface.BASIC_LOG_DETAIL);
	return new RuleResult(newCellList);
    }


    public static void main(String[] args) {
	
    }

}