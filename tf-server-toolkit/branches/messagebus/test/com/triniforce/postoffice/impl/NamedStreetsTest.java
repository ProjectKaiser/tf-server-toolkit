/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.postoffice.intf.StreetPath;

public class NamedStreetsTest extends TFTestCase {
    
    @Override
    public void test() throws Exception {
        //empty
        {
            NamedStreets sts = new NamedStreets();
            assertNull(sts.queryPath(new StreetPath()));
            assertNull(sts.queryPath(new StreetPath("")));
            assertNull(sts.queryPath(new StreetPath("q", "qqq")));
        }
        
        //one level
        {
            NamedStreets sts = new NamedStreets();
            Street root = new Street();
            sts.put("root", root);
            assertNull(sts.queryPath(new StreetPath()));
            assertSame(root, sts.queryPath(new StreetPath("root")));
            assertNull(sts.queryPath(new StreetPath("root1")));
        }
        //few levels
        {
            NamedStreets sts = new NamedStreets();
            Street root = new Street();
            Street st11 = new Street();
            Street st12 = new Street();
            Street st121 = new Street();
            root.getStreets().put("st11", st11);
            root.getStreets().put("st12", st12);
            st12.getStreets().put("st121", st121);
            sts.put("root", root);
            assertNull(sts.queryPath(new StreetPath()));
            assertSame(root, sts.queryPath(new StreetPath("root")));
            assertNull(sts.queryPath(new StreetPath("root1")));
            assertSame(st11, sts.queryPath(new StreetPath("root", "st11")));
            assertSame(st12, sts.queryPath(new StreetPath("root", "st12")));
            assertSame(st121, sts.queryPath(new StreetPath("root", "st12", "st121")));
        }


    }

    

}
