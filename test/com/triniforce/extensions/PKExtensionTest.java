/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.extensions;

import com.triniforce.db.test.TFTestCase;

public class PKExtensionTest extends TFTestCase {

    public static class MyClass {

    }

    public static class MyExtensionClass extends PKExtensionClass{
        
    }
    
    public void testGetInstance() {
        {// single
            IPKRootExtensionPoint rep = new PKRootExtensionPoint();
            IPKExtensionPoint ep = new PKExtensionPoint();
            assertTrue(ep.isSingleExtensionInstances());
            rep.putExtensionPoint("ep", ep);
            ep.putExtension("e", MyClass.class);

            MyClass mc1 = rep.getExtension("ep", "e").getInstance();
            MyClass mc2 = rep.getExtension("ep", "e").getInstance();
            assertSame(mc1, mc2);
        }
        {// multiple
            IPKRootExtensionPoint rep = new PKRootExtensionPoint();
            IPKExtensionPoint ep = new PKExtensionPoint();
            ep.setSingleExtensionInstances(false);
            assertFalse(ep.isSingleExtensionInstances());
            rep.putExtensionPoint("ep", ep);
            ep.putExtension("e", MyClass.class);

            MyClass mc1 = rep.getExtension("ep", "e").getInstance();
            MyClass mc2 = rep.getExtension("ep", "e").getInstance();
            assertNotSame(mc1, mc2);
        }
        
        {// test IPKExtensionObject usage
            IPKRootExtensionPoint rep = new PKRootExtensionPoint();
            IPKExtensionPoint ep = new PKExtensionPoint();
            rep.putExtensionPoint("ep", ep);
            ep.putExtension("e", MyExtensionClass.class);

            MyExtensionClass mc1 = rep.getExtension("ep", "e").getInstance();
            MyExtensionClass mc2 = rep.getExtension("ep", "e").getInstance();
            assertSame(mc1, mc2);
            assertSame(ep, mc1.getExtensionPoint());
            assertEquals("e", mc1.getExtension().getId());
        }

    }

    @Override
    public void test() {

        // constructor
        {
            PKExtension pke = new PKExtension();
            assertEquals("", pke.getWikiDescription());
            assertEquals("", pke.getProvider());
            assertEquals("", pke.getPluginId());

            pke.setWikiDescription("2");
            pke.setProvider("3");
            pke.setPluginId("4");
            assertEquals("2", pke.getWikiDescription());
            assertEquals("3", pke.getProvider());
            assertEquals("4", pke.getPluginId());
        }

        class MyExtension {

        }
        
        class MyExtension2{

        }

        // wiki description temp class
        {
            PKExtension e = new PKExtension() {
            };
            assertEquals("", e.getWikiDescription());
        }
        // wiki description MyExtension
        {
            IPKExtension e = new PKExtension(null,
                    new ObjectFactoryFromClassName(MyExtension.class.getName()));
            assertFalse("".equals(e.getWikiDescription()));
        }
        
        // MyExtension2 - no description
        {
            IPKExtension e = new PKExtension(null,
                    new ObjectFactoryFromClassName(MyExtension2.class.getName()));
            assertTrue("".equals(e.getWikiDescription()));
        }

    }

}
