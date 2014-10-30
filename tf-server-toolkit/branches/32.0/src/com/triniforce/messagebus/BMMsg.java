/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.messagebus;

public class BMMsg extends BM{

	private final String m_url;

    public BMMsg(String url, Object data) {
		super(data);
        m_url = url;
	}

    public String getUrl() {
        return m_url;
    }

}
