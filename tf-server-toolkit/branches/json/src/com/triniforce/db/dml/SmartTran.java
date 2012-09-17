/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.dml;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.triniforce.db.qbuilder.Expr;
import com.triniforce.db.qbuilder.OrderByClause;
import com.triniforce.db.qbuilder.QDelete;
import com.triniforce.db.qbuilder.QInsert;
import com.triniforce.db.qbuilder.QSelect;
import com.triniforce.db.qbuilder.QTable;
import com.triniforce.db.qbuilder.QUpdate;
import com.triniforce.db.qbuilder.WhereClause;
import com.triniforce.server.plugins.kernel.SrvTable;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IName;

/**
 * @author Alex
 * 
 */
public class SmartTran extends StmtContainer implements ISmartTran {

    private boolean m_bCommited;
    private boolean m_doNotCommit = false;

    /**
     * Construct transaction Connection.autoCommit must be false
     * 
     * @param connection
     */
    public SmartTran(Connection connection, IPrepSqlGetter sqlGetter) {
        super(connection, sqlGetter);
        m_bCommited = false;
    }

    public SmartTran(Connection connection) {
        this(connection, null);
    }

    public void commit() {
        close(toBeCommited());
    }

    public void close(boolean bCommit) {
        if (m_closed)
            return;

        //close all statements first
        super.close();
        
        try {
            if(bCommit){
                m_conn.commit();                
            }else{
                m_conn.rollback();
            }

        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        m_bCommited = bCommit;
    }

    @Override
    public void close() {
        close(false);

    }

    public boolean isCommited() {
        return m_bCommited;
    }

    public void doNotCommit() {
        m_doNotCommit = true;
    }

    public boolean toBeCommited() {
        return !m_doNotCommit;
    }

	public void insert(Class table, List<IName> fields, List<Object> values) {
		IName a_fields[] = new IName[fields.size()];
		fields.toArray(a_fields);
		insert(table, a_fields, values.toArray());
	}
    
	public void insert(Class table, IName[] fields, Object[] values) {
        QTable t = new SrvTable(table);
        for (IName name : fields) {
            t.addCol(name);
        }
        QInsert ins = new QInsert(t);
        IStmtContainer sc = this;
        PrepStmt ps = sc.prepareStatement(ins.toString());
        int col = 1;
        for (Object arg : values) {
            ps.setObject(col++, arg);
        }
        ps.execute();
        ps.close();
	}
	
	public ResSet select(Class table, IName fields[],
            IName lookUpFields[], Object lookUpValues[]) {
	    return select(table, fields, lookUpFields, lookUpValues, null);
    }
	
    public ResSet select(Class table, IName fields[],
            IName lookUpFields[], Object lookUpValues[], IName orderByFields[]) {
        QTable t = new SrvTable(table);
        for (IName name : fields) {
            t.addCol(name);
        }
        QSelect q = new QSelect();
        q.joinLast(t);
        
//TODO bPreparedStatement
        {
	        int i=0;
	        WhereClause wc = new WhereClause();
	        for (IName name : lookUpFields) {
	        	if(null == lookUpValues[i]){
	        		wc.and(new Expr.IsNull("", name.getName(), true));
	        	}
	        	else if(lookUpValues[i].getClass().isArray()){
	        		wc.and(new Expr.In("", name.getName(), Array.getLength(lookUpValues[i])));
	        	}
                else if(lookUpValues[i] instanceof ISmartTran.Between){
                    wc.and(new Expr.Between("", name.getName()));
                }        	
	        	else{
	        		wc.andCompare("", name.getName(), "=");
	        	}
	            i++;
	        }
	        q.where(wc);
        }

        if (null != orderByFields) {
            OrderByClause oc = new OrderByClause();
            for (IName name : orderByFields) {
                if (name instanceof ISmartTran.DescName) {
                    oc.add(new OrderByClause.DescColumn("", name.getName()));
                } else {
                    oc.addCol("", name.getName());
                }
            }
            q.orderBy(oc);
        }
        PrepStmt ps = this.prepareStatement(q.toString());
        int col = 1;
        for (Object arg : lookUpValues) {
        	if(null == arg)
        		continue;
        	if(arg.getClass().isArray()){
        		for(int i=0; i<Array.getLength(arg); i++)
        			ps.setObject(col++, Array.get(arg, i));
        	}
            else if(arg instanceof ISmartTran.Between){
                ISmartTran.Between data = (Between) arg;
                ps.setObject(col++, data.getLeftValue());
                ps.setObject(col++, data.getRightValue());
            }           
        	else{
        		ps.setObject(col++, arg);
        	}
        }
        return ps.executeQuery();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T instantiateBL(Class<? extends T> cls){
        try {
            BusinessLogic bl = (BusinessLogic) cls.newInstance();
            bl.setSt(this);
            return (T) bl;            
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }

    public void delete(Class table, IName[] lookUpFields, Object[] lookUpValues) {
        QTable t = new SrvTable(table);
        QDelete q = new QDelete(t);
        WhereClause wc = new WhereClause();
        for (IName name : lookUpFields) {
            wc.andCompare("", name.getName(), "=");
        }
        q.where(wc);
        IStmtContainer sc = this;
        PrepStmt ps = sc.prepareStatement(q.toString());
        int col = 1;
        for (Object arg : lookUpValues) {
            ps.setObject(col++, arg);
        }        
        ps.execute();
        ps.close();
    }

    public static class FieldsValues{
    	final public List<IName> m_fields;
    	final public List<Object> m_values;
    	FieldsValues(Map<IName, Object> values){
    		m_fields =new ArrayList<IName>(values.size());
    		m_values =new ArrayList<Object>(values.size());
    		for(Map.Entry<IName, Object> e: values.entrySet()){
    			m_fields.add(e.getKey());
    			m_values.add(e.getValue());
    		}
    	}
    }
    
	public void insert(Class table, Map<IName, Object> values) {
		FieldsValues fv = new FieldsValues(values);
		insert(table, fv.m_fields, fv.m_values);
	}
	
    public void update(Class table, IName[] fields, Object[] values,
            IName[] lookUpFields, Object[] lookUpValues) {
    	update(table, Arrays.asList(fields), Arrays.asList(values), Arrays.asList(lookUpFields), Arrays.asList(lookUpValues));
    }
	
	public void update(Class table, Map<IName, Object> values, Map<IName, Object> lookUpValues){
		FieldsValues fv = new FieldsValues(values);
		FieldsValues lu = new FieldsValues(lookUpValues);
		update(table, fv.m_fields, fv.m_values, lu.m_fields, lu.m_values);
	}

	public void update(Class table, List<IName> fields, List<Object> values,
			List<IName> lookUpFields, List<Object> lookUpValues) {
		
		QTable t = new SrvTable(table);
        for (IName name : fields) {
            t.addCol(name);
        }        
        QUpdate q = new QUpdate(t);
        WhereClause wc = new WhereClause();
        for (IName name : lookUpFields) {
            wc.andCompare("", name.getName(), "=");
        }
        q.where(wc);
        IStmtContainer sc = this;
        PrepStmt ps = sc.prepareStatement(q.toString());
        int col = 1;
        for (Object arg : values) {
            ps.setObject(col++, arg);
        }
        for (Object arg : lookUpValues) {
            ps.setObject(col++, arg);
        }        
        ps.execute();
        ps.close();
		
	}
	
}
