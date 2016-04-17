/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.db.test.TFTestCase;

public class LongListRequestTest extends TFTestCase {

	public void testParams() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            LongListRequest.getNamedParam(params, "arg1", String.class, LongListRequest.MustHaveValueMarker);
            fail();
        } catch (EArgumentMustHaveValue e) {
            trace(e);
        }
        
        try {
        	LongListRequest.getNamedParam(params, null, String.class, LongListRequest.MustHaveValueMarker);
        	fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("paramName"));
		}
        
        params.put("p1", null);
        try {
        	LongListRequest.getNamedParam(params, "p1", String.class, LongListRequest.MustHaveValueMarker);
            fail();
        } catch (EArgumentMustHaveValue e) {
            trace(e);
        }
        
        params.put("p1", 1);
        assertEquals((Integer)1, LongListRequest.getNamedParam(params, "p1", Integer.class, LongListRequest.MustHaveValueMarker));
        assertEquals((Integer)2, LongListRequest.getNamedParam(params, "p2", Integer.class, 2));
        String k = uuid("k");
        assertEquals(k, LongListRequest.getNamedParam(params, "p3", String.class, k));
	}
	
    @Override
    public void test() throws Exception {

        List<Object> args = new ArrayList<Object>();
        try {
            LongListRequest.getArg(args, 0, "arg1", String.class, LongListRequest.MustHaveValueMarker);
            fail();
        } catch (EArgumentMustHaveValue e) {
            trace(e);
        }
        
        args.add(null);
        try {
            LongListRequest.getArg(args, 0, "arg1", String.class, LongListRequest.MustHaveValueMarker);
            fail();
        } catch (EArgumentMustHaveValue e) {
            trace(e);
        }
        
        args.set(0, 1);
        args.add(2);
        assertEquals((Integer)1, LongListRequest.getArg(args, 0, "arg1", Integer.class, LongListRequest.MustHaveValueMarker));

    }

}
