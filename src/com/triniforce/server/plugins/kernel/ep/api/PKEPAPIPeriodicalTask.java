/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.server.plugins.kernel.ep.api;

import com.triniforce.server.plugins.kernel.PeriodicalTasksExecutor.BasicPeriodicalTask;
import com.triniforce.utils.ICommitable;

public abstract class PKEPAPIPeriodicalTask extends BasicPeriodicalTask implements IPKEPAPI, ICommitable{
}
