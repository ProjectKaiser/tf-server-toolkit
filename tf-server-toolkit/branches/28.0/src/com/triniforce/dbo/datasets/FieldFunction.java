/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.dbo.datasets;



public class FieldFunction {
    public interface IFieldFunctionCtx{
        String getField();
        String getTarget();
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
