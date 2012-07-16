/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.extensions;

import com.triniforce.db.test.TFTestCase;

public class ObjectInstantiatorTest extends TFTestCase {

    public static class MyClass {

    }

    @Override
    public void test() throws Exception {
        // from object
        Object instance0 = new MyClass();

        ObjectInstantiator oi = new ObjectInstantiator(
                new ObjectFactoryFromObject(instance0));
        Object instance1 = oi.getInstance();
        Object instance2 = oi.getInstance();
        assertSame(instance1, instance2);
        assertSame(instance0, instance2);
        oi.setSingle(true);
        assertSame(instance0, oi.getInstance());
    }

    public void testIsNewInstance() {
        //multiple
        {
            ObjectInstantiator oi = new ObjectInstantiator(
                    new ObjectFactoryFromClassName(MyClass.class.getName()));
            assertFalse(oi.isSingle());
            assertTrue(oi.isNewInstance());
            oi.getInstance();
            assertTrue(oi.isNewInstance());
            
        }
        //single
        {
            ObjectInstantiator oi = new ObjectInstantiator(
                    new ObjectFactoryFromClassName(MyClass.class.getName()));
            assertFalse(oi.isSingle());
            oi.setSingle(true);
            assertTrue(oi.isSingle());
            assertTrue(oi.isNewInstance());
            oi.getInstance();
            assertFalse(oi.isNewInstance());            
        }
    }

    public void testFromClassName() throws Exception {
        // from object
        Object instance0 = new MyClass();

        ObjectInstantiator oi = new ObjectInstantiator(
                new ObjectFactoryFromClassName(MyClass.class.getName()));
        Object instance1 = oi.getInstance();
        assertTrue(instance1 instanceof MyClass);
        Object instance2 = oi.getInstance();
        assertNotSame(instance1, instance2);
        assertNotSame(instance0, instance1);

        oi.setSingle(true);
        Object instance3 = oi.getInstance();
        assertNotSame(instance1, instance3);
        assertNotSame(instance0, instance3);
        Object instance4 = oi.getInstance();
        assertSame(instance3, instance4);
    }

}
