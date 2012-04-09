/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.soap.InterfaceDescriptionGenerator.SoapHandler.CurrentObject;
import com.triniforce.soap.SoapHandlerTest.IService.Cls2;
import com.triniforce.soap.SoapHandlerTest.IService.Req1;
import com.triniforce.soap.SoapHandlerTest.IService2.C1;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDefLibCache.MapEntry;

public class SoapHandlerTest extends TFTestCase {

    interface IService{
        int method1(int v, String v2);
        
        static class Req1{
            private String m_v;
            public void setValue1(String v){
                m_v = v;
            }
            public String getValue1(){
                return m_v;
            }
        }
        
        public static class Cls2{
            private boolean m_var1;

            public boolean getVar1(){
                return m_var1;
            } 
            
            public void setVar1(boolean v){
                m_var1 = v;
            }
        }
        
        void method2(Req1 arg, List<Cls2> list1);
        
        void methodWithObj(Object obj, List<Object> lObj);
        
        Map methodMap(Map<String, Integer> v);
        
        void decimalMethod(BigDecimal v);
    }
    
    String REQ1 = 
    "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
    "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
    "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
    "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
    "  <soap:Body>"+
    "    <method1 xmlns=\"http://tempuri.org/\">" +
    "garbage"+
    "      <arg1>  \t string parameter 1   \t\r\n</arg1>"+
    "      <arg0>  \t 1800   \t\r\n</arg0>"+
    "    </method1>"+
    "  </soap:Body>"+
    "</soap:Envelope>";
    
    String REQ2_UNKNOWN_PARAMETER = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
        "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
        "  <soap:Body>"+
        "    <method1 xmlns=\"http://tempuri.org/\">"+
        "      <arg1>  \t string parameter 1   \t\r\n</arg1>"+
        "      <arg2>  \t unknown parameter   \t\r\n</arg2>"+
        "      <arg0>  \t 1800   \t\r\n</arg0>"+
        "    </method1>"+
        "  </soap:Body>"+
        "</soap:Envelope>";
    
    String REQ2 = 
        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
        "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
        "  <soap:Body>"+
        "    <method2 xmlns=\"http://tempuri.org/\">"+
        "       <arg0>" +
        "           <value1>" +
        "               Request value _0001" +
        "           </value1>" +
        "       </arg0>" +
        "       <arg1>" +
        "           <value>" +
        "               <var1>true</var1>" +
        "           </value>" +
        "           <value>" +
        "               <var1>0</var1>" +
        "           </value>" +
        "           <value xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" />" +
        "       </arg1>" +
        "    </method2>"+
        "  </soap:Body>"+
        "</soap:Envelope>";
    
    String REQ3 = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
        "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
        "  <soap:Body>"+
        "    <methodWithObj xmlns=\"http://tempuri.org/\">"+
        "      <arg0 xsi:type=\"Req1\">" +
        "           <value1> !!!Req1 value CONTENT!!!</value1>" +
        "      </arg0>"+
        "      <arg1>" +
        "           <value xsi:type=\"xsd:string\">String value in list</value>" +
        "           <value xsi:type=\"xsd:float\">0.112</value>" +
        "           <value xsi:nil=\"true\"/>" +
        "      </arg1>"+
        "    </methodWithObj>"+
        "  </soap:Body>"+
        "</soap:Envelope>";
    
    String REQ4 = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
        "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
        "  <soap:Body>"+
        "    <decimalMethod xmlns=\"http://tempuri.org/\">"+
        "      <arg0>0.4567</arg0>"+
        "    </decimalMethod>"+
        "  </soap:Body>"+
        "</soap:Envelope>";
        
    @SuppressWarnings("unchecked")
    public void test() throws Exception {
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        
        InterfaceDescription desc = gen.parse(null, IService.class);
        
        SOAPDocument res = gen.deserialize(desc, inSource(REQ1));
        assertEquals("method1", res.m_method);
        assertEquals(2, res.m_args.length);
        assertEquals(Integer.valueOf(1800), res.m_args[0]);
        assertEquals("  \t string parameter 1   \t\n",  res.m_args[1]);
        assertEquals("http://schemas.xmlsoap.org/soap/envelope/", res.m_soap);
        assertEquals(true, res.m_bIn);
        
        try{
            res = gen.deserialize(desc, inSource(
                    "    <method1 xmlns=\"http://tempuri.org/\">"+
                    "      <arg0>1800</arg0>"+
                    "    </method1>"));
            fail();
        } catch(ESoap.EUnknownElement e){
        }
        
        try{
            res = gen.deserialize(desc, inSource(REQ2_UNKNOWN_PARAMETER));
            fail();
        }catch(ESoap.EUnknownElement e){
        }
        
        res = gen.deserialize(desc, inSource(REQ2));
        assertEquals("method2", res.m_method);
        assertEquals(2, res.m_args.length);
        Req1 req =  (Req1) res.m_args[0];
        assertNotNull(req);
        assertEquals("               Request value _0001           ", req.getValue1());
        
        List<IService.Cls2> arg1 = (List<Cls2>) res.m_args[1];
        
        assertEquals(true,  arg1.get(0).getVar1());
        assertEquals(false, arg1.get(1).getVar1());
        assertEquals(null, arg1.get(2));
        
        res = gen.deserialize(desc, inSource(REQ3));
        Req1 req1 = (Req1) res.m_args[0];
        assertEquals(" !!!Req1 value CONTENT!!!", req1.getValue1());
        
        res = gen.deserialize(desc, inSource("<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "       xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"+
                "  <soap:Body>"+
                "    <methodWithObj xmlns=\"http://tempuri.org/\">"+
                "      <arg0 xsi:type=\"Req1\">" +
                "           <value1> !!!Req1 value CONTENT!!!</value1>" +
                "      </arg0>"+
                "      <arg1>" +
                "           <value xsi:type=\"xsd:string\">String value in list</value>" +
                "           <value xsi:type=\"xsd:float\">0.112</value>" +
                "           <value xsi:nil=\"true\"/>" +
                "      </arg1>"+
                "    </methodWithObj>"+
                "  </soap:Body>"+
                "</soap:Envelope>"));
        req1 = (Req1) res.m_args[0];
        assertEquals(" !!!Req1 value CONTENT!!!", req1.getValue1());
        
        res = gen.deserialize(desc, inSource(REQ4));
        assertEquals(new BigDecimal(0.4567, new MathContext(4)), res.m_args[0]);

    }
    
    private InputStream inSource(String req) {
        return new ByteArrayInputStream(req.getBytes());
    }

    static class TestCls1{
        private String m_v1;
        private String m_v2= "default value";
        
        public String getVariable1(){
            return m_v1;
        }
        public void setVariable1(String v){
            m_v1 = v;
        }
        
        public String getVariable2(){
            return m_v2;
        }
        public void setVariable2(String v){
            m_v2 = v;
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testCurrentObject() throws SAXException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException{
        
    	QName qn = new QName("test.com", "var_001");
        try{
            new CurrentObject(qn, null);
            fail();
        } catch(NullPointerException e){}
        
        //assertNull(new CurrentObject(null, true).toObject());
        
        CurrentObject obj = new CurrentObject(qn, new TypeDef.ScalarDef(int.class));
        try{
            obj.setCurrentProp("anything");
            fail();
        } catch(ESoap.EUnknownElement e){
            assertEquals(new QName("anything").toString(), e.getMessage());
        }
        
        assertNull(obj.toObject());
        
        obj.setStringValue("672742");
        assertEquals(672742, obj.toObject());
        obj.setStringValue("56202");
        assertEquals(56202, obj.toObject());
        
        try{
            obj.setStringValue(null);
            fail();
        } catch(NullPointerException e){}
        
        try{
            obj.setPropValue("anything");
            fail();
        } catch(RuntimeException e){
            assertEquals("ScalarDef", e.getMessage());
        }
        
        //check all scalars
        obj = new CurrentObject(qn, new ScalarDef(Boolean.class));
        checkBool(obj);
        obj = new CurrentObject(qn, new ScalarDef(Boolean.TYPE));
        checkBool(obj);
        obj = new CurrentObject(qn, new ScalarDef(Integer.class));
        obj = new CurrentObject(qn, new ScalarDef(Long.class));
        checkLong(obj);
        obj = new CurrentObject(qn, new ScalarDef(Long.TYPE));
        checkLong(obj);
        obj = new CurrentObject(qn, new ScalarDef(Short.class)); 
        checkShort(obj);
        obj = new CurrentObject(qn, new ScalarDef(Short.TYPE));
        checkShort(obj);
        obj = new CurrentObject(qn, new ScalarDef(Float.class));
        checkFloat(obj);
        obj = new CurrentObject(qn, new ScalarDef(Float.TYPE)); 
        checkFloat(obj);
        obj = new CurrentObject(qn, new ScalarDef(Double.class));
        checkDouble(obj);
        obj = new CurrentObject(qn, new ScalarDef(Double.TYPE)); 
        checkDouble(obj);
        obj = new CurrentObject(qn, new ScalarDef(String.class));
        obj.setStringValue("76yhfj\n\n\t\td;fjh");
        assertEquals("76yhfj\n\n\t\td;fjh", obj.toObject());
        obj = new CurrentObject(qn, new ScalarDef(String.class));
        assertEquals("", obj.toObject());
        
        obj = new CurrentObject(qn, new ScalarDef(BigDecimal.class));
        obj.setStringValue("0.32");
        assertEquals(new BigDecimal(0.32, new MathContext(2)), obj.toObject());
        
        try{
            obj = new CurrentObject(qn, new ScalarDef(double.class), true);
            fail();
        } catch(ESoap.ENonNullableObject e){
            assertEquals(qn.toString() + ":"+Double.TYPE.toString(), e.getMessage());
        }
        obj = new CurrentObject(qn, new ScalarDef(String.class), true);
        assertEquals(null, obj.toObject());
        
        obj.setStringValue("gsgdg");
        assertEquals(null, obj.toObject());
        
        
        TypeDefLibCache lib = new TypeDefLibCache(new ClassParser(this.getClass().getPackage()));
        TypeDef def = lib.add(TestCls1.class);

        obj = new CurrentObject(qn, def);
        
        // no effect
        obj.setStringValue("anything");
        
        try{
            obj.setCurrentProp("unknown_property");
            fail();
        } catch(ESoap.EUnknownElement e){}
        
        obj.setCurrentProp("variable1");
        obj.setPropValue("string value in var1");
        
        TestCls1 vObj = (TestCls1) obj.toObject();
        assertNotNull(vObj);
        assertEquals("string value in var1", vObj.getVariable1());
        assertEquals("default value", vObj.getVariable2());
        
        obj = new CurrentObject(qn, lib.add(Integer[].class));
        try{
            obj.setCurrentProp("unkProp");
            fail();
        } catch(ESoap.EUnknownElement e){}
        
        ScalarDef compType = (ScalarDef) obj.setCurrentProp("value");
        assertEquals(Integer.class.getName(), compType.getType());
        
        obj.setPropValue(128);
        List<Integer> res = (List<Integer>) obj.toObject();
        assertEquals(Integer.valueOf(128), res.get(0));
        
        obj.setCurrentProp("value");
        obj.setPropValue(762034);
        obj.setCurrentProp("value");
        obj.setPropValue(683);
        
        res = (List<Integer>) obj.toObject();
        assertEquals(Integer.valueOf(128), res.get(0));
        assertEquals(Integer.valueOf(762034), res.get(1));
        assertEquals(Integer.valueOf(683), res.get(2));
        
        obj = new CurrentObject(qn, def, true);
        assertNull(obj.toObject());

        
        // check primitive types for array
        obj = new CurrentObject(qn, lib.add(int[].class));
        obj.setCurrentProp("value");
        obj.setPropValue(875);
        assertEquals(875, ((List)obj.toObject()).get(0));
        
        obj = new CurrentObject(qn, lib.add(boolean[].class));
        obj.setCurrentProp("value");
        obj.setPropValue(false);
        assertEquals(false, ((List)obj.toObject()).get(0));

        obj = new CurrentObject(qn, lib.add(double[].class));
        obj.setCurrentProp("value");
        obj.setPropValue(6834.532);
        obj.setCurrentProp("value");
        obj.setPropValue(7885.66);
        assertEquals(6834.532, ((List)obj.toObject()).get(0));
        assertEquals(7885.66, ((List)obj.toObject()).get(1));
        
        Type t1 = IService.class.getMethod("methodMap", new Class[]{Map.class}).getGenericReturnType();
        obj = new CurrentObject(qn, lib.add(t1));
        Map res2 = (Map) obj.toObject();
        assertEquals(Collections.emptyMap(), res2);
        
        ClassDef cDef = (ClassDef)obj.setCurrentProp("value");
        assertEquals(MapEntry.class.getName(), cDef.getType());
        
        MapEntry entry = new TypeDefLibCache.MapEntry();
        entry.setKey("key1");;
        entry.setValue("value");
        obj.setPropValue(entry);
        
        
    }

    private void checkDouble(CurrentObject obj) {
        obj.setStringValue("2376.7634");
        assertEquals(2376.7634, obj.toObject());
        obj.setStringValue("10E76");
        assertEquals(10E76, obj.toObject());
    }

    private void checkFloat(CurrentObject obj) {
        obj.setStringValue("2376.7634");
        assertEquals(Float.valueOf("2376.7634"), obj.toObject());
        obj.setStringValue("10E5");
        assertEquals(10E5f, obj.toObject());        
    }

    private void checkShort(CurrentObject obj) {
        short v = 18;
        obj.setStringValue(Short.valueOf(v).toString());
        assertEquals(v, obj.toObject());

        try{
            obj.setStringValue("5237602");
            fail();
        } catch(NumberFormatException e){}
    }

    private void checkLong(CurrentObject obj) {
        obj.setStringValue("67234627376273447");
        assertEquals(67234627376273447L, obj.toObject());
    }

    private void checkBool(CurrentObject obj) {
        obj.setStringValue("true");
        assertEquals(Boolean.TRUE, obj.toObject());
        obj.setStringValue("false");
        assertEquals(Boolean.FALSE, obj.toObject());
        obj.setStringValue("0");
        assertEquals(Boolean.FALSE, obj.toObject());
        obj.setStringValue("1");
        assertEquals(Boolean.TRUE, obj.toObject());
    }
    
    public void testReentry() throws ParserConfigurationException, IOException, SAXException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IService.class);

        String REQ_Reentry = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "  <soap:Body>"+
            "    <method1 xmlns=\"http://tempuri.org/\">"+
            "      <arg0>" +
            "           6352639" +
            "      </arg0>"+
            "      <arg1>" +
            "           string1" +
            "      </arg1>"+
            "      <arg1>" +
            "           string1" +
            "      </arg1>"+
            "    </method1>"+
            "  </soap:Body>"+
            "</soap:Envelope>";
        
        try{
            gen.deserialize(desc, inSource(REQ_Reentry));
            fail();
        } catch(ESoap.EElementReentry e){
            assertEquals("arg1", e.getMessage());
        }
        
        String REQ_Reentry2 = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "  <soap:Body>"+
            "    <method1 xmlns=\"http://tempuri.org/\">"+
            "      <arg0>" +
            "           6352639" +
            "      </arg0>"+
            "      <arg1>" +
            "           string1" +
            "      </arg1>"+
            "    </method1>"+
            "    <method1 xmlns=\"http://tempuri.org/\">"+
            "      <arg0>" +
            "           6352639" +
            "      </arg0>"+
            "      <arg1>" +
            "           string1" +
            "      </arg1>"+
            "    </method1>"+
            "  </soap:Body>"+
            "</soap:Envelope>";
        try{
            gen.deserialize(desc, inSource(REQ_Reentry2));
            fail();
        } catch(ESoap.EElementReentry e){
            assertEquals(new QName("http://tempuri.org/", "method1").toString(), e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public void testMap() throws ParserConfigurationException, SAXException, IOException{
        String REQ_MAP = 
            "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
            "  <soap:Body>"+
            "    <methodMap xmlns=\"http://tempuri.org/\">"+
            "      <arg0>" +
            "           <value>" +
            "               <key>str1</key>" +
            "               <value>10021</value>" +
            "           </value>" +
            "           <value>" +
            "               <key>str2</key>" +
            "               <value>10028</value>" +
            "           </value>" +
            "      </arg0>"+
            "    </methodMap>"+
            "  </soap:Body>"+
            "</soap:Envelope>";   
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IService.class);
        SOAPDocument req = gen.deserialize(desc, inSource(REQ_MAP));
        Map<String, Integer> res = (Map<String, Integer>) req.m_args[0];
        assertEquals(2, res.size());
        assertEquals(Integer.valueOf(10021), res.get("str1"));
        assertEquals(Integer.valueOf(10028), res.get("str2"));
    }
    
    public void testSOAP() throws ParserConfigurationException, SAXException, IOException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IService.class);
        
        try{
            gen.deserialize(desc, inSource(
                    "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
                    "  <soap:Body>"+
                    "    <methodMap xmlns=\"http://tempuri.org/\">"+
                    "      <arg0>" +
                    "           <value>" +
                    "               <key>str1</key>" +
                    "               <value>10021</value>" +
                    "           </value>" +
                    "      </arg0>"+
                    "    </methodMap>"+
                    "  </soap:Body>" +
                    "<soap:Envelope/>"+
                    "</soap:Envelope>"   
            ));
            fail();
        }catch(ESoap.EUnknownElement e){
            assertEquals(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope").toString(), e.getMessage());
        }
        try{
            gen.deserialize(desc, inSource(
                    "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
                    "  <soap:Body>"+
                    "    <methodMap xmlns=\"http://tempuri.org/\">"+
                    "      <arg0>" +
                    "           <value>" +
                    "               <key>str1</key>" +
                    "               <value>10021</value>" +
                    "           </value>" +
                    "      </arg0>"+
                    "    </methodMap>"+
                    "  </soap:Body>" +
                    "  <soap:Body/>" +
                    "</soap:Envelope>"   
            ));
            fail();
        }catch(ESoap.EElementReentry e){
            assertEquals(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body").toString(), e.getMessage());
        }
        
        try{
            gen.deserialize(desc, inSource(
                    "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                    "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                    "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
                    "  <soap:Body>"+
                    "    <methodMap xmlns=\"http://tempuri.org/\">"+
                    "      <arg0>" +
                    "           <value>" +
                    "               <key>str1</key>" +
                    "               <value>10021</value>" +
                    "           </value>" +
                    "           <soap:Body/>" +
                    "      </arg0>"+
                    "    </methodMap>"+
                    "  </soap:Body>" +
                    "</soap:Envelope>"   
            ));
            fail();
        }catch(ESoap.EUnknownElement e){
            assertEquals(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body").toString(), e.getMessage());
        }
    }
    
    interface IService2{
        void method1(List<Integer> v1, Integer[] v2);
        
        static class C1{
            String arr1[];
            List<String> list;
            public String[] getArr1() {
                return arr1;
            }
            public void setArr1(String[] arr1) {
                this.arr1 = arr1;
            }
            public List<String> getList() {
                return list;
            }
            public void setList(List<String> list) {
                this.list = list;
            }
        }
        
        static class C2 extends C1{
        	
        }
        static class C3{
        	
        }
        
        void method2(C1 v);
        
        void method2(C2 v1, C3 v2);
    }
    
    @SuppressWarnings("unchecked")
    public void testDiffArrays() throws ParserConfigurationException, SAXException, IOException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IService2.class);
             
        
        SOAPDocument res = gen.deserialize(desc, inSource(
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
                "  <soap:Body>"+
                "    <method2 xmlns=\"http://tempuri.org/\">"+
                "      <arg0>" +
                "        <arr1>" +
                "              <value>10</value>" +
                "              <value>12</value>" +
                "        </arr1>"+
                "        <list>" +
                "              <value>1451</value>" +
                "              <value>1452</value>" +
                "        </list>"+
                "      </arg0>" +
                "    </method2>"+
                "  </soap:Body>" +
                "</soap:Envelope>"   
        ));
        IService2.C1 v = (C1) res.m_args[0];
        //list type
        assertTrue(Arrays.equals(new String[]{"10","12"}, v.getArr1()));
        //array type
        assertEquals(Arrays.asList("1451","1452"), v.getList());

        
        res = gen.deserialize(desc, inSource(
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
                "  <soap:Body>"+
                "    <method1 xmlns=\"http://tempuri.org/\">"+
                "      <arg0>" +
                "           <value>10</value>" +
                "           <value>12</value>" +
                "      </arg0>"+
                "      <arg1>" +
                "           <value>1451</value>" +
                "           <value>1452</value>" +
                "      </arg1>"+
                "    </method1>"+
                "  </soap:Body>" +
                "</soap:Envelope>"   
        ));
        
        //list type
        assertTrue(Arrays.asList(res.m_args[1]).toString(), Arrays.equals(new Integer[]{1451,1452}, (Object[])res.m_args[1]));
        //array type
        assertEquals(Arrays.asList(10,12), res.m_args[0]);
    }
    
    public void testWrongObjectType() throws ParserConfigurationException, SAXException, IOException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IService2.class);
             
        try{
	        gen.deserialize(desc, inSource(
	                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	                "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
	                "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
	                "  <soap:Body>"+
	                "    <method2 xmlns=\"http://tempuri.org/\">"+
	                "      <arg0 xsi:type=\"C3\">" +
	                "      </arg0>" +
	                "    </method2>"+
	                "  </soap:Body>" +
	                "</soap:Envelope>"   
	        ));
	        fail();
        } catch(ESoap.EWrongElementType e){
        	
        }
        
        SOAPDocument res = gen.deserialize(desc, inSource(
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
                "  <soap:Body>"+
                "    <method2 xmlns=\"http://tempuri.org/\">"+
                "      <arg0 xsi:type=\"C2\">" +
                "        <arr1>" +
                "              <value>10</value>" +
                "              <value>12</value>" +
                "        </arr1>"+
                "        <list>" +
                "              <value>1451</value>" +
                "              <value>1452</value>" +
                "        </list>"+
                "      </arg0>" +
                "    </method2>"+
                "  </soap:Body>" +
                "</soap:Envelope>"   
        ));
        assertEquals(IService2.C2.class, res.m_args[0].getClass());
        
        try{
	        gen.deserialize(desc, inSource(
	                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	                "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
	                "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
	                "  <soap:Body>"+
	                "    <method2 xmlns=\"http://tempuri.org/\">"+
	                "      <arg0 xsi:type=\"C2\">" +
	                "        <arr1>" +
	                "              <value xsi:type=\"C1\">10</value>" +
	                "              <value>12</value>" +
	                "        </arr1>"+
	                "        <list>" +
	                "              <value>1451</value>" +
	                "              <value>1452</value>" +
	                "        </list>"+
	                "      </arg0>" +
	                "    </method2>"+
	                "  </soap:Body>" +
	                "</soap:Envelope>"   
	        ));
	        fail();
        } catch(ESoap.EWrongElementType e){}
        
        try{
	        gen.deserialize(desc, inSource(
	                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	                "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
	                "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
	                "  <soap:Body>"+
	                "    <method2 xmlns=\"http://tempuri.org/\">"+
	                "      <arg0 xsi:type=\"xsd:string\">lalalal" +
	                "      </arg0>" +
	                "    </method2>"+
	                "  </soap:Body>" +
	                "</soap:Envelope>"   
	        ));
	        fail();
        } catch(ESoap.EWrongElementType e){}
    }
    
    public void testMultiString() throws ParserConfigurationException, SAXException, IOException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IService2.class);
             
        
        SOAPDocument res = gen.deserialize(desc, inSource(
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
                "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
                "  <soap:Body>"+
                "    <method2 xmlns=\"http://tempuri.org/\">"+
                "      <arg0>" +
                "        <arr1>" +
                "              <value>10\n11\n12</value>" +
                "        </arr1>"+
                "        <list>" +
                "              <value>1451</value>" +
                "        </list>"+
                "      </arg0>" +
                "    </method2>"+
                "  </soap:Body>" +
                "</soap:Envelope>"   
        ));
    	IService2.C1 v =  (C1) res.m_args[0];
    	assertEquals("10\n11\n12", v.arr1[0]);
    }
    
    public void testFaultcodeException() throws ParserConfigurationException, SAXException, IOException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IService2.class);
        try{
	        gen.deserialize(desc, inSource(
	        		"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
	        		"xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"+
	        		"   <soap:Body>"+
	        		"      <soap:Fault>"+
	        		"         <faultcode>soap:Server</faultcode>"+
	        		"         <faultstring>java.io.NotSerializableException: eu.untill.rs.rscore.MemTable$Row</faultstring>"+
	        		"         <detail/>"+
	        		"      </soap:Fault>"+
	        		"   </soap:Body>"+
	        		"</soap:Envelope>" 
	        ));
	        fail();
        } catch(ESoap.EFaultCode e){
        	assertEquals("java.io.NotSerializableException: eu.untill.rs.rscore.MemTable$Row", e.getMessage());
        }
    } 
    
    
    String REQ_SYMB = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
        "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
        "  <soap:Body>"+
        "    <method1 xmlns=\"http://tempuri.org/\">"+
        "      <arg1>s=\"a&gt;&lt;b\"</arg1>"+
        "      <arg0></arg0>"+
        "    </method1>"+
        "  </soap:Body>"+
        "</soap:Envelope>";
    
    String REQ_SYMB2 = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
        "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
        "  <soap:Body>"+
        "    <method1 xmlns=\"http://tempuri.org/\">"+
        "      <arg1>a\nb\nc</arg1>"+
        "      <arg0></arg0>"+
        "    </method1>"+
        "  </soap:Body>"+
        "</soap:Envelope>";
    String REQ_SYMB3 = 
        "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
        "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
        "  <soap:Body>"+
        "    <method1 xmlns=\"http://tempuri.org/\">"+
        "      <arg1>a\n>\r\nb</arg1>"+
        "      <arg0></arg0>"+
        "    </method1>"+
        "  </soap:Body>"+
        "</soap:Envelope>";
    
    public void testHttpCodedSymbols() throws ParserConfigurationException, SAXException, IOException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, IService.class);
        
        SOAPDocument res = gen.deserialize(desc, inSource(REQ_SYMB));
        String str = (String) res.m_args[1];
        assertEquals("s=\"a><b\"", str);
        res = gen.deserialize(desc, inSource(REQ_SYMB2));
        assertEquals("a\nb\nc", res.m_args[1]);
        res = gen.deserialize(desc, inSource(REQ_SYMB3));
        assertEquals("a\n>\nb", res.m_args[1]);
        
        assertEquals("         a        ", deserialize(gen, desc,"         a        "));

    }

	private String deserialize(InterfaceDescriptionGenerator gen,
			InterfaceDescription desc, String string) throws ParserConfigurationException, SAXException, IOException {
		
		String req = String.format( 
	        "<?xml version=\"1.0\" encoding=\"utf-8\"?> "+
	        "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
	        "       xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
	        "       xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"+
	        "  <soap:Body>"+
	        "    <method1 xmlns=\"http://tempuri.org/\">"+
	        "      <arg1>%s</arg1>"+
	        "      <arg0></arg0>"+
	        "    </method1>"+
	        "  </soap:Body>"+
	        "</soap:Envelope>", string);
        return (String) gen.deserialize(desc, inSource(req)).m_args[1];
	}
	
}
