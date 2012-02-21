/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.ddl;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.triniforce.db.ddl.Delta.ColumnOperationObjectFactory;
import com.triniforce.db.ddl.Delta.IndexOperationObjectFactory;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IElementDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.TableElements;
import com.triniforce.utils.ApiAlgs;

public class TableDefSerializer {

	public void writeDef(TableDef td, ObjectOutputStream out){
		try {
			out.writeObject(td.getEntityName());
			writeElements(out, td.getFields());
			writeElements(out, td.getIndices());
		} catch (IOException e) {
			ApiAlgs.rethrowException(e);
		}
		
	}
	
	private void writeElements(ObjectOutputStream out, TableElements<? extends IElementDef> elements) throws IOException {
		out.writeInt(elements.size());
		for(int i=0; i<elements.size(); i++){
			out.writeObject(elements.getElement(i));
		}
	}

	public TableDef readDef(ObjectInputStream in){
		TableDef res = null;
		try {
			String name = (String) in.readObject();
			res = new TableDef(name);
			readFields(in, res);
			readIndexes(in, res);
		} catch(EOFException e){
		} catch (Exception e) {
			ApiAlgs.rethrowException(e);
		}
		return res;
	}

	private void readFields(ObjectInputStream in, TableDef res) throws IOException, ClassNotFoundException {
		ColumnOperationObjectFactory f = new Delta.ColumnOperationObjectFactory();
		int v = res.getVersion();
		int sz = in.readInt();
		for(int i=0; i<sz; i++){
			FieldDef element = (FieldDef) in.readObject();
			res.addModification(++v, f.addCmd(element));
		}
	}
	
	private void readIndexes(ObjectInputStream in, TableDef res) throws IOException, ClassNotFoundException {
		IndexOperationObjectFactory f = new Delta.IndexOperationObjectFactory();
		int v = res.getVersion();
		int sz = in.readInt();
		for(int i=0; i<sz; i++){
			IndexDef element = (IndexDef) in.readObject();
			res.addModification(++v, f.addCmd(element));
		}
	}
}
