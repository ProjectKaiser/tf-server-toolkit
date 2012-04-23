/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.io.ByteArrayInputStream;

import net.sf.sojo.interchange.json.JsonParserException;
import net.sf.sojo.interchange.json.generate.JsonParserGenerate;
import net.sf.sojo.interchange.json.generate.ParseException;

public class PKJsonParser {
    
    private JsonParserGenerate jsonParserGenerate = null;
    
    public Object parse(final String pvJsonString) throws JsonParserException {
        Object lvReturn = null;
        if (pvJsonString != null) {
            try {               
                ByteArrayInputStream lvInputStream = null;
                try {
                    lvInputStream = new ByteArrayInputStream(pvJsonString.getBytes("utf-8"));
                } catch (Exception e) {
                    ApiAlgs.rethrowException(e);
                }
                if (jsonParserGenerate == null) {
                    jsonParserGenerate = new JsonParserGenerate(lvInputStream, "utf-8");
                } else {
                    jsonParserGenerate.ReInit(lvInputStream);
                }
                lvReturn = jsonParserGenerate.parse();
            } catch (ParseException e) {
                throw new JsonParserException("Exception in String: '" + pvJsonString + "' --> " + e.getMessage());
            }
        }
        return lvReturn;
    }

}
