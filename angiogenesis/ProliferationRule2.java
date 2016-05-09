
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

public class ProliferationRule2 extends Rule {

    private static final int SECONDS_PER_HOUR = 60 * 60;

    private static double timeStepLengthInSeconds;

    private static double baselineGrowthRatePerTimeStep;

    private static double vegfCoefficient1 = 1.4568;
    private static double vegfConstant2 = 130.9;
    private static double bdnfCoefficient3 = .0024;
    private static double bdnfConstant4 = 1.0;
    private static double bdnfCoefficient5 = -.0004667;
    private static double bdnfConstant6 = 1.28667;
    
    private static boolean rearCellProliferationDisabled;
    private static boolean allCellProliferationDisabled;

    public ProliferationRule2(Parameters p, EnvironmentInterface e) {
	ruleName = "Proliferation2";
	ruleIdentifier = "P2";
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
	vegfConstant2 = p.getProliferationVegfConstant2();
	bdnfCoefficient3 = p.getProliferationBdnfCoefficient3();
	bdnfConstant4 = p.getProliferationBdnfConstant4();
	bdnfCoefficient5 = p.getProliferationBdnfCoefficient5();
	bdnfConstant6 = p.getProliferationBdnfConstant6();

	rearCellProliferationDisabled = p.disableRearCellProliferation();
	allCellProliferationDisabled = p.disableAllCellProliferation();

    }

    public void adjustBaselineGrowthRatePerTimeStep(double adjustmentFactor) {
	baselineGrowthRatePerTimeStep *= adjustmentFactor;
    }

    public double act(CellInterface c,
		      Storage s,
		      EnvironmentInterface e) {
	if (allCellProliferationDisabled) {
	    return 0;
	}
	if (rearCellProliferationDisabled
	    && c.getCellPosition() == CellInterface.CellPosition.REAR) {
	    return 0;
	}

	// if there is a branch ahead of the cell do not proliferate
	// because we don't know how to move a branch forward.
	if (c.hasBranchAhead()) {
	    return 0;
	}
	double growthIncreaseDueToVegf = 0;
	double vegfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.VEGF);
	double bdnfNgPerMl = c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.BDNF);
	if (vegfNgPerMl > 0) {
	    growthIncreaseDueToVegf =
		((vegfCoefficient1 * Math.log(vegfNgPerMl)) + vegfConstant2) / 100.0;
	}

	double bdnfMultiplier;
	if (bdnfNgPerMl <= 100) {
	    /*
	     * In this [bdnf] region, the multipler is characterized
	     * by the line containing points (0,1) (at 0 [bdnf] the
	     * multiplier is 1) and (100, 1.24) (at 100 [bdnf] the
	     * multiplier is 1.24).  The equation for the line passing
	     * through thses points is:
	     *
	     *  y = mx + b     slope:y-intercept form
	     *
	     *  m = (1.24 - 1.0) / (100 - 0) = .24 / 100 = .0024
	     *  b = 1
	     *
	     *  multiplier = .0024[bdnf] + 1
	     */
	    bdnfMultiplier = (bdnfCoefficient3 * bdnfNgPerMl) + bdnfConstant4;
	}
	else {
	    /*
	     * In this region, the multiplier is characterized by a
	     * line containg the points (100, 1.24) and (400, 1.1)
	     *
	     * m = (1.1 - 1.24) / (400 - 100) = -.14 / 300 = -.0004667
	     * 
	     * Thus in general:
	     *
	     *  (y - 1.24) / (x - 100) = -.0004667
	     *  y - 1.24 = -.0004667(x - 100)
	     *  y - 1.24 = -.0004667x + .04667
	     *  y = -.0004667x + 1.28667
	     *
	     * multiplier = -.0004667[bdnf] + 1.28667
	     */
	    bdnfMultiplier = Math.max(1, (bdnfCoefficient5 * bdnfNgPerMl) + bdnfConstant6);
	}
	double growthIncreaseDueToBdnf = bdnfMultiplier - 1.0;
	

	double increase = 
	    baselineGrowthRatePerTimeStep * c.getVolumeCubicMicrons()
	    * (1 + growthIncreaseDueToVegf + growthIncreaseDueToBdnf); 


	log.println("Proliferation rule: cell " + c.getIdNumber() + " vegfNgPerMl="
		    + vegfNgPerMl + " bdnfNgPerMl=" + bdnfNgPerMl,
		    LogStreamInterface.EXTRA_LOG_DETAIL);

	log.println("Proliferation rule: cell " + c.getIdNumber() + " volume "
		    + c.getVolumeCubicMicrons() 
		    + " volume increase " + increase
		    + " (" + (increase / c.getVolumeCubicMicrons()) + ")",
		    LogStreamInterface.BASIC_LOG_DETAIL);
	c.proliferate(increase, e);
	return 0;
    }
}