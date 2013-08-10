/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.soap.PropertiesSequence;


@PropertiesSequence( sequence = {"args", "startFrom", "limit"})
public class LongListRequest extends SessionRequest{
    public static final String PROP_START_FROM = "startFrom"; //$NON-NLS-1$
    public static final String PROP_LIMIT = "limit"; //$NON-NLS-1$
    
    /**
     * zero-based 
     */
    protected int m_startFrom;
    
    /**
     * 0 means no limit
     */
    protected int m_limit;
    
    public int getLimit() {
        return m_limit;
    }
    public void setLimit(int limit) {
        m_limit = limit;
    }
    public int getStartFrom() {
        return m_startFrom;
    }
    public void setStartFrom(int startFrom) {
        m_startFrom = startFrom;
    }
    
    List<Object> m_args = new ArrayList<Object>();

    public List<Object> getArgs() {
        return m_args;
    }

    /**
     * @return args[idx] if it exists and of given class, def otherwise. 
     * Throws IllegalArgumentException(argName) if cls is specified and argument does not match it
     */

    public static <T> T getArg(List args, int idx, String argName, Class<? extends T> cls, Object def){
        if(idx <0) throw new IllegalArgumentException("idx must be ge zero");
        if(args.size()<=idx) return (T)def;
        Object res = args.get(idx);
        if( null == res) return null;
        if(null != cls && !cls.isAssignableFrom(res.getClass())){
            throw new IllegalArgumentException("Incompatible type for argument '" + argName +"'. Expected " + cls.getName() + " but " + res.getClass());
        }
        return (T)res;
    }
    
    public static Object getArg3(){
    	return null;
    }
    
    /**
     * @return arg[idx] if it exists and of given class, def otherwise. 
     * Throws IllegalArgumentException(argName) if cls is specified and argument does not match it
     */
    @SuppressWarnings("unchecked")
    public <T> T getArg(int idx, String argName, Class<? extends T> cls, Object def){
    	return (T) getArg(m_args, idx, argName, cls, def);
    }
    
    public void setArgs(List<Object> args) {
        m_args = args;
    }
    

}
