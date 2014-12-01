/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils.xls;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.triniforce.utils.ApiAlgs;

public class TableToXls {
    
    Workbook m_wb = new HSSFWorkbook();
    Sheet m_sheet;
    int m_rowNum = -1;
    int m_colNum = -1;
    
    
    public TableToXls() {
        m_sheet = m_wb.createSheet("Report");
        PrintSetup printSetup = m_sheet.getPrintSetup();
        printSetup.setLandscape(true);
    }
    
    public TableToXls addRow(){
        m_rowNum++;
        m_colNum = 0;
        return this;
    }
    
    /**
     * @param rowNum
     * @return get or create row
     */
    Row gc_row(int rowNum){
        Row r = m_sheet.getRow(rowNum);
        if(null == r){
            r = m_sheet.createRow(rowNum);
        }
        return r;
    }
    
    public TableToXls addCell(String data){
        addCell(data, 1, 1);
        return this;
    }
    
    public TableToXls addCell(String data, int rowspan, int colspan){
        //skip filled rows
        {
            Row r = gc_row(m_rowNum);
            while(null != r.getCell(m_colNum)){
                m_colNum++;
            }
        }
        if(colspan > 1 || rowspan > 1){
            m_sheet.addMergedRegion(new CellRangeAddress(m_rowNum, m_rowNum
                    + rowspan - 1, m_colNum, m_colNum + colspan - 1));
        }

        for(int row = m_rowNum; row < m_rowNum + rowspan; row++){
            Row r = gc_row(row);
            for (int col = m_colNum; col < m_colNum + colspan; col++) {
                Cell c = r.createCell(col);
                c.setCellValue(data);
            }
        }
        m_colNum += colspan;
        return this;
    }
    
    public void saveToFile(File f){
        try {
            FileOutputStream out = new FileOutputStream(f);
            m_wb.write(out);
            out.close();
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }

}
