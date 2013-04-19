/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils.pipe;

import java.util.ArrayList;

public class Pipe implements IPipe{

	private ArrayList<IPushElement> m_pushElements = new ArrayList<IPushElement>();

	public void addPushElement(IPushElement element) {
		m_pushElements.add(element);
	}

	public void push(Object data, IPipeElementFeedback fb) {
		for (IPushElement element : m_pushElements) {
			if(!fb.isStopped())
				element.push(data, fb);
			else
				break;
		}
		
	}


}
