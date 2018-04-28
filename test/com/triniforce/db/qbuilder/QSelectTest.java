/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.db.qbuilder;

import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.qbuilder.QSelect.JoinType;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class QSelectTest extends TFTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Api api = new Api();
        api.setIntfImplementor(IDatabaseInfo.class, new IDatabaseInfo() {
            
            public String getIdentifierQuoteString() {
                return "\"";
            }
            
            public DbType getDbType() {
                return null;
            }
        });
        ApiStack.pushApi(api);        
    }
    
    @Override
    protected void tearDown() throws Exception {
        ApiStack.popApi();
        super.tearDown();
    }
    
    public void testSelectList(){
        {
            QSelect sel = new QSelect();
            sel.getSelectList()
                .addColumn("art", "col1")
                .addColumn("art", "col2");

            
            sel.joinLast(new QTable("articles", "art"));
            String s = sel.toString();
            assertEquals("select art.\"COL1\",art.\"COL2\" from articles art", s );
        }
        
    }
    
	public void testAddGetExpr(){
		{
			QSelect qSel = new QSelect();
			try{
				qSel.addGetExpr(new Expr.Column("ttt", "unkColumn"), null);
				fail();
			} catch(Err.EPrefixNotFound e){
				assertEquals("ttt", e.getMessage());
			}
			
			try{
				qSel.addGetExpr(new Expr.Column("tbt", "unkColumn"), null);
				fail();
			} catch(Err.EPrefixNotFound e){
				assertEquals("tbt", e.getMessage());
			}
			
			qSel.joinLast(new QTable("Table_001", "T1"));
			qSel.addGetExpr(new Expr.Column("T1", "Column_1"), "COL_TITLE1");
			
			assertEquals("select T1.\"COLUMN_1\" AS COL_TITLE1 from Table_001 T1", qSel.toString());
		}
		{
			QSelect qSel = new QSelect();
			qSel.joinLast(new QTable("table_0041"));
			qSel.addGetExpr(new Expr.SinglePred("count", new Expr.Column("", "col1")), "cnt");
			assertEquals("select count(\"COL1\") AS cnt from table_0041", qSel.toString());
		}
	}
	
	public void testJoinTableWithParams(){
		{
			QSelect qsel = new QSelect();
			qsel.joinLast(new QTable("tab1", "T"));
			qsel.joinByPrefix(JoinType.INNER, "T", "left_col1", "right_col2", new QTable("tab2", "T2"), 
					new Expr.Compare("T2", "CC3", "=", 554));
			assertEquals(
					"select from( tab1 T " +
					"inner join tab2 T2 on T.\"LEFT_COL1\" = T2.\"RIGHT_COL2\" and T2.\"CC3\" = 554 )", 
					qsel.toString());
			
			qsel = new QSelect();
			qsel.joinLast(new QTable("tab1", "T"));
			qsel.joinByPrefix(JoinType.INNER, "T", 
					new String[]{"left_col1","left_col2"}, 
					new String[]{"right_col1", "right_col2"}, new QTable("tab2", "T2"));
			assertEquals(
					"select from( tab1 T " +
					"inner join tab2 T2 on T.\"LEFT_COL1\" = T2.\"RIGHT_COL1\" and T.\"LEFT_COL2\" = T2.\"RIGHT_COL2\" )", 
					qsel.toString());
		}
		
		{
			// join by Expr
			QSelect qsel = new QSelect();
			qsel.joinLast(new QTable("tab1", "T"));
			qsel.joinByPrefix(JoinType.LEFT_OUTER, "T", new String[]{}, new String[]{}, new QTable("tab2", "T2"), 
					new Expr.Compare("T", "col1", "<", "T2", "col2"));
			assertEquals("select from( tab1 T left outer join tab2 T2 on  and T.\"COL1\" < T2.\"COL2\" )", qsel.toString());			
		}
	}
	
	public void testGroupBy(){
		QSelect qsel = new QSelect();
		qsel.joinLast(new QTable("tab1", "T"));
		assertSame(qsel, qsel.groupBy(new GroupByClause().addCol("T", "column_00")));
		assertEquals("select from tab1 T group by T.\"COLUMN_00\"", qsel.toString());
		
	}
	
}
