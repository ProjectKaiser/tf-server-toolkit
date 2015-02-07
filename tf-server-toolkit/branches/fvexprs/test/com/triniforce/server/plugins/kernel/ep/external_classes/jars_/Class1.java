/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.external_classes.jars_;

import java.io.Serializable;

public class Class1 implements Serializable {
    
    
    public int callClass3(){
        return Class3.calc();
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param args
     */
    public static void main(String[] args) {
    }

}
