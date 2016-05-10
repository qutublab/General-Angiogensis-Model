package scaffold;

import sim.engine.*;

import java.io.*;

// Writes a snapshot of sprout growth to a file 

public class GrowthCapture implements Steppable {

    private static final double LOG10 = Math.log(10);

    private int growthCaptureInterval;
    private String fileNamePrefix;
    // length for all step number suffixes; set to length of last step number
    private int forcedLength;

    private boolean writeFiles = true;


    public GrowthCapture(Environment env) {
	growthCaptureInterval = env.getGrowthCaptureInterval();
	fileNamePrefix = Environment.SPROUT_FILE_PREFIX + env.getOutputFileSuffix();
	forcedLength = numberLength(env.getSimulationLengthInTimeSteps());
    }

    public void disable() {
	writeFiles = false;
    }


    private int numberLength(long n) {
	n = Math.abs(n);
	int len = 1;
	while (n > 9) {
	    n = n / 10;
	    len++;
	}
	return len;
    }


    // reconcile this with Environment.writeOutput
    public void step(SimState state) {
	Environment env = (Environment) state;
	long completedSteps = env.schedule.getSteps();
	long currentStep = completedSteps + 1;

	if (!writeFiles) {
	    return;
	}

	// Only capture growth at the approriate step
	if (growthCaptureInterval == 0 || currentStep % growthCaptureInterval != 0) {
	    return;
	}

	// add leading zeros
	String step = "";
	int paddingLength = forcedLength - numberLength(currentStep);
	for (int i = 0; i < paddingLength; i ++) {
	    step += "0";
	}
	step += "" + currentStep;

	String fileName = fileNamePrefix + "s" + step;
	File sproutFile = new File(fileName);

	PrintStream ps = null;

	try {
	    ps = new PrintStream(new FileOutputStream(sproutFile));
	    env.printSproutCoordinates(ps);
	    ps.close();
	}
	catch (Exception e) {
	    Environment.die("[GrowthCapture.step] Unable to write output file:   "
			    + e.toString());
	}

	//	System.out.println("GrowthCapture stepped   " + state.schedule.getSteps());
    }




    // reconcile this with Environment.writeOutput
    public void stepOLD(SimState state) {
	Environment env = (Environment) state;
	long completedSteps = env.schedule.getSteps();
	long currentStep = completedSteps + 1;
	int maxSteps = env.getSimulationLengthInTimeSteps();
	int stepLength = numberLength(currentStep);
	int maxStepsLength = numberLength(maxSteps);

	// add leading zeros
	String step = "";
	int paddingLength = maxStepsLength - stepLength;
	for (int i = 0; i < paddingLength; i ++) {
	    step += "0";
	}
	step += "" + currentStep;

	String prefix = Environment.SPROUT_FILE_PREFIX;
	String suffix = env.getOutputFileSuffix();

	String fileName = prefix + suffix + "s" + step;
	File sproutFile = new File(fileName);

	PrintStream ps = null;

	try {
	    ps = new PrintStream(new FileOutputStream(sproutFile));
	    env.printSproutCoordinates(ps);
	    ps.close();
	}
	catch (Exception e) {
	    Environment.die("[GrowthCapture.step] Unable to write output file:   "
			    + e.toString());
	}

	//	System.out.println("GrowthCapture stepped   " + state.schedule.getSteps());
    }
    
}