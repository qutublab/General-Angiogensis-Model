
package scaffold;

import shared.LogStreamInterface;

import java.io.*;

public class LogStream implements LogStreamInterface {

    PrintStream ps;
    boolean echoToStdOut;
    int loggingDetailLevel;

    public LogStream(String fileName, boolean echoToStdOut, int loggingDetailLevel) {
	this.loggingDetailLevel = loggingDetailLevel;
	if (loggingDetailLevel >= 0) {
	    File f = new File(fileName);
	    try {
		ps = new PrintStream(f);
	    }
	    catch (Exception e) {
		Environment.die("Unable to open stream for log file " + fileName
				+ "     " + e.toString());
	    }
	    this.echoToStdOut = echoToStdOut;
	    this.loggingDetailLevel = loggingDetailLevel;
	}
	else {
	    //	    System.out.println("Logging disabled");
	}
    }

    public void print(String s, int detailLevel) {
	if (loggingDetailLevel >= 0 && detailLevel <= loggingDetailLevel) {
	    ps.print(s);
	    if (echoToStdOut) {
		System.out.print(s);
	    }
	}
    }
    
    public void println(String s, int detailLevel) {
	if (loggingDetailLevel >= 0 && detailLevel <= loggingDetailLevel) {
	    ps.println(s);
	    if (echoToStdOut) {
		System.out.println(s);
	    }
	}
    }

    public void close() {
	if (ps != null) {
	    ps.close();
	}
    }

}
