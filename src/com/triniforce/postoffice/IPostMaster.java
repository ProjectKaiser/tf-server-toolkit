/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.postoffice;

import java.util.concurrent.Future;


public interface IPostMaster {
    Future post(Object data);
}
