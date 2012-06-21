/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import com.triniforce.db.test.TFTestCase;

public class EntityTest extends TFTestCase {

    public static class MyEntity extends Entity{
        
    }
    
    public void testConstructor(){
        Entity e = new MyEntity();
        assertEquals(MyEntity.class.getName(), e.getEntityName());
        trace(MyEntity.class.getName());
    }
    
}
