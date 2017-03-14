/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.utils;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;

public class IReporterTest extends TestCase {
    
    Mockery context = new Mockery();
    IReporter rep;
    
    @Override    
    protected void setUp() throws Exception {
        super.setUp();
        rep = IReporterHelper.push(context);
    }

    @Override
    protected void tearDown() throws Exception {
        IReporterHelper.pop();
        super.tearDown();
    }
    
    public static class MyClass{
        String m_name;

        public String getName() {
            return m_name;
        }

        public MyClass setName(String name) {
            m_name = name;
            return this;
        }

        @Override
        public int hashCode() {
            final int PRIME = 31;
            int result = 1;
            result = PRIME * result + ((m_name == null) ? 0 : m_name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final MyClass other = (MyClass) obj;
            if (m_name == null) {
                if (other.m_name != null)
                    return false;
            } else if (!m_name.equals(other.m_name))
                return false;
            return true;
        }
    }
    
    
    public void testSimple(){
        // expectations
        context.checking(new Expectations() 
            {{
                one(rep).report("MyFile", "MyPos", "");
                one(rep).report("MyFile", "MyPos2", null);                
                one(rep).report("MyFile", "MyPos3", 123L);
                one(rep).report("MyFile", "MyPos4", new MyClass().setName("probe"));                
            }}
        );
        
        IReporterHelper.report("MyFile", "MyPos", "");
        IReporterHelper.report("MyFile", "MyPos2", null);
        rep.report("MyFile", "MyPos3", 123L);
        rep.report("MyFile", "MyPos4", new MyClass().setName("probe"));        
        
        context.assertIsSatisfied();        
    }

}
