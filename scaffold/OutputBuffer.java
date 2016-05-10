
package scaffold;

import shared.*;

import java.util.*;
import java.io.*;

public class OutputBuffer implements OutputBufferInterface {

    private static final String COMMENT_PREFIX = "////";

    private LinkedList<String> buffer;

    private boolean writable = true;

    public OutputBuffer() {
	buffer = new LinkedList<String>();
    }

    public boolean println(String s) {
	if (writable) {
	    buffer.add(s);
	}
	return writable;
    }

    public void setWritable(boolean writable) {
	this.writable = writable;
    }

    public LinkedList<String> getBuffer() {
	return buffer;
    }


    /* 
     * Will have problems if the buffer contains newline characters
     * because the comment prefix wil not start the line.
     */
    public void dumpBuffer(String prefix, PrintStream ps) {
	for (Iterator<String> i = buffer.iterator(); i.hasNext();) {
	    String s = i.next();
	    ps.println(prefix + s);
	}
    }
}
