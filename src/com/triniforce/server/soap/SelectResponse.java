package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.soap.PropertiesSequence;

// FIXME: json answer does not follow this sequence
@PropertiesSequence( sequence = {"columns", "rows"})
public class SelectResponse {
    private List<String> columns;
    private List<List<Object>> rows;
    
    public SelectResponse(List<String> columns, List<Object> values){
    	this.columns = columns;
		this.rows = new ArrayList<List<Object>>();
    	int colc = columns.size();
    	int idx = 0;
    	final int valc = values.size();
    	while(idx < valc){
    		List row = new ArrayList<Object>();
    		for(int col = 0; col < colc && idx < valc; col++){
    			row.add(values.get(idx++));
    		}
    		rows.add(row);
    	}

    }
    
    List<Object> row(int idx){
    	return rows.get(idx);
    }
    
    List<Object> firstRow(){
    	return rows.get(0);
    }
    
    Object firstRowValue(int idx){
    	return rows.get(0).get(idx);
    }

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public List<List<Object>> getRows() {
		return rows;
	}

	public void setRows(List<List<Object>> rows) {
		this.rows = rows;
	}

    
}
