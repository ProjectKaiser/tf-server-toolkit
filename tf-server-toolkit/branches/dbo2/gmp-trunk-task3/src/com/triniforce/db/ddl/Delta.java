/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.db.ddl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.triniforce.db.ddl.DBTables.DBOperation;
import com.triniforce.db.ddl.Delta.DBMetadata.IIndexLocNames;
import com.triniforce.db.ddl.DiffLeader.ICmdFactory;
import com.triniforce.db.ddl.ResultSetWalker.IObjectFactory;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.IElementDef;
import com.triniforce.db.ddl.TableDef.IndexDef;
import com.triniforce.db.ddl.TableDef.TableElements;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.ddl.TableDef.IndexDef.TYPE;
import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IProfilerStack.PSI;

public class Delta {
	
	public static class DeltaSchema {
		private HashMap<String, TableDef> m_tables;

		DeltaSchema() {
			m_tables = new HashMap<String, TableDef>();
		}

		public Map<String, TableDef> getTables() {
			return m_tables;
		}

		public void addTable(TableDef table) {
			m_tables.put(table.getEntityName(), table);
		}
	}
	
	public interface IDBNames{
		String getDbName(String appName);
		String getAppName(String dbName);
	}
	
	public static class DBMetadata{
		
		private DatabaseMetaData m_md;
		private Set<String> m_tabs;
		private IDBNames m_dbNames;
		
		String CATALOG=null, SCHEME=null;

		public DBMetadata(DatabaseMetaData md, Set<String> tabs,IDBNames dbNames, IIndexLocNames indexLocNames) {
			m_md = md;
			m_tabs = tabs;
			m_dbNames = dbNames;
		}
		
		Map<String, Collection<FieldDef>> getFields() throws SQLException{
			HashMap<String, Collection<FieldDef>> res = 
				new HashMap<String, Collection<FieldDef>>();
			for(String tName : m_tabs){
				res.put(tName, new ArrayList<FieldDef>());
			}
			ResultSet tabs;
			tabs = m_md.getColumns(CATALOG, SCHEME, "%", null);
			while(tabs.next()){
				String dbName = tabs.getString("TABLE_NAME");
				Collection<FieldDef> col = res.get(m_dbNames.getAppName(dbName));
				if(null != col){
					col.add(rs2FieldDef(tabs));
				}
			}
			
			Iterator<Entry<String, Collection<FieldDef>>> i = res.entrySet().iterator();
			while(i.hasNext()){
				if(i.next().getValue().isEmpty())
					i.remove();
			}
			return res;
		}
		
		public interface IIndexLocNames{
			String getShortName(String dbTabName, String dbFullName);
		}
		
		
//		Map<String, Collection<IndexDef>> getIndexes() throws SQLException{
//			HashMap<String, Collection<IndexDef>> res = 
//				new HashMap<String, Collection<IndexDef>>();
//			for(String tName : m_tabs){
//				res.put(tName, new ArrayList<IndexDef>());
//			}
//			
////			Set<String> added = new HashSet<String>();
//			
//			ResultSet rs;
//			PSI psi = ApiAlgs.getProfItem("test", "getPrimaryKeys");
//			try{
//				rs = m_md.getPrimaryKeys(CATALOG, SCHEME, "%");
//				ResultSetWalker<IndexTemporary> walker = new ResultSetWalker<IndexTemporary>(
//						new IndexObjectFactory(IndexDef.TYPE.PRIMARY_KEY, "PK_NAME", "TABLE_NAME", m_dbNames, m_indexLocNames),
//						rs, "PK_NAME");			
//				
//				while (walker.hasNext()) {
//					IndexTemporary idxTemp = walker.next();
//					Collection<IndexDef> col = res.get(idxTemp.m_tabName);
//					if(null != col)
//						col.add(idxTemp.toIndexDef());
//				}
//			} finally{
//				ApiAlgs.closeProfItem(psi);
//			}
//			rs.close();
//
//			psi = ApiAlgs.getProfItem("test", "getImportedKeys");
//			try{
//				rs = m_md.getImportedKeys(CATALOG, SCHEME, "%");
//				ResultSetWalker<IndexTemporary> walker = new ResultSetWalker<IndexTemporary>(
//						new IndexObjectFactory(IndexDef.TYPE.FOREIGN_KEY, "FK_NAME", "FKTABLE_NAME", m_dbNames, m_indexLocNames),
//						rs, "FK_NAME");
//				while (walker.hasNext()) {
//					IndexTemporary idxTemp = walker.next();
//					Collection<IndexDef> col = res.get(idxTemp.m_tabName);
//					if(null != col)
//						col.add(idxTemp.toIndexDef());
//				}
//				rs.close();
//			} finally{
//				ApiAlgs.closeProfItem(psi);
//			}
//
//			psi = ApiAlgs.getProfItem("test", "getIndexInfo");
//			try{
//				for(String tab : res.keySet()){
//					String dbName = m_dbNames.getDbName(tab);
//					ApiAlgs.getLog(this).trace(dbName);
//					rs = m_md.getIndexInfo(CATALOG, SCHEME, dbName, false, false);
//					ResultSetWalker<IndexTemporary> walker = new ResultSetWalker<IndexTemporary>(
//							new IndexObjectFactory(IndexDef.TYPE.INDEX, "INDEX_NAME", "TABLE_NAME", m_dbNames, m_indexLocNames),
//							rs, "INDEX_NAME");
//					while (walker.hasNext()) {
//						IndexTemporary idxTemp = walker.next();
//						Collection<IndexDef> col = res.get(idxTemp.m_tabName);
//						if(null != col){
//							IndexDef indexDef = idxTemp.toIndexDef();
//							boolean bIs = false;
//							for (IndexDef indexDef2 : col) {
//								if(indexDef2.getName().equals(indexDef.getName())){
//									bIs = true;
//									break;
//								}
//							}
//							if(!bIs)
//								col.add(indexDef);
//						}
//					}
//					rs.close();
//				}					
//			} finally{
//				ApiAlgs.closeProfItem(psi);
//			}			
//			return res;
//		}
		
		private FieldDef rs2FieldDef(ResultSet rs) throws SQLException {
			String name = rs.getString("COLUMN_NAME");
			ColumnType type = FieldDef.ddlType(rs.getInt("DATA_TYPE"));
			String defVal = rs.getString("COLUMN_DEF");
			boolean bNotNull = rs.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls;
			FieldDef res;
			if (FieldDef.isScalarType(type))
				res = FieldDef.createScalarField(name, type, bNotNull, defVal);
			else if (FieldDef.isStringType(type)) {
				int size = rs.getInt("COLUMN_SIZE");
				int charOctetLength = rs.getInt("CHAR_OCTET_LENGTH");
				if (size * 2 <= charOctetLength) {// Unicode
					if (type.equals(ColumnType.CHAR))
						type = ColumnType.NCHAR;
					else if (type.equals(ColumnType.VARCHAR))
						type = ColumnType.NVARCHAR;
				}
				res = FieldDef.createStringField(name, type, size, bNotNull,
						defVal);
			} else if (FieldDef.isDecimalType(type)) {
				res = FieldDef.createDecimalField(name, rs
						.getInt("COLUMN_SIZE"), rs.getInt("DECIMAL_DIGITS"),
						bNotNull, defVal);
			} else {
				ApiAlgs.assertTrue(false, "");
				return null;
			}

			return res;
		}
	}

	public static class DeltaSchemaLoader {
		
		final String CATALOG = null;

		final String SCHEMA = null;

		private List<String> m_tabs;
//		private Map<String, Set<String>> m_exclusions = new HashMap<String, Set<String>>();

		private IIndexLocNames m_indexLocNames;

		public static class OlapIndexLocNames implements IIndexLocNames{
			
			public String getShortName(String dbTabName, String dbFullName){
				if(dbFullName.startsWith(dbTabName.toUpperCase()+"_")){
					return dbFullName.substring(dbTabName.length()+1);
				}
				return dbFullName;
			}
		}

		public DeltaSchemaLoader(List<String> tabs, IIndexLocNames indexLocNames) {
			m_tabs = tabs;
			m_indexLocNames = indexLocNames;
		}

		public DeltaSchema loadSchema(Connection con, IDBNames dbNames) {
			DeltaSchema res = new DeltaSchema();

			DatabaseMetaData md;
			try {
				md = con.getMetaData();
				DBMetadata md2 = new DBMetadata(md, new HashSet<String>(m_tabs), dbNames, m_indexLocNames);
				if(DbType.MSSQL.equals(UpgradeRunner.getDbType(con))){
					md2.SCHEME = "dbo";
				}
				Map<String, Collection<FieldDef>> fields;
				PSI psi = ApiAlgs.getProfItem("test", "getFields");
				try{
					fields = md2.getFields();
				} finally{
					ApiAlgs.closeProfItem(psi);
				}
				IndexOperationObjectFactory indexFactory = new IndexOperationObjectFactory();
				for(Map.Entry<String, Collection<FieldDef>> entry: fields.entrySet()){
					TableDef tabDef = new TableDef(entry.getKey());
					tabDef.setSupportForeignKeys(false);
					for(FieldDef fd: entry.getValue()){
						tabDef.addField(tabDef.getVersion()+1, fd);						
					}
					
					for(IndexDef indexDef: getIndices(md, dbNames.getDbName(entry.getKey()), dbNames)){
						tabDef.addModification(tabDef.getVersion()+1, indexFactory.addCmd(indexDef));
					}
					res.addTable(tabDef);
				}

			} catch (SQLException e) {
				ApiAlgs.rethrowException(e);
			}
			return res;
		}



		static class IndexTemporary {
			public String m_parentKey;

			public String m_parentTab;

			String m_name;

			List<String> m_cols;

			IndexDef.TYPE m_type;

			boolean m_bAsc, m_bUnique;

			String m_tabName;

			public IndexTemporary(String name, TYPE type) {
				m_name = name;
				m_type = type;
				m_cols = new ArrayList<String>();
			}

			IndexDef toIndexDef() {
				IndexDef res;
				if (IndexDef.TYPE.PRIMARY_KEY.equals(m_type))
					res = IndexDef.primaryKey(m_name, m_cols);
				else if (IndexDef.TYPE.FOREIGN_KEY.equals(m_type))
					res = IndexDef.foreignKey(m_name, m_cols, m_parentTab,
							m_parentKey);
				else
					res = IndexDef.createIndex(m_name, m_cols, m_bUnique,
							m_bAsc);
				return res;
			}
		}
		
		static class IndexObjectFactory implements IObjectFactory<IndexTemporary> {

			private TYPE m_type;
		
			private String m_fname;
		
			private String m_fColumn;
		
			private String m_tabName;
		
			private String m_fTabName;
		
			private IDBNames m_dbNames;
			private IIndexLocNames m_indexLocNames;
		
			public IndexObjectFactory(IndexDef.TYPE type, String fname, String tabName, 
					String fTabName, IDBNames dbNames, IIndexLocNames indexLocNames) {
				m_type = type;
				m_fname = fname;
				m_fColumn = type.equals(IndexDef.TYPE.FOREIGN_KEY) ? "FKCOLUMN_NAME"
						: "COLUMN_NAME";
				m_tabName = tabName;
				m_fTabName = fTabName;
				m_dbNames = dbNames;
				m_indexLocNames = indexLocNames;
			}
		
			public IndexTemporary createObject(ResultSet rs) {
				try {
					if(!m_tabName.equals(rs.getString(m_fTabName)))
						return null;
//					String dbTabName = rs.getString(m_fTabName);
//					m_tabName = m_dbNames.getAppName(dbTabName);
					if (IndexDef.TYPE.INDEX.equals(m_type)) {
						int type = rs.getInt("TYPE");
						if(type == DatabaseMetaData.tableIndexStatistic)
							return null;
					} 
					
					String indexName = m_indexLocNames.getShortName(m_tabName, 
							rs.getString(m_fname).toUpperCase());
					IndexTemporary res = new IndexTemporary(indexName, m_type);
					if (IndexDef.TYPE.INDEX.equals(m_type)) {
						res.m_bAsc = !"B".equals(rs.getString("ASC_OR_DESC"));
						res.m_bUnique = !rs.getBoolean("NON_UNIQUE");
					} else if (IndexDef.TYPE.FOREIGN_KEY.equals(m_type)) {
						String pkTabName = rs.getString("PKTABLE_NAME");
						res.m_parentTab = m_dbNames.getAppName(pkTabName);
						ApiAlgs.assertNotNull(res.m_parentTab, "tab:"+m_tabName+"; parent: "+ rs.getString("PKTABLE_NAME"));
						res.m_parentKey = m_indexLocNames.getShortName(pkTabName, rs.getString("PK_NAME"));
					}
					return res;
				} catch (SQLException e) {
					ApiAlgs.rethrowException(e);
				}
				return null;
			}
		
			public void addRow(IndexTemporary obj, ResultSet rs) {
				try {
					obj.m_cols.add(rs.getString(m_fColumn));
				} catch (SQLException e) {
					ApiAlgs.rethrowException(e);
				}
			}
		}


		private List<IndexDef> getIndices(DatabaseMetaData md, String dbTabName, IDBNames dbNames)
				throws SQLException {
			ArrayList<IndexDef> res = new ArrayList<IndexDef>();
			Set<String> added = new HashSet<String>();
			
			ResultSet rs;
			PSI psi = ApiAlgs.getProfItem("test", "getPrimaryKeys");
			try{
				rs = md.getPrimaryKeys(CATALOG, SCHEMA, dbTabName);
			} finally{
				ApiAlgs.closeProfItem(psi);
			}
			ResultSetWalker<IndexTemporary> walker = new ResultSetWalker<IndexTemporary>(
					new IndexObjectFactory(IndexDef.TYPE.PRIMARY_KEY, "PK_NAME", dbTabName, "TABLE_NAME", dbNames, m_indexLocNames),
					rs, "PK_NAME");			
			
			while (walker.hasNext()) {
				IndexDef index = walker.next().toIndexDef();
				res.add(index);
				added.add(index.getName());
			}

			rs.close();

			psi = ApiAlgs.getProfItem("test", "getImportedKeys");
			try{
				rs = md.getImportedKeys(CATALOG, SCHEMA, dbTabName);
			} finally{
				ApiAlgs.closeProfItem(psi);
			}
			walker = new ResultSetWalker<IndexTemporary>(
					new IndexObjectFactory(IndexDef.TYPE.FOREIGN_KEY, "FK_NAME", dbTabName, "FKTABLE_NAME", dbNames, m_indexLocNames),
					rs, "FK_NAME");
			while (walker.hasNext()) {
				IndexDef index = walker.next().toIndexDef();
				res.add(index);
				added.add(index.getName());
			}
			rs.close();

			psi = ApiAlgs.getProfItem("test", "getIndexInfo");
			try{
				rs = md.getIndexInfo(CATALOG, SCHEMA, dbTabName, false, false);
			} finally{
				ApiAlgs.closeProfItem(psi);
			}
			walker = new ResultSetWalker<IndexTemporary>(
					new IndexObjectFactory(IndexDef.TYPE.INDEX, "INDEX_NAME", dbTabName, "TABLE_NAME", dbNames, m_indexLocNames),
					rs, "INDEX_NAME");
			while (walker.hasNext()) {
				IndexDef index = walker.next().toIndexDef();
				if (!added.contains(index.getName())){
					res.add(index);
					added.add(index.getName());
				}
			}
			rs.close();

			return res;
		}
//
//		private FieldDef rs2FieldDef(ResultSet rs) throws SQLException {
//			String name = rs.getString("COLUMN_NAME");
//			ColumnType type = FieldDef.ddlType(rs.getInt("DATA_TYPE"));
//			String defVal = rs.getString("COLUMN_DEF");
//			boolean bNotNull = rs.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls;
//			FieldDef res;
//			if (FieldDef.isScalarType(type))
//				res = FieldDef.createScalarField(name, type, bNotNull, defVal);
//			else if (FieldDef.isStringType(type)) {
//				int size = rs.getInt("COLUMN_SIZE");
//				int charOctetLength = rs.getInt("CHAR_OCTET_LENGTH");
//				if (size * 2 <= charOctetLength) {// Unicode
//					if (type.equals(ColumnType.CHAR))
//						type = ColumnType.NCHAR;
//					else if (type.equals(ColumnType.VARCHAR))
//						type = ColumnType.NVARCHAR;
//				}
//				res = FieldDef.createStringField(name, type, size, bNotNull,
//						defVal);
//			} else if (FieldDef.isDecimalType(type)) {
//				res = FieldDef.createDecimalField(name, rs
//						.getInt("COLUMN_SIZE"), rs.getInt("DECIMAL_DIGITS"),
//						bNotNull, defVal);
//			} else {
//				ApiAlgs.assertTrue(false, "");
//				return null;
//			}
//
//			return res;
//		}

//		public void addExclusion(String tabName, String ... fieldNames) {
//			m_exclusions.put(tabName, new HashSet<String>(Arrays.asList(fieldNames)));
//		}

	}

	interface ITabCommand {

		List<DBOperation> toDBOperationList();
	}

	static class AddTabCmd implements ITabCommand {

		private TableDef m_deltaTab;

		public AddTabCmd(TableDef deltaTab) {
			m_deltaTab = deltaTab;
		}

		public List<DBOperation> toDBOperationList() {
			ArrayList<TableUpdateOperation> ext = new ArrayList<TableUpdateOperation>();
			ArrayList<TableUpdateOperation> batchOps = new ArrayList<TableUpdateOperation>();
			for (Iterator<TableUpdateOperation> iter = m_deltaTab.getHistory(1).iterator(); iter.hasNext();) {
				TableUpdateOperation op =  iter.next();
				if(op instanceof AddIndexOperation && (!(op instanceof AddPrimaryKeyOperation))){
					ext.add(op);
				}
				else{
					batchOps.add(op);
				}
			}
			ArrayList<DBOperation> res = new ArrayList<DBOperation>();
			res.add(new DBOperation(m_deltaTab.getEntityName(),
					new CreateTableOperation(batchOps)));
			for (TableUpdateOperation op : ext) {
				res.add(new DBOperation(m_deltaTab.getEntityName(),op));
			}
			return res;
		}

	}

	static class DropTabCmd implements ITabCommand {
		private TableDef m_tab;

		public DropTabCmd(TableDef tab) {
			m_tab = tab;
		}

		public List<DBOperation> toDBOperationList() {
			ArrayList<DBOperation> res = new ArrayList<DBOperation>();
			AddTabCmd forward = new AddTabCmd(m_tab);
			List<DBOperation> forwardOps = forward.toDBOperationList();
			for (int i = forwardOps.size() - 1; i > 0; i--)
				res.add(new DBOperation(m_tab.getEntityName(), forwardOps.get(i)
						.getOperation().getReverseOperation()));

			CreateTableOperation createOp = (CreateTableOperation) forward
					.toDBOperationList().get(0).getOperation();
			List<TableUpdateOperation> forwardElements = createOp.getElements();
			ListIterator<TableUpdateOperation> li = forwardElements
					.listIterator(forwardElements.size());
			while (li.hasPrevious())
				res.add(new DBOperation(m_tab.getEntityName(), li.previous()
						.getReverseOperation()));
			return res;
		}

	}

	static class EditTabCmd implements ITabCommand {

		private TableDef m_src;

		private TableDef m_dst;

		public EditTabCmd(TableDef src, TableDef dst) {
			m_src = src;
			m_dst = dst;
		}

		public List<DBOperation> toDBOperationList() {

			ArrayList<DBOperation> res = new ArrayList<DBOperation>();
			{
				DiffLeader<TableUpdateOperation, FieldDef> dl = new DiffLeader<TableUpdateOperation, FieldDef>(
						new ColumnOperationObjectFactory());

				for (TableUpdateOperation operation : dl.getCommandSeq(
						toMap(m_src.getFields()), toMap(m_dst.getFields()))) {
					if(null == operation)
						throw new RuntimeException(m_dst.getEntityName());
					res.add(new DBOperation(m_dst.getEntityName(), operation));
				}
			}
			{
				DiffLeader<TableUpdateOperation, IndexDef> dl = new DiffLeader<TableUpdateOperation, IndexDef>(
						new IndexOperationObjectFactory());
				
				IKey<IndexDef> indexKey = new IKey<IndexDef>(){

					public String get(IndexDef value) {
						if(m_src.isSupportForeignKeys()){
							String key = value.getType() + ": " +value.getColumns().toString().toLowerCase();
							if(value.getType().equals(IndexDef.TYPE.FOREIGN_KEY))
								key += String .format(": %s", value.getParentTable().toLowerCase());
							return key;
						}
						else{
							String key;
							if(value.getType().equals(IndexDef.TYPE.PRIMARY_KEY))
								key = value.getType().toString();
							else
								key = "";
							key += ": " + value.getColumns().toString().toLowerCase();
							return key;
						}
						
					}
					
				};

				for (TableUpdateOperation operation : dl.getCommandSeq(
						toMap(m_src.getIndices(), indexKey), toMap(m_dst.getIndices(), indexKey))) {
					if(null == operation)
						throw new RuntimeException(m_dst.getEntityName()+":"+indexKey);
					res.add(new DBOperation(m_dst.getEntityName(), operation));
				}

			}

			return res;
		}
		
		interface IKey<T>{
			String get(T value);
		} 
		
		public static <T extends IElementDef> Map<String, T> toMap(final TableElements<T> elements){
			return toMap(elements, new IKey<T>(){
				public String get(T value) {
					return value.getName();
				}
			});
		}
		
		public static <T extends IElementDef> Map<String, T> toMap(final TableElements<T> elements, final IKey<T> key){
			return new AbstractMap<String, T>(){
				@Override
				public Set<java.util.Map.Entry<String, T>> entrySet() {
					return new AbstractSet<java.util.Map.Entry<String, T>>(){

						@Override
						public Iterator<java.util.Map.Entry<String, T>> iterator() {
							return new Iterator<java.util.Map.Entry<String, T>>(){
								
								int idx = 0;

								public boolean hasNext() {
									return idx < elements.size();
								}

								public java.util.Map.Entry<String, T> next() {
									return new java.util.Map.Entry<String, T>(){
										T e = elements.getElement(idx++);
										public String getKey() {
											return key.get(e);
										}

										public T getValue() {
											return e;
										}

										public T setValue(T arg0) {
											throw new RuntimeException("unsupported");
										}
										
									};
								}

								public void remove() {}
								
							};
						}

						@Override
						public int size() {
							return elements.size();
						}
					};
				}
				
			};
		}

	}

	public static class ColumnOperationObjectFactory implements
			ICmdFactory<TableUpdateOperation, FieldDef> {

		public AddColumnOperation addCmd(FieldDef element) {
			return new AddColumnOperation(element);
		}

		public DeleteColumnOperation dropCmd(FieldDef element) {
			return new DeleteColumnOperation(element);
		}

		public TableUpdateOperation editCmd(FieldDef srcElement,
				FieldDef dstElement) {
			return null;
		}
		
		static class ETypesNotEquals extends ICmdFactory.EUnsuccessOperation{
			private static final long serialVersionUID = 7721250271887302895L;

			public ETypesNotEquals(String colName, ColumnType srcType, ColumnType dstType) {
				super(String.format("colName: %s, srcType: %s, dstType: %s", colName, srcType, dstType));
			}
		}

		public com.triniforce.db.ddl.DiffLeader.ICmdFactory.Action getEqKeyAction(
				FieldDef srcElement, FieldDef dstElement) {
			if(!srcElement.getType().equals(dstElement.getType()))
				throw new ETypesNotEquals(srcElement.getName(), srcElement.getType(), dstElement.getType());
			return ICmdFactory.Action.NONE;
		}

	}

	public static class IndexOperationObjectFactory implements
			ICmdFactory<TableUpdateOperation, IndexDef> {

		public AddIndexOperation addCmd(IndexDef element) {
			AddIndexOperation res;
			if (element.getType().equals(IndexDef.TYPE.PRIMARY_KEY))
				res = new AddPrimaryKeyOperation(element.getName(), element
						.getColumns());
			else if (element.getType().equals(IndexDef.TYPE.INDEX))
				res = new AddIndexOperation(element);
			else if (element.getType().equals(IndexDef.TYPE.FOREIGN_KEY))
				res = new AddForeignKeyOperation(element.getName(), element
						.getColumns(), element.getParentTable(), element
						.getParentIndex());
			else {
				ApiAlgs.assertTrue(false, element.getType().toString());
				res = null;
			}
			

			return res;
		}

		public DeleteIndexOperation dropCmd(IndexDef element) {
			return new DeleteIndexOperation(element.m_name, element.getType(), element.m_bUnique);
		}

		public TableUpdateOperation editCmd(IndexDef srcElement,
				IndexDef dstElement) {
			return null;
		}

		public com.triniforce.db.ddl.DiffLeader.ICmdFactory.Action getEqKeyAction(
				IndexDef srcElement, IndexDef dstElement) {
			return ICmdFactory.Action.NONE;
		}

	}

	static class DeltaTableCmdFactory implements
			ICmdFactory<ITabCommand, TableDef> {

		public AddTabCmd addCmd(TableDef element) {
			return new AddTabCmd(element);
		}

		public DropTabCmd dropCmd(TableDef element) {
			return new DropTabCmd(element);
		}

		public EditTabCmd editCmd(TableDef srcElement, TableDef dstElement) {
			return new EditTabCmd(srcElement, dstElement);
		}

		public com.triniforce.db.ddl.DiffLeader.ICmdFactory.Action getEqKeyAction(
				TableDef srcElement, TableDef dstElement) {
			return ICmdFactory.Action.EDIT;
		}

	}

	public List<DBOperation> calculateDelta(Map<String, TableDef> src, Map<String, TableDef> dst) {
		ArrayList<DBOperation> res = new ArrayList<DBOperation>();

		DiffLeader<ITabCommand, TableDef> differ = new DiffLeader<ITabCommand, TableDef>(
				new DeltaTableCmdFactory());
		
		List<ITabCommand> tabCommands = differ.getCommandSeq(src, dst);
		
		for (ITabCommand command : tabCommands) {
			res.addAll(command.toDBOperationList());
		}
		
		sortExecSequence(res);
		
		setForeignKeysColumns(res, dst);

		return res;
	}

	private void setForeignKeysColumns(ArrayList<DBOperation> res, Map<String, TableDef> tables) {
		for (DBOperation dbOp : res) {
			if(dbOp.getOperation() instanceof AddForeignKeyOperation){
				AddForeignKeyOperation addFk = (AddForeignKeyOperation) dbOp.getOperation();
				addFk.setCreateFK(false);
			}
		}
	}

	private void sortExecSequence(ArrayList<DBOperation> res) {
		// last operations must be add foreign keys
		final Map<Class, Integer> orderNo = new HashMap<Class, Integer>();
		orderNo.put(DeleteIndexOperation.class, -2);
		orderNo.put(DeleteColumnOperation.class, -1);
		orderNo.put(CreateTableOperation.class, 0);
		orderNo.put(AddColumnOperation.class, 1);
		orderNo.put(AddIndexOperation.class, 2);
		orderNo.put(AddPrimaryKeyOperation.class, 3);
		orderNo.put(AddForeignKeyOperation.class, 4);
		

		Collections.sort(res, new Comparator<DBOperation>(){

			public int compare(DBOperation arg0, DBOperation arg1) {
				return getOpOrder(arg0) - getOpOrder(arg1);
			}
			
			int getOpOrder(DBOperation dbOp){
				Integer v = orderNo.get(dbOp.getOperation().getClass());
				ApiAlgs.assertNotNull(v, dbOp.getOperation().getClass().getName());
				return v;
			}
		});
	}
	
	public void applyCommands(Map<String, TableDef> src, List<DBOperation> commands){
		for (DBOperation op : commands) {
			TableDef def;
			if(op.getOperation() instanceof CreateTableOperation){
				CreateTableOperation createOp = (CreateTableOperation) op.getOperation();
				def = new TableDef(op.getDBOName());
				for(int i=0; i<createOp.getElements().size(); i++){
					def.addModification(i+1, createOp.getElements().get(i));
				}
				src.put(op.getDBOName(), def);
			}
			else{
				def = src.get(op.getDBOName());
				def.addModification(def.getVersion()+1, (TableUpdateOperation) op.getOperation());
			}
		}
	}

}
