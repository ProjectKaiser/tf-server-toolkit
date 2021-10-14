/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.soap;

import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.triniforce.soap.InterfaceDescriptionGenerator.IConverter;
import com.triniforce.soap.InterfaceDescriptionGenerator.Node_S;
import com.triniforce.soap.TypeDef.ScalarDef;

public class DomNode_S implements Node_S{
    private Node_S m_parent;
    private Element m_element;
    public DomNode_S(Element element, Node_S parent) {
        m_element = element;
        m_parent = parent;
    }
    
    public Node_S append(String name){
        Element e = getDocument().createElement(name);
        m_element.appendChild(e);
        Node_S res = new DomNode_S(e, this);
        return res;
    }
    
    public <T> Node_S append (Collection<T> col, IConverter<T> conv){
        for (T v: col) {            	
            conv.run(this, v);
        }
        return this;    
    }
    
    public <T> Node_S append (T val, IConverter<T> conv){
        conv.run(this, val);
        return this;    
    }
    
    public Node_S attr(String name, String v){
        m_element.setAttribute(name, v);
        return this;
    }
    public Node_S end(){
        return m_parent;
    }

    Document getDocument(){
        return m_element.getOwnerDocument();
    }
    
	public Node_S textContent(ScalarDef typeDef, Object object) {
		return text(typeDef.stringValue(object));
	}

	public Node_S text(String string) {
        m_element.setTextContent(string);
        return this;
	}

	@Override
	public List<Node_S> getChildNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}
