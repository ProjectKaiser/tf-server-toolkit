/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.extensions;

import java.util.LinkedHashMap;

import com.triniforce.db.test.TFTestCase;

public class PKExtensionPointTest extends TFTestCase {

    @Override
    public void test() {
        IPKExtensionPoint ep = new PKExtensionPoint();
        Object e1 = new MyExtension11();
        Object e2 = new MyExtension12();

        try {
            ep.getExtension("e1");
            fail();
        } catch (EExtensionNotFound e) {
            trace(e);
        }

        assertEquals(0, ep.getExtensions().size());
        String e1Id = this.getClass().getName();
        ep.putExtension(e1Id, e1);
        ep.putExtension("e2", e2);
        assertEquals(2, ep.getExtensions().size());
        ep.getExtension(e1Id);
        assertEquals(e1Id, ep.getExtension(e1Id).getId());
        assertEquals("e2", ep.getExtension("e2").getId());

        ep.removeExtension(e1Id);
        assertEquals(1, ep.getExtensions().size());
        try {
            ep.getExtension(e1Id);
            fail();
        } catch (EExtensionNotFound e) {
            trace(e);
        }

        MyExtension e = new MyExtension();
        ep.putExtension(e);
        assertEquals(2, ep.getExtensions().size());
        ep.getExtension(MyExtension.class.getName());
        assertSame(e, ep.getExtension(MyExtension.class.getName())
                .getInstance());
        assertSame(e, ep.getExtension(MyExtension.class).getInstance());

        assertTrue(ep.getExtensions() instanceof LinkedHashMap);

    }

    public static class MyExtension {
    }

    public static class MyExtension11 {
    }
    
    public static class MyExtension111 extends MyExtension11 {
    }

    public static class MyExtension12 {
    }

    public static class MyExtension21 {
    }

    public static class MyExtension22 {
    }

    public static class MyExtensionPoint extends PKExtensionPoint {
    }

    public static class MyExtensionPoint1 extends PKExtensionPoint {
    }

    public static class MyExtensionPoint2 extends PKExtensionPoint {
    }

    public void testExtensionClass() {
		IPKRootExtensionPoint rootEp = new PKRootExtensionPoint();
		IPKExtensionPoint ep1 = new MyExtensionPoint1();
		rootEp.putExtensionPoint("ep1", ep1);

		ep1.putExtension("e1", MyExtension.class);

		ep1.setExtensionClass(MyExtension11.class);
		try {
			ep1.putExtension("e2", MyExtension.class);
			fail();
		} catch (EIncompatibleExtensionClass e){
			trace(e);
		}
		
		ep1.putExtension("e3", MyExtension11.class);
		ep1.putExtension("e4", MyExtension111.class);
	}
    
    
    public void testRootExtension() {
        IPKRootExtensionPoint rootEp = new PKRootExtensionPoint();
        IPKExtensionPoint ep1 = new MyExtensionPoint1();
        IPKExtensionPoint ep2 = new MyExtensionPoint2();
        Object e11 = new MyExtension11();
        Object e12 = new MyExtension12();
        Object e21 = new MyExtension21();
        Object e22 = new MyExtension22();

        rootEp.putExtensionPoint(ep1);
        ep1.putExtension(e11);
        ep1.putExtension(e12);
        rootEp.putExtensionPoint(ep2);
        ep2.putExtension(e21);
        ep2.putExtension(e22);

        assertEquals(MyExtensionPoint1.class.getName(), rootEp
                .getExtensionPoint(MyExtensionPoint1.class).getId());
        assertEquals(MyExtensionPoint2.class.getName(), rootEp
                .getExtensionPoint(MyExtensionPoint2.class).getId());

        assertSame(ep2, rootEp.getExtension(ep2.getClass(), e21.getClass())
                .getExtensionPoint());
        assertSame(ep2, rootEp.getExtension(ep2.getClass(), e22.getClass())
                .getExtensionPoint());
        assertSame(ep1, rootEp.getExtension(ep1.getClass(), e11.getClass())
                .getExtensionPoint());
        assertSame(ep1, rootEp.getExtension(ep1.getClass(), e12.getClass())
                .getExtensionPoint());
        assertSame(rootEp, ep1.getRootExtensionPoint());
        trace(rootEp.getExtensionPoints());
        assertSame(e22, ep1.getRootExtensionPoint().getExtension(
                MyExtensionPoint2.class, MyExtension22.class).getInstance());
    }

    public void testClassNames() throws Exception {
        Class.forName(MyExtension.class.getName());
        try {
            Class.forName(MyExtension.class.getCanonicalName());
            fail();
        } catch (ClassNotFoundException e) {
            trace(e);
        }
    }

}
