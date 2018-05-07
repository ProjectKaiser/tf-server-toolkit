/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */

package com.triniforce.db.qbuilder;


import java.util.Collections;

import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.qbuilder.Expr.Between;
import com.triniforce.db.qbuilder.Expr.Func;
import com.triniforce.db.qbuilder.Expr.In;
import com.triniforce.db.qbuilder.Expr.Like;
import com.triniforce.db.qbuilder.Expr.QSelectExpr;
import com.triniforce.db.qbuilder.Expr.SinglePred;
import com.triniforce.db.qbuilder.OrderByClause.DescColumn;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class ExprTest extends TFTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Api api = new Api();
        api.setIntfImplementor(IDatabaseInfo.class, new IDatabaseInfo() {
            
            @Override
			public String getIdentifierQuoteString() {
                return "\"";
            }
            
            @Override
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
	
    
    public void testIntegralConstant(){
    	Integer ic = null;
    	new Expr.IntegralConstant(ic);
    }
    
    public void testFunc(){
        {
            Func func = new Func(Expr.Funcs.Upper, new Expr.Param());
            assertEquals( func.toString(), "{fn UCASE(?)}");
        }
        {
            Func func = new Func(Expr.Funcs.Upper, new Expr.Column("t1", "name"));
            assertEquals( func.toString(), "{fn UCASE(t1.\"NAME\")}");           
        }
        {
            Expr.Compare cmp = new Expr.Compare(
                    new Expr.Func( Expr.Funcs.Upper, new Expr.Column("t2", "name1") )
                    , Expr.EqKind.EQ
                    ,new Expr.Param()
            );
            assertEquals( cmp.toString(), "{fn UCASE(t2.\"NAME1\")} = ?");
        }

    }
    
	public void testLike(){
		Like like = new Expr.Like("", "col1", false);
		assertEquals("\"COL1\" LIKE ?", like.toString());
		
		like = new Expr.Like("FX", "col2", true);
		assertEquals("FX.\"COL2\" LIKE ? {escape \'%\'}", like.toString());
	}
	
	public void testDescColumn(){
		DescColumn col = new DescColumn("pfx", "col1");
		assertEquals("pfx.\"COL1\" DESC", col.toString());
		
		col = new DescColumn("pfx2", "col2");
		assertEquals("pfx2.\"COL2\" DESC", col.toString());
		
		OrderByClause order = new OrderByClause()
		.addCol("pfx1", "col1")
		.add(new DescColumn("pfx2", "col2"));
		assertEquals("order by pfx1.\"COL1\", pfx2.\"COL2\" DESC", order.toString());
	}
	
	public void testIn(){
		assertEquals(3, Collections.nCopies(3, null).size());
		
		In expr = new Expr.In("sdgs", "ghdshdf", 3);
		assertEquals("sdgs.\"GHDSHDF\" IN (?,?,?)", expr.toString());
		
		expr = new Expr.In("fdss", "jhgkg", 5);
		assertEquals("fdss.\"JHGKG\" IN (?,?,?,?,?)", expr.toString());
		
		assertEquals("fdss.\"JHGKG\" IN ()", new Expr.In("fdss", "jhgkg", 0).toString());
//		try{
//			new Expr.In("fdss", "jhgkg", 0);
//			fail();
//		} catch(IllegalArgumentException e){
//		}
		
		assertEquals("test1.\"TEST2\" NOT IN (?,?)", new Expr.In("test1","test2",2, false).toString());
		
		assertEquals("gdf.\"RRWERW\" IN (fgdgsd)", new Expr.In("gdf", "rrwerw", "fgdgsd",true).toString());
	}
			
	public void testBetween(){
		Between between = new Expr.Between("pfx", "column");
		assertEquals("pfx.\"COLUMN\" BETWEEN ? AND ?", between.toString());
		
		between = new Expr.Between("pfx2", "clm12");
		assertEquals("pfx2.\"CLM12\" BETWEEN ? AND ?", between.toString());
	}
	
	public void testIsNull(){
		assertEquals("prfx.\"COL1\" is null", new Expr.IsNull("prfx", "col1", true).toString());
		assertEquals("prfx.\"COL1\" is not null", new Expr.IsNull("prfx", "col1", false).toString());
	}
	
	public void testSinglePred(){
		SinglePred sp = new Expr.SinglePred("fun1", new Expr.Column("TBB", "ColName"));
		
		QSelect qSel = new QSelect();
		try{
			sp.bindToContext(qSel);
			fail();
		} catch(Err.EPrefixNotFound e){
			assertEquals("TBB", e.getMessage());
		}
		
		qSel.joinLast(new QTable("Tab1", "TBB"));
		sp.bindToContext(qSel);
		
		assertEquals("fun1(TBB.\"COLNAME\")", sp.toString());
		
	}
	
	public void testInQSelectExpr(){
		QSelect qs = new QSelect();
		qs.joinLast(new QTable("tab999").addCol("SOME_COL001"));
		qs.groupBy(new GroupByClause().addCol("", "SOME_COL001"));
		In expr = new Expr.In("T88", "C8R", qs);
		assertEquals("T88.\"C8R\" IN (select \"SOME_COL001\" from tab999 group by \"SOME_COL001\")", expr.toString());
	}
}
