/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.List;

import com.triniforce.db.dml.IResSet;
import com.triniforce.server.soap.CollectionViewRequest;
import com.triniforce.server.soap.FieldFunctionRequest;
import com.triniforce.server.soap.LongListResponse;
import com.triniforce.server.soap.WhereExpr;

public interface ICVRHandler {
	
	public static class RSFlags{
		boolean m_bFFCalc;
		boolean m_bFilter;
		boolean m_bSort;
		public RSFlags(long flags) {
			m_bFFCalc = (flags & DSMetadata.CAN_CALC_FF) == DSMetadata.CAN_CALC_FF;
			m_bFilter= (flags & DSMetadata.CAN_FILTER) == DSMetadata.CAN_FILTER;
			m_bSort= (flags & DSMetadata.CAN_SORT) == DSMetadata.CAN_SORT;
		}
	}
	
	/**
	 * Load initial dataset, which can be sorted/ordered/truncated by Fields/FieldFunctions or not
	 * Function search needed DatasetProvider by CVR.target property. And fetch data from provider by request  
	 * RSFlags show status of this state. 
	 * @param req - Request 
	 * @param rsFlags - Dataset flags. This flags are set by data provider
	 * @return - DataSet pointer
	 */
//	IResSet load(CollectionViewRequest req, RSFlags rsFlags);
	
	public LongListResponse processAsLLR(CollectionViewRequest req);
	
	public List<String> checkRequestMetadata(DSMetadata meta, CollectionViewRequest req);
	
	/**
	 * Function process initial dataset. Function call filter/order/send function if needed
	 * @param rs - initial IResSet
	 * @param flags - flags of IResSet received from provider with load function
	 * @param req - Request
	 * @return - Ordered/Filetered data
	 */
	IResSet process(IResSet rs, RSFlags flags, CollectionViewRequest req, List<FieldFunction> ffs);
	
	/**
	 * Filter IResSet with request parameters
	 * @return Filtered data
	 */
	IResSet filter(IResSet rs, List<WhereExpr> where, List<FieldFunctionRequest> ffs, List<FieldFunction> ff);
	
	/**
	 * Filter and send data. Function called when order data is not needed
	 * 
	 */
	IResSet filterAndSend(IResSet rs, List<WhereExpr> where, List<FieldFunctionRequest> ffs, List<FieldFunction> ff, List<String> fields, int from, int to);
	
	/**
	 * Order data 
	 * @return
	 */
	IResSet order(IResSet rs, List<Object> order, List<FieldFunctionRequest> ffs, List<FieldFunction> ff);
	
	/**
	 * Send data as result. Function return only requested columns of dataset and only row from range [from, to) 
	 * @return
	 */
	IResSet send(IResSet rs, List<String> fields, List<FieldFunctionRequest> ffs, List<FieldFunction> ff, int from, int to);
	
	FieldFunction initFieldFunction(CollectionViewRequest req, FieldFunctionRequest ffReq);
}
