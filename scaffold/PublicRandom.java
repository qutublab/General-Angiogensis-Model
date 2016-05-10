package scaffold;

import shared.*;

import ec.util.MersenneTwisterFast;

public class PublicRandom implements RandomInterface {

    private MersenneTwisterFast random;

    public PublicRandom(MersenneTwisterFast random) {
	this.random = random;
    }
    
    public boolean nextBoolean() {
	return random.nextBoolean();
    }

    public void nextBytes(byte[] bytes) {
	random.nextBytes(bytes);
    }

    public double nextDouble() {
	return random.nextDouble();
    }

    public float nextFloat() {
	return random.nextFloat();
    }

    public double nextGaussian() {
	return random.nextGaussian();
    }

    public int nextInt() {
	return random.nextInt();
    }

    public int nextInt(int n) {
	return random.nextInt(n);
    }

    public long nextLong() {
	return random.nextLong();
    }

}
