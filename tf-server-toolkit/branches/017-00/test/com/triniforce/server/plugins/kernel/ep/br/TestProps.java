/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.util.HashMap;

import com.triniforce.utils.TFUtils;

public class TestProps extends HashMap<String, Object>{
    private static final long serialVersionUID = 1L;
    public void putObject(Object obj){
        put(obj.getClass().getName(), obj);
    }
    public Object getObject(Class cls){
        Object res = get(cls.getName());
        TFUtils.assertNotNull(res, "res");
        return res;
    }
}
