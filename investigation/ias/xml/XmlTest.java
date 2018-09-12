/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package ias.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

import com.triniforce.db.test.TFTestCase;

public class XmlTest extends TFTestCase {

	@Override
	public void test() throws Exception {

		
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//		System.out.println("http://xml.org/sax/features/xml-1.1: " + dbf.getFeature("http://xml.org/sax/features/xml-1.1"));
////		dbf.setFeature("http://xml.org/sax/features/unicode-normalization-checking", false);
//		dbf.setFeature("http://xml.org/sax/features/xml-1.1", true);
//		dbf.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		System.out.println("http://xml.org/sax/features/xml-1.1: " + spf.getFeature("http://xml.org/sax/features/xml-1.1"));
		System.out.println("http://xml.org/sax/features/unicode-normalization-checking : "  + spf.getFeature("http://xml.org/sax/features/unicode-normalization-checking"));
//		spf.setFeature("http://xml.org/sax/features/unicode-normalization-checking", true);
		
		SAXParser sp = spf.newSAXParser();
		sp.parse(getClass().getResourceAsStream("xml11.res"), new DefaultHandler());
		
		DocumentBuilder db = dbf.newDocumentBuilder();
	
		Document res = db.parse(getClass().getResourceAsStream("xml11.res"));
		assertNotNull(res);
		
//		XPath xp = XPathFactory.newInstance().newXPath();
//		xp.evaluate("", source)
		
		
		TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory
                .newTransformer();
        transformer.setOutputProperty(
                OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.1");
        transformer
                .setOutputProperty(
                        "{http://xml.apache.org/xslt}indent-amount",
                        "2");


        DOMSource source = new DOMSource(res);
        StreamResult result = new StreamResult(System.out);
        transformer.transform(source, result);
	}
}
