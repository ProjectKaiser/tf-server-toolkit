/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.war;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import com.triniforce.db.dml.IStmtContainer;
import com.triniforce.db.dml.Stmt;
import com.triniforce.server.srvapi.SrvApiAlgs2;
import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.ApiStack;
import com.triniforce.war.api.IBasicDiag;

public class BasicDiag implements IBasicDiag{

    public String getTextFile(String fileName, int nlines) {
        String res = "";
        try {
            File f = new File(fileName);
            BufferedReader reader = new BufferedReader(new FileReader(f));
            try {
                List lines = new ArrayList();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                int startFrom = lines.size() - nlines;
                if (startFrom < 0) {
                    startFrom = 0;
                }
                for (int i = startFrom; i < lines.size(); i++) {
                    res += lines.get(i) + "\n";
                }
				
			} finally {
				reader.close();
			}

        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return res;
    }

    public String getCatalinaLog(String logName, int nlines) {
        String chome = System.getProperty("catalina.home");
        if (null != chome) {
            File f = new File(System.getProperty("catalina.home"));
            f = new File(f, "logs");
            f = new File(f, logName);
            return getTextFile(f.getAbsolutePath(), nlines);
        } else {
            return("catalina.home is not defined");
        }
    }

    public String getEnvironment() {
        StringBuffer res = new StringBuffer("");
    
		appendln(res, "=================== Server Parameters ============================");
		appendln(res, "");
		appendln(res, "=================== Servlet Context  ==============================");
        {
            ServletContext ctx= ApiStack.queryInterface(ServletContext.class);
            if( null != ctx){
            	appendln(res, "Server info: " + ctx.getServerInfo());
            	appendln(res, "Real path (/): " + ctx.getRealPath("/"));
            	appendln(res, "Parameters:\n");
                {
                    Enumeration params = ctx.getInitParameterNames();
                    while(params.hasMoreElements()){
                        String name = (String) params.nextElement();
                        res.append("  " + name + "=" + ctx.getInitParameter(name));
                        res.append('\n');
                    }
                }
            }
        }
        res.append("=================== System Properties  ==============================");
        res.append('\n');
        Properties props = System.getProperties();
        List names = new ArrayList();
        for(Object name:props.keySet()){
            names.add(name);               
        }
        Collections.sort(names);
        for(Object name: names){
        	res.append(name + ": " + props.getProperty((String) name));               
            res.append('\n');
        }
        return res.toString();
    }

    private void appendln(StringBuffer res, String string) {
		res.append(string);
		res.append('\n');
	}

	public String executeSelect(String sql, int limit) {
        StringBuffer res = new StringBuffer();
        try {
            IStmtContainer sc = SrvApiAlgs2.getStmtContainer();
            try {
                Stmt stmt = sc.getStatement();
                ResultSet rs = stmt.getStatement().executeQuery(sql);
                int processed = 0;
                {//headers
                    for( int c = 1 ; c<=rs.getMetaData().getColumnCount(); c++){
                        res.append(" " + rs.getMetaData().getColumnLabel(c));
                    }
                    res.append("\n");                        
                }
                                    
                while (rs.next() && processed < limit) {
                    for( int c = 1 ; c<=rs.getMetaData().getColumnCount(); c++){
                        res.append(" " + rs.getObject(c));
                    }
                    res.append("\n");
                    processed++;
                }

            } finally {
                sc.close();
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return res.toString();
    }

    public int executeUpdate(String sql) {
        try {
            IStmtContainer sc = SrvApiAlgs2.getStmtContainer();
            try {
                Stmt stmt = sc.getStatement();
                return stmt.getStatement().executeUpdate(sql);
            } finally {
                sc.close();
            }
        } catch (Exception e) {
            ApiAlgs.rethrowException(e);
        }
        return 0;
    }

	@Override
	public Class getImplementedInterface() {
		return IBasicDiag.class;
	}
}