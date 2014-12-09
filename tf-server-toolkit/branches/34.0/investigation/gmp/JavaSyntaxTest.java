/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package gmp;

import java.util.Iterator;
import java.util.List;

public class JavaSyntaxTest {;//";" here?
    public void pr(){
        
    }

    public static class ListBackwardIterator<T> implements Iterable<T>{
        private final List m_list;

        public ListBackwardIterator(List list) {
            m_list = list;
        }

        @SuppressWarnings("unchecked") //$NON-NLS-1$
        public Iterator<T> iterator() {
            return m_list.iterator();
        }
        
        
    }
    
    public void testIterator(){
//        List<String> myList = new ArrayList<String>();
//        ListBackwardIterator<String> lbi = new ListBackwardIterator(myList);
//        for(String s: lbi){
//        }
    }
}
