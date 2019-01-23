package com.triniforce.utils.alerter;

public abstract class StateAlerter {

	private AlerterState state;
	protected String lastReason = "";
	private Boolean isChanged = false;
	private Throwable attachedThrowable = null;
	private Object relatedObject = null;
	
	public Object getRelatedObject() {
		return relatedObject;
	}

	public Throwable getAttachedThrowable() {
		return attachedThrowable;
	}

	public AlerterState getState() {
		return state;
	}
	
	public Boolean isChanged() {
		return isChanged;
	}

	public void setState(AlerterState state) {
		setState(state, null, false);
	}

	public StateAlerter(AlerterState initialState) {
		state = initialState;
	}

	public StateAlerter() {
		this(AlerterState.UNSPECIFIED);
	}

	public String getLastReason() {
		return lastReason;
	}

	public void setState(AlerterState state, String reason, Boolean differReasons) {
		switch (state) {
		case OK: {
			if (this.state == AlerterState.BAD) {
				lastReason = reason;
				becomeOK();
				attachedThrowable = null;
			}
			break;
		}
		case BAD: {
			if (this.state != AlerterState.BAD
					|| (differReasons && this.state == AlerterState.BAD && !lastReason.equals(reason))) {
				lastReason = reason;
				becomeBAD(reason);
			}
			break;
		}
		default: {
		}
		}
		isChanged = this.state != state;
		this.state = state;
	}
	
	public void setState(AlerterState state, String reason) {
		setState(state, reason, true);
	}
	
	public void setOK() {
		setState(AlerterState.OK);
	}

	public void setBAD(String reason) {
		setState(AlerterState.BAD, reason, true);
	}
	
	public void setBAD(Exception e) {
		attachedThrowable = e;
		setBAD(e.getMessage());
	}
	
	public void enterBADMode(Object relatedObject, String reason, Throwable e) {
		attachedThrowable = e;
		this.relatedObject = relatedObject;
		setState(AlerterState.BAD, reason, false);
	}
	
	public boolean isOK() {
		return state == AlerterState.OK;
	}
	
	public boolean isBAD() {
		return state == AlerterState.BAD;
	}

	protected abstract void becomeOK();

	protected abstract void becomeBAD(String reason);

	@Override
	public String toString() {
		return state.toString();
	}
}
