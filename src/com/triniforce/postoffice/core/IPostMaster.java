/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice.core;

import java.util.concurrent.Future;


public interface IPostMaster {
    Future post(StreetPath streetPath, String box, Object data);
    Future post(StreetPath streetPath, Class box, Object data);
}
