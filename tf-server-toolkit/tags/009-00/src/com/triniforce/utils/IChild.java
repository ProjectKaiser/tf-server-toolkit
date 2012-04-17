/*
 *
 * (c) Triniforce, 2006
 *
 */
package com.triniforce.utils;

import java.util.Set;

public interface IChild {
    IChild getParent() throws EUtils.EParentNotExist;
    Set<IChild> getChilds();
}
