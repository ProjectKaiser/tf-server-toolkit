/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.List;


public class ListRowComparator extends BasicRowComparator<List<Object>>{
    
    public ListRowComparator(List<Integer> indexColumns) {
        super(indexColumns);
    }

    @Override
    public Object getLeftItem(int idx) {
        return m_leftItems.get(idx);
    }

    @Override
    public Object getRightItem(int idx) {
        return m_rightItems.get(idx);
    }

}
