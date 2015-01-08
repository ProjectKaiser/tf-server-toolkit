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
import org.apache.poi.ss.usermodel.CellStyle;
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
    
    int m_lastTopLevel = -1;
    int m_lastTopLevelRow = -1;
    int m_prevLevel = -1;
    int m_ident = 0;
    
    private int m_indentedColumn = -1;
    
    CellStyle m_indentStyles[]; 
    
    public TableToXls(){
        m_sheet = m_wb.createSheet("Report");
        PrintSetup printSetup = m_sheet.getPrintSetup();
        printSetup.setLandscape(true);
    }
    
    public TableToXls addRow(int indentLevel){
        addRow();
        boolean top = true;
        if(m_prevLevel>=0){
            if(m_lastTopLevel >= indentLevel){
                //top-level reached
                if( m_rowNum - m_lastTopLevelRow > 1){ 
                    m_sheet.groupRow(m_lastTopLevelRow + 1, m_rowNum - 1);
                    addRow();
                }
            }else if(m_lastTopLevel < indentLevel){
                m_ident = indentLevel - m_lastTopLevel;
                top = false;
            }
        }
        m_prevLevel = indentLevel;
        if(top){
            m_ident = 0;
            m_lastTopLevel = indentLevel;
            m_lastTopLevelRow = m_rowNum;            
        }
        return this;
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
    
    public TableToXls spanLast(int rowspan, int colspan){
        int spanCol = m_colNum - 1;
        m_sheet.addMergedRegion(new CellRangeAddress(m_rowNum, m_rowNum
                + rowspan - 1, spanCol, spanCol + colspan - 1));
        for(int row = m_rowNum; row < m_rowNum + rowspan; row++){
            Row r = gc_row(row);
            for (int col = spanCol; col < spanCol + colspan; col++) {
                //skip first cell
                if(row == m_rowNum && col == spanCol){
                    continue;
                }
                r.createCell(col);
            }
        }
        m_colNum += colspan - 1;
        return this;
    }
    
    
    public TableToXls addCell(String data) {
        // skip filled columns (as a result of span)
        {
            Row r = gc_row(m_rowNum);
            while (null != r.getCell(m_colNum)) {
                m_colNum++;
            }
        }
        Row r = gc_row(m_rowNum);
        Cell c = r.createCell(m_colNum);
        c.setCellValue(data);
        if (m_colNum == m_indentedColumn && m_ident > 0
                && m_ident < m_indentStyles.length) {
            c.setCellStyle(m_indentStyles[m_ident]);

        }
        m_colNum++;
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
    
    public void saveToStream(FileOutputStream out){
        try {
            m_wb.write(out);
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }


    public int getIndentedColumn() {
        return m_indentedColumn;
    }


    public void setIndentedColumn(int indentedColumn) {
        m_indentedColumn = indentedColumn;
        m_indentStyles = new CellStyle[9];
        short indent = 0;
        for(int i = 0; i < m_indentStyles.length ; i++){
            m_indentStyles[i] = m_wb.createCellStyle();
            m_indentStyles[i].setIndention(indent++);
        }
    }


}
