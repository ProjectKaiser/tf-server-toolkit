/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.utils;

import java.util.Stack;

public interface IProfilerStack {
    
    public static class EWrongPSI extends RuntimeException{
        private static final long serialVersionUID = 0L;
        public EWrongPSI(String group, String itemName) {
            super(group+':'+itemName);
        }
    } 
    
    public static class PSI{
        
        static class EPSIClosed extends RuntimeException{
            private static final long serialVersionUID = 4409449291159381040L;}
        
        IProfilerStack m_owner;
        private final String m_group;
        private final String m_item;
        private long m_creationTime;
        private long m_childsTime;
		private int m_stackNumber;
		private String m_context;
        
        public PSI(IProfilerStack owner, String group, String item, long creationTime, int stackNumber) {
            m_owner = owner;
            m_group = group;
            m_item = item;
            m_creationTime = creationTime;
            m_childsTime = 0L;
            m_stackNumber = stackNumber;
            m_context = null;

        }
        
        public PSI(IProfilerStack owner, String group, String item, long creationTime, int stackNumber, String context) {
            m_owner = owner;
            m_group = group;
            m_item = item;
            m_creationTime = creationTime;
            m_childsTime = 0L;
            m_stackNumber = stackNumber;
            m_context = context;
        }


        public void close() throws EPSIClosed{
            if(m_owner == null)
                throw new EPSIClosed();
            m_owner.release(this);
            m_owner = null;
        }
        
        public String getGroup(){
            return m_group;
        }
        
        public String getItem(){
            return m_item;
        }
        
        public long getCreationTime(){
            return m_creationTime;
        }

        public long getChildsTime() {
            return m_childsTime;
        }

        public void addChildTime(long time) {
            m_childsTime += time;
        }

		public int getStackNumber() {
			return m_stackNumber;
		}
		
		public String getContext() {
			return m_context;
		} 
        
    }
    
    PSI getStackItem(String group, String item);
    PSI getStackItem(String group, String item, String context);
    void release(PSI psi) throws EWrongPSI ;
    Stack<PSI> getStack();


}
