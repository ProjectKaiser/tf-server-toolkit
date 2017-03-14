package com.triniforce.utils;

import org.jmock.Mockery;

public class IReporterHelper {
    public static IReporter push(Mockery context){
        IReporter res = context.mock(IReporter.class);
        Api api = new Api();
        api.setIntfImplementor(IReporter.class, res);
        ApiStack.pushApi(api);
        return res;
    }
    public static void pop(){
        ApiStack.popApi();            
    }
    public static IReporter peek(){
        return ApiStack.getApi().queryIntfImplementor(IReporter.class);
    }
    public static void report(String file, String position, Object data){
        IReporter rep = ApiStack.getApi().queryIntfImplementor(IReporter.class);
        if( null == rep)return;
        rep.report(file, position, data);                      
    }
}
