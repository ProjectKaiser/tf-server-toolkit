/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.List;

public interface IPostMaster {
    INamespace createNamespace();
    INamespace createNamespace(List<IRecipient> recs);
    
}
