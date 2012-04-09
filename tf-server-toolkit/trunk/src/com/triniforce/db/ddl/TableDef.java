/**
 * Copyright(C) Triniforce 
 * All Rights Reserved.
 * 
 */

package com.triniforce.db.ddl;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.server.soap.NamedVar;
import com.triniforce.server.soap.VObject;

/**
 * Class for presentation of table in base
 */
public class TableDef extends com.triniforce.utils.Entity implements Cloneable{

    public static class EDBObjectException extends RuntimeException {        
        private static final long serialVersionUID = 271758086037920518L;        
        public String m_dboName;
        public EDBObjectException(String dboName){
            super();
            m_dboName = dboName;
        }
        
        @Override
        public String getMessage() {
        	return MessageFormat.format("TableDef: <{0}>", m_dboName); //$NON-NLS-1$
        }
    }
    
    public static class EInvalidModificationSequence extends EDBObjectException {
        private static final long serialVersionUID = 7187962719763720679L;
        int m_version;
        public EInvalidModificationSequence(String dboName, int version){            
            super(dboName);
            m_version = version;
        }
    }    
    
    public static class EMetadataException extends EDBObjectException{
        private static final long serialVersionUID = -635317593727868864L;
        private static String m_objName;
        public EMetadataException(String dboName, String objName){
            super(dboName);
            m_objName = objName;
        }
        @Override
        public String getMessage() {
            return m_dboName+'/'+m_objName;
        }        
        public String getObject(){
            return m_objName;
        }
    }
        
    public static class ENameRedefinition extends EMetadataException {
        private static final long serialVersionUID = -3398009947702351536L;

        public ENameRedefinition(String dboName, String fName) {
            super(dboName, fName);
        }
       
    }
    public static class EReferenceError extends EMetadataException {
        private static final long serialVersionUID = 8581402981833032283L;
        
        public String m_opName;

        public EReferenceError(String dboName, 
                String opName) {
            super(dboName, opName);
            m_opName = opName;
        }
        @Override
        public String getMessage() {
            return m_dboName+'/' + m_opName;
        }        
    }
    public static class EUnknownReference extends EReferenceError {
        private static final long serialVersionUID = 119991075729977220L;

        String m_refName;

        public EUnknownReference(String dboName, 
                String opName, String refName) {
            super(dboName, opName);
            m_refName = refName;
        }
        
        @Override
        public String getMessage() {
            return m_dboName+'/' + m_opName + '/' + m_refName;
        }
    }
    public static class ECycleReference extends EReferenceError {
        private static final long serialVersionUID = 5175217339600915063L;

        public ECycleReference(String dboName, String opName) {
            super(dboName, opName);
        }
    }
    
    public static class EInvalidDefinitionArgument extends IllegalArgumentException {
        private static final long serialVersionUID = 8596468757322376980L;
        public EInvalidDefinitionArgument(String msg) {
            super(msg);
        }
    }    

    public static class EUnknownHistoryRequest extends RuntimeException {
        private static final long serialVersionUID = 1893057045311180765L;
        public String m_dboName;
        public int m_dboVer;
        public int m_dboReqVer;

        public EUnknownHistoryRequest(String dboName, int dboVer, int dboReqVer) {
            super(MessageFormat.format("<{0}v.{1}> req.{2}", dboName, dboVer, dboReqVer)); //$NON-NLS-1$
            m_dboName = dboName;
            m_dboVer = dboVer;
            m_dboReqVer = dboReqVer;
        }
    }
    
    /**
     * Table primitives (fields or indices)
     */
    public interface IElementDef{
        
        public interface ISearchCondition<T extends IElementDef>{
            boolean is(T element);
        }
        
        public static class NameCondition<T extends IElementDef> implements ISearchCondition<T>{
            private String m_srchName;        
            public NameCondition(String name){
                m_srchName = name;
            }        
            public boolean is(IElementDef element) {
                return m_srchName.equals(element.getName());
            }
        }
        
        String getName();
    } 
    
   
    /**
     * Field definition, all types by one class 
     */
    public static class FieldDef implements IElementDef, com.triniforce.utils.IName, Cloneable, Serializable{
        private static final long serialVersionUID = -5431378065902561984L;
        
		private static final int[] SQL_TYPES= {Types.INTEGER,Types.SMALLINT,
            Types.REAL,Types.TIMESTAMP,Types.BLOB,Types.DECIMAL,
            Types.CHAR,Types.CHAR, Types.VARCHAR, Types.VARCHAR, Types.BIGINT, 
            Types.DOUBLE, Types.LONGVARBINARY, Types.FLOAT, Types.TIME, 
            Types.DATE};
        private static final ColumnType[] DDL_TYPES = {ColumnType.INT,ColumnType.SMALLINT,
            ColumnType.FLOAT,ColumnType.DATETIME,ColumnType.BLOB,ColumnType.DECIMAL,
            ColumnType.CHAR,ColumnType.NCHAR, ColumnType.VARCHAR, ColumnType.NVARCHAR, ColumnType.LONG, 
            ColumnType.DOUBLE, ColumnType.BLOB, ColumnType.FLOAT, ColumnType.DATETIME, 
            ColumnType.DATETIME};
        
        
        private static final String NV_NAME = "name"; //$NON-NLS-1$
        private static final String NV_TYPE = "type"; //$NON-NLS-1$
        private static final String NV_SIZE = "size"; //$NON-NLS-1$
        private static final String NV_SCALE = "scale"; //$NON-NLS-1$
        private static final String NV_NOTNULL = "bMustHaveValue"; //$NON-NLS-1$
        private static final String NV_DEFAULT = "defVal"; //$NON-NLS-1$
        
        public static class EScaleLessThanZero extends TableDef.EInvalidDefinitionArgument{
            private static final long serialVersionUID = 6863645760353959066L;
            public EScaleLessThanZero() {
                super(NV_SCALE);
            }
        }
        
        public static class EPrecisionLessThanScale extends TableDef.EInvalidDefinitionArgument{
            private static final long serialVersionUID = -6288018100996471763L;
            public EPrecisionLessThanScale() {
                super("Precision"); //$NON-NLS-1$
            }
        }
        
        public static class EWrongVObjectType extends RuntimeException{
            private static final long serialVersionUID = 1520074990740016775L;
            public EWrongVObjectType(String wrongType) {
                super(wrongType);
            }
        }
        
        public static class FieldComparator implements Comparator<FieldDef>{
            boolean m_byName;
            boolean m_byType;
            boolean m_byNotNullFlag;
            
            public FieldComparator(boolean byName, boolean byType, boolean byNotNullFlag){
                m_byName = byName;
                m_byType = byType;
                m_byNotNullFlag = byNotNullFlag;
            }
            
            public int compare(FieldDef arg0, FieldDef arg1) {
                int r;
                if(m_byName){
                    r = arg0.m_name.compareTo(arg1.m_name);
                    if(r!=0)
                        return r;
                }
                if(m_byType){
                    r = arg0.m_type.compareTo(arg1.m_type);
                    if(r!=0)
                        return r;                    
                }
                if(m_byNotNullFlag){
                    r = Boolean.valueOf(arg0.m_bNotNull).compareTo(Boolean.valueOf(arg1.m_bNotNull));
                    if(r!=0)
                        return r;
                }
                    
                return 0;
            }
            
        }
        
        /**
         * Types 
         */
        public enum ColumnType {
            INT, SMALLINT, FLOAT, DATETIME, DECIMAL, CHAR, NCHAR, VARCHAR, NVARCHAR, BLOB, LONG, DOUBLE 
        }
        
        String      m_name;
        ColumnType  m_type;
        int         m_size;
        int         m_scale;
        boolean     m_bNotNull;
        Object      m_defaultValue;

		private boolean m_bAutoInc= false;

        private FieldDef(String name, ColumnType type, int size, int scale,  boolean bMustHaveValue, Object defVal){
            m_name = name;
            m_type = type;
            m_size = size;
            m_scale = scale;            
            m_bNotNull = bMustHaveValue;
            m_defaultValue = defVal;
        }
        
        public FieldDef clone(){
            FieldDef res = new FieldDef(m_name, m_type, m_size, m_scale, m_bNotNull,m_defaultValue);
            res.setAutoincrement(isAutoincrement());
            return res;
            
        }
        
        @Override
        public boolean equals(Object arg0) {
            if(arg0==null) return false;
            FieldDef f = (FieldDef)arg0;
            return f==null ? false : m_name.equals(f.m_name) && m_type == f.m_type;
        }
        /* (non-Javadoc)
         * @see com.triniforce.db.ddl.DBTable.ITablePrimitiveDef#getName()
         */
        public String getName(){
            return m_name;
        }
        
        /**
         * Create a simple field (INT, FLOAT)
         * @param name - field name
         * @param type - field type
         * @param bMustHaveValue - field can contain null
         * @param defVal - default field value
         * @return - field definition
         */
        public static FieldDef createScalarField(String name, ColumnType type, boolean bMustHaveValue, Object defVal){
        	if(!isScalarType(type)){
            	throw new InvalidParameterException(type.toString()); 
        	}
            return new FieldDef(name, type, 1, 0, bMustHaveValue, defVal);
        }
        
        /**
         * Create a simple field (INT, FLOAT)
         * @param name - field name
         * @param type - field type
         * @param bMustHaveValue - field can contain null
         * @return - field definition
         */
        public static FieldDef createScalarField(String name, ColumnType type, boolean bMustHaveValue){
            return createScalarField(name, type, bMustHaveValue, null);            
        }
        
        /**
         * Create array field (CHAR(N) or FLOAT(N))
         * @param name - field name
         * @param type - field type
         * @param size - field size
         * @param bMustHaveValue - field can contain null
         * @return - field definition
         */
        public static FieldDef createStringField(String name, ColumnType type, int size, boolean bMustHaveValue, Object defVal){
        	switch(type){
	        case CHAR:
	        case NCHAR:
	        case VARCHAR:
	        case NVARCHAR:
	        	break;
	        default:
	        	throw new InvalidParameterException(type.toString());
        	}
            return new FieldDef(name, type, size, 0, bMustHaveValue, defVal);
        }       
      
        /**
         * Create a decimal field (DECIMAL(M,N))
         * @param name - field name
         * @param prec - field precision
         * @param scale - field scale
         * @param bMustHaveValue - field can contain null
         * @return - field definition
         */
        public static FieldDef createDecimalField(String name, int prec, int scale, boolean bMustHaveValue, Object defVal){
        	if(scale < 0)
        		throw new EScaleLessThanZero();
        	if(prec < scale)
        		throw new EPrecisionLessThanScale(); 

            return new FieldDef(name, ColumnType.DECIMAL, prec, scale, bMustHaveValue, defVal);
        }

        public static boolean isStringType(ColumnType t){
        	switch(t){
	        case CHAR:
	        case NCHAR:
	        case VARCHAR:
	        case NVARCHAR:
	        	return true;
	        default:
	        	return false;
        	}      	
        }
        public static boolean isScalarType(ColumnType t){
        	switch(t){
            case INT:
            case SMALLINT:
            case LONG:
            case FLOAT: 
            case DOUBLE: 
            case DATETIME:
            case BLOB:
	        	return true;
	        default:
	        	return false;
        	}      	
        }
        public static boolean isDecimalType(ColumnType t){
        	return t.equals(ColumnType.DECIMAL);
        }               
        
        public static int sqlType(ColumnType t){
            int i;
            for (i= 0; i < DDL_TYPES.length; i++) {
                ColumnType ddlType = DDL_TYPES[i];
                if(ddlType == t)
                    break;
            }
            return i==DDL_TYPES.length ? -i-1 : SQL_TYPES[i];
        }

        public static ColumnType ddlType(int t){
            int i;
            for (i= 0; i < SQL_TYPES.length; i++) {
                int sqlType = SQL_TYPES[i];
                if(sqlType == t)
                    break;
            }
            if(i==SQL_TYPES.length)
                throw new TableDef.EInvalidDefinitionArgument("sql type : " + t); //$NON-NLS-1$
            return DDL_TYPES[i];
        }

        public Object getDefaultValue() {
            return m_defaultValue;
        }

        public ColumnType getType() {
            return m_type;
        }
        
        public boolean bNotNull(){
            return m_bNotNull;
        }
        
        public int getSize(){
            return m_size;
        }
        
        public void setSize(int v){
        	m_size = v;
        }
        
        public int getScale(){
            return m_scale;
        }

        public VObject toVObject() {
            ArrayList<Object> attr = new ArrayList<Object>();
            attr.add(new NamedVar(NV_NAME, m_name));
            attr.add(new NamedVar(NV_TYPE, m_type.toString()));
            if(isStringType(m_type) || isDecimalType(m_type)){
                attr.add(new NamedVar(NV_SIZE, m_size));
                if(isDecimalType(m_type))
                    attr.add(new NamedVar(NV_SCALE, m_scale));
            }
            attr.add(new NamedVar(NV_NOTNULL, m_bNotNull ? 1 : 0));
            if(m_defaultValue != null)
                attr.add(new NamedVar(NV_DEFAULT, m_defaultValue));
            
            return new VObject(getClass().getName(), attr.toArray(new NamedVar[attr.size()])); 
        }

        public static FieldDef fromVObject(VObject vObj) {
            if(!FieldDef.class.getName().equals(vObj.getType()))
                throw new EWrongVObjectType(vObj.getType());
            
            FieldDef res = null;

            String name = vObj.getProp(NV_NAME);
            ColumnType type = ColumnType.valueOf((String)vObj.getProp(NV_TYPE));
            boolean bNotNull = (((Integer)vObj.getProp(NV_NOTNULL)).equals(Integer.valueOf(1)));
            Object defVal = vObj.queryProp(NV_DEFAULT);
            
            if(FieldDef.isScalarType(type)){
                res = FieldDef.createScalarField(name, type, bNotNull, defVal);
            }
            else if(FieldDef.isStringType(type)){
                Integer size = vObj.getProp(NV_SIZE);
                res = FieldDef.createStringField(name, type, size, bNotNull, defVal);
            }
            else if(FieldDef.isDecimalType(type)){
                Integer size = vObj.getProp(NV_SIZE);
                Integer scale = vObj.getProp(NV_SCALE);
                res = FieldDef.createDecimalField(name, size, scale, bNotNull, defVal);
            }
            return res;
        }

		public void setAutoincrement(boolean b) {
			m_bAutoInc = b; 
			
		}

		public boolean isAutoincrement() {
			return m_bAutoInc;
		}
    }
    /**
     * Index definition, primary key and foreign key in one
     */
    public static class IndexDef implements IElementDef, Serializable{
		private static final long serialVersionUID = 6375874272406510726L;

		public enum TYPE{PRIMARY_KEY, FOREIGN_KEY, INDEX};
        
        String m_name;
        TYPE   m_type;
        List<String> m_columns;
        boolean m_bUnique;
        boolean m_bAscending;
        boolean m_bActive;        
        String m_parentTable;
        String m_parentIndex;
                
        private IndexDef(String name, TYPE type, List<String> columns, boolean bUnique, 
                boolean bAscending, boolean bActive, String parentTab, String parentIndex){
            m_name = name;
            m_type = type;
            m_columns = columns;
            m_bUnique = bUnique;
            m_bAscending = bAscending;
            m_bActive = bActive;
            m_parentTable = parentTab;
            m_parentIndex = parentIndex;
        }

        public void setType(TYPE type) {
			m_type = type;
		}

		public void setParentTable(String parentTable) {
			m_parentTable = parentTable;
		}

		public void setParentIndex(String parentIndex) {
			m_parentIndex = parentIndex;
		}

		/**
         * Create primary key
         * @param name - key name
         * @param columns - indexed columns
         * @return - index definition
         * @throws EInvalidDefinitionArgument 
         */
        public static IndexDef primaryKey(String name, List<String> columns){
            if(columns.isEmpty())                
                throw new TableDef.EInvalidDefinitionArgument("columns"); //$NON-NLS-1$
            return new IndexDef(name, TYPE.PRIMARY_KEY, columns, true, true, true, null, null);
        }

        /**
         * Create foreign key
         * @param name - key name
         * @param columns - indexed columns
         * @param parentTab - parent table
         * @return - index definition
         * @throws EInvalidDefinitionArgument 
         */
        public static IndexDef foreignKey(String name, List<String> columns, String parentTab, String parentIndex) throws EInvalidDefinitionArgument{
            if(columns.isEmpty())                
                throw new EInvalidDefinitionArgument("columns"); //$NON-NLS-1$
            return new IndexDef(name, TYPE.FOREIGN_KEY, columns, false, true, true, parentTab, parentIndex);
        }
        
        /**
         * Create primary key
         * @param name - key name
         * @param columns - indexed columns
         * @return - index definition
         * @throws EInvalidDefinitionArgument 
         */
        public static IndexDef createIndex(String name, List<String> columns, boolean bUnique, boolean bAsc){
            if(columns.isEmpty())                
                throw new TableDef.EInvalidDefinitionArgument("columns"); //$NON-NLS-1$
            return new IndexDef(name, TYPE.INDEX, columns, bUnique, bAsc, true, null, null);
        }
        
        public List<String> getColumns() {
            return m_columns;         
        }

        public String getName() {
            return m_name;
        }

        @Override
        public boolean equals(Object arg0) {
            if(arg0==null) return false;
            IndexDef f = (IndexDef)arg0;
            return f==null ? false : m_name.equals(f.m_name) && m_type.equals(f.m_type);
        }

        public TYPE getType() {
            return m_type;
        }
        
        public boolean isUnique(){
            return m_bUnique;
        }
        
        public String getParentTable(){
            return m_type.equals(TYPE.FOREIGN_KEY) ? m_parentTable : null; 
        }
        
        public String getParentIndex(){
            return m_type.equals(TYPE.FOREIGN_KEY) ? m_parentIndex : null; 
        }
        
        

        public static class TypeCondition implements IElementDef.ISearchCondition<IndexDef>{
            private TYPE m_srchType;
            TypeCondition(IndexDef.TYPE t){
                m_srchType = t;            
            }            
            public boolean is(IndexDef element) {
                return m_srchType.equals(element.getType());
            }            
        }

        public static class RefCondition implements IElementDef.ISearchCondition<IndexDef>{
            private String m_tab, m_idx;
            RefCondition(String tab, String idx){
                m_tab = tab;
                m_idx = idx;
            }            
            public boolean is(IndexDef element) {
                return m_tab.equals(element.m_parentTable) && m_idx.equals(element.m_parentIndex);
            }            
        }

    }
    
    
    public static class ElementVerStored<T extends IElementDef>{
        private int m_version;
        private T m_element;
        
        public ElementVerStored(int v, T f){
            m_version = v;
            m_element = f;
        }
		public T getElement() {
			return m_element;
		}
		public int getVersion() {
			return m_version;
		}
    }
      
    /**
     *Collection for store table primitives with version of operation that creates it
     * @param <Primitive type>
     */    
    public abstract class TableElements<T extends IElementDef>{        
        protected ArrayList<ElementVerStored<T>> m_addedElements;
        protected ArrayList<ElementVerStored<T>> m_deletedElements;

        protected TableElements(){
            m_addedElements = new ArrayList<ElementVerStored<T>>();
            m_deletedElements = new ArrayList<ElementVerStored<T>>();
        }

        public T getElement(int idx) {
        	if(idx<0 || idx >=size())
        		throw new IndexOutOfBoundsException();
        	return m_addedElements.get(idx).m_element;
        }

        public int size() {
            return m_addedElements.size() - m_deletedElements.size();
        }

        protected void addElement(T node) throws EMetadataException{
        	if(find(m_addedElements.iterator(), new IElementDef.NameCondition<T>(node.getName())) != null)
        		throw new ENameRedefinition(getEntityName(),node.getName()); //$NON-NLS-1$
            m_addedElements.add(size(), new ElementVerStored<T>(getVersion()+1, node));                        
        }
        
        protected T removeElement(int idx) throws EMetadataException{
            if(idx < 0 || idx >= size())
                throw new IndexOutOfBoundsException();
            
            ElementVerStored<T> buf;
            buf = m_addedElements.get(idx);
            if(idx != size()-1){
                m_addedElements.remove(idx); //swap 
                m_addedElements.add(buf);
            }            
            m_deletedElements.add(new ElementVerStored<T>(getVersion()+1, buf.m_element));
            
            return buf.m_element;
        }
        
        
        /**
         * Find elemnt in collection 
         * @param it - start iterator
         * @param cond - search condition
         * @return - finded element if is or null otherwies 
         */
        public ElementVerStored<T> find(Iterator<ElementVerStored<T>> it, IElementDef.ISearchCondition<T> cond){
            ElementVerStored<T> res = null, cur=null;
            while(res==null && it.hasNext()){
                cur = it.next();
                if(cond.is(cur.getElement()))
                    res = cur;
            }
        	return res;
        }
        
        /**
         * Get field index by name
         * @param fName - field name
         * @return - finded element if is or null otherwies 
         */
        public ElementVerStored<T> findElement(String fName){
            return find(getCurrentElements().iterator(), new IElementDef.NameCondition<T>(fName));
        }
        
        public int getPosition(String fName){
            ListIterator<ElementVerStored<T>> i = getCurrentElements().listIterator();
            ElementVerStored<T> e = find(i, new IElementDef.NameCondition<T>(fName));
            return e==null ? -1 : i.previousIndex();
        }
        
        /**
         * Get elements that was added in table
         * @return - added elements
         */
        public List<ElementVerStored<T>> getAddedElements(){
        	return Collections.unmodifiableList(m_addedElements);
        }
        
        /**
         * Get elements that was deleted from table
         * @return - deleted elements
         */
        public List<ElementVerStored<T>> getDeletedElements(){
        	return Collections.unmodifiableList(m_deletedElements);
        }  

        /**
         * Get elements that in table
         * @return - table elements
         */
        public List<ElementVerStored<T>> getCurrentElements(){
        	return Collections.unmodifiableList(m_addedElements.subList(0, size()));
        }  

        /**
         * Get a version of field
         * @param iField - field index
         * @return - version of table where operation created
         */
        public int getElementVersion(int iField){
            return m_addedElements.get(iField).m_version;
        }

    }
        
    public class Fields extends TableElements<FieldDef>{

        @Override
        protected FieldDef removeElement(int idx) throws EMetadataException {
        	String fName = getElement(idx).getName();
        	for (int i = 0; i < getIndices().size(); i++) {
        		IndexDef index = getIndices().getElement(i);
        		if(index.m_columns.contains(fName))
                    throw new EMetadataException(getEntityName(), fName);				 //$NON-NLS-1$
			}
            return super.removeElement(idx);
        }        
    }
    
    public class Indices extends TableElements<IndexDef>{
                        
        /* (non-Javadoc)
         * @see com.triniforce.db.ddl.DBTable.TableElements#addElement(com.triniforce.db.ddl.DBTable.INodeDef)
         */
        @Override
        protected void addElement(IndexDef index) throws EMetadataException{            
            for (String column: index.m_columns) {
                ElementVerStored<FieldDef> field = getFields().findElement(column);
                if(field == null)
                    throw new EUnknownReference(getEntityName(), index.getName(), column); //$NON-NLS-1$
                if(index.m_bUnique){
                	if(!field.getElement().m_bNotNull)
                		throw new EMetadataException(getEntityName(), column); //$NON-NLS-1$
                }
            }
            if(index.getType().equals(IndexDef.TYPE.PRIMARY_KEY) &&
                    find(getCurrentElements().iterator(), new IndexDef.TypeCondition(IndexDef.TYPE.PRIMARY_KEY))!=null)
                throw new  EMetadataException(getEntityName(), index.getName());                      //$NON-NLS-1$
                    
            super.addElement(index);
        }        
    }

    private static final int MAX_TAB_NAME = 255; 
    
    private ArrayList<TableUpdateOperation> m_history 
                                = new ArrayList<TableUpdateOperation>();
    private Fields m_fields     = new Fields();
    private Indices m_indices   = new Indices();

	private boolean m_bSupportForeignKeys=false;
	private boolean m_bSupportNotNullableFields=true;
	
	private String m_dbName;
       
    public TableDef(String aName) throws EDBObjectException {
        super(aName);
        
        if(aName == null || aName.length() == 0 || aName.length() > MAX_TAB_NAME)
            throw new EDBObjectException(aName);         
    }
    
    public TableDef() {
        super();
    }
    
    /**
     * Version is number of modification under our subject
     */
    public int getVersion() {
        return m_history.size();
    }
    
    /**
     * @param from - start version (from this one we want history result) 
     * @return (creation history at <from> version)
     * @throws EVersionConflict when we query history of unproduced object
     */
    public List<TableUpdateOperation> getHistory(int from){
        --from;
        if(from < 0 || from > getVersion())
            throw new EUnknownHistoryRequest(getEntityName(), getVersion(), from+1); //$NON-NLS-1$
        return Collections.unmodifiableList(m_history.subList(from, m_history.size()));
    }
    
    /**
     * Add new version to DBOject
     * @param vId - version number
     * @param update - modification that we will store for run on database
     * @throws EDBObjectException 
     */
    public TableDef addModification(int vId, TableUpdateOperation update) throws EDBObjectException{
        if(vId != getVersion()+1)
            throw new EInvalidModificationSequence(getEntityName(), vId); //$NON-NLS-1$
      	update.apply(this);
       	m_history.add(update);
        return this;
    }
    
    /**
     * Get all fields of table
     */
    public Fields getFields() {
        return m_fields;        
    }
    /**
     * Get all indices of table
     */
    public Indices getIndices() {
        return m_indices;        
    }

    public TableDef addScalarField(int version, String fName, ColumnType type, boolean bMustHaveValue, Object defVal) throws EDBObjectException {
        addModification(version, new AddColumnOperation(FieldDef.createScalarField(fName, type, bMustHaveValue, defVal)));
        return this;
    }

    public TableDef addStringField(int version, String fName, ColumnType type, int size, boolean bMustHaveValue, String defVal) throws EDBObjectException {
        addModification(version, new AddColumnOperation(FieldDef.createStringField(fName, type, size, bMustHaveValue, defVal)));        
        return this;
    }

    
    
    public TableDef addDecimalField(int version, String fName, int size, int scale, boolean bMustHaveValue, Object defVal) throws EDBObjectException {
        addModification(version, new AddColumnOperation(FieldDef.createDecimalField(fName, size, scale, bMustHaveValue, defVal)));        
        return this;
    }
    
    public TableDef addField(int version, FieldDef fd){
        addModification(version, new AddColumnOperation(fd));        
        return this;
    }
    
    public TableDef addPrimaryKey(int version, String pkName, String[] fields) throws EDBObjectException {
        addModification(version, new AddPrimaryKeyOperation(pkName, Arrays.asList(fields)));                
        return this;
    }

    
    public TableDef addForeignKey(int version, String fkName, String[] fields, String parentTab, String parentKey, boolean bCascadeDelete) throws EDBObjectException {
        addModification(version, 
                new AddForeignKeyOperation(
                        fkName, 
                        Arrays.asList(fields), 
                        parentTab, parentKey, 
                        bCascadeDelete ? AddForeignKeyOperation.DELETE_RULE.CASCADE : AddForeignKeyOperation.DELETE_RULE.NOT_SPECIFIED,                        
                        AddForeignKeyOperation.UPDATE_RULE.NOT_SPECIFIED));                        
        return this;
    }

    public TableDef addIndex(int version, String name, String[] columns, boolean bUnique, boolean bAsc) throws EDBObjectException {
        addModification(version, new AddIndexOperation(IndexDef.createIndex(name, Arrays.asList(columns), bUnique, bAsc)));                        
        return this;
    }

    public TableDef deleteField(int version, String fName) throws EDBObjectException {
        addModification(version, new DeleteColumnOperation(fName));
        return this;
    }

    public TableDef deleteIndex(int version, String name, IndexDef.TYPE type) throws EDBObjectException {
        addModification(version, new DeleteIndexOperation(name, type, false));
        return this;
    }

    public TableDef deleteIndex(int version, String name, boolean bUnique) throws EDBObjectException {
        addModification(version, new DeleteIndexOperation(name, IndexDef.TYPE.INDEX, bUnique));
        return this;
    }
    
    @Override
    public TableDef clone() throws CloneNotSupportedException {
    	return clone(getEntityName());
    }
    
    public TableDef clone(String withName) throws CloneNotSupportedException {
        TableDef res = new TableDef(withName);
        
        int vId=1;
        for (TableUpdateOperation op : m_history) {
            res.addModification(vId++, op);
        }
        
        return res;
    }

	public void setSupportForeignKeys(boolean b) {
		m_bSupportForeignKeys = b;
	}
	public boolean isSupportForeignKeys() {
		return m_bSupportForeignKeys;
	}
	
	
	public void setSupportNotNullableFields(boolean b) {
		m_bSupportNotNullableFields = b;
	}
	public boolean isSupportNotNullableFields() {
		return m_bSupportNotNullableFields;
	}

	public String getDbName() {
		return m_dbName;
	}

	public void setDbName(String dbName) {
		m_dbName = dbName;
	}

    
}
