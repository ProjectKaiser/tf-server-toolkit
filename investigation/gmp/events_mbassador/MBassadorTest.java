/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package gmp.events_mbassador;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.listener.Handler;

import com.triniforce.db.test.TFTestCase;

public class MBassadorTest extends TFTestCase {

	@Handler
	public void myHandler(String str) {
		trace(str);
	}

	@Handler
	public void myHandler2(String str) {
		throw new RuntimeException("My RTE");
	}
	
	@Handler
	public void myHandler3(String str) {
		trace("3:" + str);
	}
	

	@Override
	public void test() throws Exception {
		IPublicationErrorHandler peh = new IPublicationErrorHandler(){

			@Override
			public void handleError(PublicationError error) {
				trace(error);				
			}
			
		};
		MBassador bus = new MBassador(peh);
		bus.subscribe(this);
		bus.publish("post");
	}
}
