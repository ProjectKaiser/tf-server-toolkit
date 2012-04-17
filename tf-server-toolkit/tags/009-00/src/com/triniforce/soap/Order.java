/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.soap;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.triniforce.utils.IName;

public class Order {
    
    interface IKeyGetter<T, Key>{
        Key getKey(T v);
    }

    public static <T, Key> void orderBy(List<? extends T> vals, final List<Key> order, final IKeyGetter<T,Key> keyGetter) {
        Collections.sort(vals, new Comparator<T>(){
            public int compare(T arg0, T arg1) {
                return indexof(arg0) - indexof(arg1);
            }
            int indexof(T arg){
                int res = order.indexOf(keyGetter.getKey(arg));
                if(res == -1)
                    res = order.size();
                return res;
            }
        });
    }
    
    static class NameGetter implements IKeyGetter<IName, String>{
        public String getKey(IName v) {
            return v.getName();
        }
    }

    public static void orderINames(List<? extends IName> newOps, final List<? extends IName> oldOps) {
        orderBy(newOps, new AbstractList<String>(){
            @Override
            public String get(int arg0) {
                return oldOps.get(arg0).getName();
            }

            @Override
            public int size() {
                return oldOps.size();
            }
        }, new NameGetter());
    }
}
