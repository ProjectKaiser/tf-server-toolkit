/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.utils;

public interface ICheckInterrupted {

    public static class Helper {
        public static void checkInterrupted() {
            ICheckInterrupted itf = ApiStack.getApi().queryIntfImplementor(
                    ICheckInterrupted.class);
            if (null == itf)
                return;
            itf.checkInterrupted();
        }

        public static boolean isInterruptedException(Throwable t) {
            if( null == t ) return false;
            return ( t!=null ) && (t instanceof InterruptedException)
                    || (t instanceof EInterrupted) || isInterruptedException(t.getCause());
        }

        public static void sleep(int timeOutMs) throws EInterrupted {
            try {
                if( timeOutMs <= 0){
                    Thread.sleep(10);
                }else{
                    Thread.sleep(timeOutMs);
                }
                return;
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                throw new EInterrupted();
            }
        }
    }

    public static class CheckInterrupted implements ICheckInterrupted {
        public void checkInterrupted() throws EInterrupted {
            if (Thread.currentThread().isInterrupted()) {
                throw new EInterrupted();
            }
        }
    }

    public static class EInterrupted extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EInterrupted(Throwable t) {
            super(t);
        }

        public EInterrupted() {
            super();
        }
    }

    public void checkInterrupted() throws EInterrupted;
}
