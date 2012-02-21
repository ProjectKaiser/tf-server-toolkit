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
import java.util.ArrayList;

public class StringSerializer {
   
    public static final String PREFIX_BASE64="base64";
    public static final int prefixLength = 6;
    
    public interface ISerializer{
        public Object string2Object(String data);
        public String getPrefix();
        public String object2String(Serializable data);
    }
    

    public static class Base64Serializer implements ISerializer{
       
        
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

        public String object2String(Serializable data) {
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
    
    protected static ArrayList<ISerializer> m_serializers = new ArrayList<ISerializer>();
    static{
        m_serializers.add(new Base64Serializer());
    }
    
    
    public static String object2String(Serializable data, ISerializer ser ){
        return ser.getPrefix().substring(0, prefixLength)+ser.object2String(data);
    }    
    public static String object2String(Serializable data){
        if( null == data )return null;
        return object2String(data, m_serializers.get(0));
    }
    public static Object string2Object(String str){
        if( null == str) return null;
        if( str.length() < prefixLength) return null;
        String prefix = str.substring(0, prefixLength);
        for( ISerializer ser: m_serializers){
            if(prefix.equals(ser.getPrefix())){
                return ser.string2Object(str.substring(prefixLength));
            }
        }
        ApiAlgs.assertTrue(false, "Unknown prefix:"+prefix);//$NON-NLS-1$
        return null;
    }
}
