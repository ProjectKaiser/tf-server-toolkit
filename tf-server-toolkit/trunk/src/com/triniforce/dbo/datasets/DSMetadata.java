/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.List;

import com.triniforce.db.dml.IResSet;
import com.triniforce.server.soap.CollectionViewRequest;

public class DSMetadata {
	public static long CAN_CALC_FF = 0x1;
	public static long CAN_SORT = 0x2;
	public static long CAN_FILTER = 0x4;
	
	List<String> m_columns;
	long m_flags;
	
	public DSMetadata(long flags, List<String> cols) {
		m_flags = flags;
		m_columns = cols;
	}
	
	public void check(){
		
	}
	
	IResSet load(List<String>reqColumns, CollectionViewRequest req, List<FieldFunction>ffs){
		return null;
	}
	
}
