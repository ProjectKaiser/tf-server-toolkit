package eu.untill.rs.rscore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.utils.ApiAlgs.SimpleName;
import com.triniforce.utils.Base64;
import com.triniforce.utils.IName;

import eu.untill.rs.rscore.srvapi.IMemTable.EFieldNotFound;
import eu.untill.rs.rscore.srvapi.IMemTable.IRow;
import eu.untill.rs.rscore.srvapi.IMemTable.ModType;

public class MemTableTest extends BasicServerTestCase {
	
	static class TestRow implements IRow{
		
		ModType m_modType = ModType.INSERTED;
				
		Map<String, Object> m_vals = new HashMap<String, Object>();
		
			
		public TestRow(long id, Object ... namesAndValues) {
			setField(new SimpleName("id"), id);
			setField(new SimpleName("ID"), id);
			for (int i = 0; i < namesAndValues.length/2; i++) {
				setField(new SimpleName((String) namesAndValues[i*2]), namesAndValues[i*2+1]);
			}
			setField("ID_DB_TASKS", 0);
			setField("SYS_MODIFIED",0);
		}

		public void setModType(ModType modType) {
			m_modType = modType;
		}
		
		public ModType getModType() {
			return m_modType;
		}

		public Object getField(IName fname) {
			return m_vals.get(fname.getName());
		}

		public void setField(IName fname, Object value) {
			m_vals.put(fname.getName(), value);
		}

		public IName[] getColumns() {
			IName[] res = new IName[m_vals.size()];
			int i=0;
			for (String key : m_vals.keySet()) {
				res[i++] = new SimpleName(key);
			}
			return res;
		}

		public Long getId(){
			return (Long)m_vals.get("id");
		}

		public Long getId(IName fname) {
			return (Long)m_vals.get(fname);
		}

		public Object getField(String fname) throws EFieldNotFound {
			return m_vals.get(fname);
		}

		public void setField(String fname, Object value) {
			m_vals.put(fname, value);
			
		}

		public Long getId(String fname) {
			return (Long) m_vals.get(fname);
		}

		public boolean isExist(String fname) {
			return m_vals.containsKey(fname);
		}

		public void dropField(String fname) {
			m_vals.remove(fname);
			
		}

		public Map<String, Object> getValues() {
			return m_vals;
		}

		public void setValues(Map<String, Object> v) {
		}
		


	} 

	public void testAddRow() {
		MemTable tab = new MemTable();
		try{
			tab.addRow(null);
			fail();
		} catch(NullPointerException e){
			assertEquals("row", e.getMessage());
		}
		
		TestRow row = new TestRow(124L);
		tab.addRow(row);
	}

	public void testGetById() {
		
		MemTable tab = new MemTable();
				
		tab.addRow(new TestRow(124L , "f1", "string 1"));
		tab.addRow(new TestRow(125L , "f1", "string 2"));
		tab.addRow(new TestRow(127L , "f1", "string 3"));
		tab.addRow(new TestRow(128L , "f1", "string 4"));
		
		IRow row = tab.getById(127L);
		assertNotNull(row);
		assertEquals("string 3", row.getField(new SimpleName("f1")));
		
		Collection<IRow> rows = tab.getRows();
		assertNotNull(rows);
		assertEquals(4, rows.size());
	}
	
	void tstIRow(IRow row){
		//1
		assertEquals(row.getModType(),ModType.INSERTED);
		row.setModType(ModType.UPDATED);
		assertEquals(row.getModType(),ModType.UPDATED);
		
		//2
		try{
			row.getField("str");
			fail();
		} catch(EFieldNotFound e){
			assertEquals("str", e.getMessage());
		}
		//3
		try{
			row.getField(new SimpleName("str"));
			fail();
		} catch(EFieldNotFound e){
			assertEquals("str", e.getMessage());
		}
		
		//4
		row.setField(new SimpleName(IIdDef.Helper.getFieldDef().getName()), 5L);
		row.setField(new SimpleName("id_sales"), null);
		row.setField("str", "string");
		
		assertEquals(row.getField(new SimpleName(IIdDef.Helper.getFieldDef().getName())),5L);
		assertEquals(row.getField(IIdDef.Helper.getFieldDef().getName()),5L);
		assertEquals(row.getField(new SimpleName("id_sales")),null);	
		assertEquals(row.getField("id_sales"),null);
		assertEquals(row.getField(new SimpleName("str")),"string");
		assertEquals(row.getField("str"),"string");
			
		assertEquals(row.getId().longValue(),5L);
		assertEquals(row.getId(new SimpleName("id_sales")),null);
		assertEquals(row.getId("id_sales"),null);
		//5
		row.setField(IIdDef.Helper.getFieldDef().getName(), 6L);
		row.setField("id_sales", 20L);
		row.setField(new SimpleName("str"), "char");
		
		assertEquals(row.getField(new SimpleName(IIdDef.Helper.getFieldDef().getName())),6L);
		assertEquals(row.getField(IIdDef.Helper.getFieldDef().getName()),6L);
		assertEquals(row.getField(new SimpleName("id_sales")),20L);	
		assertEquals(row.getField("id_sales"),20L);
		assertEquals(row.getField(new SimpleName("str")),"char");
		assertEquals(row.getField("str"),"char");
			
		assertEquals(row.getId().longValue(),6L);
		assertEquals(row.getId(new SimpleName("id_sales")).longValue(),20L);
		assertEquals(row.getId("id_sales").longValue(),20L);
		//6
		assertEquals(row.isExist("id_sales"), true);
		assertEquals(row.isExist("order_item"), false);
		//7
		row.dropField("id_sales");
		assertEquals(row.isExist("id_sales"), false);
		assertEquals(row.isExist("str"),true);
	}
	
	public void testEmptyRow(){
		MemTable tab = new MemTable();
		IRow row = tab.emptyRow();
		assertNotNull(row);
		
		tstIRow(row);
	}
	
	public void testCloneRow(){
		
		MemTable tab = new MemTable();
		
		IRow row = tab.emptyRow();
		row.setField(new SimpleName(IIdDef.Helper.getFieldDef().getName()), 124L);
		row.setField(new SimpleName("f1"), "string 1");
		tab.addRow(row);
		
		row = tab.emptyRow();
		row.setField(new SimpleName(IIdDef.Helper.getFieldDef().getName()), 125L);
		row.setField(new SimpleName("f1"), "string 2");
		tab.addRow(row);
		
		row = tab.emptyRow();
		row.setField(new SimpleName(IIdDef.Helper.getFieldDef().getName()), 127L);
		row.setField(new SimpleName("f1"), "string 3");
		row.setModType(ModType.UPDATED);
		tab.addRow(row);
		
		row = tab.emptyRow();
		row.setField(new SimpleName(IIdDef.Helper.getFieldDef().getName()), 128L);
		row.setField(new SimpleName("f1"), "string 4");
		tab.addRow(row);
		//-------------		
		row = tab.getById(127L);
		IRow row1 = tab.cloneRow(row);		
		assertNotNull(row1);
		assertNotSame(row1, row);
		assertEquals(ModType.UPDATED, row1.getModType());
		assertEquals(127L, row1.getField(new SimpleName(IIdDef.Helper.getFieldDef().getName())));
		assertEquals("string 3", row1.getField(new SimpleName("f1")));
				
		row = tab.getById(128L);
		row1 = tab.cloneRow(row);		
		assertNotNull(row1);
		assertNotSame(row1, row);
		assertEquals(ModType.INSERTED, row1.getModType());
		assertEquals(128L, row1.getField(new SimpleName(IIdDef.Helper.getFieldDef().getName())));
		assertEquals("string 4", row1.getField(new SimpleName("f1")));
					
	}	
	
	public void testGetSortedRows(){
		MemTable tab = new MemTable();
		tab.addRow(new TestRow(124L , "f1", "string 2", "f2", 94));
		tab.addRow(new TestRow(125L , "f1", "string 3", "f2", 102));
		tab.addRow(new TestRow(127L , "f1", "string 1", "f2", 51));
		tab.addRow(new TestRow(128L , "f1", "string 4", "f2", 62));
		tab.addRow(new TestRow(129L , "f1", null,       "f2", 62));
		
		{
			Collection<IRow> rows = tab.getSortedRows(new IName[]{new SimpleName("id")});
			assertNotNull(rows);
			assertEquals(5, rows.size());
			assertEquals(Arrays.asList(124L,125L,127L,128L,129L), getIds(rows));
		}
		{
			Collection<IRow> rows = tab.getSortedRows(new IName[]{});
			assertEquals(getIds(tab.getRows()), getIds(rows));
		}
		{
			Collection<IRow> rows = tab.getSortedRows(new IName[]{new SimpleName("f1")});
			assertEquals(Arrays.asList(129L,127L,124L,125L,128L), getIds(rows));
		}
		{
			tab.addRow(new TestRow(130L , "f1", "string 5","f2", 62));
			Collection<IRow> rows = tab.getSortedRows(new IName[]{new SimpleName("f2"), new SimpleName("f1")});
			assertEquals(Arrays.asList(127L,129L,128L,130L,124L,125L), getIds(rows));
		}
	}

	private Collection<Long> getIds(Collection<IRow> rows) {
		ArrayList<Long> res = new ArrayList<Long>(rows.size());
		Iterator<IRow> ir = rows.iterator();
		for(int i=0; i<rows.size(); i++){
			res.add( ir.next().getId());
		}
		return res;
	}
	
	public void testMapRows() {
		MemTable table1 = new MemTable();
		MemTable table2 = new MemTable();
		//1
		table2.setMapRows(null);
		assertNull(table2.getMapRows());
		//2
		assertNotNull(table1.getMapRows());
		table2.setMapRows(table1.getMapRows());
		assertSame(table1.getMapRows(), table2.getMapRows());
		//3
		table2.setMapRows(null);
		
		IRow row1 = table1.emptyRow();
		row1.setField(IIdDef.Helper.getFieldDef().getName(), 1L);
		row1.setField("f2", "1L");
		row1.setField("f3", 10L);
		table1.addRow(row1);
		
		IRow row2 = table1.emptyRow();
		row2.setField(IIdDef.Helper.getFieldDef().getName(), 2L);
		row2.setField("f2", "2L");
		row2.setField("f3", 20L);
		table1.addRow(row2);
		
		IRow row3 = table1.emptyRow();
		row3.setField(IIdDef.Helper.getFieldDef().getName(), 3L);
		row3.setField("f2", "3L");
		row3.setField("f3", 30L);
		row3.setModType(ModType.UPDATED);
		table1.addRow(row3);
		
		Map<Long, IRow> mapRows = table1.getMapRows();
		assertNotNull(mapRows);
		assertEquals(mapRows.size(), 3);
		assertSame(mapRows.get(1L), row1);
		assertSame(mapRows.get(2L), row2);
		assertEquals(mapRows.get(2L).getModType(), ModType.INSERTED);
		assertSame(mapRows.get(3L), row3);
		assertEquals(mapRows.get(3L).getModType(), ModType.UPDATED);
		
		
		
	}
	
	public void testSave() throws XPathExpressionException, UnsupportedEncodingException, IOException, SAXException, ParserConfigurationException, TransformerException{
		MemTable table1 = new MemTable();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IRow r = table1.emptyRow();
		r.setField("ID", 1);
		r.setField("F01", 1412);
		r.setField("F02_NVARCHAR", "Some string content");
		r.setField("FUNK02", 1412);
		table1.addRow(r);
		
		r = table1.emptyRow();
		r.setField("ID", 2);
		r.setField("F01", null);
		byte[] barr = new byte[]{5,64,17,(byte) 201};
		r.setField("F_BLOB", barr);
		GregorianCalendar gc = new GregorianCalendar(2001, 8, 21, 15, 22, 51);
		r.setField("F_DT", new Timestamp(gc.getTimeInMillis()));
		table1.addRow(r);
		
		TableDef td = new TableDef();
		td.setDbName("Tab_0014");
		td.addScalarField(1, "F01", ColumnType.INT, false, null);
		td.addStringField(2, "F02_NVARCHAR", ColumnType.NVARCHAR, 50, false, null);
		td.addScalarField(3, "ID", ColumnType.INT, false, null);
		td.addScalarField(4, "F_BLOB", ColumnType.BLOB, false, null);
		td.addScalarField(5, "F_DT", ColumnType.DATETIME, false, null);
		
		table1.save(td, out);
		trace(out.toString());
		
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(out.toByteArray()));
		
		XPath xp = XPathFactory.newInstance().newXPath();
//		InputSource source = new InputSource(new ByteArrayInputStream(out.toByteArray())); 
		assertTrue((Boolean)xp.evaluate("/DATAPACKET", doc, XPathConstants.BOOLEAN));
		
		assertTrue((Boolean)xp.evaluate("/DATAPACKET", doc, XPathConstants.BOOLEAN));
		assertTrue((Boolean)xp.evaluate("/DATAPACKET/METADATA/FIELDS", doc, XPathConstants.BOOLEAN));
		
		assertTrue((Boolean)xp.evaluate(".//FIELD[@attrname='F01']", doc, XPathConstants.BOOLEAN));
		assertTrue((Boolean)xp.evaluate(".//FIELD[@fieldtype='i4']", doc, XPathConstants.BOOLEAN));
		Element pn = (Element) xp.evaluate(".//FIELD[@attrname='F01']/PARAM", doc, XPathConstants.NODE);
		assertEquals("\"Tab_0014\".\"F01\"", pn.getAttribute("Value"));
		
		Element ef = (Element) xp.evaluate(".//FIELD[@attrname='F02_NVARCHAR']", doc, XPathConstants.NODE);
		assertEquals("100", ef.getAttribute("WIDTH"));
		
		assertTrue((Boolean)xp.evaluate(".//ROW", doc, XPathConstants.BOOLEAN));		
		Element e = (Element) xp.evaluate(".//ROW[@F01]", doc, XPathConstants.NODE);		
		assertFalse(e.hasAttribute("FUNK02"));
		
		e = (Element) xp.evaluate(".//ROW[@ID=\"2\"]", doc, XPathConstants.NODE);
		assertNotNull(e);
		assertFalse(e.hasAttribute("F01"));
		assertFalse(e.hasAttribute("F02_NVARCHAR"));
		assertEquals("20010921T15:22:51000", e.getAttribute("F_DT"));
		
		ef = (Element) xp.evaluate(".//FIELD[@attrname='F_BLOB']", doc, XPathConstants.NODE);
		assertEquals("Binary", ef.getAttribute("SUBTYPE"));
		assertEquals("8", ef.getAttribute("WIDTH"));
		
		
		{
			MemTable mt2 = new MemTable();
			TableDef td2 = new TableDef("test");
			td2.addField(1, FieldDef.createScalarField("F01", ColumnType.INT, false));
			td2.addField(2, FieldDef.createScalarField("ID", ColumnType.INT, false));
			td2.addField(3, FieldDef.createScalarField("F_BLOB", ColumnType.BLOB, false));
			mt2.load(td2, new ByteArrayInputStream(out.toByteArray()));
			assertTrue(mt2.getRows().size() > 0);
			IRow r1 = mt2.getById(2);
			String strvb = (String) r1.getField("F_BLOB");
			trace("BLOB in b64: " + strvb);
			byte[] vb = Base64.decode(strvb);
			assertTrue(Arrays.equals(barr, vb));
		}
	}

}
