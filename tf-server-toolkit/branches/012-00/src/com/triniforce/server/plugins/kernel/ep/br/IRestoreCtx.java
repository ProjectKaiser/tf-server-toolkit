/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.br;

import java.util.Map;

public interface IRestoreCtx{
	Map<String, String> getCurrentPluginVersions();
	Map<String, String> getSavedPluginVersions();
}