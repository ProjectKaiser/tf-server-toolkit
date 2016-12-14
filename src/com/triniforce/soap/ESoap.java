/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import javax.xml.namespace.QName;

import org.xml.sax.SAXException;


public class ESoap {
	
	
	static class EInterfaceElementException extends RuntimeException{
		public EInterfaceElementException(String e) {
			super(e);
		}

		private static final long serialVersionUID = 7033589483728802780L;}
	// SoapHandler exceptions

	static class EElementNotFound extends SAXException{
	    private static final long serialVersionUID = -5654077158782927631L;
	
	    public EElementNotFound(QName e) {
	        super(e.toString());
	    }
	}

	static class EElementReentry extends EInterfaceElementException{
	    private static final long serialVersionUID = -2707949043156630746L;
	    public EElementReentry(String tag) {
	        super(tag.toString());
	    }
	}

	static class EMethodNotFound extends ESoap.EUnknownElement{
	    private static final long serialVersionUID = 4658888395760467956L;
	    public EMethodNotFound(String tag) {
	        super(tag);
	    }
	}

	static class ENonNullableObject extends EInterfaceElementException{
	    private static final long serialVersionUID = -7975756834097126342L;
	    public ENonNullableObject(String qn, String name) {
	        super(qn.toString()+":"+name);
	    }
	}

	static class EUnknownElement extends EInterfaceElementException{
	    private static final long serialVersionUID = -5654077158782927631L;
	
	    public EUnknownElement(String e) {
	        super(e);
	    }
	}
	
	static class EWrongElementType extends EInterfaceElementException{
	    private static final long serialVersionUID = -5654077158782927631L;
	
	    public EWrongElementType(String type) {
	        super(type);
	    }
	}
	
	// Interface parsing exceptions
	
	static class InvalidTypeName extends RuntimeException{
		private static final long serialVersionUID = -8279251685410807167L;

		public InvalidTypeName(String name) {
			super(name);
		}
	}
	
	static class EFaultCode extends RuntimeException{
		private static final long serialVersionUID = -4043284967003240415L;

		public EFaultCode(String faultstring) {
	        super(faultstring);
	    }
	}
	
	public static class EParameterizedException extends Exception{
		private static final long serialVersionUID = -5178999668250524620L;
		private String m_subcode;
		
		public EParameterizedException(){
			this("", null, "");
		}
		public EParameterizedException(String message, Throwable cause, String subcode) {
			super(message, cause);
			setSubcode(subcode);
		}

		public String getSubcode() {
			return m_subcode;
		}

		public void setSubcode(String subcode) {
			m_subcode = subcode;
		}
		
	} 

}
