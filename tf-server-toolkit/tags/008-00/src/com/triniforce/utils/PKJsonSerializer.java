/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.utils;

import net.sf.sojo.interchange.AbstractSerializer;
import net.sf.sojo.interchange.json.JsonWalkerInterceptor;

public class PKJsonSerializer  extends AbstractSerializer {

    private JsonWalkerInterceptor jsonInterceptor = new JsonWalkerInterceptor();

    
    public PKJsonSerializer() {
        setWithSimpleKeyMapper(false);
        setWithNullValuesInMap(true);
        walker.addInterceptor(jsonInterceptor);
    }

    public boolean getWithNullValuesInMap() { return jsonInterceptor.getWithNullValuesInMap(); }
    public void setWithNullValuesInMap(boolean pvWithNullValuesInMap) { jsonInterceptor.setWithNullValuesInMap(pvWithNullValuesInMap); }

    public Object serialize(Object pvRootObject) {
        walker.walk(pvRootObject);
        return jsonInterceptor.getJsonString();
    }

    
    public Object deserialize(Object pvSourceObject, Class pvRootClass) {
        String lvParseString = (pvSourceObject == null ? null : pvSourceObject.toString());
        Object lvResult = new PKJsonParser().parse(lvParseString);
        lvResult = getObjectUtil().makeComplex(lvResult, pvRootClass);
        return lvResult;
    }

}