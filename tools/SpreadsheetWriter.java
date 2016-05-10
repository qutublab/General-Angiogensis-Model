
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

public class SpreadsheetWriter {



    private static final String SHEET_NAME = "Results";

    private static final int[] INITIAL_CONDITIONS_ORDER =
	new int[StateDiagramModelResult.InitialConditions.values().length];
    static {
	INITIAL_CONDITIONS_ORDER[StateDiagramModelResult.InitialConditions.V0B0.ordinal()] = 0;
	INITIAL_CONDITIONS_ORDER[StateDiagramModelResult.InitialConditions.V0B50.ordinal()] = 1;
	INITIAL_CONDITIONS_ORDER[StateDiagramModelResult.InitialConditions.V0B100.ordinal()] = 2;
	INITIAL_CONDITIONS_ORDER[StateDiagramModelResult.InitialConditions.V25B25.ordinal()] = 3;
	INITIAL_CONDITIONS_ORDER[StateDiagramModelResult.InitialConditions.V25B50.ordinal()] = 4;
	INITIAL_CONDITIONS_ORDER[StateDiagramModelResult.InitialConditions.V50B0.ordinal()] = 5;

	// test above order
	int numberOfInitialConditions =
	    StateDiagramModelResult.InitialConditions.values().length;
	boolean[] orderUsed = new boolean[numberOfInitialConditions];
	for (StateDiagramModelResult.InitialConditions ic : StateDiagramModelResult.InitialConditions.values()) {
	    int order = INITIAL_CONDITIONS_ORDER[ic.ordinal()];
	    if (order < 0 || order >= numberOfInitialConditions) {
		die("[SpreadsheetWriter] Out of bounds value: INITIAL_CONDITIONS_ORDER["
		    + ic + ".ordinal()]=" + order);
	    }
	    if (orderUsed[order]) {
		die("[SpreadsheetWriter] INITIAL_CONDITIONS_ORDER uses order "
		    + order + " more than once");
	    }
	    orderUsed[order] = true;
	}
    }



    private static final int FIRST_ROW_NUMBER = 3;
    private static final int FIRST_COL_NUMBER = 4;
    private static final int INTERTABLE_SPACE = 2;

    


    private static SimulationStats readSimulationStats(String fileName) {
	SimulationStats simStats = null;
	String status = null;
	Object obj = null;
	try {
	    FileInputStream fis = new FileInputStream(fileName);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    obj = ois.readObject();
	    ois.close();
	}
	catch (Exception e) {
	    status = "Unable to read file " + fileName + " " + e.toString();
	}
	if (status == null) {
	    String className = obj.getClass().getName();
	    if (className.equals("shared.SimulationStats")) {
		simStats = (SimulationStats) obj;
	    }
	    else {
		status =
		    "File " + fileName + " contains an object of class "
		    + className;
	    }
	}
	if (status != null) {
	    die(status);
	}
	return simStats;
    }

    private static XSSFWorkbook getWorkbook(String fileName) {
	XSSFWorkbook workbook = null;
	if (fileName == null || fileName.equals("")) {
	    workbook = new XSSFWorkbook();
	}
	else {
	    try {
		FileInputStream fis = new FileInputStream(fileName); 
		workbook = new XSSFWorkbook(fis);
		fis.close();
	    }
	    catch (Exception e) {
		die("Unable to open file " + fileName);
	    }
	}
	return workbook;
    }

    private static XSSFSheet getSheet(XSSFWorkbook workbook,
				      String sheetName) {
	XSSFSheet sheet = workbook.getSheet(sheetName);
	if (sheet == null) {
	    sheet = workbook.createSheet(sheetName);
	}
	return sheet;
    }
		
    private static int getNextOpenColumnNumber(XSSFSheet sheet,
					       int rowNumber,
					       int colNumber) {
	XSSFRow row = sheet.getRow(rowNumber);
	if (row == null) {
	    row = sheet.createRow(rowNumber);
	}
	
	// look for two consecutive empty cells
	XSSFCell cell0 = row.getCell(colNumber);
	XSSFCell cell1 = row.getCell(colNumber + 1);
	while (cell0 != null || cell1 != null) {
	    colNumber++;
	    cell0 = cell1;
	    cell1 = row.getCell(colNumber + 1);
	}
	return colNumber;
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

    private static void process(String[] statsFileNames,
				String templateFileName,
				String outputFileName) {
	// First verify stats files by reading them before doing any
	// writing to files
	SimulationStats[] simStats =
	    new SimulationStats[statsFileNames.length];
	for (int i = 0; i < statsFileNames.length; i++) {
	    simStats[i] = readSimulationStats(statsFileNames[i]);
	}

	XSSFWorkbook workbook = getWorkbook(templateFileName);
	XSSFSheet sheet = getSheet(workbook, SHEET_NAME);

	int colNumber = getNextOpenColumnNumber(sheet,
						FIRST_ROW_NUMBER,
						FIRST_COL_NUMBER);
	for (int i = 0; i < simStats.length; i++) {
	    SimulationStats stats = simStats[i];
	    int order =
		INITIAL_CONDITIONS_ORDER[stats.initialConditions.ordinal()];
	    double avg = 0;
	    double stDev = 0;;
	    for (int j = 0; j < 3; j++) {
		switch (j) {
		case 0:
		    // sprout length
		    avg = stats.average.limitedXYSproutLengthMicrons;
		    stDev = stats.standardDeviation.limitedXYSproutLengthMicrons;
		    break;
		case 1:
		    // branch count
		    avg = stats.average.limitedXYBranchCount;
		    stDev = stats.standardDeviation.limitedXYBranchCount;
		    break;
		case 2:
		    // branch length
		    avg = stats.average.individualLimitedXYBranchLengthsMicrons.getFirst();
		    stDev = stats.standardDeviation.individualLimitedXYBranchLengthsMicrons.getFirst();
		    break;
		default:
		    die("[SpreadSheetWriter.process] Unexpected index: "
			+ j);
		}
		int rowNumber =
		    FIRST_ROW_NUMBER
		    + (j * StateDiagramModelResult.InitialConditions.values().length)
		    + (j * INTERTABLE_SPACE) 
		    + order;
		XSSFRow row = sheet.getRow(rowNumber);
		if (row == null) {
		    row = sheet.createRow(rowNumber);
		}
		XSSFCell cell = row.getCell(colNumber);
		if (cell == null) {
		    cell = row.createCell(colNumber);
		}
		cell.setCellValue(avg);
		cell = row.getCell(colNumber + 1);
		if (cell == null) {
		    cell = row.createCell(colNumber + 1);
		}
		cell.setCellValue(stDev);
	    }
	}
	writeToFile(outputFileName, workbook);
    }
    
    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }


    private static void test() {
	FileOutputStream fos;
	ObjectOutputStream oos;

	String fileNameA = "tempA";
	String fileNameB = "tempB";
	String fileNameC = "tempC.xlsx";
	BasicStats b;
	SimulationStats s0 = new SimulationStats();
	b = new BasicStats();
	b.limitedXYBranchCount = 7;
	b.limitedXYSproutLengthMicrons = 500;
	b.individualLimitedXYBranchLengthsMicrons = new LinkedList<Double>();
	b.individualLimitedXYBranchLengthsMicrons.add(15.0);
	s0.average = b;
	b = new BasicStats();
	b.limitedXYBranchCount = .71;
	b.limitedXYSproutLengthMicrons = 5.2;
	b.individualLimitedXYBranchLengthsMicrons = new LinkedList<Double>();
	b.individualLimitedXYBranchLengthsMicrons.add(1.53);
	s0.standardDeviation = b;
	s0.initialConditions = StateDiagramModelResult.InitialConditions.V50B0;
	try {
	    fos = new FileOutputStream(fileNameA);
	    oos = new ObjectOutputStream(fos);
	    oos.writeObject(s0);
	    oos.close();
	}
	catch (Exception e) {
	    die(e.toString());
	}


	SimulationStats s1 = new SimulationStats();
	b = new BasicStats();
	b.limitedXYBranchCount = 9;
	b.limitedXYSproutLengthMicrons = 700;
	b.individualLimitedXYBranchLengthsMicrons = new LinkedList<Double>();
	b.individualLimitedXYBranchLengthsMicrons.add(25.0);
	s1.average = b;
	b = new BasicStats();
	b.limitedXYBranchCount = .94;
	b.limitedXYSproutLengthMicrons = 7.5;
	b.individualLimitedXYBranchLengthsMicrons = new LinkedList<Double>();
	b.individualLimitedXYBranchLengthsMicrons.add(2.56);
	s1.standardDeviation = b;
	s1.initialConditions = StateDiagramModelResult.InitialConditions.V0B0;
	try {
	    fos = new FileOutputStream(fileNameB);
	    oos = new ObjectOutputStream(fos);
	    oos.writeObject(s1);
	    oos.close();
	}
	catch (Exception e) {
	    die(e.toString());
	}
	
	process(new String[] {fileNameA, fileNameB}, fileNameC, fileNameC);
    }

    public static void main(String[] args) {
	test();
	if (true) {return;}

	String[] statsFileNames = new String[args.length - 2];
	for (int i = 0; i < args.length - 2; i++) {
	    statsFileNames[i] = args[i];
	}
	String outputFileName = args[args.length - 2];
	String templateFileName = args[args.length - 1];

	process(statsFileNames, templateFileName, outputFileName);
    }

}