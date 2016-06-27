/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.soap;

import java.util.Arrays;
import java.util.Collection;

import com.triniforce.soap.PropertiesSequence;
import com.triniforce.utils.TFUtils;

@PropertiesSequence( sequence = {"args", "namedParams", "parentId", "filetype", "fields", "lookupFields", "whereExprs", "orderBy"}) 
public class SelectRequest {

    private CollectionViewRequest m_cvr = new CollectionViewRequest();
    private Object[] args={};
    private Object[] namedParams={};
    private Object[] lookupFields={};    
    private WhereExpr[] whereExprs={};
    
//    private WhereExpr[] whereExprs={};
//    private FieldFunctionRequest[] functions={};

    public CollectionViewRequest toCollectionViewRequest() {
        m_cvr.setWhere(TFUtils.arrayToMap(lookupFields));
        m_cvr.setArgs(Arrays.asList(getArgs()));
        m_cvr.setNamedParams(TFUtils.arrayToMap(getNamedParams()));
        m_cvr.setWhereExprs(Arrays.asList(getWhereExprs()));
        return m_cvr;
    }

    public Long getParentId() {
        return m_cvr.getParentId();
    }

    public void setParentId(Long parentId) {
        m_cvr.setParentId(parentId);
    }

    public String getFiletype() {
        return m_cvr.getTarget();
    }

    public void setFiletype(String type) {
        m_cvr.setTarget(type);
    }

    public String[] getFields() {
        return toStringArray(m_cvr.getColumns());
    }

    private String[] toStringArray(Collection<String> col) {
        col.toArray(new String[col.size()]);
        return null;
    }

    public void setFields(String[] fields) {
        m_cvr.setColumns(Arrays.asList(fields));
    }

    public Object[] getLookupFields() {
        return lookupFields;
    }

    public void setLookupFields(Object[] lookupFields) {
        this.lookupFields = lookupFields;
    }


    public Object[] getOrderBy() {
        return m_cvr.getOrderBy().toArray();
    }

    public void setOrderBy(Object[] orderBy) {
        m_cvr.setOrderBy(Arrays.asList(orderBy));
    }

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public WhereExpr[] getWhereExprs() {
		return whereExprs;
	}

	public void setWhereExprs(WhereExpr[] whereExprs) {
		this.whereExprs = whereExprs;
	}

	public Object[] getNamedParams() {
		return namedParams;
	}

	public void setNamedParams(Object[] namedParams) {
		this.namedParams = namedParams;
	}

}
