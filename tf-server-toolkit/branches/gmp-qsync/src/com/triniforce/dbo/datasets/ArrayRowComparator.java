/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.List;

public class ArrayRowComparator extends BasicRowComparator<Object[]>{
    public ArrayRowComparator(List<Integer> indexColumns) {
        super(indexColumns);
    }

    @Override
    public Object getLeftItem(int idx) {
        return m_leftItems[idx];
    }

    @Override
    public Object getRightItem(int idx) {
        return m_rightItems[idx];
    }
}
