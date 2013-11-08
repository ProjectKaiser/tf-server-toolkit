/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.concurrent.Future;


public interface IPostMaster {
    Future post(String addr, Object data);
    Future post(Class addr, Object data);
}
