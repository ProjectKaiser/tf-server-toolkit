/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import com.triniforce.db.qbuilder.Err.EPrefixNotFound;
import com.triniforce.utils.IName;

/**
 * Operation like =, >, < for where clause Usage - Constructor - add
 * suboperations - bindToContext(). Allows e.g. to check table prefixes -
 * toString()
 * 
 */
public class Expr {

	protected HashMap<String, HashSet<String>> m_prefixesToFix;

    protected void addPrefixToCheck(String prefix, String col) {
        if (null == m_prefixesToFix) {
            m_prefixesToFix = new HashMap<String, HashSet<String>>();
        }
        if (!m_prefixesToFix.containsKey(prefix)) {
            m_prefixesToFix.put(prefix, new HashSet<String>());
        }
        if (col.length() > 0) {
            m_prefixesToFix.get(prefix).add(col);
        }
    }

    public enum LogicalOp {
        or, and
    };

    /**
     * Invoked when operation is joined to statement
     * 
     * @throws Err.EPrefixNotFound
     */
    public void bindToContext(IQContext ctx) throws Err.EPrefixNotFound {
        if( null == m_prefixesToFix ) return;
        for (String prefix : m_prefixesToFix.keySet()) {
            IQTable qt = ctx.getTable(prefix);
            for (String col : m_prefixesToFix.get(prefix)) {
                qt.getPrefixedCol(col);
            }
        }
    }

    public static class EUnkonwnEqKind extends RuntimeException {
        private static final long serialVersionUID = -5929708420358181332L;

        public EUnkonwnEqKind(String msg) {
            super(msg);
        }
    }

    public enum Funcs { Upper("UCASE");
        private final String m_str;
        @Override
		public String toString(){
            return m_str;
        }
        Funcs(String str) {
            m_str = str;
        }
    }
    
    public enum EqKind {

        EQ, GT, GE, LT, LE, NE;
        @Override
        public String toString() {
            switch (this) {
            case EQ:
                return "="; //$NON-NLS-1$
            case GT:
                return ">"; //$NON-NLS-1$
            case GE:
                return ">="; //$NON-NLS-1$
            case LT:
                return "<"; //$NON-NLS-1$
            case LE:
                return "<="; //$NON-NLS-1$
            case NE:
                return "!="; //$NON-NLS-1$
            }
            throw new EUnkonwnEqKind("Unknown"); //$NON-NLS-1$
        }

        public static EqKind fromString(String src) {
            if (src.equals("=")) //$NON-NLS-1$
                return EQ;
            if (src.equals(">")) //$NON-NLS-1$
                return GT;
            if (src.equals(">=")) //$NON-NLS-1$
                return GE;
            if (src.equals("<")) //$NON-NLS-1$
                return LT;
            if (src.equals("<=")) //$NON-NLS-1$
                return LE;
            if (src.equals("!=")) //$NON-NLS-1$
                return NE;
            throw new EUnkonwnEqKind(src);
        }
    };

    public static class Func extends Expr {
        private final Funcs m_func;
        private final Expr m_expr;

        public Func(Funcs func, Expr expr) {
            m_func = func;
            m_expr = expr;
        }
        @Override
        public String toString() {
            return "{fn " + m_func.toString() +"("+m_expr.toString() +")}";
        }
        @Override
        public void bindToContext(IQContext ctx) throws EPrefixNotFound {
            m_expr.bindToContext(ctx);
        }

    }

    public static class Param extends Expr {
        public Param() {
        }

        @Override
        public String toString() {
            return "?";//$NON-NLS-1$
        }
    }

    public static class Column extends Expr {
        protected String m_str;

        public Column(String tablePrefix, String col) {
            addPrefixToCheck(tablePrefix, ""); //$NON-NLS-1$
            m_str = QTable.joinPrefixedCol(tablePrefix, col);
        }
        
        public Column(String tablePrefix, IName col) {
            addPrefixToCheck(tablePrefix, ""); //$NON-NLS-1$
            m_str = QTable.joinPrefixedCol(tablePrefix, col.getName());
        }

        @Override
        public String toString() {
            return m_str;
        }
    }

    public static class List extends Expr {
        ArrayList<Expr> m_items = new ArrayList<Expr>();

        public List add(Expr expr) {
            m_items.add(expr);
            return this;
        }

        @Override
        public void bindToContext(IQContext ctx) throws EPrefixNotFound {
            for (Expr expr : m_items) {
                expr.bindToContext(ctx);
            }
        }

        @Override
        public String toString() {
            if (m_items.size() == 0)
                return ""; //$NON-NLS-1$
            StringBuilder res = new StringBuilder();
            boolean bComma = false;
            for (Expr item : m_items) {
                if (!bComma) {
                    bComma = true;
                } else {
                    res.append(", "); //$NON-NLS-1$
                }
                res.append(item.toString());
            }
            return res.toString();
        }
    }

    public static class Compare extends Expr {

        protected String m_str;

        protected String m_checkTable;

        protected String m_checkCol;

        /**
         * 
         * Sop can be "<", "!=", "=" etc. If tablePrefix2 is null, compares
         * with placeholder "?" Otherwise compares with another table field
         * 
         */

        public Compare(Expr e1, EqKind op, Expr e2) {
            m_str = e1.toString() + " " + op.toString() + " " + e2.toString();
        }

        public Compare(String tablePrefix, String col, String sop,
                String tablePrefix2, String col2) {
            this(tablePrefix, col, EqKind.fromString(sop), tablePrefix2, col2);
        }

        Compare(String tablePrefix, String col, EqKind op, String tablePrefix2,
                String col2) {
            addPrefixToCheck(tablePrefix, ""); //$NON-NLS-1$
            if (null == tablePrefix2) {
                m_str = QTable.joinPrefixedCol(tablePrefix, col) + " " //$NON-NLS-1$
                        + op.toString() + " ?"; //$NON-NLS-1$
            } else {
                m_str = QTable.joinPrefixedCol(tablePrefix, col) + " " //$NON-NLS-1$
                        + op.toString() + " " //$NON-NLS-1$
                        + QTable.joinPrefixedCol(tablePrefix2, col2);
                addPrefixToCheck(tablePrefix2, ""); //$NON-NLS-1$
            }
        }

        public Compare(String tablePrefix, String col, String sop, Object expr) {
            addPrefixToCheck(tablePrefix, ""); //$NON-NLS-1$
            m_str = QTable.joinPrefixedCol(tablePrefix, col)
                    + " " + EqKind.fromString(sop).toString() //$NON-NLS-1$
                    + " " + expr; //$NON-NLS-1$
        }

        @Override
        public String toString() {
            return m_str;
        }
    }

    public static class IntegralConstant extends Expr{
        String m_str;
        void init(Number num){
            if(null == num){
                m_str = "null";
            } else {
            	m_str = num.toString();
            }

        }
        public IntegralConstant(Long num) {
            init(num);
        }
        public IntegralConstant(Integer num) {
            init(num);
        }
        @Override
        public String toString() {
            return m_str;
        }
        
    }
    
    public static class IsNull extends Expr {
        String m_str;

        public IsNull(String prefix, String col, boolean isNull) {
            String nulls = isNull ? "is null" : "is not null"; //$NON-NLS-1$ //$NON-NLS-2$
//            if (isNull) {
                m_str = QTable.joinPrefixedCol(prefix, col) + " " + nulls; //$NON-NLS-1$
//            }
            // addPrefixToCheck(prefix, col);
            addPrefixToCheck(prefix, "");
        }

        @Override
        public String toString() {
            return m_str;
        }
    }

    public static class Like extends Expr {

        private String m_str;

        public Like(String prefix, String col, boolean bEscape) {
            m_str = QTable.joinPrefixedCol(prefix, col) + " LIKE ?"; //$NON-NLS-1$
            if (bEscape)
                m_str += " {escape \'%\'}";
            addPrefixToCheck(prefix, "");
        }

        @Override
        public String toString() {
            return m_str;
        }
    }

    public static class In extends Expr {



        private String m_str;

        public In(String prefix, String col, int nargs) {
        	this(prefix, col, nargs, true);
        }

        public In(String prefix, String col, int nargs, boolean bIn) {
        	this(prefix, col, Collections.nCopies(nargs, '?'), bIn);
		}
        
        public In(String prefix, String col, Collection vals, boolean bIn) {
        	this(prefix, col, toStringSet(vals), bIn);
		}

        static String toStringSet(Collection vals){
        	StringBuffer buf = new StringBuffer();
        	boolean bFirst = true;
        	for (Object object : vals) {
        		String str;
        		if(object instanceof String){
        			str = "\'"+object+"\'";
        		}
        		else 
        			str = object.toString();
        		if(bFirst){
        			buf.append(str);
        			bFirst = false;
        		}
        		else{
					buf.append(',');
					buf.append(str);
        		}
			}
        	return buf.toString();
        }

		public In(String prefix, String col, String setOfVals, boolean bIn) {
            addPrefixToCheck(prefix, "");
            m_str = String.format("%s %s (%s)", QTable.joinPrefixedCol(prefix, col),  bIn ? "IN" : "NOT IN", setOfVals); //$NON-NLS-1$
		}

		public In(String prefix, String col, QSelect qs) {
			this(prefix, col, qs.toString(), true);			
		}
		
		

		@Override
        public String toString() {
			return m_str;
        }
    }

    public static class Brackets extends Expr {
        protected static class BrItem {
            LogicalOp lop;

            Expr op;
        }

        ArrayList<BrItem> m_items = new ArrayList<BrItem>();

        public Brackets and(Expr op) {
            BrItem item = new BrItem();
            item.lop = LogicalOp.and;
            item.op = op;
            m_items.add(item);
            return this;
        };

        public Brackets or(Expr op) {
            BrItem item = new BrItem();
            item.lop = LogicalOp.or;
            item.op = op;
            m_items.add(item);
            return this;
        };

        @Override
        public void bindToContext(IQContext ctx) throws EPrefixNotFound {
            for (BrItem item : m_items) {
                item.op.bindToContext(ctx);
            }
        }

        @Override
        public String toString() {
            if (m_items.size() == 0)
                return ""; //$NON-NLS-1$
            StringBuilder res = new StringBuilder();
            res.append("( "); //$NON-NLS-1$
            boolean bComma = false;
            for (BrItem item : m_items) {
                if (!bComma) {
                    bComma = true;
                } else {
                    res.append(item.lop.equals(LogicalOp.or) ? " or " //$NON-NLS-1$
                            : " and "); //$NON-NLS-1$
                }
                res.append(item.op.toString());
            }
            res.append(" )"); //$NON-NLS-1$
            return res.toString();
        }
    }
    
    public static class Between extends Expr{

		private String m_str;

		public Between(String prefix, String col) {
			m_str = String.format("%s BETWEEN ? AND ?", QTable.joinPrefixedCol(prefix, col)); 
		}
		
		@Override
		public String toString() {
			return m_str;
		}
    	
    }

    public static class SinglePred extends Expr {

		private Expr m_arg;
		private String m_predName;

		public SinglePred(String predName, Expr arg) {
			m_arg = arg;
			m_predName = predName;
		}
		
		@Override
		public void bindToContext(IQContext ctx) throws EPrefixNotFound {
			super.bindToContext(ctx);
			m_arg.bindToContext(ctx);
		}
		
		@Override
		public String toString() {
			return String.format("%s(%s)", m_predName, m_arg.toString());
		}

	}
    
    public static class QSelectExpr extends Expr{

		public QSelectExpr(QSelect qs) {
			// TODO Auto-generated constructor stub
		}
    	
    }

}