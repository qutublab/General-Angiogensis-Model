
/*
 0. get parameters
 1. open spreadsheet / create spreadsheet
 2. for each input file
 3.   read file contents
 4.   for each data type (branch count, branch length, sprout length)
 5.     write average and standard deviation


*/


package tools;

import shared.*;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.*;

public class NotesWriter {



    private static final String SHEET_NAME = "Results";

    private static final int FIRST_ROW_NUMBER = 0;
    private static final int FIRST_COL_NUMBER = 0;


    private static LinkedList<LinkedList<String>> readTextFile(String fileName) {
	LinkedList<LinkedList<String>> contents = new LinkedList<LinkedList<String>>();
	File f = new File(fileName);
	FileReader fr = null;
	try {
	    fr = new FileReader(f);
	}
	catch (Exception e) {
	    die("[NotesWriter.readTextFile] Unable to create file reader "
		+ e.toString());
	}
	BufferedReader br = new BufferedReader(fr);
	String line = null;
	try {
	    line = br.readLine();
	}
	catch (Exception e) {
	    die("[NotesWriter.readTextFile] Unable to read from file "
		+ fileName + "    " + e.toString());
	}
	while (line != null) {
	    LinkedList<String> lineWords = new LinkedList<String>();
	    String word = "";
	    boolean inWord = false;
	    for (int i = 0; i < line.length(); i++) {
		char ch = line.charAt(i);
		if (inWord) {
		    if (Character.isWhitespace(ch)) {
			inWord = false;
			lineWords.addLast(word);
			word = "";
		    }
		    else {
			word = word + ch;
		    }
		}
		else {
		    if (!Character.isWhitespace(ch)) {
			inWord = true;
			word = "" + ch;
		    }
		}
	    }
	    if (inWord) {
		lineWords.addLast(word);
	    }
	    if (lineWords.size() > 0) {
		contents.addLast(lineWords);
	    }
	    try {
		line = br.readLine();
	    }
	    catch (Exception e) {
		die("[NotesWriter.readTextFile] Unable to read from file "
		    + fileName + "    " + e.toString());
		    }
	}
	try {
	    br.close();
	}
	catch (Exception e) {
	    die("[NotesWriter.readTextFile] Unable to close file "
		+ fileName + "    " + e.toString());
	}
	return contents;
    }
	

    private static XSSFWorkbook getWorkbook(String fileName) {
	XSSFWorkbook workbook = null;
	if (fileName == null || fileName.equals("")) {
	    workbook = new XSSFWorkbook();
	}
	else {
	    File f = new File(fileName);
	    if (f.exists()) {
		try {
		    FileInputStream fis = new FileInputStream(fileName); 
		    workbook = new XSSFWorkbook(fis);
		    fis.close();
		}
		catch (Exception e) {
		    die("Unable to open file " + fileName);
		}
	    }
	    else {
		workbook = new XSSFWorkbook();
	    }
	}
	return workbook;
    }
    
    private static XSSFSheet getFirstSheet(XSSFWorkbook workbook) {
	XSSFSheet sheet = null;
	int numberOfSheets = workbook.getNumberOfSheets();
	if (numberOfSheets == 0) {
	    sheet = workbook.createSheet();
	}
	else {
	    sheet = workbook.getSheetAt(0);
	    if (sheet == null) {
		sheet = workbook.createSheet();
	    }
	}
	return sheet;
    }
		
    private static int getNextOpenRowNumber(XSSFSheet sheet,
					    int rowNumber,
					    int colNumber) {
	XSSFCell cell;
	do {
	    XSSFRow row = sheet.getRow(rowNumber);
	    if (row == null) {
		row = sheet.createRow(rowNumber);
		break;
	    }
	    cell = row.getCell(colNumber);
	    rowNumber++;
	} while (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK);

	return rowNumber;
    }

    private static void writeToFile(String fileName, XSSFWorkbook workbook) {
	File f = new File(fileName);
	try {
	    FileOutputStream fos = new FileOutputStream(f);
	    workbook.write(fos);
	    fos.close();
	}
	catch (Exception e) {
	    die("Unable to write spreadsheet to file " + fileName
		+ "  " + e.toString());
	}
	System.out.println("Wrote " + fileName);
    }

    private static void process(String[] notesFileNames,
				String spreadsheetFileName,
				String outputFileName) {
	XSSFWorkbook workbook = getWorkbook(spreadsheetFileName);
	XSSFSheet sheet = getFirstSheet(workbook);

	int rowNumber = getNextOpenRowNumber(sheet,
					     FIRST_ROW_NUMBER,
					     FIRST_COL_NUMBER);
	for (String fileName : notesFileNames) {
	    LinkedList<LinkedList<String>> contents = readTextFile(fileName);
	    for (Iterator<LinkedList<String>> i = contents.iterator(); i.hasNext();) {
		XSSFRow row = sheet.getRow(rowNumber);
		if (row == null) {
		    row = sheet.createRow(rowNumber);
		}
		rowNumber++;
		LinkedList<String> lineWords = i.next();
		int colNumber = FIRST_COL_NUMBER;
		for (Iterator<String> j = lineWords.iterator(); j.hasNext();) {
		    String word = j.next();
		    XSSFCell cell = row.getCell(colNumber);
		    if (cell == null) {
			cell = row.createCell(colNumber);
		    }
		    colNumber++;
		    boolean isNumber = true;
		    double number = 0;
		    try {
			number = Double.parseDouble(word);
		    }
		    catch (Exception e) {
			isNumber = false;
		    }
		    if (isNumber) {
			cell.setCellValue(number);
		    }
		    else {
			cell.setCellValue(word);
		    }
		}
	    }
	}
	writeToFile(outputFileName, workbook);
    }
    
    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }


    public static void main(String[] args) {
	String spreadsheetFileName = args[0];
	String[] textFileNames = new String[args.length - 1];
	for (int i = 1; i < args.length; i++) {
	    textFileNames[i - 1] = args[i];
	}
	process(textFileNames, spreadsheetFileName, spreadsheetFileName);
    }

}