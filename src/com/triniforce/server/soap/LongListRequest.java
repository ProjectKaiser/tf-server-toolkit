/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.soap.PropertiesSequence;
import com.triniforce.utils.IName;
import com.triniforce.utils.TFUtils;


@PropertiesSequence( sequence = {"args", "startFrom", "limit", "params"})
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
    
    private Map<String, Object> m_namedParams = new HashMap<String, Object>();

    public List<Object> getArgs() {
        return m_args;
    }

    public static Object MustHaveValueMarker = new Object();
    
    
    static <T> T processDefault(String argName, Class<? extends T> cls, T def){
        if( def == MustHaveValueMarker ){
            throw new EArgumentMustHaveValue(argName);
        }
        return def;
    }
    
    static <T> T checkResult(Object res, String argName, Class<? extends T> cls, T def){
        if( null == res) {
            if( def == MustHaveValueMarker ){
                throw new EArgumentMustHaveValue(argName);
            }
            return def;
        }
        if(null != cls && !cls.isAssignableFrom(res.getClass())){
            throw new IllegalArgumentException("Incompatible type for argument '" + argName +"'. Expected " + cls.getName() + " but " + res.getClass());
        }
        return (T)res;
    }

    /**
     * @return args[idx] if it exists and of given class, def otherwise.
     * <p>Throws IllegalArgumentException(argName) if cls is specified and argument does not match it
     * <p>If def is {@link LongListRequest#MustHaveValueMarker} and argument does not exist or null then {@link EArgumentMustHaveValue} is thrown. 
     */

    public static <T> T getArg(List args, int idx, String argName, Class<? extends T> cls, T def){
        if(idx <0) throw new IllegalArgumentException("idx must be GE zero");
        if(args.size()<=idx){
        	return processDefault(argName, cls, def);
        }
        return checkResult(args.get(idx), argName, cls, def);
    }
    
    public static <T> T getNamedParam(Map<String, Object> params, String paramName, Class<? extends T> cls, T def){
        if(TFUtils.isEmptyString(paramName)) throw new IllegalArgumentException("paramName must have value");
        if(!params.containsKey(paramName)){
        	return processDefault(paramName, cls, def);
        }
        return checkResult(params.get(paramName), paramName, cls, def);
    }
    
    /**
     * @return arg[idx] if it exists and of given class, def otherwise. 
     * Throws IllegalArgumentException(argName) if cls is specified and argument does not match it
     */
    @SuppressWarnings("unchecked")
    public <T> T getArg(int idx, String argName, Class<? extends T> cls, Object def){
    	return (T) getArg(m_args, idx, argName, cls, def);
    }
    
    public <T> T getNamedParam(String paramName, Class<? extends T> cls, Object def){
    	return (T) getNamedParam(m_namedParams, paramName, cls, def);
    }
    
    public LongListRequest addParam(String name, Object value){
    	getNamedParams().put(name, value);
    	return this;
    }
    
    public LongListRequest namedParam(IName name, Object value){
    	return addParam(name.getName(), value);
    }
    
    
    public void setArgs(List<Object> args) {
        m_args = args;
    }
	public Map<String, Object> getNamedParams() {
		return m_namedParams;
	}
	public void setNamedParams(Map<String, Object> m_params) {
		this.m_namedParams = m_params;
	}

}
