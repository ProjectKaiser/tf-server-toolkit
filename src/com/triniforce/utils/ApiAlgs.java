package com.triniforce.utils;


import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.triniforce.utils.IProfilerStack.PSI;

public class ApiAlgs {
    
    public static List<String> m_safeExceptionsPatterns;
    
    static{
        m_safeExceptionsPatterns = new ArrayList<String>();
        m_safeExceptionsPatterns.add(".*network request to host.*");
        m_safeExceptionsPatterns.add(".*database.+shutdown.*");
    }
    
    public static boolean isSeriousException(Throwable e){
        if(null == e){
            return true;
        }
        String m = e.getMessage();
        if(null == m){
            return true;
        }

        for(String s:m_safeExceptionsPatterns){
            if(m.matches(s)){
                return false;
            }
        }
        
        if (e instanceof ESafeException || ExceptionUtils.getRootCause(e) instanceof ESafeException) {
        	return false;
        }
        
        return true;
    }
    
    public static void logError(Object obj, String msg, Throwable e){
    	if (e instanceof ESafeException) {
    		return;
    	}
        if(isSeriousException(e)){
            ApiAlgs.getLog(obj).error(msg, e);
        }else{
            ApiAlgs.getLog(obj).trace(msg, e);
        }
    }

    
    @SuppressWarnings("serial")
    public static class RethrownException extends RuntimeException{
        public RethrownException(Throwable t) {
            super(t);
            ApiAlgs.assertNotNull(t, "cause");
        }
        
        @Override
        public void printStackTrace(PrintStream s) {
        	getCause().printStackTrace(s);
        }
        
        public void printStackTrace(PrintWriter s) {
        	getCause().printStackTrace(s);
        }

    }
    
    @Deprecated
    public static Log getDevLog(Object category){
        return getLog(category.getClass());
    }
    
    /**
     * @param cls
     * @return null if not enabled
     */
    public static Class calcEnabledTraceClass(IApi api, Class cls){
        while(cls != null){
            Log log = getLog(cls);
            if(log.isTraceEnabled()){
                return cls;
            }else if(log.isDebugEnabled()){
                //yes, DEBUG disables tracing
                return null;
            }
            cls = cls.getSuperclass();
        }
        return null;
    }
    
    public static Log getLog(Object category){
        return getLog(category.getClass());
    }    

    public static Log getLog(IApi api, Class category){
        LogFactory logFactory = api.queryIntfImplementor(LogFactory.class);
        if(null == logFactory)
            return LogFactory.getLog(category);
        else
            return logFactory.getInstance(category);
    }

    
    public static Log getLog(Class category){
        return getLog(ApiStack.getApi(), category);
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
        
        @Override
        public String toString() {
        	return m_name;
        }
    }

}
