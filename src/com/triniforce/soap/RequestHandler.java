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
import java.lang.reflect.Method;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.utils.ApiAlgs;

public class RequestHandler {
    
    private InterfaceDescriptionGenerator m_gen;
    private InterfaceDescription m_desc;
    private Object m_service;

    public RequestHandler(
            InterfaceDescriptionGenerator gen, 
            InterfaceDescription desc,
            Object service) {
        m_gen = gen;
        m_desc = desc;
        m_service = service;
    } 

    public void exec(InputStream input, OutputStream output){
        String soapNS = null;
        try {
            SOAPDocument in = m_gen.deserialize(m_desc, input);
            soapNS = in.m_soap;
            Method m = getMethod(in.m_method);
            Object res = m.invoke(m_service, in.m_args);
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

    private Method getMethod(String name) throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(m_service.getClass());
        for (MethodDescriptor mDesc : info.getMethodDescriptors()) {
            if(mDesc.getName().equals(name)){
                return mDesc.getMethod();
            }
        }
        return null;
    }
}
