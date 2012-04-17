/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */ 
package com.triniforce.db.test;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import com.triniforce.utils.ITime;

public class BasicServerApiEmu implements ITime{
    
    protected final static long TIME_CYCLE;
    
    public static class TimeCycle {
        private long m_start;

        private int m_curr;

        private int m_currGlob = 0;

        private long[] m_offs;

        public TimeCycle(long start, long offs[]) {
            m_start = start;
            m_offs = offs;
            m_curr = 0;
        }

        public long get() {
            long res = m_start;
            m_start += m_offs[m_curr];
            m_curr++;
            m_curr %= m_offs.length;
            m_currGlob++;
            return res;
        }

    }
    
    protected final static long TIME_OFFSETS[] = { 1100L, 200L, 510L, 130L,
    10L, 600L, 2000L, 32000L };
    
    static {
        long s = 0;
        for (int i = 0; i < TIME_OFFSETS.length; i++) {
            s += TIME_OFFSETS[i];
        }
        TIME_CYCLE = s;
    }

    public Timestamp getTime(int i) {
        return new Timestamp(getTimeMillis(i));
    }

    public long getTimeMillis(int i) {
        long res = START_TIME + (i / TIME_OFFSETS.length) * TIME_CYCLE;
        for (int j = 0; j < i % TIME_OFFSETS.length; j++)
            res += TIME_OFFSETS[j];
        return res;
    }
    
    protected final static long START_TIME = (new GregorianCalendar(2001, 5, 16))
    .getTimeInMillis();

    
    public BasicServerApiEmu() {
        setTimeSeq(START_TIME, TIME_OFFSETS);
    }

    private TimeCycle m_timer;

    public long currentTimeMillis() {
        return m_timer.get();
    }
    public void setCurrentTimeMillis(long v) {
        m_timer = new TimeCycle(v, new long[] { 0L });
    }

    public void setTimeSeq(long startTime, long[] timeOffsets) {
        m_timer = new TimeCycle(startTime, timeOffsets);
    }
    
    public int currentTimerId() {
        return m_timer.m_currGlob;
    }

    
}
