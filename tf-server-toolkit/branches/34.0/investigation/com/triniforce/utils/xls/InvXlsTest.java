/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.utils.xls;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.triniforce.db.test.TFTestCase;

public class InvXlsTest extends TFTestCase{
    

    private static SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM", new Locale("en"));

    private static final String[] titles = {
            "ID", "Project Name", "Owner", "Days", "Start", "End"};

    //sample data to fill the sheet.
    private static final String[][] data = {
            {"1.0", "Marketing Research Tactical Plan", "J. Dow", "70", "9-Jul", null,
                "x", "x", "x", "x", "x", "x", "x", "x", "x", "x", "x"},
            null,
            {"1.1", "Scope Definition Phase", "J. Dow", "10", "9-Jul", null,
                "x", "x", null, null,  null, null, null, null, null, null, null},
            {"1.1.1", "Define research objectives", "J. Dow", "3", "9-Jul", null,
                    "x", null, null, null,  null, null, null, null, null, null, null},
            {"1.1.2", "Define research requirements", "S. Jones", "7", "10-Jul", null,
                "x", "x", null, null,  null, null, null, null, null, null, null},
            {"1.1.3", "Determine in-house resource or hire vendor", "J. Dow", "2", "15-Jul", null,
                "x", "x", null, null,  null, null, null, null, null, null, null},
            null,
            {"1.2", "Vendor Selection Phase", "J. Dow", "19", "19-Jul", null,
                null, "x", "x", "x",  "x", null, null, null, null, null, null},
            {"1.2.1", "Define vendor selection criteria", "J. Dow", "3", "19-Jul", null,
                null, "x", null, null,  null, null, null, null, null, null, null},
            {"1.2.2", "Develop vendor selection questionnaire", "S. Jones, T. Wates", "2", "22-Jul", null,
                null, "x", "x", null,  null, null, null, null, null, null, null},
            {"1.2.3", "Develop Statement of Work", "S. Jones", "4", "26-Jul", null,
                null, null, "x", "x",  null, null, null, null, null, null, null},
            {"1.2.4", "Evaluate proposal", "J. Dow, S. Jones", "4", "2-Aug", null,
                null, null, null, "x",  "x", null, null, null, null, null, null},
            {"1.2.5", "Select vendor", "J. Dow", "1", "6-Aug", null,
                null, null, null, null,  "x", null, null, null, null, null, null},
            null,
            {"1.3", "Research Phase", "G. Lee", "47", "9-Aug", null,
                null, null, null, null,  "x", "x", "x", "x", "x", "x", "x"},
            {"1.3.1", "Develop market research information needs questionnaire", "G. Lee", "2", "9-Aug", null,
                null, null, null, null,  "x", null, null, null, null, null, null},
            {"1.3.2", "Interview marketing group for market research needs", "G. Lee", "2", "11-Aug", null,
                null, null, null, null,  "x", "x", null, null, null, null, null},
            {"1.3.3", "Document information needs", "G. Lee, S. Jones", "1", "13-Aug", null,
                null, null, null, null,  null, "x", null, null, null, null, null},
    };

    @SuppressWarnings("deprecation")
    public void testCalendar() throws Exception {
        Workbook wb;

        wb = new HSSFWorkbook();

        Map<String, CellStyle> styles = createStyles(wb);

        Sheet sheet = wb.createSheet("Business Plan");

        //turn off gridlines
        sheet.setDisplayGridlines(false);
        sheet.setPrintGridlines(false);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);

        //the following three statements are required only for HSSF
        sheet.setAutobreaks(true);
        printSetup.setFitHeight((short)1);
        printSetup.setFitWidth((short)1);

        //the header row: centered text in 48pt font
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(12.75f);
        for (int i = 0; i < titles.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(titles[i]);
            cell.setCellStyle(styles.get("header"));
        }
        //columns for 11 weeks starting from 9-Jul
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);

        calendar.setTime(fmt.parse("09-Jul"));
        calendar.set(Calendar.YEAR, year);
        for (int i = 0; i < 11; i++) {
            Cell cell = headerRow.createCell(titles.length + i);
            cell.setCellValue(calendar);
            cell.setCellStyle(styles.get("header_date"));
            calendar.roll(Calendar.WEEK_OF_YEAR, true);
        }
        //freeze the first row
        sheet.createFreezePane(0, 1);

        Row row;
        Cell cell;
        int rownum = 1;
        for (int i = 0; i < data.length; i++, rownum++) {
            row = sheet.createRow(rownum);
            if(data[i] == null) continue;

            for (int j = 0; j < data[i].length; j++) {
                cell = row.createCell(j);
                String styleName;
                boolean isHeader = i == 0 || data[i-1] == null;
                switch(j){
                    case 0:
                        if(isHeader) {
                            styleName = "cell_b";
                            cell.setCellValue(Double.parseDouble(data[i][j]));
                        } else {
                            styleName = "cell_normal";
                            cell.setCellValue(data[i][j]);
                        }
                        break;
                    case 1:
                        if(isHeader) {
                            styleName = i == 0 ? "cell_h" : "cell_bb";
                        } else {
                            styleName = "cell_indented";
                        }
                        cell.setCellValue(data[i][j]);
                        break;
                    case 2:
                        styleName = isHeader ? "cell_b" : "cell_normal";
                        cell.setCellValue(data[i][j]);
                        break;
                    case 3:
                        styleName = isHeader ? "cell_b_centered" : "cell_normal_centered";
                        cell.setCellValue(Integer.parseInt(data[i][j]));
                        break;
                    case 4: {
                        calendar.setTime(fmt.parse(data[i][j]));
                        calendar.set(Calendar.YEAR, year);
                        cell.setCellValue(calendar);
                        styleName = isHeader ? "cell_b_date" : "cell_normal_date";
                        break;
                    }
                    case 5: {
                        int r = rownum + 1;
                        String fmla = "IF(AND(D"+r+",E"+r+"),E"+r+"+D"+r+",\"\")";
                        cell.setCellFormula(fmla);
                        styleName = isHeader ? "cell_bg" : "cell_g";
                        break;
                    }
                    default:
                        styleName = data[i][j] != null ? "cell_blue" : "cell_normal";
                }

                cell.setCellStyle(styles.get(styleName));
            }
        }

        //group rows for each phase, row numbers are 0-based
        sheet.groupRow(4, 6);
        sheet.groupRow(9, 13);
        sheet.groupRow(16, 18);

        //set column widths, the width is measured in units of 1/256th of a character width
        sheet.setColumnWidth(0, 256*6);
        sheet.setColumnWidth(1, 256*33);
        sheet.setColumnWidth(2, 256*20);
        sheet.setZoom(3, 4);


        // Write the output to a file
        avoidTempTestFolderDeletion();
        File file = new File(getTempTestFolder(), "businessplan.xls");
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();
    }

    /**
     * create a library of cell styles
     */
    private static Map<String, CellStyle> createStyles(Workbook wb){
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        DataFormat df = wb.createDataFormat();

        CellStyle style;
        Font headerFont = wb.createFont();
        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(headerFont);
        styles.put("header", style);

        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(headerFont);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("header_date", style);

        Font font1 = wb.createFont();
        font1.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setFont(font1);
        styles.put("cell_b", style);

        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setFont(font1);
        styles.put("cell_b_centered", style);

        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(font1);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_b_date", style);

        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(font1);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_g", style);

        Font font2 = wb.createFont();
        font2.setColor(IndexedColors.BLUE.getIndex());
        font2.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setFont(font2);
        styles.put("cell_bb", style);

        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setFont(font1);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_bg", style);

        Font font3 = wb.createFont();
        font3.setFontHeightInPoints((short)14);
        font3.setColor(IndexedColors.DARK_BLUE.getIndex());
        font3.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setFont(font3);
        style.setWrapText(true);
        styles.put("cell_h", style);

        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setWrapText(true);
        styles.put("cell_normal", style);

        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        styles.put("cell_normal_centered", style);

        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_RIGHT);
        style.setWrapText(true);
        style.setDataFormat(df.getFormat("d-mmm"));
        styles.put("cell_normal_date", style);

        style = createBorderedStyle(wb);
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setIndention((short)1);
        style.setWrapText(true);
        styles.put("cell_indented", style);

        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        styles.put("cell_blue", style);

        return styles;
    }

    private static CellStyle createBorderedStyle(Workbook wb){
        CellStyle style = wb.createCellStyle();
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }
    
	@SuppressWarnings("deprecation")
    @Override
	public void test() throws Exception {
	    avoidTempTestFolderDeletion();
	    Workbook wb = new HSSFWorkbook();
	    Sheet sheet = wb.createSheet("Report");
	    Row r = sheet.createRow(1);
	    r = sheet.createRow(1);
	    r = sheet.createRow(1);
	    Cell c = r.getCell(1);
	    assertNull(c);
	    
	}
	
    @SuppressWarnings("deprecation")
    public void testMergedCells() throws Exception {
        avoidTempTestFolderDeletion();
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Report");
        sheet.addMergedRegion(new CellRangeAddress(10,11,0,4));
        
        Row r9 = sheet.createRow(9);
        Row r10 = sheet.createRow(10);
        Row r11 = sheet.createRow(11);
        
        r9.createCell(0).setCellValue("9.0");
        r10.createCell(0).setCellValue("10.0");
        r11.createCell(0).setCellValue("11.0");
        r11.createCell(5).setCellValue("11.5");
        
        
        File file = new File(getTempTestFolder(), "merged.xls");
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();        

    }
    
    @SuppressWarnings("deprecation")
    public void testGrouping() throws Exception{
        avoidTempTestFolderDeletion();
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Report");
        sheet.createRow(0).createCell(0).setCellValue("i1");
        sheet.createRow(1).createCell(0).setCellValue("i1.1");
        sheet.createRow(2).createCell(0).setCellValue("i1.1.1");
        sheet.createRow(3).createCell(0).setCellValue("i1.1.2");
        sheet.createRow(4).createCell(0).setCellValue("i1.1.3");
        sheet.createRow(5).createCell(0).setCellValue("i1.2");
        sheet.createRow(6).createCell(0).setCellValue("i1.2.1");
        sheet.createRow(7).createCell(0);
        sheet.createRow(8).createCell(0).setCellValue("i2");
        sheet.createRow(9).createCell(0).setCellValue("i2.1");
        sheet.createRow(10).createCell(0).setCellValue("i2.1.1");
        sheet.createRow(11).createCell(0).setCellValue("i2.1.2");
        sheet.createRow(12).createCell(0);
        sheet.createRow(13).createCell(0).setCellValue("i3");
        sheet.createRow(14).createCell(0).setCellValue("i4");
        sheet.createRow(15).createCell(0).setCellValue("i5");

        sheet.groupRow(1, 6);
        sheet.groupRow(9, 11);
        
        CellStyle style1 = wb.createCellStyle();
        style1.setIndention((short)1);
        CellStyle style2 = wb.createCellStyle();
        style2.setIndention((short)2);
        sheet.getRow(1).getCell(0).setCellStyle(style1);
        sheet.getRow(2).getCell(0).setCellStyle(style2);
        sheet.getRow(3).getCell(0).setCellStyle(style2);
        sheet.getRow(4).getCell(0).setCellStyle(style2);
        sheet.getRow(5).getCell(0).setCellStyle(style1);
        sheet.getRow(6).getCell(0).setCellStyle(style2);
        sheet.getRow(9).getCell(0).setCellStyle(style1);
        sheet.getRow(10).getCell(0).setCellStyle(style2);
        sheet.getRow(11).getCell(0).setCellStyle(style2);
        
        File file = new File(getTempTestFolder(), "grouping.xls");
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();        
        
    }
    
    @SuppressWarnings("deprecation")
    public void test_TableToXls_indentation(){
        avoidTempTestFolderDeletion();
        TableToXls tx = new TableToXls();
        tx.setIndentedColumn(1);
        tx.addRow(0).addCell("p2900").addCell("Mixed Ordering").addCell("support/issues");
        tx.addRow(1).addCell("").addCell("Use In Mempry Order Data").addCell("unTill/technicals");
        tx.addRow(1).addCell("").addCell("Better ActiveTavle tracing").addCell("UBL/dev");
        
        tx.addRow(0).addCell("p14").addCell("TestTimeAttendance/ProcessTask (sync problem)").addCell("untill-products/unTil/X-bugs");
        tx.addRow(0).addCell("p0").addCell("Android. Article list is ugly if does not fit the screen").addCell("untill-products/unTil/bugs");
        
        tx.addRow(0).addCell("p0").addCell("Technical cleanup").addCell("untill-products/unTil/dev");
        tx.addRow(1).addCell("").addCell("Cleanup logs").addCell("unTil/dev/technicals");
        tx.addRow(2).addCell("").addCell("misc.log").addCell("");
        tx.addRow(2).addCell("").addCell("trace.log").addCell("");
        tx.addRow(2).addCell("").addCell("except.log").addCell("");
        
        tx.addRow(0);
        tx.saveToFile(new File(getTempTestFolder(), "test_TableToXls_grouping.xls"));
        
    }
    
    @SuppressWarnings("deprecation")
    public void test_TableToXls(){
        avoidTempTestFolderDeletion();
        
        TableToXls tx = new TableToXls();
        
        tx.addRow();
        tx.addCell("1.1").spanLast(2, 2);
        tx.addCell("1.3");
        tx.addRow();
        tx.addCell("2.3");
        tx.addRow();
        tx.addCell("3.1");
        tx.addCell("3.2");
        tx.addCell("3.3");
        
        tx.saveToFile(new File(getTempTestFolder(), "ttx.xls"));
        
        
        tx = new TableToXls();
        
        tx.addRow().addCell("").spanLast(1, 2).addCell("Build").addCell("Defect").addCell("Feature");;
        tx.addRow().addCell("Alex Belyansky").addCell("unTill").addCell("").addCell("").addCell("40 h");
        tx.addRow().addCell("Alex Belyansky").spanLast(1, 2).addCell("").addCell("").addCell("40 h");
        
        tx.addRow().addCell("Anton Moiseenko").spanLast(3, 1).addCell("UBL").addCell("").addCell("").addCell("40 h");
        tx.addRow().addCell("unTill").addCell("").addCell("").addCell("15 h");
        tx.addRow().addCell("untill-driver-api").addCell("").addCell("").addCell("11 h");
        tx.addRow().addCell("Anton Moiseenko").spanLast(1, 2).addCell("").addCell("").addCell("32 h");

        
        tx.saveToFile(new File(getTempTestFolder(), "Employee-Project (Pivot).xls"));


    }

}
