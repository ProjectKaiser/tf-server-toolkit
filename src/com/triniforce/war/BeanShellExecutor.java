/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import bsh.Interpreter;

import com.triniforce.utils.ApiStack;
import com.triniforce.utils.TFUtils;
import com.triniforce.war.api.IBasicDiag;
import com.triniforce.war.api.IBasicServerConfig;

public class BeanShellExecutor {
	
	public static final String stdScript = TFUtils.readResource(BeanShellExecutor.class, BeanShellExecutor.class.getSimpleName() + "Script.bsh");
	private String m_pwd;
    
	public BeanShellExecutor() {
	}
	
	public String execBeanShell(String upwd, String script){
		IBasicServerConfig cfg = ApiStack.getInterface(IBasicServerConfig.class);
		m_pwd = cfg.getProperties().getProperty(IBasicServerConfig.SCRIPT_PWD_KEY, "");
		if("".equals(m_pwd) || !m_pwd.equals(upwd))
			throw new EAuthException();
		return exec(script);
	}
	
	private String exec(String script){
        Interpreter interpreter = new Interpreter();
        interpreter.setStrictJava(true);
        try {
        	interpreter.eval(stdScript);
        	interpreter.set("diag", ApiStack.getInterface(IBasicDiag.class));
            interpreter.eval(script);                
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            String encName = "utf-8";//$NON-NLS-1$
            PrintStream newOut = new PrintStream(buf, true, encName);
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            try {
                System.setOut(newOut);
                System.setErr(newOut);
                Object res = interpreter.eval(script);
                newOut.flush();
                String resStr = buf.toString();
                if(null != res){
                	resStr+=res;
                }
              	return resStr;
            } finally {
                System.setErr(oldErr);
                System.setOut(oldOut);
            }
        } catch (Exception e) {
            return e.toString();
        }

	}
}
