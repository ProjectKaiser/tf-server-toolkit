/**
 * Copyright(C) Triniforce 
 * All Rights Reserved.
 * 
 */

package com.triniforce.db.ddl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;


import com.triniforce.db.ddl.TableDef.EReferenceError;
import com.triniforce.db.ddl.TableDef.ECycleReference;
import com.triniforce.db.ddl.TableDef.EUnknownReference;
import com.triniforce.db.ddl.TableDef.EMetadataException;
import com.triniforce.db.ddl.TableDef.ElementVerStored;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IElementDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.Indices;
import com.triniforce.db.ddl.TableDef.IElementDef.NameCondition;
import com.triniforce.db.ddl.UpgradeRunner.IActualState;
import com.triniforce.utils.ApiAlgs;


/**
 * Collection for all operating DBTables   
 */
public class DBTables {
    
    private class UniqueIndexCondition implements IElementDef.ISearchCondition<IndexDef>{
        private NameCondition<IndexDef> m_nameCond;
        public UniqueIndexCondition(String name){
            m_nameCond = new IElementDef.NameCondition<IndexDef>(name);
        }
        public boolean is(IndexDef element) {
            return element.m_bUnique && m_nameCond.is(element);
        }        
    }
    
    public static class DBOperation{
        String  m_dbObject;
        TableOperation m_operation;
        public DBOperation(String dbObject, TableOperation op){
            m_dbObject = dbObject;
            m_operation = op;
        }
        public String getDBOName() {
            return m_dbObject;
        }
        public TableOperation getOperation() {
            return m_operation;
        }
        @Override
        public boolean equals(Object arg0) {
            DBOperation op = (DBOperation) arg0; 
            if(op == null) return false;
            if(!op.getOperation().getClass().equals(getOperation().getClass()))
            	return false;
            return getDBOName().equals(op.getDBOName()) && op.getOperation().getName().equals(getOperation().getName());
        }
    }
    
    /**
     * Emitting operation from base one by one 
     * with standart iterating algorithm
     */
    public class CommandListIterator implements Iterator<DBOperation>
    {        
        private HashMap<String, Integer> m_asTemp;  //Cashing actual DBO versions
        private Stack<DBOperation> m_depStck;       //applying in my recursive algorithm
        private Iterator<TableDef> m_iNext;     //iterating by DBO in stored collection
        private TableDef m_fstDBO;              //current DBO
        private EReferenceError m_refError;
        
        public CommandListIterator()
        {
            m_asTemp = new HashMap<String,Integer>();
            m_depStck = new Stack<DBOperation>();
            m_iNext = m_desiredTables.values().iterator();
            m_fstDBO = m_iNext.hasNext() ? m_iNext.next() : null;
        }
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            if(!m_depStck.empty())
                return true;
            if(m_fstDBO == null)
                return false;
        
            
            int asDBO;
            
            try{
            
	            while(m_fstDBO!=null){
	            	if(!m_fstDBO.isExternalTable()){
		            	asDBO = actualDBOVersion(m_fstDBO.getEntityName());
		            	if(asDBO < m_fstDBO.getVersion()){
	                        TableOperation op=null;
		            		if(asDBO==0){
		            			op = createCreateTableOperation(m_fstDBO);
		            			if(null != op)
		            				asDBO += op.getVersionIncrease();
		            		} 
	                        if(op == null){
		                        List<TableUpdateOperation> histDBO = m_fstDBO.getHistory(++asDBO);
		                        op = histDBO.get(0);
		            		}
	                        
	                        op = createDatabaseOperation(m_fstDBO, op);
	                        pushStack(new DBOperation(m_fstDBO.getEntityName(), op));
		            		break;
		            	}
	            	}	

	            	m_fstDBO = m_iNext.hasNext() ? m_iNext.next() : null;
	            }
            } catch(EReferenceError e){
                m_refError = e;
                return false;            	
            }	            
            
            return m_fstDBO!=null;            
        }
        
        private TableOperation createDatabaseOperation(TableDef td,
				TableOperation op) {
        	IndexDef idx = null;
        	if(op instanceof AddIndexOperation){
        		idx = ((AddIndexOperation)op).getIndex();
        	} else if (op instanceof DeleteIndexOperation){
        		TableOperation revOp = op.getReverseOperation();
        		idx = ((AddIndexOperation)revOp).getIndex();
        	}
        	if(null != idx){
        		if(null != m_maxIndexSize && m_maxIndexSize < indexSize(td, idx))
        			op = new EmptyCommand();
        	}
        	return op;
		}

		private int indexSize(TableDef td, IndexDef idx) {
			int res = 0;
			for(String column : idx.getColumns()){
				ElementVerStored<FieldDef> e = td.getFields().findElement(column);
				if(null == e){
					ApiAlgs.assertNotNull(e, td.getEntityName() + "." + column);
				} else {
					FieldDef fd = e.getElement();
					res += fd.getSize();
				}
			}
			return res;
		}

		/* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public DBOperation next() {
            DBOperation op = m_depStck.pop();
            incActualDBOVersion(op.getDBOName(), op.getOperation().getVersionIncrease());
            return op;
        }
        
        public void remove() {}
        
        private CreateTableOperation createCreateTableOperation(TableDef tabDef) throws EUnknownReference{
        	List<TableUpdateOperation> hist = tabDef.getHistory(1);
        	List<TableUpdateOperation> resList = new ArrayList<TableUpdateOperation>();
            int stopIdx;
            for(stopIdx=0; stopIdx!=hist.size(); stopIdx++){
                TableUpdateOperation operation = hist.get(stopIdx);
                if(!(operation instanceof AddColumnOperation) && 
                        !(operation instanceof AddPrimaryKeyOperation)){
                    if(operation instanceof AddForeignKeyOperation){
                    	AddForeignKeyOperation addFK = (AddForeignKeyOperation)operation;
                    	if(!tabDef.isSupportForeignKeys()){
                    		break; // end of pack
                    	}
                    	addFK.setCreateFK(true);
                        IndexDef fk = addFK.getIndex();
                        int vDes = getParentIndexVersion(fk.m_parentTable, fk.m_parentIndex);
                        int vActual = actualDBOVersion(fk.m_parentTable);
                        if(vDes > vActual){
                            break;
                        }
                    }
                    else break;
                }
                if(operation instanceof AddColumnOperation){
                	AddColumnOperation addCol = (AddColumnOperation) operation;
                	addColumnOperation(tabDef.getEntityName(), addCol);
                	operation = getRealAddColumnOperation(tabDef, addCol);
                }
                operation = (TableUpdateOperation) createDatabaseOperation(tabDef, operation);
                resList.add(operation);
            }
            
            CreateTableOperation res = null;
            if(stopIdx!=0){
            	res = new CreateTableOperation(resList);
            	res.setDbName(tabDef.getDbName());
            }
            return res;
        }
        
        private AddColumnOperation getRealAddColumnOperation(TableDef tabDef, AddColumnOperation operation) {
        	if(!tabDef.isSupportNotNullableFields()){
        		AddColumnOperation addCol = (AddColumnOperation) operation;
        		FieldDef field = addCol.getField();
        		if(field.bNotNull()){
        			//Поле не должно входить в уникальный индекс
        			Indices indieces = tabDef.getIndices();
        			String fName = field.getName();
        			boolean bFieldIsInUniqueIndex = false;
        			for(int i=0; i<indieces.size(); i++){
        				IndexDef index = indieces.getElement(i);
        				if(index.isUnique() && index.getColumns().contains(fName)){
        					bFieldIsInUniqueIndex = true;
        					break;
        				}
        			}
        			if(!bFieldIsInUniqueIndex){
        				FieldDef realField = field.clone();
        				realField.m_bNotNull = false;
        				operation = new AddColumnOperation(realField);
        			}
        		}
        	}
			return operation;
		}
		private int getParentIndexVersion(String tab, String name) throws EUnknownReference{
        	Indices indices = m_desiredTables.get(tab).getIndices();
            return indices.find(indices.getAddedElements().iterator(), new UniqueIndexCondition(name)).getVersion();
        }
        
        /**
         * @param dboName - DBTable name for which to see version
         * @return DBTable version in algorithm  
         */
        private int actualDBOVersion(String dboName){
            Integer asv = m_asTemp.get(dboName);
            if(asv==null){
                asv = getActualVersion(dboName);
                m_asTemp.put(dboName, asv);
            }
            return asv; 
        }
        
        /**
         * @param dboName - DBTable name for which to increase version
         * @param dv 
         * @return - next object version
         */
        private Integer incActualDBOVersion(String dboName, int dv){
            Integer i = m_asTemp.get(dboName);
            i += dv;
            m_asTemp.put(dboName, i);
            return i;
        }
        
        /**
         * push operation in stack and all operation, he depends
         * algorithm haves recursive element
         * @param op - operation for pushing
         * @throws ECycleReference 
         * @throws EUnknownReference 
         * @throws EMetadataException 
         * @throws EDBObjectException 
         */
        private void pushStack(DBOperation op) throws EReferenceError{
            int srchOp = m_depStck.search(op);
            if(srchOp != -1){
                throw new ECycleReference(op.getDBOName(), op.getOperation().getName());  //$NON-NLS-1$
            }
            
            if(op.getOperation() instanceof AddColumnOperation){
            	AddColumnOperation addCol = (AddColumnOperation)op.getOperation();
            	addColumnOperation(op.getDBOName(), addCol);
            	op = new DBOperation(op.getDBOName(), 
            			getRealAddColumnOperation(m_desiredTables.get(op.getDBOName()), addCol));
            }
            else if(op.getOperation() instanceof DeleteIndexOperation){
            	DeleteIndexOperation delCnstr = (DeleteIndexOperation) op.getOperation();
            	if(IndexDef.TYPE.FOREIGN_KEY.equals(delCnstr.getType()) &&
            			!m_desiredTables.get(op.getDBOName()).isSupportForeignKeys()){
            		// Delete index  how it store in DB
            		op = new DBOperation(op.getDBOName(), new DeleteIndexOperation(delCnstr.getName(), IndexDef.TYPE.INDEX, delCnstr.isUniqueIndex()));
            	}		
            }
            
            m_depStck.push(op);
            if(op.getOperation() instanceof AddForeignKeyOperation)
            {
            	AddForeignKeyOperation fkOp = (AddForeignKeyOperation) op.getOperation();
                IndexDef fk = (fkOp).getIndex();
                TableDef parentTable = m_desiredTables.get(fk.m_parentTable);
                int parentIndexVersion = getParentIndexVersion(fk.m_parentTable, fk.m_parentIndex); 
                pushDependent(parentTable, parentIndexVersion);
                
                fkOp.setCreateFK(m_desiredTables.get(op.getDBOName()).isSupportForeignKeys());
                
            }
            if(op.getOperation() instanceof DeleteIndexOperation){
                DeleteIndexOperation delCnstr = (DeleteIndexOperation) op.getOperation();
                for (TableDef table : m_desiredTables.values()) {
                    ElementVerStored<IndexDef> fk = table.getIndices().find(table.getIndices().getCurrentElements().iterator(), new IndexDef.RefCondition(op.getDBOName(), delCnstr.getName()));
                    if(fk != null)
                        throw new EUnknownReference(table.getEntityName(), fk.getElement().getName(),fk.getElement().m_parentIndex); //$NON-NLS-1$
                }
                for (TableDef table : m_desiredTables.values()) {
                	int vDep = -1;
                    ListIterator<ElementVerStored<IndexDef>> delIt = table.getIndices().getDeletedElements().listIterator();
                	while(delIt.hasNext()){
                        ElementVerStored<IndexDef> fk = table.getIndices().find(delIt, new IndexDef.RefCondition(op.getDBOName(), delCnstr.getName()));
                        if(fk != null && fk.getVersion() > vDep)
                            vDep= fk.getVersion();
                	}
                    if(vDep != -1){
                        pushDependent(table, vDep);                            
                    }                            
                }
            }

        }
        
        private void addColumnOperation(String dboName, AddColumnOperation addCol) {
        	if(addCol.getField().isAutoincrement()){
        		String fname = addCol.getField().getName();
        		String dbName;
        		int ich = dboName.lastIndexOf(".");
        		if(ich != -1){
        			dbName = dboName.substring(ich+1, ich+4);
        		}
        		else{
        			dbName = dboName.substring(0, 3);
        		}
        		DBOperation dbOp = new DBOperation(dboName, 
        				new SetAutoIncFieldOperation("TrigAutoIncOn"+fname, "GEN_"+dbName+"_"+fname, fname));
        		m_depStck.push(dbOp);
        	}

		}

		private void pushDependent(TableDef table, int version) throws EReferenceError{
            int av = actualDBOVersion(table.getEntityName());
            int createBatchSize=0;
            CreateTableOperation createOp=null;
            if(version > av){
                ++av;
                List<TableUpdateOperation> h = table.getHistory(av);
                if(av == 1){
                	createOp = createCreateTableOperation(table);
                	if(createOp !=null){
                		createBatchSize = createOp.getVersionIncrease();
                	}                		
                }
                for (int i = version-av; i >= createBatchSize; i--) {
                    pushStack(new DBOperation(table.getEntityName(), h.get(i)));
                }
                if(createOp != null){
                	pushStack(new DBOperation(table.getEntityName(), createOp));
                }
            }
        }
        
        /**
         * if errors happened during iteration process then not null 
         */
        public EReferenceError getOccuredError(){
            return m_refError;
        }
    }
    
    private LinkedHashMap<String, TableDef>  m_desiredTables;
    private IActualState  m_actualTables;
	private Integer m_maxIndexSize;

        
    public DBTables(IActualState as, HashMap<String, TableDef> desiredTables){
        m_desiredTables = new LinkedHashMap<String, TableDef>(desiredTables);
        m_actualTables  = as;
    }    
    
    public DBTables() {
        m_desiredTables = new LinkedHashMap<String, TableDef>();
        m_actualTables = null;
    }

    /**
     * get list of command, for transfom database from actual state 
     * to state that DBObjects requires
     * @return list of command
     * @throws EReferenceError - one of operations has wrong index or cycle reference 
     */
    public List<DBOperation> getCommandList() throws EReferenceError{
        
        setReferences();
        
        ArrayList<DBOperation> cmdList = new ArrayList<DBOperation>();
        
        CommandListIterator iCL = new CommandListIterator();
        while(iCL.hasNext())
            cmdList.add(iCL.next());
        
        EReferenceError err = iCL.getOccuredError();
        if(err != null)
            throw err;
        
        return cmdList;
    }

    private void setReferences() throws EUnknownReference {
        for (TableDef tab: m_desiredTables.values()) {
        	if(tab.isExternalTable())
        		continue;
        	int actVers = Math.min(tab.getVersion(), getActualVersion(tab.getEntityName())+1);
            for (TableUpdateOperation op: tab.getHistory(actVers)) {
                if(op instanceof AddForeignKeyOperation){
                    AddForeignKeyOperation addFk = (AddForeignKeyOperation)op;
                    TableDef parentTable = m_desiredTables.get(addFk.getParentTable());
                    if(parentTable == null){    //no such table
                        throw new EUnknownReference(tab.getEntityName(), addFk.getName(), addFk.getParentTable()); //$NON-NLS-1$
                    }
                    
                    ElementVerStored<IndexDef> idx = parentTable.getIndices().find(parentTable.getIndices().getAddedElements().iterator(), new UniqueIndexCondition(addFk.getParentIndex()));
                    if(idx == null){ //no such index in created primary keys
                    	Log log = ApiAlgs.getLog(this);
                    	for(int i=0; i< parentTable.getIndices().size(); i++){
                    		IndexDef e = parentTable.getIndices().getElement(i);
                    		log.trace("Type:"+e.getType()+", Name:"+e.getName());
                    	}
                        throw new EUnknownReference(tab.getEntityName(), addFk.getName(), addFk.getParentIndex()); //$NON-NLS-1$
                    }
                    addFk.setRefColumns(idx.getElement().getColumns());
                }
            }
        }
    }
    
    private int getActualVersion(String tabName){
        Integer actTab = m_actualTables.getVersion(tabName);
        return actTab != null ? actTab : 0;
    }

    public Set<String>list() {
        return m_desiredTables.keySet();
    }

    public void add(TableDef tab) {
        m_desiredTables.put(tab.getEntityName(), tab);
    }

    public TableDef get(String tabName) {
        return m_desiredTables.get(tabName);
    }

    public void setActualState(IActualState as) {
        m_actualTables = as;
    }

    public int getActualState(String tabName) {
        return m_actualTables.getVersion(tabName);
    }

    public void remove(String tabName) {
        m_desiredTables.remove(tabName);
    }

	public void setMaxIndexSize(int v) {
		m_maxIndexSize = v;
	}
    

    
}
