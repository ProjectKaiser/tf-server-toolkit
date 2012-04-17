/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package gmp;

import java.util.HashMap;
import java.util.Map;

import net.sf.sojo.interchange.json.JsonSerializer;

import com.triniforce.db.test.TFTestCase;

public class ProbeTest extends TFTestCase {
	
	@SuppressWarnings("serial")
	public static class MyClass2{
		public String m_prop1;
		public String m_prop2;
		public String getProp1() {
			return m_prop1;
		}
		public void setProp1(String prop1) {
			m_prop1 = prop1;
		}
		public String getProp2() {
			return m_prop2;
		}
		public void setProp2(String prop2) {
			m_prop2 = prop2;
		}
		
		Map<String, Object> m_map = new HashMap<String, Object>(){{put("name1", "value1"); put("name2", new MyClass());}};
		public Map<String, Object> getMap() {
			return m_map;
		}
		public void setMap(Map<String, Object> map) {
			m_map = map;
		}
		
		
		
	}
	
	public void testJsonSerializerSpeed(){
        long start = System.currentTimeMillis();
        JsonSerializer js = new JsonSerializer(); 
        for (int i = 0; i < 1000; i++) {
    		MyClass2 myClass2 = new MyClass2();
    		myClass2.setProp1("p1");
    		myClass2.setProp2("p2");
    		String jsonStr = js.serialize(myClass2).toString();
    		myClass2 = (MyClass2) js.deserialize(jsonStr);
        }
        trace((System.currentTimeMillis() - start));	
	}
	
	public void testJsonSerializer(){
		JsonSerializer js = new JsonSerializer(); 
		MyClass2 myClass2 = new MyClass2();
		myClass2.setProp1("p1");
		myClass2.setProp2("p2");
		String jsonStr = js.serialize(myClass2).toString();
		trace(jsonStr);
		myClass2 = (MyClass2) js.deserialize(jsonStr);
		assertEquals("p1", myClass2.getProp1());
		assertEquals("p2", myClass2.getProp2());
	}
	
    public void testHello() {
        System.out.println("Hello, world\n");
    }

    public static class MyClass {
        String s = "the string";

		public String getS() {
			return s;
		}

		public void setS(String s) {
			this.s = s;
		}
        
    }

    public void testConstructorSpeed() throws Exception {

        final int numObjects = 1000000;

        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                new MyClass();
            }
            trace("Direct instantiation, num="+ numObjects + " :" + (System.currentTimeMillis() - start));
        }

        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                Class cls = MyClass.class;
                cls.newInstance();
            }
            trace("cls.newInstance(), num="+ numObjects + " :" + (System.currentTimeMillis() - start));
        }
        
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                try{
                    Class cls = MyClass.class;
                    cls.newInstance();
                }catch(Exception e){
                    
                }
            }
            trace("try{cls.newInstance()}catch{..}, num="+ numObjects + " :" + (System.currentTimeMillis() - start));
        }

    }
    
}
