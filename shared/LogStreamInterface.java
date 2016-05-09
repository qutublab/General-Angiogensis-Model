
package shared;

public interface LogStreamInterface {

    public final static int MINIMUM_LOG_DETAIL = 0;
    public final static int BASIC_LOG_DETAIL = 1;
    public final static int EXTRA_LOG_DETAIL = 2;
    public final static int HIGH_LOG_DETAIL = 3;

    public void print(String s, int logDetailLevel);
    public void println(String s, int logDetailLevel);

}
