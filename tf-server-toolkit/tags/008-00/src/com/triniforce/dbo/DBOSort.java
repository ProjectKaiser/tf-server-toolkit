/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo;

import java.util.ArrayList;
import java.util.List;

public class DBOSort {
	
	public DBOSort() {
	}

	public List<IDBObject> sort(List<IDBObject> list){
		ArrayList<IDBObject> listCopy = new ArrayList<IDBObject>(list);
		ArrayList<IDBObject> res = new ArrayList<IDBObject>();
		for(int i = 0; i<listCopy.size(); i++){
			IDBObject current = listCopy.get(i);
			moveObject(res, listCopy, current, i);
		}
		return res;
	}

	private void moveObject(ArrayList<IDBObject> to,
			ArrayList<IDBObject> from, IDBObject obj, int idx) {
		for(IDBObject dep : obj.getDependiencies()){
			int iDep = from.indexOf(dep);
			if(iDep > idx){
				moveObject(to, from, from.remove(iDep), idx);
			}
		}
		to.add(obj);
	}
}
