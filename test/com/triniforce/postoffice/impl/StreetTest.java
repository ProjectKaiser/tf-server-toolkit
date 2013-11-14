/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.impl;

import com.triniforce.postoffice.intf.StreetPath;

import junit.framework.TestCase;

public class StreetTest extends TestCase {
    
    public void testQueryPath(){
        Street st = new Street();
        assertSame(st, st.queryPath(null));
        assertSame(st, st.queryPath(new StreetPath()));
        Street st1 = new Street();
        Street st2 = new Street();
        st.getStreets().put("st1", st1);
        st.getStreets().put("st2", st2);
        assertSame(st1, st.queryPath(new StreetPath("st1")));
        assertSame(st2, st.queryPath(new StreetPath("st2")));
    }

}
