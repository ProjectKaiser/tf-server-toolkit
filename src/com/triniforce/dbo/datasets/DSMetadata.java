/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.List;

import com.triniforce.db.dml.IResSet;
import com.triniforce.server.soap.CollectionViewRequest;
import com.triniforce.server.soap.LongListResponse;

public class DSMetadata {
	public static long CAN_CALC_FF = 0x1;
	public static long CAN_SORT = 0x2;
	public static long CAN_FILTER = 0x4;
	
	// Must be filled in only when returned from queryTargetById() 
	private String m_target;
	
	private List<String> m_columns;
	private long m_flags;
	
	public DSMetadata(long flags, List<String> cols, String target) {
		m_flags = flags;
		m_columns = cols;
		m_target = target;
	}
	
	public DSMetadata(long flags, List<String> cols) {
		this(flags, cols, null);
	}
	
	public boolean check(CollectionViewRequest req){
		return false;
	}
	
	public IResSet load(List<String>reqColumns, CollectionViewRequest req, List<FieldFunction> ffs){
		return null;
	}
	
	public void close(){
		
	}

	public List<String> getColumns() {
		return m_columns;
	}

	public long getFlags() {
		return m_flags;
	}
	
	public void setLLRAttrs(LongListResponse res){}

	public String getTarget() {
		return m_target;
	}
	
}
