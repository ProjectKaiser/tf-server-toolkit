/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.utils.ApiAlgs;

public class RequestHandler {
	
	public interface IServiceInvoker{
		Object invokeService(String method, Object...args);
	}
	
	public static class ReflectServiceInvoker implements IServiceInvoker{
		private Object service;
		public ReflectServiceInvoker(Object service) {
			this.service = service;
		}
		public Object invokeService(String method, Object... args) {
			try {
				return invokeService(getMethod(method), args);
			} catch (IntrospectionException e) {
				ApiAlgs.rethrowException(e);
				return null;
			}
		}

		public Object invokeService(Method m, Object... args) {
			try {
	            return m.invoke(service, args);
			} catch (Exception e) {
				ApiAlgs.rethrowException(e);
				return null;
			}
		}
		
	    public Method getMethod(String name) throws IntrospectionException {
	        BeanInfo info = Introspector.getBeanInfo(service.getClass());
	        for (MethodDescriptor mDesc : info.getMethodDescriptors()) {
	            if(mDesc.getName().equals(name)){
	                return mDesc.getMethod();
	            }
	        }
	        return null;
	    }
	}
    
    private InterfaceDescriptionGenerator m_gen;
    private InterfaceDescription m_desc;
    private IServiceInvoker m_invoker;
    
    public RequestHandler(
            InterfaceDescriptionGenerator gen, 
            InterfaceDescription desc,
            IServiceInvoker service) {
        m_gen = gen;
        m_desc = desc;
        m_invoker = service;
    } 

    public void exec(InputStream input, OutputStream output){
        String soapNS = null;
        try {
            SOAPDocument in = m_gen.deserialize(m_desc, input);
            soapNS = in.m_soap;
            Object res = m_invoker.invokeService(in.m_method, in.m_args);
            SOAPDocument out = new SOAPDocument();
            out.m_soap = soapNS;
            out.m_method = in.m_method;
            out.m_bIn = false;
            out.m_args = new Object[]{res};
            Document doc = m_gen.serialize(m_desc, out);
            m_gen.writeDocument(output, doc);
        } catch (Throwable e) {
            try {
            	e.printStackTrace(System.out);
                m_gen.writeDocument(output, m_gen.serializeException(soapNS, e));
            } catch (TransformerException e1) {
                ApiAlgs.rethrowException(e1);
            }
        }
    }

	public InterfaceDescriptionGenerator getGen() {
		return m_gen;
	}

	public InterfaceDescription getDesc() {
		return m_desc;
	}

	public IServiceInvoker getInvoker() {
		return m_invoker;
	}

	public void execJson(InputStream input, OutputStream output) {
        String str = null;
        try{
			try {
	            SOAPDocument in = m_gen.deserializeJson(m_desc, input);
	            Object res = m_invoker.invokeService(in.m_method, in.m_args);
	            str = m_gen.serializeJson(m_desc, res);
	        } catch (Throwable e) {
	            str = m_gen.serializeJsonException(e);
	        }
			finally{
	            OutputStreamWriter writer = new OutputStreamWriter(output, Charset.forName("UTF-8"));
	            writer.write(str);
	            writer.close();
			}
        } catch(Exception e){
        	ApiAlgs.rethrowException(e);
        }
	}


}
