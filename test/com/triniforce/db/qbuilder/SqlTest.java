/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.db.qbuilder;

import com.triniforce.db.ddl.UpgradeRunner.DbType;
import com.triniforce.db.qbuilder.Err.EPrefixNotFound;
import com.triniforce.db.qbuilder.Expr.EUnkonwnEqKind;
import com.triniforce.db.qbuilder.Expr.EqKind;
import com.triniforce.db.qbuilder.QSelect.JoinType;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.server.srvapi.IDatabaseInfo;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class SqlTest extends TFTestCase {
	
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

    public void testInsert() {
        {// one field
            QInsert upd = new QInsert(new QTable("articles", "").addCol("name"));
            String s = upd.toString();
            assertEquals(s, "insert into articles ( \"NAME\" ) values ( ? )");
        }
        {// two fieldd
            QInsert upd = new QInsert(new QTable("articles", "").addCol("name")
                    .addCol("id"));
            String s = upd.toString();
            assertEquals(s, "insert into articles ( \"NAME\", \"ID\" ) values ( ?, ? )");
        }
    }

    public void testDelete(){
        {// by id
            QDelete del = new QDelete(
                    new QTable("articles")
                )

            .where(new WhereClause()

            .andCompare("", "id", "=")

            );
            String s = del.toString();
            assertEquals(s, "delete from articles where ( \"ID\" = ? )");
        }        
        
    }
    
    public void testUpdate() {

        {// one field
            QUpdate upd = new QUpdate(

            new QTable("articles", "").addCol("name"))

            .where(new WhereClause()

            .andCompare("", "id", "=")

            );
            String s = upd.toString();
            assertEquals(s, "update articles set \"NAME\" = ? where ( \"ID\" = ? )");
        }
        {// two fields
            QUpdate upd = new QUpdate(

            new QTable("articles", "a").addCol("name").addCol("article_number"))

            .where(new WhereClause()

            .andCompare("a", "id", "=")

            );
            String s = upd.toString();
            assertEquals(s,
                    "update articles a set a.\"NAME\" = ?, a.\"ARTICLE_NUMBER\" = ? where ( a.\"ID\" = ? )");
        }
    }

    public void testCompKind() {
        for (EqKind ck : EqKind.values()) {
            assertEquals(ck, EqKind.fromString(ck.toString()));
        }
    }

    public void testAsteric() {
        {// order by one column no table prefix
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "").addCol("*"))

            .where(new WhereClause()

            .andCompare("", "id", ">"))

            .orderBy(new OrderByClause()

            .addCol("", "name")

            );
            String s = sel.toString();
            assertEquals(s,
                    "select * from articles where ( \"ID\" > ? ) order by \"NAME\"");
        }
    }

	public void testOrderBy() {

        {// wrong table prefix
            Throwable t = null;
            try {
                new QSelect()

                .joinLast(new QTable("articles", "art").addCol("name"))

                .where(new WhereClause()

                .andCompare("art", "id", ">"))

                .orderBy(new OrderByClause()

                .addCol("art1", "name")

                );
                fail();
            } catch (Err.EPrefixNotFound e) {
                t = e;
                assertEquals(t.getMessage(), "art1");
            }
        }

        {// wrong Expr
            try {
                new QSelect()

                .joinLast(new QTable("articles", "art").addCol("name"))

                .where(new WhereClause()

                .andCompare("art", "id", ">"))

                .orderBy(new OrderByClause()

                .add(new Expr.List())

                );
                fail();
            } catch (Err.ENotAllowedExprType e) {
                assertEquals(e.getMessage(), Expr.List.class.getName());
            }
        }

        {// order by one column no table prefix
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "").addCol("name"))

            .where(new WhereClause()

            .andCompare("", "id", ">"))

            .orderBy(new OrderByClause()

            .addCol("", "name")

            );
            String s = sel.toString();
            assertEquals(s,
                    "select \"NAME\" from articles where ( \"ID\" > ? ) order by \"NAME\"");
        }
        {// order by two columns no table prefix
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "").addCol("name"))

            .where(new WhereClause()

            .andCompare("", "id", ">"))

            .orderBy(new OrderByClause()

            .addCol("", "name").addCol("", "id")

            );
            String s = sel.toString();
            assertEquals(s,
                    "select \"NAME\" from articles where ( \"ID\" > ? ) order by \"NAME\", \"ID\"");
        }

        {// order by one column with table prefix
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "art").addCol("name"))

            .where(new WhereClause()

            .andCompare("art", "id", ">"))

            .orderBy(new OrderByClause()

            .addCol("art", "name")

            );
            String s = sel.toString();
            assertEquals(
                    s,
                    "select art.\"NAME\" as art_name from articles art where ( art.\"ID\" > ? ) order by art.\"NAME\"");
        }
        {// order by two columns with table prefix
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "art").addCol("name"))

            .where(new WhereClause()

            .andCompare("art", "id", ">"))

            .orderBy(new OrderByClause()

            .addCol("art", "id").addCol("art", "name")

            );
            String s = sel.toString();
            assertEquals(
                    s,
                    "select art.\"NAME\" as art_name from articles art where ( art.\"ID\" > ? ) order by art.\"ID\", art.\"NAME\"");
        }

    }

    public void testWhere() {
        {//bug with empty brackets m_prefixesToFix  
            String tabName = "myTable";
            QSelect sel = new QSelect().joinLast(new QTable(tabName, "t").addCol("name"));
            WhereClause where = new WhereClause();
            where.and(
                    new Expr.Compare(
                            new Expr.Func(Expr.Funcs.Upper, new Expr.Column("t", "name"))
                            ,Expr.EqKind.EQ
                            ,new Expr.Param()
                    )
            );
            sel.where(where);
            assertEquals("select t.\"NAME\" as t_name from myTable t where ( {fn UCASE(t.\"NAME\")} = ? )", sel.toString());
        }
        
        // wrong first table in where clause
        {
            try {
                new QSelect()

                .joinLast(new QTable("articles", "art").addCol("name"))

                .joinLast(JoinType.INNER, "id_departament", "id",
                        new QTable("department", "dep").addCol("name"))

                .where(new WhereClause().andCompare("dep1", "id", ">"));
                fail();
            } catch (EPrefixNotFound e) {
                assertEquals(e.getMessage(), "dep1");
            }

        }
        // wrong second table in where clause
        {
            try {
                new QSelect()

                .joinLast(new QTable("articles", "art").addCol("name"))

                .joinLast(JoinType.INNER, "id_departament", "id",
                        new QTable("department", "dep").addCol("name"))

                .where(new WhereClause()

                .andCompare("dep", "id", ">")

                .andCompare("dep2", "id", ">")

                );
                fail();
            } catch (EPrefixNotFound e) {
                assertEquals(e.getMessage(), "dep2");
            }

        }
        {// correct single equals no table prefix
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "").addCol("name"))

            .where(new WhereClause()

            .andCompare("", "id", "="));
            String s = sel.toString();
            assertEquals(s, "select \"NAME\" from articles where ( \"ID\" = ? )");
        }
        {// correct single equals with table prefix
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "art").addCol("name"))

            .joinLast(JoinType.INNER, "id_departament", "id",
                    new QTable("department", "dep").addCol("name"))

            .where(new WhereClause()

            .andCompare("dep", "id", "="));
            String s = sel.toString();
            assertEquals(
                    "select art.\"NAME\" as art_name,dep.\"NAME\" as dep_name from( articles art inner join department dep on art.\"ID_DEPARTAMENT\" = dep.\"ID\" ) where ( dep.\"ID\" = ? )",
                    s);
        }

        {// correct
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "art").addCol("name"))

            .joinLast(JoinType.INNER, "id_departament", "id",
                    new QTable("department", "dep").addCol("name"))

            .where(new WhereClause()

            .andCompare("dep", "id", ">")

            .andCompare("art", "id", "<")

            .orCompare("art", "id", ">=", "dep", "id")

            .orCompare("dep", "name", "<=")

            .orCompare("dep", "name", "!=")

            .orCompare("dep", "name", "=", "art", "id")

            );
            String s = sel.toString();
            assertEquals(
                    s,
                    "select art.\"NAME\" as art_name,dep.\"NAME\" as dep_name from( articles art inner join department dep on art.\"ID_DEPARTAMENT\" = dep.\"ID\" ) where ( dep.\"ID\" > ? and art.\"ID\" < ? or art.\"ID\" >= dep.\"ID\" or dep.\"NAME\" <= ? or dep.\"NAME\" != ? or dep.\"NAME\" = art.\"ID\" )");

        }
        
        {   //before "wc.bindToContext(this)" execute onBuildWhere() for each IQTable
            WhereClause wc = new WhereClause();
            TestQTable tab = new TestQTable("", wc.toString());
            QSelect qSel = new QSelect().joinLast(tab);
            assertFalse(tab.m_bCalled);
            qSel.where(wc);
            assertTrue(tab.m_bCalled);
        }
        
        {//expr
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "art").addCol("name"))

            .joinLast(JoinType.INNER, "id_departament", "id",
                    new QTable("department", "dep").addCol("name"))

            .where(new WhereClause()

            .and( new Expr.Compare("dep", "id", "=", new Long(234))));
            assertEquals("select art.\"NAME\" as art_name,dep.\"NAME\" " +
            		"as dep_name from( articles art inner join department dep on art.\"ID_DEPARTAMENT\" = dep.\"ID\" ) where ( dep.\"ID\" = 234 )", sel.toString());
            
        }
        
        {   //if where is null, create empty where 
            TestQTable tab = new TestQTable("", "");
            QSelect qSel = new QSelect().joinLast(tab);
            assertFalse(tab.m_bCalled);
            qSel.toString();
            assertTrue(tab.m_bCalled);
        }

    }
    
    private static class TestQTable extends QTable{
        boolean m_bCalled=false;
        private String m_wc;
        public TestQTable(String name, String wc) {
            super(name);
            m_wc = wc;
        }
        @Override
        public void onBuildWhere(WhereClause wc) {
            m_bCalled = true;
            assertEquals(m_wc, wc.toString());
        }
    }

    public void testJoinType() {
        assertEquals(JoinType.INNER.toString(), "inner join");
        assertEquals(JoinType.LEFT_OUTER.toString(), "left outer join");
    }

	public void testExceptions() {

        {// same column
            QTable qt1 = new QTable("mytable", "t1");
            qt1.addCol("c1");

            try {
                qt1.addCol("c1");
                fail();
            } catch (Err.EColAlreadyExists e) {
                assertEquals(e.getMessage(), "c1");
            }
            qt1.addCol("c2");
        }

        { // same prefix
            QSelect sel = new QSelect();

            // add first table
            QTable qt1 = new QTable("mytable", "t1");
            qt1.addCol("c1");
            sel.joinLast(qt1);

            // same prefix
            try {
                QTable qt2wrongPref = new QTable("mytable2", "t1");
                sel.joinLast(qt2wrongPref);
                fail();
            } catch (Err.EPrefixAlreadyExists e) {
                assertEquals(e.getMessage(), "t1");
            }
        }

        // prefix not found
        {
            try {
                new QSelect().joinLast(
                        new QTable("articles", "art").addCol("name"))

                .joinLast(JoinType.INNER, "id_departament", "id",
                        new QTable("department", "dep").addCol("name"))

                .joinByPrefix(JoinType.INNER, "WRONG_PREF", "id_food_group",
                        "id", new QTable("food_group", "grp").addCol("name"));

                fail();
            } catch (Err.EPrefixNotFound e) {
                assertEquals(e.getMessage(), "WRONG_PREF");
            }

        }
        {// ..test description goes here...
            try {
                new QSelect().joinLast(new QTable("articles", "art").addCol("name"))
                .where(new WhereClause()
                
                .andCompare("art", "name", ">>")
                
                );
                fail();
            } catch (EUnkonwnEqKind e_) {
                assertEquals(">>", e_.getMessage());
            }
        }

    }

    public void testSingle() {
        // no prefix
        {
            QSelect sel = new QSelect().joinLast(new QTable("table1")
                    .addCol("col1"));
            String s = sel.toString();
            assertEquals(s, "select \"COL1\" from table1");
        }
        // prefix
        {
            QSelect sel = new QSelect().joinLast(new QTable("articles", "art")
                    .addCol("name"));
            String s = sel.toString();
            assertEquals(s, "select art.\"NAME\" as art_name from articles art");
        }
    }

    public void testFew() {
        // articles/department/group
        {
            QSelect sel = new QSelect()

            .joinLast(new QTable("articles", "art").addCol("name"))

            .joinLast(JoinType.INNER, "id_departament", "id",
                    new QTable("department", "dep").addCol("name"))

            .joinLast(JoinType.INNER, "id_food_group", "id",
                    new QTable("food_group", "grp").addCol("name"));

            String s = sel.toString();
            assertEquals(
                    s,
                    "select art.\"NAME\" as art_name,dep.\"NAME\" as dep_name,grp.\"NAME\" as grp_name from" +
                    "(( articles art inner join department dep on art.\"ID_DEPARTAMENT\" = dep.\"ID\" ) " +
                    "inner join food_group grp on dep.\"ID_FOOD_GROUP\" = grp.\"ID\" )");
        }
        // bookkeeping/food_group/payments
        {
            QSelect sel = new QSelect().joinLast(
                    new QTable("bookkeeping", "b").addCol("name"))

            .joinLast(JoinType.LEFT_OUTER, "id", "id_bookkp_turnover",
                    new QTable("food_group", "fg").addCol("name"))

            .joinByPrefix(JoinType.LEFT_OUTER, "b", "id", "id_bookkp",
                    new QTable("payments", "p").addCol("name"));
            String s = sel.toString();
            assertEquals(
                    s,
                    "select b.\"NAME\" as b_name,fg.\"NAME\" as fg_name,p.\"NAME\" as p_name from" +
                    "(( bookkeeping b left outer join food_group fg on b.\"ID\" = fg.\"ID_BOOKKP_TURNOVER\" ) " +
                    "left outer join payments p on b.\"ID\" = p.\"ID_BOOKKP\" )");
        }
    }
}
