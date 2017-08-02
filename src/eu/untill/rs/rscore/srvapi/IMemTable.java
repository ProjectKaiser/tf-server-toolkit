package eu.untill.rs.rscore.srvapi;

import java.util.Collection;
import java.util.Map;

import com.triniforce.utils.IName;



public interface IMemTable {
    public static enum ModType{INSERTED, UPDATED, NOTHING};
    
    
    @SuppressWarnings("serial")
	public static class EFieldNotFound extends RuntimeException{
    	public EFieldNotFound(String fname) {
			super(fname);
		}
    }
    
	public static interface IRow{
        /**
         * @return INSERTED by default
         */
        ModType getModType();
        void setModType(ModType modType);
        
		void setField(IName fname, Object value);
		Object getField(IName fname) throws EFieldNotFound;
		
		void setField(String fname, Object value);
		Object getField(String fname) throws EFieldNotFound;		
		
		//IName[] getColumns();
		Long getId();
		Long getId(IName fname);
		Long getId(String fname);
		
		boolean isExist(String fname);
		void dropField(String fname);
		
		Map<String, Object> getValues();
		void setValues(Map<String, Object> v);
	}
	
	void addRow(IRow row);
	IRow getById(long id);
    
    Collection<IRow> getRows();
    IRow emptyRow();
    IRow cloneRow(IRow row);
    
   
    //List<IRow> getRowsByModType(ModType modType);
    
   	Collection<IRow> getSortedRows(IName fnames[]);
   	
   	/* setMapRows и getMapRows используются в RSSync
   	 * в процессе переноса данных из MemTable
   	 * в SrcTable, MasterTable, ArticlesTable.  
   	 */
   	void setMapRows(Map<Long, IRow> rows);
	Map<Long, IRow> getMapRows();
	
}
