/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.utils;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.EUtils.EAssertEqualsFailed;
import com.triniforce.utils.test.JustAClass;

public class ApiAlgsTest extends TFTestCase {
	
    public void test_isSeriousException() {
        assertTrue(ApiAlgs.isSeriousException(new RuntimeException("")));
        assertFalse(ApiAlgs.isSeriousException(new RuntimeException("Unable to complete network request to host qweqwe")));
        assertFalse(ApiAlgs.isSeriousException(new RuntimeException("database C:/TESTS/TESTBUILDTESTUNTILL/DB/REGRESSION.GDB shutdown kjlkj")));
        assertFalse(ApiAlgs.isSeriousException(new RuntimeException("GDS Exception. 335544721. Unable to complete network request to host qq q q q")));
        
    }
    
	public void testAssertTrue() {
		TFUtils.assertTrue(true, "");
		try {
			TFUtils.assertTrue(false, "mymsg");
			fail();
		} catch (RuntimeException e) {
			assertTrue( e.getMessage().contains("mymsg"));			
		}			
	}
	
	public void testAssertEquals() {
		TFUtils.assertEquals(null, null);
		TFUtils.assertEquals("1", "1");
		TFUtils.assertEquals(2L, 2L);
		try {
			TFUtils.assertEquals(null, "");
			fail();
		} catch (EAssertEqualsFailed e) {
		}		
		try {
			TFUtils.assertEquals("", null);
			fail();
		} catch (EAssertEqualsFailed e) {
		}		
		try {
			TFUtils.assertEquals("1", "2");
			fail();
		} catch (EAssertEqualsFailed e){
		}		
	}
	
	public void testAssertNotNull() {
		TFUtils.assertNotNull(1, "name");
		try {
			TFUtils.assertNotNull(null, "null name");
			fail();
		} catch (RuntimeException e) {
			assertTrue( e.getMessage().contains("null name"));
			trace(e.getMessage());
		}
	}	
	
    public void testReadResource() {
        {// ok
            String res = TFUtils
                    .readResource(JustAClass.class, "script.bs");
            assertEquals("print(\"Hello\");", res);
        }

        {// EResourceNotFound
            try {
                TFUtils.readResource(JustAClass.class,
                        "script.bs1");
                fail();
            } catch (EUtils.EResourceNotFound e) {
                String msg = e.getMessage();
                assertTrue(msg, msg.contains(JustAClass.class.getName()));
                assertTrue(msg, msg.contains("script.bs1"));
            }

        }
    }

}
