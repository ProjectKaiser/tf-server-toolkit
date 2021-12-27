/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.StringWriter;

import com.triniforce.db.test.TFTestCase;
import com.triniforce.soap.InterfaceDescriptionGenerator.IConverter;
import com.triniforce.soap.InterfaceDescriptionGenerator.Node_S;
import com.triniforce.soap.TypeDef.ScalarDef;


public class SaxNodeSTest extends TFTestCase {

	public void testSaxNodeS() {
		StringWriter w = new StringWriter();
		SaxNodeS s = new SaxNodeS(null, "some", w);
		s.end();
		
		assertEquals("<some/>", w.getBuffer().toString());
	}

	public void testAppendString() {
		{
			StringWriter w = new StringWriter();
			SaxNodeS s = new SaxNodeS(null, "some", w);
			SaxNodeS r1;
			assertNotNull(r1 = s.append("child1"));
			r1.end().end();
			assertEquals("<some><child1/></some>", w.getBuffer().toString());
		}
		{
			StringWriter w = new StringWriter();
			SaxNodeS s = new SaxNodeS(null, "some", w);
			s.append("child1").end().append("child1").end().end();
			assertEquals("<some><child1/><child1/></some>", w.getBuffer().toString());
		}
	}

//	public void testAppendCollectionOfTIConverterOfT() {
//		fail("Not yet implemented");
//	}

	public void testAppendTIConverterOfT() {
		StringWriter w = new StringWriter();
		SaxNodeS s1 = new SaxNodeS(null, "some", w);
		s1.append(2342, new IConverter<Integer>(){
			@Override
			public void run(Node_S parent, Integer val) {
				parent.text(""+val);
			}}).end();
		assertEquals("<some>2342</some>", w.getBuffer().toString());
		
	}

	public void testAttrStringString() {
		StringWriter w = new StringWriter();
		SaxNodeS s = new SaxNodeS(null, "some", w);
		s.attr("prop1", "993");
		s.attr("prop2", "business$<Co>");
		s.end();
		assertEquals("<some prop1=\"993\" prop2=\"business$&lt;Co&gt;\" />", w.getBuffer().toString());
	}

	public void testEnd() {
		StringWriter w = new StringWriter();
		SaxNodeS s1 = new SaxNodeS(null, "some", w);
		SaxNodeS s2 = new SaxNodeS(s1, "some", w);
		assertSame(s1, s2.end());
	}

	public void testTextString() {
		{
			StringWriter w = new StringWriter();
			SaxNodeS s = new SaxNodeS(null, "some", w);
			s.text("content here!!!").end();
			assertEquals("<some>content here!!!</some>", w.getBuffer().toString());
		}
	}
	
	public void testTextContent(){
		{
			InterfaceDescription desc = new InterfaceDescription();
			
			TypeDef sd = new TypeDefLibCache(null, null).get(String.class);
			{
				StringWriter w = new StringWriter();
				SaxNodeS s = new SaxNodeS(null, "some", w);
				s.textContent((ScalarDef) sd, "<xml>\"bread\" & \"butter\"</xml>", desc).end();
				
				assertEquals("<some>&lt;xml&gt;&quot;bread&quot; &amp; &quot;butter&quot;&lt;/xml&gt;</some>", w.getBuffer().toString());
			}
			
			desc.setAvoidDoubleQuotesEscaping(true);
			{
				StringWriter w = new StringWriter();
				SaxNodeS s = new SaxNodeS(null, "some", w);
				s.textContent((ScalarDef) sd, "<xml>\"bread\" & \"butter\"</xml>", desc).end();
				
				assertEquals("<some>&lt;xml&gt;\"bread\" &amp; \"butter\"&lt;/xml&gt;</some>", w.getBuffer().toString());
			}
			
		}
	}

}
