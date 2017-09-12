package com.triniforce.server.soap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.triniforce.db.test.TFTestCase;

public class SelectResponseTest extends TFTestCase {
	
	List lst(Object... args){
		return Arrays.asList(args);
	}
	
	@Override
	public void test() throws Exception {
		{
			SelectResponse sr = new SelectResponse(new ArrayList<String>(), Arrays.asList());
			assertEquals(new ArrayList<String>(), sr.getColumns());
			assertEquals(0, sr.getRows().size());
		}
		
		{
			SelectResponse sr = new SelectResponse(Arrays.asList("c1"), Arrays.asList());
			assertEquals(lst("c1"), sr.getColumns());
			assertEquals(0, sr.getRows().size());
		}
		
		{
			SelectResponse sr = new SelectResponse(Arrays.asList("c1", "c2"), Arrays.asList());
			assertEquals(lst("c1", "c2"), sr.getColumns());
			assertEquals(0, sr.getRows().size());
		}
		
		{
			SelectResponse sr = new SelectResponse(Arrays.asList("c1", "c2"), Arrays.asList((Object)"v1"));
			assertEquals(lst("c1", "c2"), sr.getColumns());
			assertEquals(1, sr.getRows().size());
		}
		
		{
			SelectResponse sr = new SelectResponse(Arrays.asList("c1", "c2"), Arrays.asList((Object)"v1", "v2", "v3"));
			assertEquals(lst("c1", "c2"), sr.getColumns());
			assertEquals(2, sr.getRows().size());
			assertEquals(lst("v1", "v2"), sr.getRows().get(0));
			assertEquals(lst("v3"), sr.getRows().get(1));
		}
	
	}

}
