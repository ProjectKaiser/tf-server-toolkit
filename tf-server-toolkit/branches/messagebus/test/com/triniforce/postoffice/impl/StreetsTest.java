/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.postoffice.intf.StreetPath;

public class StreetsTest extends TFTestCase {
    
    @Override
    public void test() throws Exception {
        //empty
        {
            Streets sts = new Streets();
            assertNull(sts.queryByPath(new StreetPath()));
            assertNull(sts.queryByPath(new StreetPath("")));
            assertNull(sts.queryByPath(new StreetPath("q", "qqq")));
        }
        
        //one level
        {
            Streets sts = new Streets();
            Street root = new Street();
            sts.put("root", root);
            assertNull(sts.queryByPath(new StreetPath()));
            assertSame(root, sts.queryByPath(new StreetPath("root")));
            assertNull(sts.queryByPath(new StreetPath("root1")));
        }
        //few levels
        {
            Streets sts = new Streets();
            Street root = new Street();
            Street st11 = new Street();
            Street st12 = new Street();
            Street st121 = new Street();
            root.getChilds().put("st11", st11);
            root.getChilds().put("st12", st12);
            st12.getChilds().put("st121", st121);
            sts.put("root", root);
            assertNull(sts.queryByPath(new StreetPath()));
            assertSame(root, sts.queryByPath(new StreetPath("root")));
            assertNull(sts.queryByPath(new StreetPath("root1")));
            assertSame(st11, sts.queryByPath(new StreetPath("root", "st11")));
            assertSame(st12, sts.queryByPath(new StreetPath("root", "st12")));
            assertSame(st121, sts.queryByPath(new StreetPath("root", "st12", "st121")));
        }


    }

    

}
