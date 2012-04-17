/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.triniforce.db.ddl.DiffLeader.ICmdFactory.Action;
import com.triniforce.db.test.TFTestCase;

public class DiffLeaderTest extends TFTestCase {

	public void testDiffLeader() {
	}
	
	static class TestCG implements DiffLeader.ICmdFactory<String, String>{

		public String addCmd(String element) {
			return "add_"+element;
		}

		public String dropCmd(String element) {
			return "drop_"+element;
		}

		public String editCmd(String srcElement, String dstElement) {
			return "edit_"+srcElement;
		}

		HashMap<String, Action> m_dropAndAdd = new HashMap<String, Action>();
		
		public com.triniforce.db.ddl.DiffLeader.ICmdFactory.Action getEqKeyAction(String srcElement, String dstElement) {
			Action action = m_dropAndAdd.get(srcElement);
			return null == action ? Action.NONE : action;
		}
		
	} 

	public void testGetCommandSeq() {
		TestCG cg = new TestCG();
		DiffLeader<String, String> dl = new DiffLeader<String, String>(cg);
		
		Map<String,String> src = new HashMap<String,String>();
		Map<String,String> dst = new HashMap<String,String>();
		
		List<String> res = dl.getCommandSeq(src, dst);
		assertEquals(Collections.emptyList(), res);
		
		dst = mapOf("e_1", "e_2", "e_3", "e_4");
		
		res = dl.getCommandSeq(src, dst);
		assertEquals(4, res.size());
		assertTrue(res.toString(), res.containsAll(Arrays.asList(
				"add_ve_1",
				"add_ve_2",
				"add_ve_3",
				"add_ve_4")));
		
		src = mapOf("e_2", "e_3");
		res = dl.getCommandSeq(src, dst);
		assertEquals(2, res.size());
		assertTrue(res.toString(), res.containsAll(Arrays.asList(
				"add_ve_1",
				"add_ve_4")));
		
		cg.m_dropAndAdd.put("ve_2", Action.DROP_AND_ADD);
		res = dl.getCommandSeq(src, dst);
		assertTrue(res.toString(), res.containsAll(Arrays.asList(
				"add_ve_1", "drop_ve_2", "add_ve_2", "add_ve_4")));
		
		src = mapOf("e_2", "e_3", "e_5");
		cg.m_dropAndAdd.clear();
		res = dl.getCommandSeq(src, dst);
		assertTrue(res.toString(), res.containsAll(Arrays.asList(
				"add_ve_1", "add_ve_4", "drop_ve_5")));
		
		cg.m_dropAndAdd.put("ve_3", Action.EDIT);
		res = dl.getCommandSeq(src, dst);
		assertTrue(res.toString(), res.containsAll(Arrays.asList(
				"add_ve_1", "edit_ve_3", "add_ve_4", "drop_ve_5")));
	}

	private HashMap<String,String> mapOf(String... keys) {
		HashMap<String, String> res = new HashMap<String, String>();
		for (String value : keys) {
			res.put(value, "v"+value);
		}
		return res;
	}

}
