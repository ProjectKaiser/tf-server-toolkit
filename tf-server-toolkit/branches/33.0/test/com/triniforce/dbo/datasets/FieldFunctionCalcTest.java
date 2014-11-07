/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.Arrays;
import java.util.List;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.soap.FieldFunctionRequest;

public class FieldFunctionCalcTest extends TFTestCase {
	
	static class TestFF extends FieldFunction{
		@Override
		public Object exec(Object value) {
			return "test_FF";
		}
	}
	
	static class TestFF2 extends FieldFunction{
		@Override
		public Object exec(Object value) {
			return value;
		}
	}

	@Override
	public void test() throws Exception {
		FieldFunctionCalc c = new FieldFunctionCalc(Arrays.asList("f1","f2","f3"), 
				Arrays.asList(
						new FieldFunctionRequest("f2", "", "ff_01"),
						new FieldFunctionRequest("f1", "", "ff_02")), 
				Arrays.asList(
						(FieldFunction)new TestFF(), new TestFF2()));
		
		List<Object> res = c.calc(new IRow(){
			public Object getObject(int idx) {
				return Arrays.asList(1,2,3).get(idx-1);
			}
		});
		assertEquals(Arrays.asList("test_FF", 1), res);
	}
}
