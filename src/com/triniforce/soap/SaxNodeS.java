/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import com.triniforce.soap.InterfaceDescriptionGenerator.IConverter;
import com.triniforce.soap.InterfaceDescriptionGenerator.Node_S;
import com.triniforce.soap.TypeDef.ScalarDef;
import com.triniforce.utils.ApiAlgs;

public class SaxNodeS implements Node_S{

	private Writer m_writer;
	private String m_name;
	private SaxNodeS m_parent;
	private boolean m_bStartTerminated = false;
	private List<String[]> m_attrs = new ArrayList<>();

	public SaxNodeS(SaxNodeS parent, String name, Writer w) {
		m_writer = w;
		m_name = name;
		m_parent = parent;
    }

	public SaxNodeS append(String name) {
		terminateStart(false);
		SaxNodeS res = new SaxNodeS(this, name, m_writer);
		return res;
	}

	private void terminateStart(boolean bEnd) {
		try {
			if(m_bStartTerminated){
				if(bEnd)
					m_writer.append(String.format("</%s>", m_name));
			}
			else{
				String s = m_attrs.stream().map(e -> String.format("%s=\"%s\" ", 
						e[0], StringEscapeUtils.escapeXml10(e[1]))).reduce("", String::concat);
				m_writer.append(String.format("<%s%s%s>", m_name, m_attrs.isEmpty() ? "" : " "+s, bEnd ? "/" : ""));
				m_bStartTerminated = true;
			}
		} catch (IOException e) {
			ApiAlgs.rethrowException(e);
		}
	}

	public <T> SaxNodeS append(Collection<T> col, IConverter<T> conv) {
		for (T v : col) {
			conv.run(this, v);
		}
		return this;
	}

	public <T> SaxNodeS append(T val, IConverter<T> conv) {
		conv.run(this, val);
		return this;
	}

	public SaxNodeS attr(String name, String v) {
		m_attrs.add(new String[]{name, v});
		return this;
	}

	public SaxNodeS end() {
		terminateStart(true);
		return m_parent;
	}

	public SaxNodeS text(String string) {
		terminateStart(false);
		try {
			m_writer.append(string);
		} catch (IOException e) {
			ApiAlgs.rethrowException(e);
		}
		return this;
	}
	
	public Node_S textContent(ScalarDef typeDef, Object object) {
		terminateStart(false);
		typeDef.serialize(object, m_writer);
		return this;
	}

}
