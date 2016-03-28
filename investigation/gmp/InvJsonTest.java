package gmp;

import java.util.List;
import java.util.Map;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.PKJsonParser;
import com.triniforce.utils.StringSerializer;

public class InvJsonTest extends TFTestCase{
	
	
	public static class MyClass{
		private String s;
		private int i;
		public String getS() {
			return s;
		}
		public void setS(String s) {
			this.s = s;
		}
		public int getI() {
			return i;
		}
		public void setI(int i) {
			this.i = i;
		}
		
		public MyClass() {
			System.out.println("Hello");
		}
		
	}
	
	
	@SuppressWarnings("unused")
	@Override
	public void test() throws Exception {
		PKJsonParser jp = new PKJsonParser();
		Map map = (Map) jp.parse("{\"a\": 1, \"b\":\"the\"}");
		List lst = (List) jp.parse("[1, 2, 3]");
		
		MyClass myc = new MyClass();
		myc.setI(12);
		myc.setS("mas");
				
		
		String js = StringSerializer.Object2JSON(myc);
		trace(js);
		map = (Map) jp.parse(js);
		myc = (MyClass) StringSerializer.JSON2Object(js);
				
	}

}
