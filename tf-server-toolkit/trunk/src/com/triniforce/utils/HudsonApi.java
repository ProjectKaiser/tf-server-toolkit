/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
    http://gmpxp:8080/hudson/job/toolkit/api/json
    http://gmpxp:8080/hudson/api/json?tree=jobs[name]
*/


public class HudsonApi {
    private final String m_baseAddress;

    public HudsonApi(String baseAddress){
        m_baseAddress = baseAddress.endsWith("/")?baseAddress:baseAddress+"/";
    }
    
    public void copyJob(){
    }

    public static String encodeUrl(String str){
    	return URLParamEncoder.encode(str);
    }
    
    public String calcFullUrl(String collection, String object, String format, Map<String, String> query){
        String fullUrl = getBaseAddress();
        if(!TFUtils.isEmptyString(collection)){
            fullUrl += encodeUrl(collection) +"/";            
        }
        if(!TFUtils.isEmptyString(object)){
            fullUrl += encodeUrl(object) +"/";            
        }
        fullUrl += "api/";
        fullUrl += format;
        if(query != null && query.size() > 0){
            fullUrl += "?";
            for(String key: query.keySet()){
                fullUrl += encodeUrl(key) + "=" + encodeUrl(query.get(key));                
            }
        }
        return fullUrl;
    }
    
    String sendRequest(String collection, String object, String format, String query){
        String fullUrl = getBaseAddress();
        if(!TFUtils.isEmptyString(collection)){
            fullUrl +=collection +"/";            
        }
        if(!TFUtils.isEmptyString(object)){
            fullUrl += object +"/";            
        }
        fullUrl += "api/";
        fullUrl += format;
        if(!TFUtils.isEmptyString(query)){
            fullUrl += "?" + query;            
        }        
        return "";
    }
    
//    HttpURLConnection con = (HttpURLConnection) url.openConnection();
//
//    con.setDoOutput(true);
//    con.setDoInput(true);
//    con.setRequestMethod("POST");
//    con.setRequestProperty("Content-Type", "text/xml");
//    
//    OutputStream output = con.getOutputStream();
//
//    output.write(req.getBytes("utf-8"));
//    output.flush();
//    output.close();
//    InputStream input = con.getInputStream();
//
//    StringBuffer strBuf = new StringBuffer();
//    BufferedReader r = new BufferedReader(new InputStreamReader(input));
//    while(r.ready()){
//     strBuf.append(r.readLine());
//     strBuf.append('\n');
//    }
//    System.out.println(strBuf.toString());
//    return strBuf.toString();    
    
    
    
    public List<String> getListOfJobs(){
        List<String> res = new ArrayList<String>();
        return res;
    }

    public String getBaseAddress() {
        return m_baseAddress;
    }
    

}
