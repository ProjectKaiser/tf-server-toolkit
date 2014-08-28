/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.simple.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.ESoap.EMethodNotFound;
import com.triniforce.soap.ESoap.InvalidTypeName;
import com.triniforce.soap.InterfaceDescription.Operation;
import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.soap.InterfaceDescriptionGeneratorTest.Cls2.InnerObject;
import com.triniforce.soap.TypeDef.ArrayDef;
import com.triniforce.soap.TypeDef.ClassDef;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.soap.TypeDefLibCache.PropDef;
import com.triniforce.soap.testpkg_01.Hand;
import com.triniforce.soap.testpkg_01.ITestHorse;
import com.triniforce.soap.testpkg_02.ITestBird;
import com.triniforce.soap.testpkg_02.ITestCow;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IName;

public class InterfaceDescriptionGeneratorTest extends TFTestCase {
    
    interface TestSrv1{}
    
    static class ExtraInc{
        String Val;
        public void setVal(String val) {
            Val = val;
        }
        public String getVal() {
            return Val;
        }
    }
    
    @SoapInclude(extraClasses={ExtraInc.class, Cls1.class, Cls2.class})
    interface TestSrv2{
        void method1();
        void method2();
        int  method3(String v1);
        void method4(int arg);
        void method5(int[] arg);
        void method6(int arg0, int arg1);
    }
    
    static class Cls1{
        private List<int[]> m_v1;
		public List<int[]> getV1(){
            return m_v1;
        }
        public void setV1(List<int[]> v){
            m_v1 = v;
        }
        
        public Map<String, Integer> getVMap(){
            return null;
        }
        public void setVMap(Map<String, Integer> v){
            
        }
    }
    
    static class Cls2{
        private String m_VV;
        private Object m_VObj;
        private List<String> m_list;
        private Object[] m_arr1;
        private Map<Float, InnerObject> m_map1;
        public enum MyEnm1{ENM1, ENM2};
        private MyEnm1 m_enumValue;
        private List<List<String>> m_listOfList;
        
        static class InnerObjectParent{
            int prop1;
            public void setProp1(int prop1) {
                this.prop1 = prop1;
            }
            public int getProp1() {
                return prop1;
            }
        }
        static class InnerObject extends InnerObjectParent{
            int prop2;
            public void setProp2(int prop2) {
                this.prop2 = prop2;
            }
            public int getProp2() {
                return prop2;
            }
        }
        public String getVV(){
            return m_VV;
        } 
        public void setVV(String v){
            m_VV = v;
        }
        
        public Object getVObj(){
            return m_VObj;
        } 
        public void setVObj(Object v){
            m_VObj = v;
        }
        
        public List<String> getList1(){
            return m_list;
        }
        public void setList1(List<String> v){
            m_list = v;
        }

        public Object[] getArray1(){
            return m_arr1;
        }
        public void setArray1(Object v[]){
            m_arr1 = v;
        }
        
        
        public Map<Float, InnerObject> getMap1(){
            return m_map1;
        }
        public void setMap1(Map<Float, InnerObject> v){
            m_map1 = v;
        }
		public void setEnumValue(MyEnm1 enumValue) {
			m_enumValue = enumValue;
		}
		public MyEnm1 getEnumValue() {
			return m_enumValue;
		}
		public void setListOfList(List<List<String>> listOfList) {
			m_listOfList = listOfList;
		}
		public List<List<String>> getListOfList() {
			return m_listOfList;
		}
    }
    
    @SoapInclude(extraClasses={Cls2.InnerObject.class})
    interface TestSrv3{
        Cls2 run1(Cls1 req);
        
        Long run2();
        
        char run3();
    }
    
    static Order DEFAULT_ORDER = null; 

    @SuppressWarnings("unchecked")
    public void testParse() throws Exception {
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription sd = gen.parse(null, TestSrv1.class);
        assertEquals(Collections.emptyList(), sd.getOperations());
        
        sd = gen.parse(null, TestSrv2.class);
        assertNotNull(findByName("method1", sd.getOperations()));
        assertNotNull(findByName("method2", sd.getOperations()));
        assertNotNull(findByName("method3", sd.getOperations()));

        Operation op = findByName("method3", sd.getOperations());
        ClassDef req = op.getRequestType();
        ScalarDef scd = (ScalarDef)req.getProps().get(0).getType();
        assertEquals(String.class.getName(), scd.getType());
        scd = (ScalarDef) op.getResponseType().getProps().get(0).getType();
        assertEquals(int.class.getName(), scd.getType());
        
        op = findByName("method2", sd.getOperations());
        assertTrue(op.getResponseType().getProps().isEmpty());
                
        op = findByName("method3", sd.getOperations());
        ClassDef t1 = op.getRequestType();
        assertEquals("arg0", t1.getProps().get(0).getName());
        
        t1 = op.getResponseType();
        assertEquals("method3Result", t1.getProps().get(0).getName());
        
        ClassDef cDef = (ClassDef) sd.getType(ExtraInc.class);
        assertNotNull(cDef);
        assertSame(cDef, sd.getTypeDef("ExtraInc", false));
        
        
        
        {
            sd = gen.parse(null, TestSrv3.class);
            
            op = sd.getOperation("run1");
            assertNotNull(op.getRequestType());
            ClassDef cd = (ClassDef) op.getRequestType().getProps().get(0).getType();
            assertEquals("v1", cd.getProp("v1").getName());
            ArrayDef ad = (ArrayDef) cd.getProp("v1").getType();
            ad = (ArrayDef) ad.getComponentType();
            scd = (ScalarDef) ad.getComponentType();
            assertEquals(int.class.getName(), scd.getType());
            //assertEquals(2, tl.getArrays().size());
            assertNotNull(sd.getType(int[].class));
        }
    }
    
    
    <T extends IName>
    T findByName(String opName, List<T> vals) {
        for (T name : vals) {
            if(name.getName().equals(opName))
                return name;
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    public void testGenerateWSDL() throws XPathExpressionException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, TestSrv3.class);
        Document doc = gen.generateWSDL(desc.getWsdlDescription(), "http:\\localhost:8080\test");
        
        print(doc);
        
        XPathFactory xpf = XPathFactory.newInstance();  
        XPath xp = xpf.newXPath();

        Element defs = (Element) xp.evaluate("/definitions", doc, XPathConstants.NODE);
        assertNotNull(defs);
        
        
        assertEquals("http://tempuri.org/", defs.getAttribute("targetNamespace"));

        Element svc = (Element) xp.evaluate("./service", defs, XPathConstants.NODE);
        assertEquals("ServerName", svc.getAttribute("name"));
        
        Element e = (Element)xp.evaluate("./types/schema/complexType[@name=\'InnerObject\']", defs, XPathConstants.NODE);
        assertNotNull(e);
        e = (Element)xp.evaluate("./complexContent[@mixed=\'false\']/extension[@base=\'tns:InnerObjectParent\']", e, XPathConstants.NODE);
        assertNotNull(e);
        assertNotNull(xp.evaluate("./sequence/element[@name=\'prop2\']", e, XPathConstants.NODE));
        assertNull(xp.evaluate("./sequence/element[@name=\'Prop1\']", e, XPathConstants.NODE));
        
        Element port = (Element) xp.evaluate("./portType", defs, XPathConstants.NODE);
        assertEquals("ServerNameSoap", port.getAttribute("name"));
        
        Element esch = (Element) xp.evaluate("./types/schema", defs, XPathConstants.NODE);
        assertEquals("qualified", esch.getAttribute("elementFormDefault"));
        assertEquals("http://tempuri.org/", esch.getAttribute("targetNamespace"));
        
        
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
    
    @SuppressWarnings("unchecked")
    public void testGenerateResponse(){
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, TestSrv3.class);
        
        Cls2 cls2 = new Cls2();
        cls2.setVV("-String content 1-");
        cls2.setVObj(Integer.valueOf(7123));
        cls2.setList1(Arrays.asList("str1", null, "str2"));
        cls2.setArray1(new Object[]{new Cls2.InnerObject(), 703L});
        HashMap<Float, InnerObject> map = new HashMap<Float, Cls2.InnerObject>();
        map.put(78623.54f, null);
        map.put(623.54f, null);
        cls2.setMap1(map);
        
        SOAPDocument soapDoc = new InterfaceDescriptionGenerator.SOAPDocument();
        soapDoc.m_method = "run1";
        soapDoc.m_args = new Object[]{cls2};
        soapDoc.m_bIn = false;
        soapDoc.m_soap = "http://schemas.xmlsoap.org/soap/envelope/";
        
        Document doc = gen.serialize(desc, soapDoc);
        print(doc);
    }
    
    /*
    interface ITFService{
        LongListResponse collectionView(CollectionViewRequest req);
        ModificationResponse modification(ModificationRequest req);
    }
    
    @SuppressWarnings("unchecked")
    public void testTFService(){
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, ITFService.class);
        print(gen.generateWSDL(desc.getWsdlDescription()));
    }
    
        
    static class TestC1 implements Serializable{
        private static final long serialVersionUID = -6615448365675139508L;

        private String v;
        public TestC1() {
        }
        public TestC1(String v) {
            this.v =v; 
        }
        
    }
        
    public void testSerialize() throws IOException, ClassNotFoundException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, ITFService.class);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        oos.writeObject(new TestC1("string 1"));
        oos.writeObject(desc);
        
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        
        TestC1 c1 = (TestC1) ois.readObject();
        assertEquals("string 1", c1.v);
        
        InterfaceDescription res = (InterfaceDescription) ois.readObject();
        assertNotNull(res);
        
        assertEquals(2, res.getOperations().size());
        assertTrue(res.getOperations().get(0).getName(), null != res.getOperations().get(0).getName()); 
        Operation op = res.getOperation("collectionView");
        assertNotNull(op);
        ClassDef td = (ClassDef) res.getTypeDef("Operation", false);
        assertNotNull(td);
        assertNotNull(td.getProp("Target"));
    }
    */
    
    public void ntestParseOrder(){
        InterfaceDescription oldDesc = new InterfaceDescription();
        oldDesc.getOperations().add(new Operation("method2", null, null));
        oldDesc.getOperations().add(new Operation("method4", null, null));
        oldDesc.getOperations().add(new Operation("method1", null, null));
        oldDesc.getOperations().add(new Operation("method3", null, null));
        ClassDef cDef = new ClassDef("Cls2", null);
        cDef.getOwnProps().add(new PropDef("vObj", null, null, null));
        cDef.getOwnProps().add(new PropDef("vObjUnk", null, null, null));
        cDef.getOwnProps().add(new PropDef("map1", null, null, null));
        oldDesc.getTypes().add(cDef);
        oldDesc.getTypes().add(new ClassDef("ClsUnk", null));
        cDef = new ClassDef("Cls1", null);
        cDef.getOwnProps().add(new PropDef("v1", null, null, null));
        cDef.getOwnProps().add(new PropDef("v2", null, null, null));
        cDef.getOwnProps().add(new PropDef("v3", null, null, null));
        cDef.getOwnProps().add(new PropDef("vMap", null, null, null));
        oldDesc.getTypes().add(cDef);
        
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        
        InterfaceDescription res = gen.parse(oldDesc, TestSrv2.class);
        
        assertEquals(3, res.getOperations().size());
        assertEquals("method2", res.getOperations().get(0).getName());
        assertEquals("method1", res.getOperations().get(1).getName());
        assertEquals("method3", res.getOperations().get(2).getName());
        
        assertEquals("Cls2",res.getTypes().get(0).getName());
        assertEquals("Cls1",res.getTypes().get(1).getName());
        
        List<PropDef> props = ((ClassDef)res.getTypes().get(0)).getProps();
        assertEquals("vObj", props.get(0).getName());
        assertEquals("map1", props.get(1).getName());
        
        props = ((ClassDef)res.getTypes().get(1)).getProps();
        assertEquals("v1", props.get(0).getName());
        assertEquals("vMap", props.get(1).getName());
    }
    
    public void testSerializeException() throws XPathExpressionException, UnsupportedEncodingException, DOMException, TransformerException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        try{
            getException(3);
        }catch(Throwable e){
            Document res = gen.serializeException(InterfaceDescriptionGenerator.soapenv, e);
            
            XPathFactory xpf = XPathFactory.newInstance();  
            XPath xp = xpf.newXPath();
            
            print(res);
            Element body = (Element) xp.evaluate("/Envelope/Body", res, XPathConstants.NODE);
            assertNotNull(body);
            assertEquals(Boolean.TRUE, xp.evaluate("./Fault", body, XPathConstants.BOOLEAN));
            Element node = (Element) xp.evaluate("./Fault/faultcode", body, XPathConstants.NODE);
            assertNotNull(node);
            assertEquals("soap:Server", node.getTextContent());
            assertEquals(Boolean.TRUE, xp.evaluate("./Fault/faultstring", body, XPathConstants.BOOLEAN));
            node = (Element) xp.evaluate("./Fault/faultstring", body, XPathConstants.NODE);
            assertNotNull(node);
            assertEquals(getMsgString(e), node.getTextContent());
            node = (Element) xp.evaluate("./Fault/detail", body, XPathConstants.NODE);
            assertNotNull(node);
        }
        
        {   // SOAP 1.2
            Exception e = new Exception("ua-ua-ua");
            Document res = gen.serializeException(InterfaceDescriptionGenerator.soapenv12, e);
            
            XPathFactory xpf = XPathFactory.newInstance();  
            XPath xp = xpf.newXPath();
            
            print(res);
            Element env = (Element) xp.evaluate("/Envelope", res, XPathConstants.NODE);
            assertEquals(InterfaceDescriptionGenerator.soapenv12, env.getAttribute("xmlns:soap"));
            Element body = (Element) xp.evaluate("./Body", env, XPathConstants.NODE);
            assertNotNull(body);
            assertEquals(Boolean.TRUE, xp.evaluate("./Fault", body, XPathConstants.BOOLEAN));
            Element node = (Element) xp.evaluate("./Fault/Code/Value", body, XPathConstants.NODE);
            assertNotNull(node);
            assertEquals("soap:Receiver", node.getTextContent());
            
            node = (Element) xp.evaluate("./Fault/Reason/Text", body, XPathConstants.NODE);
            assertNotNull(node);
            assertEquals(getMsgString(e), node.getTextContent());
            node = (Element) xp.evaluate("./Fault/Detail", body, XPathConstants.NODE);
            assertNotNull(node);
        }
        {	//ns is null
            Exception e = new Exception("Пишем по русски");
            Document res = gen.serializeException(null, e);
            gen.writeDocument(System.out, res);
            /*XPathFactory xpf = XPathFactory.newInstance();  
            XPath xp = xpf.newXPath();
            Element env = (Element) xp.evaluate("/Envelope", res, XPathConstants.NODE);
            assertEquals(InterfaceDescriptionGenerator.soapenv, env.getBaseURI());*/
        	
        }
    }

    private String getMsgString(Throwable e) throws UnsupportedEncodingException {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        e.printStackTrace(new PrintStream(out));
//        return new String(out.toByteArray(), "utf-8");
        return e.toString();
    }

    private void getException(int i) throws Exception {
        if(i == 0)
            throw new Exception("!!!my_text!!!");
        else
            getException(--i);
    }
    
    public void testParseMethods(){
//        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
//        HashMap<String, Method> methods = new HashMap<String, Method>();
//        gen.parse(null, methods);

    }
    
    
    interface I1{
    	@PropertiesSequence(sequence = {})
    	public static class C1{}
    	public void fun(C1 c);
    }
    
    interface I2{
    	public static class C2{
    		private int v;

			public void setV(int v) {
				this.v = v;
			}

			public int getV() {
				return v;
			}
    		
    	}
    	public void fun(C2 c);
    }
    
    interface I3{
    	enum ENM{V1,V2};
    	public static class C1{}
    	public Map<String,String> fun(C1 c, ENM enm);
    }
    
    interface I4{
    	@PropertiesSequence(sequence={"c","b","v","a"})
    	public static class C2{
    		private int v,b,c,a;

			public void setV(int v) {
				this.v = v;
			}

			public int getV() {
				return v;
			}

			public int getB() {
				return b;
			}

			public void setB(int b) {
				this.b = b;
			}

			public int getC() {
				return c;
			}

			public void setC(int c) {
				this.c = c;
			}

			public int getA() {
				return a;
			}

			public void setA(int a) {
				this.a = a;
			}
    		
    	}
    	public void fun(C2 c);
    }
    
    interface I5{
    	@PropertiesSequence(sequence={"c","v","a"})
    	public static class C2{
    		private int v,b,c,a;

			public void setV(int v) {
				this.v = v;
			}

			public int getV() {
				return v;
			}

			public int getB() {
				return b;
			}

			public void setB(int b) {
				this.b = b;
			}

			public int getC() {
				return c;
			}

			public void setC(int c) {
				this.c = c;
			}

			public int getA() {
				return a;
			}

			public void setA(int a) {
				this.a = a;
			}
    		
    	}
    	public void fun(C2 c);
    }
    
    interface I6{
    	@PropertiesSequence(sequence={"c"})
    	public static class C2_Outter{
    		private int c;

			public void setC(int c) {
				this.c = c;
			}

			public int getC() {
				return c;
			}
    	}
    	
    	@PropertiesSequence(sequence={"c","b","v","a"})
    	public static class C2 extends C2_Outter{
    		private int v,b,a;

			public void setV(int v) {
				this.v = v;
			}

			public int getV() {
				return v;
			}

			public int getB() {
				return b;
			}

			public void setB(int b) {
				this.b = b;
			}

			public int getA() {
				return a;
			}

			public void setA(int a) {
				this.a = a;
			}
    		
    	}
    	public void fun(C2 c);
    }
    
    public void testValidateInterface() throws IntrospectionException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        assertEquals(Collections.emptyList(), gen.validateInterface(I1.class));
        assertEquals(Collections.emptyList(), gen.validateInterface(I3.class));

        assertEquals(Arrays.asList(new InterfaceDescriptionGenerator.ValErrItem.EPropSeqNotFound("C2")), 
        		 gen.validateInterface(I2.class));
        
        assertEquals(Collections.emptyList(), gen.validateInterface(I4.class));

        assertEquals(Arrays.asList(new InterfaceDescriptionGenerator.ValErrItem.ENoPropInSequence("C2","b")), 
       		 gen.validateInterface(I5.class));
        assertEquals(Arrays.asList(new InterfaceDescriptionGenerator.ValErrItem.ENoPropDefForSequence("C2","c")), 
          		 gen.validateInterface(I6.class));
    }
    
    public void testDeserializeJson() throws SAXException, IOException, ParserConfigurationException, ParseException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        {
	        InterfaceDescription desc = gen.parse(null, TestSrv2.class);
	        
	        SOAPDocument res = gen.deserializeJson(desc, JSONSerializerTest.source("{\"jsonrpc\":\"2.0\",\"method\":\"method3\",\"params\":[\"string_value\"],\"id\":1}"));
	        
	        assertNotNull(res);
	        assertEquals("method3", res.m_method);
	        assertEquals(1, res.m_args.length);
	        assertEquals("string_value", res.m_args[0]);
	        
	        res = gen.deserializeJson(desc, JSONSerializerTest.source("{\"jsonrpc\":\"2.0\",\"method\":\"method4\",\"params\":[65412],\"id\":1}"));
	        assertNotNull(res);
	        assertEquals("method4", res.m_method);
	        assertEquals(1, res.m_args.length);
	        assertEquals(65412, res.m_args[0]);
	        
	        
	        res = gen.deserializeJson(desc, JSONSerializerTest.source("{\"jsonrpc\":\"2.0\",\"method\":\"method5\",\"params\":[[65412, 763573]],\"id\":1}"));
	        int[] arg0 = (int[]) res.m_args[0];
	        assertEquals(65412, arg0[0]);
	        assertEquals(763573, arg0[1]);

	        res = gen.deserializeJsonResponse(desc, "method3", JSONSerializerTest.source("{\"jsonrpc\":\"2.0\",\"result\":65412,\"id\":1}"));
	        assertEquals(65412, res.m_args[0]);
	        
	        try{
	        	gen.deserializeJson(desc, JSONSerializerTest.source("{\"jsonrpc\":\"2.0\",\"method\":\"method_unknown\",\"params\":[],\"id\":1}"));
	        	fail();
	        } catch(ESoap.EMethodNotFound e){}

	        try{
	        	gen.deserializeJson(desc, JSONSerializerTest.source("{\"jsonrpc\":\"2.0\",\"method\":\"method5\",\"params\":[[65412, 763573], 235],\"id\":1}"));
	        	fail();
	        } catch(ESoap.EUnknownElement e){}
	        
	        try{
	        	gen.deserializeJson(desc, JSONSerializerTest.source("{\"jsonrpc\":\"2.0\",\"method\":\"method6\",\"params\":[65412, {763573],\"id\":1}"));
	        	fail();
	        } catch(ParseException e){}
	     
	        try{
	        	gen.deserializeJson(desc, JSONSerializerTest.source("{\"jsonrpc\":\"2.0\",\"method\":\"method6\",\"params\":[65412, \"string instead int\"],\"id\":1}"));
	        	fail();
	        } catch(NumberFormatException e){}
	        
	        gen.deserializeJsonResponse(desc, "method3", JSONSerializerTest.source("{\"jsonrpc\": \"2.0\", \"result\": 19, \"id\": 1}"));
        }
        {
        	InterfaceDescription desc = gen.parse(null, TestSrv3.class);
        	ClassDef cd1 = (ClassDef) desc.getOperation("run1").getRequestType().getProp("arg0").getType();
        	ArrayDef ad1 = (ArrayDef) cd1.getProp("v1").getType();
        	ApiAlgs.getLog(this).trace("raw type : " + ad1.getPropDef().getRawType());
        	ArrayDef ad2 = (ArrayDef) ad1.getPropDef().getType();
        	ApiAlgs.getLog(this).trace("raw type : " + ad2.getPropDef().getRawType());
        	
        	SOAPDocument res = gen.deserializeJson(desc, JSONSerializerTest.source("{\"jsonrpc\":\"2.0\",\"method\":\"run1\",\"params\":[{\"v1\":[[12, 15], [13]]}],\"id\":1}"));
        	Cls1 arg0 = (Cls1) res.m_args[0];
        	assertNotNull(arg0);
        	assertEquals(2, arg0.getV1().size());
        	assertEquals(15, arg0.getV1().get(0)[1]);
        }
    }
    
    public void testSerializeJson() throws UnsupportedEncodingException, SAXException, IOException, ParserConfigurationException, ParseException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, TestSrv2.class);
        {
	        String strRes = gen.serializeJson(desc, 77);
	        
	        assertEquals("{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":77}", strRes);
	        
	        SOAPDocument res = gen.deserializeJsonResponse(desc, "method3", JSONSerializerTest.source(strRes));
	        assertEquals(77, res.m_args[0]);
        }
        
        {
        	String res = gen.serializeJsonException(new ESoap.EMethodNotFound("unk_method"));
        	try{
        		gen.deserializeJsonResponse(desc, "unk_method", JSONSerializerTest.source(res));
        		fail();
        	} catch(EMethodNotFound e){}
        	
        	trace(res);
        	assertTrue(res, res.contains("\"error\":{"));
        	assertTrue(res, res.contains("\"code\":-32601"));
        	
        }
    }
    
    public void testSerialize(){
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        InterfaceDescription desc = gen.parse(null, TestSrv2.class);
        SOAPDocument soapDoc = new InterfaceDescriptionGenerator.SOAPDocument();
        soapDoc.m_method = "method3";
        soapDoc.m_args = new Object[]{};
        soapDoc.m_bIn = false;
        soapDoc.m_soap = "http://schemas.xmlsoap.org/soap/envelope/";
        
        try{
        	gen.serialize(desc, soapDoc);
        } catch(NoSuchElementException e){
        	assertEquals("method3Result", e.getMessage());
        }
        
        soapDoc.m_bIn = true;
        try{
        	gen.serialize(desc, soapDoc);
        } catch(NoSuchElementException e){
        	assertEquals("arg0", e.getMessage());
        }

    }
    
    interface ITest_01{
    	void method_01();
    }
    
    public void testParseInterfaces() throws IntrospectionException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
        {
	        InterfaceDescription res = gen.parse(null, new ArrayList<Class>());
	        assertNotNull(res);
        }
        {
	        InterfaceDescription res = gen.parse(null, new ArrayList<Class>(Arrays.asList(ITest_01.class)));
	        assertNotNull(res);
	        assertFalse(res.getOperations().isEmpty());
	        Operation op = res.getOperation("soap_method_01");
	        assertNotNull(res.getOperations().toString(), op);
        }
        {
        	InterfaceDescription res = gen.parse(null, new ArrayList<Class>(Arrays.asList(ITestHorse.class, ITestBird.class)));
        	assertFalse(res.getOperations().isEmpty());
        	
        	Operation op;
        	op = res.getOperation("testpkg_01_run");
	        assertNotNull(res.getOperations().toString(), op);
	        
        	op = res.getOperation("testpkg_02_fly");
	        assertNotNull(res.getOperations().toString(), op);
	        
	        TypeDef type = res.getType(Hand.class);
	        assertNotNull(type);
	        
	        //SoapInclude annotation classes
	        assertNotNull(res.getType(ITestHorse.CTail.class));
        }
        
        {
        	try{
        		gen.parse(null, new ArrayList<Class>(Arrays.asList(ITestHorse.class, ITestCow.class)));
            	fail();
        	} catch(InvalidTypeName e){
        		assertEquals("Hand", e.getMessage());
        	}
        }
        
//        {
//        	List<InterfaceOperationDescription> operationDescs;
//        	SoapInclude[] soapInc;
//        	operationDescs = gen.listInterfaceOperations(cls, bMultiClass)
//        	gen.parse(null, operationDescs, getClass().getPackage(), soapInc);
//        }
    	
    }
    
    static class CEmpty{}
    
    static class COutter{
    	public void method_01() {
		} 
    }
    
    static class CInner extends COutter{
    }
    
    static class CRandom extends Random{
		private static final long serialVersionUID = -1607781444558231654L;}
    
    public void testListInterfaceOps() throws IntrospectionException{
        InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
    	
        {
	        List<InterfaceOperationDescription> res = gen.listInterfaceOperations(CEmpty.class, false);
	        assertTrue(res.toString(), res.isEmpty());
        }
        {        
        	List<InterfaceOperationDescription> res = gen.listInterfaceOperations(CInner.class, false);	
        	assertEquals(1, res.size());
        	assertEquals("method_01", res.get(0).getName());
        }
        {        
        	List<InterfaceOperationDescription> res = gen.listInterfaceOperations(CRandom.class, false);	
        	assertTrue(res.toString(), res.isEmpty());
        }
    }

}
