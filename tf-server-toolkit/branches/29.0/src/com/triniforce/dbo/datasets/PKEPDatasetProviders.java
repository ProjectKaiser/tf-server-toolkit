/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import com.triniforce.extensions.PKExtensionPoint;

public class PKEPDatasetProviders extends PKExtensionPoint{

	public PKEPDatasetProviders() {
        setExtensionClass(PKEPDatasetProvider.class);
        setSingleExtensionInstances(true);
	}
}
