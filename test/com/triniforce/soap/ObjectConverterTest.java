/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceDescriptionGenerator.Node_S;
import com.triniforce.soap.InterfaceDescriptionGenerator.ObjectConverter;
import com.triniforce.soap.InterfaceDescriptionGenerator.ObjectConverter.TypedObject;
import com.triniforce.soap.TypeDefLibCache.PropDef;

public class ObjectConverterTest extends TFTestCase {

    static class Cls1{
        private String prop1;

        public Cls1(String prop1) {
            this.prop1 = prop1;
        }
        
        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }
        
        public String getProp1() {
            return prop1;
        }        
        
    }
    
    static class Cls2 extends Cls1{
        private String prop2;

        public Cls2(String prop1, String prop2) {
            super(prop1);
            this.prop2 = prop2;
        }
        
        public void setProp2(String prop) {
            this.prop2 = prop;
        }
        
        public String getProp2() {
            return prop2;
        }
    }
    
    @Override
    public void test() throws Exception {
    	{
	        TypeDefLibCache lib = new TypeDefLibCache(new ClassParser(this.getClass().getPackage(), Collections.EMPTY_MAP));
	        InterfaceDescription desc = new InterfaceDescription();
	        desc.getTypes().add(lib.add(Cls1.class));
	        desc.getTypes().add(lib.add(Cls2.class));
	        
	        ObjectConverter oc = new InterfaceDescriptionGenerator.ObjectConverter(desc, "TNS");
	        
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        dbf.setNamespaceAware(true);
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	
	        Element e = doc.createElement("test");
	        e.setAttribute("xmlns:xsi", InterfaceDescriptionGenerator.xsi);
	        doc.appendChild(e);
	        
	        Node_S parent = new InterfaceDescriptionGenerator.Node_S(e, null);
	        TypedObject val = new InterfaceDescriptionGenerator.ObjectConverter.TypedObject(
	                new PropDef("name", desc.getType(Cls1.class), null, null), new Cls2("property_from_Cls1", "property_from_Cls2"));
	        
	        oc.run(parent, val);
	        
	        TransformerFactory trf = TransformerFactory.newInstance();
	        Transformer t = trf.newTransformer();
	        t.setOutputProperty("indent", "yes");
	        t.transform(new DOMSource(doc), new StreamResult(System.out));
	        
	        XPathFactory xpf = XPathFactory.newInstance();  
	        XPath xp = xpf.newXPath();
	
	        Element prop = (Element) xp.evaluate("./name/prop1", e, XPathConstants.NODE);
	        assertEquals("property_from_Cls1", prop.getTextContent());
	        prop = (Element) xp.evaluate("./name/prop2", e, XPathConstants.NODE);
	        assertEquals("property_from_Cls2", prop.getTextContent());
    	}
        {
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        dbf.setNamespaceAware(true);
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        Document doc = db.newDocument();
	
	        Element e = doc.createElement("test");
	        e.setAttribute("xmlns:xsi", InterfaceDescriptionGenerator.xsi);
	        doc.appendChild(e);
	        
	        Node_S parent = new InterfaceDescriptionGenerator.Node_S(e, null);

	        InterfaceDescription desc = new InterfaceDescription();
        	ObjectConverter oc = new ObjectConverter(desc, "TNS");
        	
        	GregorianCalendar gc = new GregorianCalendar(2001, 0, 12, 21, 21, 12);
        	gc.setTimeZone(TimeZone.getTimeZone("GMT"));
        	Date dt = gc.getTime();
        	PropDef propDef = new PropDef("myTime", desc.getType(Date.class), Date.class.getName(), null);
        	TypedObject val = new TypedObject(propDef, dt);
        	oc.run(parent, val);
        	
        	
	        TransformerFactory trf = TransformerFactory.newInstance();
	        Transformer t = trf.newTransformer();
	        t.setOutputProperty("indent", "yes");
	        t.transform(new DOMSource(doc), new StreamResult(System.out));
	        
	        XPathFactory xpf = XPathFactory.newInstance();  
	        XPath xp = xpf.newXPath();
	        
	
	        Element prop = (Element) xp.evaluate("./myTime", e, XPathConstants.NODE);
	        assertEquals("2001-01-12T21:21:12.000Z", prop.getTextContent());
        }
    }
}
