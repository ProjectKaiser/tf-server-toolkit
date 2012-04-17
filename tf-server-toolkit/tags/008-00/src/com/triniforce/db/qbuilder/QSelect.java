/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.triniforce.db.qbuilder.Err.EPrefixNotFound;
import com.triniforce.db.qbuilder.QStatement.QStatementWithWhere;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.utils.ApiStack;

public class QSelect extends QStatementWithWhere{

    // http://savage.net.au/SQL/sql-92.bnf.html#query specification
    public static class SelectList{
        public static class SLExpr{
            private final String m_tablePrefix;
            private final String m_colName;
            SLExpr(String tablePrefix, String colName){
                m_tablePrefix = tablePrefix;
                m_colName = colName;
            }
            public String toString(){
                return QTable.joinPrefixedCol( m_tablePrefix,  m_colName);
            }
        }
        List<SLExpr> m_exprs = new ArrayList<SLExpr>();
        public SelectList addColumn(String tablePrefix, String colName){
            m_exprs.add(new SLExpr(tablePrefix, colName));
            return this;            
        }
        public StringBuffer toStringBuffer() {
            StringBuffer res =new StringBuffer();
            boolean bComma = false;
            for(SLExpr expr: m_exprs){
                if(bComma){
                    res.append(",");
                }else{
                    res = res.append(" ");
                    bComma = true;
                }
                res.append(expr.toString());
            }
            return res;
        }
    }
    
    
    SelectList m_selectList = new SelectList();
    
    public SelectList getSelectList(){return m_selectList;};
    
    protected OrderByClause m_order;
    
    public enum JoinType {
        INNER, LEFT_OUTER;
        @Override
        public String toString() {
            switch (this) {
            case INNER:
                return "inner join"; //$NON-NLS-1$
            case LEFT_OUTER:
                return "left outer join"; //$NON-NLS-1$
            }
            return " unkown join"; //$NON-NLS-1$
        }
    };

    public static class JoinedTable {
        JoinType jt;

        public IQTable qt;
        JoinedTable leftQt;

        String leftCols[];

        String rightCols[];
        
        Expr expr;
    }    
    protected LinkedHashMap<String, JoinedTable> m_tables = new LinkedHashMap<String, JoinedTable>();    
    protected LinkedHashMap<String,Expr> m_getExprs = new LinkedHashMap<String,Expr>();
	private GroupByClause m_group;
    
    public QSelect joinLast(IQTable qt) {
        return joinByPrefix(JoinType.INNER, null, new String[] { "id" }, //$NON-NLS-1$
                new String[] { "id_parent" }, qt); //$NON-NLS-1$
    }

    public QSelect joinLast(JoinType jt, IQTable qt) {
        return joinByPrefix(jt, null, new String[] { "id" }, new String[] { "id_parent" }, //$NON-NLS-1$ //$NON-NLS-2$
                qt);
    }

    public QSelect joinLast(JoinType jt, String leftCol, String rightCol,
            IQTable qt) {
        return joinByPrefix(jt, null, new String[] { leftCol }, new String[] { rightCol },
                qt);
    }
    
    public QSelect joinByPrefix(JoinType jt, String leftTablePrefix, String leftCol, String rightCol,
            IQTable qt) {
        return joinByPrefix(jt, leftTablePrefix, new String[] { leftCol }, new String[] { rightCol },
                qt);
    }    
    
    public QSelect joinByPrefix(JoinType jt, String leftTablePrefix, String leftCol, String rightCol,
            IQTable qt, Expr expr) {
        return joinByPrefix(jt, leftTablePrefix, new String[] { leftCol }, new String[] { rightCol },
                qt, expr);
    }    

    public QSelect joinByPrefix(JoinType jt, String leftTablePrefix, String leftCols[], String rightCols[],
            IQTable qt) throws Err.EPrefixAlreadyExists,
            Err.EPrefixNotFound {
        return joinByPrefix(jt, leftTablePrefix, leftCols, rightCols, qt, null);
    }

    
    /**
     * @param jt
     * @param leftCols
     * @param rightCols
     * @param qt
     * @param leftTablePrefix
     *            if null previous table is used
     * @return
     */
    public QSelect joinByPrefix(JoinType jt, String leftTablePrefix, String leftCols[], String rightCols[],
            IQTable qt, Expr expr ) throws Err.EPrefixAlreadyExists,
            Err.EPrefixNotFound {

        if (leftCols.length <= 0) {
            throw new IllegalArgumentException("leftCols is zero");//$NON-NLS-1$            
        }
        if (rightCols.length <= 0) {
            throw new IllegalArgumentException("rightCols is zero");//$NON-NLS-1$            
        }
        if (rightCols.length != rightCols.length) {
            throw new IllegalArgumentException("rightCols != leftCols");//$NON-NLS-1$            
        }

        if (m_tables.containsKey(qt.getPrefix())) {
            throw new Err.EPrefixAlreadyExists(qt.getPrefix());
        }
        JoinedTable t = new JoinedTable();
        t.jt = jt;
        t.qt = qt;
        t.leftCols = leftCols;
        t.rightCols = rightCols;
        t.expr = expr;

        {// leftQt
            if (null != leftTablePrefix) {
                JoinedTable leftTable = m_tables.get(leftTablePrefix);
                if (null == leftTable) {
                    throw new Err.EPrefixNotFound(leftTablePrefix);
                }
                t.leftQt = leftTable;
            } else {
                t.leftQt = null;
            }

        }
        m_tables.put(qt.getPrefix(), t);
        return this;
    }

    public QSelect orderBy(OrderByClause oc) {
        m_order = oc;
        oc.bindToContext(this);
        return this;
    }    

    @Override
    public QSelect where(WhereClause wc){
        for (JoinedTable tab : m_tables.values()) {
            tab.qt.onBuildWhere(wc);
        }
        super.where(wc);
        return this;
    }    
    
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer("select"); //$NON-NLS-1$
        
        {//SelectList
            StringBuffer sb = m_selectList.toStringBuffer();
            if(sb.length() > 0 ){
                res.append(sb);
            }
        }

        // columns
        {
        	IDatabaseInfo dbInfo = ApiStack.getInterface(IDatabaseInfo.class);
        	String quoteString = dbInfo.getIdentifierQuoteString();
            boolean bComma = false;
            for (JoinedTable t : m_tables.values()) {
                Set<String> names = t.qt.getCols().keySet();
                for (String name : names) {
                    if (bComma) {
                        res = res.append(","); //$NON-NLS-1$
                    } else {
                        res = res.append(" "); //$NON-NLS-1$
                        bComma = true;
                    }
                    if (t.qt.getPrefix().length() > 0) {
                        res = res.append(t.qt.getPrefixedCol(name)).append(" as ") //$NON-NLS-1$
                        .append(t.qt.getPrefix()).append("_").append(name); //$NON-NLS-1$
                    } else {
                    	String resName;
                    	if(name.equals("*"))
                    		resName = name;
                    	else
                    		resName = quoteString + name.toUpperCase() + quoteString;
                        res = res.append(resName);
                    }
                }                
            }
            
            for(Map.Entry<String, Expr> getExpr : m_getExprs.entrySet()){
                if (bComma) {
                    res = res.append(","); //$NON-NLS-1$
                } else {
                    res = res.append(" "); //$NON-NLS-1$
                    bComma = true;
                }
                res = res.append(String.format("%s AS %s", getExpr.getValue(), getExpr.getKey()));
            }
        }
        // from clause
        {
            res.append(" from"); //$NON-NLS-1$
            for (int i = 0; i < m_tables.size() - 1; i++) {
                res.append("("); //$NON-NLS-1$
            }
            JoinedTable prevT = null;
            for (JoinedTable t : m_tables.values()) {
                if (null != prevT) {
                    IQTable joinT = (t.leftQt != null) ? t.leftQt.qt : prevT.qt;
                    res.append(" ").append(t.jt.toString()).append(" ").append(t.qt.getDbName()) //$NON-NLS-1$ //$NON-NLS-2$
                            .append(" ").append(t.qt.getPrefix()).append(" on "); //$NON-NLS-1$ //$NON-NLS-2$
                    for (int i = 0; i < t.leftCols.length; i++) {
                        if (i > 0) {
                            res.append(" and "); //$NON-NLS-1$
                        }
                        res.append(joinT.calcPrefixedCol(t.leftCols[i]))
                                .append(" = ").append(t.qt.calcPrefixedCol(t.rightCols[i])); //$NON-NLS-1$
                    }
                    if(null != t.expr){
                    	res.append(" and ");
                    	res.append(t.expr.toString());
                    }                    
                    res.append(" )"); //$NON-NLS-1$
                } else {
                    res.append(" "); //$NON-NLS-1$
                    if (t.qt.getPrefix().length() > 0) {
                        res.append(t.qt.getDbName()).append(" ").append(t.qt.getPrefix()); //$NON-NLS-1$
                    } else {
                        res.append(t.qt.getDbName());
                    }
                }
                prevT = t;
            }
        }
        
        if(null == m_where){
            where(new WhereClause());
        }
        appendClause(res, m_where);
        appendClause(res, m_group);
        appendClause(res, m_order);
        
        return res.toString();
    }
    public IQTable getTable(String prefix) throws EPrefixNotFound {
        JoinedTable jt = m_tables.get(prefix);
        if(null == jt){
            throw new Err.EPrefixNotFound(prefix);
        }
        return jt.qt;
    }

	public QSelect addGetExpr(Expr expr, String caption) {
		expr.bindToContext(this);
		m_getExprs.put(caption, expr);
		return this;
	}

	public QSelect groupBy(GroupByClause groupByClause) {
		m_group = groupByClause;
		return this;
		
	}
}
