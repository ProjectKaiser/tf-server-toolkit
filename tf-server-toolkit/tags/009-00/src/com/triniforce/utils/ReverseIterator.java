/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReverseIterator<T> implements Iterable<T>{

    private final ListIterator m_listIterator;
    
    public ReverseIterator(List<T> list) {
        m_listIterator = list.listIterator(list.size());
    }
    
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public Iterator<T> iterator() {
        return new Iterator(){

            public boolean hasNext() {
                return m_listIterator.hasPrevious();
            }

            public Object next() {
                return m_listIterator.previous();
            }

            public void remove() {
                m_listIterator.remove();
            }
        };
    }
}
