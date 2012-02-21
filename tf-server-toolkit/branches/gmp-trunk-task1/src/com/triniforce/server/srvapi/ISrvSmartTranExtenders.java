/*
 * Copyright(C) Triniforce
 * All Rights Reserved.
 *
 */
package com.triniforce.server.srvapi;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.triniforce.utils.ApiAlgs;
import com.triniforce.utils.IFinitable;

public interface ISrvSmartTranExtenders {
    /**
     * 
     * Transaction outer extender. Provides Object => Object mapping with
     * reference couting. There are two set of entries: transaction and server.
     * After transaction is finished transaction set is cleaned up and refcounts
     * of corresponding server values are descreased by one. When refcount
     * becomes zero server entry is removed.
     * 
     */
    public interface IRefCountHashMap {
        public interface IFactory {
            Object newInstance(Object key);
        }

        /**
         * If key exists in transaction set just returns its value.
         * <p>
         * If value with such key exists in server set increase its count,
         * otherwise instance is created using factory. Result is put into
         * transaction set.
         */
        Object put(Object key, IFactory factory);

        /**
         * @return Server value for given key, null if none
         */
        Object getServerValue(Object key);

        /**
         * Get refcount of server value
         * 
         */
        int getServerRefCount(Object key);

        /**
         * Returns transaction keys. If cls is not null then only instances of
         * given classes are returned.
         */
        Set<Object> getTransactionKeys();

        /**
         * Returns server keys. If cls is not null then only instances of given
         * classes are returned.
         */
        Set<Object> getServerKeys();

        /**
         * Removes all keys from transaction set and descrease corresponsing
         * server set refcounts. If refcount becomes zero entry is removed from
         * server set.
         * 
         */
        void flush();

    }

    /**
     * Transaction outer extender which keeps LockerKey => LockerValue mapping
     * using IRefCountHashMap.
     * <p>
     * IdObjectKey is used as key, Long as a value.
     */
    public interface ILocker extends IModeAny {
        public static class LockerKey {
            public LockerKey(Object key) {
                m_key = key;

            }

            public final Object m_key;

            @Override
            public int hashCode() {
                final int PRIME = 31;
                int result = 1;
                result = PRIME * result
                        + ((m_key == null) ? 0 : m_key.hashCode());
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                final LockerKey other = (LockerKey) obj;
                if (m_key == null) {
                    if (other.m_key != null)
                        return false;
                } else if (!m_key.equals(other.m_key))
                    return false;
                return true;
            }
        }
        
        public static interface ILockableObject{};

        public static class LockerValue implements IFinitable {
            ReentrantLock m_lock = new ReentrantLock();

            String m_ownerThreadName;

            public void lock(LockerKey key) {
                if (!m_lock.isHeldByCurrentThread()) {
                    try {
                        int lockCnt = 0;
                        while (!m_lock.tryLock(5, TimeUnit.SECONDS)) {
                            ApiAlgs.getLog(this).warn(
                                    Thread.currentThread().getName()
                                            + " is waiting for " + key.m_key +" owned by "
                                            + m_ownerThreadName);
                            lockCnt++;
                            if(lockCnt > 12){
                                throw new RuntimeException("Lock timeiout expired");
                            }
                        }
                        m_ownerThreadName = Thread.currentThread().getName();
                    } catch (Exception e) {
                        ApiAlgs.rethrowException(e);
                    }
                }
            }

            public void finit() {
                if (m_lock.isHeldByCurrentThread()) {
                    m_lock.unlock();
                }
            }
        }

        /**
         * Gets LockerValue by LockerKey(key) from IRefCountHashMap and locks it (
         * LockerValue.lock()). If locked - registers LockerValue into IFiniter.
         */
        void lock(ILockableObject key);
    }

    /**
     * Transaction outer extender. Finits registered finitables.
     * IFinitableWithRollback interface is supported.
     */
    public interface IFiniter {

        /**
         * IFinitable can also be of IFinitableWithRollback instance
         */
        void registerFiniter(IFinitable finiter);

        Set<IFinitable> getRegisteredFiniters();

        /**
         * Finits and remove all registered finitables. Finiters are called
         * INSIDE try/catch blocks. if !bCommit and finiter is instanse of
         * IFinitableWithRollback then rollback method is called.
         */
        void flush(boolean bCommit);
    }

}
