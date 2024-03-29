/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.json.simple.parser.ParseException;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceDescriptionGenerator.JsonResult;
import com.triniforce.soap.InterfaceDescriptionGenerator.SOAPDocument;
import com.triniforce.soap.InterfaceOperationDescription.NamedArg;
import com.triniforce.soap.JSONSerializerTest.Service001.ObjWEnum;
import com.triniforce.soap.JSONSerializerTest.Service001.ObjWEnum2;
import com.triniforce.soap.JSONSerializerTest.Service001.Outter01;
import com.triniforce.soap.JSONSerializerTest.Service001.Real1;
import com.triniforce.soap.JSONSerializerTest.Service001.SimpleEnum;
import com.triniforce.utils.StringSerializer;

public class JSONSerializerTest extends TFTestCase {
	
	@SoapInclude(extraClasses={
			Service001.Real1.class
	})
	static class Service001{
		public String method_001(int v1, int v2){
			return null;
		}
		
		
		public String method_0011(int v1, int v2, String v3){
			return null;
		}

		public String method_0012(int v1){
			return null;
		}

		public String method_002(int v1, int v2){
			return null;
		}

		public void log_log(String s, Object o, String s2){
		}
		
		interface IAbstract{}
		static class Real1  implements IAbstract{
			private String m_prop_001;

			public String getProp_001() {
				return m_prop_001;
			}

			public void setProp_001(String prop_001) {
				m_prop_001 = prop_001;
			}
		}
		public void method_003(IAbstract v){}
		
		public void method_004(Object[] v){}
		
		public void method_005(Map<String, String> map){}
		
		static class Outter01{
			private Object m_objValue;
			public Object getObjValue() {
				return m_objValue;
			}
			public void setObjValue(Object objValue) {
				m_objValue = objValue;
			}
		}
		
		public void method_006(Outter01 v){}
		
		public void method_007(String v){}
		
		public void method_008(TObj1 v){}
		
		public void method_dt01(Date arg1){}
		
		public enum SimpleEnum{Value777, Value111};
		public SimpleEnum method_enum(SimpleEnum se){
			return se;}
		
		public static class ObjWEnum2{
			private List<ObjWEnum> m_inners;
			private Map<Integer, ObjWEnum> m_inners2;

			public List<ObjWEnum> getInners() {
				return m_inners;
			}

			public void setInners(List<ObjWEnum> inners) {
				m_inners = inners;
			}

			public Map<Integer, ObjWEnum> getInners2() {
				return m_inners2;
			}

			public void setInners2(Map<Integer, ObjWEnum> inners2) {
				m_inners2 = inners2;
			}
		}
		
		public static class ObjWEnum{
			private SimpleEnum enumhere;

			public SimpleEnum getEnumhere() {
				return enumhere;
			}

			public void setEnumhere(SimpleEnum enumhere) {
				this.enumhere = enumhere;
			}
		}
		public ObjWEnum2 method_enum2(){
			return null;}
		
		public void method_map(Map<String, Object> map){
			
		}
		
		static class C1{
			private int param;

			public int getParam() {
				return param;
			}

			public void setParam(int param) {
				this.param = param;
			}
			
			public C1() {
				
			}
			public C1(int v) {
				param = v;
			}
			
		}

		public void method_list(List<C1> l){}
	}
	
	static class Prop01{
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	public void testSerialize() throws IOException, ParseException {
		InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
		InterfaceDescription desc = gen.parse(null, Service001.class);
		JSONSerializer srz = new JSONSerializer();
		
		
		assertEquals( "{\"jsonrpc\":\"2.0\",\"method\":\"method_0012\",\"params\":[124],\"id\":1}", 
				serialize(srz, desc, "method_0012", 124));
		assertEquals( "{\"jsonrpc\":\"2.0\",\"method\":\"method_0012\",\"params\":[515],\"id\":1}", 
				serialize(srz, desc, "method_0012", 515));
		assertEquals( "{\"jsonrpc\":\"2.0\",\"method\":\"method_0011\",\"params\":[515,634,\"turbo\"],\"id\":1}", 
				serialize(srz, desc, "method_0011", 515, 634, "turbo"));
//		assertEquals( "{\"jsonrpc\":\"2.0\",\"params\":[515,{\"~unique-id~\":\"0\",\"value\":\"val_0012\",\"class\":\"com.triniforce.soap.JSONSerializerTest$1\"}],\"method\":\"method_001\",\"id\":1}", serialize(srz, desc, 515, new Prop01(){{setValue("val_0012");}}));
		assertEquals( "{\"jsonrpc\":\"2.0\",\"method\":\"method_001\",\"params\":[515,{\"value\":\"val_0012\"}],\"id\":1}", 
				serialize(srz, desc, 515, new Prop01(){{setValue("val_0012");}}));
		
		TimeZone tz0 = TimeZone.getDefault();
		try{
			TimeZone.setDefault(TimeZone.getTimeZone("GMT+3"));
			Date dt = new GregorianCalendar(2005, 1, 12).getTime();
			String str = serialize(srz, desc, "method_dt01", dt);
	
			assertEquals( "{\"jsonrpc\":\"2.0\",\"method\":\"method_dt01\",\"params\":[\"2005-02-11T21:00:00.000Z\"],\"id\":1}", str);
			
			SOAPDocument res = srz.deserialize(desc, new ByteArrayInputStream(str.getBytes()));
			
			assertEquals(dt, res.m_args[0]);
			
			
		}finally{
			TimeZone.setDefault(tz0);
		}
		
		assertEquals( "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"i am result\"}", 
				serializeResponse(srz, desc, "method_0011", "i am result"));
		assertEquals( "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"Value777\"}", 
				serializeResponse(srz, desc, "method_enum", SimpleEnum.Value777));

		ObjWEnum v = new ObjWEnum();
		v.setEnumhere(SimpleEnum.Value777);
		ObjWEnum2 v2 = new ObjWEnum2();
		v2.m_inners = Arrays.asList(v);
		v2.setInners2(new HashMap<Integer, ObjWEnum>());
		v = new ObjWEnum();
		v.setEnumhere(SimpleEnum.Value111);
		v2.getInners2().put(5665, v);
		assertEquals( "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"inners\":[{\"enumhere\":\"Value777\"}],"
				+ "\"inners2\":[{\"key\":5665,\"value\":{\"enumhere\":\"Value111\"}}]}}", 
				serializeResponse(srz, desc, "method_enum2", v2));
		
		Real1 vreal = new Real1();
		assertEquals( "{\"jsonrpc\":\"2.0\",\"method\":\"method_003\",\"params\":[{\"type\":\"Real1\",\"prop_001\":null}],\"id\":1}",
				serialize(srz, desc, "method_003", vreal));
		
		
		assertEquals( "{\"jsonrpc\":\"2.0\",\"method\":\"method_007\",\"params\":[\"{\\\"some\\\":[\\\"internal\\\", 11, \\\"json\\\"]}\"],\"id\":1}",
				serialize(srz, desc, "method_007", "{\"some\":[\"internal\", 11, \"json\"]}"));

		
	}

	private String serialize(JSONSerializer srz, InterfaceDescription desc,
			Object ... values) throws IOException {
		return serialize(srz, desc, "method_001", values);
	}
	
	private String serialize(JSONSerializer srz, InterfaceDescription desc, String method,
			Object ... values) throws IOException {
		SOAPDocument soap = new InterfaceDescriptionGenerator.SOAPDocument();
		soap.m_method = method;
		soap.m_args = values;
		soap.m_bIn = true;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		srz.serialize(desc, soap, out);
		return out.toString("utf-8");
	}
	
	private String serializeResponse(JSONSerializer srz, InterfaceDescription desc, String method,
			Object value) throws IOException {
		SOAPDocument soap = new InterfaceDescriptionGenerator.SOAPDocument();
		soap.m_method = method;
		soap.m_args = new Object[]{value};
		soap.m_bIn = false;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		srz.serialize(desc, soap, out);
		return out.toString("utf-8");
	}
	
	static class TObj2{
		
		public TObj2(String sv) {
			setV(sv);
		}

		public String getV() {
			return v;
		}

		public void setV(String v) {
			this.v = v;
		}

		private String v;
	}
	static class TObj1{
		private int v;
		private TObj2 obj;
		
		
		public TObj1() {
		}
		
		public TObj1(int vv, String sv) {
			setV(vv);
			setObj(new TObj2(sv));
		}

		public int getV() {
			return v;
		}

		public void setV(int v) {
			this.v = v;
		}

		public TObj2 getObj() {
			return obj;
		}

		public void setObj(TObj2 obj) {
			this.obj = obj;
		}
		
	}

	public void testDeserialize() throws IOException, ParseException {
		JSONSerializer srz = new JSONSerializer();
		InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
		InterfaceDescription desc = gen.parse(null, Service001.class);
		SOAPDocument res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_001\",\"params\":[124],\"id\":1}"));
		assertNotNull(res);
		assertEquals(true, res.m_bIn);
		assertEquals("method_001", res.m_method);
		assertEquals(124, res.m_args[0]);
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_002\",\"params\":[532, 2131],\"id\":1}"));
		assertEquals(true, res.m_bIn);
		assertEquals("method_002", res.m_method);
		assertEquals(532, res.m_args[0]);
		assertEquals(2131, res.m_args[1]);
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_003\",\"params\":[{\"type\":\"Real1\", \"prop_001\":\"setter\"}],\"id\":1}"));
		Service001.Real1 obj = (Real1) res.m_args[0];
		assertEquals("setter",obj.getProp_001());
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_004\",\"params\":[[{\"type\":\"Real1\",\"prop_001\":\"setter\"}, {\"type\":\"Int\", \"value\":7652}]],\"id\":1}"));
		Object[] arg0 = (Object[]) res.m_args[0];
		obj = (Real1) arg0[0];
		assertNotNull(obj);
		assertEquals("setter",obj.getProp_001());

		Integer  i = (Integer) arg0[1];
		assertNotNull(i);
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_004\",\"params\":[[{\"type\":\"Real1\",\"prop_001\":\"setter\"}, {\"type\":\"DateTime\", \"value\":\"1970-01-01T08:00:00\"}]],\"id\":1}"));
		arg0 = (Object[]) res.m_args[0];
		obj = (Real1) arg0[0];
		assertNotNull(obj);
		assertEquals("setter",obj.getProp_001());
		Date dt = (Date) arg0[1];
		assertNotNull(dt);

		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_005\",\"params\":[[" +
				"{\"key\":\"key_001\", \"value\":\"vvvv\"}, {\"key\":\"key_002\", \"value\":\"vvv2\"}]],\"id\":1}"));
		Map<String,String> map = (Map<String, String>) res.m_args[0];
		assertEquals("vvvv", map.get("key_001"));
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_004\",\"params\":[[\"string_value\", 362472]],\"id\":1}"));
		arg0 = (Object[]) res.m_args[0];
		assertEquals("string_value", arg0[0]);
		assertEquals(362472L, arg0[1]);
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_006\",\"params\":[{\"objValue\":\"string_YT\"}],\"id\":1}"));
		Outter01 obj01 = (Outter01) res.m_args[0];
		assertEquals("string_YT", obj01.getObjValue());
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_006\",\"params\":[{\"objValue\":672}],\"id\":1}"));
		obj01 = (Outter01) res.m_args[0];
		assertEquals(672L, obj01.getObjValue());
		
		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_006\",\"params\":[{\"objValue\":{\"type\":\"Short\", \"value\":67}}],\"id\":1}"));
		obj01 = (Outter01) res.m_args[0];
		assertEquals((short)67, obj01.getObjValue());


		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_007\",\"params\":[\"string cont\\\"aining [ :}\"],\"id\":1}"));
		assertEquals("string cont\"aining [ :}", res.m_args[0]);

		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_007\",\"params\":[\"\"],\"id\":1}"));
		assertEquals("", res.m_args[0]);

//		res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_007\",\"params\":[\"arg0\":1,2,3],\"id\":1}"));
//		assertEquals("", res.m_args[0]);

		{		
			String json = StringSerializer.Object2JSON(new TObj1(1234, "my_str_19951"));
			trace(json);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			LinkedHashMap<String, Object> mapObj = new LinkedHashMap<String, Object>();
			HashMap<String, Object> m2 = new LinkedHashMap<String, Object>();
			m2.put("arg0",json);
			mapObj.put("method", "method_008");
			mapObj.put("params", m2);
			srz.serializeObject(mapObj, out);
			trace(new String(out.toByteArray(), "utf-8"));
			
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

			res = srz.deserialize(desc, in);
			
			assertNotNull(res);
			trace(res.m_args[0]);
//			assertEquals(json,  res.m_args[0]);
		}
		{
			String str = "{\"jsonrpc\":\"2.0\",\"method\":\"log_log\",\"params\":[\"log_me\",null,\"INFO\"]}";
			res = srz.deserialize(desc, source(str));
			assertEquals(Arrays.asList("log_me", null, "INFO"), Arrays.asList(res.m_args));
		}
		{//enum types
			String str = "{\"jsonrpc\":\"2.0\",\"method\":\"method_enum\",\"params\":[\"Value111\"]}";
			res = srz.deserialize(desc, source(str));
			assertEquals(Arrays.asList(SimpleEnum.Value111), Arrays.asList(res.m_args));
			
			str = "{\"jsonrpc\":\"2.0\",\"method\":\"method_enum\",\"params\":[\"ValueUnknown\"]}";
			res = srz.deserialize(desc, source(str));
			trace("enum: " + res.m_args[0]);
		}
		{// Map
			String str = "{\"jsonrpc\":\"2.0\",\"method\":\"method_map\",\"params\":[[{\"key\":\"k0\",\"value\":99},{\"key\":\"k1\",\"value\":\"fdgd\"}]]}";
			Map mapres = (Map) srz.deserialize(desc, source(str)).m_args[0];
			assertEquals(99L,mapres.get("k0"));
			assertEquals("fdgd",mapres.get("k1"));
		}
		{//List of objects
		}

		{
			ArrayList<InterfaceOperationDescription> ops = new ArrayList<InterfaceOperationDescription>();
			ops.add(new InterfaceOperationDescription("method_858585", Arrays.asList(
					new NamedArg("header", String.class)), new NamedArg("resulting", int.class)));
			ArrayList<SoapInclude> incls = new ArrayList<SoapInclude>();
			desc = gen.parse(null, ops, getClass().getPackage(), incls);
			res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_858585\",\"params\":[\"GGG\"]}"));
			assertEquals("GGG", res.m_args[0]);
			{//unicode
				res = srz.deserialize(desc, source("{\"jsonrpc\":\"2.0\",\"method\":\"method_858585\",\"params\":[\"\\u0442\\u0435\\u0441\\u0442\"]}"));
				assertEquals("тест", res.m_args[0]);
			}
			
			
		}
		

	}
	
	public void testDeserialize2() throws UnsupportedEncodingException, IOException, ParseException{
		JSONSerializer srz = new JSONSerializer();
		InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
		InterfaceDescription desc = gen.parse(null, Service001.class);

		String str = "{\"jsonrpc\":\"2.0\",\"method\":\"method_list\",\"params\":[[{\"param\":1},{\"param\":2}]]}";
		List l1 = (List) srz.deserialize(desc, source(str)).m_args[0];
		assertNotNull(l1);

		
	}

	public static InputStream source(String string) throws UnsupportedEncodingException {
		ByteArrayInputStream res = new ByteArrayInputStream(string.getBytes("utf-8"));
		return res;
	}
	
	enum ENM1{VAL1};
	
	public void testSerializeObject() throws IOException{
//		InterfaceDescriptionGenerator gen = new InterfaceDescriptionGenerator();
//		InterfaceDescription desc = gen.parse(null, Service001.class);
		JSONSerializer srz = new JSONSerializer();
		JsonResult jsonRes = new JsonResult();
		jsonRes.setResult(ENM1.VAL1);

		srz.serializeObject(jsonRes, System.out);
		
	}

}
