/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.mds;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.dml.ResSet;
import com.triniforce.utils.ApiAlgs;

public class MDSDb {
    
    public static MDS createFromResSet(ResSet resSet) {
        
    	if (resSet == null) return null;
        
        MDS mds = new MDS();
        
        ResultSetMetaData md;
        try {
        	
        	md = resSet.getResultSet().getMetaData();
			int columnCount = md.getColumnCount();
    		
			List<String> names = new ArrayList<String>();
			
    		for (int i = 1; i <= columnCount; i++) {
				names.add(md.getColumnName(i));
			}
    		mds.appendNames(names);
    		
    		while(resSet.next()){
            	IMDSRow row = new MDSRow();
    			for(int i = 1; i <= columnCount; i++){
            		row.add(resSet.getObject(i));
            	}
    			mds.appendRow(row);
            }
    	} catch (Exception e) {
			ApiAlgs.rethrowException(e);
		}
               
        return mds;
    }

}
