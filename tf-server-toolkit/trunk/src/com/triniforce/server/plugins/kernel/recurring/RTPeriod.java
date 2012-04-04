/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */ 

package com.triniforce.server.plugins.kernel.recurring;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.PeriodType;

public abstract class RTPeriod {
    
    public static final RTPeriod DAY = new DayPeriod();
    public static final RTPeriod WEEK = new WeekPeriod();
    public static final RTPeriod MONTH = new MonthPeriod();
    public static final RTPeriod YEAR = new YearPeriod();
    
    public static final int DAY_PAST_THRESHOLD = 1000 * 3600 * 12;
    public static final int OTHERS_PAST_THRESHOLD = 1000 * 3600 * 24 * 3;
    
    public static class DayPeriod extends RTPeriod{
        private DayPeriod() {
            super(0, DAY_PAST_THRESHOLD);
        }

        @Override
        protected DurationFieldType getDurationFieldType() {
            return DurationFieldType.days();
        }
    }
    
    public static class WeekPeriod extends RTPeriod{
        private WeekPeriod() {
            super(1, OTHERS_PAST_THRESHOLD);
        }

        @Override
        protected DurationFieldType getDurationFieldType() {
            return DurationFieldType.weeks();
        }  
    }
    
    public static class MonthPeriod extends RTPeriod{
        private MonthPeriod() {
            super(2, OTHERS_PAST_THRESHOLD);
        } 
        @Override
        protected DurationFieldType getDurationFieldType() {
            return DurationFieldType.months();
        }  
    }
    
    public static class YearPeriod extends RTPeriod{
        private YearPeriod() {
            super(3, OTHERS_PAST_THRESHOLD);
        } 
        @Override
        protected DurationFieldType getDurationFieldType() {
            return DurationFieldType.years();
        }  
    }

    protected final int m_dbValue;
    protected int m_pastThreshold;
    
    public int getPastThreshold(){
        return m_pastThreshold;        
    }
    
    public int getDbValue(){
        return m_dbValue;
    }
    
    private RTPeriod(int dbValue, int pastThreshold) {
        m_dbValue = dbValue;
        m_pastThreshold = pastThreshold;
    }
    
    public static RTPeriod fromDbValue(Integer dbValue){
        return fromDbValue(dbValue, true);
    }
    
    public static RTPeriod fromDbValue(Integer dbValue, boolean throwIfWrong){
        if (dbValue == null)
            return null;
        switch (dbValue) {
        case 0:
            return DAY;
        case 1:
            return WEEK;
        case 2:
            return MONTH;
        case 3:
            return YEAR;
        default:
            if (throwIfWrong){
                throw new IllegalArgumentException("The RTPeriod dbValue '" + dbValue +"' is not recognized");
            }
        }
        return null;
    }
    
    protected abstract DurationFieldType getDurationFieldType();
    
    public long calcNextOccurence(long pastInstant, long currentInstant, String timeZone){
        DateTimeZone tz = DateTimeZone.forID(timeZone);
        DateTime start = new DateTime(pastInstant, tz);
        DateTime through = new DateTime(currentInstant, tz);
        PeriodType periodType = PeriodType.forFields(new DurationFieldType[]{getDurationFieldType()});
        Period p = new Period(start, through, periodType);
        MutablePeriod mp = new MutablePeriod(periodType); 
        mp.setValue(0, p.getValue(0) + 1);
        return start.plus(mp).getMillis();
    }
    
}
