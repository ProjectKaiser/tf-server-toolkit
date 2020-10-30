/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.plugins.kernel.ep.external_classes;

import java.io.File;
import java.net.URLClassLoader;
import java.util.Collection;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.extensions.PluginsLoader;

public class PackagesFolderTest2 extends TFTestCase {

	private Class findclass(PackagesFolder pf, String classname) {
		Collection<Class> classes = pf.listClassesOfType(Object.class);
		assertEquals(1, classes.size());
		Class c = null;
		for (Class cs : classes) {
			if (cs.getName().endsWith(classname)) {
				c = cs;
				break;
			}
		}
		if (c == null)
			fail();
		return c;
	}

	@Override
	public void test() throws Exception {
		final File t = getTempTestFolder();
		final File p1 = new File(t, "class2-1.1");
		p1.mkdirs();
		copyTestResources(new String[] { "class2.jar" }, p1);
		// Default parent class loader, all parent JARs available 
		{

			PackagesFolder pf = new PackagesFolder() {
				@Override
				public File getFolder() {
					return t;
				}
			};

			Class c = findclass(pf, "Class2");
			assertNotNull(c.getClassLoader()
					.loadClass("com.triniforce.server.plugins.kernel.ep.external_classes.jars.Class2"));
			assertNotNull(c.getClassLoader().loadClass("junit.framework.Test"));
			assertNotNull(c.getClassLoader().loadClass("org.apache.log4j.Appender"));
		}

		// Custom parent class loader, only certain parent JARs available
		{
			URLClassLoader parentCL = PluginsLoader.filteredUrlClassLoader(getClass(), new String[] { "/log4j-", "/junit-" }); 
			try {

				PackagesFolder pf = new PackagesFolder() {
					@Override
					public File getFolder() {
						return t;
					}
				};
				pf.setParentClassLoader(parentCL);
				Class c = findclass(pf, "Class2");

				assertNotNull(c.getClassLoader()
						.loadClass("com.triniforce.server.plugins.kernel.ep.external_classes.jars.Class2"));
				assertNotNull(c.getClassLoader().loadClass("org.apache.log4j.Appender"));
				assertNotNull(c.getClassLoader().loadClass("junit.framework.Test"));
				try {
					c.getClassLoader().loadClass("org.dbunit.DatabaseTestCase");
					throw new RuntimeException("Expected: ClassNotFoundException");
				} catch (ClassNotFoundException e) {
					// expected
				}

			} finally {
				parentCL.close();
			}
		}

	}

}
