/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import java.util.Map;
import java.util.regex.Pattern;

public class VersionComparator {
    public static int compareVersions(String v1, String v2){
        return normalisedVersion(v1).compareTo(normalisedVersion(v2));
    }
    
    public static int compareVersions(Map<String, String> V1, Map<String, String> V2){
        boolean hasLesserElement = false;
        boolean hasGreaterElement = false;
        for(String key2: V2.keySet()){
            if(V1.containsKey(key2)){
                int vCompare = compareVersions(V1.get(key2), V2.get(key2)); 
                if ( vCompare < 0){
                    hasLesserElement = true;
                    break;
                }
                if ( vCompare > 0){
                    hasGreaterElement = true;
                }
            }else{
                hasLesserElement = true;
                break;                
            }
        }
        if(hasLesserElement){
            return -1;
        }
        if(hasGreaterElement || V1.size() > V2.size()){
            return 1;
        }
        return 0;
    }
    
    
    public static String normalisedVersion(String aVersion) {
        String version = null == aVersion ?"": aVersion;
        return normalisedVersion(version, 10);
    }

    static Pattern m_pattern = Pattern.compile(".", Pattern.LITERAL);
    
    public static String normalisedVersion(String version, int maxWidth) {
        String[] split = m_pattern.split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }

}
