/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.utils.ApiAlgs;

class SoapHandler extends DefaultHandler{
    
    static class FaultMessage{
    	String m_string;
    	String m_code;
    }
    
    Stack<QName> m_stk = new Stack<QName>();
    private String m_tns;
    //private ClassDef m_methodDef;
    private HashMap<String, String> m_uriMap;
    
//    SOAPDocument m_result;
    String m_soap;
    
    FaultMessage m_fault=null;
    
    SAXHandler m_saxHandler;
    
    public SoapHandler(String tns, InterfaceDescription desc, Map<String, Boolean> configuration) {
        m_tns = tns;
        m_uriMap = new HashMap<String, String>();
        m_saxHandler = new SAXHandler(desc, configuration);
    }
    
    @Override
    public void startElement(String ns, String tag, String arg2, Attributes attrs) throws SAXException {
        QName qn = new QName(ns, tag);
        
        if(m_stk.isEmpty()){
            // must be soap
            if(InterfaceDescriptionGenerator.soapenv.equals(ns) || InterfaceDescriptionGenerator.soapenv12.equals(ns)){
                m_soap = ns;
            }
            else{
                throw new ESoap.EUnknownElement(qn.toString());
            }
        }

        m_saxHandler.m_stringValue.clear();
        
        if(ns.equals(m_soap)){
            if(tag.equals("Envelope")){
                if(!m_stk.isEmpty())
                    throw new ESoap.EUnknownElement(qn.toString());
            }
            else if(tag.equals("Body")){
                if(!m_stk.peek().equals(new QName(m_soap, "Envelope")))
                    throw new ESoap.EUnknownElement(qn.toString());
                if(null != m_saxHandler.m_method)
                    throw new ESoap.EElementReentry(qn.toString());
            }
            else if(tag.equals("Fault")){
                if(!m_stk.peek().equals(new QName(m_soap, "Body")))
                    throw new ESoap.EUnknownElement(qn.toString());
                m_fault = new FaultMessage();
            }
        }
        else if(ns.equals(m_tns)){
        	TypeDef td = null;
        	String typeName = attrs.getValue(InterfaceDescriptionGenerator.xsi, "type");
        	if(null != typeName){
        		qn = getQName(typeName);
        		td = m_saxHandler.getType(qn.getLocalPart(), !m_tns.equals(qn.getNamespaceURI()));
        		ApiAlgs.assertNotNull(td, typeName);
        	}
            m_saxHandler.startElement(tag, isNull(attrs), td);
        }
        else{
        	if(!(m_fault != null && 
        			("faultcode".equals(tag) || "faultstring".equals(tag) || "detail".equals(tag))))
        		throw new ESoap.EUnknownElement(qn.toString());
        }
        
        m_stk.push(qn);
    }

	private QName getQName(String typeName) {
        int iDiv = typeName.indexOf(':');
        String prefix = "";
        String local = typeName;
        if(iDiv!=-1){
            prefix = typeName.substring(0, iDiv);
            local = typeName.substring(iDiv+1);
        }
        QName res = new QName(m_uriMap.get(prefix), local, prefix);
        return res;
    }

    private boolean isNull(Attributes attrs) {
        boolean res = false;
        String strNull = attrs.getValue(InterfaceDescriptionGenerator.xsi, "nil");
        if(null != strNull){
            res = strNull.equals("true");
        }
        return  res;
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    	m_saxHandler.characters(ch, start, length);    	
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(uri.equals(m_tns)){
        	m_saxHandler.endElement();
        }
        if(m_fault != null){
        	if("faultstring".equals(localName)){
        		m_fault.m_string = m_saxHandler.getNodeCharacters();
        	}
        }
    	m_saxHandler.m_stringValue.clear();
        m_stk.pop();
    }
    
	@Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        m_uriMap.put(prefix, uri); 
        super.startPrefixMapping(prefix, uri);
    }

	public SOAPDocument getResult() {
		SOAPDocument result = new SOAPDocument();
		result.m_soap = m_soap;
		result.m_method = m_saxHandler.m_method;
		result.m_bIn = m_saxHandler.m_bIn;
		result.m_args = m_saxHandler.m_args;
		return result;
	}
	
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		ApiAlgs.getLog(this).trace("Invalid request", e);
		super.fatalError(e);
	}
}
