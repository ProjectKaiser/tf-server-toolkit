/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.Map;

public interface IPostMaster {
    INamespace createNamespace();
    INamespace createNamespace(Map<String, IPOBox> boxes);

    
    
}
