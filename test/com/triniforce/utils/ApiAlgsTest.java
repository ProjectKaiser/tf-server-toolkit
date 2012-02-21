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
	
	public void testAssertTrue() {
		ApiAlgs.assertTrue(true, "");
		try {
			ApiAlgs.assertTrue(false, "mymsg");
			fail();
		} catch (RuntimeException e) {
			assertTrue( e.getMessage().contains("mymsg"));			
		}			
	}
	
	public void testAssertEquals() {
		ApiAlgs.assertEquals(null, null);
		ApiAlgs.assertEquals("1", "1");
		ApiAlgs.assertEquals(2L, 2L);
		try {
			ApiAlgs.assertEquals(null, "");
			fail();
		} catch (EAssertEqualsFailed e) {
		}		
		try {
			ApiAlgs.assertEquals("", null);
			fail();
		} catch (EAssertEqualsFailed e) {
		}		
		try {
			ApiAlgs.assertEquals("1", "2");
			fail();
		} catch (EAssertEqualsFailed e){
		}		
	}
	
	public void testAssertNotNull() {
		ApiAlgs.assertNotNull(1, "name");
		try {
			ApiAlgs.assertNotNull(null, "null name");
			fail();
		} catch (RuntimeException e) {
			assertTrue( e.getMessage().contains("null name"));
			trace(e.getMessage());
		}
	}	
	
    public void testReadResource() {
        {// ok
            String res = ApiAlgs
                    .readResource(JustAClass.class, "script.bs");
            assertEquals("print(\"Hello\");", res);
        }

        {// EResourceNotFound
            try {
                ApiAlgs.readResource(JustAClass.class,
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
