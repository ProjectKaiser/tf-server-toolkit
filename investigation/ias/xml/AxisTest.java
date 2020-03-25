/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias.xml;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.triniforce.db.test.TFTestCase;

public class AxisTest extends TFTestCase {

	@Override
	public void test() throws Exception {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element e1 = doc.createElement("e1");
		e1.setTextContent("\u0002HI ALEX");
		doc.appendChild(e1);
		Transformer tr = TransformerFactory.newInstance().newTransformer();
		tr.transform(new DOMSource(doc), new StreamResult(System.out));
	}
}
