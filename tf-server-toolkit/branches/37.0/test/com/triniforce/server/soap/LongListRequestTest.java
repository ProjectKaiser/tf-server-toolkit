/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.List;

import com.triniforce.db.test.TFTestCase;

public class LongListRequestTest extends TFTestCase {

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
