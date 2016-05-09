
package angiogenesis;

import shared.*;

public class ActivationRule extends Rule {


    //    public static double vegfThresholdNgPerMl = 0; // .5;

    private static double vegf0NgmlActivationProbability;
    private static double vegf25NgmlActivationProbability;

    private static double bdnf0NgmlActivationProbability;
    private static double bdnf50NgmlActivationProbability;

    private static RandomInterface random;

    public ActivationRule(Parameters p, EnvironmentInterface e) {
	ruleName = "Activation";
	ruleIdentifier = "A";
	versionString = "0.2";

	random = e.getRandom();

	vegf0NgmlActivationProbability = 
	    p.getVegf0NgmlActivationProbability();
	vegf25NgmlActivationProbability =
	    p.getVegf25NgmlActivationProbability();

	bdnf0NgmlActivationProbability = 
	    p.getBdnf0NgmlActivationProbability();
	bdnf50NgmlActivationProbability = 
	    p.getBdnf50NgmlActivationProbability();



	//	vegfThresholdNgPerMl = p.getVegfActivationThresholdNgPerMl();
	OutputBufferInterface buffer = e.getOutputBuffer();
	buffer.println(ruleName + "  " + versionString);
	//	buffer.println("Using a vegf concentration threshold of " + vegfThresholdNgPerMl
	//		       + " for activation");

    }

    /*
    public void initialize(EnvironmentInterface e) {
	super.initialize(e);
    }
    */



    private static double vegfActivationProbability(double vegfNgPerMl) {
	double vegfSlope =
	    (vegf25NgmlActivationProbability - vegf0NgmlActivationProbability)
	    / 25;
	double vegfYIntercept = vegf0NgmlActivationProbability;
	double probability = 
	    Math.min(1,
		     Math.max(0, (vegfSlope * vegfNgPerMl) + vegfYIntercept));
	return probability;
    }
	


    private static boolean closeEnough(double a, double b) {
	double tolerance = .0000000001;
	return Math.abs(a - b) < tolerance;
    }

    private static void testVegfActivationProbability() {
	System.out.println("Begin testVegfActivationProbability");
	vegf0NgmlActivationProbability = .10;
	vegf25NgmlActivationProbability = .35;
	double vegfNgPerMl;
	double expectedActivationProbability;
	double activationProbability;

	vegfNgPerMl = 0;
	expectedActivationProbability = .10;
	activationProbability = vegfActivationProbability(vegfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: vegfActivationProbability("
			      + vegfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}
	
	vegfNgPerMl = 25;
	expectedActivationProbability = .35;
	activationProbability = vegfActivationProbability(vegfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: vegfActivationProbability("
			      + vegfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}
	
	vegfNgPerMl = 10;
	expectedActivationProbability = .20;
	activationProbability = vegfActivationProbability(vegfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: vegfActivationProbability("
			      + vegfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}

	vegfNgPerMl = 35;
	expectedActivationProbability = .45;
	activationProbability = vegfActivationProbability(vegfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: vegfActivationProbability("
			      + vegfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}

	vegfNgPerMl = 99999;
	expectedActivationProbability = 1;
	activationProbability = vegfActivationProbability(vegfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: vegfActivationProbability("
			      + vegfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}

	System.out.println("testVegfActivationProbability passed!");
    }



    private static void testBdnfActivationProbability() {
	System.out.println("Begin testBdnfActivationProbability");
	bdnf0NgmlActivationProbability = .05;
	bdnf50NgmlActivationProbability = .30;
	double bdnfNgPerMl;
	double expectedActivationProbability;
	double activationProbability;

	bdnfNgPerMl = 0;
	expectedActivationProbability = .05;
	activationProbability = bdnfActivationProbability(bdnfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: bdnfActivationProbability("
			      + bdnfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}
	
	bdnfNgPerMl = 50;
	expectedActivationProbability = .30;
	activationProbability = bdnfActivationProbability(bdnfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: bdnfActivationProbability("
			      + bdnfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}
	
	bdnfNgPerMl = 10;
	expectedActivationProbability = .10;
	activationProbability = bdnfActivationProbability(bdnfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: bdnfActivationProbability("
			      + bdnfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}

	bdnfNgPerMl = 70;
	expectedActivationProbability = .40;
	activationProbability = bdnfActivationProbability(bdnfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: bdnfActivationProbability("
			      + bdnfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}

	bdnfNgPerMl = 99999;
	expectedActivationProbability = 1;
	activationProbability = bdnfActivationProbability(bdnfNgPerMl);
	if (!closeEnough(activationProbability, expectedActivationProbability)) {
	    SimpleRuleSet.die("Fail: bdnfActivationProbability("
			      + bdnfNgPerMl + ")=" + activationProbability
			      + "  expected "
			      + expectedActivationProbability);
	}

	System.out.println("testBdnfActivationProbability passed!");
    }



    private static double bdnfActivationProbability(double bdnfNgPerMl) {
	double bdnfSlope =
	    (bdnf50NgmlActivationProbability - bdnf0NgmlActivationProbability)
	    / 50;
	double bdnfYIntercept = bdnf0NgmlActivationProbability;
	double probability = 
	    Math.min(1,
		     Math.max(0, (bdnfSlope * bdnfNgPerMl) + bdnfYIntercept));
	return probability;
    }
	

    private static void testActivationProbability() {
	System.out.println("Begin testActivationProbability");
	vegf0NgmlActivationProbability = 0;
	vegf25NgmlActivationProbability = .25;
	bdnf0NgmlActivationProbability = 0;
	bdnf50NgmlActivationProbability = .50;
	
	double vegfNgPerMl;
	double bdnfNgPerMl;
	double expectedActProb;
	double actProb;

	vegfNgPerMl = 0;
	bdnfNgPerMl = 0;
	expectedActProb = 0;
	actProb = activationProbability(vegfNgPerMl, bdnfNgPerMl);
	if (!closeEnough(actProb, expectedActProb)) {
	    SimpleRuleSet.die("Fail: activationProbability(" + vegfNgPerMl
			      + ", " + bdnfNgPerMl + ")=" + actProb
			      + "  expected "
			      + expectedActProb);
	}

	vegfNgPerMl = 60;
	bdnfNgPerMl = 50;
	expectedActProb = .80;
	actProb = activationProbability(vegfNgPerMl, bdnfNgPerMl);
	if (!closeEnough(actProb, expectedActProb)) {
	    SimpleRuleSet.die("Fail: activationProbability(" + vegfNgPerMl
			      + ", " + bdnfNgPerMl + ")=" + actProb
			      + "  expected "
			      + expectedActProb);
	}

	vegfNgPerMl = 80;
	bdnfNgPerMl = 90;
	expectedActProb = .98;
	actProb = activationProbability(vegfNgPerMl, bdnfNgPerMl);
	if (!closeEnough(actProb, expectedActProb)) {
	    SimpleRuleSet.die("Fail: activationProbability(" + vegfNgPerMl
			      + ", " + bdnfNgPerMl + ")=" + actProb
			      + "  expected "
			      + expectedActProb);
	}

	vegfNgPerMl = 9999;
	bdnfNgPerMl = 9999;
	expectedActProb = 1;
	actProb = activationProbability(vegfNgPerMl, bdnfNgPerMl);
	if (!closeEnough(actProb, expectedActProb)) {
	    SimpleRuleSet.die("Fail: activationProbability(" + vegfNgPerMl
			      + ", " + bdnfNgPerMl + ")=" + actProb
			      + "  expected "
			      + expectedActProb);
	}


	System.out.println("testActivationProbability passed!");
    }

    private static double activationProbability(double vegfNgPerMl,
						double bdnfNgPerMl) {
	double vegfActProb = vegfActivationProbability(vegfNgPerMl);
	double bdnfActProb = bdnfActivationProbability(bdnfNgPerMl);
	// The probability of activation is compliment of probability
	// of nonactivation.  The probability of nonactivation is the
	// product of the individual probabilities of nonactivation.

	double actProb = 1 - ((1 - vegfActProb) * (1 - bdnfActProb));
	return actProb;
    }

    public RuleResult act(CellInterface c,
			  Storage s,
			  EnvironmentInterface e) {
	double vegfNgPerMl = 
	    c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.VEGF);
	double bdnfNgPerMl = 
	    c.getAvgNgPerMl(EnvironmentInterface.ConcentrationType.BDNF);

	double actProb = activationProbability(vegfNgPerMl, bdnfNgPerMl);
	double r = random.nextDouble();	
	if (r < actProb) {
	    int result = c.activate();
	    if (result > 0) {
		log.println("Activation rule: activated cell " + c.getIdNumber() + "; tip cell "
			    + c.getPredecessor().getPredecessor().getIdNumber()
			    + "; stalk cell " + c.getPredecessor().getIdNumber(),
			    LogStreamInterface.BASIC_LOG_DETAIL);
	    }
	}
	return null;
    }


    public static void main(String[] args) {
	testVegfActivationProbability();
	testBdnfActivationProbability();
	testActivationProbability();
    }

}