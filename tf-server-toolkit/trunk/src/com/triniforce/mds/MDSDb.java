/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.mds;

import com.triniforce.db.dml.ResSet;

public class MDSDb {
    // public static LongListResponse createFromResSet(ResSet resSet,
    // List<IName> extraColumns){
    //
    // ResultSetMetaData md;
    // try {
    // md = resSet.getResultSet().getMetaData();
    // String colNames[] = new String[md.getColumnCount() + (null ==
    // extraColumns?0:extraColumns.size())];
    // for (int i = 0; i < colNames.length; i++) {
    // colNames[i] = md.getColumnName(i + 1).toLowerCase();
    //
    // }
    // LongListResponse res = new LongListResponse(colNames);
    // while(resSet.next()){
    // Object row[] = new Object[colNames.length];
    // for (int i = 0; i < md.getColumnCount(); i++) {
    // row[i] = resSet.getObject(i + 1);
    // }
    // res.addRow(row);
    // }
    // return res;
    // } catch (Exception e) {
    // ApiAlgs.rethrowException(e);
    // }
    //
    // return null;
    //
    // }
    public static MDS createFromResSet(ResSet resSet) {
        // TODO
        return null;
    }

}
