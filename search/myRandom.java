package search;

import java.util.Random;
import shared.RandomInterface;

public class myRandom extends Random implements RandomInterface
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public myRandom()
	{
	}
	
	public myRandom(long seed)
	{
		super(seed);
	}
	
	public static void main(String[]args)
	{
		System.out.println("myRandom");
		myRandom r = new myRandom();
		System.out.println(r.nextInt(10));
	}
}

