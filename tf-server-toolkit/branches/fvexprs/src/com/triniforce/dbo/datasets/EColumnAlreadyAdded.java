/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

public class EColumnAlreadyAdded extends RuntimeException {
	private static final long serialVersionUID = -8136815977138247000L;

	public EColumnAlreadyAdded(String column) {
		super(column);
	}
}
