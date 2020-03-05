/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.plugins.kernel;

import com.triniforce.db.ddl.TableDef.EDBObjectException;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.extensions.PKPlugin;
import com.triniforce.server.plugins.kernel.tables.NextId;
import com.triniforce.server.srvapi.IIdGenerator;
import com.triniforce.server.srvapi.ISOQuery;
import com.triniforce.server.srvapi.ISORegistration;
import com.triniforce.server.srvapi.IBasicServer.Mode;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IProfiler;
import com.triniforce.utils.IProfilerStack.PSI;

public class IdGeneratorTest extends BasicServerTestCase {

	private static final int RSV_SIZE = 0x2000;
	
	static class IdGeneratorTestTab extends NextId{
		public IdGeneratorTestTab() {
			super(IdGeneratorTestTab.class.getName());
		}
	}

	@Override
	protected void setUp() throws Exception {
		addPlugin(new PKPlugin() {
			
			@Override
			public void doRegistration() {
			}
			
			@Override
			public void doRegistration(ISORegistration reg) throws EDBObjectException {
				reg.registerTableDef(new IdGeneratorTestTab());
			}
			
			@Override
			public void doExtensionPointsRegistration() {
			}
		});
		super.setUp();
		getServer().enterMode(Mode.Running);
	}
	
	@Override
	protected void tearDown() throws Exception {
		getServer().leaveMode();
		super.tearDown();
	}
	
    @Override
	public void test() throws Exception {
		IIdGenerator gen = new IdGenerator(IdGenerator.KEY_CACHE_SIZE, 
				ApiStack.getInterface(ISOQuery.class).getEntity(IdGeneratorTestTab.class.getName()));

		long v = gen.getKey();
		assertTrue(""+v, v >=  IdGenerator.MIN_GENERATED_KEY);
		
		for(int i=0; i<2*IdGenerator.KEY_CACHE_SIZE; i++){
			long k = gen.getKey();
			trace(k);
			assertEquals("" + k, v+i+1, k);
		}
	}
	
	public void testBigReserve(){
        IIdGenerator gen = ApiStack.getApi().getIntfImplementor(IIdGenerator.class);

        PSI psi = ApiAlgs.getProfItem("test", "testBigReserve");
        for(int i=0; i<RSV_SIZE; i++){
            gen.getKey();
        }
        ApiAlgs.closeProfItem(psi);
        ApiAlgs.getLog(this).trace(ApiStack.getInterface(IProfiler.class).toString());
	}

	
	public void testSetKey(){
        IdGenerator gen = ApiStack.getApi().getIntfImplementor(IIdGenerator.class);
        try{
            gen.setKey(10L);
            fail();
        } catch(RuntimeException e){}
        
        long v = gen.getKey()+1000;
	    
        gen.setKey(v);
        assertEquals(v, gen.getKey());
        assertEquals(v+1, gen.getKey());
        
        IdGenerator gen2 = new IdGenerator(20, new NextId());
        assertEquals(v + IdGenerator.KEY_CACHE_SIZE, gen2.getKey());
	}
}
