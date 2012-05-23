/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.recurring;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

import com.triniforce.server.plugins.kernel.recurring.RTPeriod;
import com.triniforce.db.test.TFTestCase;

public class RTPeriodTest extends TFTestCase {

    @Override
    public void test() throws Exception{

        List<RTPeriod> periods= new ArrayList<RTPeriod>();
        Set<Integer> dbValues = new HashSet<Integer>();
        periods.add(RTPeriod.DAY);
        periods.add(RTPeriod.WEEK);
        periods.add(RTPeriod.MONTH);
        periods.add(RTPeriod.YEAR);
        
        //test that dbValue unique
        {
            for(RTPeriod p: periods){
                dbValues.add(p.getDbValue());
                assertEquals(p, RTPeriod.fromDbValue(p.getDbValue()));
            }
            assertEquals(periods.size(), dbValues.size());
        }
        
        assertNull(RTPeriod.fromDbValue("", false));
        
        //test wrong values
        assertNull(RTPeriod.fromDbValue(-1, false));
        try{
            RTPeriod.fromDbValue(-1);
            fail();
        }catch(IllegalArgumentException e){
            trace(e);
        }
        
        //test thershold
        assertEquals(1000 * 3600 * 12, RTPeriod.DAY.getPastThreshold());
        assertEquals(1000 * 3600 * 24 * 3, RTPeriod.WEEK.getPastThreshold());
        assertEquals(1000 * 3600 * 24 * 3, RTPeriod.MONTH.getPastThreshold());
        assertEquals(1000 * 3600 * 24 * 3, RTPeriod.YEAR.getPastThreshold());
    }
    
    public static DateTimeZone tzMsc = DateTimeZone.forID("Europe/Moscow");
    DateTime april2012Msc = new DateTime(2012, 04, 1, 12, 0, tzMsc);
    //2011-03-27 01:59:59.999+03:00
    DateTime prevTransitionMsc = new DateTime(tzMsc.previousTransition(april2012Msc.getMillis()), tzMsc);
    
    public void test_calcNextOccurence(){
        
        long afterPrevTransitionMsc =  prevTransitionMsc.getMillis() + 612308734;
        
        //test day
        {
            assertEquals(afterPrevTransitionMsc, RTPeriod.DAY.calcNextOccurence(afterPrevTransitionMsc, prevTransitionMsc.getMillis(), tzMsc.getID()));
            for(int i = 0;i< 10;i++){
                DateTime before =  prevTransitionMsc.minusDays(i).plusHours(3);
                Long nextOccurence = RTPeriod.DAY.calcNextOccurence(before.getMillis(), prevTransitionMsc.getMillis(), tzMsc.getID());
                DateTime after = new DateTime(nextOccurence, tzMsc);
                if(i>0){
                    assertTrue(before.isBefore(after));
                }else{
                    assertEquals(after, before);                    
                }
                assertEquals(0, Days.daysBetween(prevTransitionMsc, after).getDays());
                assertEquals(before.getHourOfDay(), after.getHourOfDay());
            }
        }
        //test week
        {
            for(int i = 0;i< 10;i++){
                DateTime before =  prevTransitionMsc.minusWeeks(i).plusDays(2);
                Long nextOccurence = RTPeriod.WEEK.calcNextOccurence(before.getMillis(), prevTransitionMsc.getMillis(), tzMsc.getID());
                DateTime after = new DateTime(nextOccurence, tzMsc);
                if(i>0){
                    assertTrue(before.isBefore(after));
                }else{
                    assertEquals(after, before);                    
                }
                assertEquals(0, Weeks.weeksBetween(prevTransitionMsc, after).getWeeks());
                assertEquals(before.getHourOfDay(), after.getHourOfDay());
                assertEquals(before.getDayOfWeek(), after.getDayOfWeek());
            }
        }
        //test month
        {
            for(int i = 0;i< 10;i++){
                DateTime before =  prevTransitionMsc.minusMonths(i).plusDays(2);
                Long nextOccurence = RTPeriod.MONTH.calcNextOccurence(before.getMillis(), prevTransitionMsc.getMillis(), tzMsc.getID());
                DateTime after = new DateTime(nextOccurence, tzMsc);
                if(i>0){
                    assertTrue(before.isBefore(after));
                }else{
                    assertEquals(after, before);                    
                }
                assertEquals(0, Months.monthsBetween(prevTransitionMsc, after).getMonths());
                assertEquals(before.getHourOfDay(), after.getHourOfDay());
                assertEquals(before.getDayOfMonth(), after.getDayOfMonth());
            }
        }
        //test year
        {
            for(int i = 1;i< 10;i++){
                DateTime before =  prevTransitionMsc.minusMonths(i).plusDays(2);
                Long nextOccurence = RTPeriod.YEAR.calcNextOccurence(before.getMillis(), prevTransitionMsc.getMillis(), tzMsc.getID());
                DateTime after = new DateTime(nextOccurence, tzMsc);
                if(i>0){
                    assertTrue(before.isBefore(after));
                }else{
                    assertEquals(after, before);                    
                }
                assertEquals(0, Years.yearsBetween(prevTransitionMsc, after).getYears());
                assertEquals(before.getHourOfDay(), after.getHourOfDay());
                assertEquals(before.getDayOfMonth(), after.getDayOfMonth());
                assertEquals(before.getMonthOfYear(), after.getMonthOfYear());
            }
        }
    }
    
    

}
