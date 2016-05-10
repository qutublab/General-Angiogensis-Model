
import java.util.*;

public class PseudoGenAlg {

    private static final int NUMBER_OF_GENERATIONS = 40;
    private static final int POPULATION_SIZE = 20;
    private static final double KEEP_FRACTION = .50;
    private static final double SCORE_MULTIPLIER = 100;

    private static final int KEEP_COUNT =
	(int) Math.round(POPULATION_SIZE * KEEP_FRACTION);

    private static Random rand;

    private static void initialize(String seedArg) {
	long seed;
	if (seedArg == null || seedArg.equals("")) {
	    seed = System.currentTimeMillis();
	}
	else {
	    seed = Long.parseLong(seedArg);
	}
	rand = new Random(seed);
	System.out.println("[PseudoGenAlg.initialize] seed=" + seed);
    }

    private static double generateScore() {
	double r;
	r = rand.nextDouble();
	//	r = rand.nextGaussian();
	return r * SCORE_MULTIPLIER;
    }


    private static LinkedList<Double> createPopulation() {
	LinkedList<Double> pop = new LinkedList<Double>();
	for (int i = 0; i < POPULATION_SIZE; i++) {
	    double s = generateScore();
	    pop.add(s);
	}
	Collections.sort(pop);
	return pop;
    }

    private static void updatePopulation(LinkedList<Double> pop) {
	int count = 0;
	for (Iterator<Double> i = pop.iterator(); i.hasNext();) {
	    double d = i.next();
	    if (count < KEEP_COUNT) {
		count ++;
	    }
	    else {
		i.remove();
	    }
	}
	for (int i = 0; i < POPULATION_SIZE - KEEP_COUNT; i++) {
	    double s = generateScore();
	    pop.add(s);
	}
	Collections.sort(pop);
    }


    private static void printBestScore(int generationNumber,
				       LinkedList<Double> pop) {
	System.out.println("[PseudogenAlg.run] generationNumber="
			   + generationNumber
			   + " best score: " + pop.getFirst()
			   + " worst score: " + pop.getLast());
    }

    private static void run() {
	int generationNumber = 1;
	LinkedList<Double> pop = createPopulation();
	printBestScore(generationNumber, pop);
	for (generationNumber = 2;
	     generationNumber <= NUMBER_OF_GENERATIONS;
	     generationNumber++) {
	    updatePopulation(pop);
	    printBestScore(generationNumber, pop);
	}
    }




    public static void main(String[] args) {
	String seedArg = "";
	if (args.length > 0) {
	    seedArg = args[0];
	}
	initialize(seedArg);
	run();
    }



}