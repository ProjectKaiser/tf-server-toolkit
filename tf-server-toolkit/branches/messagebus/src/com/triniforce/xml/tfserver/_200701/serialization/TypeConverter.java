/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.xml.tfserver._200701.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;
import com.triniforce.db.dml.IStmtContainer;

public class TypeConverter {

    public static final String CHARSET_NAME = "UTF-8"; //$NON-NLS-1$
    public static final int MAX_STRING_SIZE = 0x100000;
    public static final int READ_PORTION_SIZE = 0x10000;

    public static Object convertXmlToSql(Object xmlObject, ColumnType type) {
        
        Object result = xmlObject;
        if(type.equals(ColumnType.DATETIME)){
            if(!(xmlObject instanceof XMLGregorianCalendar)){
                return null;
            }
            XMLGregorianCalendar xmlDate = (XMLGregorianCalendar) xmlObject;            
            long time = xmlDate.toGregorianCalendar().getTimeInMillis();
            result = new Date(time);
        }
            
        return result;
    }

    public static Object convertSqlToXml(Object sql){
        if(sql instanceof java.util.Date){
            GregorianCalendar gregorian = new GregorianCalendar();
            gregorian.setTimeInMillis(((java.util.Date) sql).getTime());
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorian);
            } catch (DatatypeConfigurationException e) {
                throw new IStmtContainer.ESQLProblem(e);
            }            
        }
        if(sql instanceof Short){
            return Integer.valueOf(((Short)sql).intValue());
        }
        if(sql instanceof Float){
            return Double.valueOf(((Float)sql).doubleValue());
        }
        if(sql instanceof InputStream){
            try {
                return convertReader(new InputStreamReader((InputStream)sql, CHARSET_NAME));
            } catch (UnsupportedEncodingException e) {
                throw new IStmtContainer.ESQLProblem(e);
            }
        }
        if(sql instanceof Reader){
            return convertReader((Reader) sql);
        }
        
        return sql;
    }

    private static String convertReader(Reader reader){
        StringBuilder resBuffer = new StringBuilder();
        char portion[] = new char[READ_PORTION_SIZE];
        for(int i=0; i<MAX_STRING_SIZE/READ_PORTION_SIZE; i++){
            int readed;
            try {
                readed = reader.read(portion, i*READ_PORTION_SIZE, READ_PORTION_SIZE);
            } catch (IOException e) {
                throw new IStmtContainer.ESQLProblem(e);
            }
            if(readed < 0)
                break;
            resBuffer.append(portion, 0, readed);
            if(readed != READ_PORTION_SIZE){
                break;
            }
        }
        return resBuffer.toString();
    }

}
