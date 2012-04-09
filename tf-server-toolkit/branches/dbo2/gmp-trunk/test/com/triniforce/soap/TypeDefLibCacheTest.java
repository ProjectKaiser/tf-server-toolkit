/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.TypeDef.ArrayDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.EnumDef;
import com.triniforce.soap.TypeDef.MapDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDefLibCache.ArrayDefLib;
import com.triniforce.soap.TypeDefLibCache.ClassDefLib;
import com.triniforce.soap.TypeDefLibCache.MapDefLib;
import com.triniforce.soap.TypeDefLibCache.MapEntry;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.soap.TypeDefLibCache.ScalarDefLib;
import com.triniforce.soap.TypeDefLibCacheTest.IA1.C1;
import com.triniforce.utils.ApiAlgs;

public class TypeDefLibCacheTest extends TFTestCase {

    private TypeDefLibCache m_lib;
    private ClassParser m_parser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        m_parser = new ClassParser(this.getClass().getPackage());
        m_lib = new TypeDefLibCache(m_parser);
        
        assertNotNull(m_lib.get(int.class));
        assertNotNull(m_lib.add(int.class));
    }
    
    static enum Set1{Val_1, Val_2};
    
    public void testScalarDefLib(){
        ScalarDefLib scLib = new TypeDefLibCache.ScalarDefLib();
    	{
	        ScalarDef sd = scLib.get(int.class);
	        assertNotNull(sd);
	        
	        assertNotNull(scLib.add(Object.class));
	        
	        sd = scLib.get(Date.class);
	        assertNotNull(sd);
	        assertTrue(sd.isNullable());
	        assertEquals("dateTime", sd.getName());
	        Date res = (Date) sd.valueOf("2002-10-10T00:00:00+05:00");
	        GregorianCalendar gc = new GregorianCalendar(2002, 9, 9, 19, 0, 0);
	        gc.setTimeZone(TimeZone.getTimeZone("GMT"));
	        assertEquals(res, gc.getTime());
    	}

    }
    
    public void testEnumDefLib(){
    	{
            HashMap<Type, TypeDef> clss = new HashMap<Type, TypeDef>();
    		ClassDefLib enumLib = new TypeDefLibCache.ClassDefLib(null, m_lib, clss, null);
    		ApiAlgs.getLog(this).trace(Set1.class.getName());
    		EnumDef res = (EnumDef) enumLib.add(Set1.class);
    		assertNotNull(res);
    		assertNull(enumLib.get(Integer.class));
    		assertEquals(Arrays.asList("Val_1", "Val_2"), Arrays.asList(res.getPossibleValues()));
    		
    		assertEquals(Set1.Val_1, res.valueOf("Val_1"));
    		assertEquals(Set1.Val_2, res.valueOf("Val_2"));
    		assertEquals(null, res.valueOf("Val_3"));
    		
    		assertEquals("Val_2", res.stringValue(Set1.Val_2));
    	}
    }
    
    interface IA1{
        class C1{} 
        List<C1> getV1();
    }
    
    public void testClassDefLib(){
        HashMap<Type, TypeDef> clss = new HashMap<Type, TypeDef>();
        ClassDefLib cLib = new TypeDefLibCache.ClassDefLib(m_parser, m_lib, clss, m_lib);
        ClassDef cd = (ClassDef) cLib.add(IA1.C1.class);
        assertNotNull(cd);
        assertSame(cd, cLib.add(IA1.C1.class));
        assertSame(cd, cLib.get(IA1.C1.class));
    }
    
    static class arraYoFfloaT{
    	
    }
    
    static class MapOfObjectByObject{}
    
    @SuppressWarnings("unchecked")
    public void testMapDefLib(){
        assertEquals("MapOfObjectByObject", m_lib.add(MapOfObjectByObject.class).getName());
        
        HashMap<TypeDef, ArrayDef> map = new HashMap<TypeDef, ArrayDef>();
        MapDefLib mapLib = new TypeDefLibCache.MapDefLib(m_lib, map, m_lib);
        
        MapDef md = (MapDef) mapLib.add(Map.class);
        assertNotNull(md);
        assertEquals("MapOfObjectByObject1", md.getName());
        assertSame(md, map.values().iterator().next());
        
        HashMap<Object, Object> obj = new HashMap<Object, Object>();
        obj.put(123, "str1");
        obj.put(124, "str2");
        obj.put(125, "str3");
        Collection<Map.Entry> entries = (Collection<Entry>) md.getPropDef().get(obj);
        
        HashMap<Integer, String> res = new HashMap<Integer, String>();
        for (Entry entry : entries) {
            res.put((Integer)entry.getKey(), (String)entry.getValue());   
        }
        assertEquals(3, res.size());
        assertEquals("str2", res.get(124));

        MapEntry entry = new TypeDefLibCache.MapEntry();
        entry.setKey(655);
        entry.setValue("newString");
        md.getPropDef().set(res, entry);
        assertEquals(4, res.size());
        
        
    }
    
    public void testArrayDefLib() throws SecurityException, NoSuchMethodException{
        HashMap<TypeDef, ArrayDef> map = new HashMap<TypeDef, ArrayDef>();
        ArrayDefLib arrLib = new TypeDefLibCache.ArrayDefLib(m_lib, map, m_lib);
        
        ArrayDef ad = arrLib.add(int[].class);
        assertNotNull(ad);
        assertSame(ad, arrLib.get(int[].class));
        assertSame(ad, arrLib.getDefs().iterator().next());
        
        ad = arrLib.add(IA1.class.getMethod("getV1", (Class[])null).getGenericReturnType());
        assertNotNull(m_lib.get(IA1.C1.class));
        
        assertEquals(IA1.C1[].class.getName(), ad.getType());
        
        PropDef prop = ad.getPropDef();
        assertNotNull(prop);
        assertEquals("value", prop.getName());
        
        List<C1> list = Arrays.asList(new IA1.C1(), new IA1.C1());
        assertSame(list, ad.getPropDef().get(list));
        
        Object[] arr = list.toArray();
        assertEquals(list, ad.getPropDef().get(arr));
        
        list = new ArrayList<C1>();
        C1 obj = new C1();
        ad.getPropDef().set(list, obj);
        assertEquals(1, list.size());
        
        ad = arrLib.add(boolean[].class);
        assertFalse(ad.getPropDef().getType().isNullable());
        Collection res = (Collection) ad.getPropDef().get(new int[]{0,1,2});
        assertEquals(Arrays.asList(0,1,2), res);
        
        assertEquals("arraYoFfloaT", m_lib.add(arraYoFfloaT.class).getName());
        assertEquals("ArrayOfFloat1", m_lib.add(float[].class).getName());
    }
}
