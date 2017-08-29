package eu.untill.rs.rscore;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.microsoft.windowsazure.core.utils.Base64;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.server.srvapi.IIdDef;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.utils.IName;

import eu.untill.rs.rscore.srvapi.IMemTable;

public class MemTable implements IMemTable {
	
    protected static class Row implements IRow, Serializable{
		private static final long serialVersionUID = -23385295430284458L;

		HashMap<String, Object> m_values = new HashMap<String, Object>();
		
		ModType m_modType = ModType.INSERTED;
		
		public Object getField(String fname) throws EFieldNotFound {
			if (!m_values.containsKey(fname)) {
				ApiAlgs.getLog(this).trace(m_values.keySet().toString());
				throw new EFieldNotFound(fname);
			}
			Object res = m_values.get(fname);
			return res;
		}

		public Object getField(IName fname) throws EFieldNotFound {
			return getField(fname.getName());
		}
		
		public void setField(String fname, Object value) {
			m_values.put(fname, value);
		}		
		
		public void setField(IName fname, Object value) {
			setField(fname.getName(), value);
		}
		
		public Long getId() {
			String idName = IIdDef.Helper.getFieldDef().getName();
			return ((Number) getField(idName)).longValue();//$NON-NLS-1$
		}
		
		public Long getId(String fname) {
//			return ((Number) getField(fname)).longValue();
			return (Long) getField(fname);
		}
		
		public Long getId(IName fname) {
			return (Long) getField(fname.getName());
		}
		
				
		public ModType getModType() {
			return m_modType;
		}

		public void setModType(ModType modType) {
			m_modType = modType;
		}
	
		public boolean isExist(String fname) {
			return m_values.containsKey(fname); 
		}

		public void dropField(String fname) {
			m_values.remove(fname);
			
		}

		public Map<String, Object> getValues() {
			return m_values;
		}

		public void setValues(Map<String, Object> v) {
			m_values.clear();
			m_values.putAll(v);
		}
		
	}
	
	private Map<Long, IRow> m_rows;
	private SimpleDateFormat m_dateFormat;

	public MemTable() {
		m_rows = new HashMap<Long, IRow>();
		m_dateFormat = new SimpleDateFormat("yyyyMMdd\'T\'HH:mm:ssSSS");
	}

	public void addRow(IRow row) {
		if(null == row)
			throw new NullPointerException("row");
		
		m_rows.put(row.getId(), row);
	}

	public IRow getById(long id) {
		return m_rows.get(id);
	}

	public Collection<IRow> getRows() {
		return m_rows.values();
	}
	
	public IRow emptyRow() {
		IRow res = new Row();
		return res;
	}
	

	@SuppressWarnings("unchecked")
	public IRow cloneRow(IRow row) {
		
		Row res=new Row();
		Row row1 = (Row) row;
		res.m_values = (HashMap<String, Object>)  row1.m_values.clone();
		res.setModType(row1.getModType());
						
		return res;
	}
	
	static class RowComparator implements Comparator<Object[]>{

		public int compare(Object[] arg0, Object[] arg1) {
			int res = 0;
            for (int i=0; i< arg0.length; i++) {
                res = compareValues(arg0[i], arg1[i]);
                if(res != 0)
                    break;
            }
            return res;
		}
		
        @SuppressWarnings({ "unchecked", "rawtypes" }) //$NON-NLS-1$
        private int compareValues(Object obj0, Object obj1) {
            if(null == obj0){
                if(null == obj1)
                    return 0;
                else 
                    return -1; 
            }
            if(null == obj1){
                return 1;
            }
            if(!(obj0 instanceof Comparable)){
                return 0;
            }
            Comparable v1 = (Comparable) obj0;
            if(!(obj1 instanceof Comparable)){
                return 0;
            }
            Comparable v2 = (Comparable) obj1;
            return v1.compareTo(v2);
        }
		
	}
	
	/*

	public Collection<IRow> getSortedRows(final IName[] fnames) {
		if(0 == fnames.length){
			return getRows();
		}
		ArrayList<IRow> res = new ArrayList<IRow>(getRows());
		Collections.sort(res, new Comparator<IRow>(){
			Object m_vals1[] = new Object[1];
			Object m_vals2[] = new Object[1];
			RowComparator m_rowComp = new RowComparator();
			public int compare(IRow arg0, IRow arg1) {
				m_vals1[0] = arg0.getField(fnames[0]);
				m_vals2[0] = arg1.getField(fnames[0]);
				return m_rowComp.compare(m_vals1, m_vals2);
			}
		});
		return res;
	}
	*/
	public Collection<IRow> getSortedRows(final IName[] fnames) {
		if(0 == fnames.length){
			return getRows();
		}
		ArrayList<IRow> res = new ArrayList<IRow>(getRows());
		Collections.sort(res, new Comparator<IRow>(){
			int l = fnames.length;
			Object m_vals1[] = new Object[l];
			Object m_vals2[] = new Object[l];
			RowComparator m_rowComp = new RowComparator();
			public int compare(IRow arg0, IRow arg1) {
				for (int i = 0; i < fnames.length; i++) {
					m_vals1[i] = arg0.getField(fnames[i]);
					m_vals2[i] = arg1.getField(fnames[i]); 
				}
				return m_rowComp.compare(m_vals1, m_vals2);
			}
		});
		return res;
	}

	public void setMapRows(Map<Long, IRow> rows) {
		m_rows = rows;
	}
	
	public Map<Long, IRow> getMapRows() {
		return m_rows;
	}

	public void save(TableDef tdef, OutputStream out)  {
		try{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Node dp = doc.appendChild(doc.createElement("DATAPACKET"));
			Node flds = dp.appendChild(doc.createElement("METADATA"))
			.appendChild(doc.createElement("FIELDS"));
			for(int i=0; i<tdef.getFields().size(); i++){
				Element fld = doc.createElement("FIELD");
				FieldDef fd = tdef.getFields().getElement(i);
				setFieldElement(fld, tdef.getDbName(), fd);
				flds.appendChild(fld);
			}
			Node rd = dp.appendChild(doc.createElement("ROWDATA"));
			for(IRow row : m_rows.values()){
				Element r = doc.createElement("ROW");
				for(int i=0; i<tdef.getFields().size(); i++){
					FieldDef fd = tdef.getFields().getElement(i);
					if(row.getValues().containsKey(fd.getName())){
						Object v = row.getField(fd);
						if(null != v){
							String strValue = convertValue2String(fd, v);
							r.setAttribute(fd.getName(), strValue);
						}
					}
				}
				rd.appendChild(r);
			}
			Transformer tr = TransformerFactory.newInstance().newTransformer();
	        tr.setOutputProperty("indent", "yes");
			tr.transform(new DOMSource(doc), new StreamResult(out));
		} catch(ParserConfigurationException | TransformerException  e){
			ApiAlgs.rethrowException(e);
		}
	}
	
	private String convertValue2String(FieldDef fd, Object v) {
		if(null == v)
			return null;
		String res;
		if(fd.getType().equals(ColumnType.BLOB)){
			byte[] barr = (byte[]) v;
			res = Base64.encode(barr);
		}
		else if(fd.getType().equals(ColumnType.DATETIME)){
			Timestamp dt = (Timestamp) v;
			res = m_dateFormat.format(dt);
		}
		else{
			res = v.toString();
		}
		return res;
	}

	public void load(TableDef tdef, InputStream in) throws XPathExpressionException{
		XPath xp = XPathFactory.newInstance().newXPath();
		InputSource src = new InputSource(in);
		NodeList ns = (NodeList) xp.evaluate(".//ROW", src, XPathConstants.NODESET);
		
		
		for(int i=0; i< ns.getLength(); i++){
			Element node = (Element) ns.item(i);
			IRow r1 = emptyRow();
			for(int j=0; j<tdef.getFields().size(); j++){
				FieldDef fd = tdef.getFields().getElement(j);
				String value = node.getAttribute(fd.getName());
				if(fd.getName().equals("ID")){
					r1.setField(fd, Long.parseLong(value));
				}
				else
					r1.setField(fd, value);
				
			}
			if(!r1.getValues().containsKey("ID")){
				r1.setField("ID", i);
			}
			addRow(r1);
		}
	}
	
	//INT, SMALLINT, FLOAT, DATETIME, 
	//DECIMAL, CHAR, NCHAR, VARCHAR, 
	//NVARCHAR, BLOB, LONG, DOUBLE
    String[] DELPHI_FT = {
    		"i4", "i2", "r8", "dateTime", 
    		"fixed", "string.uni", "string.uni", "string.uni", 
    		"string.uni", "bin.hex", "i8", "r8"
    };

	private void setFieldElement(Element fld, String tname, FieldDef fd) {
		fld.setAttribute("fieldtype", DELPHI_FT[fd.getType().ordinal()]);
		if(FieldDef.isStringType(fd.getType())){
			int sz = fd.getSize();
//			if(EnumSet.of(ColumnType.NCHAR, ColumnType.NVARCHAR).contains(fd.getType()))
				sz *= 2;
			fld.setAttribute("WIDTH", Integer.toString(sz));
		}
		if(FieldDef.isDecimalType(fd.getType())){
			fld.setAttribute("WIDTH", Integer.toString(fd.getSize()));
			fld.setAttribute("DECIMALS", Integer.toString(fd.getScale()));			
		}
		if(ColumnType.BLOB.equals(fd.getType())){
			fld.setAttribute("SUBTYPE", "Binary");
			fld.setAttribute("WIDTH", Integer.toString(8));
		}
		fld.setAttribute("attrname", fd.getName());
		
		Document doc = fld.getOwnerDocument();
		Element ep = (Element) fld.appendChild(doc.createElement("PARAM"));
		ep.setAttribute("Roundtrip", "True");
		ep.setAttribute("Value", String.format("\"%s\".\"%s\"", tname, fd.getName()));
		ep.setAttribute("Name", "ORIGIN");

	}

}
