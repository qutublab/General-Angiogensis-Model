package shared;


/*
 * NOTE: do not assume that methods are synchronized and that
 * instances can be shared by multiple threads.
 *
 * This interface is needed because Mason's MersenneTwisterFast is not
 * a sublclass of Random (because its methods are not synchronized).
 */

public interface RandomInterface {

    public boolean nextBoolean();
    public void nextBytes(byte[] bytes);
    public double nextDouble();
    public float nextFloat();
    public double nextGaussian();
    public int nextInt();
    public int nextInt(int n);
    public long nextLong();


}
