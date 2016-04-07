/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;

public class ApiStackFiniter implements javax.servlet.Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
		ApiAlgs.getLog(this).trace("ApiStack cleaned");
		ApiStack.finitThreadLocal();
	}

	@Override
	public void destroy() {
	}

}
