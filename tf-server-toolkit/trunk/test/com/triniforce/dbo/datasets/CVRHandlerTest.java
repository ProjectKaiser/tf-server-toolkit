/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.triniforce.db.dml.IResSet;
import com.triniforce.db.test.TFTestCase;
import com.triniforce.dbo.datasets.EDSException.ECVRColumnException.EColumnRequestedTwice;
import com.triniforce.dbo.datasets.EDSException.ECVRColumnException.EWrongColumnName;
import com.triniforce.dbo.datasets.EDSException.ECVRColumnException.EWrongFieldFunctionName;
import com.triniforce.dbo.datasets.EDSException.ECVRColumnException.EWrongNameInOrderClause;
import com.triniforce.dbo.datasets.EDSException.ECVRColumnException.EWrongNameInWhereClause;
import com.triniforce.dbo.datasets.ICVRHandler.RSFlags;
import com.triniforce.extensions.PKRootExtensionPoint;
import com.triniforce.server.soap.CollectionViewRequest;
import com.triniforce.server.soap.FieldFunctionRequest;
import com.triniforce.server.soap.LongListResponse;
import com.triniforce.server.soap.WhereExpr;
import com.triniforce.server.srvapi.IBasicServer;
import com.triniforce.utils.Api;
import com.triniforce.utils.ApiStack;

public class CVRHandlerTest extends TFTestCase {

	private Api api;
	private PKRootExtensionPoint rootPoint;
	private PKEPDatasetProviders provsPoint;

	public void testFilter() {
		List<WhereExpr> where = new ArrayList<WhereExpr>();
		ArrayList<FieldFunctionRequest> ffs = new ArrayList<FieldFunctionRequest>();
		ArrayList<FieldFunction> ff = new ArrayList<FieldFunction>();
		CVRHandler h = new CVRHandler();
		IResSet res = h.filter(getRS(), where, ffs,ff);
		
		assertTrue(res.next());
		assertEquals(1, res.getObject(1));
		assertTrue(res.next());
		assertEquals("string_11", res.getObject(2));

		where.add(new WhereExpr.ExprEquals("idx", 3));
//		where.put("idx", 3);
		res = h.filter(getRS(), where, ffs,ff);
		assertTrue(res.next());
		assertEquals("string_04", res.getObject(2));

		where.clear();
		where.add(new WhereExpr.ExprEquals("idx", 97));
//		where.put("idx", 97);
		res = h.filter(getRS(), where, ffs,ff);
		assertFalse(res.next());

		
		where.clear();
		where.add(new WhereExpr.ExprEquals("name", "string_05"));
		res = h.filter(getRS(), where, ffs,ff);
		assertTrue(res.next());
		assertEquals(4, res.getObject(1));
		
		where.clear();
		where.add(new WhereExpr.ExprEquals("name", "string_06"));
		where.add(new WhereExpr.ExprEquals("price", 59.99));
		res = h.filter(getRS(), where, ffs,ff);
		assertTrue(res.next());
		assertEquals(5, res.getObject(1));
		assertTrue(res.next());
		assertEquals(7, res.getObject(1));
		assertFalse(res.next());
		
		
		where.clear();
		where.add(new WhereExpr.ExprEquals("sale", "50%"));
		FieldFunctionRequest ffr = new FieldFunctionRequest();
		ffr.setFieldName("price");
		ffr.setFunctionName(TestFunSale.class.getName());
		ffr.setResultName("sale");
		ffs.add(ffr);
		ff.add(new TestFunSale());
		res = h.filter(getRS(), where, ffs, ff);
		int idx = res.getColumns().indexOf("sale");
		assertTrue(idx>=0);
		res.next();
		assertEquals("50%", res.getObject(idx+1));
		assertEquals(1, res.getObject(1));
		res.next();
		assertEquals("50%", res.getObject(idx+1));
		assertEquals(5, res.getObject(1));
		assertTrue(res.next());
		assertFalse(res.next());			
	}
	
	public static class TestFunSale extends FieldFunction{
		@Override
		public Object exec(Object value) {
			return (Double)value > 50  ? "50%" : "10%";
		}
	}

	static int counter = 0;
	public static class TestFunCounter extends FieldFunction{
		@Override
		public Object exec(Object value) {
			return counter++;
		}
	}
	
	static Object testSrc[][] = {
			{1, "string_01", 99.99}, 
			{2, "string_11",  9.99},
			{3, "string_04", 19.99},
			{4, "string_05", 29.99},
			{5, "string_06", 59.99},
			{6, "string_06", 29.99},
			{7, "string_06", 59.99}};

	public static  IResSet getRS() {
		return new IResSet(){
			int row = -1;
			
			public Object getObject(int i) {
				return testSrc[row][i-1];
			}
			public boolean next() {
				if(row == testSrc.length)
					return false;
				row ++;
				return row != testSrc.length;
			}

			public List<String> getColumns() {
				return Arrays.asList("idx", "name", "price");
			}
			
		};
	}

	public void testFilterAndSend() {
		ArrayList<WhereExpr> where = new ArrayList<WhereExpr>();
		ArrayList<FieldFunctionRequest> ffs = new ArrayList<FieldFunctionRequest>();
		ArrayList<FieldFunction> ff = new ArrayList<FieldFunction>();
		ArrayList<String> fields = new ArrayList<String>();
		fields.addAll(Arrays.asList("idx", "name", "price"));
		CVRHandler h = new CVRHandler();
		{
			IResSet res = h.filterAndSend(getRS(), where, ffs, ff, fields,0,0);
			assertTrue(res.next());
			assertEquals(Arrays.asList("idx", "name", "price"), res.getColumns());
		}
		{
			fields.clear();
			fields.addAll(Arrays.asList("name", "price"));
			IResSet res = h.filterAndSend(getRS(), where, ffs, ff, fields,0,0);
			assertEquals(Arrays.asList("name", "price"), res.getColumns());
			assertTrue(res.next());
		}
		{
			IResSet res = h.filterAndSend(getRS(), where, ffs, ff, fields,2,3);
			assertTrue(res.next());
			assertEquals(19.99, res.getObject(2));
			assertFalse(res.next());
		}
		{
			ffs.add(new FieldFunctionRequest("price", TestFunSale.class.getName(), "sale"));
			ff.add(new TestFunSale());
			fields.clear();
			fields.addAll(Arrays.asList("sale"));
			IResSet res = h.filterAndSend(getRS(), where, ffs, ff, fields,0,0);
			assertTrue(res.next());
			assertEquals("50%", res.getObject(1));
		}
		
		{
			where.add(new WhereExpr.ExprEquals("sale", "10%"));
			IResSet res = h.filterAndSend(getRS(), where, ffs, ff, fields,0,0);
			assertTrue(res.next());
			assertEquals("10%", res.getObject(1));
		}
	}

	public void testOrder() {
		ArrayList<FieldFunctionRequest> ffs = new ArrayList<FieldFunctionRequest>();
		ArrayList<FieldFunction> ff = new ArrayList<FieldFunction>();
		ArrayList<Object> order = new ArrayList<Object>();
		CVRHandler h = new CVRHandler();
		
		{
			IResSet res = h.order(getRS(), order, ffs, ff);
			assertTrue(res.next());
		}
		{
			order.add("name");
			IResSet res = h.order(getRS(), order, ffs, ff);
			assertTrue(res.next());
			assertEquals("string_01", res.getObject(2));
			assertTrue(res.next());
			assertEquals("string_04", res.getObject(2));
		}
		{
			order.add("price");
			h.order(getRS(), order, ffs, ff);
		}
		{
			order.clear();
			order.addAll(Arrays.asList("sale", "name"));
			ffs.add(new FieldFunctionRequest("price", TestFunSale.class.getName(), "sale"));
			ff.add(new TestFunSale());
			IResSet res = h.order(getRS(), order, ffs, ff);
			
			res.next();
			assertEquals("10%", res.getObject(4));
			assertEquals("string_04", res.getObject(2));
		}
		{
			ffs.clear();
			ff.clear();
			order.clear();
			order.addAll(Arrays.asList("sale", "name"));
			ffs.add(new FieldFunctionRequest("price", TestFunSale.class.getName(), "sale"));
			ff.add(new TestFunSale());
			ffs.add(new FieldFunctionRequest("idx", TestFunCounter.class.getName(), "num"));
			ff.add(new TestFunCounter());
			IResSet res = h.order(getRS(), order, ffs, ff);
			assertFalse(res.getColumns().toString(), res.getColumns().contains("num"));

		}
	}

	public void testSend() {
		ArrayList<FieldFunctionRequest> ffs = new ArrayList<FieldFunctionRequest>();
		ArrayList<FieldFunction> ff = new ArrayList<FieldFunction>();
		ArrayList<String> fields = new ArrayList<String>();
		CVRHandler h = new CVRHandler();
		{
			IResSet res = h.send(getRS(), fields, ffs, ff, 0, 0);
			assertEquals(Collections.EMPTY_LIST, res.getColumns());
		}
		{
			fields.add("price");
			fields.add("name");
			IResSet res = h.send(getRS(), fields, ffs, ff, 0, 0);
			assertEquals(Arrays.asList("price", "name"), res.getColumns());
		}
		{
			IResSet res = h.send(getRS(), fields, ffs, ff, 2, 3);
			assertTrue(res.next());
			assertEquals("string_04", res.getObject(2));
			assertFalse(res.next());
		}
		{
			IResSet res = h.send(getRS(), fields, ffs, ff, 2, 0);
			assertTrue(res.next());
			assertEquals("string_04", res.getObject(2));
			assertTrue(res.next());
			assertEquals("string_05", res.getObject(2));
			assertTrue(res.next());
			
		}
		{
			ffs.add(new FieldFunctionRequest("price", TestFunSale.class.getName(), "sale"));
			ff.add(new TestFunSale());
			IResSet res = h.send(getRS(), fields, ffs, ff, 0, 0);
			assertEquals(Arrays.asList("price", "name"), res.getColumns());
		}
		{	
			fields.add("sale");
			IResSet res = h.send(getRS(), fields, ffs, ff, 0, 0);
			res.next();
			assertEquals("50%", res.getObject(3));
		}
		{
			// Default name for FF.result
			fields.clear();
			fields.addAll(Arrays.asList("name", "com.triniforce.dbo.datasets.CVRHandlerTest$TestFunSale(price)"));
			ffs.clear();
			ff.clear();
			FieldFunctionRequest ffreq = new FieldFunctionRequest();
			ffreq.setFunctionName(TestFunSale.class.getName());
			ffreq.setFieldName("price");
			ffs.add(ffreq);
			ff.add(new TestFunSale());
			IResSet res = h.send(getRS(), fields, ffs, ff, 0, 0);
			res.next();
			assertEquals("50%", res.getObject(2));
			
		}
	}

	public void testProcess() {
//		assertTrue(Arrays.asList("s1","s2","s3").equals(Arrays.asList("s2", "s1","s3")));
	
		CVRHandler h = new CVRHandler();
		RSFlags flags = new ICVRHandler.RSFlags(0);
		List<FieldFunction> ffs = new ArrayList<FieldFunction>();
		
		{ // No changes 
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("idx", "name", "price"));
			IResSet src = getRS();
			IResSet res = h.process(src, flags, req, ffs);
			assertSame(src, res);
		}
		{ // No order - filterAndSend used
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("name", "price", "idx"));
			IResSet res = h.process(getRS(), flags, req,ffs);
			assertEquals(Arrays.asList("name", "price", "idx"), res.getColumns());
		}
		{ // Order dataset
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("idx", "name", "price"));
			req.getOrderBy().add("price");
			IResSet res = h.process(getRS(), flags, req,ffs);
			res.next();
			assertEquals(2, res.getObject(1));
		}
		{ //Filter
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("idx", "name", "price"));
			req.getWhere().put("idx", 5);
			IResSet res = h.process(getRS(), flags, req,ffs);
			assertTrue(res.next());
			assertEquals("string_06", res.getObject(2));
		}
		{//Add only FF needed for filter
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("idx", "name", "price"));

			req.getFunctions().add(new FieldFunctionRequest("price", TestFunCounter.class.getName(), "counter"));
			req.getFunctions().add(new FieldFunctionRequest("price", TestFunSale.class.getName(), "sale_2"));
			
			ffs.add(new TestFunCounter());
			ffs.add(new TestFunSale());
			
			
			IResSet res = h.process(getRS(), flags, req, ffs);
			assertEquals(Arrays.asList("idx", "name", "price", "counter", "sale_2"), res.getColumns());
			int i = 0;
			while(res.next()) i++;
			assertEquals(i, counter);
		}
		{// trunc by start/limit
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("idx", "name", "price"));
			req.setStartFrom(3);
			req.setLimit(1);
			IResSet res = h.process(getRS(), flags, req, ffs);
			assertTrue(res.next());
			assertEquals(4, res.getObject(1));
			assertFalse(res.next());
		}
		{// trunc by start
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("idx", "name", "price"));
			req.setStartFrom(3);
			req.setLimit(0);
			IResSet res = h.process(getRS(), flags, req, ffs);
			assertTrue(res.next());
			assertEquals(4, res.getObject(1));
			assertTrue(res.next());
			assertEquals(5, res.getObject(1));
		}
	}
	
	public void testCheckRequestMetadata(){
		CVRHandler h = new CVRHandler();
		DSMetadata meta = new DSMetadata(0, Arrays.asList("column_784", "column_785", "column_901", "column_902", "column_903"));
		
		{
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("column_784","column_785","column_784"));
			try{
				h.checkRequestMetadata(meta, req);
				fail();
			}catch(EColumnRequestedTwice e){
				assertTrue(e.getMessage().contains("column_784"));
			}
		}
		{
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("column_784","column_785","column_787","column_784"));
			try{
				h.checkRequestMetadata(meta, req);
				fail();
			}catch(EWrongColumnName e){
				assertTrue(e.getMessage().contains("column_787"));
			}
		}
		{
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("column_784","column_785"));
			req.getFunctions().add(new FieldFunctionRequest("column_789", "fun_01","result"));
			try{
				h.checkRequestMetadata(meta, req);
				fail();
			}catch(EWrongFieldFunctionName e){
				assertTrue(e.getMessage().contains("column_789"));
			}			
		}
		
		{
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("column_784","column_785"));
			req.getWhere().put("column_774", "");
			req.getFunctions().add(new FieldFunctionRequest("column_784", "fun_01","result"));
			try{
				h.checkRequestMetadata(meta, req);
				fail();
			}catch(EWrongNameInWhereClause e){
				assertTrue(e.getMessage().contains("column_774"));
			}			
		}
		{
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("column_784","column_785"));
			req.getWhereExprs().add(new WhereExpr.ExprBetween("column_231",1, 34));
			try{
				h.checkRequestMetadata(meta, req);
				fail();
			}catch(EWrongNameInWhereClause e){
				assertTrue(e.getMessage().contains("column_231"));
			}	
		}
		
		{
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("column_784","column_785"));
			req.getOrderBy().add("column_767");
			try{
				h.checkRequestMetadata(meta, req);
				fail();
			}catch(EWrongNameInOrderClause e){
				assertTrue(e.getMessage().contains("column_767"));
			}	
		}
		{
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("column_784","column_785"));
			req.getOrderBy().add(new CollectionViewRequest.DescField("column_711"));
			try{
				h.checkRequestMetadata(meta, req);
				fail();
			}catch(EWrongNameInOrderClause e){
				assertTrue(e.getMessage().contains("column_711"));
			}	
		}
		
		{
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("column_784","column_785", "column_903", "column_902"));
			req.getOrderBy().add(new CollectionViewRequest.DescField("column_901"));
			req.getWhere().put("column_903", 0);
			
			List<String> res = h.checkRequestMetadata(meta, req);
			assertEquals(Arrays.asList("column_784","column_785", "column_903", "column_902", "column_901"), res);
			
		}
	}
	
	static boolean MD_CHECKED = true;
	static boolean MD_CLOSED = true;
	
	static class MD extends DSMetadata{
		
		public MD() {
			super(FLAGS, COLUMNS);
			MD_CLOSED = false;
		}
		
		@Override
		public
		IResSet load(List<String>reqColumns, CollectionViewRequest req, List<FieldFunction>ffs){
			return getRS();
		}
		
		@Override
		public boolean check(CollectionViewRequest req) {
			return MD_CHECKED;
		}
		
		@Override
		public void close() {
			MD_CLOSED = true;
		}
		
	}
	
	static long FLAGS = 0;
	static List<String> COLUMNS = Arrays.asList("idx", "name", "price");
	
	public static class TestProvider extends PKEPDatasetProvider{
		@Override
		public
		DSMetadata queryTarget(String target) {
			if(target.equals("TestProvider_01"))
				return new MD();
			return null;
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		api = new Api();
		rootPoint = new PKRootExtensionPoint();
		provsPoint = new PKEPDatasetProviders();
		provsPoint.putExtension(TestProvider.class);
		rootPoint.putExtensionPoint(provsPoint);
		api.setIntfImplementor(IBasicServer.class, rootPoint);
		ApiStack.pushApi(api);
	}
	
	@Override
	protected void tearDown() throws Exception {
		ApiStack.popApi();
		super.tearDown();
	}
	
	public void testProcessRequest(){
			CVRHandler h = new CVRHandler();
			CollectionViewRequest req = new CollectionViewRequest();
			req.setColumns(Arrays.asList("idx","name"));
			{
				req.setTarget("TestProvider_01");
				LongListResponse res = h.processRequest(req);
				assertNotNull(res);
			}
			
			{
				// provider make own filtering
				FLAGS = DSMetadata.CAN_FILTER;
				req.getWhere().put("idx", 3);
				LongListResponse res = h.processRequest(req);
				assertEquals(1, res.values().get(0));
			}
			{		
				FLAGS = DSMetadata.CAN_SORT;
				req.getWhere().clear();
				req.getOrderBy().add(new CollectionViewRequest.DescField("name"));			
				LongListResponse res = h.processRequest(req);
				assertEquals(1, res.values().get(0));
				assertTrue(MD_CLOSED);
			}
			{
				MD_CHECKED = false;
				LongListResponse res = h.processRequest(req);
				assertTrue(res.values().isEmpty());
			}
	
		}

}
