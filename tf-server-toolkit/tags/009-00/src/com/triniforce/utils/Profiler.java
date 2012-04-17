/* 
 * Copyright(C) Triniforce 
 * All Rights Reserved. 
 * 
 */
package com.triniforce.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import com.triniforce.utils.IProfilerStack.PSI;

public class Profiler implements IProfiler {
    
    static class Group{
        final String m_name;
        private HashMap<String, Item> m_items;
        
        public Group(String name) {
            m_name = name;
            m_items = new HashMap<String, Item>();
        }
        
        public String getName() {
            return m_name;
        }
        
        public Collection<Item> getItems() {
            ArrayList<Item> copy = new ArrayList<Item>(m_items.values());
            Collections.sort(copy, new Comparator<Item>(){
                public int compare(Item o1, Item o2) {
                    return -Long.valueOf(o1.getOwnTime()).compareTo(Long.valueOf(o2.getOwnTime()));
                }});
            
            return copy;
        }

        public void addItem(Item item) {
            m_items.put(item.getName(), item);
        }

        public Item getItem(String item) {
            return m_items.get(item);
        }        
    }
    
    public static class Item{       
        private final String m_name;
        private long m_totalTime;
        private long m_childsTime;
        private int m_itNum;
        
        public Item(String name) {
            m_name = name;
            m_totalTime = 0L;
            m_childsTime = 0L;
            m_itNum = 0;
        }

        public String getName(){
            return m_name;
        }

        public int getItNum() {
            return m_itNum;
        }

        public long getTotalTime() {
            return m_totalTime;
        }

        public long getChildsTime() {
            return m_childsTime;
        }

        public void setIt(long total, long childs) {
            m_totalTime += total;
            m_childsTime += childs;
            m_itNum ++;
        }

        public long getOwnTime() {
            return m_totalTime - m_childsTime;
        }

        public double getAvgTotalTime() {
            return getTotalTime() / getItNum();
        }
        
    }

    public interface INanoTimer{
        long get();
    }
    
    public static class ProfilerStack implements IProfilerStack{
        
        private Stack<PSI> m_stack;
        private IProfiler  m_profiler;
        private INanoTimer m_timer;
        
        public Stack<PSI> getStack() {
        	return this.m_stack; 
        }
        
        public ProfilerStack(IProfiler profiler, INanoTimer timer){
            m_stack = new Stack<PSI>();
            m_profiler = profiler;
            m_timer = timer == null ? new INanoTimer(){
                public long get() {
                    return System.nanoTime();
                }}
            : timer;
        }

        public PSI getStackItem(String group, String item) {
        	synchronized (m_profiler) {
        		PSI psi = new PSI(this, group, item, m_timer.get(), m_stack.size());
                m_stack.push(psi);
                return psi;
        	}
        	
        }

        public PSI getStackItem(String group, String item, String context) {
            synchronized (m_profiler) {
            	PSI psi = new PSI(this, group, item, m_timer.get(), m_stack.size(), context);
                m_stack.push(psi);
                return psi;
			}
        	
        }
        
        public void release(PSI psi) throws EWrongPSI {
        	long totalTime;
        	boolean bInner;
        	synchronized (m_profiler) {
        		PSI e = m_stack.pop();
                if(psi.getStackNumber() != e.getStackNumber()){
                    ApiAlgs.getLog(this).error(psi.getGroup()+"."+ psi.getItem() + " / " +e.getGroup()+"."+ e.getItem());
                    while(psi.getStackNumber() < e.getStackNumber())
                    	e = m_stack.pop();
                }
                
                totalTime = m_timer.get()-psi.getCreationTime();
                
                if(!m_stack.isEmpty()){
                    PSI parent = m_stack.peek();
                    parent.addChildTime(totalTime);
                }
                bInner = stackContainsItem(psi);
        	}
        	m_profiler.reportItem(psi.getGroup(), psi.getItem(), totalTime, psi.getChildsTime(), bInner);
        }

		private boolean stackContainsItem(PSI item) {
			for (PSI psi : m_stack) {
				if(psi.getGroup().equals(item.getGroup()) && psi.getItem().equals(item.getItem()))
					return true;
			}
			return false;
		}

    }
    
    private HashMap<String, Group> m_stat;
    
    public Profiler() {
        m_stat = new HashMap<String, Group>();
    }

    public HashMap<String, Group> getStat() {
        return m_stat;
    }

    @Override
    synchronized public String toString() {
        StringWriter buf = new StringWriter();
        PrintWriter printer = new PrintWriter(buf);
        printer.println("Profiler name = \"Default profiler\""); //$NON-NLS-1$
        printer.printf("%-70s%-10s%-10s%-10s%-10s%-10s\n", "Name", "OwnTotal", "Childs", "Total", "AvgTotal", "Count"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
        repeat(printer, '=', 120, true);
        for(Group group : getStat().values()){
            repeat(printer, '-', 4, false);
            printer.printf(" Group: %s ", group.getName()); //$NON-NLS-1$
            repeat(printer, '-', 120-9-4-group.getName().length(), true);
            for (Item item: group.getItems()) {
                String name[] = disassembleString(item.getName(), new int[]{69,66,66});
                printer.printf("%-69s %9s %9s %9s %9s %9d\n",  //$NON-NLS-1$
                        name[0],
                        formatTime(item.getOwnTime(), 9), 
                        formatTime(item.getChildsTime(),9), 
                        formatTime(item.getTotalTime(),9), 
                        formatTime(item.getAvgTotalTime(),9), 
                        item.getItNum());
                for(int i=1; i<3; i++){
                    if(name[i]!=null)
                        printer.printf("   %-46s\n", name[i]); //$NON-NLS-1$
                    else
                        break;
                }
            }
        }
        printer.close();
        buf.flush();
        return buf.toString();
    }

    private String formatTime(double t, int i) {
        String res=""; //$NON-NLS-1$
        double v = t;
        if(t <      1000000L){
            v /=    1000L;
            res = String.format("%4.1fmk", v); //$NON-NLS-1$
        }
        else if(t < 1000000000L){
            v /=    1000000L; 
            res = String.format("%4.1fml", v); //$NON-NLS-1$
        }
        else if(t < 1000000000000L){
            v /=    1000000000L; 
            res = String.format("%4.1fsc", v); //$NON-NLS-1$
        }
        else if(t < 1000000000000000L){
            v /=    1000000000L; 
            res = String.format("%3.0fsc", v); //$NON-NLS-1$
        }
        else if(t < 60000000000000000L){
            v /=    60000000000L; 
            res = String.format("%3.0fmn", v); //$NON-NLS-1$
        }
        else if(t < 3600000000000000000L){
            v /=    3600000000000L; 
            res = String.format("%3.0fhr", v); //$NON-NLS-1$
        }
        return res;
    }

    private String[] disassembleString(String name, int[] sz) {
        int pos = 0;
        String[] res = new String[sz.length];
        for(int i=0; i< sz.length; i++){
            int end = pos + sz[i];
            if(name.length() < end){
                res[i] = name.substring(pos, name.length());
                break;
            }
            res[i] = name.substring(pos, end);
            pos = end;
        }
        return res;
    }

    private void repeat(PrintWriter printer, char c, int i, boolean bRet) {
        String s = new String();
        for (int j = 0; j < i; j++) {
            s += c;
        }
        if(bRet)
            printer.println(s);
        else
            printer.print(s);
    }

    synchronized public void  reportItem(String group, String itemName, long total, long childs, boolean bInner) {
        Group g = m_stat.get(group);
        Item item;
        if(g == null){
            g = new Group(group);
            m_stat.put(group, g);
            item = null;
        }
        else{
            item = g.getItem(itemName);            
        }
        if(item == null){
            item = new Item(itemName);
            g.addItem(item);
        }
        if(bInner){
        	long own = total - childs;
        	item.setIt(-own, -own);
        }
        else
        	item.setIt(total, childs);
    }

	synchronized public void clearResult() {
		m_stat.clear();
	}
	
	private HashMap<Object, ActivityStatus> m_act_stat = new HashMap<Object, ActivityStatus>();
	
	synchronized public void removeActivityStatus(Object activityKey) {
		m_act_stat.remove(activityKey);
	}

	synchronized public void updateActivityStatus(Object activityKey, ActivityStatus status) {
		m_act_stat.put(activityKey, status);
	}
	
	synchronized public String getSnapshot() {
		
		HashSet<Object> requestsKeys = new HashSet<Object>();
		HashSet<Object> servicesKeys = new HashSet<Object>();
		
		for (Object key : m_act_stat.keySet()) {
			if (m_act_stat.get(key).idleInfo == null) {
				requestsKeys.add(key);
			} else {
				servicesKeys.add(key);
			}
		}
		
		StringWriter buf = new StringWriter();
        PrintWriter printer = new PrintWriter(buf);
		
        printer.printf("\nREQUESTS\n");
        for (Object key : requestsKeys) {
        	printer.println("========="+key.toString());
        	ActivityStatus status = m_act_stat.get(key);
        	Stack<PSI> stackPSI = status.ps.getStack();
			printer.print("Stack = ");
			int i = 0;
			for (PSI psi : stackPSI) {
				if (i!=0) {
					repeat(printer,' ',8, false);
				} 
				printer.println(""+psi.getGroup()+"."+psi.getItem()+"."+psi.getContext());
				i=i+1;
			}
		}
        
        printer.printf("\nSERVICES\n");
        for (Object key : servicesKeys) {
        	printer.println("========="+key.toString());
        	ActivityStatus status = m_act_stat.get(key);
        	printer.println("IdleInfo = "+status.idleInfo);
			Stack<PSI> stackPSI = status.ps.getStack();
			printer.print("Stack = ");
			int i = 0;
			for (PSI psi : stackPSI) {
				if (i!=0) {
					repeat(printer,' ',8, false);
				} 
				printer.println(""+psi.getGroup()+"."+psi.getItem()+"."+psi.getContext());
				i=i+1;
			}
		}
        
        printer.close();
        buf.flush();
        return buf.toString();
	}


}
