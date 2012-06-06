/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.WsdlDescription.WsdlTypeElement;

public class WsdlTypeElementTest extends TFTestCase {

	
	static class C1 {
		private int prop;
		private boolean prop2;
		private Boolean prop3;

		public void setProp(int prop) {
			this.prop = prop;
		}

		public int getProp() {
			return prop;
		}

		public void setProp2(boolean prop2) {
			this.prop2 = prop2;
		}

		public boolean isProp2() {
			return prop2;
		}

		public void setProp3(Boolean prop3) {
			this.prop3 = prop3;
		}

		public Boolean getProp3() {
			return prop3;
		}
	} 
	
	@Override
	public void test() throws Exception {
		TypeDefLibCache lib = new TypeDefLibCache(new ClassParser(getClass().getPackage()));
		ClassDef def = (ClassDef) lib.add(C1.class);
		WsdlTypeElement res = new WsdlDescription.WsdlTypeElement("n1", def.getProp("prop").getType(), true, 1);;
		assertFalse(res.isNillable());
		res = new WsdlDescription.WsdlTypeElement("n1", def.getProp("prop2").getType(), true, 1);;
		assertFalse(res.isNillable());
		
		assertEquals("boolean", def.getProp("prop3").getType().getName());
		
		res = new WsdlDescription.WsdlTypeElement("n1", def.getProp("prop3").getType(), true, 1);;
		assertEquals(1, res.getMinOccur());
		assertTrue(res.isNillable());
	}
}
