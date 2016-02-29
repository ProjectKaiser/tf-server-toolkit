package gmp;

import org.junit.BeforeClass;
import org.junit.Test;

public class InvJUnit4Test{
	
	@Test
	public void aaa(){
		System.out.println("aaa");
	}
	
	@Test
	public void test3() throws Exception {
		System.out.println("test3");
	}
	

	@BeforeClass
	public static void before(){
		System.out.println("before");		
	}
	
}
