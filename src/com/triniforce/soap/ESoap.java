/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import javax.xml.namespace.QName;

import org.xml.sax.SAXException;


public class ESoap {
	
	// SoapHandler exceptions

	static class EElementNotFound extends SAXException{
	    private static final long serialVersionUID = -5654077158782927631L;
	
	    public EElementNotFound(QName e) {
	        super(e.toString());
	    }
	}

	static class EElementReentry extends SAXException{
	    private static final long serialVersionUID = -2707949043156630746L;
	    public EElementReentry(QName name) {
	        super(name.toString());
	    }
	}

	static class EMethodNotFound extends ESoap.EUnknownElement{
	    private static final long serialVersionUID = 4658888395760467956L;
	    public EMethodNotFound(QName e) {
	        super(e);
	    }
	}

	static class ENonNullableObject extends SAXException{
	    private static final long serialVersionUID = -7975756834097126342L;
	    public ENonNullableObject(String name) {
	        super(name);
	    }
	}

	static class EUnknownElement extends SAXException{
	    private static final long serialVersionUID = -5654077158782927631L;
	
	    public EUnknownElement(QName e) {
	        super(e.toString());
	    }
	}
	
	static class EWrongElementType extends SAXException{
	    private static final long serialVersionUID = -5654077158782927631L;
	
	    public EWrongElementType(QName e, String type) {
	        super(e.toString());
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

}
