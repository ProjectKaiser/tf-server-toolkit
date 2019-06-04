/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias;

import java.util.Arrays;

public class Returnme {

	public static void main(String[] args) throws Exception {
		System.out.println("args: " + Arrays.asList(args));
		if(args.length >= 2){
			if(args[0].equals("return"))
				System.exit(Integer.parseInt(args[1]));
			if(args[0].equals("throw"))
				throw new Exception(args[1]);
		}
			
	}

}
