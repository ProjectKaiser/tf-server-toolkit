package com.triniforce.utils;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.triniforce.utils.IProfilerStack.PSI;

public class ApiAlgs {
    
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

    public static void rethrowException(Exception e) throws RuntimeException{
        //writeExceptionInfo(e, "handleException"); gmp: is it not really needed, will
        //be reported at the top
        if( e instanceof RuntimeException)throw (RuntimeException)e;
        throw new RuntimeException(e);
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
        if(null != value) return;
        throw new EUtils.EAssertNotNullFailed(MessageFormat.format("Unexpected null value for \"{0}\"",name)); //$NON-NLS-1$
    }   
    
    
    
    public static void assertEquals(Object expected, Object actual){
        if( expected == actual ) return;
        if( expected == null || actual == null){
            throw new EUtils.EAssertEqualsFailed(expected, actual);
        }
        if(expected.equals(actual)) return;
        throw new EUtils.EAssertEqualsFailed(expected, actual);
    }
    public static void assertTrue(boolean expr, String msg){
        if(!expr){
            throw new EUtils.EAssertTrueFailed(msg);
        }
    }
    
    
    /**
     * @param cls Class reousrce will be read from
     * @param name Resource name
     * @return String with resource content 
     * @throws EUtils.EResourceNotFound
     */
    public static String readResource(Class cls, String resourceName) throws EUtils.EResourceNotFound{
        StringBuffer res = new StringBuffer();
        char buf[] = new char[256];
        try {
            InputStream is = cls.getResourceAsStream(resourceName);
            if( null == is) throw new EUtils.EResourceNotFound(cls, resourceName);            
            InputStreamReader isReader = new InputStreamReader(is);
            int nRead;
            while ((nRead = isReader.read(buf)) > 0) {
                res.append(buf, 0, nRead);
            }
        } catch (IOException e) {
            ApiAlgs.rethrowException(e);
        }
        return res.toString();
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
