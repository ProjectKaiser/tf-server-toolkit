/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;

import java.util.Map;

public abstract class FieldFunction {
    public interface IFieldFunctionCtx{
        String getField();
        String getTarget();
        Long getParentId();
        Map<String, Object> getParams();
    }
    
    /**
     * This method can e.g. check if ctx.getField is of appropriate type.
     * @param ctx
     */
    public void init(IFieldFunctionCtx ctx) throws RuntimeException{
    }


	public Object exec(Object value){
		return null;
	}
}
