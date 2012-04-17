/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.utils.pipe;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.triniforce.db.test.TFTestCase;

public class PipeTest extends TFTestCase {

	public void testAddPushElement() {
		Pipe pipe = new Pipe();
		Mockery ctx = new Mockery();
		IPushElement element = ctx.mock(IPushElement.class);
		pipe.addPushElement(element);
		pipe.addPushElement(element);
		pipe.addPushElement(element);
		ctx.assertIsSatisfied();
	}

	public void testPush() {
		Pipe pipe = new Pipe();
		Mockery ctx = new Mockery();
		final IPipeElementFeedback fb = ctx.mock(IPipeElementFeedback.class);
		final IPushElement element = ctx.mock(IPushElement.class);
		{
			pipe.addPushElement(element);
			ctx.checking(new Expectations(){{
				one(element).push("data1", fb);
				exactly(1).of(fb).isStopped(); will(returnValue(false));
			}});
			pipe.push("data1", fb);
			
			ctx.assertIsSatisfied();
		}
		{
			pipe.addPushElement(element);
			pipe.addPushElement(element);
			ctx.checking(new Expectations(){{
				one(element).push("data2", fb);
				one(element).push("data2", fb);
				one(element).push("data2", fb);
				exactly(3).of(fb).isStopped(); will(returnValue(false));
			}});
			pipe.push("data2", fb);
			
			ctx.assertIsSatisfied();
		}
		{//	test isStoped flag 
			ctx.checking(new Expectations(){{
				one(element).push("data3", fb);
				one(fb).isStopped(); will(returnValue(false));
				one(fb).isStopped(); will(returnValue(true));
			}});
			pipe.push("data3", fb);
			
			ctx.assertIsSatisfied();
		}
	}

}
