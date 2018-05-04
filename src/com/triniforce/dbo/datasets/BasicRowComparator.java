/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.Comparator;
import java.util.List;

import com.triniforce.server.soap.VNone;

public abstract class BasicRowComparator<T> implements Comparator<T>{
    List<Integer> m_indexColumns;

    protected T m_leftItems;
    protected T m_rightItems;
    
    
    public abstract Object getLeftItem(int idx);
    public abstract Object getRightItem(int idx);
    
    public BasicRowComparator(List<Integer> indexColumns) {
        m_indexColumns = indexColumns;
    }        
    
    Object reduceNone(Object o){
    	if(o instanceof VNone){
    		return null;
    	}
    	return o;
    }
    
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    int compareValues(Object obj0, Object obj1) {
        obj0 = reduceNone(obj0);
        obj1 = reduceNone(obj1);
    	if(null == obj0){
            if(null == obj1)
                return 0;
            else 
                return -1; 
        }
        if(null == obj1){
            return 1;
        }
        if(!(obj0 instanceof Comparable)){
            return 0;
        }
        Comparable v1 = (Comparable) obj0;
        if(!(obj1 instanceof Comparable)){
            return 0;
        }
        Comparable v2 = (Comparable) obj1;
        return v1.compareTo(v2);
    }     
    int compareColumn(Integer colIndex) {
        int res;
        if(colIndex < 0){
            int index = -colIndex-1;
            res = compareValues(getRightItem(index), getLeftItem(index));
        }
        else
            res = compareValues(getLeftItem(colIndex), getRightItem(colIndex));
        return res;
    }
    public int compare(T arg0, T arg1) {
        m_leftItems = arg0;
        m_rightItems = arg1;
        int res = 0;
        for (Integer colIndex : m_indexColumns) {
            res = compareColumn(colIndex);
            if(res != 0)
                break;
        }
        return res;
    }        
}

