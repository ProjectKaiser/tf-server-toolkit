/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils.xls;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.triniforce.db.test.TFTestCase;

public class TableToXlsTest extends TFTestCase{
    
    public void testEngine_rowsVisibility(){
        
        /*
         * Concluson - createRow() overrides previous row
         * 
         */
        
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Report");
        Row r1 = sheet.createRow(1);
        Cell c1 = r1.createCell(0);
        c1.setCellValue("1");
        assertNotNull(r1.getCell(0));
        
        Row r2 = sheet.getRow(1);
        assertNotNull(r2.getCell(0));
        
        //r12 is created after c added ro r1
        
        Row r13 = sheet.createRow(1);
        assertNull(r13.getCell(0));
        
        assertSame(r13, sheet.getRow(1));
        
        //empty cell
        
        r13.createCell(2);
        assertNotNull(r13.getCell(2));
        assertEquals("", r13.getCell(2).getStringCellValue());
        
        
    }
    
    public void testEngine_merge(){
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Report");
        Row r = sheet.createRow(1);
        
        //can add row few times 
        r = sheet.createRow(1);
        r = sheet.createRow(1);
        
        //can get non-existing row
        Cell c = r.getCell(100);
        assertNull(c);
        
        //add merged region
        sheet.addMergedRegion(new CellRangeAddress(10,14,0,4));
        
        sheet.createRow(10).createCell(0).setCellValue("10.0");
        
        
        //not existing cell in merged region
        assertNull(sheet.getRow(12));
        
        //existing cell in merged region
        sheet.createRow(12).createCell(1).setCellValue("12.1");
        assertEquals("12.1",sheet.getRow(12).getCell(1).getStringCellValue());
        assertEquals("10.0",sheet.getRow(10).getCell(0).getStringCellValue());
        
    }
    
    @Override
    public void test() throws Exception {
        TableToXls tx = new TableToXls();
        
        tx.addRow();
        tx.addCell("1.1", 1, 1);
        tx.addCell("1.2", 1, 1);
        tx.addCell("1.3", 1, 1);
        tx.addRow();
        tx.addCell("2.1", 1, 1);
        tx.addCell("2.2", 1, 1);
        tx.addCell("2.3", 1, 1);
        
    }
    
    

}
