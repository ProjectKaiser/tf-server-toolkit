/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.TypeDef.ArrayDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.MapDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.utils.ApiAlgs;

public class ClassParserTest extends TFTestCase {

    private ClassParser m_cp;
    private TypeDefLibCache m_lib;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_cp = new ClassParser(ClassParserTest.class.getPackage());
        m_lib = new TypeDefLibCache(m_cp);
    }
    
    static class C1{}

    interface C2{
        int getV1();
        void setV1(int v);
        
        boolean isBoolV1();
        void setBoolV1(boolean v);
    }

    interface C3{
        Integer getValue2();
        void setValue2(Integer v);
    }

    static class C4{
        // No properties 
        // just get (no name)
        public Integer get() {
            return null;
        }
        public void set(int v) {
        }
        
        // No set/get method
        public String getStr1() {
            return null;
        }
        public String getStr2() {
            return null;
        }
        public void setStr3() {
        }
        
        public Long getStatic1() {
            return null;
        }
        public static void setStatic1(Long v) {
        }

        public static Long getStatic2() {
            return null;
        }
        
        public void setStatic2(Long v) {
        }
    }
    
    interface C5{
        public static class C51{
            public int getVar1() {
                return 0;
            }
            public void setVar1(int v) {
            }
        }
        C51 getV51();
        void setV51(C51 v);
        C52 getV52();
        void setV52(C52 v);
        
        public static class C52 extends C51{
            public int getVar2() {
                return 0;
            }
            public void setVar2(int v) {
            }            
        }
        static class C53 extends C52{}
    }

    @PropertiesSequence(sequence = { "prop3", "prop1", "prop2", "prop4" })
    public static class CWithSeq{
    	String prop1, prop2, prop3, prop4, prop5;

		public String getProp1() {
			return prop1;
		}

		public void setProp1(String prop1) {
			this.prop1 = prop1;
		}

		public String getProp2() {
			return prop2;
		}

		public void setProp2(String prop2) {
			this.prop2 = prop2;
		}

		public String getProp3() {
			return prop3;
		}

		public void setProp3(String prop3) {
			this.prop3 = prop3;
		}

		public String getProp4() {
			return prop4;
		}

		public void setProp4(String prop4) {
			this.prop4 = prop4;
		}
		
		public String getProp5() {
			return prop5;
		}

		public void setProp5(String prop5) {
			this.prop5 = prop5;
		}
    }
    
    @Override
    public void test() throws Exception {

        ClassDef cDef = (ClassDef) m_cp.parse(C1.class, m_lib, null);
        assertEquals(Collections.emptyList(), cDef.getProps());

        cDef = (ClassDef) m_cp.parse(C2.class, m_lib, null);
        assertEquals(2, cDef.getProps().size());
        PropDef prop = cDef.getProp("v1");
        assertEquals("v1", prop.getName());
        ScalarDef scDef = (ScalarDef) prop.getType();
        assertEquals(int.class.getName(), scDef.getType());
        prop = cDef.getProp("boolV1");
        assertNotNull(prop);
        assertFalse(cDef.getProp("boolV1").getType().isNullable());

        cDef = (ClassDef) m_cp.parse(C3.class, m_lib, null);
        assertEquals(1, cDef.getProps().size());
        prop = cDef.getProps().get(0);
        assertEquals("value2", prop.getName());
        scDef = (ScalarDef) prop.getType();
        assertEquals(Integer.class.getName(), scDef.getType());

        cDef = (ClassDef) m_cp.parse(C4.class, m_lib, null);
        assertEquals(Collections.emptyList(), cDef.getProps());

        cDef = (ClassDef) m_cp.parse(C5.class, m_lib, null);
        prop = cDef.getProp("v51");
        assertEquals(C5.C51.class.getName(), prop.getRawType());
        
        cDef = (ClassDef) prop.getType();
        
        prop = cDef.getProp("var1");
        assertNotNull(prop);
        assertEquals("var1", prop.getName());
        
        assertEquals(C5.C51.class, C5.C52.class.getSuperclass());
        
        cDef = (ClassDef) m_lib.get(C5.C52.class);
        assertSame(m_lib.get(C5.C51.class), cDef.getParentDef());
        List<PropDef> props = cDef.getOwnProps();
        assertNotNull(props);
        assertNotNull(InterfaceDescriptionGenerator.findByName(props, "var2"));
        assertNull(InterfaceDescriptionGenerator.findByName(props, "var1"));
        
        props = cDef.getProps();
        assertNotNull(InterfaceDescriptionGenerator.findByName(props, "var2"));
        assertNotNull(InterfaceDescriptionGenerator.findByName(props, "var1"));
        
        cDef = (ClassDef) m_lib.get(C5.C51.class);
        try{
            cDef.getProps().add(new PropDef());
            fail();
        } catch(UnsupportedOperationException e){
            
        }
        cDef = (ClassDef) m_lib.get(C5.C52.class);
        try{
            cDef.getProps().add(new PropDef());
            fail();
        } catch(UnsupportedOperationException e){
            
        }
        
        cDef = (ClassDef) m_cp.parse(CWithSeq.class, m_lib, null);
        props = cDef.getOwnProps();
        assertEquals("prop3", props.get(0).getName());
        assertEquals("prop1", props.get(1).getName());
        assertEquals("prop2", props.get(2).getName());
        assertEquals("prop4", props.get(3).getName());
        assertEquals("prop5", props.get(4).getName());
        
    }
    
    public void testTypeDefLibCache(){
        TypeDef c1Def = m_lib.add(C1.class);
        assertNotNull(c1Def);

        TypeDef.ScalarDef sDef = (ScalarDef) m_lib.add(int.class);
        assertNotNull(sDef);
        
        
    }
    
    interface IL1{
        List<Integer> fun1();
        int[] fun2();
        Map<String, int[]> fun3();
        Map<Long, int[]> fun4();
        static class Cls1{}
        Map<String, Long> fun5();
    }
    
    public void testAddTypeDef() throws SecurityException, NoSuchMethodException{
        Type t1 = IL1.class.getMethod("fun2", (Class[])null).getGenericReturnType();
        ArrayDef arr = (ArrayDef) m_lib.add(t1);
        ScalarDef sc = (ScalarDef) arr.getComponentType();
        assertEquals(int.class.getName(), sc.getType());
        
        //test caching
        assertSame(arr, m_lib.add(int[].class));      
        
        int sz = m_lib.getDefs().size();
        TypeDef td = m_lib.add(Object.class);
        assertEquals(Object.class.getName(), td.getType());
        assertEquals(sz, m_lib.getDefs().size());
        arr = (ArrayDef) m_lib.add(Object[].class);
        assertEquals(Object.class.getName(), arr.getComponentType().getType());

        m_lib = new TypeDefLibCache(m_cp);
        
        t1 = IL1.class.getMethod("fun3", (Class[])null).getGenericReturnType();
        MapDef mapDef = (MapDef) m_lib.add(t1);
        assertEquals(Map.class.getName(), mapDef.getType());
        assertEquals("MapOfArrayOfIntByString", mapDef.getName());
        assertEquals(String.class.getName(), mapDef.getKeyDef().getType());
        assertEquals(int[].class.getName(), mapDef.getValueDef().getType());
        
        Collection<TypeDef> maps = m_lib.getDefs();
        assertEquals(2, maps.size());
        
        assertSame(mapDef, m_lib.add(t1));
        assertEquals(2, maps.size());
        
        t1 = IL1.class.getMethod("fun4", (Class[])null).getGenericReturnType();
        assertNotSame(mapDef, m_lib.add(t1));
        t1 = IL1.class.getMethod("fun5", (Class[])null).getGenericReturnType();
        assertNotSame(mapDef, m_lib.add(t1));
        maps = m_lib.getDefs();
        assertEquals(4, maps.size());

        assertEquals("MapOfLongByString", m_lib.add(t1).getName());
        
        mapDef = (MapDef) m_lib.add(Map.class);
        assertNotNull(mapDef);
        
        assertSame(m_lib.add(int.class), m_lib.get(int.class));
    }
    
    static class Cls1{}
    
    public void testTypeDefNames(){
        TypeDef td = m_lib.add(int.class);
        assertEquals("int", td.getName());
        assertEquals("string", m_lib.add(String.class).getName());
        assertEquals("object", m_lib.add(Object.class).getName());
        assertEquals("ArrayOfInt", m_lib.add(int[].class).getName());
        assertEquals("Cls1", m_lib.add(Cls1.class).getName());
    }
    
    interface INameRedef{
        Integer getV1();
        void setV1(Integer v);
        void setV1(String v);
        void setV2(Long v1, Long v2);
        Long getV2();
        void setV3();
    }
    
    public void testPropNameRedefinition(){
        ClassDef td = (ClassDef) m_lib.add(INameRedef.class);
        assertEquals(1, td.getProps().size());
    }
    
    public void testGetSeters(){
        Iterator<Method> res = m_cp.getSetters(INameRedef.class);
        assertEquals("setV1", res.next().getName());
        assertEquals("setV1", res.next().getName());
        assertEquals(null,    res.next());
    }
    
    public void testGetTypeDef(){
        assertNull(m_lib.get(Cls1.class));
        m_lib.add(Cls1.class);
        assertNotNull(m_lib.get(Cls1.class));
        assertNull(m_lib.get(int[].class));
        m_lib.add(int[].class);
        assertNotNull(m_lib.get(int[].class));
    }
    
    static class C11{
        int val1;
		private List<int[]> m_val2;

        public int getVal1() {
            return val1;
        }

        public void setVal1(int val1) {
            this.val1 = val1;
        }
        
        public List<int[]> getVal2(){
        	return m_val2;
        }
        
        public void setVal2(List<int[]> val2){
        	m_val2 = val2;
        }
    }
    
    public void testPropDef(){
        ClassDef cd = (ClassDef) m_lib.add(C11.class);
        C11 obj = new C11();
        PropDef prop = cd.getProp("val1"); 
        prop.set(obj, 41);
        assertEquals(41, prop.get(obj));
        
        prop = ((ArrayDef)cd.getProp("val2").getType()).getPropDef();
        ApiAlgs.getLog(this).trace(prop.getRawType());
        ArrayList<Object> l = new ArrayList<Object>();
        prop.set(l, Arrays.asList(123,534));

    }
    
    static class Cls101{
    	public static class Cls1010{
    	}
    	public class Cls1011{
    	}
    	static class Cls1012{
    	}
    }
    static class Cls1010{}
    
    public void testParseInnerType(){
     	assertEquals("Cls1010", m_lib.add(Cls101.Cls1010.class).getName());
     	
     	try{
     		ApiAlgs.getLog(this).trace(m_lib.add(Cls1010.class).getName());
     		fail();
     	} catch(ESoap.InvalidTypeName e){
     		assertTrue(e.getMessage(), e.getMessage().contains("Cls1010"));
     	}
    }
    
    public void testInnerTypesMustBeAlsoIncluded(){
    	m_lib.add(Cls101.class);
    	// class included
    	assertNotNull(m_lib.get(Cls101.Cls1010.class));
    	
    	// non static class
    	assertNull(m_lib.get(Cls101.Cls1011.class));
    	// non public class
    	assertNull(m_lib.get(Cls101.Cls1012.class));
    }
    
    public static class Cls201{
    	public String getProp1(){
			return null;
    	}
    }
    
    public void testReadOnlyProperty(){
    	
    }
    
    public static class COut{
    	public static class  CIn extends COut{}	
    }
    
    public void testInnerClss(){
    	m_lib.add(COut.class);
    }
}
