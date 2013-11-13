/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.core;

import java.util.ArrayList;
import java.util.Collection;

public class StreetPath {
    
    Collection<String> m_path = new ArrayList<String>();
    
    public StreetPath(String... names) {
        for (String name: names){
            m_path.add(name);
        }
    }
    
    public StreetPath(Collection<String> names) {
        m_path = names;
    }
    
    Collection<String> getPath(){
        return m_path;
    }
    
}
