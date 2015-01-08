/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.triniforce.db.ddl.DiffLeader.ICmdFactory.Action;

/**
 * @author IAS
 *
 * @param <TCmd> command type
 * @param <E> element type
 */
public class DiffLeader<TCmd, E> {
	
	interface ICmdFactory<TCmd, E>{
		public static class EUnsuccessOperation extends RuntimeException{
			private static final long serialVersionUID = 8903316545969896499L;
			public EUnsuccessOperation() {
			}
			public EUnsuccessOperation(String msg) {
				super(msg);
			}
		}
		
		enum Action{NONE, EDIT, DROP_AND_ADD};
		
		Action getEqKeyAction(E srcElement, E dstElement) throws EUnsuccessOperation;
		
		TCmd addCmd(E element);
		TCmd dropCmd(E element);
		TCmd editCmd(E srcElement, E dstElement);
	}

	private ICmdFactory<TCmd, E> m_cmdFactory;
	
	public DiffLeader(ICmdFactory<TCmd, E> cmdFact) {
		m_cmdFactory = cmdFact;
	}
	
	<TKey> List<TCmd> getCommandSeq(Map<TKey, E> src, Map<TKey, E> dst){
		ArrayList<TCmd> res = new ArrayList<TCmd>();
		// all destiny elements must be created or edited 
		// depending on source elements
		for (Map.Entry<TKey, E> entry : dst.entrySet()) {
			E srcValue = src.get(entry.getKey());
			if(null != srcValue){
				// key equal operation
				Action action = m_cmdFactory.getEqKeyAction(srcValue, entry.getValue());
				if(Action.DROP_AND_ADD.equals(action)){
					res.add(m_cmdFactory.dropCmd(entry.getValue()));					
					res.add(m_cmdFactory.addCmd(entry.getValue()));
				}
				else if (Action.EDIT.equals(action)){
					res.add(m_cmdFactory.editCmd(srcValue, entry.getValue()));
				}
			} else{
				res.add(m_cmdFactory.addCmd(entry.getValue()));
			}
		}
		// all source elements must be droped if they are not in destiny
		for (Map.Entry<TKey, E> entry : src.entrySet()) {
			if(!dst.containsKey(entry.getKey()))
				res.add(m_cmdFactory.dropCmd(entry.getValue()));					
		}
		return res;
	}

}
