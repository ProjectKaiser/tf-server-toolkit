/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListSplitter implements Iterable<List>{

    private final Iterable m_src;
    private final int m_size;

    public ListSplitter(Iterable src, int size){
    	TFUtils.assertTrue(size>0, "Size should be more than zero: " + size);
        m_src = src;
        m_size = size;

    }
    
    class Iter implements Iterator{
        private Iterator m_iterator;
        List m_cur;
        public Iter(){
            m_iterator = m_src != null? m_src.iterator(): new ArrayList().iterator();
            m_cur = new ArrayList<Object>(m_size);
        }

        public boolean hasNext(){
            return m_iterator.hasNext();
        }

        public Object next() {
            while(m_iterator.hasNext() && m_cur.size() < m_size){
                m_cur.add(m_iterator.next());
            }
            List res = m_cur;
            m_cur = new ArrayList<Object>(m_size);
            return res;
        }

        public void remove() {
        }
        
    }
    
    public Iterator<List> iterator() {
        return new Iter();
    }

}
