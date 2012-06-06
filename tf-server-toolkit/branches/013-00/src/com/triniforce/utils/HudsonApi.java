/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    http://localhost:8080/hudson/job/toolkit/api/json
    http://gmpxp:8080/hudson/api/json?tree=jobs[name]
    http://localhost:8080/hudson/job/toolkit-2/config.xml
*/


public class HudsonApi {
    private final String m_baseAddress;
    private final String m_user;
    private final String m_password;

    public HudsonApi(String baseAddress, String user, String password){
        m_user = user;
        m_password = password;
        m_baseAddress = baseAddress.endsWith("/")?baseAddress:baseAddress+"/";
    }
    
    
    public void copyJob(String srcName, String dstName){
        String url = getBaseAddress() + "createItem";
        Map<String, String> q = new HashMap<String, String>();
        q.put("name", dstName);
        q.put("mode", "copy");
        q.put("from", srcName);
        postRequest(url, q, null);
    }

    public void createJob(String jobName, String configXml){
        String url = getBaseAddress() + "createItem";
        Map<String, String> q = new HashMap<String, String>();
        q.put("name", jobName);
        postRequest(url, q, configXml);
    }
    
    public static String encodeUrl(String str){
    	return URLParamEncoder.encode(str);
    }

    public static String appendQuery(String url, Map<String, String> query){
        if(query != null && query.size() > 0){
            url += "?";
            boolean first = true;
            for(String key: query.keySet()){
                url += (first?"":"&") + encodeUrl(key) + "=" + encodeUrl(query.get(key));
                first = false;
            }
        }
        return url;
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
        return appendQuery(fullUrl, query);
    }
    
    String postRequest(String fullUrl, Map<String, String> query, String data){
        return postRequest(fullUrl, query, data, "POST");
    }
    
    String postRequest(String fullUrl, Map<String, String> query, String data, String method){
        try {
            fullUrl = appendQuery(fullUrl, query);
            URL url = new URL(fullUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            
            

            //con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod(method);
            con.setRequestProperty("Content-Type", "text/xml");
            
            if (!TFUtils.isEmptyString(m_user) && !TFUtils.isEmptyString(m_password)){
                String userpass = m_user + ":" + m_password;
                String basicAuth = "Basic " + new String(Base64.encodeBytes(userpass.getBytes()));
                con.setRequestProperty ("Authorization", basicAuth);
            }
            
            if(!TFUtils.isEmptyString(data)){
                con.setDoOutput(true);
                OutputStream output = con.getOutputStream();
                output.write(data.getBytes("utf-8"));
                output.flush();
                output.close();
            }
            InputStream input = con.getInputStream();
        
            StringBuffer strBuf = new StringBuffer();
            BufferedReader r = new BufferedReader(new InputStreamReader(input));
            while(r.ready()){
                strBuf.append(r.readLine());
                strBuf.append('\n');
            }
            return strBuf.toString();    
            
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return "";        
        
    }
    
    String postRequest(String collection, String object, String format, Map<String, String> query){
        return postRequest(calcFullUrl(collection, object, format, query), null, null);
    }
    
    public List<String> getListOfJobs(){
        Map<String, String> q = new HashMap<String, String>();
        q.put("tree", "jobs[name]");
        String strRes = postRequest(null, null, "json", q);
        PKJsonParser jp = new PKJsonParser();
        List<String> res = new ArrayList<String>();
        Object resO = jp.parse(strRes);
        if (!(resO instanceof Map)){
            return res;
        }
        Map resMap = (Map) resO;
        List jobs = (List) resMap.get("jobs");
        for(Object task: jobs){
            Map taskM =(Map) task;
            res.add((String) taskM.get("name"));    
        }
        return res;
    }
    
    // http://localhost:8080/hudson/job/toolkit-2/config.xml
    public String getJobConfigXml(String jobName){
        String url = getBaseAddress() + "job/" + encodeUrl(jobName) +"/config.xml";
        return postRequest(url, null, null, "GET");
    }
    
    /**
     * Yes. Just POST the updated config.xml to /job/[jobname]/config.xml. 
     */
    public String updateJobConfigXml(String jobName, String config){
        String url = getBaseAddress() + "job/" + encodeUrl(jobName) +"/config.xml";
        return postRequest(url, null, config, "POST");
    }
    
    public String getBaseAddress(){
        return m_baseAddress;
    }

}
