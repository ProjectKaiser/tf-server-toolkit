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
import java.util.Set;

import com.triniforce.db.dml.IResSet;
import com.triniforce.extensions.IPKExtension;
import com.triniforce.extensions.IPKRootExtensionPoint;
import com.triniforce.server.plugins.kernel.ep.view.PKEPFieldFunctions;
import com.triniforce.server.soap.CollectionViewRequest;
import com.triniforce.server.soap.FieldFunctionRequest;
import com.triniforce.server.soap.LongListResponse;
import com.triniforce.server.soap.WhereExpr;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.TFUtils;

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
			ApiAlgs.assertTrue(iFfr>=0, column);
			FieldFunctionRequest ffr = ffs.get(iFfr);
			res.addFieldFunction(getFFResName(ffr), ffr.getFieldName(), ff.get(iFfr));
		}
	}

	Collection<String> requestedColumns(List<WhereExpr> exprs){
		HashSet<String> res = new HashSet<String>();
		for (WhereExpr expr : exprs) {
		    res.addAll(expr.calcColumnNames());
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
			String ffResName = getFFResName(fieldFunctionRequest);
			if(key.equals(ffResName))
				return i;
			i++;
		}
		return -1;
	}

	private String getFFResName(FieldFunctionRequest fieldFunctionRequest) {
		String res = fieldFunctionRequest.getResultName();
		if(null == res)
			res = String.format("%s(%s)", fieldFunctionRequest.getFunctionName(), fieldFunctionRequest.getFieldName());
		return res;
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
				ApiAlgs.assertTrue(iReq>=0, ffColumn);
				FieldFunctionRequest ffReq = ffs.get(iReq);
				pipe.addFieldFunction(getFFResName(ffReq), ffReq.getFieldName(), ff.get(iReq));
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
		List<String> ffResColumns = new ArrayList<String>();
		for (FieldFunctionRequest ffReq : req.getFunctions()) {
			ffResColumns.add(getFFResName(ffReq));
		}
		resColumns.addAll(ffResColumns);
		
		
		List<WhereExpr> where = (List<WhereExpr>) getWhereExprs(req);
		if(isFilterNeeded(flags, where, req.getWhere().keySet(), ffResColumns)){
			rs = filter(rs, where, req.getFunctions(), ffs);
		}
		if(isOrderNeeded(flags, req.getOrderBy(), ffResColumns)){
			rs = order(rs, req.getOrderBy(), req.getFunctions(), ffs);
		}
		
		if(isAfterOrderFilterNeeded(flags, req)){
			rs = filter(rs, req.getAfterOrderWhereExprs(), req.getFunctions(), ffs);
		}
		
		if(isTruncNeeded(rs.getColumns(), resColumns, req)){
			int to = req.getLimit() == 0 ? 0 : req.getStartFrom() + req.getLimit();
			rs = send(rs, resColumns, req.getFunctions(), ffs, req.getStartFrom(), to);
		}
		return rs;
	}

	private boolean isAfterOrderFilterNeeded(RSFlags flags,
			CollectionViewRequest req) {
		return !(flags.m_bFilter || req.getAfterOrderWhereExprs() == null || req.getAfterOrderWhereExprs().isEmpty());
	}

	private List<WhereExpr> getWhereExprs(CollectionViewRequest req) {
		ArrayList<WhereExpr> res = new ArrayList<WhereExpr>(req.getWhereExprs());
		for(Map.Entry<String, Object> entry : req.getWhere().entrySet()){
			if("is_active".equals(entry.getKey())  && TFUtils.equals(2, entry.getValue())){
				//LATER: is_active = 2 is handled internalle by SOME some datasets
				//Here we ignore it as a work around
			}else{
				res.add(new WhereExpr.ExprEquals(entry.getKey(), entry.getValue()));
			}
		}
		return res;
	}

	private boolean isFilterNeeded(RSFlags flags, List<WhereExpr> where, Set<String> whereKeys, List<String> ffResColumns) {
		return (!(flags.m_bFilter || where.isEmpty())) || intersects(whereKeys, ffResColumns);
		
	}

	private boolean isTruncNeeded(List<String> rsColumns, List<String> reqColumns, CollectionViewRequest req) {
		return (!rsColumns.equals(reqColumns)) || (0!=req.getStartFrom() || 0!=req.getLimit());
	}

	private boolean isOrderNeeded(RSFlags flags, List<Object> orderBy, List<String> ffResColumns) {
		return (!(flags.m_bSort | orderBy.isEmpty())) || intersects(orderColumns(orderBy), ffResColumns);
	}

	private boolean intersects(Collection<String> list1, Collection<String> list2) {
		for (String l2 : list2) {
			if(list1.contains(l2))
				return true;
		}
		return false;
	}

	public List<FieldFunction> initFieldFunctions(CollectionViewRequest req) {
		ArrayList<FieldFunction> res = new ArrayList<FieldFunction>();
		for(FieldFunctionRequest ffReq : req.getFunctions())
			res.add(initFieldFunction(req, ffReq));
		return res;
	}

	public FieldFunction initFieldFunction(CollectionViewRequest req,
			final FieldFunctionRequest ffReq) {
		final String target = req.getTarget();
		final String field = ffReq.getFieldName();
		final Long parentId = req.getParentId();
		IPKRootExtensionPoint root = ApiStack.getInterface(IBasicServer.class);
		FieldFunction ff = root.getExtensionPoint(PKEPFieldFunctions.class).getExtension(ffReq.getFunctionName()).getInstance();
		ff.init(new FieldFunction.IFieldFunctionCtx() {
			public String getTarget() {
				return target;
			}
			public String getField() {
				return field;
			}
			@Override
			public Long getParentId() {
				return parentId;
			}
			@Override
			public Map<String, Object> getParams() {
				return ffReq.getParams();
			}
		});
		return ff;
	}
		
	public List<String> checkRequestMetadata(DSMetadata meta, CollectionViewRequest req){
		List<String> requestedColumns = new ArrayList<String>();
		
		for(String column : req.getColumns()){
			if(!meta.getColumns().contains(column)){
				throw new EDSException.ECVRColumnException.EColumnNotFound.EWrongColumnName(column);
			}
			if(requestedColumns.contains(column)){
				throw new EDSException.ECVRColumnException.EColumnRequestedTwice(column);				
			}
			requestedColumns.add(column);
		}
		
		List<String> ffs = new ArrayList<String>();
		
		for(FieldFunctionRequest ffReq : req.getFunctions()){
			String column = ffReq.getFieldName();
			if(!meta.getColumns().contains(column))
				throw new EDSException.ECVRColumnException.EColumnNotFound.EWrongFieldFunctionName(column);
			if(!requestedColumns.contains(column))
				requestedColumns.add(column);
			ffs.add(ffReq.getResultName());
		}
		

		HashSet<String> whereColumns = new HashSet<String>(req.getWhere().keySet());
		whereColumns.addAll(requestedColumns(req.getWhereExprs()));

		for(String column : whereColumns){
			if(!(meta.getColumns().contains(column))){
				if(!ffs.contains(column))
					throw new EDSException.ECVRColumnException.EColumnNotFound.EWrongNameInWhereClause(column);
			}
			else{
				if(!requestedColumns.contains(column))
					requestedColumns.add(column);
			}
		}
		
		for(String column: orderColumns(req.getOrderBy())){
			if(!meta.getColumns().contains(column)){
				if(!ffs.contains(column))
					throw new EDSException.ECVRColumnException.EColumnNotFound.EWrongNameInOrderClause(column);
			}
			else{
				if(!requestedColumns.contains(column))
					requestedColumns.add(column);
			}
		}
		if(req.getStartFrom() < 0)
			throw new IllegalArgumentException("startFrom");
		if(req.getLimit() < 0)
			throw new IllegalArgumentException("limit");
		return requestedColumns;
		
	}
	
	
	public LongListResponse processAsLLR(CollectionViewRequest req){
		DSMetadata md = findProvider(req);
		
		if(null == md)
			throw new EDSException.EDSProviderNotFound(req.getTarget());
		
		try{
			List<String> providerColumns = checkRequestMetadata(md, req);
			IResSet resSet;
			
			if(!md.check(req)){
				resSet = new SortResSet(providerColumns); // empty RS
			}
			else{
				List<FieldFunction> ffs = initFieldFunctions(req);
				IResSet rs = md.load(providerColumns, req, ffs);
				RSFlags rsFlags = new RSFlags(md.getFlags());
				resSet = process(rs, rsFlags, req, ffs);
			}
			LongListResponse llr = resSet2LLR(resSet);
			md.setLLRAttrs(llr);
			return llr;
		}finally{
			md.close();
		}
	}

	private LongListResponse resSet2LLR(IResSet resSet) {
		List<String> cols = resSet.getColumns();
		LongListResponse res = new LongListResponse(cols.toArray(new String[cols.size()]));
		ArrayList<Object> row = new ArrayList<Object>(cols.size());
		while(resSet.next()){
			for (int i = 0; i < cols.size(); i++) {
				row.add(resSet.getObject(i+1));
			}
			res.addRow(row);
			row.clear();
		}
		return res;
	}

	public DSMetadata findProvider(CollectionViewRequest req) {
		DSMetadata md = null;
		IPKRootExtensionPoint root = ApiStack.getInterface(IBasicServer.class);
		Collection<IPKExtension> providerExtensions = root.getExtensionPoint(PKEPDatasetProviders.class).getExtensions().values();
		if(null == req.getParentOf()){
			TFUtils.assertNotNull(req.getTarget(), "CollectionViewRequest.target");
		}
		for (IPKExtension ipkExtension : providerExtensions) {
			PKEPDatasetProvider provider = ipkExtension.getInstance();
			if(null == req.getTarget()){
				if( provider instanceof IQueryTargetById){
					md = ((IQueryTargetById)provider).queryTargetById(req.getParentOf());
				}
			}else{
				md = provider.queryTarget(req.getTarget());
			}
			if(null != md)
				break;
		}
		return md;
	}
	

}
