/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel.tables;

import java.util.UUID;

import com.triniforce.db.dml.ResSet;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.INamedDbId;
import com.triniforce.server.srvapi.ISrvSmartTran;
import com.triniforce.server.srvapi.ISrvSmartTranFactory;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.server.srvapi.INamedDbId.ENotFound;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IName;

public class TNamedDbIdTest extends BasicServerTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		getServer().enterMode(Mode.Running);
		TNamedDbId dbId = (TNamedDbId) ApiStack.getInterface(INamedDbId.class);
		dbId.clear();
	}
	
	@Override
	protected void tearDown() throws Exception {
		getServer().leaveMode();
		super.tearDown();
	}
	
	@SuppressWarnings("deprecation")
    public void test_updateNname() {
	    final String key1 = UUID.randomUUID().toString();
	    final String key2 = UUID.randomUUID().toString();
	    ISrvSmartTran tr = ApiStack.getInterface(ISrvSmartTran.class);
	    
        TNamedDbId dbId = (TNamedDbId) ApiStack.getInterface(INamedDbId.class);
        
        
	    tr.insert(TNamedDbId.class, new IName[]{TNamedDbId.name, TNamedDbId.id}, new Object[]{key1, 1});
	    tr.insert(TNamedDbId.class, new IName[]{TNamedDbId.name, TNamedDbId.id}, new Object[]{key2, 2});
	    
	    //
	    {
	        ResSet res = tr.select(TNamedDbId.class, new IName[]{TNamedDbId.name, TNamedDbId.nname}, new IName[]{}, new Object[]{});
	        res.next();
	        assertNotNull(res.getObject(1));	        
	        assertNull(res.getObject(2));
	    }
	    
        //Since prev tests loads it
        dbId.loadData();

	    
	    assertNotNull(dbId.queryId(key1));
	    assertNotNull(dbId.queryId(key2));
	    TNamedDbId.updateNname();
	    
	    final String key3 = UUID.randomUUID().toString();
	    tr.insert(TNamedDbId.class, new IName[]{TNamedDbId.name, TNamedDbId.nname, TNamedDbId.id}, new Object[]{"", key3, 3});
	    
	    dbId.loadData();
	    assertEquals((Long)1L, dbId.queryId(key1));
	    assertEquals((Long)2L, dbId.queryId(key2));
	    assertEquals((Long)3L, dbId.queryId(key3));
	    
	}
	
	
	public void testCreateId() {
		// close current transaction
		SrvApiAlgs2.getIServerTran().commit();
		
		TNamedDbId dbId = (TNamedDbId) ApiStack.getInterface(INamedDbId.class);
		assertNotNull(dbId);
		long res1 = dbId.createId("test1.name");
		long res2 = dbId.createId("test2.name");
		long res3 = dbId.createId("test3.name");
		assertFalse(res1 == res2);
		assertFalse(res1 == res3);
		
		assertEquals(res1, dbId.createId("test1.name"));
		
		// must be created in separated transaction
		ISrvSmartTranFactory trf = SrvApiAlgs2.getISrvTranFactory();
		trf.push();
		try{
			assertEquals(Long.valueOf(res2), TNamedDbId.PQGetByName.exec("test2.name"));
		}finally{
			trf.pop();
		}
	}

	
	public void testQueryId() {
	    
		TNamedDbId dbId = (TNamedDbId) ApiStack.getInterface(INamedDbId.class);

		//unicode pattern
		{
	        final String UNICODE_PATTERN = "۞∑русскийڧüöäë面伴";
	        String longUnicodeId = "";
	        {
	            longUnicodeId = UNICODE_PATTERN;
	            while(longUnicodeId.length() + 1 + UNICODE_PATTERN.length() < TNamedDbId.nname.getSize()){
	                longUnicodeId+= "." + UNICODE_PATTERN;  
	            }
	        }
			assertNull(dbId.queryId(longUnicodeId));
			Long id = dbId.createId(longUnicodeId);
			assertEquals(id, dbId.queryId(longUnicodeId));
			
		}
		
		
		assertNull(dbId.queryId("testQueryId"));
		long res[] = {
				dbId.createId("testQueryId1"),
				dbId.createId("testQueryId2"),
				dbId.createId("testQueryId3"),
				dbId.createId("testQueryId4")
		};
		assertEquals(Long.valueOf(res[0]), dbId.queryId("testQueryId1"));
		assertEquals(Long.valueOf(res[2]), dbId.queryId("testQueryId3"));
		assertEquals(Long.valueOf(res[1]), dbId.queryId("testQueryId2"));
		assertEquals(null, dbId.queryId("testQueryId5"));
		
		assertSame(dbId.queryId("testQueryId3"), dbId.queryId("testQueryId3"));
		
		TNamedDbId dbId2 = new TNamedDbId();

		assertEquals(Long.valueOf(res[0]), dbId2.queryId("testQueryId1"));
		assertEquals("testQueryId1", dbId2.getName(res[0]));
		
	}
	
	public void testDropId() {
		TNamedDbId dbId = (TNamedDbId) ApiStack.getInterface(INamedDbId.class);
		dbId.dropId(1243L);
		
		long id1 = dbId.createId("testDropId1");
		long id2 = dbId.createId("testDropId2");
		long id3 = dbId.createId("testDropId3");
		
		dbId.dropId(id2);
		
		assertEquals(Long.valueOf(id1), dbId.queryId("testDropId1"));
		assertEquals(null, dbId.queryId("testDropId2"));
		assertEquals(Long.valueOf(id3), dbId.queryId("testDropId3"));
		
		// must be dropped in separated transaction
		ISrvSmartTranFactory trf = SrvApiAlgs2.getISrvTranFactory();
		trf.push();
		try{
			assertEquals(null, dbId.queryId("testDropId2"));
		}finally{
			trf.pop();
		}

	}

	public void testGetId() {
		TNamedDbId dbId = (TNamedDbId) ApiStack.getInterface(INamedDbId.class);
		assertNull(dbId.queryId("testQueryId"));
		long res[] = {
				dbId.createId("testQueryId1"),
				dbId.createId("testQueryId2"),
				dbId.createId("testQueryId3"),
				dbId.createId("testQueryId4")
		};
		assertEquals(res[0], dbId.getId("testQueryId1"));
		assertEquals(res[2], dbId.getId("testQueryId3"));
		assertEquals(res[1], dbId.getId("testQueryId2"));
		try{
			dbId.getId("testQueryId5");
			fail();
		} catch(ENotFound e){
		}
	}

	public void testGetName() {
		TNamedDbId dbId = (TNamedDbId) ApiStack.getInterface(INamedDbId.class);
		
		long ids[] = {
				dbId.createId("testGetName1"),
				dbId.createId("testGetName2"),
				dbId.createId("testGetName3"),
				dbId.createId("testGetName4")
		};
		
		assertEquals("testGetName3", dbId.getName(ids[2]));
		assertEquals("testGetName1", dbId.getName(ids[0]));
		try{
			dbId.getName(68340634634L);
			fail();
		} catch(ENotFound e){
		}
		
		assertSame(dbId.getName(ids[1]), dbId.getName(ids[1]));
	}


}
