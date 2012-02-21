/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.utils;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;


public class ApiStack implements IApi, IFinitable{
    
    protected Stack<IApi> m_stack = new Stack<IApi>();

    @SuppressWarnings("unchecked")
    public static <T> T getInterface(Class<? extends T> intf){
        return (T)getApi().getIntfImplementor(intf);        
    }
    @SuppressWarnings("unchecked")
    public static <T> T queryInterface(Class<? extends T> intf){
        return (T)getApi().queryIntfImplementor(intf);        
    }
    
    
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public <T> T getIntfImplementor(Class cls) throws EInterfaceNotFound {
        Object result = queryIntfImplementor(cls);
        if (null == result) {
            throw new EInterfaceNotFound(cls.getName());
        }
        return (T)result;
    }

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public <T> T queryIntfImplementor(Class intf) {
        //ListIterator<IApi> i = m_stack.listIterator(m_stack.size());
        T res = null;
        for(int i=m_stack.size()-1; null == res && i >= 0; i--){
        	IApi api = m_stack.elementAt(i);
        	if(null != api){
        		res = (T)api.queryIntfImplementor(intf);
        	}
        }
//        while(null == res && i.hasPrevious()){
//            IApi api = i.previous();
//            if(null != api)
//            	res = (T)api.queryIntfImplementor(intf);
//        }
        return res;
    }
    
    protected static ThreadLocal<ApiStack> m_SrvTL = new ThreadLocal<ApiStack>(){
        @Override
        protected ApiStack initialValue() {
            return new ApiStack();
        }
    };
    
    /**
     * @return ISrvApi for current thread
     */
    public static IApi getApi(){
        return (IApi)(m_SrvTL.get());         
    }
    
    /**
     * @return SrvApi container ( SrvApi instance )for current thread
     */
    public static ApiStack getThreadApiContainer(){
        return m_SrvTL.get();         
    }    
    
    /**
     * @param newSrvApi new SrvApi container
     * @return previous SrvApi container
     */
    public static void pushApi(IApi newSrvApi){
        m_SrvTL.get().m_stack.push(newSrvApi);
    }

    public static void popApi() {
        m_SrvTL.get().m_stack.pop();
    }

    public Stack<IApi> getStack() {
        return m_stack;
    }

    public void setStack(Stack<IApi> stack) {
        m_stack = stack;
    }
    public void finit() {
    	ListIterator<IApi> i = m_stack.listIterator(m_stack.size());
    	while(i.hasPrevious())
        {// cycle
    		IApi api = i.previous();
            try {
                // finit element
            	if(api instanceof IFinitable){
            		((IFinitable)api).finit();
            	}
            } catch (Throwable t) {
                ApiAlgs.getLog(this).error("Stack finit problem", t);//$NON-NLS-1$;
            }
        }
    }
	public Map<Class, Object> getImplementors() {
		Map<Class, Object> res = new HashMap<Class, Object>();
        for(int i=0 ;i < m_stack.size(); i++){
        	IApi api = m_stack.elementAt(i);
        	res.putAll(api.getImplementors());
        }
        return res;
	}
	
	@Override
	public String toString() {
		String res = "";
        for(int i=0 ;i < m_stack.size(); i++){
        	res += "--- ApiStack entry:\n";
        	IApi api = m_stack.elementAt(i);
        	for(Class cls: api.getImplementors().keySet()){
        	    Object o = api.getIntfImplementor(cls);
        		res += cls.getName() + " by " + o.getClass().getName() + "\n";
        	}
        }
        res += "--- ApiStack eof\n";
        return res;
	}

}
