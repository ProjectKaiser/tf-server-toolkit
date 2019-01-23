package com.triniforce.utils.alerter;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.mockito.Mockito;

import com.triniforce.db.test.TFTestCase;

public class StateAlerterTest extends TFTestCase {
	
	private class TestStateAlerter extends StateAlerter {

		@Override
		public void becomeOK() {
		}

		@Override
		public void becomeBAD(String reason) {
		}
		
		public TestStateAlerter(AlerterState initialState) {
			super(initialState);
		}

		public TestStateAlerter() {
			super();
		}

	}
	
	public void testInitialState() {
		TestStateAlerter tsa = new TestStateAlerter();
		assertEquals(AlerterState.UNSPECIFIED, tsa.getState());
		tsa = new TestStateAlerter(AlerterState.OK);
		assertEquals(AlerterState.OK, tsa.getState());
	}
	
	public void testAlerter() {
		final String r1 = "reason1";
		final String r2 = "reason2"; 
		TestStateAlerter sa = spy(new TestStateAlerter());

		// UNSPECIFIED -> OK
		sa.setState(AlerterState.OK);
		verify(sa).setState(AlerterState.OK);
		verify(sa).setState(AlerterState.OK, null, false);
		verifyNoMoreInteractions(sa);
		assertTrue(sa.isChanged());
		assertTrue(sa.isOK());
		assertFalse(sa.isBAD());
		Mockito.reset(sa);
		
		sa.setState(AlerterState.OK);
		verify(sa).setState(AlerterState.OK);
		verify(sa).setState(AlerterState.OK, null, false);
		verifyNoMoreInteractions(sa);
		assertFalse(sa.isChanged());
		assertTrue(sa.isOK());
		assertFalse(sa.isBAD());
		Mockito.reset(sa);
		
		// OK -> BAD
		sa.setState(AlerterState.BAD, r1);
		verify(sa).setState(AlerterState.BAD, r1, true);
		verify(sa).setState(AlerterState.BAD, r1);
		verify(sa).becomeBAD(r1);
		verifyNoMoreInteractions(sa);
		assertTrue(sa.isChanged());
		assertFalse(sa.isOK());
		assertTrue(sa.isBAD());
		assertEquals(r1, sa.getLastReason());
		Mockito.reset(sa);
		
		sa.setState(AlerterState.BAD, r1);
		verify(sa).setState(AlerterState.BAD, r1);
		verify(sa).setState(AlerterState.BAD, r1, true);
		verifyNoMoreInteractions(sa);
		assertFalse(sa.isChanged());
		assertFalse(sa.isOK());
		assertTrue(sa.isBAD());
		assertEquals(r1, sa.getLastReason());
		Mockito.reset(sa);
		
		sa.setState(AlerterState.BAD, r2);
		verify(sa).setState(AlerterState.BAD, r2);
		verify(sa).setState(AlerterState.BAD, r2, true);
		verify(sa).becomeBAD(r2);
		verifyNoMoreInteractions(sa);
		assertFalse(sa.isChanged());
		assertFalse(sa.isOK());
		assertTrue(sa.isBAD());
		assertEquals(r2, sa.getLastReason());
		Mockito.reset(sa);
		
		sa.setState(AlerterState.BAD, r2);
		verify(sa).setState(AlerterState.BAD, r2);
		verify(sa).setState(AlerterState.BAD, r2, true);
		verifyNoMoreInteractions(sa);
		assertFalse(sa.isChanged());
		assertFalse(sa.isOK());
		assertTrue(sa.isBAD());
		assertEquals(r2, sa.getLastReason());
		Mockito.reset(sa);
		
		// BAD -> OK
		sa.setOK();
		verify(sa).setOK();
		verify(sa).setState(AlerterState.OK);
		verify(sa).setState(AlerterState.OK, null, false);
		verify(sa).becomeOK();
		verifyNoMoreInteractions(sa);
		assertTrue(sa.isChanged());
		assertTrue(sa.isOK());
		assertFalse(sa.isBAD());
		assertNull(sa.getLastReason());
		Mockito.reset(sa);
		
		sa.setState(AlerterState.OK);
		verify(sa).setState(AlerterState.OK);
		verify(sa).setState(AlerterState.OK, null, false);
		verifyNoMoreInteractions(sa);
		assertFalse(sa.isChanged());
		assertTrue(sa.isOK());
		assertFalse(sa.isBAD());
		assertNull(sa.getLastReason());
		Mockito.reset(sa);
		
		// exception attach, OK -> BAD
		final Exception exception = new Exception("test exception");
		sa.setBAD(exception);
		verify(sa).setState(AlerterState.BAD, exception.getMessage(), true);
		verify(sa).becomeBAD(exception.getMessage());
		assertEquals(sa.getAttachedThrowable(), exception);
		Mockito.reset(sa);
		
		// become BAD with the same exception exception attach, OK -> BAD
		sa.setBAD(exception);
		verify(sa).setState(AlerterState.BAD, exception.getMessage(), true);
		verify(sa, never()).becomeBAD(exception.getMessage());
		assertEquals(sa.getAttachedThrowable(), exception);
		Mockito.reset(sa);
	}
	
	public void testEnterBADState() {
		final String r1 = "reason1";
		final String r2 = "reason2"; 
		TestStateAlerter sa = spy(new TestStateAlerter());
	
		Object testObject = new Object();
		Throwable e = new Throwable("test throwable");
		sa.enterBADMode(testObject, r1, e);
		assertTrue(e == sa.getAttachedThrowable());
		assertTrue(testObject == sa.getRelatedObject());
		assertEquals(r1, sa.getLastReason());
		verify(sa).becomeBAD(r1);
		
		// no becomeBAD even if different reason and throwable
		sa.enterBADMode(testObject, r2, e);
		verify(sa).becomeBAD(r1);
	}
}
