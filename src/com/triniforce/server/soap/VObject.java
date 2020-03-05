/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.server.soap;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.triniforce.soap.PropertiesSequence;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.xml.tfserver._200701.serialization.DOMSerializer;

@PropertiesSequence( sequence = {"type", "props"})
public class VObject {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Persist {
    }

    public interface IVOSerializable {
        public void fromVObject(VObject vObj);

        /**
         * Why not toVObject(VObject obj) ? Because name of object must be
         * stored into VObject, this name is passed in VObject constructor in
         * implementor.toVObject()
         */
        public VObject toVObject();
    }

    public static class EWrongPropType extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EWrongPropType(Object obj, String propName, Object propValue) {
            super(MessageFormat.format(
                    "Property ''{0}'' for class ''{1}'' can not have type ''{2}''",
                    propName, obj.getClass().getName(), propValue.getClass()
                            .getName()));
        }
    }

    public static class EPropNotFound extends RuntimeException {
        private static final long serialVersionUID = 9211799853560335706L;

        public EPropNotFound(String name) {
            super(name);
        }
    }

    private static final HashMap<String, Class<? extends IVOSerializable>> REGISTERED_CLASSES = new HashMap<String, Class<? extends IVOSerializable>>();
    static {

    }

    private String m_type;

    private Map<String, Object> m_props;

    public VObject(String type, NamedVar[] vars) {
        m_type = type;
        m_props = new HashMap<String, Object>();
        if (vars != null) {
            for (NamedVar var : vars) {
                m_props.put(var.getName(), var.getValue());
            }
        }
    }

    public VObject(Object obj) {
        this(obj.getClass().getName(), null);
    }

    public VObject(Class cls) {
        this(cls.getName(), null);
    }

    public String getType() {
        return m_type;
    }
    public void setType(String type) {
        m_type = type;
    }
    
    public Object queryProp(String name) {
        return m_props.get(name);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProp(String name) {
        Object res = queryProp(name);
        if (null == res) {
            if (!m_props.containsKey(name))
                throw new EPropNotFound(name);
        }
        return (T) res;
    }

    public void setProp(String name, Object value) {
    	if(value instanceof IVOSerializable)
    		value = ((IVOSerializable)value).toVObject();
    	if(value instanceof Object[])
    		value = convertArray((Object[]) value);
    		
        m_props.put(name, value);
    }

    private Object[] convertArray(Object[] value) {
    	Object[] res = new Object[value.length];
        for (int i = 0; i < res.length; i++) {
            Object obj = value[i];
            if (obj instanceof IVOSerializable) {
                res[i] = ((IVOSerializable) obj).toVObject();
            } else {
                res[i] = obj;
            }
        }
        return res;
	}

	public void setListProp(String name, List value) {
        Object res[] = new Object[value.size()];
        for (int i = 0; i < res.length; i++) {
            Object obj = value.get(i);
            if (obj instanceof IVOSerializable) {
                res[i] = ((IVOSerializable) obj).toVObject();
            } else {
                res[i] = obj;
            }
        }
        m_props.put(name, res);
    }

    public Map<String, Object> getProps() {
        return m_props;
    }
    public void setProps(Map<String, Object> props) {
        m_props = props;
    }

    private java.lang.Object __equalsCalc = null;

    public static boolean bothNotNull(Object o1, Object o2) {
        return o1 != null && o2 != null;
    }

    public static boolean bothNull(Object o1, Object o2) {
        return o1 == null && o2 == null;
    }

    public static boolean compareProps(VObject vo1, VObject vo2) {

        Map<String, Object> props1 = vo1.getProps(), props2 = vo2.getProps();

        if (bothNull(props1, props2))
            return true;
        if (!bothNotNull(props1, props2))
            return false;

        if (props1.size() != props2.size())
            return false;
        for (Entry e : props1.entrySet()) {
            Object key = e.getKey();
            Object v1 = e.getValue();
            if (v1 == null) {
                if (!(props2.get(key) == null && props2.containsKey(key)))
                    return false;
            } else {
                Object v2 = props2.get(key);
                if (!bothNotNull(v1, v2))
                    return false;
                if (v1 instanceof Object[]) {
                    if (!(v2 instanceof Object[]))
                        return false;
                    Object arr1[] = (Object[]) v1;
                    Object arr2[] = (Object[]) v2;
                    if (arr1.length != arr2.length)
                        return false;
                    for (int i = 0; i < arr1.length; i++) {
                        if (!arr1[i].equals(arr2[i]))
                            return false;
                    }
                } else {
                    if (!v1.equals(v2))
                        return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof VObject))
            return false;
        VObject other = (VObject) obj;
        if (this == obj)
            return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true
                && ((this.m_type == null && other.getType() == null) || (this.m_type != null && this.m_type
                        .equals(other.getType())))
                && (VObject.compareProps(this, other));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;

    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getType() != null) {
            _hashCode += getType().hashCode();
        }
        if (getProps() != null) {
            _hashCode += getProps().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    public String propsToString() {
        ArrayList<String> names = new ArrayList<String>(getProps().keySet());
        Collections.sort(names);
        String res = "{";//$NON-NLS-1$
        int cnt = 0;
        for (String name : names) {
            if (cnt++ > 0) {
                res = res + ", ";//$NON-NLS-1$
            }
            Object prop = getProps().get(name);
            if (prop instanceof Object[]) {
                res = res + MessageFormat.format("{0}:[", name);//$NON-NLS-1$
                Object arr[] = (Object[]) prop;
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) {
                        res = res + ", ";//$NON-NLS-1$
                    }
                    res = res + arr[i].toString();
                }
                res = res + "]";//$NON-NLS-1$
            } else {
                res = res
                        + MessageFormat.format(
                                "{0}:{1}", name, getProps().get(name));//$NON-NLS-1$
            }
        }
        res = res + "}";//$NON-NLS-1$
        return res;
    }

    @Override
    public String toString() {
        return MessageFormat.format("<{0}>:{1}", getType(), propsToString());
    }

    public void saveToStream(OutputStream out) {
        synchronized (DOC_BUILDER) {
            try {
                DOMSerializer srz = new DOMSerializer();
                Document doc = DOC_BUILDER.newDocument();
                Element e = srz.fillVariantElement(doc, this);
                TransformerFactory tf = DOMSerializer.TRANSFORMER_FACTORY;
                Transformer t = tf.newTransformer();
                t.setOutputProperty("indent", "yes");
                t.transform(new DOMSource(e), new StreamResult(out));
            } catch (Exception e) {
                ApiAlgs.rethrowException(e);
            }
        }
    }

    static DocumentBuilder DOC_BUILDER;

    static {
        DOMSerializer.DOCBUILDER_FACTORY.setNamespaceAware(true);
        try {
            DOC_BUILDER = DOMSerializer.DOCBUILDER_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static VObject loadFromStream(InputStream in) {
        synchronized (DOC_BUILDER) {
            try {
                Document doc = DOC_BUILDER.parse(new InputSource(in));
                DOMSerializer srz = new DOMSerializer();
                VObject res = srz.createVObject(doc.getDocumentElement());
                return res;
            } catch (Exception e) {
                ApiAlgs.rethrowException(e);
            }
            return null;
        }
    }

    public static void registerClass(Class<? extends IVOSerializable> cls) {
        REGISTERED_CLASSES.put(cls.getName(), cls);
    }

    @SuppressWarnings("unchecked")
    public static <T> void fillArrayList(List<T> al, Object objs[]) {
        for (int i = 0; i < objs.length; i++) {
            Object obj = objs[i];
            if (obj instanceof VObject) {
                al.add((T) VObject.getObject((VObject) obj));
            } else {
                al.add((T) obj);
            }
        }
    }

    public <T> void getListProp(List<T> al, String propName) {
        VObject.fillArrayList(al, (Object[]) getProp(propName));
    }

    public static class EClassNotFound extends RuntimeException {
        private static final long serialVersionUID = 5955569469617846314L;

        public EClassNotFound(String msg) {
            super(msg);
        }
    }

    /**
     * @return List of field names annotated with
     * @Persist interface. Each field marked with this interface has to be in
     *          'f_' or 'm_' form
     */
    public static ArrayList<String> getFieldNames(Class cls) {
        ArrayList<String> res = new ArrayList<String>();
        for (Field m : cls.getDeclaredFields()) {
            if (m.isAnnotationPresent(Persist.class)) {
                res.add(m.getName());
            }
        }
        return res;
    }

    /**
     * 
     * @return List of method names, with no get/set prefix. Only only one value
     *         for each pair set/get is returned.
     * 
     * <p>
     * Method names are calculated from fields annotated with
     * @Persist interface. E.g. m_name, m_idx fields give {"Name", "Idx"} list.
     */
    public static ArrayList<String> getPropMethodNames(Class cls) {
        ArrayList<String> res = new ArrayList<String>();
        for (String name : getFieldNames(cls)) {
            res.add(name.substring(2, 3).toUpperCase(Locale.ENGLISH) + name.substring(3));
        }
        return res;
    }

    public static IVOSerializable getObject(VObject vObj) {
        Class<? extends IVOSerializable> cls = REGISTERED_CLASSES.get(vObj
                .getType());
        if (null == cls)
            throw new EClassNotFound(vObj.getType());

        IVOSerializable res;
        try {
            IVOSerializable instance = cls.newInstance();
            res = instance;
            res.fromVObject(vObj);
            return res;
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return null;
    }

}
