
package tools;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;

import java.io.*;

public class Covariance {

    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }


    private static int columnCharToIndex(char c) {
	if (c < 'A' || c > 'Z') {
	    die("[Covariance.columnCharToIndex]");
	}
	int index = c - 'A';
	return index;
    }

    private static int convertColumnLabel(String s) {
    }

    private static int extractColumnIndex(String s) {
	String label = "";
	boolean letterFound = true;
	int index = 0;
	while (index < s.length() && letterFound) {
	    char c = s.charAt(index);
	    if (Character.isLetter(c)) {
		label = label + c;
		index++;
	    }
	    else {
		letterFound = false;;
	    }
	}
	if (label.length() == 0 || label.length() > 2) {
	    die("[Covariance.extractColumnIndex] Column portion of " + s
		+ " must be 1 or 2 letters");
	}
	label = label.toUpperCase();
	int labelLength = label.length();
	int columnIndex = 0;
	// Process the rightmost letter
	columnIndex += columnCharToIndex(label.charAt(labelLength - 1));
	// Process the leftmost letter if the label has two letters
	if (labelLength == 2) {
	    columnIndex += 26 * (columnCharToIndex(label.charAt(0)) + 1);
	}
	return columnIndex;
    }

    private static String extractRowIndex(String s) {
	int index = 0;
	boolean digitFound = false;
	while (index < s.length() && !digitFound) {
	    char c = s.charAt(index);
	    if (Character.isDigit(c)) {
		digitFound = true;
	    }
	    else {
		if (!Character.isLetter(c)) {
		    die(s + " is not a valid spreadsheet location indicator");
		}
		else {
		    index++;
		}
	    }
	}
	if (index == 0 || index == s.length()) {
	    die(s + " is not a valid spreadsheet location indicator");
	}
	String rowString = s.substring(index);
	int rowNum = 0;
	try {
	    rowNum = Integer.parseInt(rowString);
	}
	catch (Exception e) {
	    die(s + " is not a valid spreadsheet location indicator");
	}
	// row indices are 0-based
	int rowIndex = rowNum - 1;
	return rowIndex;
    }

    

    public static void testIndexExtraction() {
	String label;
	int col;
	int row;

	label = "A1";
	expectedCol = 0;
	expectedRow = 0;
	col = extractColumnIndex(label);
	row = extractRowIndex(label);
	if (col != expectedCol || row != expectedRow) {
	    die("[Covariance.testIndexExtraction] label: " + label
		+ " expected column index: " + expectedCol
		+ " extractColumnIndex(" + label + ")=" + col
		+ " expected row index: " + expectedRow
		+ " extractRowIndex(" + label + ")=" + row);
	}

	label = "AB99";
	expectedCol = 28;
	expectedRow = 98;
	col = extractColumnIndex(label);
	row = extractRowIndex(label);
	if (col != expectedCol || row != expectedRow) {
	    die("[Covariance.testIndexExtraction] label: " + label
		+ " expected column index: " + expectedCol
		+ " extractColumnIndex(" + label + ")=" + col
		+ " expected row index: " + expectedRow
		+ " extractRowIndex(" + label + ")=" + row);
	}

	label = "BZ22";
	expectedCol = 77;
	expectedRow = 21;
	col = extractColumnIndex(label);
	row = extractRowIndex(label);
	if (col != expectedCol || row != expectedRow) {
	    die("[Covariance.testIndexExtraction] label: " + label
		+ " expected column index: " + expectedCol
		+ " extractColumnIndex(" + label + ")=" + col
		+ " expected row index: " + expectedRow
		+ " extractRowIndex(" + label + ")=" + row);
	}
    }


    public static void main(String[] args) {

	testIndexExtraction();
	if (true) {return;}
	String fileName = args[0];
	
    }



}