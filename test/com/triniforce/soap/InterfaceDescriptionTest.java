/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.ESoap.EParameterizedException;
import com.triniforce.soap.InterfaceDescription.MessageDef;
import com.triniforce.soap.InterfaceDescription.Operation;
import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.soap.InterfaceDescriptionTest.IWithError.EError2;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.soap.WsdlDescription.WsdlType;
import com.triniforce.soap.WsdlDescription.WsdlType.Restriction;
import com.triniforce.soap.WsdlDescription.WsdlTypeElement;
import com.triniforce.utils.ApiAlgs;

public class InterfaceDescriptionTest extends TFTestCase {
    
    interface I1{
        void method();
        int method2(int [] in, String in2);
        char method3();
        
        void method4(Map<String, Object> arg0);
    } 

    @SuppressWarnings("unchecked")
    public void testGetWsdlTypes() {
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        WsdlDescription desc = gen.parse(null, I1.class).getWsdlDescription();
        
        Collection<WsdlTypeElement> typeElements = desc.getWsdlTypeElements();
        assertEquals(8, typeElements.size());
        
        WsdlTypeElement t1 = getElement(desc.getWsdlTypeElements(), "method");
        assertEquals("method", t1.getName());
        WsdlType wt = t1.getType();
        assertTrue(wt.getElements().isEmpty());
        
        Collection<WsdlTypeElement> elements = desc.getWsdlTypeElements();
        t1 = getElement(elements, "methodResponse");
        wt = t1.getType();
        assertTrue(wt.getElements().isEmpty());
        
        t1 = getElement(desc.getWsdlTypeElements(), "method2");
        WsdlType msgType = t1.getType();
        WsdlTypeElement e1 = getElement(msgType.getElements(), "arg0");
        assertNotNull(e1);
        wt = e1.getType();
        assertEquals(0, e1.getMinOccur());
        assertEquals(1, e1.getMaxOccur());
       
        e1 = getElement(msgType.getElements(), "arg1");
        wt = e1.getType();
        assertEquals(0, e1.getMinOccur());
        assertEquals(1, e1.getMaxOccur());
        ScalarDef sd = (ScalarDef) wt.getTypeDef();
        assertEquals(String.class.getName(), sd.getType());
      
        t1 = getElement(desc.getWsdlTypeElements(), "method2Response");
        msgType = t1.getType(); 
        e1 = getElement(msgType.getElements(), "method2Result");
        assertNotNull(e1);
        wt = e1.getType();
        assertEquals(1, e1.getMinOccur());
        assertEquals(1, e1.getMaxOccur());
        
        Collection<WsdlType> types = desc.getWsdlTypes();
        assertEquals(4, types.size());
        
        
        WsdlType t  = getType(desc.getWsdlTypes(), "char");
        assertNotNull(t);
        Restriction r = t.getResriction();
        assertEquals("short", r.m_base.getName());
        assertFalse(t.isComplex());
        
        {
            t  = getType(desc.getWsdlTypes(), "MapOfObjectByString");
            e1 = t.getElements().iterator().next();
            assertTrue(e1.isResidentType());
            t  = getType(desc.getWsdlTypes(), "MapEntryObjectByString");
            assertNotNull(t);
        }
        
    }

    private WsdlType getType(Collection<WsdlType> wsdlTypes, String name) {
        for(WsdlType type : wsdlTypes){
        	trace(type.getTypeDef().getName());
        	if(name.equals(type.getTypeDef().getName())){
        		return type;
        	}
        }
        return null;
	}

	private WsdlTypeElement getElement(Collection<WsdlTypeElement> wsdlTypeElements, String name) {
        for (WsdlTypeElement element : wsdlTypeElements) {
            if(element.getName().equals(name))
                return element;
        }
        return null;
    }
    
    @SoapInclude(extraClasses={I2.Cls1.class})
    interface I2{
        static class Cls1{
        }
    } 

    
    @SuppressWarnings("unchecked")
    public void testGetTypeByName(){
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, I2.class);
        
        TypeDef td = desc.getTypeDef("Cls1", false);
        assertNotNull(td);
        assertEquals(I2.Cls1.class.getName(), td.getType());
        
        td = desc.getTypeDef("int", true);
        assertNotNull(td);
        ScalarDef sd = (ScalarDef) td; 
        assertEquals(Integer.class.getName(), sd.getType());
        
        td = desc.getTypeDef("string", true);
        assertNotNull(td);
        sd = (ScalarDef) td; 
        assertEquals(String.class.getName(), sd.getType());

        td = desc.getTypeDef("short", true);
        assertNotNull(td);
        sd = (ScalarDef) td; 
        assertEquals(Short.class.getName(), sd.getType());
    }
    
    interface IWithError{
    	public static class EError1 extends EParameterizedException{
			private static final long serialVersionUID = 1L;}
    	public static class EError2 extends EParameterizedException{
			private static final long serialVersionUID = 1L;}
    	
    	void method() throws EError1, EError2;
    	void method2() throws EError2;
    }
    
    static class EError3 extends EError2{
		private static final long serialVersionUID = 1L;
    }
    public void testError(){
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IWithError.class);
        
        Operation op1 = desc.getOperation("method");
        List<ClassDef> res = op1.getThrows();
        assertNotNull(res);
        
        TypeDef td = desc.getType(IWithError.EError1.class);
        assertNotNull(td);
        
        PropDef pd = op1.getThrowByType(IWithError.EError1.class);
        assertNotNull(pd);
        assertEquals("EError1", pd.getName());
        assertEquals("EError1", pd.getType().getName());
        pd = op1.getThrowByType(IWithError.EError2.class);
        assertEquals("EError2", pd.getType().getName());
        pd = op1.getThrowByType(EError3.class);
        assertEquals("EError2", pd.getType().getName());
        

    }
    
    @SuppressWarnings("unchecked")
    public void testMessageDef(){
        MessageDef mDef = new InterfaceDescription.MessageDef("testName1");
        assertEquals("testName1", mDef.getName());
        assertEquals(Collections.emptyList(), mDef.getProps());
        assertEquals(Object[].class.getName(), mDef.getType());

        mDef = new InterfaceDescription.MessageDef("testName1");
        mDef.addParameter("firstArgument", int.class, new TypeDef(null, null));
        mDef.addParameter("secondArgument", String[].class, new TypeDef(null, String[].class));
        
        assertEquals(2, mDef.getProps().size());
        PropDef propDef = mDef.getProps().get(0);
        assertEquals("firstArgument", propDef.getName());
        assertEquals(int.class.getName(), propDef.getRawType());
        // test getter and setter
        Object[] obj = new Object[2];
        propDef.set(obj, 577);
        assertEquals(577, propDef.get(obj));
        propDef = mDef.getProps().get(1);
        assertEquals("secondArgument", propDef.getName());
        assertEquals(String[].class.getName(), propDef.getRawType());
        assertEquals(null, propDef.get(obj));
        propDef.set(obj, Arrays.asList("thwogisd", "762346"));
        assertEquals(Arrays.asList("thwogisd", "762346"), propDef.get(obj));
     
        assertEquals(Object[].class.getName(), mDef.getType());
    }
    
    static class Cs1{
    	private String m_prop1;
		private String m_prop2;

		public String getProp2() {
			return m_prop2;
		}

		public void setProp2(String prop2) {
			m_prop2 = prop2;
		}

		public String getProp1() {
			return m_prop1;
		}

		public void setProp1(String prop1) {
			m_prop1 = prop1;
		}
    }
    
    interface ITest1{
    	Cs1 method_003();
    	Cs1 method_004();
    }
    
    public void testGetType() throws XPathExpressionException, ClassNotFoundException, IOException{
        XPathFactory xpf = XPathFactory.newInstance();  
        XPath xp = xpf.newXPath();
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, ITest1.class);
        
        desc.ignoreProperty("method_003", false, "method_003Result", "prop1");

        
        Cs1 value1 = new Cs1();
        value1.m_prop1 = "str0012";
        value1.m_prop2 = "str0014";
        SOAPDocument soap = new InterfaceDescriptionGenerator.SOAPDocument();
        soap.m_bIn = false;
        soap.m_args = new Object[]{value1};
        soap.m_method = "method_003";
        
        Document res = gen.serialize(desc, soap);       
        print(res);
        assertNotNull( xp.evaluate("//prop2", res, XPathConstants.NODE));
        assertNull(    xp.evaluate("//prop1", res, XPathConstants.NODE));
        
        soap.m_method = "method_004";
        Document res2 = gen.serialize(desc, soap);       
        print(res2);
        assertNotNull( xp.evaluate("//prop2", res2, XPathConstants.NODE));
        assertNotNull( xp.evaluate("//prop1", res2, XPathConstants.NODE));
    }
    
    private void print(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer t = tf.newTransformer();
            t.setOutputProperty("indent", "yes");
            t.transform(new DOMSource(doc), new StreamResult(System.out));
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
    }
    
    static class CS0{
    	List<Integer> m_list;
    	String m_ign001;
		public List<Integer> getList() {
			return m_list;
		}
		public void setList(List<Integer> list) {
			m_list = list;
		}
		public String getIgn001() {
			return m_ign001;
		}
		public void setIgn001(String ign001) {
			m_ign001 = ign001;
		}    	
    }

    interface IFieldIgnore{
    	CS0 method__001();
    }
    
    public void testIgnoreField() throws ClassNotFoundException, IOException{
    	InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IFieldIgnore.class);
        desc.ignoreProperty("method__001", false, "method__001Result", "ign001");
        
        SOAPDocument doc = new SOAPDocument();
        doc.m_method = "method__001";
        doc.m_args = new Object[]{
        		new CS0(){{
        			setList(Arrays.asList(1,2,3));
        			setIgn001("ignored");
        		}}
        };
        Document res = gen.serialize(desc, doc);
        print(res);
    }

}
