/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceDescription;
import com.triniforce.soap.InterfaceDescriptionGenerator;
import com.triniforce.soap.WsdlDescription;
import com.triniforce.soap.TypeDef;
import com.triniforce.soap.InterfaceDescription.MessageDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.soap.WsdlDescription.WsdlType;
import com.triniforce.soap.WsdlDescription.WsdlType.Restriction;
import com.triniforce.soap.WsdlDescription.WsdlTypeElement;

public class InterfaceDescriptionTest extends TFTestCase {
    
    interface I1{
        void method();
        int method2(int [] in, String in2);
        char method3();
    } 

    @SuppressWarnings("unchecked")
    public void testGetWsdlTypes() {
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        WsdlDescription desc = gen.parse(null, I1.class).getWsdlDescription();
        
        Collection<WsdlTypeElement> typeElements = desc.getWsdlTypeElements();
        assertEquals(6, typeElements.size());
        
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
        assertEquals(2, types.size());
        
        
        WsdlType t  = getType(desc.getWsdlTypes(), "char");
        assertNotNull(t);
        Restriction r = t.getResriction();
        assertEquals("short", r.m_base.getName());
        assertFalse(t.isComplex());
        
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


}
