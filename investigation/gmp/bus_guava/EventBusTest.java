/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package gmp.bus_guava;

import com.google.common.eventbus.EventBus;
import com.triniforce.db.test.TFTestCase;

public class EventBusTest extends TFTestCase{
	
	@Override
	public void test() throws Exception {
		EventBus eb = new EventBus();
		eb.post("qq");
	}

}
