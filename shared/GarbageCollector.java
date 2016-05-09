
package shared;

public class GarbageCollector {

    public static void run() {
	Runtime rt = Runtime.getRuntime();
	long freeBegin = rt.freeMemory();
	long totalBegin = rt.totalMemory();
	long maxBegin = rt.maxMemory();
	long timeBegin = System.currentTimeMillis();
	System.gc();
	long timeEnd = System.currentTimeMillis();
	long freeEnd = rt.freeMemory();
	long totalEnd = rt.totalMemory();
	long maxEnd = rt.maxMemory();
	long dTime = timeEnd - timeBegin;
	long dFree = freeEnd - freeBegin;
	long dTotal = totalEnd - totalBegin;
	long dMax = maxEnd - maxBegin;
	System.out.println("[GarbageCollector.run]"
			   + " dTime=" + dTime + "ms"
			   + " dFree=" + dFree + " (" + freeEnd + ")"
			   + " dTotal=" + dTotal + " (" + totalEnd + ")"
			   + " dMax=" + dMax + " (" + maxEnd + ")");
    }

}
