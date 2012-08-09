package com.triniforce.utils;


import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.triniforce.utils.IProfilerStack.PSI;

public class ApiAlgs {
    
    @SuppressWarnings("serial")
    public static class RethrownException extends RuntimeException{
        public RethrownException(Throwable t) {
            super(t);
        }
    }
    
    public static Log getLog(Object category){
        return getLog(category.getClass());
    }    
    
    public static Log getLog(Class category){
        Log res;
        LogFactory logFactory = ApiStack.queryInterface(LogFactory.class);
        if(null == logFactory)
            res = LogFactory.getLog(category);
        else
            res = logFactory.getInstance(category);
        return res;
    }

    public static void rethrowException(Throwable e) throws RuntimeException{
        if( e instanceof RuntimeException)throw (RuntimeException)e;
        throw new RethrownException(e);
    }
    
    public static void rethrowException(Exception e) throws RuntimeException{
        //writeExceptionInfo(e, "handleException"); gmp: is it not really needed, will
        //be reported at the top
        if( e instanceof RuntimeException)throw (RuntimeException)e;
        throw new RethrownException(e);
    }
    
    public static void rethrowException(String msg, Exception e) throws RuntimeException{
        //writeExceptionInfo(e, "handleException"); gmp: is it not really needed, will
        //be reported at the top
        if( e instanceof RuntimeException)throw (RuntimeException)e;
        throw new RuntimeException(msg, e);
    }

    
    public static ITime getITime(){
        return ApiStack.getApi().getIntfImplementor(ITime.class);
    }
    
    public static String getLocalTabName(String appTabName) {
        String withOutter = appTabName.substring(appTabName.lastIndexOf('.') + 1);
        int innerIdx = withOutter.lastIndexOf('$');
        return innerIdx == -1 ? withOutter : withOutter.substring(innerIdx + 1);
    }
    
    public static PSI getProfItem(String grp, String item){
        IProfilerStack stack = ApiStack.getApi().queryIntfImplementor(IProfilerStack.class);
        if( null == stack ){
            return null;
        }
        return stack.getStackItem(grp, item);
    }
    public static void closeProfItem(PSI psi){
        if( null == psi) return;
        psi.close();
    }    
    
    public static void assertNotNull(Object value, String name){
        TFUtils.assertNotNull(value, name);
    }   
    
    
    
    public static void assertEquals(Object expected, Object actual){
        TFUtils.assertEquals(expected, actual);
    }
    public static void assertTrue(boolean expr, String msg){
        TFUtils.assertTrue(expr, msg);
    }
    
    
    /**
	 * @param cls Class reousrce will be read from
	 * @param name Resource name
	 * @return String with resource content 
	 * @throws EUtils.EResourceNotFound
	 */
	public static String readResource(Class cls, String resourceName) throws EUtils.EResourceNotFound{
		return TFUtils.readResource(cls, resourceName);
	}
    
    /**
     *  For testing purposes, when it is needed to pass IName parameter
     */
    public static class SimpleName implements IName, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 7933776920136326222L;
        private String m_name;

        public SimpleName(String nam) {
            m_name = nam;
        }
        
        public SimpleName() {
        }

        public void setName(String m_name) {
            this.m_name = m_name;
        }
        public String getName() {
            return m_name;
        }

        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((m_name == null) ? 0 : m_name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final SimpleName other = (SimpleName) obj;
            if (m_name == null) {
                if (other.m_name != null)
                    return false;
            } else if (!m_name.equals(other.m_name))
                return false;
            return true;
        }
    }

}
