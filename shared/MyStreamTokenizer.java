
package shared;


/*
 * Single and double quote characters are recognized as string
 * delimiters.  The return tokens of type TT_WORD just like nonquoted
 * strings.
 */

import java.io.*;

public class MyStreamTokenizer extends StreamTokenizer {

    private String inputName;

    
    public MyStreamTokenizer(Reader r) {
	super(r);
	quoteChar('\'');
	quoteChar('\"');
    }


    public void setInputName(String inputName) {
	this.inputName = inputName;
    }

    public String getInputName() {
	return inputName;
    }


    public int nextToken() {
	int tkn = -1;
	try {
	    tkn = super.nextToken();
	}
	catch (Exception e) {
	    die(e.toString());
	}
	switch (tkn) {
	case TT_EOF:
	case TT_EOL:
	case TT_WORD:
	    break;
	case '\"':
	case '\'':
	    tkn = TT_WORD;
	    break;
	default:
	    //	    die("Unexpected character: '" + (char) tkn + "' (" + tkn + ") on line " + lineno()
	    //		+ " of file " + inputName);
	}
	return tkn;
    }
    

    public int nextNonEOLToken() {
	int tkn = nextToken();
	while (tkn == TT_EOL) {
	    tkn = nextToken();
	}
	return tkn;
    }


    private void die (String s) {
	System.err.println(s);
	System.exit(1);
    }

    
}
    
