/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.qsync.impl;

import com.triniforce.extensions.PKExtensionPoint;

public class PKEPQSyncStaticSyncers  extends PKExtensionPoint {
	public PKEPQSyncStaticSyncers() {
		setSingleExtensionInstances(true);
		setExtensionClass(QSyncStatisSyncer.class);
	}
}
