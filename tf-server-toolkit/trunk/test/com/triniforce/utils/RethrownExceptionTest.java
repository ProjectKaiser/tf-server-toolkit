/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.utils;

import java.io.StringWriter;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.WriterAppender;

public class RethrownExceptionTest extends TestCase {
	static StringWriter WRITER = new StringWriter();

	public static class TestAppender extends WriterAppender{
		public TestAppender() {
			setWriter(WRITER);
		}
	}
	
	
	public void testStackTrace(){
		Properties props = new Properties();
		props.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender.stdout.Target", "System.out");
		props.put("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
		props.put("log4j.appender.stdout.layout.ConversionPattern", "%t %d{yyyy-MM-dd HH:mm:ss} %-5p %c %x - %m%n");
		props.put("log4j.appender.testapp", "com.triniforce.utils.RethrownExceptionTest$TestAppender");
		props.put("log4j.appender.testapp.layout", "org.apache.log4j.SimpleLayout");
		props.put("log4j.rootLogger", "TRACE,stdout,testapp");

		PropertyConfigurator.configure(props);
		
		try{
			rethrowException();
		}catch(RuntimeException e){
//			incExpectedLogErrorCount(1);
			ApiAlgs.getLog(this).error("test", e);
			String s = WRITER.getBuffer().toString();
			assertFalse(s.isEmpty());
			System.out.println(s);
			assertFalse(s.contains("com.triniforce.utils.ApiAlgs$RethrownException: java.lang.Exception"));
		}
	}
	
	private void rethrowException(){
		try{
			throwException();
		}catch(Exception e){
			ApiAlgs.rethrowException(e);
		}
	}
	
	private void throwException() throws Exception{
		throw new Exception();
	}
}
