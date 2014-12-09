/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.soap;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import com.triniforce.soap.PropertiesSequence;
import com.triniforce.utils.ApiAlgs;

@PropertiesSequence( sequence = {"parentId", "filetype", "fields", "lookupFields", "lookupValues", "orderBy"}) 
public class SelectRequest {

    private CollectionViewRequest m_cvr = new CollectionViewRequest();
    String[] lookupFields={};
    Object[] lookupValues={};

    public CollectionViewRequest toCollectionViewRequest() {
        ApiAlgs.assertEquals(lookupValues.length, lookupFields.length);
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < lookupFields.length; i++) {
            map.put(lookupFields[i], lookupValues[i]);
        }
        m_cvr.setWhere(map);
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

    public String[] getLookupFields() {
        return lookupFields;
    }

    public void setLookupFields(String[] lookupFields) {
        this.lookupFields = lookupFields;
    }

    public Object[] getLookupValues() {
        return lookupValues;
    }

    public void setLookupValues(Object[] lookupValues) {
        this.lookupValues = lookupValues;
    }

    public Object[] getOrderBy() {
        return m_cvr.getOrderBy().toArray();
    }

    public void setOrderBy(Object[] orderBy) {
        m_cvr.setOrderBy(Arrays.asList(orderBy));
    }

}
