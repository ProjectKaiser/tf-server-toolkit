/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.util.Arrays;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceOperationDescription.NamedArg;

public class InterfaceOperationDescriptionTest extends TFTestCase {

	public void testAddNamedArg() {
		InterfaceOperationDescription opDesc = new InterfaceOperationDescription("fun1", 
				Arrays.asList(new NamedArg("arg1", int.class), new NamedArg("arg2", int.class), new NamedArg("arg3", int.class)),
				new NamedArg("res", int.class));
		
		
		opDesc.addNamedArg(1, "addArg_01", String.class);
		
		assertEquals("addArg_01", opDesc.getArgs().get(1).getName());
		assertEquals(String.class, opDesc.getArgs().get(1).getType());
		
		opDesc.addNamedArg(10, "addArg_02", boolean.class);
		
		assertEquals("addArg_02", opDesc.getArgs().get(4).getName());
		assertEquals(boolean.class, opDesc.getArgs().get(4).getType());
	}

}
