
package tools;

import shared.*;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;

import java.io.*;

public class Extractor {

    private static final String[] HEADER1 =
	new String[] {"tip Idle -> Idle",
		      "tip Idle -> Migration",
		      "tip Idle -> Proliferation",
		      "tip Idle -> Branching",
		      
		      "tip Migration -> Idle",
		      "tip Migration -> Migration",
		      "tip Migration -> Proliferation",
		      "tip Migration -> Branching",
		      
		      "tip Proliferation -> Idle",
		      "tip Proliferation -> Migration",
		      "tip Proliferation -> Proliferation",
		      "tip Proliferation -> Branching",
		      
		      "tip Branching -> Idle",
		      "tip Branching -> Migration",
		      "tip Branching -> Proliferation",
		      "tip Branching -> Branching",
		      
		      "stalk Idle -> Idle",
		      "stalk Idle -> Proliferation",
		      "stalk Idle -> Branching",
		      
		      "stalk Elongation -> Idle",
		      "stalk Elongation -> Proliferation",
		      "stalk Elongation -> Branching",
		      
		      "stalk Proliferation -> Idle",
		      "stalk Proliferation -> Proliferation",
		      "stalk Proliferation -> Branching",
		      
		      "stalk Branching -> Idle",
		      "stalk Branching -> Proliferation",
		      "stalk Branching -> Branching"};
    
    
    
    private static void die(String s) {
	System.err.println(s);
	System.exit(1);
    }

    private static StateDiagramModelResult read(String fileName) {
	Object obj = null;
	try {
	    FileInputStream fis = new FileInputStream(fileName);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    obj = ois.readObject();
	    ois.close();
	}
	catch (Exception e) {
	    die("[Extractor.read] Cannot read file " + fileName
		+ "  " + e.toString());
	}
	String className = obj.getClass().getName();
	if (!className.equals("shared.StateDiagramModelResult")) {
	    die("[Extractor.read] Object in file " + fileName
		+ " is of class: " + className);
	}
	StateDiagramModelResult sdmr = (StateDiagramModelResult) obj;
	return sdmr;
    }


    private static void fillHeader(XSSFRow row) {
	XSSFCell cell = getCell(row, 0);
	cell.setCellValue("Model");
	int colNumber = 1;
	for (int labelNumber = 0; labelNumber < HEADER1.length; labelNumber++) {
	    cell = getCell(row, colNumber);
	    cell.setCellValue(HEADER1[labelNumber]);
	    colNumber++;
	}
    }


    private static void fillOne(XSSFRow row,
				String modelName,
				StateDiagramModelResult sdmr) {
	StateDiagramModel sdm = sdmr.model;
	// Use the same order as HEADER1
	double[] transProbs =
	    new double[] {sdm.tipQuiescentToQuiescent,
			  sdm.tipQuiescentToMigration,
			  sdm.tipQuiescentToProliferation,
			  sdm.tipQuiescentToBranching,

			  sdm.tipMigrationToQuiescent,
			  sdm.tipMigrationToMigration,
			  sdm.tipMigrationToProliferation,
			  sdm.tipMigrationToBranching,

			  sdm.tipProliferationToQuiescent,
			  sdm.tipProliferationToMigration,
			  sdm.tipProliferationToProliferation,
			  sdm.tipProliferationToBranching,

			  sdm.tipBranchingToQuiescent,
			  sdm.tipBranchingToMigration,
			  sdm.tipBranchingToProliferation,
			  sdm.tipBranchingToBranching,

			  sdm.stalkQuiescentToQuiescent,
			  sdm.stalkQuiescentToProliferation,
			  sdm.stalkQuiescentToBranching,

			  sdm.stalkElongationToQuiescent,
			  sdm.stalkElongationToProliferation,
			  sdm.stalkElongationToBranching,

			  sdm.stalkProliferationToQuiescent,
			  sdm.stalkProliferationToProliferation,
			  sdm.stalkProliferationToBranching,

			  sdm.stalkBranchingToQuiescent,
			  sdm.stalkBranchingToProliferation,
			  sdm.stalkBranchingToBranching};

	XSSFCell cell;
	cell = getCell(row, 0);
	cell.setCellValue(modelName);
	int colNumber = 1;
	for (double prob : transProbs) {
	    cell = getCell(row, colNumber);
	    cell.setCellValue(prob);
	    colNumber++;
	}
	cell = getCell(row, colNumber);
	cell.setCellValue(sdmr.score);
    }






    private static XSSFRow getRow(XSSFSheet sheet, int rowNumber) {
	XSSFRow row = sheet.getRow(rowNumber);
	if (row == null) {
	    row = sheet.createRow(rowNumber);
	}
	return row;
    }

    private static XSSFCell getCell(XSSFRow row, int colNumber) {
	XSSFCell cell = row.getCell(colNumber);
	if (cell == null) {
	    cell = row.createCell(colNumber);
	}
	return cell;
    }



    private static void fill(String[] inputFileNames, String outputFileName) {
	XSSFWorkbook workbook = null;
	workbook = new XSSFWorkbook();
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
	XSSFRow row = getRow(sheet, 0);
	fillHeader(row);
	int rowNumber = 1;
	for (String ifn : inputFileNames) {
	    StateDiagramModelResult sdmr = read(ifn);
	    row = getRow(sheet, rowNumber);
	    fillOne(row, ifn, sdmr);
	    rowNumber++;
	}
	writeToFile(outputFileName, workbook);
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



    public static void main(String[] args) {
	String outputFileName = "sdmrCovariance.xlsx";
	fill(args, outputFileName);
    }



}