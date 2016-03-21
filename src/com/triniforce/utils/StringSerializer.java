/*
 *
 * (c) Triniforce
 *
 */
package com.triniforce.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class StringSerializer {

	public static interface IAfterDeserialization{
		void afterDeserialization();
	}
	
    public static final String PREFIX_BASE64 = "base64";
    public static final String PREFIX_JSON = "pojoja";
    public static final int prefixLength = 6;

    public interface ISerializer {
        public Object string2Object(String data);

        public String getPrefix();

        public String object2String(Object data);
    }

    public static class PojojaSerializer implements ISerializer {
        
        public String getPrefix() {
            return PREFIX_JSON;
        }

        public String object2String(Object data) {
            return new PKJsonSerializer().serialize(data).toString();
        }

        public Object string2Object(String data) {
            return new PKJsonSerializer().deserialize(data);
        }
    }
    
    public static class Base64Serializer implements ISerializer {
        public Object string2Object(String data) {
            Object res = null;
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(data
                        .getBytes());
                Base64.InputStream in64 = new Base64.InputStream(bis);
                ObjectInputStream ois = new ObjectInputStream(in64);
                res = ois.readObject();
                ois.close();
            } catch (Exception e) {
                ApiAlgs.rethrowException(e);
            }
            return res;
        }

        public String getPrefix() {
            return PREFIX_BASE64;
        }

        public String object2String(Object data) {
            String res = null;
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                OutputStream out64 = new Base64.OutputStream(bos);
                ObjectOutputStream oos = new ObjectOutputStream(out64);
                oos.writeObject(data);
                oos.close();
                res = bos.toString();
            } catch (Exception e) {
                ApiAlgs.rethrowException(e);
            }
            return res;

        }
    }

    protected static Map<String, ISerializer> m_serializers = new HashMap<String, ISerializer>();
    protected static ISerializer m_defSerializer;
    static {
        {
            ISerializer ser = new Base64Serializer();
            m_serializers.put(ser.getPrefix(), ser);
            m_defSerializer = ser;
        }
        {
            ISerializer ser = new PojojaSerializer();
            m_serializers.put(ser.getPrefix(), ser);
        }
    }

    public static String object2String(Object data, ISerializer ser) {
        return ser.getPrefix().substring(0, prefixLength)
                + ser.object2String(data);
    }
    
    public static String Object2JSON(Object data){
    	return rawObject2String(data, PREFIX_JSON);
    }
    
    public static Object JSON2Object(String data){
    	Object res = rawString2Object(data, PREFIX_JSON);
    	if( res instanceof IAfterDeserialization){
    		((IAfterDeserialization) res).afterDeserialization();
    	}
    	return res;
    }
    
    public static String rawObject2String(Object data, String serKey) {
        ISerializer ser = m_serializers.get(serKey);
        TFUtils.assertNotNull(ser, "Wrong serKey " + serKey);
        return ser.object2String(data);
    }
    
    public static Object rawString2Object(String data, String serKey) {
        ISerializer ser = m_serializers.get(serKey);
        TFUtils.assertNotNull(ser, "Wrong serKey " + serKey);
        return ser.string2Object(data);
    }
    
    public static String objectToString(Object data, String serKey) {
        ISerializer ser = m_serializers.get(serKey);
        TFUtils.assertNotNull(ser, "Wrong serKey " + serKey);
        return object2String(data, ser);
    }
    
    public static String object2String(Serializable data, String serKey) {
        return objectToString(data, serKey);
    }
    public static String object2String(IPropSerializabe data, String serKey) {
        return objectToString(data, serKey);
    }

    public static String object2String(Serializable data) {
        return object2String(data, m_defSerializer);
    }

    public static Object string2Object(String str) {
        if (null == str)
            return null;
        if (str.length() < prefixLength)
            return null;
        String prefix = str.substring(0, prefixLength);
        ISerializer ser = m_serializers.get(prefix);
        if (null != ser) {
            return ser.string2Object(str.substring(prefixLength));
        }
        TFUtils.assertTrue(false, "Unknown prefix:" + prefix);//$NON-NLS-1$
        return null;
    }
}
