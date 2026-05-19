/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.ep.external_classes;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Collection;

import com.triniforce.db.test.TFTestCase;

public class PackagesFolderTest extends TFTestCase {

    public void testClassLoaderClosedAfterClose() throws Exception {
        final File t = new File(getTempTestFolder(), "testCLClose");
        t.mkdirs();
        final File p1 = new File(t, "class1-21.1.923");
        p1.mkdirs();
        copyTestResources(new String[] { "class1.jar", "class3_1.jar" }, p1);

        PackagesFolder pf = new PackagesFolder() {
            @Override
            public File getFolder() {
                return t;
            }
        };

        Collection<Class> classes = pf.listClassesOfType(Object.class);
        assertEquals(1, classes.size());
        Class c = classes.iterator().next();
        ClassLoader cl = c.getClassLoader();
        WeakReference<ClassLoader> ref = new WeakReference<>(cl);

        // Release all strong references to the classloader
        classes = null;
        c = null;
        cl = null;
        pf.close();
        pf = null;

        // Verify the classloader is GC'd
        long deadline = System.currentTimeMillis() + 5000;
        while (ref.get() != null && System.currentTimeMillis() < deadline) {
            System.gc();
            Thread.sleep(10);
        }
        assertNull("ClassLoader should have been garbage collected after close()", ref.get());
    }

    @Override
    public void test() throws Exception {

        final File t = getTempTestFolder();
        //test empty folder
        {
            PackagesFolder pf = new PackagesFolder() {
                @Override
                public File getFolder() {
                    return t;
                }
            };
            Collection<Class> classes = pf.listClassesOfType(Object.class);
            assertEquals(0, classes.size());
        }

        // test class1.jar", "class3_1.jar
        {
            final File p1 = new File(t, "class1-21.1.923");
            p1.mkdirs();
            copyTestResources(new String[] { "class1.jar", "class3_1.jar" }, p1);

            PackagesFolder pf = new PackagesFolder() {

                @Override
                public File getFolder() {
                    return t;
                }
            };

            Collection<Class> classes = pf.listClassesOfType(Object.class);
            assertEquals(1, classes.size());
            Class c = classes.iterator().next();
            Method m = c.getMethod("callClass3");
            Object o = c.newInstance();
            Object res = m.invoke(o);
            assertEquals(1, res);
            
        }

        {
            final File p1 = new File(t, "class2-1.1");
            p1.mkdirs();
            copyTestResources(new String[] { "class2.jar", "class3_2.jar" }, p1);

            PackagesFolder pf = new PackagesFolder() {

                @Override
                public File getFolder() {
                    return t;
                }
            };

            Collection<Class> classes = pf.listClassesOfType(Object.class);
            assertEquals(2, classes.size());
            Class c = null;
            for(Class cs: classes){
                if(cs.getName().endsWith("Class2")){
                    c = cs;
                    break;
                }
            }
            if(c == null){
            	fail();
            } else {
				Method m = c.getMethod("callClass3");
	            Object o = c.newInstance();
	            Object res = m.invoke(o);
	            assertEquals(2, res);
            }
        }

    }

}
