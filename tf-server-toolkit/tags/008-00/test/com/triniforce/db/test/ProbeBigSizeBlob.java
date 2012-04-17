/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */

package com.triniforce.db.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.triniforce.db.ddl.AddColumnOperation;
import com.triniforce.db.ddl.TableDef;
import com.triniforce.db.ddl.TableDef.FieldDef;
import com.triniforce.db.ddl.TableDef.FieldDef.ColumnType;

public class ProbeBigSizeBlob {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        DBTestCase tc = new DBTestCase();
        tc.getConnection().setAutoCommit(false);        
        System.out.println(tc.getConnection().toString());
        
        TableDef tab1 = new TableDef("ProbeBigSizeBlob.blob_t");
        tab1.addModification(1, new AddColumnOperation(FieldDef.createScalarField("b", ColumnType.BLOB, true)));
        tab1.addModification(2, new AddColumnOperation(FieldDef.createScalarField("i", ColumnType.INT, true)));           
        //String tabName = tc.createTableIfNeeded(tab1);
        
        System.out.println("Type:");
        System.out.println("quit - quit");
        System.out.println("w10 - write 10M blob");        
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            String s = in.readLine();
            if(s.equals("quit")){
                System.out.println("Bye !");
                break;              
            }
            if(s.equals("w10")){
                System.out.println("Bye !");
                break;              
            }else{
                System.out.println("you entered: " + s);                
            }
        }
        
        
       
        

    }

}
