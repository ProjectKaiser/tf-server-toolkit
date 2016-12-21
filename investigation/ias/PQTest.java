/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.util.PriorityQueue;
import java.util.Random;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.utils.ApiAlgs;

public class PQTest  extends TFTestCase {

	
	static class T1 implements Comparable<T1>{
		int value;
		T1(int v){
			value = v;
		}
		
		@Override
		public int compareTo(T1 o) {
			return Integer.valueOf(value).compareTo(o.value);
		}
		
	}
	
	@Override
	public void test() throws Exception {
		PriorityQueue<T1> pq = new PriorityQueue<T1>();
		Random rnd = new Random(100);
		assertTrue(rnd.nextInt() < 100);
		pq.add(new T1(rnd.nextInt(100)));
		pq.add(new T1(rnd.nextInt(100)));
		pq.add(new T1(rnd.nextInt(100)));
		pq.add(new T1(rnd.nextInt(100)));
		pq.add(new T1(rnd.nextInt(100)));
		pq.add(new T1(rnd.nextInt(100)));
		
		int prev = 0;
		for(int i =0; i< 5000; i++){
			T1 t1 = pq.poll();
			if(prev > t1.value){
				ApiAlgs.getLog(this).error("wrong value: " + t1.value + ", after " + prev);
			}
			prev = t1.value;
			t1.value += rnd.nextInt(100);
			pq.add(t1);
			if(i%10 == 0){
				trace("" + prev);
			}
		}
	}
}
