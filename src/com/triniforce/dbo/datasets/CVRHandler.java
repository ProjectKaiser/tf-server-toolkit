/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.triniforce.db.dml.IResSet;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.IPKRootExtensionPoint;
import com.triniforce.server.plugins.kernel.ep.view.PKEPFieldFunctions;
import com.triniforce.server.soap.CollectionViewRequest;
import com.triniforce.server.soap.FieldFunctionRequest;
import com.triniforce.server.soap.WhereExpr;
import com.triniforce.server.soap.WhereExpr.ColumnExpr;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiStack;

public class CVRHandler implements ICVRHandler {
	

	public IResSet filter(IResSet rs, List<WhereExpr> where,
			List<FieldFunctionRequest> ffs, List<FieldFunction> ff) {
		PipeResSet res = new PipeResSet(rs);
		for(String column : requestedColumns(where)){
			addFFifNeeded(res, column, ffs, ff);
		}
		setWhereExprs(res, where);
		return res;
	}
	
	private void addFFifNeeded(PipeResSet res, String column, List<FieldFunctionRequest> ffs, List<FieldFunction> ff) {
		if(!res.getColumns().contains(column)){
			//Request FieldFunction
			int iFfr = getFieldFunctionRequest(ffs, column);
			FieldFunctionRequest ffr = ffs.get(iFfr);
			res.addFieldFunction(ffr.getResultName(), ffr.getFieldName(), ff.get(iFfr));
		}

	}

	Collection<String> requestedColumns(List<WhereExpr> exprs){
		HashSet<String> res = new HashSet<String>();
		for (WhereExpr expr : exprs) {
			if(expr instanceof ColumnExpr)
				res.add(((ColumnExpr)expr).getColumnName());
		}
		return res;
	}
	
	private void setWhereExprs(PipeResSet res, List<WhereExpr> where) {
		res.addFilter(where);
	}

	private int getFieldFunctionRequest(List<FieldFunctionRequest> ffReqs,
			String key) {
		int i = 0;
		for (Iterator iterator = ffReqs.iterator(); iterator.hasNext();) {
			FieldFunctionRequest fieldFunctionRequest = (FieldFunctionRequest) iterator
					.next();
			if(fieldFunctionRequest.getResultName().equals(key))
				return i;
			i++;
		}
		return -1;
	}

	public IResSet filterAndSend(IResSet rs, List<WhereExpr> where,
			List<FieldFunctionRequest> ffs, List<FieldFunction> ff, List<String> fields, int from, int to) {
		PipeResSet fltRs = new PipeResSet(rs);
		Iterator<FieldFunction> iff = ff.iterator();
		for(FieldFunctionRequest req : ffs){
			fltRs.addFieldFunction(req.getResultName(), req.getFieldName(), iff.next());
		}
		setWhereExprs(fltRs, where);

		TruncResSet result = new TruncResSet(fltRs);
		for (String column : fields) {
			result.addColumn(column);
		}
		result.setFromBorder(from);
		result.setToBorder(to);
		return result;
	}

	public IResSet order(IResSet rs, List<Object> order,
			List<FieldFunctionRequest> ffs, List<FieldFunction> ff) {
		PipeResSet pipe = new PipeResSet(rs);
		
		for(String orderColumn : orderColumns(order)){
			addFFifNeeded(pipe, orderColumn, ffs, ff);
		}
		
		List<String> columns = pipe.getColumns();
		SortResSet result = new SortResSet(columns);
		while(pipe.next()){
			Object[] vals = new Object[columns.size()];
			for(int i =0; i<columns.size(); i++)
				vals[i] = pipe.getObject(i+1);
			result.addRow(vals);
		}
		result.sort(order);
		return result;
	}

	private List<String> orderColumns(final List<Object> order) {
		return new AbstractList<String>() {
			@Override
			public String get(int index) {
				Object obj = order.get(index);
				return obj instanceof CollectionViewRequest.DescField ? ((CollectionViewRequest.DescField)obj).getField() : (String)obj;
			}

			@Override
			public int size() {
				return order.size();
			}
		};
	}

	public IResSet send(IResSet rs, List<String> fields,
			List<FieldFunctionRequest> ffs, List<FieldFunction> ff, int from, int to) {
		List<String> rsColumns = rs.getColumns();
		ArrayList<String> ffColumns = new ArrayList<String>(fields);
		ffColumns.removeAll(rsColumns);
		if(!ffColumns.isEmpty()){
			PipeResSet pipe = new PipeResSet(rs);
			for(String ffColumn : ffColumns){
				int iReq = getFieldFunctionRequest(ffs, ffColumn);
				FieldFunctionRequest ffReq = ffs.get(iReq);
				pipe.addFieldFunction(ffReq.getResultName(), ffReq.getFieldName(), ff.get(iReq));
			}
			rs = pipe;
		}
		
		TruncResSet res = new TruncResSet(rs);
		for (String column : fields) {
			res.addColumn(column);
		}
		res.setFromBorder(from);
		res.setToBorder(to);
		return res;
	}
	
	public IResSet load(CollectionViewRequest req, RSFlags rsFlags){
		return null;
	}
	
	public IResSet process(IResSet rs, RSFlags flags, CollectionViewRequest req, List<FieldFunction> ffs){
		List<String> resColumns = new ArrayList<String>(req.getColumns());
		for (FieldFunctionRequest ffReq : req.getFunctions()) {
			resColumns.add(ffReq.getResultName());	
		}
		
		List<WhereExpr> where = (List<WhereExpr>) getWhereExprs(req);
		if(isFilterNeeded(flags, req.getWhere())){
			rs = filter(rs, where, req.getFunctions(), ffs);
		}
		if(isOrderNeeded(flags, req.getOrderBy())){
			rs = order(rs, req.getOrderBy(), req.getFunctions(), ffs);
		}
		if(isTruncNeeded(rs.getColumns(), resColumns)){
			rs = send(rs, resColumns, req.getFunctions(), ffs, req.getStartFrom(), req.getStartFrom() + req.getLimit());
		}
		return rs;
	}

	private List<WhereExpr> getWhereExprs(CollectionViewRequest req) {
		ArrayList<WhereExpr> res = new ArrayList<WhereExpr>(req.getWhereExprs());
		for(Map.Entry<String, Object> entry : req.getWhere().entrySet()){
			res.add(new WhereExpr.ExprEquals(entry.getKey(), entry.getValue()));
		}
		return res;
	}

	private boolean isFilterNeeded(RSFlags flags, Map<String, Object> where) {
		return !(flags.m_bFilter | where.isEmpty());
	}

	private boolean isTruncNeeded(List<String> rsColumns, List<String> reqColumns) {
		return !rsColumns.equals(reqColumns);
	}

	private boolean isOrderNeeded(RSFlags flags, List<Object> orderBy) {
		return !(flags.m_bSort | orderBy.isEmpty());
	}

	public List<FieldFunction> initFieldFunctions(CollectionViewRequest req) {
		ArrayList<FieldFunction> res = new ArrayList<FieldFunction>();
		for(FieldFunctionRequest ffReq : req.getFunctions())
			res.add(initFieldFunction(req, ffReq));
		return res;
	}

	public FieldFunction initFieldFunction(CollectionViewRequest req,
			FieldFunctionRequest ffReq) {
		IPKRootExtensionPoint root = ApiStack.getInterface(IBasicServer.class);
		FieldFunction ff = root.getExtensionPoint(PKEPFieldFunctions.class).getExtension(ffReq.getFunctionName()).getInstance();
		return ff;
	}
		
	public List<String> checkRequestMetadata(DSMetadata meta, CollectionViewRequest req){
		List<String> requestedColumns = new ArrayList<String>();
		
		for(String column : req.getColumns()){
			if(!meta.m_columns.contains(column)){
				throw new EDSException.ECVRColumnException.EWrongColumnName(column);
			}
			if(requestedColumns.contains(column)){
				throw new EDSException.ECVRColumnException.EColumnRequestedTwice(req.getColumns().get(0));				
			}
			requestedColumns.add(column);
		}
		
		
		for(FieldFunctionRequest ffReq : req.getFunctions()){
			String column = ffReq.getFieldName();
			if(!meta.m_columns.contains(column))
				throw new EDSException.ECVRColumnException.EWrongFieldFunctionName(column);
			if(!requestedColumns.contains(column))
				requestedColumns.add(column);
		}

		for(String column : req.getWhere().keySet()){
			if(!meta.m_columns.contains(column))
				throw new EDSException.ECVRColumnException.EWrongNameInWhereClause(column);
			if(!requestedColumns.contains(column))
				requestedColumns.add(column);
		}
		for(WhereExpr expr: req.getWhereExprs()){
			if(expr instanceof ColumnExpr){
				String column = ((ColumnExpr)expr).getColumnName();
				if(!meta.m_columns.contains(column))
					throw new EDSException.ECVRColumnException.EWrongNameInWhereClause(column);
				if(!requestedColumns.contains(column))
					requestedColumns.add(column);
			}
		}
		for(Object orderBy: req.getOrderBy()){
			String column;
			if(orderBy instanceof CollectionViewRequest.DescField){
				column = ((CollectionViewRequest.DescField) orderBy).getField();
			}
			else
				column = (String) orderBy;
			if(!meta.m_columns.contains(column))
				throw new EDSException.ECVRColumnException.EWrongNameInOrderClause(column);
			if(!requestedColumns.contains(column))
				requestedColumns.add(column);
		}
		return requestedColumns;
		
	}
	
	
	public IResSet handleRequest(CollectionViewRequest req){
		IPKRootExtensionPoint root = ApiStack.getInterface(IBasicServer.class);
		Collection<IPKExtension> providerExtensions = root.getExtensionPoint(PKEPDatasetProviders.class).getExtensions().values();
		DSMetadata md = null;
		for (IPKExtension ipkExtension : providerExtensions) {
			PKEPDatasetProvider provider = ipkExtension.getInstance();
			md = provider.queryTarget(req.getTarget());
			if(null != md)
				break;
		}
		
		if(null == md)
			throw new EDSException.EDSProviderNotFound(req.getTarget());
		
		List<String> providerColumns = checkRequestMetadata(md, req);
		md.check();
		
		List<FieldFunction> ffs = initFieldFunctions(req);
		IResSet rs = md.load(providerColumns, req, ffs);
		
		
		RSFlags rsFlags = new RSFlags(md.m_flags);
		return process(rs, rsFlags, req, ffs);
	}
	

}
