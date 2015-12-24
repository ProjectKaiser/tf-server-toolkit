/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package gmp;

import com.triniforce.db.test.BasicServerTestCase;
import com.triniforce.utils.ICheckInterrupted;

public class InvPeriodicalTest extends BasicServerTestCase{
    @Override
    public void test() throws Exception {
        getServer().startPeriodicalTasks();
        try {
            ICheckInterrupted.Helper.sleep(20000);
        } finally {
            getServer().stopPeriodicalTasks();
        }

    }
    
    public void testThreads(){
        Runnable r =new Runnable() {
            
            int i;
            @Override
            public void run() {
                while(true){
                    trace("tick" + i++);
                    ICheckInterrupted.Helper.sleep(1000);
                }
            }
        };
        
        Thread t = new Thread(r);
        t.start();
        trace("started");
        getServer().startPeriodicalTasks();
        ICheckInterrupted.Helper.sleep(5000);
        
    }
    
    public static void main(String[] args) {
        Runnable r =new Runnable() {
            
            int i;
            @Override
            public void run() {
                while(true){
                    System.out.println("tick" + i++);
                    ICheckInterrupted.Helper.sleep(1000);
                }
            }
        };
        
        Thread t = new Thread(r);
        t.start();
        System.out.println("started");
        ICheckInterrupted.Helper.sleep(5000);
        System.out.println("bye");
    }

}
